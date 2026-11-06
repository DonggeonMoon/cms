import {type FormEvent, useEffect, useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router';
import type {ProductDetail, ProductSaleType, ReservationItem} from '../../api/domain';
import {EMPTY_STORE_CHOICE, type StoreChoice, StoreOrderFields} from './StoreOrderFields';
import {type DiscountChoice, DiscountSelector, NO_DISCOUNT} from './DiscountSelector';
import {shopApi} from '../../api/endpoints';
import {memberApi} from '../../api/members';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDate, formatDateTime} from '../../components/format';
import {BILLING_CYCLE} from '../../components/labels';
import {reservationUsage} from '../../components/reservationUsage';
import {StableLabel} from '../../components/StableLabel';
import {estimateReservation} from './ReservationPicker';

const positive = (value: string | null, fallback = 1) => Math.max(fallback, Number(value) || fallback);

const MODES: Record<ProductSaleType, { title: string; receiverLegend: string; totalLabel: string; submit: string }> = {
    PURCHASE: {title: '주문 정보 입력', receiverLegend: '배송 정보', totalLabel: '결제 예정 금액', submit: '주문하고 결제하기'},
    SUBSCRIPTION: {title: '구독 신청', receiverLegend: '배송 정보', totalLabel: '회차별 결제 금액', submit: '구독하고 결제하기'},
    DONATION: {title: '후원 정보 입력', receiverLegend: '후원자 정보', totalLabel: '후원 금액', submit: '후원하고 결제하기'},
    QUOTE: {title: '견적 문의', receiverLegend: '연락처', totalLabel: '', submit: '견적 요청 보내기'},
    PASS: {title: '이용권 구매', receiverLegend: '구매자 정보', totalLabel: '결제 금액', submit: '구매하고 결제하기'},
    GIFT_CARD: {title: '상품권 구매', receiverLegend: '구매자 정보', totalLabel: '결제 금액', submit: '구매하고 결제하기'},
    PREORDER: {title: '예약 판매 주문', receiverLegend: '배송 정보', totalLabel: '결제 예정 금액', submit: '예약 주문하고 결제하기'},
    FUNDING: {title: '펀딩 참여', receiverLegend: '배송 정보', totalLabel: '결제 금액', submit: '펀딩 참여하고 결제하기'},
    INQUIRY: {title: '', receiverLegend: '', totalLabel: '', submit: ''},
};

export function OrderFormPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const productId = Number(params.get('productId'));
    const optionId = params.get('optionId') ?? '';
    const discountId = params.get('discountId') ?? '';
    const quantity = positive(params.get('quantity'));
    const amount = Number(params.get('amount')) || 0;
    const [requestMessage, setRequestMessage] = useState('');
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [rewards, setRewards] = useState<DiscountChoice>(NO_DISCOUNT);
    const [storeChoice, setStoreChoice] = useState<StoreChoice>(EMPTY_STORE_CHOICE);
    const [storeOpen, setStoreOpen] = useState(true);
    const [receiver, setReceiver] = useState({receiverName: '', phoneNumber: '', postalCode: '', address: '', detailAddress: '', country: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        shopApi.product(productId).then(setProduct).catch(setError);
        memberApi.me().then((me) => setReceiver((current) => ({
            receiverName: current.receiverName || me.name || '',
            phoneNumber: current.phoneNumber || me.mobilePhoneNumber || me.telephoneNumber || '',
            postalCode: current.postalCode || me.postalCode || '',
            address: current.address || me.address || '',
            detailAddress: current.detailAddress || me.detailAddress || '',
            country: current.country,
        }))).catch(() => undefined);
    }, [productId]);

    if (error && !product) {
        return <ErrorBanner error={error}/>;
    }
    if (!product) {
        return <p className="muted">불러오는 중…</p>;
    }
    const option = product.options.find((o) => String(o.id) === optionId);
    const discount = product.discounts.find((d) => String(d.id) === discountId);
    const subtotal = (Number(product.price) + Number(option?.additionalPrice ?? 0)) * quantity;
    const total = product.saleType === 'DONATION' ? amount
        : discount ? Math.round(subtotal * (100 - Number(discount.rate)) / 100) : subtotal;
    const mode = MODES[product.saleType ?? 'PURCHASE'];
    const purchase = (product.saleType ?? 'PURCHASE') === 'PURCHASE';
    const shipsLikePurchase = product.saleType === 'PREORDER' || product.saleType === 'FUNDING';
    const store = purchase && product.storeOrder;
    const delivering = store && storeChoice.fulfillmentType === 'LOCAL_DELIVERY';
    const needsAddress = shipsLikePurchase ? product.requiresShipping !== false
        : purchase && ((!store && product.requiresShipping !== false) || delivering);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const created = await shopApi.order({
                productId,
                productOptionId: option ? option.id : null,
                discountId: discount ? discount.id : null,
                quantity,
                ...(product.saleType === 'DONATION' || (store && !delivering)
                    ? {receiverName: receiver.receiverName, phoneNumber: receiver.phoneNumber} : receiver),
                ...(store ? {...storeChoice, deliverySlotAt: storeChoice.deliverySlotAt ? `${storeChoice.deliverySlotAt}:00` : null} : {}),
                amount: product.saleType === 'DONATION' ? amount : null,
                ...(store ? {} : {requestMessage: product.saleType === 'QUOTE' ? requestMessage : null}),
                ...(purchase ? rewards : {}),
            });
            navigate(product.saleType === 'QUOTE' ? '/mypage/orders' : `/checkout/ORDER/${created.id}`);
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>{mode.title}</h1>
            <dl className="meta">
                <dt>상품</dt>
                <dd><Link to={`/products/${product.id}`}>{product.name}</Link></dd>
                {option && (<><dt>옵션</dt><dd>{option.name} (+{won(option.additionalPrice)})</dd></>)}
                {discount && (<><dt>할인</dt><dd>{discount.name} ({discount.rate}%)</dd></>)}
                {product.saleType !== 'DONATION' && (<><dt>수량</dt><dd>{quantity}개</dd></>)}
                {product.saleType === 'SUBSCRIPTION' && product.billingCycle && (<><dt>결제 주기</dt><dd>{BILLING_CYCLE[product.billingCycle]}</dd></>)}
                {product.preorder?.shipDate && (<><dt>출고 예정일</dt><dd>{formatDate(product.preorder.shipDate)}</dd></>)}
                {product.funding?.deadline && (<><dt>펀딩 마감</dt><dd>{formatDateTime(product.funding.deadline)}</dd></>)}
            </dl>
            <form onSubmit={submit}>
                {product.saleType === 'QUOTE' && (
                    <div className="field">
                        <label htmlFor="requestMessage">요청 내용</label>
                        <textarea id="requestMessage" rows={5} maxLength={2000} value={requestMessage}
                                  placeholder="필요한 수량, 일정, 조건 등을 적어 주세요."
                                  onChange={(e) => setRequestMessage(e.target.value)}/>
                    </div>
                )}
                {store && <StoreOrderFields value={storeChoice} onChange={setStoreChoice} onAvailability={setStoreOpen}/>}
                <fieldset>
                    <legend>{store ? '주문자 정보' : mode.receiverLegend}</legend>
                    <Field label={product.saleType === 'DONATION' ? '후원자 이름' : product.saleType === 'QUOTE' ? '담당자 이름' : store ? '주문자' : '수령인'}
                           name="receiverName" required value={receiver.receiverName}
                           onChange={(e) => setReceiver({...receiver, receiverName: e.target.value})}/>
                    <Field label="연락처" name="phoneNumber" type="tel" required value={receiver.phoneNumber}
                           onChange={(e) => setReceiver({...receiver, phoneNumber: e.target.value})}/>
                    {product.saleType !== 'DONATION' && (!store || delivering) && (
                        <>
                            <Field label="우편번호" name="postalCode" value={receiver.postalCode}
                                   onChange={(e) => setReceiver({...receiver, postalCode: e.target.value})}/>
                            <Field label="주소" name="address" required={needsAddress} value={receiver.address}
                                   onChange={(e) => setReceiver({...receiver, address: e.target.value})}/>
                            <Field label="상세 주소" name="detailAddress" value={receiver.detailAddress}
                                   onChange={(e) => setReceiver({...receiver, detailAddress: e.target.value})}/>
                            <Field label="해외 배송 국가 코드 (국내는 비움, 예: US, JP)" name="country" value={receiver.country}
                                   onChange={(e) => setReceiver({...receiver, country: e.target.value.toUpperCase()})}/>
                        </>
                    )}
                </fieldset>
                {product.saleType === 'QUOTE'
                    ? <p className="muted">요청을 보내면 관리자가 견적 금액을 회신합니다. 마이페이지 주문 내역에서 수락하고 결제할 수 있습니다.</p>
                    : (
                        purchase ? (
                            <DiscountSelector postalCode={needsAddress ? receiver.postalCode : null} value={rewards} onChange={setRewards}
                                              lines={[{productId, productOptionId: option ? option.id : null, quantity,
                                                  discountId: discount ? discount.id : null}]}/>
                        ) : <p className="price align-right">{mode.totalLabel} {won(total)}</p>
                    )}
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    <button type="button" onClick={() => navigate(-1)}>이전</button>
                    <button type="submit" className="primary" disabled={submitting || (store && !storeOpen)}>
                        <StableLabel current={submitting ? '보내는 중…' : mode.submit} labels={['보내는 중…', mode.submit]}/>
                    </button>
                </div>
            </form>
        </section>
    );
}

export function ReservationFormPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const reservationItemId = Number(params.get('reservationItemId'));
    const quantity = positive(params.get('quantity'));
    const sessionId = params.get('sessionId') ? Number(params.get('sessionId')) : null;
    const passId = params.get('passId') ? Number(params.get('passId')) : null;
    const partySize = params.get('partySize') ? Number(params.get('partySize')) : null;
    const partyCounts = params.get('partyCounts') ? JSON.parse(params.get('partyCounts')!) : null;
    const selection = {startAt: params.get('startAt'), endAt: params.get('endAt'),
        cells: (params.get('cells') ?? '').split(',').filter(Boolean), quantity, partyCounts};
    const [item, setItem] = useState<ReservationItem | null>(null);
    const [form, setForm] = useState({applicantName: '', phoneNumber: ''});
    const [visit, setVisit] = useState({visitPostalCode: '', visitAddress: '', visitDetailAddress: '', requestMemo: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        shopApi.reservationItem(reservationItemId).then(setItem).catch(setError);
        memberApi.me().then((me) => setForm((current) => ({
            applicantName: current.applicantName || me.name || '',
            phoneNumber: current.phoneNumber || me.mobilePhoneNumber || me.telephoneNumber || '',
        }))).catch(() => undefined);
    }, [reservationItemId]);

    if (error && !item) {
        return <ErrorBanner error={error}/>;
    }
    if (!item) {
        return <p className="muted">불러오는 중…</p>;
    }

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const reservation = await shopApi.reserve({reservationItemId, ...selection, quantity, sessionId, passId, partySize, ...form,
                ...(item.reservationType === 'VISIT' ? visit : {})});
            navigate(reservation.status === 'PENDING_PAYMENT' ? `/checkout/RESERVATION/${reservation.id}` : '/mypage/reservations');
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>예약 정보 입력</h1>
            <dl className="meta">
                <dt>예약 상품</dt>
                <dd><Link to={`/reservation-items/${item.id}`}>{item.name}</Link></dd>
                {item.reservationType === 'PROGRAM' ? (
                    <>
                        <dt>진행 일정</dt>
                        <dd>{(() => {
                            const session = item.sessions.find((s) => s.id === sessionId);
                            return session
                                ? `${formatDateTime(session.startAt)}${session.endAt ? ` ~ ${formatDateTime(session.endAt)}` : ''}${session.name ? ` · ${session.name}` : ''}`
                                : `${formatDateTime(item.startDateTime)} ~ ${formatDateTime(item.endDateTime)}`;
                        })()}</dd>
                        <dt>인원</dt>
                        <dd>{quantity}명</dd>
                    </>
                ) : (
                    <>
                        <dt>이용</dt>
                        <dd>{reservationUsage({reservationType: item.reservationType, ...selection, partySize, partyBreakdown: partyCounts,
                            cells: selection.cells.map((key) => item.layout?.cells.find((c) => c.key === key)?.label ?? key)})}</dd>
                        {item.reservationType === 'ITEM_RENTAL' && (
                            <>
                                <dt>수량</dt>
                                <dd>{quantity}개{Number(item.depositAmount ?? 0) > 0 && ` · 보증금 ${won(Number(item.depositAmount) * quantity)} 포함`}</dd>
                            </>
                        )}
                    </>
                )}
            </dl>
            <form onSubmit={submit}>
                <fieldset>
                    <legend>신청자 정보</legend>
                    <Field label="신청자" name="applicantName" required value={form.applicantName}
                           onChange={(e) => setForm({...form, applicantName: e.target.value})}/>
                    <Field label="연락처" name="phoneNumber" type="tel" required value={form.phoneNumber}
                           onChange={(e) => setForm({...form, phoneNumber: e.target.value})}/>
                </fieldset>
                {item.reservationType === 'VISIT' && (
                    <fieldset>
                        <legend>방문 정보</legend>
                        <Field label="우편번호" name="visitPostalCode" value={visit.visitPostalCode}
                               onChange={(e) => setVisit({...visit, visitPostalCode: e.target.value})}/>
                        <Field label="방문 주소" name="visitAddress" required value={visit.visitAddress}
                               onChange={(e) => setVisit({...visit, visitAddress: e.target.value})}/>
                        <Field label="상세 주소" name="visitDetailAddress" value={visit.visitDetailAddress}
                               onChange={(e) => setVisit({...visit, visitDetailAddress: e.target.value})}/>
                        <div className="field">
                            <label htmlFor="requestMemo">요청 사항</label>
                            <textarea id="requestMemo" rows={3} placeholder="주차, 출입 방법, 증상 등" value={visit.requestMemo}
                                      onChange={(e) => setVisit({...visit, requestMemo: e.target.value})}/>
                        </div>
                    </fieldset>
                )}
                <p className="price align-right">결제 금액 {passId ? '이용권 사용 (0원)' : item.reservationType === 'PROGRAM'
                    ? (Number(item.price) === 0 ? '무료' : won(Number(item.price) * quantity))
                    : won(estimateReservation(item, selection))}</p>
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    <button type="button" onClick={() => navigate(-1)}>이전</button>
                    <button type="submit" className="primary" disabled={submitting}>
                        <StableLabel current={submitting ? '신청 중…' : '예약 신청'} labels={['신청 중…', '예약 신청']}/>
                    </button>
                </div>
            </form>
        </section>
    );
}
