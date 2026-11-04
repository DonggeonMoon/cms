import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {optionSetBody, regenerate} from '../../components/ProductOptionsEditor';
import {estimateShipping} from '../../components/shipping';
import {routes} from '../../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const POLICY = {baseFee: 3000, freeThreshold: 30000, defaultCourier: null,
    regionFees: [{name: '제주', postalCodeFrom: '63000', postalCodeTo: '63644', fee: 4000}]};

const item = (id: number, name: string, amount: number, extra: object = {}) => ({
    id, productId: id * 10, productOptionId: null, productName: name, optionName: null, imageUrl: null, unitPrice: amount,
    quantity: 1, amount, stock: null, requiresShipping: true, available: true, ...extra,
});

describe('option combinations', () => {
    it('builds every combination and keeps prices of combinations that still exist', () => {
        const first = regenerate([{name: '색상', valuesText: '빨강, 파랑'}, {name: '사이즈', valuesText: 'S'}], []);
        expect(first.map((o) => o.name)).toEqual(['빨강 / S', '파랑 / S']);
        const priced = first.map((o, i) => (i === 0 ? {...o, id: 5, additionalPrice: 1000} : o));
        const next = regenerate([{name: '색상', valuesText: '빨강, 파랑'}, {name: '사이즈', valuesText: 'S, L'}], priced);
        expect(next).toHaveLength(4);
        expect(next[0]).toMatchObject({id: 5, additionalPrice: 1000});
        expect(optionSetBody({groups: [{name: '색상', valuesText: '빨강'}], options: next.slice(0, 1)}).optionGroups)
            .toEqual([{name: '색상', values: ['빨강']}]);
    });
});

describe('shipping estimate', () => {
    it('waives only the base fee above the threshold and always adds the regional fee', () => {
        expect(estimateShipping(POLICY, true, 10000, '06000')).toMatchObject({fee: 3000, remainingForFree: 20000});
        expect(estimateShipping(POLICY, true, 30000, '63100')).toMatchObject({fee: 4000, regionName: '제주'});
        expect(estimateShipping(POLICY, false, 10000, '63100').fee).toBe(0);
    });
});

describe('cart', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('orders only the selected available items', async () => {
        const checkouts: unknown[] = [];
        const cart = {items: [item(1, '티셔츠', 20000), item(2, '머그컵', 15000), item(3, '단종 상품', 9000, {available: false})],
            itemsAmount: 35000, shippingFee: 0, shippingRegion: null, freeShippingThreshold: 30000, totalPrice: 35000};
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'sky', name: '김하늘', userType: 'MEMBER'});
            if (url === '/api/members/me') return respond(200, {name: '김하늘', mobilePhoneNumber: '010-1234-5678', address: '서울', postalCode: '06000'});
            if (url === '/api/cart') return respond(200, cart);
            if (url === '/api/shipping-policy') return respond(200, POLICY);
            if (url === '/api/cart/checkout') {
                checkouts.push(JSON.parse(String(init?.body)));
                return respond(201, {id: 77});
            }
            return respond(200, url === '/api/configs' || url === '/api/site' ? {} : []);
        }));
        const router = createMemoryRouter(routes, {initialEntries: ['/cart']});
        render(<AuthProvider><RouterProvider router={router}/></AuthProvider>);

        expect(await screen.findByText('주문 불가 (판매 중지·품절)')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('checkbox', {name: '머그컵 선택'}));
        expect(await screen.findByText(/10,000원 더 담으면/)).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '선택 상품 주문하기 (1)'}));

        expect(await screen.findByDisplayValue('010-1234-5678')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '주문하고 결제하기'}));
        await waitFor(() => expect(router.state.location.pathname).toBe('/checkout/ORDER/77'));
        expect(checkouts[0]).toMatchObject({cartItemIds: [1], receiverName: '김하늘', address: '서울'});
    });
});
