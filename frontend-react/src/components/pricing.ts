import type {ExchangeRate, TaxSetting} from '../api/domain';

/** 부가세는 표시용 안내일 뿐 결제금액 자체는 바꾸지 않는다(백엔드 TaxSetting 설계와 동일). */
export function vatAmountOf(amount: number, tax: TaxSetting | null): number {
    if (!tax || amount <= 0) {
        return 0;
    }
    const rate = tax.vatRate / 100;
    const base = tax.priceIncludesVat ? amount / (1 + rate) : amount;
    return Math.round(base * rate);
}

/** 환율도 표시용 환산 안내일 뿐이다. rate는 "1 단위당 원화 금액"이라 원화 금액을 나누면 환산된다. */
export function convertedAmounts(amount: number, rates: ExchangeRate[]): { currencyCode: string; amount: number }[] {
    return rates.filter((r) => r.enabled && Number(r.rate) > 0).map((r) => ({currencyCode: r.currencyCode, amount: amount / Number(r.rate)}));
}

export function formatConverted(converted: { currencyCode: string; amount: number }[]): string {
    return converted.map((c) => `${c.amount.toLocaleString('ko-KR', {maximumFractionDigits: 2})} ${c.currencyCode}`).join(' / ');
}
