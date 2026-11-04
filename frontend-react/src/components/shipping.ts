import type {ShippingPolicy} from '../api/domain';

export interface ShippingEstimate {
    fee: number;
    regionName: string | null;
    remainingForFree: number | null;
}

/** 서버의 ShippingPolicy.quote 와 같은 규칙: 무료 배송 기준은 기본 배송비에만, 지역 추가 배송비는 늘 더한다. */
export function estimateShipping(policy: ShippingPolicy | null, hasShippable: boolean, subtotal: number,
                                 postalCode: string | null | undefined): ShippingEstimate {
    if (!policy || !hasShippable) {
        return {fee: 0, regionName: null, remainingForFree: null};
    }
    const threshold = policy.freeThreshold == null ? null : Number(policy.freeThreshold);
    const base = threshold != null && subtotal >= threshold ? 0 : Number(policy.baseFee);
    const code = (postalCode ?? '').replace(/\D/g, '');
    const region = code.length === 5
        ? policy.regionFees.find((r) => code >= r.postalCodeFrom && code <= r.postalCodeTo) ?? null
        : null;
    return {
        fee: base + Number(region?.fee ?? 0),
        regionName: region?.name ?? null,
        remainingForFree: threshold != null && base > 0 ? threshold - subtotal : null,
    };
}
