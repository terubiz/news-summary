import { useState, useCallback } from 'react';
import type { ChannelType, CreateChannelRequest, Channel } from '../../hooks/useChannels';

/** vercel-ui-skills: ライトモード通知チャンネル設定フォーム */

const CHANNEL_TYPES: { value: ChannelType; label: string; icon: string }[] = [
  { value: 'EMAIL', label: 'メール', icon: '✉️' },
  { value: 'SLACK', label: 'Slack', icon: '💬' },
  { value: 'LINE', label: 'LINE', icon: '💚' },
  { value: 'DISCORD', label: 'Discord', icon: '🎮' },
];

const DELIVERY_SCHEDULES = [
  { value: 'IMMEDIATE', label: '即時' },
  { value: 'HOURLY', label: '毎時' },
  { value: 'DAILY_09', label: '毎日 9:00' },
  { value: 'DAILY_18', label: '毎日 18:00' },
];

const STOCK_INDICES = [
  { value: 'NKX', label: '日経225' },
  { value: 'SPX', label: 'S&P500' },
  { value: 'IXIC', label: 'NASDAQ' },
  { value: 'GDAXI', label: 'DAX' },
];

const CONFIG_FIELDS: Record<ChannelType, { key: string; label: string; placeholder: string }[]> = {
  EMAIL: [{ key: 'toAddress', label: '送信先メールアドレス', placeholder: 'you@example.com' }],
  SLACK: [{ key: 'webhookUrl', label: 'Webhook URL', placeholder: 'https://hooks.slack.com/services/...' }],
  LINE: [
    { key: 'channelAccessToken', label: 'チャンネルアクセストークン', placeholder: 'トークンを入力' },
    { key: 'userId', label: 'ユーザーID', placeholder: 'U...' },
  ],
  DISCORD: [{ key: 'webhookUrl', label: 'Webhook URL', placeholder: 'https://discord.com/api/webhooks/...' }],
};

interface ChannelSettingsFormProps {
  editingChannel?: Channel;
  onSubmit: (data: CreateChannelRequest) => void;
  onCancel: () => void;
  isSubmitting: boolean;
  onTest?: (id: number) => void;
  testResult?: { success: boolean } | null;
  isTesting?: boolean;
}

export function ChannelSettingsForm({
  editingChannel,
  onSubmit,
  onCancel,
  isSubmitting,
  onTest,
  testResult,
  isTesting,
}: ChannelSettingsFormProps) {
  const [channelType, setChannelType] = useState<ChannelType>(
    editingChannel?.channelType ?? 'EMAIL'
  );
  const [configValues, setConfigValues] = useState<Record<string, string>>(() => {
    if (editingChannel?.config) {
      try {
        return JSON.parse(editingChannel.config);
      } catch {
        return {};
      }
    }
    return {};
  });
  const [deliverySchedule, setDeliverySchedule] = useState(
    editingChannel?.deliverySchedule ?? 'IMMEDIATE'
  );
  const [filterIndices, setFilterIndices] = useState<string[]>(
    editingChannel?.filterIndices ?? []
  );

  const handleConfigChange = useCallback((key: string, value: string) => {
    setConfigValues((prev) => ({ ...prev, [key]: value }));
  }, []);

  const toggleFilterIndex = useCallback((value: string) => {
    setFilterIndices((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
  }, []);

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      onSubmit({
        channelType,
        config: JSON.stringify(configValues),
        deliverySchedule,
        filterIndices,
      });
    },
    [channelType, configValues, deliverySchedule, filterIndices, onSubmit]
  );

  const inputStyle = {
    fontSize: '14px',
    color: '#1B1B1B',
    backgroundColor: '#FFFFFF',
    borderRadius: '6px',
    border: '1px solid #C8CDD1',
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="p-5"
      style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '8px',
        border: '1px solid #C8CDD1',
      }}
    >
      <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#1B1B1B', marginBottom: '16px' }}>
        {editingChannel ? 'チャンネルを編集' : '新しいチャンネルを追加'}
      </h3>

      {/* チャンネルタイプ選択 */}
      {!editingChannel ? (
        <fieldset style={{ marginBottom: '16px', border: 'none', padding: 0 }}>
          <legend style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B', marginBottom: '8px' }}>
            チャンネルタイプ
          </legend>
          <div className="flex" style={{ gap: '8px' }}>
            {CHANNEL_TYPES.map((ct) => (
              <button
                key={ct.value}
                type="button"
                onClick={() => {
                  setChannelType(ct.value);
                  setConfigValues({});
                }}
                style={{
                  flex: 1,
                  fontSize: '13px',
                  fontWeight: channelType === ct.value ? 600 : 400,
                  color: channelType === ct.value ? '#FFFFFF' : '#797979',
                  backgroundColor: channelType === ct.value ? '#000001' : '#FFFFFF',
                  padding: '8px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${channelType === ct.value ? '#000001' : '#C8CDD1'}`,
                  cursor: 'pointer',
                  textAlign: 'center' as const,
                }}
                aria-pressed={channelType === ct.value}
              >
                <span style={{ display: 'block', marginBottom: '2px' }}>{ct.icon}</span>
                {ct.label}
              </button>
            ))}
          </div>
        </fieldset>
      ) : null}

      {/* 接続設定フィールド */}
      {CONFIG_FIELDS[channelType].map((field) => (
        <div key={field.key} style={{ marginBottom: '12px' }}>
          <label
            htmlFor={`config-${field.key}`}
            style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B', display: 'block', marginBottom: '4px' }}
          >
            {field.label}
          </label>
          <input
            id={`config-${field.key}`}
            type="text"
            value={configValues[field.key] ?? ''}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfigChange(field.key, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 outline-none"
            style={inputStyle}
          />
        </div>
      ))}

      {/* 送信スケジュール */}
      <div style={{ marginBottom: '16px' }}>
        <label
          style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B', display: 'block', marginBottom: '8px' }}
        >
          送信スケジュール
        </label>
        <div className="flex flex-wrap" style={{ gap: '6px' }}>
          {DELIVERY_SCHEDULES.map((s) => (
            <button
              key={s.value}
              type="button"
              onClick={() => setDeliverySchedule(s.value)}
              style={{
                fontSize: '12px',
                fontWeight: deliverySchedule === s.value ? 600 : 400,
                color: deliverySchedule === s.value ? '#FFFFFF' : '#797979',
                backgroundColor: deliverySchedule === s.value ? '#000001' : '#FFFFFF',
                padding: '5px 12px',
                borderRadius: '6px',
                border: `1px solid ${deliverySchedule === s.value ? '#000001' : '#C8CDD1'}`,
                cursor: 'pointer',
              }}
              aria-pressed={deliverySchedule === s.value}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      {/* 対象指数フィルタ */}
      <fieldset style={{ marginBottom: '16px', border: 'none', padding: 0 }}>
        <legend style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B', marginBottom: '8px' }}>
          対象指数フィルタ（空の場合はすべて）
        </legend>
        <div className="flex flex-wrap" style={{ gap: '6px' }}>
          {STOCK_INDICES.map((idx) => (
            <button
              key={idx.value}
              type="button"
              onClick={() => toggleFilterIndex(idx.value)}
              style={{
                fontSize: '12px',
                fontWeight: filterIndices.includes(idx.value) ? 600 : 400,
                color: filterIndices.includes(idx.value) ? '#FFFFFF' : '#797979',
                backgroundColor: filterIndices.includes(idx.value) ? '#000001' : '#FFFFFF',
                padding: '5px 12px',
                borderRadius: '6px',
                border: `1px solid ${filterIndices.includes(idx.value) ? '#000001' : '#C8CDD1'}`,
                cursor: 'pointer',
              }}
              aria-pressed={filterIndices.includes(idx.value)}
            >
              {idx.label}
            </button>
          ))}
        </div>
      </fieldset>

      {/* 接続テスト結果 */}
      {testResult !== undefined && testResult !== null ? (
        <div
          className="mb-4 p-3"
          style={{
            backgroundColor: testResult.success ? '#F0FDF4' : '#FEF2F2',
            borderRadius: '6px',
            border: `1px solid ${testResult.success ? '#BBF7D0' : '#FECACA'}`,
          }}
        >
          <p style={{ fontSize: '13px', color: testResult.success ? '#16A34A' : '#DC2626' }}>
            {testResult.success ? '接続テスト成功' : '接続テスト失敗。設定を確認してください'}
          </p>
        </div>
      ) : null}

      {/* ボタン */}
      <div className="flex items-center" style={{ gap: '8px' }}>
        <button
          type="submit"
          disabled={isSubmitting}
          style={{
            fontSize: '14px',
            fontWeight: 600,
            color: '#FFFFFF',
            backgroundColor: '#000001',
            padding: '8px 20px',
            borderRadius: '6px',
            border: 'none',
            cursor: isSubmitting ? 'not-allowed' : 'pointer',
            opacity: isSubmitting ? 0.5 : 1,
          }}
        >
          {isSubmitting ? '保存中...' : (editingChannel ? '更新' : '追加')}
        </button>
        {editingChannel && onTest ? (
          <button
            type="button"
            onClick={() => onTest(editingChannel.id)}
            disabled={isTesting}
            style={{
              fontSize: '13px',
              fontWeight: 500,
              color: '#797979',
              backgroundColor: '#FFFFFF',
              padding: '8px 16px',
              borderRadius: '6px',
              border: '1px solid #C8CDD1',
              cursor: isTesting ? 'not-allowed' : 'pointer',
              opacity: isTesting ? 0.5 : 1,
            }}
          >
            {isTesting ? 'テスト中...' : '接続テスト'}
          </button>
        ) : null}
        <button
          type="button"
          onClick={onCancel}
          style={{
            fontSize: '13px',
            fontWeight: 500,
            color: '#797979',
            backgroundColor: 'transparent',
            padding: '8px 16px',
            borderRadius: '6px',
            border: 'none',
            cursor: 'pointer',
          }}
        >
          キャンセル
        </button>
      </div>
    </form>
  );
}
