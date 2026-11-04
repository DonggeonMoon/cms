import type {ProductSummary} from '../api/domain';
import {won} from './AdminCrudPage';
import {BILLING_CYCLE} from './labels';

export function priceLabel(product: Pick<ProductSummary, 'saleType' | 'price' | 'billingCycle'>): string {
    switch (product.saleType) {
        case 'SUBSCRIPTION':
            return `${product.billingCycle ? BILLING_CYCLE[product.billingCycle] : ''} ${won(product.price)}`.trim();
        case 'DONATION':
            return `${won(product.price)}부터`;
        case 'QUOTE':
            return '견적 문의';
        default:
            return won(product.price);
    }
}
