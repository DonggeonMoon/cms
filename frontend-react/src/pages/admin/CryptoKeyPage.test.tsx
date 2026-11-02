import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {CryptoKeyPage} from './CryptoKeyPage';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const KEY = {
    initialized: true, algorithm: 'AES', key: '11111111-2222-4333-8444-555555555555',
    createdAt: '2026-11-02T10:00:00', modifiedAt: '2026-11-02T10:00:00',
};

function stubServer(password: string) {
    let verified = false;
    const calls: string[] = [];
    vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
        calls.push(url);
        if (url === '/api/auth/me') {
            return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
        }
        if (url === '/api/auth/csrf') {
            return respond(204);
        }
        if (url === '/api/admin/reauthenticate') {
            verified = JSON.parse(String(init?.body)).password === password;
            return verified ? respond(204) : respond(400, {code: 'REAUTHENTICATION_FAILED', message: '비밀번호가 올바르지 않습니다.'});
        }
        if (url === '/api/admin/crypto-key') {
            return verified ? respond(200, KEY) : respond(403, {code: 'REAUTHENTICATION_REQUIRED'});
        }
        return respond(404, {});
    }));
    return calls;
}

describe('crypto key page', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks for the password before loading the key', async () => {
        const calls = stubServer('adminpass1');
        render(<AuthProvider><CryptoKeyPage/></AuthProvider>);

        expect(await screen.findByDisplayValue('admin')).toBeInTheDocument();
        expect(calls).not.toContain('/api/admin/crypto-key');

        await userEvent.type(screen.getByLabelText(/비밀번호/), 'wrong');
        await userEvent.click(screen.getByRole('button', {name: '확인'}));
        expect(await screen.findByText('비밀번호가 올바르지 않습니다.')).toBeInTheDocument();
        expect(calls).not.toContain('/api/admin/crypto-key');

        await userEvent.type(screen.getByLabelText(/비밀번호/), 'adminpass1');
        await userEvent.click(screen.getByRole('button', {name: '확인'}));
        expect(await screen.findByRole('button', {name: '키 재생성'})).toBeInTheDocument();
        expect(screen.getByText('AES')).toBeInTheDocument();
    });
});
