import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  useChannels,
  useCreateChannel,
  useUpdateChannel,
  useDeleteChannel,
  useTestChannel,
} from '../hooks/useChannels';
import { ChannelSettingsForm } from '../components/settings/ChannelSettingsForm';
import type { Channel, CreateChannelRequest } from '../hooks/useChannels';

/** vercel-ui-skills: ライトモード通知設定画面 */

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

const SCHEDULE_LABELS: Record<string, string> = {
  IMMEDIATE: '即時',
  HOURLY: '毎時',
  DAILY_09: '毎日 9:00',
  DAILY_18: '毎日 18:00',
};

export default function NotificationSettingsPage() {
  const { data: channels, isLoading, isError } = useChannels();
  const createMutation = useCreateChannel();
  const updateMutation = useUpdateChannel();
  const deleteMutation = useDeleteChannel();
  const testMutation = useTestChannel();

  const [showForm, setShowForm] = useState(false);
  const [editingChannel, setEditingChannel] = useState<Channel | undefined>(undefined);
  const [testResult, setTestResult] = useState<{ success: boolean } | null>(null);

  const handleCreate = useCallback(
    (data: CreateChannelRequest) => {
      createMutation.mutate(data, {
        onSuccess: () => {
          setShowForm(false);
          setEditingChannel(undefined);
        },
      });
    },
    [createMutation]
  );

  const handleUpdate = useCallback(
    (data: CreateChannelRequest) => {
      if (!editingChannel) return;
      updateMutation.mutate(
        { id: editingChannel.id, ...data },
        {
          onSuccess: () => {
            setShowForm(false);
            setEditingChannel(undefined);
          },
        }
      );
    },
    [editingChannel, updateMutation]
  );

  const handleDelete = useCallback(
    (id: number) => {
      deleteMutation.mutate(id);
    },
    [deleteMutation]
  );

  const handleTest = useCallback(
    (id: number) => {
      setTestResult(null);
      testMutation.mutate(id, {
        onSuccess: (success) => setTestResult({ success }),
        onError: () => setTestResult({ success: false }),
      });
    },
    [testMutation]
  );

  const handleEdit = useCallback((channel: Channel) => {
    setEditingChannel(channel);
    setShowForm(true);
    setTestResult(null);
  }, []);

  const handleCancel = useCallback(() => {
    setShowForm(false);
    setEditingChannel(undefined);
    setTestResult(null);
  }, []);

  return (
    <main
      className="min-h-dvh"
      style={{
        backgroundColor: '#F9F9F9',
        fontFamily: 'Inter, system-ui, sans-serif',
      }}
    >
      {/* ヘッダー */}
      <header
        className="flex items-center justify-between px-6 py-4"
        style={{ borderBottom: '1px solid #C8CDD1' }}
      >
        <div className="flex items-center" style={{ gap: '12px' }}>
          <Link
            to="/dashboard"
            style={{ fontSize: '13px', color: '#797979', textDecoration: 'none' }}
          >
            ← ダッシュボード
          </Link>
          <h1 style={{ fontSize: '17px', fontWeight: 600, color: '#1B1B1B' }}>
            通知設定
          </h1>
        </div>
        {!showForm ? (
          <button
            type="button"
            onClick={() => {
              setEditingChannel(undefined);
              setShowForm(true);
              setTestResult(null);
            }}
            style={{
              fontSize: '13px',
              fontWeight: 600,
              color: '#FFFFFF',
              backgroundColor: '#000001',
              padding: '6px 16px',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            チャンネルを追加
          </button>
        ) : null}
      </header>

      <div className="max-w-2xl mx-auto px-6 py-8">
        {/* フォーム */}
        {showForm ? (
          <div style={{ marginBottom: '24px' }}>
            <ChannelSettingsForm
              editingChannel={editingChannel}
              onSubmit={editingChannel ? handleUpdate : handleCreate}
              onCancel={handleCancel}
              isSubmitting={createMutation.isPending || updateMutation.isPending}
              onTest={editingChannel ? handleTest : undefined}
              testResult={testResult}
              isTesting={testMutation.isPending}
            />
          </div>
        ) : null}

        {/* ローディング */}
        {isLoading ? (
          <div className="py-12 text-center">
            <p style={{ fontSize: '14px', color: '#797979' }}>読み込み中...</p>
          </div>
        ) : null}

        {/* エラー */}
        {isError ? (
          <div className="py-12 text-center">
            <p style={{ fontSize: '14px', color: '#DC2626' }}>チャンネル情報の取得に失敗しました</p>
          </div>
        ) : null}

        {/* チャンネル一覧 */}
        {channels ? (
          <>
            {channels.length === 0 && !showForm ? (
              <div className="py-12 text-center">
                <p style={{ fontSize: '14px', color: '#797979' }}>
                  通知チャンネルがまだ設定されていません
                </p>
              </div>
            ) : (
              <div className="flex flex-col" style={{ gap: '12px' }}>
                {channels.map((channel) => (
                  <div
                    key={channel.id}
                    className="flex items-center justify-between p-4"
                    style={{
                      backgroundColor: '#FFFFFF',
                      borderRadius: '8px',
                      border: '1px solid #C8CDD1',
                      opacity: channel.enabled ? 1 : 0.5,
                    }}
                  >
                    <div className="flex items-center" style={{ gap: '12px' }}>
                      <span style={{ fontSize: '20px' }}>
                        {CHANNEL_TYPE_ICONS[channel.channelType] ?? '📢'}
                      </span>
                      <div>
                        <p style={{ fontSize: '14px', fontWeight: 500, color: '#1B1B1B' }}>
                          {CHANNEL_TYPE_LABELS[channel.channelType] ?? channel.channelType}
                        </p>
                        <p style={{ fontSize: '12px', color: '#929292' }}>
                          {SCHEDULE_LABELS[channel.deliverySchedule] ?? channel.deliverySchedule}
                          {channel.filterIndices.length > 0
                            ? ` · ${channel.filterIndices.join(', ')}`
                            : ' · すべての指数'}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center" style={{ gap: '8px' }}>
                      <button
                        type="button"
                        onClick={() => handleEdit(channel)}
                        style={{
                          fontSize: '12px',
                          color: '#797979',
                          backgroundColor: 'transparent',
                          padding: '4px 10px',
                          borderRadius: '4px',
                          border: '1px solid #C8CDD1',
                          cursor: 'pointer',
                        }}
                      >
                        編集
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(channel.id)}
                        style={{
                          fontSize: '12px',
                          color: '#DC2626',
                          backgroundColor: 'transparent',
                          padding: '4px 10px',
                          borderRadius: '4px',
                          border: '1px solid #FECACA',
                          cursor: 'pointer',
                        }}
                        aria-label={`${CHANNEL_TYPE_LABELS[channel.channelType]}チャンネルを削除`}
                      >
                        削除
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        ) : null}
      </div>
    </main>
  );
}
