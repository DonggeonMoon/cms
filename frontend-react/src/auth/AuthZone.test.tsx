import {render, screen, within} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('login zones', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks the admin zone for the admin login and the site zone for the member login', async () => {
        const meCalls: (string | null)[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') {
                const zone = new Headers(init?.headers).get('X-CMS-Zone');
                meCalls.push(zone);
                return zone === 'admin'
                    ? respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'})
                    : respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url === '/api/admin/dashboard') {
                return respond(200, {
                    memberCount: 0, articleCount: 0, questionCount: 0, pendingPaymentOrders: 0, paidOrders: 0,
                    revenueLast30Days: 0, confirmedReservations: 0, receivedReports: 0, todayVisits: 0, last7DaysVisits: [],
                });
            }
            return respond(200, url === '/api/menus' || url === '/api/boards' || url.startsWith('/api/banners') ? [] : {});
        }));

        const router = createMemoryRouter(routes, {initialEntries: ['/admin']});
        render(<RouterProvider router={router}/>);
        expect(await screen.findByRole('link', {name: /관리자$/})).toBeInTheDocument();
        expect(meCalls).toContain('admin');

        await router.navigate('/');
        const account = await screen.findByRole('navigation', {name: '계정'});
        expect(await within(account).findByRole('link', {name: '로그인'})).toBeInTheDocument();
        expect(meCalls.at(-1)).toBeNull();
    });
});
