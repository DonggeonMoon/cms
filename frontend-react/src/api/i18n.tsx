import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {request} from './client';

export const LANGUAGE_NAMES: Record<string, string> = {
    ko: '한국어', en: 'English', ja: '日本語', zh: '中文', vi: 'Tiếng Việt', th: 'ไทย', id: 'Bahasa Indonesia',
    es: 'Español', fr: 'Français', de: 'Deutsch', ru: 'Русский',
};

export type BundleFormat = 'PROPERTIES' | 'YAML';

export interface I18nSetting {
    languages: string[];
    defaultLanguage: string;
}

export const i18nApi = {
    setting: () => request<I18nSetting>('/api/i18n'),
    messages: (language: string) => request<Record<string, string>>(`/api/i18n/messages/${language}`),
    updateSetting: (body: I18nSetting) => request<I18nSetting>('/api/admin/i18n', {method: 'PUT', body: {...body}}),
    bundle: (language: string, format: BundleFormat) =>
        request<{ language: string; format: BundleFormat; text: string; count: number }>(`/api/admin/i18n/bundles/${language}`, {query: {format}}),
    saveBundle: (language: string, format: BundleFormat, text: string) =>
        request<{ count: number; text: string }>(`/api/admin/i18n/bundles/${language}`, {method: 'PUT', body: {format, text}}),
};

export const DEFAULT_MESSAGES: Record<string, string> = {
    'nav.login': '로그인',
    'nav.mypage': '마이페이지',
    'nav.cart': '장바구니',
    'mypage.points': '포인트',
    'mypage.coupons': '쿠폰함',
    'mypage.downloads': '내려받기',
    'nav.logout': '로그아웃',
    'nav.greeting': '{name}님',
    'mypage.profile': '회원정보',
    'mypage.orders': '주문 내역',
    'mypage.subscriptions': '구독',
    'mypage.reservations': '예약 내역',
    'mypage.applications': '신청 내역',
    'mypage.reports': '신고',
    'mypage.media': '수강 내역',
    'mypage.password': '비밀번호 변경',
    'mypage.quit': '회원 탈퇴',
    'footer.privacy': '개인정보처리방침',
    'language.label': '언어',
};

interface I18nState {
    language: string;
    languages: string[];
    setLanguage: (language: string) => void;
    t: (key: string, fallback: string, values?: Record<string, string>) => string;
}

const STORAGE_KEY = 'cms.language';

const format = (text: string, values?: Record<string, string>) =>
    values ? text.replace(/\{(\w+)}/g, (match, name: string) => values[name] ?? match) : text;

const I18nContext = createContext<I18nState>({
    language: 'ko',
    languages: ['ko'],
    setLanguage: () => undefined,
    t: (_key, fallback, values) => format(fallback, values),
});

function stored(): string | null {
    try {
        return localStorage.getItem(STORAGE_KEY);
    } catch {
        return null;
    }
}

export function I18nProvider({children}: { children: ReactNode }) {
    const [setting, setSetting] = useState<I18nSetting>({languages: ['ko'], defaultLanguage: 'ko'});
    const [language, setLanguageState] = useState<string>(stored() ?? 'ko');
    const [messages, setMessages] = useState<Record<string, string>>({});

    useEffect(() => {
        i18nApi.setting().then((loaded) => {
            if (loaded && Array.isArray(loaded.languages)) {
                setSetting(loaded);
                setLanguageState((current) => (loaded.languages.includes(current) ? current : loaded.defaultLanguage));
            }
        }).catch(() => undefined);
    }, []);

    useEffect(() => {
        document.documentElement.lang = language;
        i18nApi.messages(language).then((loaded) => setMessages(loaded && typeof loaded === 'object' ? loaded : {})).catch(() => setMessages({}));
    }, [language]);

    const setLanguage = useCallback((next: string) => {
        setLanguageState(next);
        try {
            localStorage.setItem(STORAGE_KEY, next);
        } catch {
            // 저장할 수 없으면 이번 화면에서만 바뀐다.
        }
    }, []);

    const value = useMemo<I18nState>(() => ({
        language,
        languages: setting.languages,
        setLanguage,
        t: (key, fallback, values) => format(messages[key] ?? fallback, values),
    }), [language, setting.languages, setLanguage, messages]);

    return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nState {
    return useContext(I18nContext);
}
