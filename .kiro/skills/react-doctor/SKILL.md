---
name: react-doctor
description: React code quality diagnostics using react-doctor CLI. Use when reviewing React code quality, diagnosing performance or architecture issues, or running a health check on the frontend codebase. Checks 60+ rules across state, effects, performance, bundle size, security, correctness, and accessibility.
compatibility: Requires Node.js 18+. Run at the frontend project root directory.
---

## React Doctor — コード品質診断

[react-doctor](https://github.com/millionco/react-doctor) は React コードベースを診断し、0〜100 のヘルススコアと改善アクションを出力するツール。

### 実行方法

```bash
# フロントエンドプロジェクトルートで実行
npx -y react-doctor@latest .

# 詳細（ファイル・行番号付き）
npx -y react-doctor@latest . --verbose

# スコアのみ出力
npx -y react-doctor@latest . --score
```

### スコア基準

| スコア | 評価 |
|---|---|
| 75〜100 | Great ✅ |
| 50〜74 | Needs work ⚠️ |
| 0〜49 | Critical 🚨 |

### チェックカテゴリ（60+ルール）

1. **State & Effects** — 不正な `useEffect` 依存配列、無限ループリスク
2. **Performance** — 不要な再レンダリング、メモ化漏れ
3. **Architecture** — コンポーネント内コンポーネント定義、循環依存
4. **Bundle Size** — バレルインポート、未使用エクスポート
5. **Security** — `dangerouslySetInnerHTML` の不適切な使用
6. **Correctness** — key prop 漏れ、条件付きフック呼び出し
7. **Accessibility** — aria属性漏れ、セマンティックHTML違反

### 本プロジェクトでの利用方針

- 実装タスク完了後、フロントエンドコードに対して実行する
- スコア 75 以上を目標とする
- `error` 重大度の診断は必ず修正してからコミットする
- `warning` は優先度に応じて対応する

### 設定ファイル（react-doctor.config.json）

```json
{
  "ignore": {
    "rules": [],
    "files": ["src/generated/**"]
  },
  "failOn": "error"
}
```

### 診断結果の読み方

```
✗ react/no-array-index-key (error)
  src/components/summary/SummaryList.tsx:42
  Do not use Array index in keys
  → Use stable unique IDs (e.g., summary.id) as React keys
```

各診断には `filePath`, `rule`, `severity`, `message`, `help`, `line` が含まれる。
