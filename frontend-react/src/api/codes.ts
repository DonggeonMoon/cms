import {request} from './client';
import type {Code, CodeSaveRequest, Page} from './types';

export const codeApi = {
    children: (parentId?: number) => request<Code[]>('/api/codes', {query: {parentId}}),
};

export const codeAdminApi = {
    search: (params: { parentId?: number; rootOnly?: boolean; keyword?: string; page: number }) =>
        request<Page<Code>>('/api/admin/codes', {query: {...params, size: 20}}),
    get: (id: number) => request<Code>(`/api/admin/codes/${id}`),
    create: (body: CodeSaveRequest) => request<void>('/api/admin/codes', {method: 'POST', body}),
    update: (id: number, body: CodeSaveRequest) => request<void>(`/api/admin/codes/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/codes/${id}`, {method: 'DELETE'}),
};
