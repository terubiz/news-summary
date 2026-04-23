import { useState, useCallback } from 'react';

const STOCK_INDICES = [
  { value: 'NKX', label: '日経225' },
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

  return (
    <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
      {/* 指数フィルタ（複数選択風チェックボックス） */}
      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
        <button
          type="button"
          onClick={() => onIndexChange('')}
          style={{
            fontSize: '13px',
            fontWeight: !indexSymbol ? 600 : 400,
            color: !indexSymbol ? '#FFFFFF' : '#A0A0A0',
            backgroundColor: !indexSymbol ? 'rgba(255,255,255,0.12)' : 'transparent',
            padding: '6px 14px',
            borderRadius: '6px',
            border: `1px solid ${!indexSymbol ? 'rgba(255,255,255,0.2)' : 'rgba(255,255,255,0.1)'}`,
            cursor: 'pointer',
          }}
          aria-pressed={!indexSymbol}
        >
          すべて
        </button>
        {STOCK_INDICES.map((idx) => (
          <button
            key={idx.value}
            type="button"
            onClick={() => onIndexChange(indexSymbol === idx.value ? '' : idx.value)}
            style={{
              fontSize: '13px',
              fontWeight: indexSymbol === idx.value ? 600 : 400,
              color: indexSymbol === idx.value ? '#FFFFFF' : '#A0A0A0',
              backgroundColor: indexSymbol === idx.value ? 'rgba(255,255,255,0.12)' : 'transparent',
              padding: '6px 14px',
              borderRadius: '6px',
              border: `1px solid ${indexSymbol === idx.value ? 'rgba(255,255,255,0.2)' : 'rgba(255,255,255,0.1)'}`,
              cursor: 'pointer',
            }}
            aria-pressed={indexSymbol === idx.value}
          >
            {idx.label}
          </button>
        ))}
      </div>

      {/* キーワード検索 */}
      <form onSubmit={handleKeywordSubmit} style={{ flex: 1, minWidth: '200px' }}>
        <input
          type="search"
          value={localKeyword}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => setLocalKeyword(e.target.value)}
          placeholder="キーワード検索..."
          style={{
            width: '100%',
            fontSize: '13px',
            color: '#E8E8E8',
            backgroundColor: 'rgba(255, 255, 255, 0.06)',
            borderRadius: '6px',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            padding: '8px 12px',
            outline: 'none',
          }}
          aria-label="要約をキーワードで検索"
        />
      </form>
    </div>
  );
}
