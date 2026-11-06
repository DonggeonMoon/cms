import type {BillingCycle, CellKind, ContentStatus, FormFieldType, LedgerType, PaymentMethod, QueueStatus, ReservationType, SelectionMethod, SubmissionStatus, EnrollmentStatus, OrderReturnStatus, OrderReturnType, OrderStatus, PassUnit, ProductSaleType, PublishStatus, ReportStatus, ReservationStatus, StockMovementType} from '../api/domain';

export const ORDER_STATUS: Record<OrderStatus, string> = {
    QUOTE_REQUESTED: '견적 요청',
    QUOTED: '견적 회신',
    PENDING_PAYMENT: '결제 대기',
    PAID: '결제 완료',
    SHIPPING: '배송 중',
    DELIVERED: '배송 완료',
    PREPARING: '준비 중',
    READY: '준비 완료',
    COMPLETED: '전달 완료',
    CANCELED: '취소',
};

export const RESERVATION_STATUS: Record<ReservationStatus, string> = {
    PENDING_PAYMENT: '결제 대기',
    CONFIRMED: '확정',
    CANCELED: '취소',
};

export const QUEUE_STATUS: Record<QueueStatus, string> = {
    WAITING: '대기',
    CALLED: '호출',
    DONE: '완료',
    SKIPPED: '건너뜀',
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

export const CONTENT_STATUS: Record<ContentStatus, string> = {
    DRAFT: '임시 저장',
    IN_REVIEW: '검토 중',
    REJECTED: '반려',
    SCHEDULED: '게시 예약',
    PUBLISHED: '게시',
};

export const toOptions = (labels: Record<string, string>) =>
    Object.entries(labels).map(([value, label]) => ({value, label}));

export const ENROLLMENT_STATUS: Record<EnrollmentStatus, string> = {
    IN_PROGRESS: '수강 중',
    COMPLETED: '수강 완료',
};

export const SALE_TYPE: Record<ProductSaleType, string> = {
    PURCHASE: '구매',
    SUBSCRIPTION: '구독',
    DONATION: '후원',
    QUOTE: '견적 문의',
    PASS: '이용권',
    GIFT_CARD: '상품권',
    PREORDER: '예약 판매',
    FUNDING: '펀딩',
    INQUIRY: '문의만',
};

export const LEDGER_TYPE: Record<LedgerType, string> = {
    CHARGE: '충전',
    GIFT_ISSUED: '상품권 발급',
    GIFT_REDEEMED: '상품권 충전',
    PAYMENT: '결제',
    REFUND: '환불',
    ADMIN: '관리자 조정',
};

export const PAYMENT_METHOD: Record<PaymentMethod, string> = {
    CARD: '카드',
    BANK_TRANSFER: '계좌이체',
    VIRTUAL_ACCOUNT: '가상계좌',
    MOBILE: '휴대폰',
    PREPAID: '지갑(적립금)',
};

export const STOCK_MOVEMENT_TYPE: Record<StockMovementType, string> = {
    OUT: '출고',
    IN: '입고·복원',
    ADJUST: '수동 조정',
};

export const RETURN_TYPE: Record<OrderReturnType, string> = {
    RETURN: '반품',
    EXCHANGE: '교환',
};

export const RETURN_STATUS: Record<OrderReturnStatus, string> = {
    REQUESTED: '신청',
    APPROVED: '승인',
    REJECTED: '거절',
};

export const PASS_UNIT: Record<PassUnit, string> = {
    PERIOD: '기간권',
    COUNT: '횟수권',
};

export const BILLING_CYCLE: Record<BillingCycle, string> = {
    MONTHLY: '매월',
    YEARLY: '매년',
};

export const RESERVATION_TYPE: Record<ReservationType, string> = {
    PROGRAM: '프로그램',
    CONSULTATION: '상담',
    RENTAL: '대관',
    LODGING: '숙박·대실',
    EVENT: '공연·행사',
    ITEM_RENTAL: '물품 대여',
    VISIT: '출장·방문',
    TABLE: '식당 테이블',
    TOUR: '여행·투어',
};

export const RESERVATION_TYPE_GROUPS: { label: string; types: ReservationType[] }[] = [
    {label: '시간 예약', types: ['PROGRAM', 'CONSULTATION', 'VISIT', 'TOUR']},
    {label: '공간 예약', types: ['RENTAL', 'LODGING', 'EVENT', 'TABLE']},
    {label: '물품', types: ['ITEM_RENTAL']},
];

export const CELL_KIND: Record<CellKind, string> = {
    SEAT: '좌석',
    ROOM: '객실',
    SPACE: '공간',
    AISLE: '통로',
    STAGE: '무대',
    EMPTY: '빈칸',
};

export const SELECTION_METHOD: Record<SelectionMethod, string> = {
    REVIEW: '심사',
    LOTTERY: '추첨',
    CONTEST: '공모전',
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
