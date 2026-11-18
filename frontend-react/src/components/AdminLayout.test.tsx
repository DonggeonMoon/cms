import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('admin layout language switching', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('switches the admin menu to the chosen language without touching the shop visitor language', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'root', name: '관리자', userType: 'ADMIN'});
            if (url === '/api/admin/me/permissions') return respond(200, {superAdmin: true, gradeName: null, areas: []});
            if (url === '/api/i18n') return respond(200, {languages: ['ko', 'en'], defaultLanguage: 'ko'});
            if (url === '/api/i18n/messages/en') return respond(200, {'admin.navGroup.status': 'Status', 'admin.nav.dashboard': 'Dashboard'});
            if (url === '/api/i18n/messages/ko') return respond(200, {});
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {CONTENT: true}});
            if (url === '/api/configs') return respond(200, {});
            return respond(200, {content: [], page: {size: 20, number: 0, totalElements: 0, totalPages: 0}});
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/admin/contents']})}/></AuthProvider>);

        const nav = await screen.findByRole('navigation', {name: '관리자 메뉴'});
        expect(within(nav).getByRole('link', {name: '대시보드'})).toBeInTheDocument();

        await userEvent.selectOptions(await screen.findByLabelText('언어'), 'en');

        expect(await within(nav).findByRole('link', {name: 'Dashboard'})).toBeInTheDocument();
        expect(localStorage.getItem('cms.adminLanguage')).toBe('en');
        expect(localStorage.getItem('cms.language')).not.toBe('en');
    });
});
