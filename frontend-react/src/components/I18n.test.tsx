import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const MENU = {id: 7, parentId: null, name: '소개', type: 'LINK', depth: 0, sequence: 1, link: 'https://example.com', targetId: null,
    feature: null, isUsing: true, children: []};

describe('site translation', () => {
    beforeEach(() => localStorage.clear());
    afterEach(() => vi.unstubAllGlobals());

    it('switches menu names and common labels to the chosen language', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/i18n') return respond(200, {languages: ['ko', 'en'], defaultLanguage: 'ko'});
            if (url === '/api/i18n/messages/en') return respond(200, {'nav.login': 'Log in', 'menu.7': 'About', 'site.name': 'Junggo Heaven'});
            if (url === '/api/i18n/messages/ko') return respond(200, {});
            if (url === '/api/auth/me') return respond(401, {code: 'UNAUTHENTICATED'});
            if (url === '/api/site') return respond(200, {id: 1, name: '중고천국', features: {}});
            if (url === '/api/menus') return respond(200, [MENU]);
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url) ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/privacy']})}/></AuthProvider>);

        expect(await screen.findByRole('link', {name: '소개'})).toBeInTheDocument();
        await userEvent.selectOptions(await screen.findByRole('combobox', {name: '언어'}), 'en');

        expect(await screen.findByRole('link', {name: 'About'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Log in'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Junggo Heaven'})).toBeInTheDocument();
        expect(localStorage.getItem('cms.language')).toBe('en');
    });
});
