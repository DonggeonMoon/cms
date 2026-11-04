import {render, screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';
import {AdminAccountPage} from './AdminAccountPage';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const GRADES = [
    {id: 1, name: '최고 관리자', description: null, superAdmin: true, areas: ['STATUS', 'SITE', 'CONTENT', 'BUSINESS', 'PUBLIC', 'MEMBER', 'COMMON', 'SECURITY'], adminCount: 1},
    {id: 2, name: '콘텐츠 담당', description: null, superAdmin: false, areas: ['CONTENT'], adminCount: 0},
];
const ADMINS = [
    {id: 1, username: 'root', name: '관리자', gradeId: 1, gradeName: '최고 관리자', superAdmin: true, locked: false, lastLoginDateTime: null, createdAt: ''},
    {id: 2, username: 'editor01', name: '편집자', gradeId: null, gradeName: null, superAdmin: false, locked: true, lastLoginDateTime: null, createdAt: ''},
];

describe('admin accounts', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks for the password, then assigns grades and saves grade areas', async () => {
        let verified = false;
        const sent: { url: string; body: unknown }[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'root', name: '관리자', userType: 'ADMIN'});
            if (url === '/api/auth/csrf') return respond(204);
            if (url === '/api/admin/reauthenticate') {
                verified = true;
                return respond(204);
            }
            if (!verified) return respond(403, {code: 'REAUTHENTICATION_REQUIRED'});
            if (init?.method && init.method !== 'GET') {
                sent.push({url, body: init.body ? JSON.parse(String(init.body)) : null});
                return respond(204);
            }
            if (url === '/api/admin/admins') return respond(200, ADMINS);
            if (url === '/api/admin/admin-grades') return respond(200, GRADES);
            return respond(404, {});
        }));
        render(<AuthProvider><AdminAccountPage/></AuthProvider>);

        await userEvent.type(await screen.findByLabelText(/비밀번호/), 'adminpass1');
        await userEvent.click(screen.getByRole('button', {name: '확인'}));

        await userEvent.selectOptions(await screen.findByLabelText('editor01 등급'), '2');
        await waitFor(() => expect(sent.at(-1)).toEqual({url: '/api/admin/admins/2/grade', body: {gradeId: 2}}));
        await userEvent.click(screen.getByRole('button', {name: '잠금 풀기'}));
        await waitFor(() => expect(sent.at(-1)?.url).toBe('/api/admin/admins/2/unlock'));

        const superAreas = screen.getByRole('group', {name: '최고 관리자 등급 관리 영역'});
        expect(within(superAreas).getByLabelText('보안')).toBeDisabled();
        const contentGrade = screen.getByRole('form', {name: '콘텐츠 담당 등급'});
        await userEvent.click(within(contentGrade).getByLabelText('비즈니스'));
        await userEvent.click(within(contentGrade).getByRole('button', {name: '저장'}));
        await waitFor(() => expect(sent.at(-1)).toEqual({url: '/api/admin/admin-grades/2',
            body: {name: '콘텐츠 담당', description: '', areas: ['CONTENT', 'BUSINESS']}}));
    });

    it('shows only the menus of the admin grade and hides admin management from non super admins', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(200, {id: 2, username: 'editor01', name: '편집자', userType: 'ADMIN'});
            if (url === '/api/admin/me/permissions') return respond(200, {superAdmin: false, gradeName: '콘텐츠 담당', areas: ['CONTENT']});
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {CONTENT: true, PRODUCT: true}});
            if (url === '/api/configs') return respond(200, {});
            return respond(200, {content: [], page: {size: 20, number: 0, totalElements: 0, totalPages: 0}});
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/admin/contents']})}/></AuthProvider>);

        const nav = await screen.findByRole('navigation', {name: '관리자 메뉴'});
        await waitFor(() => expect(within(nav).queryByRole('link', {name: '상품'})).not.toBeInTheDocument());
        expect(within(nav).getByRole('link', {name: '컨텐츠'})).toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '관리자 관리'})).not.toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: 'API 키 관리'})).not.toBeInTheDocument();
    });
});
