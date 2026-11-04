import type {EnrollmentStatus, OrderStatus, PublishStatus, ReportStatus, ReservationStatus} from '../api/domain';

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

export const ENROLLMENT_STATUS: Record<EnrollmentStatus, string> = {
    IN_PROGRESS: '수강 중',
    COMPLETED: '수강 완료',
};
export const SUBMISSION_STATUS: Record<SubmissionStatus, string> = {
    RECEIVED: '접수',
    REVIEWING: '검토 중',
    ACCEPTED: '선정',
    REJECTED: '미선정',
    WITHDRAWN: '취소',
};

export const FORM_FIELD_TYPE: Record<FormFieldType, string> = {
    TEXT: '짧은 글',
    TEXTAREA: '긴 글',
    NUMBER: '숫자',
    DATE: '날짜',
    EMAIL: '이메일',
    PHONE: '전화번호',
    SELECT: '선택(목록)',
    RADIO: '선택(하나)',
    CHECKBOX: '체크(여러 개/동의)',
    FILE: '파일 첨부',
};
