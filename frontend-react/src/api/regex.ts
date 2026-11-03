import {useEffect, useState} from 'react';
import {request} from './client';

export type MemberRegexTarget = 'USERNAME' | 'PASSWORD' | 'NICKNAME' | 'EMAIL' | 'MOBILE_PHONE' | 'PHONE_NUMBER' | 'POSTAL_CODE';
export type NumberRegexTarget = 'ORDER_NUMBER' | 'RESERVATION_NUMBER' | 'PAYMENT_NUMBER';
export type RegexTarget = MemberRegexTarget | NumberRegexTarget;

export interface RegexRule {
    id: number;
    name: string;
    pattern: string;
    message: string;
    hint: string | null;
    description: string | null;
    usedBy: string[];
    modifiedAt: string | null;
}

export interface RegexOption {
    id: number;
    name: string;
    pattern: string;
}

export interface PublicRegex {
    target: RegexTarget;
    pattern: string;
    message: string;
    hint: string | null;
}

export interface RegexTestResult {
    valid: boolean;
    matches: boolean;
    error: string | null;
}

type Body = Record<string, unknown>;

export const regexApi = {
    publicRules: () => request<PublicRegex[]>('/api/regexes'),
};

export const regexAdminApi = {
    list: () => request<RegexRule[]>('/api/admin/regexes'),
    options: () => request<RegexOption[]>('/api/admin/regexes/options'),
    create: (body: Body) => request<void>('/api/admin/regexes', {method: 'POST', body}),
    update: (id: number, body: Body) => request<void>(`/api/admin/regexes/${id}`, {method: 'PUT', body}),
    test: (pattern: string, value: string) =>
        request<RegexTestResult>('/api/admin/regexes/test', {method: 'POST', body: {pattern, value}}),
    binding: (target: NumberRegexTarget) =>
        request<{ regexRuleId: number | null }>(`/api/admin/regex-bindings/${target}`),
    bind: (target: NumberRegexTarget, regexRuleId: number | null) =>
        request<void>(`/api/admin/regex-bindings/${target}`, {method: 'PUT', body: {regexRuleId}}),
};

export function useRegexHints(): Partial<Record<RegexTarget, string>> {
    const [hints, setHints] = useState<Partial<Record<RegexTarget, string>>>({});
    useEffect(() => {
        let active = true;
        regexApi.publicRules()
            .then((rules) => {
                if (active && Array.isArray(rules)) {
                    setHints(Object.fromEntries(rules.filter((rule) => rule.hint).map((rule) => [rule.target, rule.hint!])));
                }
            })
            .catch(() => undefined);
        return () => {
            active = false;
        };
    }, []);
    return hints;
}
