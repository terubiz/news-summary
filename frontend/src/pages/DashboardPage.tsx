import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { SummaryList } from '../components/summary/SummaryList';
import { useSseStream } from '../hooks/useSseStream';
import { useAuth } from '../hooks/useAuth';
import { ChannelMultiSelector } from '../components/settings/ChannelMultiSelector';
import { api } from '../lib/api';

export default function DashboardPage() {
  const { logout, user } = useAuth();
  const { isConnected, error: sseError } = useSseStream();
  const queryClient = useQueryClient();
  const [sendTarget, setSendTarget] = useState<number | null>(null);
  const [isCollecting, setIsCollecting] = useState(false);
  const [collectResult, setCollectResult] = useState<string | null>(null);
  const [fromDays, setFromDays] = useState(1);

  const dateRangeOptions = [
    { value: 1, label: '1日前' },
    { value: 3, label: '3日前' },
    { value: 7, label: '1週間前' },
    { value: 30, label: '1ヶ月前' },
    { value: 90, label: '3ヶ月前' },
    { value: 180, label: '6ヶ月前' },
    { value: 365, label: '1年前' },
  ] as const;

  const handleSend = useCallback((summaryId: number) => {
    setSendTarget(summaryId);
  }, []);

  const handleCloseSend = useCallback(() => {
    setSendTarget(null);
  }, []);

  const handleCollect = useCallback(async () => {
    setIsCollecting(true);
    setCollectResult(null);
    try {
      const { data } = await api.post<{ message: string }>('/collect', { fromDays });
      setCollectResult(data.message + '（要約生成中...）');

      // 要約生成は非同期のため、SSEのフォールバックとしてポーリングで更新を確認
      const pollInterval = setInterval(() => {
        queryClient.invalidateQueries({ queryKey: ['summaries'] });
      }, 3000);

      // 30秒後にポーリング停止
      setTimeout(() => {
        clearInterval(pollInterval);
        setCollectResult((prev) => prev?.replace('（要約生成中...）', '') ?? null);
      }, 30000);
    } catch {
      setCollectResult('収集に失敗しました');
    } finally {
      setIsCollecting(false);
    }
  }, [fromDays, queryClient]);

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
        {/* 手動収集ボタン */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
          <select
            value={fromDays}
            onChange={(e) => setFromDays(Number(e.target.value))}
            disabled={isCollecting}
            aria-label="記事の取得期間"
            style={{
              fontSize: '14px',
              color: '#E8E8E8',
              backgroundColor: '#1A1D21',
              padding: '10px 12px',
              borderRadius: '6px',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              cursor: isCollecting ? 'not-allowed' : 'pointer',
              opacity: isCollecting ? 0.7 : 1,
              outline: 'none',
            }}
          >
            {dateRangeOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <button
            type="button"
            onClick={handleCollect}
            disabled={isCollecting}
            style={{
              fontSize: '14px',
              fontWeight: 600,
              color: '#FFFFFF',
              backgroundColor: isCollecting ? '#333' : '#2563EB',
              padding: '10px 20px',
              borderRadius: '6px',
              border: 'none',
              cursor: isCollecting ? 'not-allowed' : 'pointer',
              opacity: isCollecting ? 0.7 : 1,
            }}
          >
            {isCollecting ? '収集中...' : '📰 ニュース収集＆要約'}
          </button>
          {collectResult ? (
            <span style={{ fontSize: '13px', color: '#A0A0A0' }}>{collectResult}</span>
          ) : null}
          {fromDays > 30 ? (
            <span style={{ fontSize: '12px', color: '#F59E0B' }}>
              ※ 無料プランは過去30日分のみ取得可能
            </span>
          ) : null}
        </div>

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
