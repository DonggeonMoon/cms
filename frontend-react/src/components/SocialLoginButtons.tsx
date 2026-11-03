import {useEffect, useState} from 'react';
import {socialApi, type SocialProviderOption} from '../api/social';

export function SocialLoginButtons({redirect, verb = '로그인'}: { redirect: string; verb?: string }) {
    const [providers, setProviders] = useState<SocialProviderOption[]>([]);

    useEffect(() => {
        socialApi.providers().then((list) => setProviders(Array.isArray(list) ? list : [])).catch(() => setProviders([]));
    }, []);

    if (providers.length === 0) {
        return null;
    }
    return (
        <div className="social-login">
            <p className="social-divider"><span>간편 {verb}</span></p>
            <div className="social-buttons">
                {providers.map((option) => (
                    <a key={option.provider} className={`social-button social-${option.path}`}
                       href={socialApi.authorizeUrl(option.path, redirect)}>
                        {option.label}로 {verb}
                    </a>
                ))}
            </div>
        </div>
    );
}

export const SOCIAL_ERRORS: Record<string, string> = {
    state: '로그인 요청이 만료되었거나 다른 브라우저에서 시작되었습니다. 다시 시도해 주세요.',
    cancelled: '소셜 로그인을 취소했습니다.',
    failed: '소셜 로그인 제공자와 통신하지 못했습니다. 잠시 후 다시 시도해 주세요.',
    locked: '로그인 시도 횟수를 초과해 계정이 잠겼습니다. 관리자에게 문의하세요.',
};
