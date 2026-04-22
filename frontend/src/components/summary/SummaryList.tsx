import { useState, useCallback } from 'react';
import { useSummaries } from '../../hooks/useSummaries';
import { SummaryCard } from './SummaryCard';
import { SummaryFilter } from './SummaryFilter';

/** linear-ui-skills: ダークモード要約一覧 */

const PAGE_SIZE = 20;

interface SummaryListProps {
  onSend?: (summaryId: number) => void;
}

export function SummaryList({ onSend }: SummaryListProps) {
  const [page, setPage] = useState(0);
  const [indexSymbol, setIndexSymbol] = useState('');
  const [keyword, setKeyword] = useState('');

  const { data, isLoading, isError } = useSummaries({
    page,
    size: PAGE_SIZE,
    indexSymbol: indexSymbol || undefined,
    keyword: keyword || undefined,
  });

  const handleIndexChange = useCallback((value: string) => {
    setIndexSymbol(value);
    setPage(0);
  }, []);

  const handleKeywordChange = useCallback((value: string) => {
    setKeyword(value);
    setPage(0);
  }, []);

  return (
    <div>
      {/* フィルタ */}
      <div style={{ marginBottom: '16px' }}>
        <SummaryFilter
          indexSymbol={indexSymbol}
          keyword={keyword}
          onIndexChange={handleIndexChange}
          onKeywordChange={handleKeywordChange}
        />
      </div>

      {/* ローディング */}
      {isLoading ? (
        <div className="flex flex-col" style={{ gap: '12px' }}>
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="p-4"
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                borderRadius: '8px',
                border: '1px solid rgba(255, 255, 255, 0.06)',
              }}
            >
              <div
                className="rounded"
                style={{ width: '60%', height: '14px', backgroundColor: 'rgba(255,255,255,0.06)', marginBottom: '12px' }}
              />
              <div
                className="rounded"
                style={{ width: '90%', height: '14px', backgroundColor: 'rgba(255,255,255,0.04)', marginBottom: '8px' }}
              />
              <div
                className="rounded"
                style={{ width: '40%', height: '12px', backgroundColor: 'rgba(255,255,255,0.04)' }}
              />
            </div>
          ))}
        </div>
      ) : null}

      {/* エラー */}
      {isError ? (
        <div
          className="p-4"
          style={{
            backgroundColor: 'rgba(210, 158, 121, 0.1)',
            borderRadius: '8px',
            border: '1px solid rgba(210, 158, 121, 0.2)',
          }}
        >
          <p style={{ fontSize: '13px', color: '#D29E79' }}>
            要約データの取得に失敗しました
          </p>
        </div>
      ) : null}

      {/* 要約カード一覧 */}
      {data ? (
        <>
          {data.content.length === 0 ? (
            <div className="py-12 text-center">
              <p style={{ fontSize: '14px', color: '#525456' }}>
                {keyword || indexSymbol ? '条件に一致する要約がありません' : '要約がまだありません'}
              </p>
            </div>
          ) : (
            <div className="flex flex-col" style={{ gap: '12px' }}>
              {data.content.map((summary) => (
                <SummaryCard key={summary.id} summary={summary} onSend={onSend} />
              ))}
            </div>
          )}

          {/* ページネーション */}
          {data.totalPages > 1 ? (
            <div className="flex items-center justify-center mt-6" style={{ gap: '8px' }}>
              <button
                type="button"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{
                  fontSize: '13px',
                  color: page === 0 ? '#525456' : '#B2B3B3',
                  backgroundColor: 'rgba(255, 255, 255, 0.04)',
                  padding: '6px 12px',
                  borderRadius: '6px',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  cursor: page === 0 ? 'not-allowed' : 'pointer',
                  opacity: page === 0 ? 0.5 : 1,
                }}
                aria-label="前のページ"
              >
                前へ
              </button>
              <span style={{ fontSize: '13px', color: '#525456' }}>
                {page + 1} / {data.totalPages}
              </span>
              <button
                type="button"
                onClick={() => setPage((p) => Math.min(data.totalPages - 1, p + 1))}
                disabled={page >= data.totalPages - 1}
                style={{
                  fontSize: '13px',
                  color: page >= data.totalPages - 1 ? '#525456' : '#B2B3B3',
                  backgroundColor: 'rgba(255, 255, 255, 0.04)',
                  padding: '6px 12px',
                  borderRadius: '6px',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  cursor: page >= data.totalPages - 1 ? 'not-allowed' : 'pointer',
                  opacity: page >= data.totalPages - 1 ? 0.5 : 1,
                }}
                aria-label="次のページ"
              >
                次へ
              </button>
            </div>
          ) : null}
        </>
      ) : null}
    </div>
  );
}
