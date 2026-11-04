import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, MemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';
import {StoreOrderQueuePage} from '../admin/StoreAdminPages';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const SETTING = {pickupEnabled: true, dineInEnabled: true, tables: ['1번', '2번'], openTime: null, closeTime: null, closedDays: [],
    paused: false, notice: '포장은 10분 걸립니다', openNow: true};
const LATTE = {id: 5, productId: 50, productOptionId: null, productName: '라떼', optionName: null, imageUrl: null, unitPrice: 5000,
    quantity: 2, amount: 10000, stock: null, requiresShipping: false, storeOrder: true, available: true};

function stub(extra: (url: string, init?: RequestInit) => Response | undefined) {
    vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
        const hit = extra(url, init);
        if (hit) return hit;
        if (url === '/api/auth/me') return respond(200, {id: 1, username: 'cafeguest', name: '손님', userType: 'MEMBER'});
        if (url === '/api/members/me') return respond(200, {name: '손님', mobilePhoneNumber: '010-1234-5678'});
        if (url === '/api/store-setting') return respond(200, SETTING);
        if (url === '/api/site') return respond(200, {id: 1, name: '카페', features: {PRODUCT: true}});
        if (url === '/api/configs') return respond(200, {});
        if (url.startsWith('/api/orders/preview')) return respond(200, {itemsAmount: 10000, shippingFee: 0, couponDiscount: 0, pointsUsed: 0,
            totalPrice: 10000, pointBalance: 0, maxUsablePoints: 0, minUsePoints: 0, pointsToEarn: 0, coupons: []});
        return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
            ? respond(200, []) : respond(204);
    }));
}

describe('store orders', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('orders store-only cart items for a table without asking for an address', async () => {
        const sent: unknown[] = [];
        stub((url, init) => {
            if (url === '/api/cart') return respond(200, {items: [LATTE], itemsAmount: 10000, shippingFee: 0, shippingRegion: null,
                freeShippingThreshold: null, totalPrice: 10000});
            if (url === '/api/cart/checkout') {
                sent.push(JSON.parse(String(init?.body)));
                return respond(201, {id: 9});
            }
            return undefined;
        });
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/orders/cart?items=5']})}/></AuthProvider>);

        expect(await screen.findByText('포장은 10분 걸립니다')).toBeInTheDocument();
        expect(screen.queryByLabelText(/^주소/)).not.toBeInTheDocument();
        await userEvent.click(screen.getByLabelText('매장 식사'));
        await userEvent.selectOptions(screen.getByLabelText('테이블'), '2번');
        await userEvent.type(screen.getByLabelText('요청 사항'), '덜 달게');
        await userEvent.click(screen.getByRole('button', {name: '주문하고 결제하기'}));

        await waitFor(() => expect(sent[0]).toMatchObject({cartItemIds: [5], fulfillmentType: 'DINE_IN', tableName: '2번',
            requestMessage: '덜 달게', receiverName: '손님'}));
        expect(sent[0]).not.toHaveProperty('address');
    });

    it('shows paid store orders as tickets and moves them to the next step', async () => {
        const changed: unknown[] = [];
        stub((url, init) => {
            if (url === '/api/admin/store-orders') return respond(200, [{id: 3, pickupNumber: 7, fulfillmentType: 'PICKUP', tableName: null,
                status: 'PAID', createdAt: '2026-11-04T12:00:00', requestMessage: '샷 추가',
                items: [{id: 1, productName: '라떼', optionName: null, quantity: 2, status: 'ORDERED'}]}]);
            if (url === '/api/admin/orders/3/status') {
                changed.push(JSON.parse(String(init?.body)));
                return respond(204);
            }
            return undefined;
        });
        render(<MemoryRouter><StoreOrderQueuePage/></MemoryRouter>);

        expect(await screen.findByText('7번')).toBeInTheDocument();
        expect(screen.getByText('요청: 샷 추가')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '접수'}));
        await waitFor(() => expect(changed[0]).toEqual({status: 'PREPARING'}));
    });
});
