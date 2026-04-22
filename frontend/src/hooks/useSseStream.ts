import { useEffect, useState, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';

interface SseStreamState {
  isConnected: boolean;
  error: string | null;
  lastEventTime: Date | null;
}

/**
 * SSEストリーム購読フック。
 * 新しい要約が生成されるとTanStack Queryキャッシュを無効化し、自動更新する。
 * バックエンド接続失敗時はエラー表示し、最終取得データを維持する。
 */
export function useSseStream(): SseStreamState {
  const queryClient = useQueryClient();
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastEventTime, setLastEventTime] = useState<Date | null>(null);
  const retryCountRef = useRef(0);
  const maxRetries = 5;

  useEffect(() => {
    let eventSource: EventSource | null = null;
    let retryTimeout: ReturnType<typeof setTimeout> | null = null;

    function connect() {
      const token = localStorage.getItem('accessToken');
      // SSEはAuthorizationヘッダーを送れないため、クエリパラメータでトークンを渡す
      const url = token
        ? `/api/v1/summaries/stream?token=${encodeURIComponent(token)}`
        : '/api/v1/summaries/stream';

      eventSource = new EventSource(url);

      eventSource.onopen = () => {
        setIsConnected(true);
        setError(null);
        retryCountRef.current = 0;
      };

      eventSource.onmessage = (event) => {
        try {
          // イベントデータをパースして確認（使用はしない）
          JSON.parse(event.data);
        } catch {
          // パース失敗でもキャッシュ無効化は行う
        }
        setLastEventTime(new Date());
        // 要約一覧と指数データのキャッシュを無効化
        queryClient.invalidateQueries({ queryKey: ['summaries'] });
        queryClient.invalidateQueries({ queryKey: ['indices'] });
      };

      eventSource.onerror = () => {
        setIsConnected(false);
        eventSource?.close();

        if (retryCountRef.current < maxRetries) {
          const delay = Math.min(1000 * Math.pow(2, retryCountRef.current), 30000);
          retryCountRef.current += 1;
          setError('リアルタイム接続が切断されました。再接続を試みています...');
          retryTimeout = setTimeout(connect, delay);
        } else {
          setError('リアルタイム接続に失敗しました。ページを再読み込みしてください');
        }
      };
    }

    connect();

    return () => {
      eventSource?.close();
      if (retryTimeout) clearTimeout(retryTimeout);
    };
  }, [queryClient]);

  return { isConnected, error, lastEventTime };
}
