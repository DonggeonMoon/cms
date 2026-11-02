import {type Query, request} from './client';
import type * as D from './domain';
import type {Page} from './types';

type Body = Record<string, unknown>;

export interface Resource<T> {
    search: (query: Query) => Promise<Page<T>>;
    get: (id: number) => Promise<T>;
    create: (body: Body) => Promise<void>;
    update: (id: number, body: Body) => Promise<void>;
    remove: (id: number) => Promise<void>;
}

export function resource<T>(listPath: string, itemPath: (id: number) => string = (id) => `${listPath}/${id}`,
                            createPath = listPath, writePath = itemPath): Resource<T> {
    return {
        search: (query) => request<Page<T>>(listPath, {query}),
        get: (id) => request<T>(itemPath(id)),
        create: (body) => request<void>(createPath, {method: 'POST', body}),
        update: (id, body) => request<void>(writePath(id), {method: 'PUT', body}),
        remove: (id) => request<void>(writePath(id), {method: 'DELETE'}),
    };
}

export const siteApi = {
    current: () => request<D.Site>('/api/site'),
    configs: () => request<Record<string, string>>('/api/configs'),
    menus: () => request<D.Menu[]>('/api/menus'),
    banners: (type?: 'BANNER' | 'POPUP') => request<D.Banner[]>('/api/banners', {query: {type}}),
    contents: () => request<D.ContentSummary[]>('/api/contents'),
    content: (id: number) => request<D.Content>(`/api/contents/${id}`),
    map: () => request<D.PublicMap>('/api/map'),
    policy: (type: D.PolicyType) => request<D.Policy>(`/api/policies/${type}`),
    trackVisit: (path: string, referrer: string) =>
        request<void>('/api/statistics/visits', {method: 'POST', body: {path, referrer}}),
};

export const adminApi = {
    sites: resource<D.Site>('/api/admin/sites'),
    policies: {
        get: (type: D.PolicyType) => request<D.Policy>(`/api/admin/policies/${type}`),
        save: (type: D.PolicyType, contentHtml: string) =>
            request<D.Policy>(`/api/admin/policies/${type}`, {method: 'PUT', body: {contentHtml}}),
    },
    map: {
        settings: () => request<D.MapSetting>('/api/admin/map/settings'),
        updateSettings: (body: Body) => request<D.MapSetting>('/api/admin/map/settings', {method: 'PUT', body}),
        branches: resource<D.Branch>('/api/admin/map/branches'),
    },
    configs: resource<D.Config>('/api/admin/configs'),
    banners: resource<D.Banner>('/api/admin/banners'),
    contents: resource<D.Content>('/api/admin/contents'),
    discounts: resource<D.Discount>('/api/admin/discounts'),
    products: resource<D.ProductDetail>('/api/products', (id) => `/api/products/${id}`, '/api/admin/products',
        (id) => `/api/admin/products/${id}`),
    programs: resource<D.Program>('/api/programs', (id) => `/api/programs/${id}`, '/api/admin/programs',
        (id) => `/api/admin/programs/${id}`),
    menus: {
        tree: () => request<D.Menu[]>('/api/admin/menus'),
        create: (body: Body) => request<void>('/api/admin/menus', {method: 'POST', body}),
        update: (id: number, body: Body) => request<void>(`/api/admin/menus/${id}`, {method: 'PUT', body}),
        reorder: (parentId: number | null, ids: number[]) =>
            request<void>('/api/admin/menus/order', {method: 'PUT', body: {parentId, ids}}),
        remove: (id: number) => request<void>(`/api/admin/menus/${id}`, {method: 'DELETE'}),
    },
    boards: {
        list: () => request<D.Board[]>('/api/boards'),
        create: (body: Body) => request<void>('/api/admin/boards', {method: 'POST', body}),
        update: (id: number, body: Body) => request<void>(`/api/admin/boards/${id}`, {method: 'PUT', body}),
        remove: (id: number) => request<void>(`/api/admin/boards/${id}`, {method: 'DELETE'}),
    },
    archives: {
        list: () => request<D.Archive[]>('/api/archives'),
        create: (body: Body) => request<void>('/api/admin/archives', {method: 'POST', body}),
        update: (id: number, body: Body) => request<void>(`/api/admin/archives/${id}`, {method: 'PUT', body}),
        remove: (id: number) => request<void>(`/api/admin/archives/${id}`, {method: 'DELETE'}),
        articles: (archiveId: number) => resource<D.ArchiveArticleDetail>(
            `/api/archives/${archiveId}/articles`,
            (id) => `/api/archives/${archiveId}/articles/${id}`,
            `/api/admin/archives/${archiveId}/articles`,
            (id) => `/api/admin/archives/${archiveId}/articles/${id}`),
    },
    questions: {
        answer: (id: number, contentHtml: string) =>
            request<void>(`/api/admin/questions/${id}/answer`, {method: 'PUT', body: {contentHtml}}),
        deleteAnswer: (id: number) => request<void>(`/api/admin/questions/${id}/answer`, {method: 'DELETE'}),
    },
    orders: {
        search: (query: Query) => request<Page<D.Order>>('/api/admin/orders', {query}),
        create: (body: Body, paid: boolean) => request<D.Order>('/api/admin/orders', {method: 'POST', body, query: {paid}}),
        markPaid: (id: number) => request<void>(`/api/admin/orders/${id}/pay`, {method: 'POST'}),
        changeStatus: (id: number, status: D.OrderStatus) =>
            request<void>(`/api/admin/orders/${id}/status`, {method: 'PUT', body: {status}}),
    },
    reservations: {
        search: (query: Query) => request<Page<D.Reservation>>('/api/admin/reservations', {query}),
        create: (body: Body, paid: boolean) =>
            request<D.Reservation>('/api/admin/reservations', {method: 'POST', body, query: {paid}}),
        confirm: (id: number) => request<void>(`/api/admin/reservations/${id}/confirm`, {method: 'POST'}),
        cancel: (id: number) => request<void>(`/api/admin/reservations/${id}/cancel`, {method: 'POST'}),
    },
    payments: (query: Query) => request<Page<D.Payment>>('/api/admin/payments', {query}),
    cryptoKey: {
        get: () => request<D.CryptoKey>('/api/admin/crypto-key'),
        regenerate: () => request<D.ReencryptResult>('/api/admin/crypto-key/regenerate', {method: 'POST'}),
        changeAlgorithm: (algorithm: D.CryptoAlgorithm) =>
            request<D.ReencryptResult>('/api/admin/crypto-key/algorithm', {method: 'PUT', body: {algorithm}}),
    },
    reviews: (query: Query) => request<Page<D.Review>>('/api/admin/reviews', {query}),
    reports: {
        search: (query: Query) => request<Page<D.ReportSummary>>('/api/admin/reports', {query}),
        get: (id: number) => request<D.ReportDetail>(`/api/reports/${id}`),
        changeStatus: (id: number, status: D.ReportStatus) =>
            request<void>(`/api/admin/reports/${id}/status`, {method: 'PUT', body: {status}}),
        answer: (id: number, title: string, contentHtml: string) =>
            request<void>(`/api/admin/reports/${id}/answers`, {method: 'POST', body: {title, contentHtml}}),
        deleteAnswer: (id: number, answerId: number) =>
            request<void>(`/api/admin/reports/${id}/answers/${answerId}`, {method: 'DELETE'}),
    },
    satisfactions: {
        list: () => request<D.Satisfaction[]>('/api/admin/satisfactions'),
        opinions: (menuId: number) =>
            request<Page<{ score: number; opinion: string; createdAt: string }>>(`/api/admin/satisfactions/${menuId}/opinions`),
        reset: (menuId: number) => request<void>(`/api/admin/satisfactions/${menuId}`, {method: 'DELETE'}),
    },
    auditLogs: (query: Query) => request<Page<D.AuditLog>>('/api/admin/audit-logs', {query}),
    dashboard: () => request<D.Dashboard>('/api/admin/dashboard'),
    statistics: {
        daily: (from: string, to: string) => request<D.DailyVisits[]>('/api/admin/statistics/daily', {query: {from, to}}),
        breakdown: (dimension: 'BROWSER' | 'OS' | 'URL', from: string, to: string) =>
            request<{ label: string; visits: number }[]>('/api/admin/statistics/breakdown', {query: {dimension, from, to}}),
    },
};

export const boardApi = {
    list: () => request<D.Board[]>('/api/boards'),
    get: (boardId: number) => request<D.Board>(`/api/boards/${boardId}`),
    articles: (boardId: number, keyword: string, page: number) =>
        request<{ notices: D.ArticleSummary[]; articles: Page<D.ArticleSummary> }>(
            `/api/boards/${boardId}/articles`, {query: {keyword, page}}),
    article: (boardId: number, id: number) => request<D.ArticleDetail>(`/api/boards/${boardId}/articles/${id}`),
    access: (boardId: number, id: number, password: string) =>
        request<D.ArticleDetail>(`/api/boards/${boardId}/articles/${id}/access`, {method: 'POST', body: {password}}),
    create: (boardId: number, body: Body) =>
        request<void>(`/api/boards/${boardId}/articles`, {method: 'POST', body}),
    update: (boardId: number, id: number, body: Body) =>
        request<void>(`/api/boards/${boardId}/articles/${id}`, {method: 'PUT', body}),
    remove: (boardId: number, id: number, password?: string) =>
        request<void>(`/api/boards/${boardId}/articles/${id}`, {method: 'DELETE', body: {password}}),
    like: (boardId: number, id: number) =>
        request<{ likes: number }>(`/api/boards/${boardId}/articles/${id}/likes`, {method: 'POST'}),
    comments: (boardId: number, id: number) => request<D.Comment[]>(`/api/boards/${boardId}/articles/${id}/comments`),
    addComment: (boardId: number, id: number, body: Body) =>
        request<void>(`/api/boards/${boardId}/articles/${id}/comments`, {method: 'POST', body}),
    removeComment: (boardId: number, id: number, commentId: number, password?: string) =>
        request<void>(`/api/boards/${boardId}/articles/${id}/comments/${commentId}`, {method: 'DELETE', body: {password}}),
};

export const qnaApi = {
    list: (keyword: string, page: number) =>
        request<Page<D.QuestionSummary>>('/api/questions', {query: {keyword, page, size: 10}}),
    get: (id: number) => request<D.QuestionDetail>(`/api/questions/${id}`),
    access: (id: number, password: string) =>
        request<D.QuestionDetail>(`/api/questions/${id}/access`, {method: 'POST', body: {password}}),
    create: (body: Body) => request<void>('/api/questions', {method: 'POST', body}),
    update: (id: number, body: Body) => request<void>(`/api/questions/${id}`, {method: 'PUT', body}),
    remove: (id: number, password?: string) => request<void>(`/api/questions/${id}`, {method: 'DELETE', body: {password}}),
};

export const archiveApi = {
    list: () => request<D.Archive[]>('/api/archives'),
    get: (id: number) => request<D.Archive>(`/api/archives/${id}`),
    articles: (id: number, keyword: string, page: number) =>
        request<Page<D.ArchiveArticleSummary>>(`/api/archives/${id}/articles`, {query: {keyword, page}}),
    article: (id: number, articleId: number) =>
        request<D.ArchiveArticleDetail>(`/api/archives/${id}/articles/${articleId}`),
    comments: (id: number, articleId: number) =>
        request<D.ArchiveComment[]>(`/api/archives/${id}/articles/${articleId}/comments`),
    addComment: (id: number, articleId: number, content: string) =>
        request<void>(`/api/archives/${id}/articles/${articleId}/comments`, {method: 'POST', body: {content}}),
    removeComment: (id: number, articleId: number, commentId: number) =>
        request<void>(`/api/archives/${id}/articles/${articleId}/comments/${commentId}`, {method: 'DELETE'}),
};

export const shopApi = {
    products: (keyword: string, page: number) =>
        request<Page<D.ProductSummary>>('/api/products', {query: {keyword, page}}),
    product: (id: number) => request<D.ProductDetail>(`/api/products/${id}`),
    reviews: (id: number, page: number) => request<Page<D.Review>>(`/api/products/${id}/reviews`, {query: {page}}),
    addReview: (id: number, body: Body) => request<void>(`/api/products/${id}/reviews`, {method: 'POST', body}),
    removeReview: (id: number) => request<void>(`/api/reviews/${id}`, {method: 'DELETE'}),
    order: (body: Body) => request<D.Order>('/api/orders', {method: 'POST', body}),
    myOrders: (page: number) => request<Page<D.Order>>('/api/orders/me', {query: {page}}),
    myOrder: (id: number) => request<D.Order>(`/api/orders/${id}`),
    cancelOrder: (id: number) => request<void>(`/api/orders/${id}/cancel`, {method: 'POST'}),
    programs: (keyword: string, page: number) =>
        request<Page<D.Program>>('/api/programs', {query: {keyword, page}}),
    program: (id: number) => request<D.Program>(`/api/programs/${id}`),
    programReviews: (id: number, page: number) => request<Page<D.Review>>(`/api/programs/${id}/reviews`, {query: {page}}),
    addProgramReview: (id: number, body: Body) => request<void>(`/api/programs/${id}/reviews`, {method: 'POST', body}),
    reserve: (body: Body) => request<D.Reservation>('/api/reservations', {method: 'POST', body}),
    myReservations: (page: number) => request<Page<D.Reservation>>('/api/reservations/me', {query: {page}}),
    cancelReservation: (id: number) => request<void>(`/api/reservations/${id}/cancel`, {method: 'POST'}),
    pay: (targetType: D.PaymentTargetType, targetId: number, amount: number, method: D.PaymentMethod) =>
        request<D.Payment>('/api/payments', {method: 'POST', body: {targetType, targetId, amount, method}}),
};

export const govApi = {
    createReport: (body: Body) => request<{ id: number }>('/api/reports', {method: 'POST', body}),
    myReports: (page: number) => request<Page<D.ReportSummary>>('/api/reports/me', {query: {page}}),
    report: (id: number) => request<D.ReportDetail>(`/api/reports/${id}`),
    accessReport: (id: number, password: string) =>
        request<D.ReportDetail>(`/api/reports/${id}/access`, {method: 'POST', body: {password}}),
    satisfaction: (menuId: number) => request<D.Satisfaction>('/api/satisfactions', {query: {menuId}}),
    vote: (menuId: number, score: number, opinion: string) =>
        request<D.Satisfaction>('/api/satisfactions', {method: 'POST', body: {menuId, score, opinion}}),
};
