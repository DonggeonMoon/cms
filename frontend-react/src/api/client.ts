import type {ErrorBody, FieldError} from './types';

const XSRF_COOKIE = 'XSRF-TOKEN';
const XSRF_HEADER = 'X-XSRF-TOKEN';
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);
const ZONE_HEADER = 'X-CMS-Zone';

export type ApiZone = 'site' | 'admin';

// the visitor site and the admin console hold separate logins on the server; this picks which one requests use
let currentZone: ApiZone = 'site';

export function setApiZone(zone: ApiZone) {
    currentZone = zone;
}

export class ApiError extends Error {
    readonly status: number;
    readonly code: string;
    readonly fieldErrors: FieldError[];

    constructor(status: number, body: Partial<ErrorBody>) {
        super(body.message ?? '요청을 처리하지 못했습니다.');
        this.status = status;
        this.code = body.code ?? 'UNKNOWN';
        this.fieldErrors = body.fieldErrors ?? [];
    }

    fieldMessage(field: string): string | undefined {
        return this.fieldErrors.find((error) => error.field === field)?.message;
    }
}

function readCookie(name: string): string | undefined {
    return document.cookie
        .split(';')
        .map((part) => part.trim())
        .find((part) => part.startsWith(`${name}=`))
        ?.slice(name.length + 1);
}

async function ensureCsrfToken(): Promise<string | undefined> {
    let token = readCookie(XSRF_COOKIE);
    if (!token) {
        await fetch('/api/auth/csrf', {credentials: 'same-origin'});
        token = readCookie(XSRF_COOKIE);
    }
    return token ? decodeURIComponent(token) : undefined;
}

export type Query = Record<string, string | number | boolean | null | undefined>;

export function withQuery(path: string, query?: Query): string {
    if (!query) {
        return path;
    }
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(query)) {
        if (value !== undefined && value !== null && value !== '') {
            params.set(key, String(value));
        }
    }
    const search = params.toString();
    return search ? `${path}?${search}` : path;
}

interface RequestOptions {
    method?: string;
    body?: unknown;
    query?: Query;
}

export async function request<T>(path: string, {method = 'GET', body, query}: RequestOptions = {}): Promise<T> {
    const headers = new Headers({Accept: 'application/json'});
    if (currentZone === 'admin') {
        headers.set(ZONE_HEADER, 'admin');
    }
    const isForm = body instanceof FormData;
    if (body !== undefined && !isForm) {
        headers.set('Content-Type', 'application/json');
    }
    if (!SAFE_METHODS.has(method)) {
        const token = await ensureCsrfToken();
        if (token) {
            headers.set(XSRF_HEADER, token);
        }
    }

    const response = await fetch(withQuery(path, query), {
        method,
        headers,
        credentials: 'same-origin',
        body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    });

    if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}));
        throw new ApiError(response.status, errorBody as Partial<ErrorBody>);
    }
    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
        return undefined as T;
    }
    const text = await response.text();
    return (text ? JSON.parse(text) : undefined) as T;
}
