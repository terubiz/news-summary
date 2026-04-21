---
name: react-typescript
description: React 18 + TypeScript best practices for Vite SPA (non-Next.js). Use when creating React components, hooks, or frontend features. Covers component patterns, state management with TanStack Query and Zustand, TypeScript typing, performance optimization, and accessibility. Incorporates Vercel react-best-practices rules adapted for Vite/SPA context.
---

## React + TypeScript Best Practices（Vite SPA向け）

本スキルは Vercel の `react-best-practices` ルールを Vite + React SPA 向けに適用したものです。
Next.js 固有のルール（RSC、Server Actions、`next/dynamic` 等）は除外しています。

---

### コンポーネント設計

- 関数コンポーネント + hooks を使用（クラスコンポーネント不使用）
- named export を優先
- props インターフェースを明示的に定義

```tsx
interface SummaryCardProps {
  summary: Summary;
  onSelect: (id: number) => void;
}

export function SummaryCard({ summary, onSelect }: SummaryCardProps) {
  return (
    <article onClick={() => onSelect(summary.id)}>
      <h2>{summary.title}</h2>
    </article>
  );
}
```

### TypeScript

- `React.FC` は使わず明示的な型付けを優先
- イベントハンドラ: `React.ChangeEvent<HTMLInputElement>` 等を明示
- ジェネリクスで再利用可能なコンポーネントを作成
- `unknown` を `any` の代わりに使用

### 状態管理

- ローカル状態: `useState` / `useReducer`
- サーバー状態: **TanStack Query**（キャッシュ・再取得・楽観的更新）
- グローバル状態: **Zustand**（設定値、認証状態）
- フォーム状態: React Hook Form

### パフォーマンス最適化（Vercel react-best-practices より）

**ウォーターフォール排除（CRITICAL）**
- 独立したデータ取得は `Promise.all()` で並列実行
- `useQuery` を複数使う場合は `useQueries` で並列化

**バンドルサイズ（CRITICAL）**
- バレルファイル（`index.ts` の再エクスポート）を避け、直接インポート
- 重いコンポーネントは `React.lazy` + `Suspense` で遅延ロード
- サードパーティライブラリ（分析系等）は初期ロード後に遅延ロード

**再レンダリング最適化（MEDIUM）**
- コールバック内でのみ使う状態は購読しない（`rerender-defer-reads`）
- 高コストなコンポーネントは `React.memo` でメモ化
- デフォルト値のオブジェクト・配列はコンポーネント外に定義
- `useEffect` の依存配列にはプリミティブ値を使用
- コンポーネント内でコンポーネントを定義しない（`rerender-no-inline-components`）
- `&&` の代わりに三項演算子を使用（`rendering-conditional-render`）

### カスタムフック

```tsx
// SSEストリーム購読フック
export function useSseStream(url: string) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const es = new EventSource(url);
    es.onmessage = (e) => {
      const summary = JSON.parse(e.data);
      queryClient.invalidateQueries({ queryKey: ['summaries'] });
    };
    return () => es.close();
  }, [url, queryClient]);
}
```

### エラーハンドリング

- `ErrorBoundary` でコンポーネントツリーをラップ
- TanStack Query の `onError` でAPIエラーをハンドリング
- バックエンド接続失敗時は最後に取得したキャッシュデータを表示

### アクセシビリティ

- セマンティックHTML要素を使用（`<button>`, `<nav>`, `<main>` 等）
- インタラクティブ要素に `aria-label` を付与
- キーボードナビゲーションを確保
- カラーコントラスト比 4.5:1 以上を維持

### テスト（Vitest + React Testing Library）

```tsx
import { render, screen } from '@testing-library/react';
import { SummaryCard } from './SummaryCard';

test('要約テキストが表示される', () => {
  render(<SummaryCard summary={mockSummary} onSelect={vi.fn()} />);
  expect(screen.getByText(mockSummary.title)).toBeInTheDocument();
});
```
