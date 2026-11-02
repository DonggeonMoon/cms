import {render, screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

const OFFICE = {
    name: '본점', address: '서울 중구 세종대로 110', detailAddress: '1층', latitude: 37.5666103, longitude: 126.9783882,
    phoneNumber: '02-120', businessHours: '평일 09:00~18:00', description: '시청역 4번 출구',
};
const BRANCH = {
    id: 1, name: '강남점', address: '서울 강남구 테헤란로 1', detailAddress: null, latitude: 37.498, longitude: 127.027,
    phoneNumber: null, businessHours: null, description: null, sequence: 0, isUsing: true,
};

const API_KEYS = [{id: 7, name: '카카오맵 운영 키'}, {id: 8, name: '네이버 지도 키'}];
const EMPTY_OFFICE = {
    name: null, address: null, detailAddress: null, latitude: null, longitude: null, phoneNumber: null,
    businessHours: null, description: null,
};

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

function stub(map: unknown, user: unknown = null, adminSettings?: unknown) {
    vi.stubGlobal('fetch', vi.fn(async (url: string) => {
        if (url === '/api/map') {
            return respond(200, map);
        }
        if (url === '/api/auth/me') {
            return user ? respond(200, user) : respond(401, {code: 'UNAUTHENTICATED'});
        }
        if (url === '/api/admin/map/settings') {
            return respond(200, adminSettings);
        }
        if (url === '/api/admin/api-keys/options') {
            return respond(200, API_KEYS);
        }
        return respond(200, url === '/api/menus' ? [] : {});
    }));
}

function renderAt(path: string) {
    render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: [path]})}/></AuthProvider>);
}

describe('map page', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
        document.head.querySelectorAll('script[src*="dapi.kakao.com"], script[src*="oapi.map.naver.com"]').forEach((s) => s.remove());
    });

    it('falls back to map-app links when no key is configured and shows only enabled tabs', async () => {
        stub({provider: 'KAKAO', apiKey: null, directions: OFFICE, branches: [BRANCH]});
        renderAt('/map');

        expect(await screen.findByRole('tab', {name: '오시는 길'})).toBeInTheDocument();
        expect(screen.getByText('서울 중구 세종대로 110 1층')).toBeInTheDocument();
        expect(screen.getAllByRole('link', {name: '카카오맵 길찾기'})[0]).toHaveAttribute(
            'href', `https://map.kakao.com/link/to/${encodeURIComponent('본점')},37.5666103,126.9783882`);

        await userEvent.click(screen.getByRole('tab', {name: '지점 안내'}));
        expect(await screen.findByRole('button', {name: '강남점'})).toBeInTheDocument();
    });

    it('shows a single section without tabs and says so when nothing is enabled', async () => {
        stub({provider: 'NAVER', apiKey: null, directions: null, branches: [BRANCH]});
        renderAt('/map');
        expect(await screen.findByRole('button', {name: '강남점'})).toBeInTheDocument();
        expect(screen.queryByRole('tab')).not.toBeInTheDocument();
        expect(screen.getAllByRole('link', {name: '네이버 지도에서 보기'})[0]).toHaveAttribute(
            'href', `https://map.naver.com/p/search/${encodeURIComponent(BRANCH.address)}`);
    });

    it('loads the selected provider SDK with its key', async () => {
        stub({provider: 'KAKAO', apiKey: 'kakao-js-key', directions: OFFICE, branches: null});
        renderAt('/map');

        await waitFor(() => expect(document.head.querySelector('script[src*="dapi.kakao.com"]')).not.toBeNull());
        expect(document.head.querySelector('script[src*="dapi.kakao.com"]')?.getAttribute('src'))
            .toContain('appkey=kakao-js-key');
    });

    it('tells the admin to switch branch guidance on before registering branches', async () => {
        stub(null, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'}, {
            provider: 'KAKAO', kakaoApiKeyId: null, naverApiKeyId: null, apiKey: null, directionsEnabled: false, branchesEnabled: false,
            office: EMPTY_OFFICE,
        });
        renderAt('/admin/map');

        await userEvent.click(await screen.findByRole('tab', {name: /지점 안내/}));
        expect(screen.getByText('지점 안내를 켜고 저장하면 지점을 등록할 수 있습니다.')).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: '지점 등록'})).not.toBeInTheDocument();
    });

    it('lets the admin pick the map key by the name registered in API key management', async () => {
        stub(null, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'}, {
            provider: 'KAKAO', kakaoApiKeyId: 7, naverApiKeyId: null, apiKey: null, directionsEnabled: false, branchesEnabled: false,
            office: EMPTY_OFFICE,
        });
        renderAt('/admin/map');

        const select = await screen.findByLabelText('카카오맵 JavaScript 키');
        expect(select).toHaveDisplayValue('카카오맵 운영 키');
        expect(screen.getByRole('option', {name: '네이버 지도 키'})).toBeInTheDocument();
        expect(within(select.closest('form')!).getByRole('link', {name: 'API 키 관리'})).toHaveAttribute('href', '/admin/api-keys');
    });
});
