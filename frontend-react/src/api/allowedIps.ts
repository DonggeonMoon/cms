import {request} from './client';

export interface AllowedIp {
    id: number;
    address: string;
    memo: string;
    createdAt: string;
}

export interface AllowedIpSettings {
    enabled: boolean;
    forceDisabled: boolean;
    currentIp: string;
    currentIpAllowed: boolean;
    entries: AllowedIp[];
}

const BASE = '/api/admin/allowed-ips';

export const allowedIpApi = {
    get: () => request<AllowedIpSettings>(BASE),
    create: (address: string, memo: string) => request<AllowedIpSettings>(BASE, {method: 'POST', body: {address, memo}}),
    update: (id: number, address: string, memo: string) =>
        request<AllowedIpSettings>(`${BASE}/${id}`, {method: 'PUT', body: {address, memo}}),
    remove: (id: number) => request<AllowedIpSettings>(`${BASE}/${id}`, {method: 'DELETE'}),
    changeEnabled: (enabled: boolean) => request<AllowedIpSettings>(`${BASE}/enabled`, {method: 'PUT', body: {enabled}}),
};
