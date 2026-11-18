import type {ExchangeRate, TaxSetting} from '../api/domain';
import {won} from './AdminCrudPage';
import {convertedAmounts, formatConverted, vatAmountOf} from './pricing';
import {useI18n} from '../api/i18n';

export function PriceNotice({amount, tax, rates}: { amount: number; tax: TaxSetting | null; rates: ExchangeRate[] }) {
    const {t} = useI18n();
    const vat = vatAmountOf(amount, tax);
    const converted = convertedAmounts(amount, rates);
    if (!tax && converted.length === 0) {
        return null;
    }
    return (
        <div className="muted small">
            {tax && <div>{t('shop.priceNotice.vat', '(부가세 {inclusion} {rate}%, {amount})', {
                inclusion: tax.priceIncludesVat ? t('shop.priceNotice.included', '포함') : t('shop.priceNotice.excluded', '별도'),
                rate: String(tax.vatRate), amount: won(vat),
            })}</div>}
            {converted.length > 0 && <div>{t('shop.priceNotice.converted', '≈ {converted} (환율 안내용, 실제 결제는 원화)', {converted: formatConverted(converted)})}</div>}
        </div>
    );
}
