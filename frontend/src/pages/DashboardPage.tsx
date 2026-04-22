import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { SummaryList } from '../components/summary/SummaryList';
import { useSseStream } from '../hooks/useSseStream';
import { useAuth } from '../hooks/useAuth';
import { ChannelMultiSelector } from '../components/settings/ChannelMultiSelector';

/** linear-ui-skills: #080A0A背景, Inter font, ダークモード */

export default function DashboardPage() {
  const { logout, user } = useAuth();
  const { isConnected, error: sseError } = useSseStream();
  const [sendTarget, setSendTarget] = useState<number | null>(null);

  const handleSend = useCallback((summaryId: number) => {
    setSendTarget(summaryId);
  }, []);

  const handleCloseSend = useCallback(() => {
    setSendTarget(null);
  }, []);

  return (
    <main
      className="min-h-dvh"
      style={{
        backgroundColor: '#080A0A',
        fontFamily: 'Inter, system-ui, sans-serif',
        color: '#E2E4E3',
      }}
    >
      {/* ヘッダー */}
      <header
        className="flex items-center justify-between px-6 py-4"
        style={{
          borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        }}
      >
        <div className="flex items-center" style={{ gap: '16px' }}>
          <h1 style={{ fontSize: '17px', fontWeight: 600 }}>
            Economic News AI
          </h1>
          {/* SSE接続ステータス */}
          <span
            style={{
              width: '6px',
              height: '6px',
              borderRadius: '50%',
              backgroundColor: isConnected ? '#75B88A' : '#D29E79',
              display: 'inline-block',
            }}
            title={isConnected ? 'リアルタイム接続中' : '接続切断'}
          />
        </div>
        <nav className="flex items-center" style={{ gap: '16px' }}>
          <Link
            to="/settings/summary"
            style={{
              fontSize: '13px',
              color: '#525456',
              textDecoration: 'none',
            }}
          >
            要約設定
          </Link>
          <Link
            to="/settings/notifications"
            style={{
              fontSize: '13px',
              color: '#525456',
              textDecoration: 'none',
            }}
          >
            通知設定
          </Link>
          {user ? (
            <span style={{ fontSize: '12px', color: '#444749' }}>
              {user.email}
            </span>
          ) : null}
          <button
            type="button"
            onClick={logout}
            style={{
              fontSize: '12px',
              color: '#525456',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
            }}
            aria-label="ログアウト"
          >
            ログアウト
          </button>
        </nav>
      </header>

      {/* SSEエラー表示 */}
      {sseError ? (
        <div
          className="mx-6 mt-4 px-4 py-3"
          style={{
            backgroundColor: 'rgba(210, 158, 121, 0.1)',
            borderRadius: '6px',
            border: '1px solid rgba(210, 158, 121, 0.2)',
          }}
        >
          <p style={{ fontSize: '13px', color: '#D29E79' }}>{sseError}</p>
        </div>
      ) : null}

      <div className="px-6 py-5">
        {/* 要約一覧 */}
        <section>
          <h2
            style={{
              fontSize: '15px',
              fontWeight: 500,
              color: '#B2B3B3',
              marginBottom: '16px',
            }}
          >
            最新の要約
          </h2>
          <SummaryList onSend={handleSend} />
        </section>
      </div>

      {/* 送信モーダル */}
      {sendTarget !== null ? (
        <ChannelMultiSelector
          summaryId={sendTarget}
          onClose={handleCloseSend}
        />
      ) : null}
    </main>
  );
}
