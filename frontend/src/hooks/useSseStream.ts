import { useEffect, useState, useRef, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';

interface SseStreamState {
  isConnected: boolean;
  error: string | null;
  lastEventTime: Date | null;
}

/**
 * SSEストリーム購読フック。
 *
 * - 要約生成イベント受信時にTanStack Queryキャッシュを無効化し自動更新
 * - 切断時は指数バックオフ（最大60秒）で再接続
 * - Page Visibility API: タブ非表示で切断、表示復帰で再接続
 * - バックエンドは30秒間隔でハートビートを送信するため通常は切断されない
 */
export function useSseStream(): SseStreamState {
  const queryClient = useQueryClient();
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastEventTime, setLastEventTime] = useState<Date | null>(null);

  const eventSourceRef = useRef<EventSource | null>(null);
  const retryTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const retryCountRef = useRef(0);

  const cleanup = useCallback(() => {
    eventSourceRef.current?.close();
    eventSourceRef.current = null;
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
  }, []);

  const connect = useCallback(() => {
    cleanup();

    const token = localStorage.getItem('accessToken');
    const url = token
      ? `/api/v1/summaries/stream?token=${encodeURIComponent(token)}`
      : '/api/v1/summaries/stream';

    const es = new EventSource(url);
    eventSourceRef.current = es;

    es.onopen = () => {
      setIsConnected(true);
      setError(null);
      retryCountRef.current = 0;
    };

    es.addEventListener('summary-created', (event) => {
      try {
        JSON.parse((event as MessageEvent).data);
      } catch {
        // パース失敗でもキャッシュ無効化は行う
      }
      setLastEventTime(new Date());
      queryClient.invalidateQueries({ queryKey: ['summaries'] });
      queryClient.invalidateQueries({ queryKey: ['indices'] });
    });

    es.onerror = () => {
      setIsConnected(false);
      es.close();
      eventSourceRef.current = null;

      // タブが非表示なら再接続しない（表示復帰時に再接続する）
      if (document.hidden) return;

      const delay = Math.min(1000 * Math.pow(2, retryCountRef.current), 60000);
      retryCountRef.current += 1;

      if (retryCountRef.current === 1) {
        setError('リアルタイム接続が切断されました。再接続を試みています...');
      }

      retryTimeoutRef.current = setTimeout(connect, delay);
    };
  }, [cleanup, queryClient]);

  useEffect(() => {
    connect();

    const handleVisibilityChange = () => {
      if (document.hidden) {
        // タブ非表示 → 接続を切断してリソース節約
        cleanup();
        setIsConnected(false);
      } else {
        // タブ表示復帰 → 再接続＆最新データ取得
        retryCountRef.current = 0;
        setError(null);
        connect();
        queryClient.invalidateQueries({ queryKey: ['summaries'] });
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      cleanup();
    };
  }, [connect, cleanup, queryClient]);

  return { isConnected, error, lastEventTime };
}
