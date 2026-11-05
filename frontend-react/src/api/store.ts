import {request} from './client';
import type {Order} from './domain';

export type FulfillmentType = 'DELIVERY' | 'PICKUP' | 'DINE_IN' | 'LOCAL_DELIVERY';
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export const FULFILLMENT: Record<FulfillmentType, string> = {
    DELIVERY: '배송', PICKUP: '포장', DINE_IN: '매장 식사', LOCAL_DELIVERY: '배달',
};
export const DAY_OF_WEEK: Record<DayOfWeek, string> = {
    MONDAY: '월', TUESDAY: '화', WEDNESDAY: '수', THURSDAY: '목', FRIDAY: '금', SATURDAY: '토', SUNDAY: '일',
};

export interface StoreSetting {
    pickupEnabled: boolean;
    dineInEnabled: boolean;
    tables: string[];
    openTime: string | null;
    closeTime: string | null;
    closedDays: DayOfWeek[];
    paused: boolean;
    notice: string | null;
    localDeliveryEnabled: boolean;
    deliveryZones: string[];
    deliveryFee: number | null;
    deliveryMinimumAmount: number | null;
    deliverySlotMinutes: number | null;
    openNow: boolean;
}

export const storeApi = {
    setting: () => request<StoreSetting>('/api/store-setting'),
    save: (body: unknown) => request<StoreSetting>('/api/admin/store-setting', {method: 'PUT', body}),
    queue: () => request<Order[]>('/api/admin/store-orders'),
};
