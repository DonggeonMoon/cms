import {useEffect, useMemo, useState} from 'react';
import {request} from './client';
import type {FieldSpec} from '../components/SchemaForm';

export interface Grade {
    id: number;
    name: string;
    description: string | null;
    rank: number;
    isDefault: boolean;
}

export interface MyGrade {
    gradeId: number | null;
    gradeName: string | null;
    rank: number;
    expiresAt: string | null;
    baseGradeName: string | null;
}

export type GradeChangeReason = 'MANUAL' | 'PURCHASE' | 'SUBSCRIPTION' | 'EXPIRED' | 'REFUNDED';

export const GRADE_CHANGE_REASON: Record<GradeChangeReason, string> = {
    MANUAL: '관리자 지정', PURCHASE: '구매', SUBSCRIPTION: '구독', EXPIRED: '기간 만료', REFUNDED: '주문 취소',
};

export interface GradeHistory {
    id: number;
    fromGradeName: string | null;
    toGradeName: string | null;
    expiresAt: string | null;
    reason: GradeChangeReason;
    orderId: number | null;
    changedBy: string | null;
    createdAt: string;
}

export const gradeApi = {
    list: () => request<Grade[]>('/api/grades'),
    mine: () => request<MyGrade>('/api/members/me/grade'),
    create: (body: unknown) => request<Grade>('/api/admin/grades', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<Grade>(`/api/admin/grades/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/grades/${id}`, {method: 'DELETE'}),
    ofMember: (memberId: number) => request<MyGrade>(`/api/admin/members/${memberId}/grade`),
    assign: (memberId: number, body: { gradeId: number | null; expiresAt: string | null }) =>
        request<void>(`/api/admin/members/${memberId}/grade`, {method: 'PUT', body}),
    history: (memberId: number) => request<GradeHistory[]>(`/api/admin/members/${memberId}/grade/history`),
};

export function useGrades(): Grade[] {
    const [grades, setGrades] = useState<Grade[]>([]);
    useEffect(() => {
        gradeApi.list().then((loaded) => setGrades(Array.isArray(loaded) ? loaded : [])).catch(() => setGrades([]));
    }, []);
    return grades;
}

export function gradeNameOf(grades: Grade[], gradeId: number | null | undefined): string | null {
    return gradeId == null ? null : grades.find((grade) => grade.id === gradeId)?.name ?? null;
}

export function gradeSelectField(name: string, label: string, grades: Grade[], help?: string): FieldSpec {
    return {
        name, label, type: 'select', wide: true, help,
        options: grades.map((grade) => ({value: String(grade.id), label: `${grade.name} 이상`})),
    };
}

export function useWithGradeFields(base: FieldSpec[], gradeFields: (grades: Grade[]) => FieldSpec[]): FieldSpec[] {
    const grades = useGrades();
    return useMemo(() => {
        if (grades.length === 0) {
            return base;
        }
        const at = base.findIndex((field) => field.type === 'html');
        const index = at < 0 ? base.length : at;
        return [...base.slice(0, index), ...gradeFields(grades), ...base.slice(index)];
    }, [base, grades, gradeFields]);
}
