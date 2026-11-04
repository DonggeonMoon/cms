import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const lesson = (id: number, extra: object) => ({
    id, sequence: id, title: `차시 ${id}`, mediaId: null, mediaTitle: null, durationSeconds: null, quiz: [], passScore: null,
    mediaCompleted: false, quizPassed: false, completed: false, locked: false, ...extra,
});

const COURSE = {
    id: 5, title: '기초 과정', description: null, thumbnailFileId: null, thumbnailUrl: null, sequential: true, accessGradeId: null,
    isUsing: true, lessonCount: 2, createdAt: '2026-11-01T00:00:00',
    myProgress: {status: 'IN_PROGRESS', completedLessons: 1, enrolledAt: '2026-11-01T00:00:00', completedAt: null},
    lessons: [
        lesson(1, {mediaId: 9, mediaTitle: '1강 영상', durationSeconds: 600, mediaCompleted: true, completed: true}),
        lesson(2, {quiz: [{question: '1+1은?', options: ['1', '2']}, {question: '하늘 색은?', options: ['파랑', '빨강']}], passScore: 100}),
    ],
};

describe('course pages', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('links the lecture, grades the quiz without knowing answers and shows wrong questions', async () => {
        const submitted: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/auth/me') return respond(200, {id: 1, username: 'student1', name: '학생', userType: 'MEMBER'});
            if (url === '/api/courses/5') return respond(200, COURSE);
            if (url === '/api/courses/5/lessons/2/quiz') {
                submitted.push(JSON.parse(String(init?.body)));
                return respond(200, {score: 50, passScore: 100, passed: false, correct: [true, false], attempts: 1, myProgress: COURSE.myProgress});
            }
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {MEDIA: true}});
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/menus') || url.startsWith('/api/banners') || ['/api/boards', '/api/archives', '/api/contents'].includes(url)
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/courses/5']})}/></AuthProvider>);

        expect(await screen.findByRole('link', {name: '1강 영상'})).toHaveAttribute('href', '/media/9');
        expect(screen.getByText(/1 \/ 2차시 완료/)).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '퀴즈 풀기'}));
        expect(screen.getByRole('button', {name: '제출'})).toBeDisabled();
        await userEvent.click(screen.getByLabelText('2'));
        await userEvent.click(screen.getByLabelText('빨강'));
        await userEvent.click(screen.getByRole('button', {name: '제출'}));

        await waitFor(() => expect(submitted[0]).toEqual({answers: [1, 1]}));
        expect(await screen.findByText(/50점으로 통과 점수\(100점\)에 못 미쳤습니다/)).toBeInTheDocument();
        expect(screen.getAllByText('틀렸습니다.')).toHaveLength(1);
    });
});
