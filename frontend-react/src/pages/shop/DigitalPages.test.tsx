import {render, screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../../auth/AuthContext';
import {routes} from '../../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const access = (id: number, extra: object) => ({
    id, orderId: id * 10, productId: 1, productName: `전자책 ${id}`, downloadLimit: 3, downloadCount: 1, remainingDownloads: 2,
    expiresAt: '2026-12-01T00:00:00', revokedAt: null, usable: true, createdAt: '2026-11-01T00:00:00',
    files: [{id: 7, name: 'book.pdf', size: 2048, downloadUrl: `/api/digital/${id}/files/7`}], ...extra,
});

describe('my downloads', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('links usable files and explains why the others cannot be downloaded', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'reader', name: '독자', userType: 'MEMBER'});
            if (url.startsWith('/api/digital/me')) return respond(200, {
                content: [access(1, {}), access(2, {revokedAt: '2026-11-02T00:00:00', usable: false}),
                    access(3, {remainingDownloads: 0, downloadCount: 3, usable: false})],
                page: {size: 20, number: 0, totalElements: 3, totalPages: 1},
            });
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {PRODUCT: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/mypage/downloads']})}/></AuthProvider>);

        expect(await screen.findByRole('link', {name: 'book.pdf'})).toHaveAttribute('href', '/api/digital/1/files/7');
        expect(screen.getByText(/남은 횟수 0 \/ 3/)).toBeInTheDocument();
        expect(screen.getByText('주문이 취소되었습니다.')).toBeInTheDocument();
        expect(screen.getByText('내려받기 횟수를 모두 썼습니다.')).toBeInTheDocument();
        expect(screen.getAllByRole('link', {name: 'book.pdf'})).toHaveLength(1);
    });
});
