import { useState, useCallback } from 'react';

/** linear-ui-skills: ダークモードフィルタ */

const STOCK_INDICES = [
  { value: '', label: 'すべて' },
  { value: 'N225', label: '日経225' },
  { value: 'SPX', label: 'S&P500' },
  { value: 'IXIC', label: 'NASDAQ' },
  { value: 'GDAXI', label: 'DAX' },
];

interface SummaryFilterProps {
  indexSymbol: string;
  keyword: string;
  onIndexChange: (value: string) => void;
  onKeywordChange: (value: string) => void;
}

export function SummaryFilter({
  indexSymbol,
  keyword,
  onIndexChange,
  onKeywordChange,
}: SummaryFilterProps) {
  const [localKeyword, setLocalKeyword] = useState(keyword);

  const handleKeywordSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      onKeywordChange(localKeyword);
    },
    [localKeyword, onKeywordChange]
  );

  const handleKeywordKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Enter') {
        onKeywordChange(localKeyword);
      }
    },
    [localKeyword, onKeywordChange]
  );

  return (
    <div className="flex flex-wrap items-center" style={{ gap: '12px' }}>
      {/* Stock Index フィルタ */}
      <div className="flex" style={{ gap: '4px' }}>
        {STOCK_INDICES.map((idx) => (
          <button
            key={idx.value}
            type="button"
            onClick={() => onIndexChange(idx.value)}
            style={{
              fontSize: '12px',
              fontWeight: indexSymbol === idx.value ? 500 : 400,
              color: indexSymbol === idx.value ? '#E2E4E3' : '#525456',
              backgroundColor: indexSymbol === idx.value ? 'rgba(255, 255, 255, 0.08)' : 'transparent',
              padding: '4px 10px',
              borderRadius: '6px',
              border: '1px solid',
              borderColor: indexSymbol === idx.value ? 'rgba(255, 255, 255, 0.12)' : 'transparent',
              cursor: 'pointer',
            }}
            aria-pressed={indexSymbol === idx.value}
          >
            {idx.label}
          </button>
        ))}
      </div>

      {/* キーワード検索 */}
      <form onSubmit={handleKeywordSubmit} className="flex-1" style={{ minWidth: '200px' }}>
        <input
          type="search"
          value={localKeyword}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setLocalKeyword(e.target.value)}
          onKeyDown={handleKeywordKeyDown}
          placeholder="キーワード検索..."
          className="w-full px-3 py-2 outline-none"
          style={{
            fontSize: '13px',
            color: '#E2E4E3',
            backgroundColor: 'rgba(255, 255, 255, 0.04)',
            borderRadius: '6px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
          }}
          aria-label="要約をキーワードで検索"
        />
      </form>
    </div>
  );
}
