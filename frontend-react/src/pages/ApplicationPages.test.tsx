import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

const FORM = {
    id: 3, title: '청년 창업 지원금', contentHtml: '<p>안내</p>', startDateTime: null, endDateTime: null, maximumSubmissions: null,
    submissionCount: 0, allowMultiple: false, isEnabled: true, selectionMethod: 'REVIEW', winnerCount: null, drawnAt: null, isOpen: true, applied: false, createdAt: '2026-11-01T00:00:00',
    fields: [
        {key: 'company', label: '회사명', type: 'TEXT', required: true, options: null, help: null},
        {key: 'stage', label: '사업 단계', type: 'RADIO', required: true, options: ['예비 창업', '초기 창업'], help: null},
    ],
};

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('application pages', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('renders the defined fields and shows per-field errors from the server', async () => {
        const sent: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'founder', name: '창업자', userType: 'MEMBER'});
            if (url === '/api/applications/3') return respond(200, FORM);
            if (url === '/api/applications/3/submissions') {
                sent.push(JSON.parse(String(init?.body)));
                return respond(400, {code: 'INVALID_INPUT', message: '입력값이 올바르지 않습니다.',
                    fieldErrors: [{field: 'company', message: '회사명을(를) 입력해 주세요.'}]});
            }
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/applications/3']})}/></AuthProvider>);

        expect(await screen.findByLabelText(/회사명/)).toBeInTheDocument();
        await userEvent.click(screen.getByLabelText('초기 창업'));
        await userEvent.click(screen.getByRole('button', {name: '신청하기'}));

        expect(await screen.findByText('회사명을(를) 입력해 주세요.')).toBeInTheDocument();
        expect(sent[0]).toEqual({answers: {stage: '초기 창업'}});
    });

    it('tells applicants that a finished lottery is closed and where to find the result', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'founder', name: '창업자', userType: 'MEMBER'});
            if (url === '/api/applications/3') return respond(200, {...FORM, selectionMethod: 'LOTTERY', winnerCount: 5,
                drawnAt: '2026-11-03T10:00:00', isOpen: false, applied: true});
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/applications/3']})}/></AuthProvider>);

        expect(await screen.findByText(/추첨을 마쳤습니다/)).toBeInTheDocument();
        expect(screen.getByText(/추첨으로 5명 선정/)).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: '신청하기'})).not.toBeInTheDocument();
    });
});
