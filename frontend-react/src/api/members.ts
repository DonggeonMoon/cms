import {request} from './client';
import type {AdminJoinSettings, JoinSettings, MemberJoinRequest, MemberProfile, MemberResponse, MemberSummary, Page} from './types';

export const memberApi = {
    joinSettings: () => request<JoinSettings>('/api/members/join-settings'),
    join: (body: MemberJoinRequest) => request<void>('/api/members', {method: 'POST', body}),
    isUsernameAvailable: (username: string) =>
        request<{ available: boolean }>('/api/members/username-availability', {query: {username}})
            .then((result) => result.available),
    me: () => request<MemberResponse>('/api/members/me'),
    modify: (body: MemberProfile) => request<void>('/api/members/me', {method: 'PUT', body}),
    changePassword: (currentPassword: string, newPassword: string, newPasswordCheck: string) =>
        request<void>('/api/members/me/password', {
            method: 'PUT',
            body: {currentPassword, newPassword, newPasswordCheck},
        }),
    quit: () => request<void>('/api/members/me', {method: 'DELETE'}),
};

export const memberAdminApi = {
    search: (keyword: string, page: number) =>
        request<Page<MemberSummary>>('/api/admin/members', {query: {keyword, page, size: 10}}),
    get: (id: number) => request<MemberResponse>(`/api/admin/members/${id}`),
    unlock: (id: number) => request<void>(`/api/admin/members/${id}/unlock`, {method: 'POST'}),
    remove: (id: number) => request<void>(`/api/admin/members/${id}`, {method: 'DELETE'}),
    joinSettings: () => request<AdminJoinSettings>('/api/admin/members/join-settings'),
    saveJoinSettings: (body: AdminJoinSettings) =>
        request<AdminJoinSettings>('/api/admin/members/join-settings', {method: 'PUT', body}),
};
