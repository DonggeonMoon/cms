import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

const SITE = {id: 1, name: '우리 기관', description: null, domain: null, theme: null, type: null, locale: 'ko', isDefault: true, features: {}};
const ITEM = {
    id: 7, title: '현장 영상', description: '첫 촬영', fileId: 3, fileName: 'field.mp4', mediaType: 'VIDEO',
    mediaUrl: '/api/files/3/view', thumbnailFileId: 4, thumbnailUrl: '/api/files/4/thumbnail', posterUrl: '/api/files/4/view', durationSeconds: 100, completionRate: 90, isUsing: true, createdAt: '2026-11-01T10:00:00',
    myProgress: null,
};
const PROGRESS = {status: 'IN_PROGRESS', progressRate: 0, watchedSeconds: 0, lastPositionSeconds: 0, startedAt: '2026-11-03T10:00:00', lastWatchedAt: null, completedAt: null};

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

function stubApi(member: boolean, calls: { url: string; body: unknown }[] = []) {
    vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
        calls.push({url, body: init?.body ? JSON.parse(String(init.body)) : null});
        switch (url) {
            case '/api/site':
                return respond(200, SITE);
            case '/api/auth/me':
                return member ? respond(200, {id: 1, username: 'learner', name: '학습자', userType: 'MEMBER'}) : respond(401, {code: 'UNAUTHENTICATED'});
            case '/api/media/7':
                return respond(200, ITEM);
            case '/api/media/7/start':
                return respond(200, PROGRESS);
            case '/api/media/7/progress':
                return respond(200, {...PROGRESS, progressRate: 30, watchedSeconds: 30, lastPositionSeconds: 30});
            case '/api/media?mediaType=AUDIO&page=0':
                return respond(200, {content: [{...ITEM, id: 8, title: '봄 노래', mediaType: 'AUDIO'}], page: {size: 12, number: 0, totalElements: 1, totalPages: 1}});
            case '/api/configs':
                return respond(200, {});
            default:
                return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                    ? respond(200, [])
                    : respond(204);
        }
    }));
}

function renderAt(path: string) {
    render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: [path]})}/></AuthProvider>);
}

describe('media pages', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks guests to log in instead of playing', async () => {
        stubApi(false);
        renderAt('/media/7');
        expect(await screen.findByRole('heading', {name: '현장 영상'})).toBeInTheDocument();
        expect(screen.getByText(/회원만 재생할 수 있습니다/)).toBeInTheDocument();
        expect(document.querySelector('video')).toBeNull();
    });

    it('filters the list by music', async () => {
        stubApi(false);
        renderAt('/media');
        await userEvent.click(await screen.findByRole('tab', {name: '음악'}));
        expect(await screen.findByText('봄 노래')).toBeInTheDocument();
        expect(screen.getByText('봄 노래').closest('a')).toHaveAttribute('href', '/media/8');
    });

    it('starts tracking on first play and reports only the played range', async () => {
        const calls: { url: string; body: unknown }[] = [];
        stubApi(true, calls);
        renderAt('/media/7');
        await waitFor(() => expect(document.querySelector('video')).not.toBeNull());
        const video = document.querySelector('video')!;
        expect(video).toHaveAttribute('poster', '/api/files/4/view');
        let time = 0;
        let paused = false;
        Object.defineProperty(video, 'currentTime', {get: () => time, set: (value: number) => (time = value), configurable: true});
        Object.defineProperty(video, 'paused', {get: () => paused, configurable: true});

        fireEvent.play(video);
        fireEvent.playing(video);
        for (const t of [1, 2, 3, 30]) {
            time = t;
            fireEvent.timeUpdate(video);
        }
        time = 31;
        fireEvent.timeUpdate(video);
        paused = true;
        fireEvent.pause(video);

        expect(await screen.findByText('30%')).toBeInTheDocument();
        expect(calls.some((call) => call.url === '/api/media/7/start')).toBe(true);
        const report = calls.find((call) => call.url === '/api/media/7/progress');
        expect(report?.body).toEqual({ranges: [{start: 0, end: 3}, {start: 30, end: 31}], position: 31});
    });
});
