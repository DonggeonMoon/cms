import type {Order, OrderItem} from '../../api/domain';
import {won} from '../../components/AdminCrudPage';
import {useI18n} from '../../api/i18n';

export function OrderAmount({order}: { order: Order }) {
    const {t} = useI18n();
    if (order.totalPrice == null) {
        return <>{t('shop.orderItems.pendingQuote', '견적 대기')}</>;
    }
    return (
        <>
            {won(order.totalPrice)}
            {Number(order.shippingFee ?? 0) > 0 && <div className="muted small">{t('shop.orderItems.shippingIncluded', '배송비 {amount} 포함', {amount: won(order.shippingFee!)})}</div>}
            {Number(order.couponDiscount ?? 0) > 0 && <div className="muted small">{t('shop.orderItems.couponDiscount', '쿠폰 -{amount}', {amount: won(order.couponDiscount!)})}</div>}
            {order.pointsUsed > 0 && <div className="muted small">{t('shop.orderItems.pointsUsed', '포인트 -{amount}P', {amount: order.pointsUsed.toLocaleString('ko-KR')})}</div>}
            {order.pointsEarned > 0 && <div className="muted small">{t('shop.orderItems.pointsEarned', '적립 {amount}P', {amount: order.pointsEarned.toLocaleString('ko-KR')})}</div>}
            {Number(order.refundedAmount ?? 0) > 0 && <div className="muted small">{t('shop.orderItems.refunded', '환불 {amount}', {amount: won(order.refundedAmount!)})}</div>}
        </>
    );
}

export function OrderItemsTable({order, onCancel}: { order: Order; onCancel?: (item: OrderItem) => void }) {
    const {t} = useI18n();
    if (order.items.length === 0) {
        return null;
    }
    return (
        <table className="compact">
            <thead>
            <tr>
                <th>{t('admin.product.product', '상품')}</th><th>{t('admin.order.column.unitPrice', '단가')}</th>
                <th>{t('admin.order.column.quantity', '수량')}</th><th>{t('admin.order.column.amount', '금액')}</th>
                <th>{t('admin.order.column.status', '상태')}</th>{onCancel && <th/>}
            </tr>
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
                            ? <span className="badge">{t('shop.orderItems.canceled', '취소')}{Number(item.refundedAmount ?? 0) > 0 && t('shop.orderItems.canceledRefund', ' · 환불 {amount}', {amount: won(item.refundedAmount!)})}</span>
                            : t('shop.orderItems.ordered', '주문')}
                    </td>
                    {onCancel && (
                        <td className="button-cell">
                            {item.status === 'ORDERED' && <button type="button" className="danger" onClick={() => onCancel(item)}>{t('shop.orderItems.partialCancel', '부분 취소')}</button>}
                        </td>
                    )}
                </tr>
            ))}
            </tbody>
        </table>
    );
}
