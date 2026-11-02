import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

function stub() {
    vi.stubGlobal('fetch', vi.fn(async (url: string) => {
        if (url === '/api/policies/PRIVACY_CONSENT') {
            return respond(200, {type: 'PRIVACY_CONSENT', title: '개인정보 수집·이용 동의', contentHtml: '<p>수집 항목: 아이디</p>',
                version: 1, lastWorker: 'admin', modifiedAt: '2026-11-02T10:00:00'});
        }
        if (url === '/api/policies/PRIVACY_POLICY') {
            return respond(200, {type: 'PRIVACY_POLICY', title: '개인정보처리방침', contentHtml: '<p>제1조 목적</p>',
                version: 1, lastWorker: 'admin', modifiedAt: '2026-11-02T10:00:00'});
        }
        if (url === '/api/auth/me') {
            return respond(401, {code: 'UNAUTHENTICATED'});
        }
        return respond(200, url === '/api/menus' || url === '/api/boards' || url.startsWith('/api/banners') ? [] : {});
    }));
}

describe('privacy texts', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('shows the consent before the sign-up form and only continues once agreed', async () => {
        stub();
        render(<RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/join']})}/>);

        expect(await screen.findByText('수집 항목: 아이디')).toBeInTheDocument();
        const next = screen.getByRole('button', {name: '동의하고 계속'});
        expect(next).toBeDisabled();
        expect(screen.queryByLabelText(/아이디/)).not.toBeInTheDocument();

        await userEvent.click(screen.getByLabelText(/개인정보 수집·이용에 동의합니다/));
        await userEvent.click(next);
        expect(await screen.findByLabelText(/아이디/)).toBeInTheDocument();
    });

    it('links the privacy policy from the footer', async () => {
        stub();
        const router = createMemoryRouter(routes, {initialEntries: ['/']});
        render(<RouterProvider router={router}/>);

        const footer = await screen.findByRole('navigation', {name: '사이트 정보'});
        await userEvent.click(within(footer).getByRole('link', {name: '개인정보처리방침'}));
        expect(await screen.findByText('제1조 목적')).toBeInTheDocument();
        expect(router.state.location.pathname).toBe('/privacy');
    });
});
