import type {ProductSummary} from '../api/domain';
import {won} from './AdminCrudPage';
import {billingCycle, type T} from './labels';

export function priceLabel(product: Pick<ProductSummary, 'saleType' | 'price' | 'billingCycle'>, t: T): string {
    switch (product.saleType) {
        case 'SUBSCRIPTION':
            return `${product.billingCycle ? billingCycle(t)[product.billingCycle] : ''} ${won(product.price)}`.trim();
        case 'DONATION':
            return t('label.priceLabel.donationFrom', '{price}부터', {price: won(product.price)});
        case 'QUOTE':
            return t('label.priceLabel.quote', '견적 문의');
        case 'INQUIRY':
            return t('label.priceLabel.inquiry', '문의만');
        default:
            return won(product.price);
    }
}
