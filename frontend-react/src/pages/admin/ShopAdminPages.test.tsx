import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {ReviewAdminPage} from './ShopAdminPages';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('ReviewAdminPage review policy', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('loads the current minimum content length and saves a new value', async () => {
        const saved: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/review-policy') return respond(200, {minimumContentLength: 0});
            if (url === '/api/admin/review-policy' && init?.method === 'PUT') {
                const body = JSON.parse(String(init.body));
                saved.push(body);
                return respond(200, body);
            }
            if (url.startsWith('/api/admin/reviews')) return respond(200, {content: [], page: {size: 20, number: 0, totalElements: 0, totalPages: 0}});
            return respond(204);
        }));

        render(<RouterProvider router={createMemoryRouter([{path: '/', element: <ReviewAdminPage/>}])}/>);

        const input = await screen.findByLabelText('리뷰 내용 최소 글자 수') as HTMLInputElement;
        expect(input.value).toBe('0');

        await userEvent.clear(input);
        await userEvent.type(input, '10');
        await userEvent.click(screen.getByRole('button', {name: '저장'}));

        expect(await screen.findByText('저장했습니다.')).toBeInTheDocument();
        expect(saved[0]).toMatchObject({minimumContentLength: 10});
    });
});
