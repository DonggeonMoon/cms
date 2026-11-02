import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {ApiKeyPage} from './ApiKeyPage';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const PAGE = {
    content: [{id: 1, name: '카카오맵 운영 키', value: 'abcd1234secret', description: '', modifiedAt: '2026-11-02T10:00:00'}],
    page: {size: 20, number: 0, totalElements: 1, totalPages: 1},
};

describe('API key page', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks for the password again before listing keys and masks the values', async () => {
        let verified = false;
        const calls: string[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            calls.push(url);
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            if (url === '/api/auth/csrf') {
                return respond(204);
            }
            if (url === '/api/admin/reauthenticate') {
                verified = true;
                return respond(204);
            }
            if (url.startsWith('/api/admin/api-keys')) {
                return verified ? respond(200, PAGE) : respond(403, {code: 'REAUTHENTICATION_REQUIRED'});
            }
            return respond(404, {});
        }));
        render(<AuthProvider><ApiKeyPage/></AuthProvider>);

        expect(await screen.findByDisplayValue('admin')).toBeInTheDocument();
        expect(calls.some((url) => url.startsWith('/api/admin/api-keys'))).toBe(false);

        await userEvent.type(screen.getByLabelText(/비밀번호/), 'adminpass1');
        await userEvent.click(screen.getByRole('button', {name: '확인'}));

        expect(await screen.findByText('카카오맵 운영 키')).toBeInTheDocument();
        expect(screen.getByText('abcd••••••••')).toBeInTheDocument();
        expect(screen.queryByText('abcd1234secret')).not.toBeInTheDocument();
    });
});
