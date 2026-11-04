import {useEffect, useState} from 'react';
import {request} from './client';

export type AdminArea = 'STATUS' | 'SITE' | 'CONTENT' | 'BUSINESS' | 'PUBLIC' | 'MEMBER' | 'COMMON' | 'SECURITY';

export const ADMIN_AREA: Record<AdminArea, string> = {
    STATUS: '현황', SITE: '사이트', CONTENT: '컨텐츠', BUSINESS: '비즈니스', PUBLIC: '공공', MEMBER: '회원', COMMON: '공통 설정', SECURITY: '보안',
};

export interface AdminGrade {
    id: number;
    name: string;
    description: string | null;
    superAdmin: boolean;
    areas: AdminArea[];
    adminCount: number;
}

export interface AdminAccount {
    id: number;
    username: string;
    name: string;
    gradeId: number | null;
    gradeName: string | null;
    superAdmin: boolean;
    locked: boolean;
    lastLoginDateTime: string | null;
    createdAt: string;
}

export interface MyPermissions {
    superAdmin: boolean;
    gradeName: string | null;
    areas: AdminArea[];
}

export const adminAccountApi = {
    permissions: () => request<MyPermissions>('/api/admin/me/permissions'),
    grades: () => request<AdminGrade[]>('/api/admin/admin-grades'),
    createGrade: (body: unknown) => request<void>('/api/admin/admin-grades', {method: 'POST', body}),
    updateGrade: (id: number, body: unknown) => request<void>(`/api/admin/admin-grades/${id}`, {method: 'PUT', body}),
    removeGrade: (id: number) => request<void>(`/api/admin/admin-grades/${id}`, {method: 'DELETE'}),
    admins: () => request<AdminAccount[]>('/api/admin/admins'),
    createAdmin: (body: unknown) => request<void>('/api/admin/admins', {method: 'POST', body}),
    assignGrade: (id: number, gradeId: number | null) => request<void>(`/api/admin/admins/${id}/grade`, {method: 'PUT', body: {gradeId}}),
    unlock: (id: number) => request<void>(`/api/admin/admins/${id}/unlock`, {method: 'POST'}),
    removeAdmin: (id: number) => request<void>(`/api/admin/admins/${id}`, {method: 'DELETE'}),
};

export function useMyPermissions(): MyPermissions | null {
    const [permissions, setPermissions] = useState<MyPermissions | null>(null);
    useEffect(() => {
        adminAccountApi.permissions()
            .then((loaded) => setPermissions(loaded && Array.isArray(loaded.areas) ? loaded : null))
            .catch(() => setPermissions(null));
    }, []);
    return permissions;
}
