import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { SummaryList } from '../components/summary/SummaryList';
import { useSseStream } from '../hooks/useSseStream';
import { useAuth } from '../hooks/useAuth';
import { ChannelMultiSelector } from '../components/settings/ChannelMultiSelector';

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
        backgroundColor: '#0F1114',
        fontFamily: 'Inter, system-ui, sans-serif',
        color: '#E8E8E8',
      }}
    >
      {/* ヘッダー */}
      <header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '12px 24px',
          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <h1 style={{ fontSize: '18px', fontWeight: 700, color: '#FFFFFF', margin: 0 }}>
            News Summary
          </h1>
          <span
            style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: isConnected ? '#4ADE80' : '#F87171',
              display: 'inline-block',
            }}
            title={isConnected ? 'リアルタイム接続中' : '接続切断'}
          />
        </div>
        <nav style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <Link
            to="/settings/summary"
            style={{ fontSize: '13px', color: '#A0A0A0', textDecoration: 'none' }}
          >
            要約設定
          </Link>
          <Link
            to="/settings/notifications"
            style={{ fontSize: '13px', color: '#A0A0A0', textDecoration: 'none' }}
          >
            通知設定
          </Link>
          {user ? (
            <span style={{ fontSize: '12px', color: '#808080' }}>
              {user.email}
            </span>
          ) : null}
          <button
            type="button"
            onClick={logout}
            style={{
              fontSize: '12px',
              color: '#A0A0A0',
              backgroundColor: 'transparent',
              border: '1px solid rgba(255,255,255,0.15)',
              borderRadius: '4px',
              padding: '4px 10px',
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
          style={{
            margin: '16px 24px 0',
            padding: '10px 16px',
            backgroundColor: 'rgba(248, 113, 113, 0.1)',
            borderRadius: '6px',
            border: '1px solid rgba(248, 113, 113, 0.2)',
          }}
        >
          <p style={{ fontSize: '13px', color: '#F87171', margin: 0 }}>{sseError}</p>
        </div>
      ) : null}

      <div style={{ padding: '24px' }}>
        <section>
          <h2
            style={{
              fontSize: '16px',
              fontWeight: 600,
              color: '#FFFFFF',
              marginBottom: '16px',
            }}
          >
            最新の要約
          </h2>
          <SummaryList onSend={handleSend} />
        </section>
      </div>

      {sendTarget !== null ? (
        <ChannelMultiSelector
          summaryId={sendTarget}
          onClose={handleCloseSend}
        />
      ) : null}
    </main>
  );
}
