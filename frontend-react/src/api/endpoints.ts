import {type Query, request} from './client';
import type * as D from './domain';
import type {MediaType, Page} from './types';

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
        save: (type: D.PolicyType, contentHtml: string, isUsing = true) =>
            request<D.Policy>(`/api/admin/policies/${type}`, {method: 'PUT', body: {contentHtml, isUsing}}),
    },
    map: {
        settings: () => request<D.MapSetting>('/api/admin/map/settings'),
        updateSettings: (body: Body) => request<D.MapSetting>('/api/admin/map/settings', {method: 'PUT', body}),
        branches: resource<D.Branch>('/api/admin/map/branches'),
    },
    reauthenticate: (password: string) => request<void>('/api/admin/reauthenticate', {method: 'POST', body: {password}}),
    configs: resource<D.Config>('/api/admin/configs'),
    media: {
        ...resource<D.MediaItem>('/api/admin/media'),
        learners: (id: number, page: number) =>
            request<Page<D.Learner>>(`/api/admin/media/${id}/learners`, {query: {page}}),
    },
    chatbot: {
        get: () => request<D.ChatbotSetting>('/api/admin/chatbot'),
        save: (body: Omit<D.ChatbotSetting, 'defaultInstructions'>) =>
            request<D.ChatbotSetting>('/api/admin/chatbot', {method: 'PUT', body}),
    },
    apiKeys: {
        ...resource<D.ApiKey>('/api/admin/api-keys'),
        options: () => request<D.ApiKeyOption[]>('/api/admin/api-keys/options'),
    },
    banners: resource<D.Banner>('/api/admin/banners'),
    contents: {
        ...resource<D.Content>('/api/admin/contents'),
        reviews: (query: Query) => request<Page<D.Content>>('/api/admin/contents/reviews', {query}),
        requestReview: (id: number, publishAt: string | null) =>
            request<D.Content>(`/api/admin/contents/${id}/review-request`, {method: 'POST', body: {publishAt}}),
        approve: (id: number) => request<D.Content>(`/api/admin/contents/${id}/approve`, {method: 'POST'}),
        reject: (id: number, comment: string) => request<D.Content>(`/api/admin/contents/${id}/reject`, {method: 'POST', body: {comment}}),
        unpublish: (id: number) => request<D.Content>(`/api/admin/contents/${id}/unpublish`, {method: 'POST'}),
    },
    discounts: resource<D.Discount>('/api/admin/discounts'),
    products: {
        ...resource<D.ProductDetail>('/api/products', (id) => `/api/products/${id}`, '/api/admin/products',
            (id) => `/api/admin/products/${id}`),
        resolveFunding: () => request<{ failed: number }>('/api/admin/products/funding/resolve', {method: 'POST'}),
        gradePrices: (id: number) => request<D.GradePrice[]>(`/api/admin/products/${id}/grade-prices`),
        saveGradePrices: (id: number, gradePrices: D.GradePrice[]) =>
            request<D.GradePrice[]>(`/api/admin/products/${id}/grade-prices`, {method: 'PUT', body: gradePrices}),
    },
    programs: resource<D.Program>('/api/reservation-items', (id) => `/api/reservation-items/${id}`, '/api/admin/reservation-items',
        (id) => `/api/admin/reservation-items/${id}`),
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
        setGuestPosting: (body: D.GuestPostingPolicy) =>
            request<D.GuestPostingPolicy>('/api/admin/questions/guest-posting', {method: 'PUT', body}),
    },
    orders: {
        search: (query: Query) => request<Page<D.Order>>('/api/admin/orders', {query}),
        create: (body: Body, paid: boolean) => request<D.Order>('/api/admin/orders', {method: 'POST', body, query: {paid}}),
        markPaid: (id: number) => request<void>(`/api/admin/orders/${id}/pay`, {method: 'POST'}),
        changeStatus: (id: number, status: D.OrderStatus) =>
            request<void>(`/api/admin/orders/${id}/status`, {method: 'PUT', body: {status}}),
        quote: (id: number, amount: number, message: string) =>
            request<D.Order>(`/api/admin/orders/${id}/quote`, {method: 'POST', body: {amount, message}}),
        cancelItem: (id: number, itemId: number) =>
            request<D.Order>(`/api/admin/orders/${id}/items/${itemId}/cancel`, {method: 'POST'}),
        ship: (id: number, courier: string, trackingNumber: string) =>
            request<D.Order>(`/api/admin/orders/${id}/shipment`, {method: 'PUT', body: {courier, trackingNumber}}),
    },
    returnRequests: {
        search: (query: Query) => request<Page<D.OrderReturn>>('/api/admin/orders/return-requests', {query}),
        approve: (id: number) => request<D.OrderReturn>(`/api/admin/orders/return-requests/${id}/approve`, {method: 'POST'}),
        reject: (id: number, adminMemo: string) =>
            request<D.OrderReturn>(`/api/admin/orders/return-requests/${id}/reject`, {method: 'POST', body: {adminMemo}}),
    },
    licenseKeys: {
        addBulk: (productId: number, codes: string[]) =>
            request<{ added: number; duplicate: number }>('/api/admin/license-keys/bulk', {method: 'POST', body: {productId, codes}}),
        search: (productId: number, page: number) =>
            request<Page<D.LicenseKey>>('/api/admin/license-keys', {query: {productId, page}}),
        availableCount: (productId: number) => request<number>('/api/admin/license-keys/available-count', {query: {productId}}),
        remove: (id: number) => request<void>(`/api/admin/license-keys/${id}`, {method: 'DELETE'}),
    },
    stockMovements: {
        search: (productId: number, page: number) =>
            request<Page<D.StockMovement>>('/api/admin/stock-movements', {query: {productId, page}}),
        adjust: (productId: number, productOptionId: number | null, delta: number, note: string) =>
            request<D.StockMovement>('/api/admin/stock-movements/adjust', {method: 'POST', body: {productId, productOptionId, delta, note}}),
    },
    receivables: {
        search: (memberId: number | null, page: number) =>
            request<Page<D.Receivable>>('/api/admin/receivables', {query: {memberId: memberId ?? undefined, page}}),
        create: (memberId: number, orderId: number | null, totalAmount: number, note: string) =>
            request<D.Receivable>('/api/admin/receivables', {method: 'POST', body: {memberId, orderId, totalAmount, note}}),
        collect: (id: number, amount: number) =>
            request<D.Receivable>(`/api/admin/receivables/${id}/collect`, {method: 'POST', body: {amount}}),
    },
    salesReport: (from: string, to: string) => request<D.SalesReportRow[]>('/api/admin/sales-report', {query: {from, to}}),
    settlements: (from: string, to: string) => request<D.SettlementRow[]>('/api/admin/settlements', {query: {from, to}}),
    marketing: {
        send: (title: string, content: string, targetGradeId: number | null, channels: D.NotificationChannel[]) =>
            request<D.MarketingCampaign>('/api/admin/marketing/campaigns', {method: 'POST', body: {title, content, targetGradeId, channels}}),
        history: (page: number) => request<Page<D.MarketingCampaign>>('/api/admin/marketing/campaigns', {query: {page}}),
    },
    notifications: {
        list: (page: number) => request<Page<D.AdminNotification>>('/api/admin/notifications', {query: {page}}),
    },
    votes: {
        search: (query: Query) => request<Page<D.Vote>>('/api/admin/votes', {query}),
        create: (body: Body) => request<D.Vote>('/api/admin/votes', {method: 'POST', body}),
        close: (id: number) => request<D.Vote>(`/api/admin/votes/${id}/close`, {method: 'POST'}),
        remove: (id: number) => request<void>(`/api/admin/votes/${id}`, {method: 'DELETE'}),
    },
    shippingPolicy: {
        save: (body: Body) => request<D.ShippingPolicy>('/api/admin/shipping-policy', {method: 'PUT', body}),
    },
    taxSetting: {
        save: (body: Body) => request<D.TaxSetting>('/api/admin/tax-setting', {method: 'PUT', body}),
    },
    exchangeRates: {
        search: () => request<D.ExchangeRate[]>('/api/admin/exchange-rates'),
        save: (currencyCode: string, rate: number, enabled: boolean) =>
            request<D.ExchangeRate>('/api/admin/exchange-rates', {method: 'PUT', body: {currencyCode, rate, enabled}}),
        remove: (id: number) => request<void>(`/api/admin/exchange-rates/${id}`, {method: 'DELETE'}),
    },
    applications: {
        ...resource<D.ApplicationForm>('/api/admin/applications'),
        submissions: (id: number, query: Query) =>
            request<Page<D.Submission>>(`/api/admin/applications/${id}/submissions`, {query}),
        review: (submissionId: number, body: Body) =>
            request<D.Submission>(`/api/admin/applications/submissions/${submissionId}`, {method: 'PUT', body}),
        draw: (id: number) => request<D.DrawResult>(`/api/admin/applications/${id}/draw`, {method: 'POST'}),
        summary: (id: number, query: Query) => request<D.SubmissionSummary>(`/api/admin/applications/${id}/summary`, {query}),
        finalizeContest: (id: number) => request<D.DrawResult>(`/api/admin/applications/${id}/finalize-contest`, {method: 'POST'}),
        scores: (submissionId: number) =>
            request<D.ScoreDetail[]>(`/api/admin/applications/submissions/${submissionId}/scores`),
    },
    subscriptions: {
        search: (query: Query) => request<Page<D.Subscription>>('/api/admin/subscriptions', {query}),
        cancel: (id: number) => request<D.Subscription>(`/api/admin/subscriptions/${id}/cancel`, {method: 'POST'}),
        pause: (id: number, resumeOn: string | null) =>
            request<D.Subscription>(`/api/admin/subscriptions/${id}/pause`, {method: 'POST', body: {resumeOn}}),
        resume: (id: number) => request<D.Subscription>(`/api/admin/subscriptions/${id}/resume`, {method: 'POST'}),
        renewDue: () => request<{ renewed: number }>('/api/admin/subscriptions/renew', {method: 'POST'}),
    },
    reservations: {
        search: (query: Query) => request<Page<D.Reservation>>('/api/admin/reservations', {query}),
        create: (body: Body, paid: boolean) =>
            request<D.Reservation>('/api/admin/reservations', {method: 'POST', body, query: {paid}}),
        confirm: (id: number) => request<void>(`/api/admin/reservations/${id}/confirm`, {method: 'POST'}),
        cancel: (id: number) => request<void>(`/api/admin/reservations/${id}/cancel`, {method: 'POST'}),
        assignWorker: (id: number, body: { workerName: string; workerPhone: string }) =>
            request<D.Reservation>(`/api/admin/reservations/${id}/worker`, {method: 'PUT', body}),
        completeWork: (id: number, body: { report: string; photoFileIds: number[] }) =>
            request<D.Reservation>(`/api/admin/reservations/${id}/complete`, {method: 'POST', body}),
        returnItems: (id: number, body: { deduction: number; memo: string }) =>
            request<D.Reservation>(`/api/admin/reservations/${id}/return`, {method: 'POST', body}),
        extend: (id: number, endAt: string, paid: boolean) =>
            request<D.Reservation>(`/api/admin/reservations/${id}/extend`, {method: 'POST', body: {endAt, paid}}),
        cancelExtension: (id: number) => request<D.Reservation>(`/api/admin/reservations/${id}/extension/cancel`, {method: 'POST'}),
        extensions: (id: number) => request<D.ReservationExtension[]>(`/api/admin/reservations/${id}/extensions`),
        checkIn: (code: string) => request<D.Reservation>('/api/admin/reservations/check-in', {method: 'POST', body: {code}}),
        queue: (programId: number, date?: string) =>
            request<D.Reservation[]>('/api/admin/reservations/queue', {query: {programId, date}}),
        changeQueueStatus: (id: number, status: D.QueueStatus) =>
            request<D.Reservation>(`/api/admin/reservations/${id}/queue-status`, {method: 'POST', body: {status}}),
    },
    sessions: {
        create: (programId: number, body: Body) =>
            request<D.ProgramSession>(`/api/admin/reservation-items/${programId}/sessions`, {method: 'POST', body}),
        update: (programId: number, id: number, body: Body) =>
            request<D.ProgramSession>(`/api/admin/reservation-items/${programId}/sessions/${id}`, {method: 'PUT', body}),
        remove: (programId: number, id: number) =>
            request<void>(`/api/admin/reservation-items/${programId}/sessions/${id}`, {method: 'DELETE'}),
    },
    customerRecords: {
        search: (query: Query) => request<Page<D.CustomerRecord>>('/api/admin/customer-records', {query}),
        create: (body: Body) => request<D.CustomerRecord>('/api/admin/customer-records', {method: 'POST', body}),
        update: (id: number, body: Body) => request<D.CustomerRecord>(`/api/admin/customer-records/${id}`, {method: 'PUT', body}),
        remove: (id: number) => request<void>(`/api/admin/customer-records/${id}`, {method: 'DELETE'}),
    },
    classAttendance: {
        roster: (productId: number, date: string) =>
            request<D.AttendanceRosterEntry[]>('/api/admin/class-attendance/roster', {query: {productId, date}}),
        mark: (subscriptionId: number, attendedOn: string, present: boolean, note: string) =>
            request<D.AttendanceRosterEntry>('/api/admin/class-attendance/mark', {method: 'POST', body: {subscriptionId, attendedOn, present, note}}),
    },
    passes: {
        search: (query: Query) => request<Page<D.MemberPass>>('/api/admin/passes', {query}),
        cancel: (id: number) => request<void>(`/api/admin/passes/${id}`, {method: 'DELETE'}),
    },
    progress: {
        templates: (targetType: D.ProgressTargetType) => request<D.ProgressStageTemplate[]>(`/api/admin/progress/${targetType}/templates`),
        createTemplate: (targetType: D.ProgressTargetType, body: Body) =>
            request<D.ProgressStageTemplate>(`/api/admin/progress/${targetType}/templates`, {method: 'POST', body}),
        updateTemplate: (id: number, body: Body) => request<D.ProgressStageTemplate>(`/api/admin/progress/templates/${id}`, {method: 'PUT', body}),
        removeTemplate: (id: number) => request<void>(`/api/admin/progress/templates/${id}`, {method: 'DELETE'}),
        get: (targetType: D.ProgressTargetType, targetId: number) => request<D.Progress>(`/api/admin/progress/${targetType}/${targetId}`),
        advance: (targetType: D.ProgressTargetType, targetId: number, body: Body) =>
            request<D.ProgressStageEvent>(`/api/admin/progress/${targetType}/${targetId}/advance`, {method: 'POST', body}),
    },
    messages: {
        list: (targetType: D.ProgressTargetType, targetId: number) =>
            request<D.CustomerMessage[]>('/api/admin/messages', {query: {targetType, targetId}}),
        post: (targetType: D.ProgressTargetType, targetId: number, body: Body) =>
            request<D.CustomerMessage>('/api/admin/messages', {method: 'POST', query: {targetType, targetId}, body}),
    },
    repairs: {
        search: (query: Query) => request<Page<D.RepairRequest>>('/api/admin/repairs', {query}),
        get: (id: number) => request<D.RepairRequest>(`/api/admin/repairs/${id}`),
        createQuoteOrder: (id: number, productId: number) =>
            request<D.Order>(`/api/admin/repairs/${id}/quote-order`, {method: 'POST', body: {productId}}),
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
        setGuestPosting: (body: D.GuestPostingPolicy) =>
            request<D.GuestPostingPolicy>('/api/admin/reports/guest-posting', {method: 'PUT', body}),
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
        series: (unit: D.PeriodUnit, from: string, to: string) =>
            request<D.DailyVisits[]>('/api/admin/statistics/series', {query: {unit, from, to}}),
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
    editComment: (boardId: number, id: number, commentId: number, content: string, password?: string) =>
        request<void>(`/api/boards/${boardId}/articles/${id}/comments/${commentId}`, {method: 'PUT', body: {content, password}}),
    removeComment: (boardId: number, id: number, commentId: number, password?: string) =>
        request<void>(`/api/boards/${boardId}/articles/${id}/comments/${commentId}`, {method: 'DELETE', body: {password}}),
};

export const qnaApi = {
    guestPosting: () => request<D.GuestPostingPolicy>('/api/questions/guest-posting'),
    list: (keyword: string, page: number, unanswered = false) =>
        request<Page<D.QuestionSummary>>('/api/questions', {query: {keyword, page, size: 10, unanswered}}),
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
    like: (id: number, articleId: number) =>
        request<{ likes: number }>(`/api/archives/${id}/articles/${articleId}/likes`, {method: 'POST'}),
    article: (id: number, articleId: number) =>
        request<D.ArchiveArticleDetail>(`/api/archives/${id}/articles/${articleId}`),
    comments: (id: number, articleId: number) =>
        request<D.ArchiveComment[]>(`/api/archives/${id}/articles/${articleId}/comments`),
    addComment: (id: number, articleId: number, content: string, parentId: number | null = null) =>
        request<void>(`/api/archives/${id}/articles/${articleId}/comments`, {method: 'POST', body: {content, parentId}}),
    editComment: (id: number, articleId: number, commentId: number, content: string) =>
        request<void>(`/api/archives/${id}/articles/${articleId}/comments/${commentId}`, {method: 'PUT', body: {content}}),
    removeComment: (id: number, articleId: number, commentId: number) =>
        request<void>(`/api/archives/${id}/articles/${articleId}/comments/${commentId}`, {method: 'DELETE'}),
};

export const digitalApi = {
    mine: (page: number) => request<Page<D.DigitalAccess>>('/api/digital/me', {query: {page}}),
};

export const shopApi = {
    products: (keyword: string, page: number) =>
        request<Page<D.ProductSummary>>('/api/products', {query: {keyword, page}}),
    product: (id: number) => request<D.ProductDetail>(`/api/products/${id}`),
    searchProducts: (params: URLSearchParams) =>
        request<Page<D.ProductSummary>>(params.size > 0 ? `/api/products?${params}` : '/api/products'),
    reviews: (id: number, page: number) => request<Page<D.Review>>(`/api/products/${id}/reviews`, {query: {page}}),
    addReview: (id: number, body: Body) => request<void>(`/api/products/${id}/reviews`, {method: 'POST', body}),
    removeReview: (id: number) => request<void>(`/api/reviews/${id}`, {method: 'DELETE'}),
    order: (body: Body) => request<D.Order>('/api/orders', {method: 'POST', body}),
    myOrders: (page: number) => request<Page<D.Order>>('/api/orders/me', {query: {page}}),
    myOrder: (id: number) => request<D.Order>(`/api/orders/${id}`),
    cancelOrder: (id: number) => request<void>(`/api/orders/${id}/cancel`, {method: 'POST'}),
    acceptQuote: (id: number) => request<D.Order>(`/api/orders/${id}/accept-quote`, {method: 'POST'}),
    cancelOrderItem: (id: number, itemId: number) =>
        request<D.Order>(`/api/orders/${id}/items/${itemId}/cancel`, {method: 'POST'}),
    requestReturn: (id: number, type: D.OrderReturnType, reason: string, exchangeOptionId: number | null) =>
        request<D.OrderReturn>(`/api/orders/${id}/return-requests`, {method: 'POST', body: {type, reason, exchangeOptionId}}),
    myReturnRequests: (page: number) => request<Page<D.OrderReturn>>('/api/orders/return-requests/me', {query: {page}}),
    myLicenseKeys: (page: number) => request<Page<D.MyLicenseKey>>('/api/license-keys/me', {query: {page}}),
    votes: (page: number) => request<Page<D.MyVote>>('/api/votes', {query: {page}}),
    castBallot: (id: number, optionIndex: number) =>
        request<D.MyVote>(`/api/votes/${id}/ballots`, {method: 'POST', body: {optionIndex}}),
    shippingPolicy: () => request<D.ShippingPolicy>('/api/shipping-policy'),
    taxSetting: () => request<D.TaxSetting>('/api/tax-setting'),
    exchangeRates: () => request<D.ExchangeRate[]>('/api/exchange-rates'),
    cart: (postalCode?: string, country?: string) =>
        request<D.Cart>('/api/cart', {query: {postalCode: postalCode || undefined, country: country || undefined}}),
    addToCart: (body: { productId: number; productOptionId: number | null; quantity: number }) =>
        request<D.Cart>('/api/cart', {method: 'POST', body}),
    changeCartQuantity: (id: number, quantity: number) => request<D.Cart>(`/api/cart/${id}`, {method: 'PUT', body: {quantity}}),
    removeFromCart: (id: number) => request<D.Cart>(`/api/cart/${id}`, {method: 'DELETE'}),
    checkoutCart: (body: Body) => request<D.Order>('/api/cart/checkout', {method: 'POST', body}),
    mySubscriptions: (page: number) => request<Page<D.Subscription>>('/api/subscriptions/me', {query: {page}}),
    cancelSubscription: (id: number) => request<D.Subscription>(`/api/subscriptions/${id}/cancel`, {method: 'POST'}),
    extendReservation: (id: number, endAt: string) =>
        request<D.Reservation>(`/api/reservations/${id}/extend`, {method: 'POST', body: {endAt}}),
    cancelExtension: (id: number) => request<D.Reservation>(`/api/reservations/${id}/extension/cancel`, {method: 'POST'}),
    myRecords: (page: number) => request<Page<D.MyCustomerRecord>>('/api/customer-records/me', {query: {page}}),
    myNotifications: (page: number) => request<Page<D.AppNotification>>('/api/members/me/notifications', {query: {page}}),
    unreadNotificationCount: () => request<number>('/api/members/me/notifications/unread-count'),
    markNotificationRead: (id: number) => request<void>(`/api/members/me/notifications/${id}/read`, {method: 'POST'}),
    myAttendance: (subscriptionId: number, page: number) =>
        request<Page<D.MyAttendance>>('/api/class-attendance/me', {query: {subscriptionId, page}}),
    myPasses: (page: number) => request<Page<D.MemberPass>>('/api/passes/me', {query: {page}}),
    usablePasses: (programId: number) => request<D.MemberPass[]>('/api/passes/usable', {query: {programId}}),
    wallet: {
        balance: () => request<number>('/api/wallet/balance'),
        ledger: (page: number) => request<Page<D.WalletLedgerEntry>>('/api/wallet/ledger', {query: {page}}),
        myGiftCodes: (page: number) => request<Page<D.GiftCard>>('/api/wallet/gift-codes', {query: {page}}),
        issueGiftCode: (amount: number) => request<D.GiftCard>('/api/wallet/gift-codes', {method: 'POST', body: {amount}}),
        redeem: (code: string) => request<D.GiftCard>('/api/wallet/redeem', {method: 'POST', body: {code}}),
    },
    progress: (targetType: D.ProgressTargetType, targetId: number) =>
        request<D.Progress>('/api/progress', {query: {targetType, targetId}}),
    messages: {
        list: (targetType: D.ProgressTargetType, targetId: number) =>
            request<D.CustomerMessage[]>('/api/messages', {query: {targetType, targetId}}),
        post: (targetType: D.ProgressTargetType, targetId: number, body: Body) =>
            request<D.CustomerMessage>('/api/messages', {method: 'POST', query: {targetType, targetId}, body}),
    },
    repairs: {
        create: (body: Body) => request<D.RepairRequest>('/api/repairs', {method: 'POST', body}),
        mine: (page: number) => request<Page<D.RepairRequest>>('/api/repairs/me', {query: {page}}),
    },
    pauseSubscription: (id: number, resumeOn: string | null) =>
        request<D.Subscription>(`/api/subscriptions/${id}/pause`, {method: 'POST', body: {resumeOn}}),
    resumeSubscription: (id: number) => request<D.Subscription>(`/api/subscriptions/${id}/resume`, {method: 'POST'}),
    subscriptionPlans: (id: number) => request<D.ProductSummary[]>(`/api/subscriptions/${id}/plans`),
    changeSubscriptionPlan: (id: number, productId: number) =>
        request<D.Subscription>(`/api/subscriptions/${id}/plan`, {method: 'PUT', body: {productId}}),
    cancelSubscriptionPlanChange: (id: number) => request<D.Subscription>(`/api/subscriptions/${id}/plan`, {method: 'DELETE'}),
    wishlist: (page: number) => request<Page<D.WishlistEntry>>('/api/wishlist', {query: {page}}),
    wishlistIds: () => request<number[]>('/api/wishlist/product-ids'),
    wish: (productId: number) => request<void>(`/api/wishlist/${productId}`, {method: 'PUT'}),
    unwish: (productId: number) => request<void>(`/api/wishlist/${productId}`, {method: 'DELETE'}),
    programs: (keyword: string, page: number) =>
        request<Page<D.Program>>('/api/reservation-items', {query: {keyword, page}}),
    program: (id: number) => request<D.Program>(`/api/reservation-items/${id}`),
    availability: (id: number, query: { date?: string; from?: string; to?: string; sessionId?: number }) =>
        request<D.Availability>(`/api/reservation-items/${id}/availability`, {query}),
    programReviews: (id: number, page: number) => request<Page<D.Review>>(`/api/reservation-items/${id}/reviews`, {query: {page}}),
    addProgramReview: (id: number, body: Body) => request<void>(`/api/reservation-items/${id}/reviews`, {method: 'POST', body}),
    reserve: (body: Body) => request<D.Reservation>('/api/reservations', {method: 'POST', body}),
    myReservations: (page: number) => request<Page<D.Reservation>>('/api/reservations/me', {query: {page}}),
    cancelReservation: (id: number) => request<void>(`/api/reservations/${id}/cancel`, {method: 'POST'}),
    pay: (targetType: D.PaymentTargetType, targetId: number, amount: number, method: D.PaymentMethod) =>
        request<D.Payment>('/api/payments', {method: 'POST', body: {targetType, targetId, amount, method}}),
};

export const govApi = {
    createReport: (body: Body) => request<{ id: number }>('/api/reports', {method: 'POST', body}),
    reportGuestPosting: () => request<D.GuestPostingPolicy>('/api/reports/guest-posting'),
    myReports: (page: number) => request<Page<D.ReportSummary>>('/api/reports/me', {query: {page}}),
    report: (id: number) => request<D.ReportDetail>(`/api/reports/${id}`),
    accessReport: (id: number, password: string) =>
        request<D.ReportDetail>(`/api/reports/${id}/access`, {method: 'POST', body: {password}}),
    satisfaction: (menuId: number) => request<D.Satisfaction>('/api/satisfactions', {query: {menuId}}),
    vote: (menuId: number, score: number, opinion: string) =>
        request<D.Satisfaction>('/api/satisfactions', {method: 'POST', body: {menuId, score, opinion}}),
};

export const mediaApi = {
    list: (keyword: string, mediaType: MediaType | '', page: number) =>
        request<Page<D.MediaItem>>('/api/media', {query: {keyword, mediaType, page}}),
    get: (id: number) => request<D.MediaItem>(`/api/media/${id}`),
    start: (id: number) => request<D.MediaProgress>(`/api/media/${id}/start`, {method: 'POST'}),
    record: (id: number, ranges: { start: number; end: number }[], position: number) =>
        request<D.MediaProgress>(`/api/media/${id}/progress`, {method: 'POST', body: {ranges, position}}),
    mine: (page: number) => request<Page<D.MyMedia>>('/api/media/me', {query: {page}}),
};

export const applicationApi = {
    list: (keyword: string, page: number) => request<Page<D.ApplicationForm>>('/api/applications', {query: {keyword, page}}),
    get: (id: number) => request<D.ApplicationForm>(`/api/applications/${id}`),
    submit: (id: number, answers: Record<string, unknown>) =>
        request<D.Submission>(`/api/applications/${id}/submissions`, {method: 'POST', body: {answers}}),
    mine: (page: number) => request<Page<D.Submission>>('/api/applications/me', {query: {page}}),
    withdraw: (id: number) => request<D.Submission>(`/api/applications/submissions/${id}/withdraw`, {method: 'POST'}),
    payAdmission: (id: number) => request<D.Order>(`/api/applications/submissions/${id}/admission-payment`, {method: 'POST'}),
    judging: (id: number, page: number) => request<Page<D.JudgeSubmission>>(`/api/applications/${id}/judging`, {query: {page}}),
    score: (submissionId: number, score: number, comment: string) =>
        request<{ submissionId: number; score: number; comment: string | null }>(`/api/applications/submissions/${submissionId}/score`,
            {method: 'POST', body: {score, comment}}),
};

export const chatbotApi = {
    status: () => request<{ enabled: boolean; welcomeMessage: string | null }>('/api/chatbot'),
    send: (messages: D.ChatMessage[]) =>
        request<{ reply: string }>('/api/chatbot/messages', {method: 'POST', body: {messages}}).then((r) => r.reply),
};
