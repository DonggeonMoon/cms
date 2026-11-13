import {request} from './client';

export interface LoginSecuritySettings {
    maxLoginTryCount: number;
}

const BASE = '/api/admin/login-security';

export const loginSecurityApi = {
    get: () => request<LoginSecuritySettings>(BASE),
    update: (maxLoginTryCount: number) => request<LoginSecuritySettings>(BASE, {method: 'PUT', body: {maxLoginTryCount}}),
};
