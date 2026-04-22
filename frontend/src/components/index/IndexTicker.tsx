import { useIndexData, getDisplayName } from '../../hooks/useIndexData';
import type { IndexData } from '../../hooks/useIndexData';

/** stripe-ui-skills: 上昇=#75B88A, 下落=#D29E79 */
const COLOR_UP = '#75B88A';
const COLOR_DOWN = '#D29E79';
const COLOR_NEUTRAL = '#6f6f6f';

function getChangeColor(value: number): string {
  if (value > 0) return COLOR_UP;
  if (value < 0) return COLOR_DOWN;
  return COLOR_NEUTRAL;
}

function formatNumber(value: number): string {
  return value.toLocaleString('ja-JP', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatChange(value: number): string {
  const prefix = value > 0 ? '+' : '';
  return `${prefix}${formatNumber(value)}`;
}

function formatRate(value: number): string {
  const prefix = value > 0 ? '+' : '';
  return `${prefix}${value.toFixed(2)}%`;
}

interface IndexTickerItemProps {
  data: IndexData;
}

function IndexTickerItem({ data }: IndexTickerItemProps) {
  const color = getChangeColor(data.changeAmount);

  return (
    <div
      className="flex flex-col px-5 py-3"
      style={{
        minWidth: '180px',
        borderRight: '1px solid rgba(255, 255, 255, 0.08)',
      }}
    >
      <div className="flex items-center" style={{ gap: '6px', marginBottom: '4px' }}>
        <span
          style={{
            fontSize: '12px',
            fontWeight: 500,
            color: '#B2B3B3',
            letterSpacing: 0,
          }}
        >
          {getDisplayName(data.symbol)}
        </span>
        {data.isStale ? (
          <span
            style={{
              fontSize: '12px',
              color: '#FAE6C2',
              backgroundColor: 'rgba(250, 230, 194, 0.15)',
              padding: '1px 4px',
              borderRadius: '3px',
            }}
          >
            stale
          </span>
        ) : null}
      </div>
      <span
        className="tabular-nums"
        style={{
          fontSize: '16px',
          fontWeight: 600,
          color: '#E2E4E3',
          fontVariantNumeric: 'tabular-nums',
        }}
      >
        {formatNumber(data.currentValue)}
      </span>
      <div className="flex items-center" style={{ gap: '8px', marginTop: '2px' }}>
        <span
          className="tabular-nums"
          style={{
            fontSize: '12px',
            fontWeight: 400,
            color,
            fontVariantNumeric: 'tabular-nums',
          }}
        >
          {formatChange(data.changeAmount)}
        </span>
        <span
          className="tabular-nums"
          style={{
            fontSize: '12px',
            fontWeight: 400,
            color,
            fontVariantNumeric: 'tabular-nums',
          }}
        >
          {formatRate(data.changeRate)}
        </span>
      </div>
    </div>
  );
}

export function IndexTicker() {
  const { data: indices, isLoading, isError } = useIndexData();

  if (isLoading) {
    return (
      <div
        className="flex overflow-x-auto"
        style={{
          backgroundColor: 'rgba(255, 255, 255, 0.03)',
          borderRadius: '8px',
          border: '1px solid rgba(255, 255, 255, 0.06)',
        }}
      >
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex flex-col px-5 py-3" style={{ minWidth: '180px' }}>
            <div
              className="rounded"
              style={{ width: '60px', height: '12px', backgroundColor: 'rgba(255,255,255,0.06)', marginBottom: '8px' }}
            />
            <div
              className="rounded"
              style={{ width: '100px', height: '16px', backgroundColor: 'rgba(255,255,255,0.06)', marginBottom: '4px' }}
            />
            <div
              className="rounded"
              style={{ width: '80px', height: '12px', backgroundColor: 'rgba(255,255,255,0.06)' }}
            />
          </div>
        ))}
      </div>
    );
  }

  if (isError) {
    return (
      <div
        className="px-5 py-3"
        style={{
          backgroundColor: 'rgba(210, 158, 121, 0.1)',
          borderRadius: '8px',
          border: '1px solid rgba(210, 158, 121, 0.2)',
        }}
      >
        <p style={{ fontSize: '13px', color: COLOR_DOWN }}>
          株価指数データの取得に失敗しました
        </p>
      </div>
    );
  }

  return (
    <div
      className="flex overflow-x-auto"
      style={{
        backgroundColor: 'rgba(255, 255, 255, 0.03)',
        borderRadius: '8px',
        border: '1px solid rgba(255, 255, 255, 0.06)',
      }}
    >
      {indices?.map((index) => (
        <IndexTickerItem key={index.symbol} data={index} />
      ))}
    </div>
  );
}
