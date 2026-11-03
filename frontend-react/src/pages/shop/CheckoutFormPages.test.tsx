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
    id: 3, name: '중고 아이폰', price: 500000, description: null, imageUrl: null, availableQuantity: 5, averageRating: null,
    contentHtml: '', options: [{id: 7, name: '256GB', additionalPrice: 100000}], discounts: [],
};

describe('order form page', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('fills the receiver from the profile and creates the order from the chosen option and quantity', async () => {
        const bodies: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'sky', name: '김하늘', userType: 'MEMBER'});
            }
            if (url === '/api/members/me') {
                return respond(200, {name: '김하늘', mobilePhoneNumber: '010-1234-5678', address: '서울'});
            }
            if (url === '/api/products/3') {
                return respond(200, PRODUCT);
            }
            if (url === '/api/orders' && init?.method === 'POST') {
                bodies.push(JSON.parse(String(init.body)));
                return respond(201, {id: 42, status: 'PENDING_PAYMENT'});
            }
            return respond(200, url === '/api/configs' || url === '/api/site' ? {} : []);
        }));
        const router = createMemoryRouter(routes, {initialEntries: ['/orders/new?productId=3&optionId=7&quantity=2']});
        render(<AuthProvider><RouterProvider router={router}/></AuthProvider>);

        expect(await screen.findByText(/256GB/)).toBeInTheDocument();
        expect(await screen.findByDisplayValue('010-1234-5678')).toBeInTheDocument();
        expect(screen.getByText(/1,200,000/)).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: '주문하고 결제하기'}));

        expect(bodies[0]).toMatchObject({productId: 3, productOptionId: 7, quantity: 2, receiverName: '김하늘', address: '서울'});
        expect(router.state.location.pathname).toBe('/checkout/ORDER/42');
    });
});
