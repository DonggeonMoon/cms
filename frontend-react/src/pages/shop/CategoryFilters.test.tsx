import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const brand = {key: 'brand', label: '브랜드', type: 'SELECT', options: ['가나', '다라'], unit: null, filterable: true};
const weight = {key: 'weight', label: '무게', type: 'NUMBER', options: null, unit: 'kg', filterable: true};
const CATEGORIES = [
    {id: 1, name: '가전', parentId: null, sequence: 0, attributes: [brand], effectiveAttributes: [brand], productCount: 0},
    {id: 2, name: '노트북', parentId: 1, sequence: 0, attributes: [weight], effectiveAttributes: [brand, weight], productCount: 1},
];

describe('product category filters', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('narrows the list by sub category, choice and range, keeping them in the address', async () => {
        const searched: string[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(401, {code: 'UNAUTHORIZED', message: ''});
            if (url === '/api/product-categories') return respond(200, CATEGORIES);
            if (url.startsWith('/api/products')) {
                searched.push(decodeURIComponent(url));
                return respond(200, {content: [], page: {size: 12, number: 0, totalElements: 0, totalPages: 0}});
            }
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {PRODUCT: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/products']})}/></AuthProvider>);

        await userEvent.click(await screen.findByRole('button', {name: '가전'}));
        await userEvent.click(await screen.findByRole('button', {name: '노트북'}));
        await userEvent.click(await screen.findByLabelText('다라'));
        await userEvent.type(screen.getByLabelText('무게 최대'), '2');
        await userEvent.click(screen.getByRole('button', {name: '적용'}));

        await waitFor(() => expect(searched.at(-1)).toBe('/api/products?categoryId=2&attr.brand=다라&max.weight=2'));
        expect(screen.getByText('조건에 맞는 상품이 없습니다.')).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: '전체'}));
        await waitFor(() => expect(searched.at(-1)).toBe('/api/products'));
    });
});
