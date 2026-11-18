import {useEffect, useState} from 'react';
import {socialApi, type SocialProviderOption} from '../api/social';
import {useI18n} from '../api/i18n';

export function SocialLoginButtons({redirect, verb}: { redirect: string; verb?: string }) {
    const {t} = useI18n();
    const resolvedVerb = verb ?? t('nav.login', '로그인');
    const [providers, setProviders] = useState<SocialProviderOption[]>([]);

    useEffect(() => {
        socialApi.providers().then((list) => setProviders(Array.isArray(list) ? list : [])).catch(() => setProviders([]));
    }, []);

    if (providers.length === 0) {
        return null;
    }
    return (
        <div className="social-login">
            <p className="social-divider"><span>{t('shop.social.easy', '간편 {verb}', {verb: resolvedVerb})}</span></p>
            <div className="social-buttons">
                {providers.map((option) => (
                    <a key={option.provider} className={`social-button social-${option.path}`}
                       href={socialApi.authorizeUrl(option.path, redirect)}>
                        {t('shop.social.withProvider', '{label}로 {verb}', {label: option.label, verb: resolvedVerb})}
                    </a>
                ))}
            </div>
        </div>
    );
}

export function socialErrors(t: (key: string, fallback: string) => string): Record<string, string> {
    return {
        state: t('shop.social.error.state', '로그인 요청이 만료되었거나 다른 브라우저에서 시작되었습니다. 다시 시도해 주세요.'),
        cancelled: t('shop.social.error.cancelled', '소셜 로그인을 취소했습니다.'),
        failed: t('shop.social.error.failed', '소셜 로그인 제공자와 통신하지 못했습니다. 잠시 후 다시 시도해 주세요.'),
        locked: t('shop.social.error.locked', '로그인 시도 횟수를 초과해 계정이 잠겼습니다. 관리자에게 문의하세요.'),
    };
}
