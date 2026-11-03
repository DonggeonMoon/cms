import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const EMPTY_PAGE = {content: [], page: {size: 10, number: 0, totalElements: 0, totalPages: 0}};

describe('list search', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('searches an archive by keyword and keeps it in the address', async () => {
        const urls: string[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            urls.push(url);
            if (url === '/api/archives/5') {
                return respond(200, {id: 5, name: '공지사항', description: null, type: 'NORMAL', supportsComment: false});
            }
            if (url.startsWith('/api/archives/5/articles')) {
                return respond(200, EMPTY_PAGE);
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            return respond(200, url === '/api/configs' || url === '/api/site' ? {} : []);
        }));
        const router = createMemoryRouter(routes, {initialEntries: ['/archives/5']});
        render(<RouterProvider router={router}/>);

        await userEvent.type(await screen.findByRole('textbox', {name: '공지사항 검색'}), '휴무');
        await userEvent.click(screen.getByRole('button', {name: '검색'}));

        expect(await screen.findByText('검색 결과가 없습니다.')).toBeInTheDocument();
        expect(router.state.location.search).toBe('?keyword=%ED%9C%B4%EB%AC%B4');
        expect(urls.some((url) => url.startsWith('/api/archives/5/articles') && url.includes('keyword=%ED%9C%B4%EB%AC%B4'))).toBe(true);
    });
});
