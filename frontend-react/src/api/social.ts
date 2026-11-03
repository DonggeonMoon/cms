import {request} from './client';
import type {MemberProfile} from './types';

export type SocialProvider = 'KAKAO' | 'NAVER' | 'GOOGLE' | 'APPLE' | 'FACEBOOK';

export interface SocialProviderOption {
    provider: SocialProvider;
    path: string;
    label: string;
}

export interface PendingSocialSignup {
    provider: SocialProvider;
    label: string;
    email: string | null;
    name: string | null;
    nickname: string | null;
    mobilePhoneNumber: string | null;
    suggestedUsername: string;
}

export interface SocialJoinRequest extends MemberProfile {
    username: string;
    isPrivacyConsentAgreed: boolean;
}

export interface SocialLoginSetting {
    provider: SocialProvider;
    label: string;
    enabled: boolean;
    configured: boolean;
    clientIdKeyId: number | null;
    clientSecretKeyId: number | null;
    appleTeamId: string | null;
    appleKeyId: string | null;
    applePrivateKeyKeyId: number | null;
    redirectUri: string;
    consoleUrl: string;
}

export const socialApi = {
    providers: () => request<SocialProviderOption[]>('/api/auth/oauth/providers'),
    authorizeUrl: (path: string, redirect: string) =>
        `/api/auth/oauth/${path}/authorize?redirect=${encodeURIComponent(redirect)}`,
    pending: () => request<PendingSocialSignup>('/api/auth/oauth/signup'),
    signup: (body: SocialJoinRequest) =>
        request<{ redirect: string }>('/api/auth/oauth/signup', {method: 'POST', body}),
};

export const socialAdminApi = {
    settings: () => request<SocialLoginSetting[]>('/api/admin/social-logins'),
    save: (provider: SocialProvider, body: Record<string, unknown>) =>
        request<SocialLoginSetting>(`/api/admin/social-logins/${provider.toLowerCase()}`, {method: 'PUT', body}),
};
