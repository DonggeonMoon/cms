import type {BillingCycle, CellKind, ContentStatus, FormFieldType, LedgerType, PaymentMethod, QueueStatus, ReservationType, SubmissionStatus, EnrollmentStatus, OrderReturnStatus, OrderReturnType, OrderStatus, PassUnit, ProductSaleType, PublishStatus, ReportStatus, ReservationStatus, StockMovementType} from '../api/domain';
import type {AccessPeriodMode, CourseApplicationStatus, EnrollmentMode, RevealPolicy, ScorePolicy} from '../api/courses';

export type T = (key: string, fallback: string, values?: Record<string, string>) => string;

export const orderStatus = (t: T): Record<OrderStatus, string> => ({
    QUOTE_REQUESTED: t('label.orderStatus.QUOTE_REQUESTED', '견적 요청'),
    QUOTED: t('label.orderStatus.QUOTED', '견적 회신'),
    PENDING_PAYMENT: t('label.orderStatus.PENDING_PAYMENT', '결제 대기'),
    PAID: t('label.orderStatus.PAID', '결제 완료'),
    SHIPPING: t('label.orderStatus.SHIPPING', '배송 중'),
    DELIVERED: t('label.orderStatus.DELIVERED', '배송 완료'),
    CANCELED: t('label.orderStatus.CANCELED', '취소'),
});

export const reservationStatus = (t: T): Record<ReservationStatus, string> => ({
    PENDING_PAYMENT: t('label.reservationStatus.PENDING_PAYMENT', '결제 대기'),
    CONFIRMED: t('label.reservationStatus.CONFIRMED', '확정'),
    CANCELED: t('label.reservationStatus.CANCELED', '취소'),
});

export const queueStatus = (t: T): Record<QueueStatus, string> => ({
    WAITING: t('label.queueStatus.WAITING', '대기'),
    CALLED: t('label.queueStatus.CALLED', '호출'),
    DONE: t('label.queueStatus.DONE', '완료'),
    SKIPPED: t('label.queueStatus.SKIPPED', '건너뜀'),
});

export const reportStatus = (t: T): Record<ReportStatus, string> => ({
    RECEIVED: t('label.reportStatus.RECEIVED', '접수'),
    IN_PROGRESS: t('label.reportStatus.IN_PROGRESS', '처리 중'),
    ANSWERED: t('label.reportStatus.ANSWERED', '답변 완료'),
    CLOSED: t('label.reportStatus.CLOSED', '종결'),
});

export const publishStatus = (t: T): Record<PublishStatus, string> => ({
    DRAFT: t('label.publishStatus.DRAFT', '임시 저장'),
    PUBLISHED: t('label.publishStatus.PUBLISHED', '게시'),
});

export const contentStatus = (t: T): Record<ContentStatus, string> => ({
    DRAFT: t('label.contentStatus.DRAFT', '임시 저장'),
    IN_REVIEW: t('label.contentStatus.IN_REVIEW', '검토 중'),
    REJECTED: t('label.contentStatus.REJECTED', '반려'),
    SCHEDULED: t('label.contentStatus.SCHEDULED', '게시 예약'),
    PUBLISHED: t('label.contentStatus.PUBLISHED', '게시'),
});

export const toOptions = (labels: Record<string, string>) =>
    Object.entries(labels).map(([value, label]) => ({value, label}));

export const enrollmentStatus = (t: T): Record<EnrollmentStatus, string> => ({
    IN_PROGRESS: t('label.enrollmentStatus.IN_PROGRESS', '수강 중'),
    COMPLETED: t('label.enrollmentStatus.COMPLETED', '수강 완료'),
});

export const courseEnrollmentMode = (t: T): Record<EnrollmentMode, string> => ({
    FREE: t('label.courseEnrollmentMode.FREE', '자유 수강'),
    APPLY: t('label.courseEnrollmentMode.APPLY', '신청 후 수강'),
    APPROVAL: t('label.courseEnrollmentMode.APPROVAL', '승인제'),
});

export const courseApplicationStatus = (t: T): Record<CourseApplicationStatus, string> => ({
    APPLIED: t('label.courseApplicationStatus.APPLIED', '신청'),
    APPROVED: t('label.courseApplicationStatus.APPROVED', '승인'),
    REJECTED: t('label.courseApplicationStatus.REJECTED', '반려'),
    CANCELED: t('label.courseApplicationStatus.CANCELED', '취소'),
});

export const accessPeriodMode = (t: T): Record<AccessPeriodMode, string> => ({
    UNLIMITED: t('label.accessPeriodMode.UNLIMITED', '무제한'),
    FIXED: t('label.accessPeriodMode.FIXED', '고정 기간'),
    DAYS_FROM_ENROLLMENT: t('label.accessPeriodMode.DAYS_FROM_ENROLLMENT', '신청일 기준 N일'),
});

export const revealPolicy = (t: T): Record<RevealPolicy, string> => ({
    IMMEDIATELY: t('label.revealPolicy.IMMEDIATELY', '제출 직후 공개'),
    AFTER_PASS: t('label.revealPolicy.AFTER_PASS', '통과 후 공개'),
    NEVER: t('label.revealPolicy.NEVER', '비공개'),
});

export const scorePolicy = (t: T): Record<ScorePolicy, string> => ({
    HIGHEST: t('label.scorePolicy.HIGHEST', '최고 점수 인정'),
    LATEST: t('label.scorePolicy.LATEST', '최종 점수 인정'),
});

export const saleType = (t: T): Record<ProductSaleType, string> => ({
    PURCHASE: t('label.saleType.PURCHASE', '구매'),
    SUBSCRIPTION: t('label.saleType.SUBSCRIPTION', '구독'),
    DONATION: t('label.saleType.DONATION', '후원'),
    QUOTE: t('label.saleType.QUOTE', '견적 문의'),
    PASS: t('label.saleType.PASS', '이용권'),
    GIFT_CARD: t('label.saleType.GIFT_CARD', '상품권'),
    PREORDER: t('label.saleType.PREORDER', '예약 판매'),
    FUNDING: t('label.saleType.FUNDING', '펀딩'),
    INQUIRY: t('label.saleType.INQUIRY', '문의만'),
});

export const ledgerType = (t: T): Record<LedgerType, string> => ({
    CHARGE: t('label.ledgerType.CHARGE', '충전'),
    GIFT_ISSUED: t('label.ledgerType.GIFT_ISSUED', '상품권 발급'),
    GIFT_REDEEMED: t('label.ledgerType.GIFT_REDEEMED', '상품권 충전'),
    PAYMENT: t('label.ledgerType.PAYMENT', '결제'),
    REFUND: t('label.ledgerType.REFUND', '환불'),
    ADMIN: t('label.ledgerType.ADMIN', '관리자 조정'),
});

export const paymentMethod = (t: T): Record<PaymentMethod, string> => ({
    CARD: t('label.paymentMethod.CARD', '카드'),
    BANK_TRANSFER: t('label.paymentMethod.BANK_TRANSFER', '계좌이체'),
    VIRTUAL_ACCOUNT: t('label.paymentMethod.VIRTUAL_ACCOUNT', '가상계좌'),
    MOBILE: t('label.paymentMethod.MOBILE', '휴대폰'),
    PREPAID: t('label.paymentMethod.PREPAID', '지갑(적립금)'),
});

export const stockMovementType = (t: T): Record<StockMovementType, string> => ({
    OUT: t('label.stockMovementType.OUT', '출고'),
    IN: t('label.stockMovementType.IN', '입고·복원'),
    ADJUST: t('label.stockMovementType.ADJUST', '수동 조정'),
});

export const returnType = (t: T): Record<OrderReturnType, string> => ({
    RETURN: t('label.returnType.RETURN', '반품'),
    EXCHANGE: t('label.returnType.EXCHANGE', '교환'),
});

export const returnStatus = (t: T): Record<OrderReturnStatus, string> => ({
    REQUESTED: t('label.returnStatus.REQUESTED', '신청'),
    APPROVED: t('label.returnStatus.APPROVED', '승인'),
    REJECTED: t('label.returnStatus.REJECTED', '거절'),
});

export const passUnit = (t: T): Record<PassUnit, string> => ({
    PERIOD: t('label.passUnit.PERIOD', '기간권'),
    COUNT: t('label.passUnit.COUNT', '횟수권'),
});

export const billingCycle = (t: T): Record<BillingCycle, string> => ({
    MONTHLY: t('label.billingCycle.MONTHLY', '매월'),
    YEARLY: t('label.billingCycle.YEARLY', '매년'),
});

export const reservationType = (t: T): Record<ReservationType, string> => ({
    PROGRAM: t('label.reservationType.PROGRAM', '프로그램'),
    CONSULTATION: t('label.reservationType.CONSULTATION', '상담'),
    RENTAL: t('label.reservationType.RENTAL', '대관'),
    LODGING: t('label.reservationType.LODGING', '숙박·대실'),
    EVENT: t('label.reservationType.EVENT', '공연·행사'),
    ITEM_RENTAL: t('label.reservationType.ITEM_RENTAL', '물품 대여'),
    VISIT: t('label.reservationType.VISIT', '출장·방문'),
    TABLE: t('label.reservationType.TABLE', '식당 테이블'),
    TOUR: t('label.reservationType.TOUR', '여행·투어'),
});

export const reservationTypeGroups = (t: T): { label: string; types: ReservationType[] }[] => [
    {label: t('label.reservationTypeGroup.TIME', '시간 예약'), types: ['PROGRAM', 'CONSULTATION', 'VISIT', 'TOUR']},
    {label: t('label.reservationTypeGroup.SPACE', '공간 예약'), types: ['RENTAL', 'LODGING', 'EVENT', 'TABLE']},
    {label: t('label.reservationTypeGroup.ITEM', '물품'), types: ['ITEM_RENTAL']},
];

export const cellKind = (t: T): Record<CellKind, string> => ({
    SEAT: t('label.cellKind.SEAT', '좌석'),
    ROOM: t('label.cellKind.ROOM', '객실'),
    SPACE: t('label.cellKind.SPACE', '공간'),
    AISLE: t('label.cellKind.AISLE', '통로'),
    STAGE: t('label.cellKind.STAGE', '무대'),
    EMPTY: t('label.cellKind.EMPTY', '빈칸'),
});

export const submissionStatus = (t: T): Record<SubmissionStatus, string> => ({
    RECEIVED: t('label.submissionStatus.RECEIVED', '접수'),
    REVIEWING: t('label.submissionStatus.REVIEWING', '검토 중'),
    ACCEPTED: t('label.submissionStatus.ACCEPTED', '선정'),
    REJECTED: t('label.submissionStatus.REJECTED', '미선정'),
    WITHDRAWN: t('label.submissionStatus.WITHDRAWN', '취소'),
});

export const formFieldType = (t: T): Record<FormFieldType, string> => ({
    TEXT: t('label.formFieldType.TEXT', '짧은 글'),
    TEXTAREA: t('label.formFieldType.TEXTAREA', '긴 글'),
    NUMBER: t('label.formFieldType.NUMBER', '숫자'),
    DATE: t('label.formFieldType.DATE', '날짜'),
    EMAIL: t('label.formFieldType.EMAIL', '이메일'),
    PHONE: t('label.formFieldType.PHONE', '전화번호'),
    SELECT: t('label.formFieldType.SELECT', '선택(목록)'),
    RADIO: t('label.formFieldType.RADIO', '선택(하나)'),
    CHECKBOX: t('label.formFieldType.CHECKBOX', '체크(여러 개/동의)'),
    FILE: t('label.formFieldType.FILE', '파일 첨부'),
});
