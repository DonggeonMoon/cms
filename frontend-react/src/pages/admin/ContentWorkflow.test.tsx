import {render, screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {ContentAdminPage} from './ContentAdminPages';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const content = (id: number, extra: object) => ({
    id, title: `글 ${id}`, contentHtml: '', status: 'DRAFT', version: 1, lastWorker: 'writer01', memo: '', accessGradeId: null,
    createdAt: '2026-11-01T00:00:00', modifiedAt: '2026-11-01T00:00:00', reviewRequestedBy: null, reviewRequestedAt: null,
    reviewedBy: null, reviewComment: null, publishAt: null, publishedAt: null, ...extra,
});

describe('content publishing workflow', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('requests review with a schedule, approves, rejects with a reason and lists the review queue', async () => {
        const posted: { url: string; body: unknown }[] = [];
        const listed: string[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (init?.method === 'POST') {
                posted.push({url, body: init.body ? JSON.parse(String(init.body)) : null});
                return respond(200, {});
            }
            if (url.startsWith('/api/admin/contents')) {
                listed.push(url);
                return respond(200, {
                    content: [content(1, {}), content(2, {status: 'IN_REVIEW', reviewRequestedBy: 'writer01', publishAt: '2030-01-01T09:00:00'}),
                        content(3, {status: 'REJECTED', reviewComment: '오타'})],
                    page: {size: 20, number: 0, totalElements: 3, totalPages: 1},
                });
            }
            return respond(200, []);
        }));
        vi.stubGlobal('prompt', vi.fn(() => '사진 저작권 확인 필요'));
        render(<MemoryRouter><ContentAdminPage/></MemoryRouter>);

        const draftRow = (await screen.findByText('글 1')).closest('tr')!;
        await userEvent.type(within(draftRow).getByLabelText('글 1 예약 발행 일시'), '2030-01-02T10:00');
        await userEvent.click(within(draftRow).getByRole('button', {name: '검토 요청'}));
        await waitFor(() => expect(posted.at(-1)).toEqual({url: '/api/admin/contents/1/review-request', body: {publishAt: '2030-01-02T10:00:00'}}));

        const reviewRow = screen.getByText('글 2').closest('tr')!;
        expect(within(reviewRow).getByText(/게시 예정/)).toBeInTheDocument();
        await userEvent.click(within(reviewRow).getByRole('button', {name: '승인'}));
        await waitFor(() => expect(posted.at(-1)?.url).toBe('/api/admin/contents/2/approve'));
        await userEvent.click(within(reviewRow).getByRole('button', {name: '반려'}));
        await waitFor(() => expect(posted.at(-1)).toEqual({url: '/api/admin/contents/2/reject', body: {comment: '사진 저작권 확인 필요'}}));

        expect(screen.getByText('반려: 오타')).toBeInTheDocument();
        await userEvent.click(screen.getByLabelText('검토 대기만 보기'));
        await waitFor(() => expect(listed.at(-1)).toMatch(/^\/api\/admin\/contents\/reviews/));
    });
});
