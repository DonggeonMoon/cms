import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router';
import type {Cart, CartItem, ShippingPolicy} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {memberApi} from '../../api/members';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {estimateShipping} from '../../components/shipping';
import {Thumbnail} from '../../components/Thumbnail';
import {type DiscountChoice, DiscountSelector, NO_DISCOUNT} from './DiscountSelector';
import {EMPTY_STORE_CHOICE, type StoreChoice, StoreOrderFields} from './StoreOrderFields';

function ShippingLine({policy, items, postalCode}: { policy: ShippingPolicy | null; items: CartItem[]; postalCode?: string | null }) {
    const shippable = items.filter((item) => item.requiresShipping);
    const subtotal = shippable.reduce((sum, item) => sum + Number(item.amount), 0);
    const shipping = estimateShipping(policy, shippable.length > 0, subtotal, postalCode);
    const itemsAmount = items.reduce((sum, item) => sum + Number(item.amount), 0);
    return (
        <dl className="meta summary">
            <dt>상품 금액</dt>
            <dd>{won(itemsAmount)}</dd>
            <dt>배송비</dt>
            <dd>
                {won(shipping.fee)}{shipping.regionName && <span className="muted small"> ({shipping.regionName} 추가 포함)</span>}
                {shipping.remainingForFree != null && shipping.remainingForFree > 0 && (
                    <div className="muted small">{won(shipping.remainingForFree)} 더 담으면 기본 배송비가 무료입니다.</div>
                )}
            </dd>
            <dt>결제 예정 금액</dt>
            <dd className="price">{won(itemsAmount + shipping.fee)}</dd>
        </dl>
    );
}

export function CartPage() {
    const navigate = useNavigate();
    const [cart, setCart] = useState<Cart | null>(null);
    const [policy, setPolicy] = useState<ShippingPolicy | null>(null);
    const [postalCode, setPostalCode] = useState<string | null>(null);
    const [unselected, setUnselected] = useState<Set<number>>(new Set());
    const [error, setError] = useState<unknown>(null);

    const apply = useCallback((action: Promise<Cart>) => {
        setError(null);
        action.then(setCart).catch(setError);
    }, []);
    useEffect(() => {
        apply(shopApi.cart());
        shopApi.shippingPolicy().then(setPolicy).catch(() => setPolicy(null));
        memberApi.me().then((me) => setPostalCode(me.postalCode ?? null)).catch(() => undefined);
    }, [apply]);

    const selected = useMemo(() => (cart?.items ?? []).filter((item) => item.available && !unselected.has(item.id)), [cart, unselected]);
    const toggle = (id: number) => setUnselected((current) => {
        const next = new Set(current);
        if (next.has(id)) {
            next.delete(id);
        } else {
            next.add(id);
        }
        return next;
    });

    if (!cart) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    return (
        <section className="card">
            <h1>장바구니</h1>
            <ErrorBanner error={error}/>
            {cart.items.length === 0 ? (
                <p className="muted">장바구니가 비어 있습니다. <Link to="/products">상품 보러 가기</Link></p>
            ) : (
                <>
                    <ul className="cart-list">
                        {cart.items.map((item) => (
                            <li key={item.id} className={item.available ? undefined : 'unavailable'}>
                                <input type="checkbox" aria-label={`${item.productName} 선택`} disabled={!item.available}
                                       checked={item.available && !unselected.has(item.id)} onChange={() => toggle(item.id)}/>
                                <Thumbnail src={item.imageUrl} name={item.productName}/>
                                <div className="cart-item-info">
                                    <Link to={`/products/${item.productId}`}>{item.productName}</Link>
                                    {item.optionName && <span className="muted small">{item.optionName}</span>}
                                    <span className="muted small">{won(item.unitPrice)}</span>
                                    {!item.available && <span className="badge danger">주문 불가 (판매 중지·품절)</span>}
                                </div>
                                <input type="number" aria-label={`${item.productName} 수량`} min={1} max={item.stock ?? 999}
                                       value={item.quantity}
                                       onChange={(e) => apply(shopApi.changeCartQuantity(item.id, Math.max(1, Number(e.target.value) || 1)))}/>
                                <strong>{won(item.amount)}</strong>
                                <button type="button" className="danger" onClick={() => apply(shopApi.removeFromCart(item.id))}>삭제</button>
                            </li>
                        ))}
                    </ul>
                    <ShippingLine policy={policy} items={selected} postalCode={postalCode}/>
                    <div className="toolbar form-actions">
                        <button type="button" className="primary" disabled={selected.length === 0}
                                onClick={() => navigate(`/orders/cart?items=${selected.map((item) => item.id).join(',')}`)}>
                            선택 상품 주문하기 ({selected.length})
                        </button>
                    </div>
                </>
            )}
        </section>
    );
}

export function CartOrderPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const ids = useMemo(() => (params.get('items') ?? '').split(',').map(Number).filter(Boolean), [params]);
    const [items, setItems] = useState<CartItem[] | null>(null);
    const [receiver, setReceiver] = useState({receiverName: '', phoneNumber: '', postalCode: '', address: '', detailAddress: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);
    const [discount, setDiscount] = useState<DiscountChoice>(NO_DISCOUNT);
    const [storeChoice, setStoreChoice] = useState<StoreChoice>(EMPTY_STORE_CHOICE);
    const [storeOpen, setStoreOpen] = useState(true);

    useEffect(() => {
        shopApi.cart().then((cart) => setItems(cart.items.filter((item) => item.available && ids.includes(item.id)))).catch(setError);
        memberApi.me().then((me) => setReceiver((current) => ({
            receiverName: current.receiverName || me.name || '',
            phoneNumber: current.phoneNumber || me.mobilePhoneNumber || me.telephoneNumber || '',
            postalCode: current.postalCode || me.postalCode || '',
            address: current.address || me.address || '',
            detailAddress: current.detailAddress || me.detailAddress || '',
        }))).catch(() => undefined);
    }, [ids]);

    if (!items) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    const store = items.length > 0 && items.every((item) => item.storeOrder);
    const mixed = items.some((item) => item.storeOrder) && !store;
    const needsAddress = !store && items.some((item) => item.requiresShipping);
    const previewLines = items.map((item) => ({productId: item.productId, productOptionId: item.productOptionId, quantity: item.quantity}));
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const order = await shopApi.checkoutCart({cartItemIds: items.map((item) => item.id), ...discount,
                ...(store ? {receiverName: receiver.receiverName, phoneNumber: receiver.phoneNumber, ...storeChoice} : receiver)});
            navigate(`/checkout/ORDER/${order.id}`);
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>주문 정보 입력</h1>
            {items.length === 0 ? <p className="muted">주문할 상품이 없습니다. <Link to="/cart">장바구니로</Link></p> : (
                <form onSubmit={submit}>
                    <ul className="plain-list">
                        {items.map((item) => (
                            <li key={item.id}>
                                {item.productName}{item.optionName && ` (${item.optionName})`} × {item.quantity} — {won(item.amount)}
                            </li>
                        ))}
                    </ul>
                    {mixed && <p className="notice-box">매장 주문 전용 상품과 배송 상품은 함께 주문할 수 없습니다. 장바구니에서 나눠 주문해 주세요.</p>}
                    {store && <StoreOrderFields value={storeChoice} onChange={setStoreChoice} onAvailability={setStoreOpen}/>}
                    <fieldset>
                        <legend>{store ? '주문자 정보' : '배송 정보'}</legend>
                        <Field label={store ? '주문자' : '수령인'} name="receiverName" required value={receiver.receiverName}
                               onChange={(e) => setReceiver({...receiver, receiverName: e.target.value})}/>
                        <Field label="연락처" name="phoneNumber" type="tel" required value={receiver.phoneNumber}
                               onChange={(e) => setReceiver({...receiver, phoneNumber: e.target.value})}/>
                        {!store && <>
                        <Field label="우편번호" name="postalCode" value={receiver.postalCode}
                               onChange={(e) => setReceiver({...receiver, postalCode: e.target.value})}/>
                        <Field label="주소" name="address" required={needsAddress} value={receiver.address}
                               onChange={(e) => setReceiver({...receiver, address: e.target.value})}/>
                        <Field label="상세 주소" name="detailAddress" value={receiver.detailAddress}
                               onChange={(e) => setReceiver({...receiver, detailAddress: e.target.value})}/>
                        </>}
                    </fieldset>
                    <DiscountSelector lines={previewLines} postalCode={store ? null : receiver.postalCode} value={discount} onChange={setDiscount}/>
                    <ErrorBanner error={error}/>
                    <div className="toolbar form-actions">
                        <button type="button" onClick={() => navigate('/cart')}>장바구니로</button>
                        <button type="submit" className="primary" disabled={submitting || mixed || (store && !storeOpen)}>
                            {submitting ? '주문 중…' : '주문하고 결제하기'}
                        </button>
                    </div>
                </form>
            )}
        </section>
    );
}
