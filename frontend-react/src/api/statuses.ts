import {useCallback, useEffect, useState} from 'react';
import {request} from './client';
import type {StatusDefinition, StatusDomain} from './domain';

type Body = Record<string, unknown>;

export const statusApi = {
    list: (domain: StatusDomain) => request<StatusDefinition[]>(`/api/statuses/${domain}`),
    create: (domain: StatusDomain, body: Body) => request<StatusDefinition>(`/api/admin/statuses/${domain}`, {method: 'POST', body}),
    update: (id: number, body: Body) => request<StatusDefinition>(`/api/admin/statuses/items/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/statuses/items/${id}`, {method: 'DELETE'}),
    applyToOrder: (id: number, code: string | null) =>
        request<void>(`/api/admin/orders/${id}/custom-status`, {method: 'PUT', body: {code}}),
    applyToReservation: (id: number, code: string | null) =>
        request<void>(`/api/admin/reservations/${id}/custom-status`, {method: 'PUT', body: {code}}),
};

export function useStatusLabels(domain: StatusDomain, fallback: Record<string, string>) {
    const [statuses, setStatuses] = useState<StatusDefinition[]>([]);
    const reload = useCallback(() => {
        statusApi.list(domain).then((list) => setStatuses(Array.isArray(list) ? list : [])).catch(() => setStatuses([]));
    }, [domain]);
    useEffect(reload, [reload]);
    const label = useCallback((status: string, customStatus?: string | null) => {
        const custom = customStatus ? statuses.find((s) => s.code === customStatus) : undefined;
        return custom?.label ?? statuses.find((s) => s.code === status)?.label ?? fallback[status] ?? status;
    }, [statuses, fallback]);
    return {statuses, label, reload};
}
