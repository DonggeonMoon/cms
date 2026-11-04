import type {Query} from './client';
import {request} from './client';
import type {EnrollmentStatus} from './domain';
import type {Page} from './types';

export interface QuizQuestion {
    question: string;
    options: string[];
    answerIndex?: number;
}

export interface CourseLesson {
    id: number;
    sequence: number;
    title: string;
    mediaId: number | null;
    mediaTitle: string | null;
    durationSeconds: number | null;
    quiz: QuizQuestion[];
    passScore: number | null;
    mediaCompleted: boolean;
    quizPassed: boolean;
    completed: boolean;
    locked: boolean;
}

export interface MyCourseProgress {
    status: EnrollmentStatus;
    completedLessons: number;
    enrolledAt: string;
    completedAt: string | null;
}

export interface Course {
    id: number;
    title: string;
    description: string | null;
    thumbnailFileId: number | null;
    thumbnailUrl: string | null;
    sequential: boolean;
    accessGradeId: number | null;
    isUsing: boolean;
    lessonCount: number;
    lessons: CourseLesson[] | null;
    myProgress: MyCourseProgress | null;
    createdAt: string;
}

export interface QuizResult {
    score: number;
    passScore: number;
    passed: boolean;
    correct: boolean[];
    attempts: number;
    myProgress: MyCourseProgress | null;
}

export interface CourseLearner {
    memberId: number;
    username: string;
    status: EnrollmentStatus;
    completedLessons: number;
    totalLessons: number;
    enrolledAt: string;
    completedAt: string | null;
}

export interface LessonDraft {
    id: number | null;
    title: string;
    mediaId: number | null;
    passScore: number | null;
    quiz: { question: string; optionsText: string; answerIndex: number }[];
}

export const courseApi = {
    list: (keyword: string, page: number) => request<Page<Course>>('/api/courses', {query: {keyword, page}}),
    get: (id: number) => request<Course>(`/api/courses/${id}`),
    enroll: (id: number) => request<Course>(`/api/courses/${id}/enroll`, {method: 'POST'}),
    submitQuiz: (id: number, lessonId: number, answers: number[]) =>
        request<QuizResult>(`/api/courses/${id}/lessons/${lessonId}/quiz`, {method: 'POST', body: {answers}}),
    mine: (page: number) => request<Page<Course>>('/api/courses/me', {query: {page}}),
};

export const courseAdminApi = {
    search: (query: Query) => request<Page<Course>>('/api/admin/courses', {query}),
    get: (id: number) => request<Course>(`/api/admin/courses/${id}`),
    create: (body: unknown) => request<void>('/api/admin/courses', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<void>(`/api/admin/courses/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/courses/${id}`, {method: 'DELETE'}),
    learners: (id: number, page: number) => request<Page<CourseLearner>>(`/api/admin/courses/${id}/learners`, {query: {page}}),
};

export const toLessonDrafts = (lessons: CourseLesson[]): LessonDraft[] => lessons.map((lesson) => ({
    id: lesson.id,
    title: lesson.title,
    mediaId: lesson.mediaId,
    passScore: lesson.passScore,
    quiz: lesson.quiz.map((q) => ({question: q.question, optionsText: q.options.join(', '), answerIndex: q.answerIndex ?? 0})),
}));

export const lessonBody = (drafts: LessonDraft[]) => drafts.map((draft) => ({
    id: draft.id,
    title: draft.title,
    mediaId: draft.mediaId,
    passScore: draft.quiz.length ? draft.passScore : null,
    quiz: draft.quiz.map((q) => ({
        question: q.question,
        options: q.optionsText.split(',').map((o) => o.trim()).filter(Boolean),
        answerIndex: q.answerIndex,
    })),
}));
