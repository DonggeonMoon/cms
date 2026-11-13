import {request} from './client';

export interface IdentityVerificationSettings {
    enabled: boolean;
}

const BASE = '/api/admin/identity-verification';

export const identityVerificationApi = {
    get: () => request<IdentityVerificationSettings>('/api/identity-verification'),
    update: (enabled: boolean) => request<IdentityVerificationSettings>(BASE, {method: 'PUT', body: {enabled}}),
};
