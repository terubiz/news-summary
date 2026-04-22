import { useState, useCallback } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

type AuthMode = 'login' | 'register';

interface FormErrors {
  email?: string;
  password?: string;
}

function validateEmail(email: string): string | undefined {
  if (!email.trim()) return 'メールアドレスを入力してください';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return '有効なメールアドレスを入力してください';
  return undefined;
}

function validatePassword(password: string, mode: AuthMode): string | undefined {
  if (!password) return 'パスワードを入力してください';
  if (mode === 'register' && password.length < 8) return 'パスワードは8文字以上で入力してください';
  return undefined;
}

export default function LoginPage() {
  const { login, register, isAuthenticated, isLoading, error, clearError } = useAuth();
  const [mode, setMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formErrors, setFormErrors] = useState<FormErrors>({});

  const switchMode = useCallback((newMode: AuthMode) => {
    setMode(newMode);
    setFormErrors({});
    clearError();
  }, [clearError]);

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();

    const emailError = validateEmail(email);
    const passwordError = validatePassword(password, mode);

    if (emailError || passwordError) {
      setFormErrors({ email: emailError, password: passwordError });
      return;
    }

    setFormErrors({});

    if (mode === 'login') {
      await login(email, password);
    } else {
      await register(email, password);
    }
  }, [email, password, mode, login, register]);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <main
      className="flex items-center justify-center min-h-dvh"
      style={{ backgroundColor: '#F9F9F9', fontFamily: 'Inter, system-ui, sans-serif' }}
    >
      <div
        className="w-full max-w-md p-8"
        style={{
          backgroundColor: '#FFFFFF',
          borderRadius: '23px',
          border: '1px solid #C8CDD1',
        }}
      >
        {/* ヘッダー */}
        <h1
          className="text-center mb-6"
          style={{
            fontSize: '24px',
            fontWeight: 700,
            color: '#1B1B1B',
          }}
        >
          Economic News AI
        </h1>

        {/* タブ切り替え */}
        <div className="flex mb-6" style={{ gap: '4px' }}>
          <button
            type="button"
            onClick={() => switchMode('login')}
            className="flex-1 py-3 text-center transition-colors"
            style={{
              fontSize: '14px',
              fontWeight: mode === 'login' ? 600 : 400,
              color: mode === 'login' ? '#000001' : '#797979',
              backgroundColor: mode === 'login' ? '#F0F0F0' : 'transparent',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            ログイン
          </button>
          <button
            type="button"
            onClick={() => switchMode('register')}
            className="flex-1 py-3 text-center transition-colors"
            style={{
              fontSize: '14px',
              fontWeight: mode === 'register' ? 600 : 400,
              color: mode === 'register' ? '#000001' : '#797979',
              backgroundColor: mode === 'register' ? '#F0F0F0' : 'transparent',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            新規登録
          </button>
        </div>

        {/* APIエラー表示 */}
        {error ? (
          <div
            className="mb-4 p-3"
            style={{
              backgroundColor: '#FEF2F2',
              borderRadius: '6px',
              border: '1px solid #FECACA',
            }}
          >
            <p style={{ fontSize: '13px', color: '#DC2626' }}>{error.message}</p>
          </div>
        ) : null}

        {/* フォーム */}
        <form onSubmit={handleSubmit}>
          {/* メールアドレス */}
          <div className="mb-4">
            <label
              htmlFor="email"
              className="block mb-1"
              style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B' }}
            >
              メールアドレス
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                setEmail(e.target.value);
                if (formErrors.email) setFormErrors((prev) => ({ ...prev, email: undefined }));
              }}
              placeholder="you@example.com"
              autoComplete="email"
              className="w-full px-3 py-2 outline-none"
              style={{
                fontSize: '14px',
                color: '#1B1B1B',
                backgroundColor: '#FFFFFF',
                borderRadius: '6px',
                border: `1px solid ${formErrors.email ? '#DC2626' : '#C8CDD1'}`,
              }}
              aria-invalid={formErrors.email ? 'true' : undefined}
              aria-describedby={formErrors.email ? 'email-error' : undefined}
            />
            {formErrors.email ? (
              <p id="email-error" style={{ fontSize: '12px', color: '#DC2626', marginTop: '4px' }}>
                {formErrors.email}
              </p>
            ) : null}
          </div>

          {/* パスワード */}
          <div className="mb-6">
            <label
              htmlFor="password"
              className="block mb-1"
              style={{ fontSize: '13px', fontWeight: 500, color: '#1B1B1B' }}
            >
              パスワード
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                setPassword(e.target.value);
                if (formErrors.password) setFormErrors((prev) => ({ ...prev, password: undefined }));
              }}
              placeholder={mode === 'register' ? '8文字以上' : ''}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              className="w-full px-3 py-2 outline-none"
              style={{
                fontSize: '14px',
                color: '#1B1B1B',
                backgroundColor: '#FFFFFF',
                borderRadius: '6px',
                border: `1px solid ${formErrors.password ? '#DC2626' : '#C8CDD1'}`,
              }}
              aria-invalid={formErrors.password ? 'true' : undefined}
              aria-describedby={formErrors.password ? 'password-error' : undefined}
            />
            {formErrors.password ? (
              <p id="password-error" style={{ fontSize: '12px', color: '#DC2626', marginTop: '4px' }}>
                {formErrors.password}
              </p>
            ) : null}
          </div>

          {/* 送信ボタン */}
          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-3 transition-colors"
            style={{
              fontSize: '14px',
              fontWeight: 600,
              color: '#FFFFFF',
              backgroundColor: '#000001',
              borderRadius: '6px',
              border: 'none',
              cursor: isLoading ? 'not-allowed' : 'pointer',
              opacity: isLoading ? 0.5 : 1,
            }}
            aria-label={mode === 'login' ? 'ログイン' : '新規登録'}
          >
            {isLoading ? '処理中...' : (mode === 'login' ? 'ログイン' : 'アカウントを作成')}
          </button>
        </form>
      </div>
    </main>
  );
}
