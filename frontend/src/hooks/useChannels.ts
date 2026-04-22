import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';

export type ChannelType = 'EMAIL' | 'SLACK' | 'LINE' | 'DISCORD';

export interface Channel {
  id: number;
  channelType: ChannelType;
  config: string;
  deliverySchedule: string;
  filterIndices: string[];
  enabled: boolean;
}

export interface CreateChannelRequest {
  channelType: ChannelType;
  config: string;
  deliverySchedule: string;
  filterIndices: string[];
}

export interface UpdateChannelRequest {
  config?: string;
  deliverySchedule?: string;
  filterIndices?: string[];
  enabled?: boolean;
}

async function fetchChannels(): Promise<Channel[]> {
  const { data } = await api.get<Channel[]>('/channels');
  return data;
}

async function createChannel(request: CreateChannelRequest): Promise<Channel> {
  const { data } = await api.post<Channel>('/channels', request);
  return data;
}

async function updateChannel({ id, ...request }: UpdateChannelRequest & { id: number }): Promise<Channel> {
  const { data } = await api.put<Channel>(`/channels/${id}`, request);
  return data;
}

async function deleteChannel(id: number): Promise<void> {
  await api.delete(`/channels/${id}`);
}

async function testChannel(id: number): Promise<boolean> {
  const { data } = await api.post<{ success: boolean }>(`/channels/${id}/test`);
  return data.success;
}

export function useChannels() {
  return useQuery({
    queryKey: ['channels'],
    queryFn: fetchChannels,
  });
}

export function useCreateChannel() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createChannel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channels'] });
    },
  });
}

export function useUpdateChannel() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateChannel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channels'] });
    },
  });
}

export function useDeleteChannel() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteChannel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channels'] });
    },
  });
}

export function useTestChannel() {
  return useMutation({
    mutationFn: testChannel,
  });
}
