import type { Summary, IndexImpact } from '../../hooks/useSummaries';
import { getDisplayName } from '../../hooks/useIndexData';

/** linear-ui-skills: ダークモードカード */

const IMPACT_COLORS: Record<string, string> = {
  POSITIVE: '#75B88A',
  NEGATIVE: '#D29E79',
  NEUTRAL: '#6f6f6f',
};

const IMPACT_LABELS: Record<string, string> = {
  POSITIVE: '上昇',
  NEGATIVE: '下落',
  NEUTRAL: '中立',
};

function formatDate(dateStr: string): string {
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('ja-JP', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateStr;
  }
}

interface ImpactBadgeProps {
  impact: IndexImpact;
}

function ImpactBadge({ impact }: ImpactBadgeProps) {
  const color = IMPACT_COLORS[impact.impactDirection] ?? '#6f6f6f';
  const label = IMPACT_LABELS[impact.impactDirection] ?? impact.impactDirection;

  return (
    <span
      className="inline-flex items-center"
      style={{
        fontSize: '11px',
        fontWeight: 500,
        color,
        backgroundColor: `${color}15`,
        padding: '2px 8px',
        borderRadius: '4px',
        gap: '4px',
      }}
    >
      <span>{getDisplayName(impact.indexSymbol)}</span>
      <span style={{ fontSize: '10px' }}>{label}</span>
    </span>
  );
}

interface SummaryCardProps {
  summary: Summary;
  onSend?: (summaryId: number) => void;
}

export function SummaryCard({ summary, onSend }: SummaryCardProps) {
  return (
    <article
      className="p-4"
      style={{
        backgroundColor: 'rgba(255, 255, 255, 0.03)',
        borderRadius: '8px',
        border: '1px solid rgba(255, 255, 255, 0.06)',
      }}
    >
      {/* 指数影響バッジ */}
      {summary.indexImpacts.length > 0 ? (
        <div className="flex flex-wrap mb-3" style={{ gap: '6px' }}>
          {summary.indexImpacts.map((impact) => (
            <ImpactBadge key={impact.id} impact={impact} />
          ))}
        </div>
      ) : null}

      {/* 要約テキスト */}
      <p
        className="text-pretty"
        style={{
          fontSize: '14px',
          fontWeight: 400,
          color: '#B2B3B3',
          lineHeight: '1.6',
          marginBottom: '12px',
        }}
      >
        {summary.summaryText}
      </p>

      {/* フッター: 日時 + 送信ボタン */}
      <div className="flex items-center justify-between">
        <time
          dateTime={summary.generatedAt}
          style={{
            fontSize: '12px',
            color: '#525456',
          }}
        >
          {formatDate(summary.generatedAt)}
        </time>
        <div className="flex items-center" style={{ gap: '8px' }}>
          <span
            style={{
              fontSize: '11px',
              color: '#525456',
              backgroundColor: 'rgba(255, 255, 255, 0.05)',
              padding: '2px 6px',
              borderRadius: '3px',
            }}
          >
            {summary.summaryMode}
          </span>
          {onSend ? (
            <button
              type="button"
              onClick={() => onSend(summary.id)}
              className="transition-colors"
              style={{
                fontSize: '12px',
                fontWeight: 500,
                color: '#5E6AD2',
                backgroundColor: 'rgba(94, 106, 210, 0.1)',
                padding: '4px 10px',
                borderRadius: '4px',
                border: 'none',
                cursor: 'pointer',
              }}
              aria-label={`要約 ${summary.id} を送信`}
            >
              送信
            </button>
          ) : null}
        </div>
      </div>
    </article>
  );
}
