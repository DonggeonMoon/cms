import {afterEach, describe, expect, it, vi} from 'vitest';
import {ApiError, request, withQuery} from './client';

function jsonResponse(status: number, body?: unknown): Response {
    return new Response(body === undefined ? null : JSON.stringify(body), {
        status,
        headers: {'Content-Type': 'application/json'},
    });
}

describe('request', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('fetches a CSRF token first and sends it as X-XSRF-TOKEN on unsafe requests', async () => {
        const fetchMock = vi.fn(async (url: string, _init?: RequestInit) => {
            if (url === '/api/auth/csrf') {
                document.cookie = 'XSRF-TOKEN=abc%3D123; path=/';
                return new Response(null, {status: 204});
            }
            return jsonResponse(201, {id: 1});
        });
        vi.stubGlobal('fetch', fetchMock);

        await request('/api/members', {method: 'POST', body: {username: 'u'}});

        expect(fetchMock).toHaveBeenCalledTimes(2);
        const init = fetchMock.mock.calls[1][1] as RequestInit;
        const headers = init.headers as Headers;
        expect(headers.get('X-XSRF-TOKEN')).toBe('abc=123');
        expect(headers.get('Content-Type')).toBe('application/json');
        expect(init.body).toBe('{"username":"u"}');
    });

    it('does not request a CSRF token for GET', async () => {
        const fetchMock = vi.fn(async () => jsonResponse(200, []));
        vi.stubGlobal('fetch', fetchMock);

        await request('/api/codes');

        expect(fetchMock).toHaveBeenCalledOnce();
    });

    it('turns an error body into ApiError with field errors', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => jsonResponse(400, {
            code: 'INVALID_INPUT',
            message: '입력값이 올바르지 않습니다.',
            fieldErrors: [{field: 'username', message: '아이디 형식 오류'}],
        })));

        const error = await request('/api/codes').catch((e: unknown) => e);

        expect(error).toBeInstanceOf(ApiError);
        expect((error as ApiError).code).toBe('INVALID_INPUT');
        expect((error as ApiError).fieldMessage('username')).toBe('아이디 형식 오류');
    });

    it('returns undefined for 204', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => new Response(null, {status: 204})));
        await expect(request('/api/codes')).resolves.toBeUndefined();
    });
});

describe('withQuery', () => {
    it('skips empty values', () => {
        expect(withQuery('/api/x', {a: 'b', empty: '', none: undefined, zero: 0, flag: false}))
            .toBe('/api/x?a=b&zero=0&flag=false');
    });
});
