import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

function renderAt(path: string) {
    const router = createMemoryRouter(routes, {initialEntries: [path]});
    render(<AuthProvider><RouterProvider router={router}/></AuthProvider>);
    return router;
}

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('login flow', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('shows the server message when login fails', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED', message: '로그인이 필요합니다.'});
            }
            if (url === '/api/auth/csrf') {
                document.cookie = 'XSRF-TOKEN=t; path=/';
                return respond(204);
            }
            return respond(401, {code: 'LOGIN_FAILED', message: '아이디 또는 비밀번호가 올바르지 않습니다.'});
        }));

        renderAt('/login');
        await userEvent.type(await screen.findByLabelText(/아이디/), 'tester');
        await userEvent.type(screen.getByLabelText(/비밀번호/), 'wrong-pass');
        await userEvent.click(screen.getByRole('button', {name: '로그인'}));

        expect(await screen.findByRole('alert')).toHaveTextContent('아이디 또는 비밀번호가 올바르지 않습니다.');
    });

    it('redirects anonymous users away from admin pages', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => respond(401, {code: 'UNAUTHENTICATED'})));

        const router = renderAt('/admin/codes');

        expect(await screen.findByRole('heading', {name: '관리자 로그인'})).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/admin/login');
    });

    it('logs in and lands on the requested page', async () => {
        let loggedIn = false;
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') {
                return loggedIn
                    ? respond(200, {id: 1, username: 'tester', name: '테스터', userType: 'MEMBER'})
                    : respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url === '/api/auth/csrf') {
                document.cookie = 'XSRF-TOKEN=t; path=/';
                return respond(204);
            }
            if (url === '/api/auth/login') {
                loggedIn = true;
                return respond(200, {id: 1, username: 'tester', name: '테스터', userType: 'MEMBER'});
            }
            return respond(404, {code: 'NOT_FOUND'});
        }));

        const router = renderAt('/');
        await router.navigate('/login');
        await userEvent.type(await screen.findByLabelText(/아이디/), 'tester');
        await userEvent.type(screen.getByLabelText(/비밀번호/), 'password123');
        await userEvent.click(screen.getByRole('button', {name: '로그인'}));

        expect(await screen.findByText('테스터님, 환영합니다.')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: '마이페이지'})).toBeInTheDocument();
    });
});
