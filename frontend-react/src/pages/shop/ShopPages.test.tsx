import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const PRODUCT = {
    id: 3, name: '머그컵', price: 10000, yourPrice: 10000, imageUrl: null, accessGradeId: null, description: null,
    donation: null, funding: null, saleType: 'PURCHASE', commitmentMonths: null, preorder: null, averageRating: null,
    availableQuantity: 5, specs: [], digitalFileCount: 0, downloadLimit: null, downloadDays: null, inquiry: null,
    optionGroups: [], options: [], discounts: [], donationPresets: [], contentHtml: '', billingCycle: null,
};

const EMPTY_REVIEWS = {content: [], page: {size: 20, number: 0, totalElements: 0, totalPages: 0}};

describe('product reviews', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('shows the minimum length hint from the review policy and requires that many characters', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'buyer', name: '구매자', userType: 'MEMBER'});
            if (url === '/api/products/3') return respond(200, PRODUCT);
            if (url === '/api/products/3/reviews') return respond(200, EMPTY_REVIEWS);
            if (url === '/api/review-policy') return respond(200, {minimumContentLength: 5});
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {PRODUCT: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/products/3']})}/></AuthProvider>);

        expect(await screen.findByText('최소 5자 이상 입력해 주세요.')).toBeInTheDocument();
        const textarea = screen.getByLabelText('리뷰 내용') as HTMLTextAreaElement;
        expect(textarea).toBeRequired();
        expect(textarea.minLength).toBe(5);
    });

    it('submits a long-enough review and shows it in the list', async () => {
        const sent: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'buyer', name: '구매자', userType: 'MEMBER'});
            if (url === '/api/products/3') return respond(200, PRODUCT);
            if (url === '/api/review-policy') return respond(200, {minimumContentLength: 0});
            if (url === '/api/products/3/reviews' && init?.method === 'POST') {
                sent.push(JSON.parse(String(init.body)));
                return respond(201);
            }
            if (url === '/api/products/3/reviews') return respond(200, EMPTY_REVIEWS);
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {PRODUCT: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/products/3']})}/></AuthProvider>);

        await userEvent.type(await screen.findByLabelText(/리뷰 제목/), '좋아요');
        await userEvent.type(screen.getByLabelText('리뷰 내용'), '아주 마음에 들어요');
        await userEvent.click(screen.getByRole('button', {name: '리뷰 등록'}));

        expect(sent[0]).toMatchObject({title: '좋아요'});
    });
});
