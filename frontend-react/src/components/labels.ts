import type {OrderStatus, PublishStatus, ReportStatus, ReservationStatus} from '../api/domain';

export const ORDER_STATUS: Record<OrderStatus, string> = {
    PENDING_PAYMENT: '결제 대기',
    PAID: '결제 완료',
    SHIPPING: '배송 중',
    DELIVERED: '배송 완료',
    CANCELED: '취소',
};

export const RESERVATION_STATUS: Record<ReservationStatus, string> = {
    PENDING_PAYMENT: '결제 대기',
    CONFIRMED: '확정',
    CANCELED: '취소',
};

export const REPORT_STATUS: Record<ReportStatus, string> = {
    RECEIVED: '접수',
    IN_PROGRESS: '처리 중',
    ANSWERED: '답변 완료',
    CLOSED: '종결',
};

export const PUBLISH_STATUS: Record<PublishStatus, string> = {
    DRAFT: '임시 저장',
    PUBLISHED: '게시',
};

export const toOptions = (labels: Record<string, string>) =>
    Object.entries(labels).map(([value, label]) => ({value, label}));

