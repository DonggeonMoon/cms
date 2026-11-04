import type {Order, OrderItem} from '../../api/domain';
import {won} from '../../components/AdminCrudPage';

export function OrderAmount({order}: { order: Order }) {
    if (order.totalPrice == null) {
        return <>견적 대기</>;
    }
    return (
        <>
            {won(order.totalPrice)}
            {Number(order.shippingFee ?? 0) > 0 && <div className="muted small">배송비 {won(order.shippingFee!)} 포함</div>}
            {Number(order.couponDiscount ?? 0) > 0 && <div className="muted small">쿠폰 -{won(order.couponDiscount!)}</div>}
            {order.pointsUsed > 0 && <div className="muted small">포인트 -{order.pointsUsed.toLocaleString('ko-KR')}P</div>}
            {order.pointsEarned > 0 && <div className="muted small">적립 {order.pointsEarned.toLocaleString('ko-KR')}P</div>}
            {Number(order.refundedAmount ?? 0) > 0 && <div className="muted small">환불 {won(order.refundedAmount!)}</div>}
        </>
    );
}

export function OrderItemsTable({order, onCancel}: { order: Order; onCancel?: (item: OrderItem) => void }) {
    if (order.items.length === 0) {
        return null;
    }
    return (
        <table className="compact">
            <thead>
            <tr><th>상품</th><th>단가</th><th>수량</th><th>금액</th><th>상태</th>{onCancel && <th/>}</tr>
            </thead>
            <tbody>
            {order.items.map((item) => (
                <tr key={item.id} className={item.status === 'CANCELED' ? 'muted' : undefined}>
                    <td>{item.productName}{item.optionName && <span className="muted"> ({item.optionName})</span>}</td>
                    <td>{won(item.unitPrice)}</td>
                    <td>{item.quantity}</td>
                    <td>{won(item.amount)}</td>
                    <td>
                        {item.status === 'CANCELED'
                            ? <span className="badge">취소{Number(item.refundedAmount ?? 0) > 0 && ` · 환불 ${won(item.refundedAmount!)}`}</span>
                            : '주문'}
                    </td>
                    {onCancel && (
                        <td className="button-cell">
                            {item.status === 'ORDERED' && <button type="button" className="danger" onClick={() => onCancel(item)}>부분 취소</button>}
                        </td>
                    )}
                </tr>
            ))}
            </tbody>
        </table>
    );
}
