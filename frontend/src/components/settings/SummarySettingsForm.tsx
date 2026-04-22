import { useState, useCallback, useEffect } from 'react';
import type { SummarySettings, UpdateSummarySettingsRequest } from '../../hooks/useSettings';

/** vercel-ui-skills: ライトモード設定フォーム */

const STOCK_INDICES = [
  { value: 'N225', label: '日経225' },
  { value: 'SPX', label: 'S&P500' },
  { value: 'IXIC', label: 'NASDAQ' },
  { value: 'GDAXI', label: 'DAX' },
];

const SUPPLEMENT_LEVELS = [
  {
    value: 'BEGINNER' as const,
    label: '初心者向け',
    description: '経済用語の解説付き。専門知識がなくても理解できる丁寧な説明',
  },
  {
    value: 'INTERMEDIATE' as const,
    label: '中級者向け',
    description: '基本的な経済知識を前提とした標準的な解説',
  },
  {
    value: 'ADVANCED' as const,
    label: '上級者向け',
    description: '用語解説を省略し、分析と考察に集中した簡潔な要約',
  },
];

const SUMMARY_MODES = [
  { value: 'SHORT' as const, label: '短め', description: '150文字以内' },
  { value: 'STANDARD' as const, label: '標準', description: '300文字以内' },
  { value: 'DETAILED' as const, label: '詳細', description: '600文字以内' },
];

interface SummarySettingsFormProps {
  settings: SummarySettings;
  onSubmit: (data: UpdateSummarySettingsRequest) => void;
  isSubmitting: boolean;
}

export function SummarySettingsForm({ settings, onSubmit, isSubmitting }: SummarySettingsFormProps) {
  const [selectedIndices, setSelectedIndices] = useState<string[]>(settings.selectedIndices);
  const [perspectives, setPerspectives] = useState<string[]>(settings.analysisPerspectives);
  const [supplementLevel, setSupplementLevel] = useState(settings.supplementLevel);
  const [summaryMode, setSummaryMode] = useState(settings.summaryMode);
  const [customPerspective, setCustomPerspective] = useState('');

  useEffect(() => {
    setSelectedIndices(settings.selectedIndices);
    setPerspectives(settings.analysisPerspectives);
    setSupplementLevel(settings.supplementLevel);
    setSummaryMode(settings.summaryMode);
  }, [settings]);

  const toggleIndex = useCallback((value: string) => {
    setSelectedIndices((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
  }, []);

  const togglePerspective = useCallback((value: string) => {
    setPerspectives((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
  }, []);

  const addCustomPerspective = useCallback(() => {
    const trimmed = customPerspective.trim();
    if (trimmed && !perspectives.includes(trimmed)) {
      setPerspectives((prev) => [...prev, trimmed]);
      setCustomPerspective('');
    }
  }, [customPerspective, perspectives]);

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      onSubmit({
        selectedIndices,
        analysisPerspectives: perspectives,
        supplementLevel,
        summaryMode,
      });
    },
    [selectedIndices, perspectives, supplementLevel, summaryMode, onSubmit]
  );

  const inputStyle = {
    fontSize: '14px',
    color: '#1B1B1B',
    backgroundColor: '#FFFFFF',
    borderRadius: '6px',
    border: '1px solid #C8CDD1',
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Stock Index 選択 */}
      <fieldset style={{ marginBottom: '24px', border: 'none', padding: 0 }}>
        <legend
          style={{ fontSize: '14px', fontWeight: 600, color: '#1B1B1B', marginBottom: '8px' }}
        >
          対象株価指数
        </legend>
        <div className="flex flex-wrap" style={{ gap: '8px' }}>
          {STOCK_INDICES.map((idx) => (
            <button
              key={idx.value}
              type="button"
              onClick={() => toggleIndex(idx.value)}
              style={{
                fontSize: '13px',
                fontWeight: selectedIndices.includes(idx.value) ? 600 : 400,
                color: selectedIndices.includes(idx.value) ? '#FFFFFF' : '#797979',
                backgroundColor: selectedIndices.includes(idx.value) ? '#000001' : '#FFFFFF',
                padding: '6px 14px',
                borderRadius: '6px',
                border: `1px solid ${selectedIndices.includes(idx.value) ? '#000001' : '#C8CDD1'}`,
                cursor: 'pointer',
              }}
              aria-pressed={selectedIndices.includes(idx.value)}
            >
              {idx.label}
            </button>
          ))}
        </div>
      </fieldset>

      {/* 分析観点 */}
      <fieldset style={{ marginBottom: '24px', border: 'none', padding: 0 }}>
        <legend
          style={{ fontSize: '14px', fontWeight: 600, color: '#1B1B1B', marginBottom: '8px' }}
        >
          分析観点
        </legend>
        <div className="flex flex-wrap" style={{ gap: '6px', marginBottom: '12px' }}>
          {settings.availablePerspectives
            .filter((p) => p.name !== 'CUSTOM')
            .map((p) => (
              <button
                key={p.name}
                type="button"
                onClick={() => togglePerspective(p.name)}
                style={{
                  fontSize: '12px',
                  fontWeight: perspectives.includes(p.name) ? 600 : 400,
                  color: perspectives.includes(p.name) ? '#FFFFFF' : '#797979',
                  backgroundColor: perspectives.includes(p.name) ? '#000001' : '#FFFFFF',
                  padding: '5px 12px',
                  borderRadius: '6px',
                  border: `1px solid ${perspectives.includes(p.name) ? '#000001' : '#C8CDD1'}`,
                  cursor: 'pointer',
                }}
                aria-pressed={perspectives.includes(p.name)}
              >
                {p.displayName}
              </button>
            ))}
        </div>
        {/* カスタム観点追加 */}
        <div className="flex" style={{ gap: '8px' }}>
          <input
            type="text"
            value={customPerspective}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCustomPerspective(e.target.value)}
            placeholder="カスタム観点を追加..."
            className="flex-1 px-3 py-2 outline-none"
            style={inputStyle}
            onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                addCustomPerspective();
              }
            }}
          />
          <button
            type="button"
            onClick={addCustomPerspective}
            style={{
              fontSize: '13px',
              fontWeight: 500,
              color: '#797979',
              backgroundColor: '#FFFFFF',
              padding: '6px 12px',
              borderRadius: '6px',
              border: '1px solid #C8CDD1',
              cursor: 'pointer',
            }}
          >
            追加
          </button>
        </div>
        {/* カスタム観点表示 */}
        {perspectives
          .filter(
            (p) => !settings.availablePerspectives.some((ap) => ap.name === p)
          )
          .map((p) => (
            <span
              key={p}
              className="inline-flex items-center mt-2 mr-2"
              style={{
                fontSize: '12px',
                color: '#1B1B1B',
                backgroundColor: '#F0F0F0',
                padding: '4px 10px',
                borderRadius: '6px',
                gap: '6px',
              }}
            >
              {p}
              <button
                type="button"
                onClick={() => togglePerspective(p)}
                style={{
                  fontSize: '10px',
                  color: '#797979',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  padding: 0,
                }}
                aria-label={`${p}を削除`}
              >
                ✕
              </button>
            </span>
          ))}
      </fieldset>

      {/* 補足レベル */}
      <fieldset style={{ marginBottom: '24px', border: 'none', padding: 0 }}>
        <legend
          style={{ fontSize: '14px', fontWeight: 600, color: '#1B1B1B', marginBottom: '8px' }}
        >
          補足レベル
        </legend>
        <div className="flex flex-col" style={{ gap: '8px' }}>
          {SUPPLEMENT_LEVELS.map((level) => (
            <label
              key={level.value}
              className="flex items-start cursor-pointer p-3"
              style={{
                borderRadius: '6px',
                border: `1px solid ${supplementLevel === level.value ? '#000001' : '#C8CDD1'}`,
                backgroundColor: supplementLevel === level.value ? '#F5F5F5' : '#FFFFFF',
              }}
            >
              <input
                type="radio"
                name="supplementLevel"
                value={level.value}
                checked={supplementLevel === level.value}
                onChange={() => setSupplementLevel(level.value)}
                className="mt-0.5 mr-3"
              />
              <div>
                <span style={{ fontSize: '13px', fontWeight: 600, color: '#1B1B1B' }}>
                  {level.label}
                </span>
                <p style={{ fontSize: '12px', color: '#797979', marginTop: '2px' }}>
                  {level.description}
                </p>
              </div>
            </label>
          ))}
        </div>
      </fieldset>

      {/* 文字数モード */}
      <fieldset style={{ marginBottom: '24px', border: 'none', padding: 0 }}>
        <legend
          style={{ fontSize: '14px', fontWeight: 600, color: '#1B1B1B', marginBottom: '8px' }}
        >
          文字数モード
        </legend>
        <div className="flex" style={{ gap: '8px' }}>
          {SUMMARY_MODES.map((mode) => (
            <button
              key={mode.value}
              type="button"
              onClick={() => setSummaryMode(mode.value)}
              style={{
                flex: 1,
                fontSize: '13px',
                fontWeight: summaryMode === mode.value ? 600 : 400,
                color: summaryMode === mode.value ? '#FFFFFF' : '#797979',
                backgroundColor: summaryMode === mode.value ? '#000001' : '#FFFFFF',
                padding: '10px 12px',
                borderRadius: '6px',
                border: `1px solid ${summaryMode === mode.value ? '#000001' : '#C8CDD1'}`,
                cursor: 'pointer',
                textAlign: 'center' as const,
              }}
              aria-pressed={summaryMode === mode.value}
            >
              <div>{mode.label}</div>
              <div style={{ fontSize: '11px', marginTop: '2px', opacity: 0.7 }}>
                {mode.description}
              </div>
            </button>
          ))}
        </div>
      </fieldset>

      {/* 保存ボタン */}
      <button
        type="submit"
        disabled={isSubmitting}
        style={{
          fontSize: '14px',
          fontWeight: 600,
          color: '#FFFFFF',
          backgroundColor: '#000001',
          padding: '10px 24px',
          borderRadius: '6px',
          border: 'none',
          cursor: isSubmitting ? 'not-allowed' : 'pointer',
          opacity: isSubmitting ? 0.5 : 1,
        }}
      >
        {isSubmitting ? '保存中...' : '設定を保存'}
      </button>
    </form>
  );
}
