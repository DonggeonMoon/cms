import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {routes} from '../router';

const base = {
    order: 0, link: null, opensNewWindow: false, startDateTime: null, endDateTime: null, isUsing: true,
    width: null, height: null, top: null, left: null, fileId: null, imageUrl: null,
    backgroundColor: null, textColor: null, isModal: false, dismissDays: 1,
};

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

function stub(topBars: unknown[], popups: unknown[]) {
    vi.stubGlobal('fetch', vi.fn(async (url: string) => {
        if (url === '/api/banners?type=BANNER') {
            return respond(200, topBars);
        }
        if (url === '/api/banners?type=POPUP') {
            return respond(200, popups);
        }
        if (url === '/api/auth/me') {
            return respond(401, {code: 'UNAUTHENTICATED'});
        }
        return respond(200, url === '/api/menus' || url === '/api/boards' ? [] : {});
    }));
}

function renderHome() {
    return render(<RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/']})}/>);
}

describe('banners and popups', () => {
    beforeEach(() => localStorage.clear());
    afterEach(() => vi.unstubAllGlobals());

    it('shows the top bar above the header with its colors and hides it for the day', async () => {
        stub([{...base, id: 1, type: 'BANNER', name: '점검', description: '오늘 밤 점검합니다', backgroundColor: '#111111', textColor: '#ffffff'}], []);
        const {unmount} = renderHome();

        const bar = await screen.findByRole('region', {name: '공지 배너'});
        expect(within(bar).getByText('오늘 밤 점검합니다')).toBeInTheDocument();
        expect(bar).toHaveStyle({backgroundColor: '#111111', color: '#ffffff'});
        expect(bar.compareDocumentPosition(screen.getByRole('banner')) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

        await userEvent.click(within(bar).getByRole('button', {name: '오늘 하루 보지 않기'}));
        expect(screen.queryByRole('region', {name: '공지 배너'})).not.toBeInTheDocument();

        unmount();
        renderHome();
        await screen.findByRole('navigation', {name: '계정'});
        expect(screen.queryByRole('region', {name: '공지 배너'})).not.toBeInTheDocument();
    });

    it('keeps a popup hidden for the configured number of days, or offers no dismissal at all', async () => {
        stub([], [
            {...base, id: 7, type: 'POPUP', name: '세일', description: '주말 세일', dismissDays: 3},
            {...base, id: 8, type: 'POPUP', name: '필독', description: '꼭 보세요', dismissDays: 0},
        ]);
        const {unmount} = renderHome();

        const sale = await screen.findByRole('dialog', {name: '세일'});
        expect(within(screen.getByRole('dialog', {name: '필독'})).queryByRole('button', {name: /보지 않기/})).not.toBeInTheDocument();
        await userEvent.click(within(sale).getByRole('button', {name: '3일간 보지 않기'}));

        vi.useFakeTimers({toFake: ['Date'], now: Date.now() + 2 * 24 * 60 * 60 * 1000});
        unmount();
        const second = renderHome();
        expect(await screen.findByRole('dialog', {name: '필독'})).toBeInTheDocument();
        expect(screen.queryByRole('dialog', {name: '세일'})).not.toBeInTheDocument();

        vi.setSystemTime(Date.now() + 24 * 60 * 60 * 1000);
        second.unmount();
        renderHome();
        expect(await screen.findByRole('dialog', {name: '세일'})).toBeInTheDocument();
        vi.useRealTimers();
    });

    it('opens a modal popup over a backdrop that does not close it on click', async () => {
        stub([], [{...base, id: 5, type: 'POPUP', name: '필수 공지', description: '꼭 읽어 주세요', isModal: true}]);
        renderHome();

        const dialog = await screen.findByRole('dialog', {name: '필수 공지'});
        expect(dialog).toHaveAttribute('aria-modal', 'true');
        await userEvent.click(screen.getByTestId('modal-backdrop'));
        expect(screen.getByRole('dialog', {name: '필수 공지'})).toBeInTheDocument();
        expect(document.body.style.overflow).toBe('hidden');

        await userEvent.click(within(dialog).getByRole('button', {name: '닫기'}));
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        expect(document.body.style.overflow).toBe('');
    });

    it('shows a modeless popup without a backdrop', async () => {
        stub([], [{...base, id: 6, type: 'POPUP', name: '이벤트', description: '이벤트 안내'}]);
        renderHome();

        const dialog = await screen.findByRole('dialog', {name: '이벤트'});
        expect(dialog).not.toHaveAttribute('aria-modal');
        expect(screen.queryByTestId('modal-backdrop')).not.toBeInTheDocument();
    });
});
