import type {ExchangeRate, TaxSetting} from '../api/domain';
import {won} from './AdminCrudPage';
import {convertedAmounts, formatConverted, vatAmountOf} from './pricing';

/** 부가세·환율 둘 다 표시용 안내일 뿐 결제금액 자체를 바꾸지 않는다. */
export function PriceNotice({amount, tax, rates}: { amount: number; tax: TaxSetting | null; rates: ExchangeRate[] }) {
    const vat = vatAmountOf(amount, tax);
    const converted = convertedAmounts(amount, rates);
    if (!tax && converted.length === 0) {
        return null;
    }
    return (
        <div className="muted small">
            {tax && <div>(부가세 {tax.priceIncludesVat ? '포함' : '별도'} {tax.vatRate}%, {won(vat)})</div>}
            {converted.length > 0 && <div>≈ {formatConverted(converted)} (환율 안내용, 실제 결제는 원화)</div>}
        </div>
    );
}
