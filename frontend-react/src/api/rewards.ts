import {request} from './client';
import type {Page} from './types';

export type PointType = 'EARN' | 'SIGNUP' | 'REFERRAL' | 'ADMIN' | 'USE' | 'RESTORE' | 'REVOKE' | 'EXPIRE';

export type TFunc = (key: string, fallback: string, values?: Record<string, string>) => string;

export const pointType = (t: TFunc): Record<PointType, string> => ({
    EARN: t('label.pointType.EARN', '구매 적립'), SIGNUP: t('label.pointType.SIGNUP', '가입 적립'),
    REFERRAL: t('label.pointType.REFERRAL', '추천인 적립'), ADMIN: t('label.pointType.ADMIN', '관리자 조정'),
    USE: t('label.pointType.USE', '사용'), RESTORE: t('label.pointType.RESTORE', '사용 취소'),
    REVOKE: t('label.pointType.REVOKE', '적립 취소'), EXPIRE: t('label.pointType.EXPIRE', '소멸'),
});

export interface PointPolicy {
    isEnabled: boolean;
    earnRate: number;
    expireDays: number;
    minUsePoints: number;
    maxUseRate: number;
    signupPoints: number;
    referralPoints: number;
}

export interface PointSummary {
    balance: number;
    expiringSoon: number;
    expiringBy: string;
}

export interface PointLedger {
    id: number;
    type: PointType;
    amount: number;
    balanceAfter: number;
    expiresAt: string | null;
    orderId: number | null;
    memo: string | null;
    createdAt: string;
}

export type DiscountType = 'AMOUNT' | 'RATE';
export type CouponIssueType = 'DOWNLOAD' | 'SIGNUP' | 'GRADE' | 'MANUAL';
export type CouponIssueStatus = 'AVAILABLE' | 'USED' | 'EXPIRED';

export const couponIssueType = (t: TFunc): Record<CouponIssueType, string> => ({
    DOWNLOAD: t('label.couponIssueType.DOWNLOAD', '다운로드'), SIGNUP: t('label.couponIssueType.SIGNUP', '가입 시 자동 지급'),
    GRADE: t('label.couponIssueType.GRADE', '등급 달성 시 자동 지급'), MANUAL: t('label.couponIssueType.MANUAL', '관리자 지급'),
});

export const couponStatus = (t: TFunc): Record<CouponIssueStatus, string> => ({
    AVAILABLE: t('label.couponStatus.AVAILABLE', '사용 가능'), USED: t('label.couponStatus.USED', '사용함'), EXPIRED: t('label.couponStatus.EXPIRED', '기한 만료'),
});

export interface Coupon {
    id: number;
    name: string;
    description: string | null;
    discountType: DiscountType;
    discountValue: number;
    maxDiscountAmount: number | null;
    minOrderAmount: number | null;
    productId: number | null;
    issueType: CouponIssueType;
    targetGradeId: number | null;
    issueStartAt: string | null;
    issueEndAt: string | null;
    validDays: number | null;
    validUntil: string | null;
    totalQuantity: number | null;
    issuedCount: number;
    isEnabled: boolean;
}

export interface CouponIssue {
    id: number;
    couponId: number;
    memberId: number;
    username: string | null;
    couponName: string;
    discountType: DiscountType | null;
    discountValue: number | null;
    maxDiscountAmount: number | null;
    minOrderAmount: number | null;
    productId: number | null;
    status: CouponIssueStatus;
    expiresAt: string | null;
    orderId: number | null;
    usedAt: string | null;
    createdAt: string;
}

export interface CouponOption {
    issueId: number;
    name: string;
    discount: number;
    applicable: boolean;
    expiresAt: string | null;
}

export interface OrderPreview {
    itemsAmount: number;
    shippingFee: number;
    couponDiscount: number;
    pointsUsed: number;
    totalPrice: number;
    pointBalance: number;
    maxUsablePoints: number;
    minUsePoints: number;
    pointsToEarn: number;
    coupons: CouponOption[];
}

export interface PreviewLine {
    productId: number;
    productOptionId: number | null;
    quantity: number;
    discountId?: number | null;
}

export const won0 = (value: number | null | undefined) => `${Number(value ?? 0).toLocaleString('ko-KR')}`;

/** 백엔드 Coupon.discountFor()·PointService.maxUsable()와 같은 계산. 실제 승인은 서버가 다시 검증한다. */
export function couponDiscountFor(coupon: { discountType: DiscountType; discountValue: number; maxDiscountAmount: number | null; minOrderAmount: number | null }, base: number) {
    if (base <= 0 || (coupon.minOrderAmount && base < coupon.minOrderAmount)) {
        return 0;
    }
    let discount = coupon.discountType === 'AMOUNT' ? coupon.discountValue : Math.floor(base * coupon.discountValue / 100);
    if (coupon.discountType === 'RATE' && coupon.maxDiscountAmount) {
        discount = Math.min(discount, coupon.maxDiscountAmount);
    }
    return Math.min(discount, base);
}

export function maxUsablePoints(policy: PointPolicy, balance: number, afterCoupon: number) {
    if (!policy.isEnabled) {
        return 0;
    }
    const cap = Math.floor(afterCoupon * policy.maxUseRate / 100);
    return Math.max(0, Math.min(balance, cap));
}

export function couponBenefit(coupon: { discountType: DiscountType | null; discountValue: number | null; maxDiscountAmount: number | null; minOrderAmount: number | null }, t: TFunc) {
    if (!coupon.discountType) {
        return '-';
    }
    const main = coupon.discountType === 'RATE'
        ? t('label.couponBenefit.rate', '{percent}% 할인', {percent: String(Number(coupon.discountValue))})
        : t('label.couponBenefit.amount', '{amount}원 할인', {amount: won0(coupon.discountValue)});
    const cap = coupon.discountType === 'RATE' && coupon.maxDiscountAmount
        ? t('label.couponBenefit.cap', ' (최대 {amount}원)', {amount: won0(coupon.maxDiscountAmount)}) : '';
    const min = coupon.minOrderAmount
        ? t('label.couponBenefit.min', ' · {amount}원 이상 구매 시', {amount: won0(coupon.minOrderAmount)}) : '';
    return main + cap + min;
}

export const pointApi = {
    policy: () => request<PointPolicy>('/api/points/policy'),
    mine: () => request<PointSummary>('/api/members/me/points'),
    myHistory: (page: number) => request<Page<PointLedger>>('/api/members/me/points/history', {query: {page}}),
    savePolicy: (body: PointPolicy) => request<PointPolicy>('/api/admin/points/policy', {method: 'PUT', body: {...body}}),
    ofMember: (memberId: number) => request<PointSummary>(`/api/admin/members/${memberId}/points`),
    historyOf: (memberId: number, page: number) =>
        request<Page<PointLedger>>(`/api/admin/members/${memberId}/points/history`, {query: {page}}),
    adjust: (memberId: number, amount: number, memo: string) =>
        request<void>(`/api/admin/members/${memberId}/points`, {method: 'POST', body: {amount, memo}}),
};

export const couponApi = {
    downloadable: () => request<{ coupon: Coupon; downloaded: boolean; soldOut: boolean }[]>('/api/coupons'),
    download: (id: number) => request<CouponIssue>(`/api/coupons/${id}/download`, {method: 'POST'}),
    mine: () => request<CouponIssue[]>('/api/members/me/coupons'),
    list: () => request<Coupon[]>('/api/admin/coupons'),
    create: (body: Record<string, unknown>) => request<Coupon>('/api/admin/coupons', {method: 'POST', body}),
    update: (id: number, body: Record<string, unknown>) => request<Coupon>(`/api/admin/coupons/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/coupons/${id}`, {method: 'DELETE'}),
    issues: (id: number, page: number) => request<Page<CouponIssue>>(`/api/admin/coupons/${id}/issues`, {query: {page}}),
    issue: (id: number, usernames: string[]) =>
        request<{ issued: number; skipped: string[] }>(`/api/admin/coupons/${id}/issues`, {method: 'POST', body: {usernames}}),
};

export const previewOrder = (body: { lines: PreviewLine[]; couponIssueId: number | null; usePoints: number; postalCode?: string | null }) =>
    request<OrderPreview>('/api/orders/preview', {method: 'POST', body});
