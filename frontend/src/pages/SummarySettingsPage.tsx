import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useSummarySettings, useUpdateSummarySettings } from '../hooks/useSettings';
import { SummarySettingsForm } from '../components/settings/SummarySettingsForm';
import type { UpdateSummarySettingsRequest } from '../hooks/useSettings';

/** vercel-ui-skills: ライトモード設定画面 */

const SUPPLEMENT_LABELS: Record<string, string> = {
  BEGINNER: '初心者向け',
  INTERMEDIATE: '中級者向け',
  ADVANCED: '上級者向け',
};

const MODE_LABELS: Record<string, string> = {
  SHORT: '短め（150文字）',
  STANDARD: '標準（300文字）',
  DETAILED: '詳細（600文字）',
};

const INDEX_LABELS: Record<string, string> = {
  NKX: '日経225',
  SPX: 'S&P500',
  IXIC: 'NASDAQ',
  GDAXI: 'DAX',
};

export default function SummarySettingsPage() {
  const { data: settings, isLoading, isError } = useSummarySettings();
  const updateMutation = useUpdateSummarySettings();

  const handleSubmit = useCallback(
    (data: UpdateSummarySettingsRequest) => {
      updateMutation.mutate(data);
    },
    [updateMutation]
  );

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
            要約設定
          </h1>
        </div>
      </header>

      <div className="max-w-2xl mx-auto px-6 py-8">
        {/* 現在有効な設定の常時表示 */}
        {settings ? (
          <div
            className="mb-8 p-4"
            style={{
              backgroundColor: '#FFFFFF',
              borderRadius: '8px',
              border: '1px solid #C8CDD1',
            }}
          >
            <h2
              style={{
                fontSize: '13px',
                fontWeight: 600,
                color: '#797979',
                marginBottom: '12px',
                textTransform: 'uppercase' as const,
                letterSpacing: '0.05em',
              }}
            >
              現在の設定
            </h2>
            <div className="grid grid-cols-2" style={{ gap: '12px' }}>
              <div>
                <span style={{ fontSize: '12px', color: '#929292' }}>対象指数</span>
                <p style={{ fontSize: '13px', color: '#1B1B1B', marginTop: '2px' }}>
                  {settings.selectedIndices.map((s) => INDEX_LABELS[s] ?? s).join('、') || '未設定'}
                </p>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#929292' }}>補足レベル</span>
                <p style={{ fontSize: '13px', color: '#1B1B1B', marginTop: '2px' }}>
                  {SUPPLEMENT_LABELS[settings.supplementLevel] ?? settings.supplementLevel}
                </p>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#929292' }}>文字数モード</span>
                <p style={{ fontSize: '13px', color: '#1B1B1B', marginTop: '2px' }}>
                  {MODE_LABELS[settings.summaryMode] ?? settings.summaryMode}
                </p>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#929292' }}>分析観点</span>
                <p style={{ fontSize: '13px', color: '#1B1B1B', marginTop: '2px' }}>
                  {settings.analysisPerspectives.length > 0
                    ? `${settings.analysisPerspectives.length}件選択中`
                    : '未設定'}
                </p>
              </div>
            </div>
          </div>
        ) : null}

        {/* 成功メッセージ */}
        {updateMutation.isSuccess ? (
          <div
            className="mb-4 p-3"
            style={{
              backgroundColor: '#F0FDF4',
              borderRadius: '6px',
              border: '1px solid #BBF7D0',
            }}
          >
            <p style={{ fontSize: '13px', color: '#16A34A' }}>
              設定を保存しました。次回の収集・要約生成から反映されます
            </p>
          </div>
        ) : null}

        {/* エラーメッセージ */}
        {updateMutation.isError ? (
          <div
            className="mb-4 p-3"
            style={{
              backgroundColor: '#FEF2F2',
              borderRadius: '6px',
              border: '1px solid #FECACA',
            }}
          >
            <p style={{ fontSize: '13px', color: '#DC2626' }}>
              設定の保存に失敗しました
            </p>
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
            <p style={{ fontSize: '14px', color: '#DC2626' }}>設定の取得に失敗しました</p>
          </div>
        ) : null}

        {/* 設定フォーム */}
        {settings ? (
          <SummarySettingsForm
            settings={settings}
            onSubmit={handleSubmit}
            isSubmitting={updateMutation.isPending}
          />
        ) : null}
      </div>
    </main>
  );
}
