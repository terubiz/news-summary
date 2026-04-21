---
name: junit5-property-testing
description: JUnit5 + jqwik property-based testing patterns for Kotlin and Spring Boot. Use when writing property tests, generating test data with @ForAll, or verifying invariants. Covers Arbitraries, @Property, @Example, and Spring integration.
---

## JUnit5 + jqwik Property-Based Testing

jqwik は JUnit Platform 上で動作するプロパティベーステストライブラリ。
Spring Boot + Kotlin プロジェクトでは `spring-jqwik` 拡張を使用する。

### 依存関係（build.gradle.kts）

```kotlin
testImplementation("net.jqwik:jqwik:1.9.x")
testImplementation("net.jqwik:jqwik-kotlin:1.9.x")
testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:1.x.x") // 任意
```

### 基本的なプロパティテスト

```kotlin
import net.jqwik.api.*
import net.jqwik.api.constraints.*
import org.assertj.core.api.Assertions.assertThat

class NewsArticlePropertyTest {

    @Property(tries = 100)
    fun `重複URLは保存されない`(
        @ForAll @UniqueElements @Size(min = 1, max = 50) urls: List<@StringLength(min = 10, max = 200) String>
    ) {
        val saved = newsCollectorService.saveArticles(urls.map { buildArticle(url = it) })
        assertThat(saved.map { it.sourceUrl }.distinct()).hasSameSizeAs(saved)
    }
}
```

### カスタム Arbitrary（ジェネレータ）

```kotlin
@Provide
fun newsArticles(): Arbitrary<NewsArticle> {
    val titles = Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(100)
    val urls = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50)
        .map { "https://example.com/$it" }
    return Combinators.combine(titles, urls)
        .`as` { title, url -> NewsArticle(title = title, sourceUrl = url) }
}

@Property(tries = 100)
fun `要約文字数は設定モードの上限を超えない`(
    @ForAll("newsArticles") article: NewsArticle,
    @ForAll summaryMode: SummaryMode
) {
    val summary = aiSummarizerService.summarize(listOf(article), settings.copy(summaryMode = summaryMode))
    assertThat(summary.bodyText.length).isLessThanOrEqualTo(summaryMode.charLimit)
}
```

### アノテーション一覧

| アノテーション | 用途 |
|---|---|
| `@Property(tries = 100)` | プロパティテストメソッド（最低100回実行） |
| `@Example` | 例示ベーステスト（JUnit5の`@Test`相当） |
| `@ForAll` | ランダム生成パラメータ |
| `@Provide` | カスタムArbitraryファクトリ |
| `@UniqueElements` | コレクション内の重複排除 |
| `@Size(min, max)` | コレクションサイズ制約 |
| `@StringLength(min, max)` | 文字列長制約 |
| `@IntRange(min, max)` | 整数範囲制約 |
| `@Positive` / `@Negative` | 正数・負数制約 |

### Spring Boot との統合

```kotlin
@SpringBootTest
@ExtendWith(SpringExtension::class)
class SummaryServicePropertyTest(
    @Autowired private val summaryService: SummaryService
) {

    @Property(tries = 100)
    fun `上級者向け設定では用語解説セクションが付加されない`(
        @ForAll("newsArticles") article: NewsArticle
    ) {
        val settings = SummarySettings(supplementLevel = SupplementLevel.ADVANCED)
        val summary = summaryService.summarize(listOf(article), settings)
        assertThat(summary.summaryText).doesNotContain("用語解説")
    }
}
```

### テスト命名規則

本プロジェクトでは以下の形式でタグを付与する：
`Feature: economic-news-ai-summarizer, Property {番号}: {プロパティ内容}`

### ベストプラクティス

- `@Property(tries = 100)` を最低値として設定（CI環境では50でも可）
- 失敗時は jqwik が自動的に入力を縮小（shrinking）して最小再現ケースを提示
- `@Example` と `@Property` を組み合わせて境界値も明示的にテスト
- `Arbitraries.of(enumValues)` で Enum の全値をカバー
- Spring コンテキストが重い場合は `@SpringBootTest` を統合テストクラスに分離
