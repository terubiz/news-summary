import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';

// --- 要約設定 ---

export interface PerspectiveOption {
  name: string;
  displayName: string;
}

export interface SummarySettings {
  selectedIndices: string[];
  analysisPerspectives: string[];
  supplementLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  summaryMode: 'SHORT' | 'STANDARD' | 'DETAILED';
  availablePerspectives: PerspectiveOption[];
}

export interface UpdateSummarySettingsRequest {
  selectedIndices: string[];
  analysisPerspectives: string[];
  supplementLevel: string;
  summaryMode: string;
}

async function fetchSummarySettings(): Promise<SummarySettings> {
  const { data } = await api.get<SummarySettings>('/settings/summary');
  return data;
}

async function updateSummarySettings(request: UpdateSummarySettingsRequest): Promise<SummarySettings> {
  const { data } = await api.put<SummarySettings>('/settings/summary', request);
  return data;
}

export function useSummarySettings() {
  return useQuery({
    queryKey: ['settings', 'summary'],
    queryFn: fetchSummarySettings,
  });
}

export function useUpdateSummarySettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateSummarySettings,
    onSuccess: (data) => {
      queryClient.setQueryData(['settings', 'summary'], data);
    },
  });
}

// --- スケジュール設定 ---

export interface ScheduleSettings {
  cronExpression: string;
  enabled: boolean;
}

async function fetchSchedule(): Promise<ScheduleSettings | null> {
  const { data } = await api.get<ScheduleSettings | null>('/settings/schedule');
  return data;
}

async function updateSchedule(request: ScheduleSettings): Promise<ScheduleSettings> {
  const { data } = await api.put<ScheduleSettings>('/settings/schedule', request);
  return data;
}

export function useScheduleSettings() {
  return useQuery({
    queryKey: ['settings', 'schedule'],
    queryFn: fetchSchedule,
  });
}

export function useUpdateSchedule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateSchedule,
    onSuccess: (data) => {
      queryClient.setQueryData(['settings', 'schedule'], data);
    },
  });
}
