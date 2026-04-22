import { useState, useCallback } from 'react';
import { api } from '../lib/api';
import { useAuthStore } from '../store/authStore';

interface AuthError {
  message: string;
  field?: string;
}

export function useAuth() {
  const { login: storeLogin, logout: storeLogout, isAuthenticated, user } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<AuthError | null>(null);

  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);
    try {
      const { data } = await api.post('/auth/login', { email, password });
      storeLogin(data.accessToken, data.refreshToken, email);
      return true;
    } catch (err: unknown) {
      const axiosError = err as { response?: { status?: number } };
      if (axiosError.response?.status === 401) {
        setError({ message: 'メールアドレスまたはパスワードが正しくありません' });
      } else if (axiosError.response?.status === 429) {
        setError({ message: 'ログイン試行回数が上限に達しました。しばらくしてから再度お試しください' });
      } else {
        setError({ message: 'ログインに失敗しました。ネットワーク接続を確認してください' });
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [storeLogin]);

  const register = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await api.post('/auth/register', { email, password });
      // 登録成功後に自動ログイン
      const { data } = await api.post('/auth/login', { email, password });
      storeLogin(data.accessToken, data.refreshToken, email);
      return true;
    } catch (err: unknown) {
      const axiosError = err as { response?: { status?: number; data?: { message?: string } } };
      if (axiosError.response?.status === 409) {
        setError({ message: 'このメールアドレスは既に登録されています', field: 'email' });
      } else if (axiosError.response?.status === 400) {
        setError({ message: axiosError.response.data?.message ?? '入力内容を確認してください' });
      } else {
        setError({ message: '登録に失敗しました。ネットワーク接続を確認してください' });
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [storeLogin]);

  const logout = useCallback(() => {
    storeLogout();
  }, [storeLogout]);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    login,
    register,
    logout,
    isAuthenticated: isAuthenticated(),
    isLoading,
    error,
    clearError,
    user,
  };
}
