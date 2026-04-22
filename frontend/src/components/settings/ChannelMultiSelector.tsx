import { useState, useCallback } from 'react';
import { useChannels } from '../../hooks/useChannels';
import { api } from '../../lib/api';

/** ダッシュボードから要約を即時送信するためのチャンネル選択モーダル */

const CHANNEL_TYPE_LABELS: Record<string, string> = {
  EMAIL: 'メール',
  SLACK: 'Slack',
  LINE: 'LINE',
  DISCORD: 'Discord',
};

const CHANNEL_TYPE_ICONS: Record<string, string> = {
  EMAIL: '✉️',
  SLACK: '💬',
  LINE: '💚',
  DISCORD: '🎮',
};

interface ChannelMultiSelectorProps {
  summaryId: number;
  onClose: () => void;
}

export function ChannelMultiSelector({ summaryId, onClose }: ChannelMultiSelectorProps) {
  const { data: channels, isLoading } = useChannels();
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [isSending, setIsSending] = useState(false);
  const [result, setResult] = useState<{ success: boolean; message: string } | null>(null);

  const toggleChannel = useCallback((id: number) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id]
    );
  }, []);

  const handleSend = useCallback(async () => {
    if (selectedIds.length === 0) return;
    setIsSending(true);
    setResult(null);
    try {
      await Promise.all(
        selectedIds.map((channelId) =>
          api.post(`/channels/${channelId}/send`, { summaryId })
        )
      );
      setResult({ success: true, message: '送信しました' });
      setTimeout(onClose, 1500);
    } catch {
      setResult({ success: false, message: '送信に失敗しました' });
    } finally {
      setIsSending(false);
    }
  }, [selectedIds, summaryId, onClose]);

  const enabledChannels = channels?.filter((c) => c.enabled) ?? [];

  return (
    <div
      className="fixed inset-0 flex items-center justify-center"
      style={{
        backgroundColor: 'rgba(0, 0, 0, 0.6)',
        zIndex: 50,
      }}
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="送信先チャンネルを選択"
    >
      <div
        className="w-full max-w-sm p-5"
        style={{
          backgroundColor: '#111111',
          borderRadius: '8px',
          border: '1px solid rgba(255, 255, 255, 0.1)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#E2E4E3', marginBottom: '16px' }}>
          送信先を選択
        </h3>

        {isLoading ? (
          <p style={{ fontSize: '13px', color: '#525456' }}>読み込み中...</p>
        ) : enabledChannels.length === 0 ? (
          <p style={{ fontSize: '13px', color: '#525456' }}>
            有効な通知チャンネルがありません。通知設定から追加してください
          </p>
        ) : (
          <div className="flex flex-col" style={{ gap: '8px', marginBottom: '16px' }}>
            {enabledChannels.map((channel) => (
              <label
                key={channel.id}
                className="flex items-center cursor-pointer p-3"
                style={{
                  borderRadius: '6px',
                  border: `1px solid ${
                    selectedIds.includes(channel.id)
                      ? 'rgba(94, 106, 210, 0.4)'
                      : 'rgba(255, 255, 255, 0.08)'
                  }`,
                  backgroundColor: selectedIds.includes(channel.id)
                    ? 'rgba(94, 106, 210, 0.1)'
                    : 'transparent',
                }}
              >
                <input
                  type="checkbox"
                  checked={selectedIds.includes(channel.id)}
                  onChange={() => toggleChannel(channel.id)}
                  className="mr-3"
                />
                <span style={{ fontSize: '16px', marginRight: '8px' }}>
                  {CHANNEL_TYPE_ICONS[channel.channelType] ?? '📢'}
                </span>
                <span style={{ fontSize: '13px', color: '#E2E4E3' }}>
                  {CHANNEL_TYPE_LABELS[channel.channelType] ?? channel.channelType}
                </span>
              </label>
            ))}
          </div>
        )}

        {/* 結果表示 */}
        {result ? (
          <div
            className="mb-3 p-2"
            style={{
              borderRadius: '4px',
              backgroundColor: result.success
                ? 'rgba(117, 184, 138, 0.15)'
                : 'rgba(210, 158, 121, 0.15)',
            }}
          >
            <p
              style={{
                fontSize: '12px',
                color: result.success ? '#75B88A' : '#D29E79',
              }}
            >
              {result.message}
            </p>
          </div>
        ) : null}

        {/* ボタン */}
        <div className="flex items-center justify-end" style={{ gap: '8px' }}>
          <button
            type="button"
            onClick={onClose}
            style={{
              fontSize: '13px',
              color: '#525456',
              backgroundColor: 'transparent',
              padding: '6px 14px',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            キャンセル
          </button>
          <button
            type="button"
            onClick={handleSend}
            disabled={selectedIds.length === 0 || isSending}
            style={{
              fontSize: '13px',
              fontWeight: 600,
              color: '#FFFFFF',
              backgroundColor: '#5E6AD2',
              padding: '6px 16px',
              borderRadius: '6px',
              border: 'none',
              cursor: selectedIds.length === 0 || isSending ? 'not-allowed' : 'pointer',
              opacity: selectedIds.length === 0 || isSending ? 0.5 : 1,
            }}
          >
            {isSending ? '送信中...' : `送信（${selectedIds.length}件）`}
          </button>
        </div>
      </div>
    </div>
  );
}
