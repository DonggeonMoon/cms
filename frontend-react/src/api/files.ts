import {request} from './client';
import type {FileInfo, MediaType, Page} from './types';

export const fileApi = {
    upload: (file: File, alternativeText?: string) => {
        const form = new FormData();
        form.append('file', file);
        if (alternativeText) {
            form.append('alternativeText', alternativeText);
        }
        return request<FileInfo>('/api/files', {method: 'POST', body: form});
    },
};

export const fileAdminApi = {
    search: (keyword: string, mediaType: MediaType | '', page: number) =>
        request<Page<FileInfo>>('/api/admin/files', {query: {keyword, mediaType, page, size: 10}}),
    remove: (id: number) => request<void>(`/api/admin/files/${id}`, {method: 'DELETE'}),
};
