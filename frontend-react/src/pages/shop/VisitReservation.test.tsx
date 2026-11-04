import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const PROGRAM = {
    id: 7, name: '에어컨 청소', price: 80000, reservationType: 'VISIT', layout: null, slotMinutes: 120, openTime: '09:00:00',
    closeTime: '13:00:00', startDateTime: '2030-01-01T00:00:00', endDateTime: '2030-12-31T23:59:00', isEnabled: true, isOpen: true,
};

describe('visit reservation', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks for the visit address and sends it with the booking', async () => {
        const sent: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'homeowner', name: '고객', userType: 'MEMBER'});
            if (url === '/api/members/me') return respond(200, {name: '고객', mobilePhoneNumber: '010-1234-5678'});
            if (url === '/api/programs/7') return respond(200, PROGRAM);
            if (url === '/api/reservations' && init?.method === 'POST') {
                sent.push(JSON.parse(String(init.body)));
                return respond(201, {id: 3, status: 'CONFIRMED'});
            }
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {PROGRAM: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {
            initialEntries: ['/reservations/new?programId=7&startAt=2030-01-02T09:00:00&endAt=2030-01-02T11:00:00'],
        })}/></AuthProvider>);

        await userEvent.type(await screen.findByLabelText(/방문 주소/), '서울 강남구 테헤란로 1');
        await userEvent.type(screen.getByLabelText('요청 사항'), '주차 가능');
        await userEvent.click(screen.getByRole('button', {name: '예약 신청'}));

        await waitFor(() => expect(sent[0]).toMatchObject({
            programId: 7, startAt: '2030-01-02T09:00:00', visitAddress: '서울 강남구 테헤란로 1', requestMemo: '주차 가능',
        }));
    });
});
