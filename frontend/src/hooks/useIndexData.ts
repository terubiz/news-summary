import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';

export interface IndexData {
  symbol: string;
  currentValue: number;
  changeAmount: number;
  changeRate: number;
  isStale: boolean;
}

const SYMBOL_DISPLAY_NAMES: Record<string, string> = {
  N225: '日経225',
  SPX: 'S&P500',
  IXIC: 'NASDAQ',
  GDAXI: 'DAX',
};

export function getDisplayName(symbol: string): string {
  return SYMBOL_DISPLAY_NAMES[symbol] ?? symbol;
}

async function fetchIndices(): Promise<IndexData[]> {
  const { data } = await api.get<IndexData[]>('/indices');
  return data;
}

export function useIndexData() {
  return useQuery({
    queryKey: ['indices'],
    queryFn: fetchIndices,
    refetchInterval: 60_000, // 1分ごとに自動更新
    staleTime: 30_000,
  });
}
