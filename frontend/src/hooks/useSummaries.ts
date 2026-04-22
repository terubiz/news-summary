import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { api } from '../lib/api';

export interface IndexImpact {
  id: number;
  indexSymbol: string;
  impactDirection: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
}

export interface Summary {
  id: number;
  summaryText: string;
  supplementLevel: string;
  summaryMode: string;
  status: string;
  generatedAt: string;
  indexImpacts: IndexImpact[];
}

export interface SummaryListResponse {
  content: Summary[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface SummaryFilters {
  page: number;
  size: number;
  indexSymbol?: string;
  keyword?: string;
}

async function fetchSummaries(filters: SummaryFilters): Promise<SummaryListResponse> {
  const params: Record<string, string | number> = {
    page: filters.page,
    size: filters.size,
  };
  if (filters.indexSymbol) params.indexSymbol = filters.indexSymbol;
  if (filters.keyword) params.keyword = filters.keyword;

  const { data } = await api.get<SummaryListResponse>('/summaries', { params });
  return data;
}

export function useSummaries(filters: SummaryFilters) {
  return useQuery({
    queryKey: ['summaries', filters],
    queryFn: () => fetchSummaries(filters),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}
