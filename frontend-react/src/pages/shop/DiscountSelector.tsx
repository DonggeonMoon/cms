import {useEffect, useState} from 'react';
import {type OrderPreview, previewOrder, type PreviewLine} from '../../api/rewards';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';

export interface DiscountChoice {
    couponIssueId: number | null;
    usePoints: number;
}

export const NO_DISCOUNT: DiscountChoice = {couponIssueId: null, usePoints: 0};

/** 쿠폰·포인트를 고르면 서버에서 금액을 다시 계산해 보여 준다. 결제 금액은 서버 계산과 같아야 하므로 화면에서 따로 계산하지 않는다. */
export function DiscountSelector({lines, postalCode, value, onChange}: {
    lines: PreviewLine[];
    postalCode: string | null | undefined;
    value: DiscountChoice;
    onChange: (choice: DiscountChoice) => void;
}) {
    const [preview, setPreview] = useState<OrderPreview | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [pointsText, setPointsText] = useState(value.usePoints ? String(value.usePoints) : '');
    const key = JSON.stringify(lines);

    useEffect(() => {
        if (lines.length === 0) {
            return;
        }
        let alive = true;
        const timer = setTimeout(() => {
            previewOrder({lines, couponIssueId: value.couponIssueId, usePoints: value.usePoints, postalCode})
                .then((result) => {
                    if (alive) {
                        setPreview(result);
                        setError(null);
                    }
                })
                .catch((e) => alive && setError(e));
        }, 250);
        return () => {
            alive = false;
            clearTimeout(timer);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [key, value.couponIssueId, value.usePoints, postalCode]);

    if (!preview) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">금액을 계산하는 중…</p>;
    }
    const setPoints = (points: number) => {
        const clamped = Math.max(0, Math.min(points, preview.maxUsablePoints));
        setPointsText(clamped ? String(clamped) : '');
        onChange({...value, usePoints: clamped});
    };
    return (
        <div className="discount-selector">
            {preview.coupons.length > 0 && (
                <div className="field">
                    <label htmlFor="coupon">쿠폰</label>
                    <select id="coupon" value={value.couponIssueId ?? ''}
                            onChange={(e) => onChange({couponIssueId: e.target.value ? Number(e.target.value) : null, usePoints: 0})}>
                        <option value="">쿠폰 사용 안 함</option>
                        {preview.coupons.map((coupon) => (
                            <option key={coupon.issueId} value={coupon.issueId} disabled={!coupon.applicable}>
                                {coupon.name} {coupon.applicable ? `(-${won(coupon.discount)})` : '· 조건 미달'}
                            </option>
                        ))}
                    </select>
                </div>
            )}
            {preview.pointBalance > 0 && (
                <div className="field">
                    <label htmlFor="usePoints">포인트 (보유 {preview.pointBalance.toLocaleString('ko-KR')}P)</label>
                    <div className="toolbar">
                        <input id="usePoints" type="number" min={0} max={preview.maxUsablePoints} value={pointsText}
                               placeholder="0" onChange={(e) => setPointsText(e.target.value)}
                               onBlur={() => setPoints(Number(pointsText) || 0)}/>
                        <button type="button" onClick={() => setPoints(preview.maxUsablePoints)}>최대 사용</button>
                    </div>
                    <p className="muted small">
                        이번 주문에 최대 {preview.maxUsablePoints.toLocaleString('ko-KR')}P
                        {preview.minUsePoints > 0 && ` · ${preview.minUsePoints.toLocaleString('ko-KR')}P 이상부터`} 쓸 수 있습니다.
                    </p>
                </div>
            )}
            <ErrorBanner error={error}/>
            <dl className="meta summary">
                <dt>상품 금액</dt>
                <dd>{won(preview.itemsAmount)}</dd>
                <dt>배송비</dt>
                <dd>{won(preview.shippingFee)}</dd>
                {Number(preview.couponDiscount) > 0 && (<><dt>쿠폰 할인</dt><dd>-{won(preview.couponDiscount)}</dd></>)}
                {preview.pointsUsed > 0 && (<><dt>포인트 사용</dt><dd>-{won(preview.pointsUsed)}</dd></>)}
                <dt>결제 예정 금액</dt>
                <dd className="price">{won(preview.totalPrice)}</dd>
                {preview.pointsToEarn > 0 && (<><dt>적립 예정</dt><dd>{preview.pointsToEarn.toLocaleString('ko-KR')}P</dd></>)}
            </dl>
        </div>
    );
}
