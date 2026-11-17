import type {Query} from './client';
import {request} from './client';
import type {Comment, EnrollmentStatus} from './domain';
import type {FileInfo, Page} from './types';

export interface MaterialInfo {
    id: number;
    title: string;
    mimeType: string | null;
    size: number | null;
    downloadUrl: string;
}

export interface QuizQuestion {
    question: string;
    options: string[];
    answerIndex?: number;
    explanation?: string | null;
}

export type RevealPolicy = 'IMMEDIATELY' | 'AFTER_PASS' | 'NEVER';
export type ScorePolicy = 'HIGHEST' | 'LATEST';

export interface Quiz {
    id: number;
    title: string;
    questions: QuizQuestion[];
    passScore: number;
    isUsing: boolean;
    retakeLimit: number | null;
    timeLimitMinutes: number | null;
    shuffleQuestions: boolean;
    shuffleOptions: boolean;
    revealPolicy: RevealPolicy;
    scorePolicy: ScorePolicy;
    itemCount: number;
    createdAt: string;
}

export interface CourseLesson {
    id: number;
    sequence: number;
    title: string;
    unitName: string | null;
    streamingId: number | null;
    streamingTitle: string | null;
    durationSeconds: number | null;
    quizId: number | null;
    quizTitle: string | null;
    quiz: QuizQuestion[];
    passScore: number | null;
    streamingCompleted: boolean;
    progressRate: number | null;
    lastPositionSeconds: number | null;
    quizPassed: boolean;
    quizScore: number | null;
    completed: boolean;
    locked: boolean;
    materials: MaterialInfo[];
    mandatory: boolean;
    preventSkipping: boolean;
    preventSkippingOverride: boolean | null;
    attemptsUsed: number | null;
    attemptsAllowed: number | null;
}

export interface MyCourseProgress {
    status: EnrollmentStatus;
    completedLessons: number;
    enrolledAt: string;
    completedAt: string | null;
}

export type EnrollmentMode = 'FREE' | 'APPLY' | 'APPROVAL';
export type CourseApplicationStatus = 'APPLIED' | 'APPROVED' | 'REJECTED' | 'CANCELED';
export type AccessPeriodMode = 'UNLIMITED' | 'FIXED' | 'DAYS_FROM_ENROLLMENT';

export interface Course {
    id: number;
    title: string;
    description: string | null;
    thumbnailFileId: number | null;
    thumbnailUrl: string | null;
    categoryId: number | null;
    sequential: boolean;
    accessGradeId: number | null;
    isUsing: boolean;
    isCourse: boolean;
    enrollmentMode: EnrollmentMode;
    applicationOpen: boolean;
    applicationStartAt: string | null;
    applicationEndAt: string | null;
    accessPeriodMode: AccessPeriodMode;
    accessStartAt: string | null;
    accessEndAt: string | null;
    accessDays: number | null;
    myAccessUntil: string | null;
    myAccessOpen: boolean | null;
    maxPlaybackRate: number | null;
    preventSkipping: boolean;
    completionThresholdPercent: number;
    attendanceRequired: boolean;
    myAttendanceOk: boolean | null;
    paid: boolean;
    price: number | null;
    productId: number | null;
    refundableProgressBelowPercent: number | null;
    trackProgress: boolean;
    completionRate: number;
    lessonCount: number;
    lessons: CourseLesson[] | null;
    materials: MaterialInfo[] | null;
    myProgress: MyCourseProgress | null;
    myApplicationStatus: CourseApplicationStatus | null;
    createdAt: string;
}

export interface CourseApplication {
    id: number;
    memberId: number;
    username: string;
    status: CourseApplicationStatus;
    appliedAt: string;
    decidedAt: string | null;
}

export interface QuizResult {
    score: number;
    passScore: number;
    passed: boolean;
    correct: boolean[];
    durationSeconds: number;
    attempts: number;
    attemptsRemaining: number | null;
    revealed: boolean;
    answerIndexes: number[] | null;
    explanations: (string | null)[] | null;
    myProgress: MyCourseProgress | null;
}

export interface QuizStartQuestion {
    question: string;
    options: string[];
}

export interface QuizStartResponse {
    questions: QuizStartQuestion[];
    timeLimitMinutes: number | null;
    attemptsUsed: number;
    attemptsAllowed: number | null;
    startedAt: string;
}

export interface CourseLearner {
    memberId: number;
    username: string;
    status: EnrollmentStatus;
    completedLessons: number;
    totalLessons: number;
    enrolledAt: string;
    completedAt: string | null;
    accessUntil: string | null;
    accessExtendedUntil: string | null;
    attendanceOk: boolean | null;
}

export interface CourseStatisticsRow {
    courseId: number;
    title: string;
    isCourse: boolean;
    paid: boolean;
    totalEnrolled: number;
    inProgress: number;
    completed: number;
    revoked: number;
    completionRate: number;
    pendingApplications: number;
    approvedApplications: number;
    rejectedApplications: number;
    canceledApplications: number;
    revenue: number;
    paidCount: number;
    refundedCount: number;
    pendingPaymentCount: number;
}

export interface CourseStatisticsResponse {
    rows: CourseStatisticsRow[];
    totalRevenue: number;
    totalEnrolled: number;
    totalCompleted: number;
}

export interface UnansweredQuestion {
    commentId: number;
    courseId: number | null;
    courseTitle: string;
    isCourse: boolean;
    lessonId: number;
    lessonTitle: string;
    authorName: string | null;
    content: string;
    createdAt: string;
}

export interface LessonDraft {
    id: number | null;
    title: string;
    unitName: string;
    streamingId: number | null;
    quizId: number | null;
    materials: FileInfo[];
    mandatory: boolean;
    preventSkippingOverride: boolean | null;
}

const materialAsFileInfo = (material: MaterialInfo): FileInfo => ({
    id: material.id,
    originalName: material.title,
    extension: '',
    mimeType: material.mimeType ?? '',
    mediaType: 'ETC',
    size: material.size ?? 0,
    alternativeText: null,
    downloadCount: 0,
    downloadUrl: material.downloadUrl,
    thumbnailUrl: null,
    createdAt: '',
});

export interface QuizQuestionStat {
    question: string;
    attemptCount: number;
    wrongCount: number;
    wrongRate: number;
}

export interface QuizStatistics {
    quizId: number;
    title: string;
    totalAttempts: number;
    uniqueTestTakers: number;
    averageScore: number;
    passRate: number;
    questions: QuizQuestionStat[];
}

export const quizAdminApi = {
    search: (query: Query) => request<Page<Quiz>>('/api/admin/quizzes', {query}),
    get: (id: number) => request<Quiz>(`/api/admin/quizzes/${id}`),
    create: (body: unknown) => request<void>('/api/admin/quizzes', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<void>(`/api/admin/quizzes/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/quizzes/${id}`, {method: 'DELETE'}),
    statistics: (id: number) => request<QuizStatistics>(`/api/admin/quizzes/${id}/statistics`),
};

export const courseApi = {
    list: (keyword: string, page: number, isCourse?: boolean) =>
        request<Page<Course>>('/api/courses', {query: {keyword, page, isCourse}}),
    get: (id: number) => request<Course>(`/api/courses/${id}`),
    enroll: (id: number) => request<Course>(`/api/courses/${id}/enroll`, {method: 'POST'}),
    cancelApplication: (id: number) => request<void>(`/api/courses/${id}/applications/cancel`, {method: 'POST'}),
    startQuiz: (id: number, lessonId: number) =>
        request<QuizStartResponse>(`/api/courses/${id}/lessons/${lessonId}/quiz/start`, {method: 'POST'}),
    submitQuiz: (id: number, lessonId: number, answers: number[]) =>
        request<QuizResult>(`/api/courses/${id}/lessons/${lessonId}/quiz`, {method: 'POST', body: {answers}}),
    mine: (page: number) => request<Page<Course>>('/api/courses/me', {query: {page}}),
    comments: (id: number, lessonId: number) => request<Comment[]>(`/api/courses/${id}/lessons/${lessonId}/comments`),
    addComment: (id: number, lessonId: number, parentId: number | null, content: string) =>
        request<void>(`/api/courses/${id}/lessons/${lessonId}/comments`, {method: 'POST', body: {parentId, content}}),
    editComment: (id: number, lessonId: number, commentId: number, content: string) =>
        request<void>(`/api/courses/${id}/lessons/${lessonId}/comments/${commentId}`, {method: 'PUT', body: {content}}),
    removeComment: (id: number, lessonId: number, commentId: number) =>
        request<void>(`/api/courses/${id}/lessons/${lessonId}/comments/${commentId}`, {method: 'DELETE'}),
};

export const courseAdminApi = {
    search: (query: Query) => request<Page<Course>>('/api/admin/courses', {query}),
    statistics: () => request<CourseStatisticsResponse>('/api/admin/courses/statistics'),
    unansweredQuestions: (page: number) => request<Page<UnansweredQuestion>>('/api/admin/courses/qna/unanswered', {query: {page}}),
    get: (id: number) => request<Course>(`/api/admin/courses/${id}`),
    create: (body: unknown) => request<void>('/api/admin/courses', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<void>(`/api/admin/courses/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/courses/${id}`, {method: 'DELETE'}),
    learners: (id: number, page: number) => request<Page<CourseLearner>>(`/api/admin/courses/${id}/learners`, {query: {page}}),
    learnerProgress: (id: number, memberId: number) => request<Course>(`/api/admin/courses/${id}/learners/${memberId}`),
    applications: (id: number, page: number) => request<Page<CourseApplication>>(`/api/admin/courses/${id}/applications`, {query: {page}}),
    approveApplication: (id: number, applicationId: number) =>
        request<void>(`/api/admin/courses/${id}/applications/${applicationId}/approve`, {method: 'POST'}),
    rejectApplication: (id: number, applicationId: number) =>
        request<void>(`/api/admin/courses/${id}/applications/${applicationId}/reject`, {method: 'POST'}),
    extendAccess: (id: number, memberId: number, accessUntil: string | null) =>
        request<void>(`/api/admin/courses/${id}/learners/${memberId}/extend`, {method: 'POST', body: {accessUntil}}),
    refundEnrollment: (id: number, memberId: number) =>
        request<void>(`/api/admin/courses/${id}/learners/${memberId}/refund`, {method: 'POST'}),
    grantQuizAttempts: (id: number, lessonId: number, memberId: number, count: number) =>
        request<void>(`/api/admin/courses/${id}/lessons/${lessonId}/learners/${memberId}/grant-attempts`, {method: 'POST', body: {count}}),
};

export const toLessonDrafts = (lessons: CourseLesson[]): LessonDraft[] => lessons.map((lesson) => ({
    id: lesson.id,
    title: lesson.title,
    unitName: lesson.unitName ?? '',
    streamingId: lesson.streamingId,
    quizId: lesson.quizId,
    materials: lesson.materials.map(materialAsFileInfo),
    mandatory: lesson.mandatory,
    preventSkippingOverride: lesson.preventSkippingOverride,
}));

export const lessonBody = (drafts: LessonDraft[]) => drafts.map((draft) => ({
    id: draft.id,
    title: draft.title,
    unitName: draft.unitName || null,
    streamingId: draft.streamingId,
    quizId: draft.quizId,
    materialFileIds: draft.materials.map((file) => file.id),
    mandatory: draft.mandatory,
    preventSkippingOverride: draft.preventSkippingOverride,
}));

export const materialsToFileInfos = (materials: MaterialInfo[]): FileInfo[] => materials.map(materialAsFileInfo);
