import type {ErrorBody, FieldError} from './types';

const XSRF_COOKIE = 'XSRF-TOKEN';
const XSRF_HEADER = 'X-XSRF-TOKEN';
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);
const ZONE_HEADER = 'X-CMS-Zone';
const SITE_HEADER = 'X-CMS-Site';

export type ApiZone = 'site' | 'admin';

let currentZone: ApiZone = 'site';
let currentSiteCode: string | null = null;

export function setApiZone(zone: ApiZone) {
    currentZone = zone;
}

/** 공개 사이트는 URL의 /s/{code} 프리픽스로, 관리자는 선택한 사이트로 지정한다. 비워 두면 기본 사이트. */
export function setApiSiteCode(code: string | null) {
    currentSiteCode = code;
}

export function getApiSiteCode(): string | null {
    return currentSiteCode;
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

export const CONNECTION_ERROR_CODES = ['NETWORK_ERROR', 'SERVER_UNAVAILABLE', 'TIMEOUT', 'INVALID_RESPONSE'] as const;

const CONNECTION_MESSAGES: Record<(typeof CONNECTION_ERROR_CODES)[number], string> = {
    NETWORK_ERROR: '서버에 연결할 수 없습니다. 인터넷 연결을 확인하거나, 서버가 켜져 있는지 확인한 뒤 다시 시도해 주세요.',
    SERVER_UNAVAILABLE: '서버가 응답하지 않습니다. 서버가 꺼져 있거나 점검 중일 수 있으니 잠시 후 다시 시도해 주세요.',
    TIMEOUT: '서버 응답이 너무 늦어 요청을 멈췄습니다. 잠시 후 다시 시도해 주세요.',
    INVALID_RESPONSE: '서버에서 알 수 없는 응답이 왔습니다. 서버가 제대로 실행 중인지 확인해 주세요.',
};

const TIMEOUT_MS = 30_000;

function connectionError(code: keyof typeof CONNECTION_MESSAGES, status = 0): ApiError {
    return new ApiError(status, {code, message: CONNECTION_MESSAGES[code]});
}

export function isConnectionError(error: unknown): boolean {
    return error instanceof ApiError && (CONNECTION_ERROR_CODES as readonly string[]).includes(error.code);
}

/** 네트워크가 끊기거나 서버가 꺼져 있을 때 브라우저 기본 메시지(Failed to fetch 등) 대신 연결 문제임을 알리는 오류로 바꾼다. */
async function send(url: string, init: RequestInit, timeoutMs: number | null): Promise<Response> {
    const controller = new AbortController();
    const timer = timeoutMs == null ? undefined : setTimeout(() => controller.abort(), timeoutMs);
    try {
        return await fetch(url, {...init, signal: controller.signal});
    } catch (error) {
        throw connectionError(controller.signal.aborted ? 'TIMEOUT' : 'NETWORK_ERROR');
    } finally {
        clearTimeout(timer);
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
        await send('/api/auth/csrf', {credentials: 'same-origin'}, TIMEOUT_MS);
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
    if (currentSiteCode) {
        headers.set(SITE_HEADER, currentSiteCode);
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

    const response = await send(withQuery(path, query), {
        method,
        headers,
        credentials: 'same-origin',
        body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    }, isForm ? null : TIMEOUT_MS);

    if (!response.ok) {
        const errorBody = await response.json().catch(() => null) as Partial<ErrorBody> | null;
        // 개발 서버 프록시·게이트웨이는 백엔드가 꺼져 있으면 JSON 이 아닌 5xx 를 돌려준다.
        if ([502, 503, 504].includes(response.status) || (response.status >= 500 && !errorBody?.code)) {
            throw connectionError('SERVER_UNAVAILABLE', response.status);
        }
        throw new ApiError(response.status, errorBody ?? {});
    }
    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
        return undefined as T;
    }
    const text = await response.text();
    try {
        return (text ? JSON.parse(text) : undefined) as T;
    } catch {
        throw connectionError('INVALID_RESPONSE', response.status);
    }
}
