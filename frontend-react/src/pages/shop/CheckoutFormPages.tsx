import {type FormEvent, useEffect, useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router';
import type {ExchangeRate, ProductDetail, ProductSaleType, ReservationItem, TaxSetting} from '../../api/domain';
import {type DiscountChoice, DiscountSelector, NO_DISCOUNT} from './DiscountSelector';
import {shopApi} from '../../api/endpoints';
import {memberApi} from '../../api/members';
import {couponApi, couponBenefit, type CouponIssue, couponDiscountFor, maxUsablePoints, type PointPolicy, pointApi} from '../../api/rewards';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDate, formatDateTime} from '../../components/format';
import {billingCycle} from '../../components/labels';
import {PostcodeButton} from '../../components/PostcodeButton';
import {PriceNotice} from '../../components/PriceNotice';
import {reservationUsage} from '../../components/reservationUsage';
import {StableLabel} from '../../components/StableLabel';
import {estimateReservation} from './ReservationPicker';
import {useI18n} from '../../api/i18n';

const positive = (value: string | null, fallback = 1) => Math.max(fallback, Number(value) || fallback);

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const modes = (t: T): Record<ProductSaleType, { title: string; receiverLegend: string; totalLabel: string; submit: string }> => ({
    PURCHASE: {title: t('shop.checkoutForm.purchase.title', '주문 정보 입력'), receiverLegend: t('shop.checkoutForm.shippingInfo', '배송 정보'),
        totalLabel: t('shop.checkoutForm.purchase.totalLabel', '결제 예정 금액'), submit: t('shop.checkoutForm.purchase.submit', '주문하고 결제하기')},
    SUBSCRIPTION: {title: t('shop.checkoutForm.subscription.title', '구독 신청'), receiverLegend: t('shop.checkoutForm.shippingInfo', '배송 정보'),
        totalLabel: t('shop.checkoutForm.subscription.totalLabel', '회차별 결제 금액'), submit: t('shop.checkoutForm.subscription.submit', '구독하고 결제하기')},
    DONATION: {title: t('shop.checkoutForm.donation.title', '후원 정보 입력'), receiverLegend: t('shop.checkoutForm.donation.legend', '후원자 정보'),
        totalLabel: t('shop.checkoutForm.donation.totalLabel', '후원 금액'), submit: t('shop.checkoutForm.donation.submit', '후원하고 결제하기')},
    QUOTE: {title: t('shop.checkoutForm.quote.title', '견적 문의'), receiverLegend: t('shop.checkoutForm.quote.legend', '연락처'),
        totalLabel: '', submit: t('shop.checkoutForm.quote.submit', '견적 요청 보내기')},
    PASS: {title: t('shop.checkoutForm.pass.title', '이용권 구매'), receiverLegend: t('shop.checkoutForm.buyerInfo', '구매자 정보'),
        totalLabel: t('shop.checkoutForm.pass.totalLabel', '결제 금액'), submit: t('shop.checkoutForm.pass.submit', '구매하고 결제하기')},
    GIFT_CARD: {title: t('shop.checkoutForm.giftCard.title', '상품권 구매'), receiverLegend: t('shop.checkoutForm.buyerInfo', '구매자 정보'),
        totalLabel: t('shop.checkoutForm.pass.totalLabel', '결제 금액'), submit: t('shop.checkoutForm.pass.submit', '구매하고 결제하기')},
    PREORDER: {title: t('shop.checkoutForm.preorder.title', '예약 판매 주문'), receiverLegend: t('shop.checkoutForm.shippingInfo', '배송 정보'),
        totalLabel: t('shop.checkoutForm.purchase.totalLabel', '결제 예정 금액'), submit: t('shop.checkoutForm.preorder.submit', '예약 주문하고 결제하기')},
    FUNDING: {title: t('shop.checkoutForm.funding.title', '펀딩 참여'), receiverLegend: t('shop.checkoutForm.shippingInfo', '배송 정보'),
        totalLabel: t('shop.checkoutForm.pass.totalLabel', '결제 금액'), submit: t('shop.checkoutForm.funding.submit', '펀딩 참여하고 결제하기')},
    INQUIRY: {title: '', receiverLegend: '', totalLabel: '', submit: ''},
});

export function OrderFormPage() {
    const {t} = useI18n();
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
    const [receiver, setReceiver] = useState({receiverName: '', phoneNumber: '', postalCode: '', address: '', detailAddress: '', country: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);
    const [tax, setTax] = useState<TaxSetting | null>(null);
    const [rates, setRates] = useState<ExchangeRate[]>([]);

    useEffect(() => {
        shopApi.product(productId).then(setProduct).catch(setError);
        shopApi.taxSetting().then((setting) => setTax(setting ?? null)).catch(() => setTax(null));
        shopApi.exchangeRates().then((r) => setRates(r ?? [])).catch(() => setRates([]));
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    const option = product.options.find((o) => String(o.id) === optionId);
    const discount = product.discounts.find((d) => String(d.id) === discountId);
    const subtotal = (Number(product.price) + Number(option?.additionalPrice ?? 0)) * quantity;
    const total = product.saleType === 'DONATION' ? amount
        : discount ? Math.round(subtotal * (100 - Number(discount.rate)) / 100) : subtotal;
    const mode = modes(t)[product.saleType ?? 'PURCHASE'];
    const purchase = (product.saleType ?? 'PURCHASE') === 'PURCHASE';
    const shipsLikePurchase = product.saleType === 'PREORDER' || product.saleType === 'FUNDING';
    const needsAddress = shipsLikePurchase ? product.requiresShipping !== false
        : purchase && product.requiresShipping !== false;

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
                ...(product.saleType === 'DONATION' ? {receiverName: receiver.receiverName, phoneNumber: receiver.phoneNumber} : receiver),
                amount: product.saleType === 'DONATION' ? amount : null,
                requestMessage: product.saleType === 'QUOTE' ? requestMessage : null,
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
                <dt>{t('shop.checkoutForm.product', '상품')}</dt>
                <dd><Link to={`/products/${product.id}`}>{product.name}</Link></dd>
                {option && (<><dt>{t('shop.checkoutForm.option', '옵션')}</dt><dd>{option.name} (+{won(option.additionalPrice)})</dd></>)}
                {discount && (<><dt>{t('shop.product.discount', '할인')}</dt><dd>{discount.name} ({discount.rate}%)</dd></>)}
                {product.saleType !== 'DONATION' && (<><dt>{t('shop.checkoutForm.quantity', '수량')}</dt><dd>{t('shop.checkoutForm.quantityOf', '{count}개', {count: String(quantity)})}</dd></>)}
                {product.saleType === 'SUBSCRIPTION' && product.billingCycle && (<><dt>{t('shop.checkoutForm.billingCycle', '결제 주기')}</dt><dd>{billingCycle(t)[product.billingCycle]}</dd></>)}
                {product.preorder?.shipDate && (<><dt>{t('shop.checkoutForm.shipDate', '출고 예정일')}</dt><dd>{formatDate(product.preorder.shipDate)}</dd></>)}
                {product.funding?.deadline && (<><dt>{t('shop.checkoutForm.fundingDeadline', '펀딩 마감')}</dt><dd>{formatDateTime(product.funding.deadline)}</dd></>)}
            </dl>
            <form onSubmit={submit}>
                {product.saleType === 'QUOTE' && (
                    <div className="field">
                        <label htmlFor="requestMessage">{t('shop.checkoutForm.requestMessage', '요청 내용')}</label>
                        <textarea id="requestMessage" rows={5} maxLength={2000} value={requestMessage}
                                  placeholder={t('shop.checkoutForm.requestMessagePlaceholder', '필요한 수량, 일정, 조건 등을 적어 주세요.')}
                                  onChange={(e) => setRequestMessage(e.target.value)}/>
                    </div>
                )}
                <fieldset>
                    <legend>{mode.receiverLegend}</legend>
                    <Field label={product.saleType === 'DONATION' ? t('shop.checkoutForm.donorName', '후원자 이름')
                               : product.saleType === 'QUOTE' ? t('shop.checkoutForm.contactName', '담당자 이름') : t('shop.checkoutForm.receiver', '수령인')}
                           name="receiverName" required value={receiver.receiverName}
                           onChange={(e) => setReceiver({...receiver, receiverName: e.target.value})}/>
                    <Field label={t('admin.order.field.phoneNumber', '연락처')} name="phoneNumber" type="tel" required value={receiver.phoneNumber}
                           onChange={(e) => setReceiver({...receiver, phoneNumber: e.target.value})}/>
                    {product.saleType !== 'DONATION' && (
                        <>
                            <div className="row align-end">
                                <Field label={t('admin.order.field.postalCode', '우편번호')} name="postalCode" value={receiver.postalCode}
                                       onChange={(e) => setReceiver({...receiver, postalCode: e.target.value})}/>
                                <PostcodeButton onFound={(result) => setReceiver({...receiver, postalCode: result.zonecode,
                                    address: result.roadAddress || result.jibunAddress})}/>
                            </div>
                            <Field label={t('admin.order.field.address', '주소')} name="address" required={needsAddress} value={receiver.address}
                                   onChange={(e) => setReceiver({...receiver, address: e.target.value})}/>
                            <Field label={t('admin.order.field.detailAddress', '상세 주소')} name="detailAddress" value={receiver.detailAddress}
                                   onChange={(e) => setReceiver({...receiver, detailAddress: e.target.value})}/>
                            <Field label={t('shop.checkoutForm.countryCode', '해외 배송 국가 코드 (국내는 비움, 예: US, JP)')} name="country" value={receiver.country}
                                   onChange={(e) => setReceiver({...receiver, country: e.target.value.toUpperCase()})}/>
                        </>
                    )}
                </fieldset>
                {product.saleType === 'QUOTE'
                    ? <p className="muted">{t('shop.checkoutForm.quoteNotice', '요청을 보내면 관리자가 견적 금액을 회신합니다. 마이페이지 주문 내역에서 수락하고 결제할 수 있습니다.')}</p>
                    : (
                        purchase ? (
                            <DiscountSelector postalCode={needsAddress ? receiver.postalCode : null} value={rewards} onChange={setRewards}
                                              lines={[{productId, productOptionId: option ? option.id : null, quantity,
                                                  discountId: discount ? discount.id : null}]}/>
                        ) : (
                            <div className="align-right">
                                <p className="price">{mode.totalLabel} {won(total)}</p>
                                <PriceNotice amount={total} tax={tax} rates={rates}/>
                            </div>
                        )
                    )}
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    <button type="button" onClick={() => navigate(-1)}>{t('shop.checkoutForm.back', '이전')}</button>
                    <button type="submit" className="primary" disabled={submitting}>
                        <StableLabel current={submitting ? t('shop.checkoutForm.sending', '보내는 중…') : mode.submit} labels={[t('shop.checkoutForm.sending', '보내는 중…'), mode.submit]}/>
                    </button>
                </div>
            </form>
        </section>
    );
}

export function ReservationFormPage() {
    const {t} = useI18n();
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
    const [tax, setTax] = useState<TaxSetting | null>(null);
    const [rates, setRates] = useState<ExchangeRate[]>([]);
    const [coupons, setCoupons] = useState<CouponIssue[]>([]);
    const [pointBalance, setPointBalance] = useState(0);
    const [pointPolicy, setPointPolicy] = useState<PointPolicy | null>(null);
    const [couponIssueId, setCouponIssueId] = useState<number | null>(null);
    const [pointsText, setPointsText] = useState('');

    useEffect(() => {
        shopApi.reservationItem(reservationItemId).then(setItem).catch(setError);
        shopApi.taxSetting().then((setting) => setTax(setting ?? null)).catch(() => setTax(null));
        shopApi.exchangeRates().then((r) => setRates(r ?? [])).catch(() => setRates([]));
        memberApi.me().then((me) => setForm((current) => ({
            applicantName: current.applicantName || me.name || '',
            phoneNumber: current.phoneNumber || me.mobilePhoneNumber || me.telephoneNumber || '',
        }))).catch(() => undefined);
        couponApi.mine().then((list) => setCoupons(list ?? [])).catch(() => setCoupons([]));
        pointApi.mine().then((summary) => setPointBalance(summary?.balance ?? 0)).catch(() => setPointBalance(0));
        pointApi.policy().then((p) => setPointPolicy(p ?? null)).catch(() => setPointPolicy(null));
    }, [reservationItemId]);

    if (error && !item) {
        return <ErrorBanner error={error}/>;
    }
    if (!item) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }

    const subtotal = passId ? 0 : item.reservationType === 'PROGRAM' ? Number(item.price) * quantity
        : estimateReservation(item, selection);
    const usableCoupons = coupons.filter((c) => c.status === 'AVAILABLE' && c.productId == null && c.discountType != null);
    const chosenCoupon = usableCoupons.find((c) => c.id === couponIssueId) ?? null;
    const couponDiscount = chosenCoupon
        ? couponDiscountFor({discountType: chosenCoupon.discountType!, discountValue: chosenCoupon.discountValue!,
            maxDiscountAmount: chosenCoupon.maxDiscountAmount, minOrderAmount: chosenCoupon.minOrderAmount}, subtotal)
        : 0;
    const afterCoupon = subtotal - couponDiscount;
    const maxPoints = pointPolicy ? maxUsablePoints(pointPolicy, pointBalance, afterCoupon) : 0;
    const usePoints = Math.min(Number(pointsText) || 0, maxPoints);
    const total = passId ? 0 : Math.max(0, afterCoupon - usePoints);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const reservation = await shopApi.reserve({reservationItemId, ...selection, quantity, sessionId, passId, partySize, ...form,
                ...(item.reservationType === 'VISIT' ? visit : {}),
                ...(passId ? {} : {couponIssueId, usePoints})});
            navigate(reservation.status === 'PENDING_PAYMENT' ? `/checkout/RESERVATION/${reservation.id}` : '/mypage/reservations');
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>{t('shop.reservationForm.title', '예약 정보 입력')}</h1>
            <dl className="meta">
                <dt>{t('admin.reservation.column.item', '예약 상품')}</dt>
                <dd><Link to={`/reservation-items/${item.id}`}>{item.name}</Link></dd>
                {item.reservationType === 'PROGRAM' ? (
                    <>
                        <dt>{t('shop.reservationItem.schedule', '진행 일정')}</dt>
                        <dd>{(() => {
                            const session = item.sessions.find((s) => s.id === sessionId);
                            return session
                                ? `${formatDateTime(session.startAt)}${session.endAt ? ` ~ ${formatDateTime(session.endAt)}` : ''}${session.name ? ` · ${session.name}` : ''}`
                                : `${formatDateTime(item.startDateTime)} ~ ${formatDateTime(item.endDateTime)}`;
                        })()}</dd>
                        <dt>{t('shop.reservationItem.partySize', '인원')}</dt>
                        <dd>{t('shop.reservationForm.peopleCount', '{count}명', {count: String(quantity)})}</dd>
                    </>
                ) : (
                    <>
                        <dt>{t('admin.reservation.column.usage', '이용')}</dt>
                        <dd>{reservationUsage({reservationType: item.reservationType, ...selection, partySize, partyBreakdown: partyCounts,
                            cells: selection.cells.map((key) => item.layout?.cells.find((c) => c.key === key)?.label ?? key)}, t)}</dd>
                        {item.reservationType === 'ITEM_RENTAL' && (
                            <>
                                <dt>{t('shop.checkoutForm.quantity', '수량')}</dt>
                                <dd>{t('shop.checkoutForm.quantityOf', '{count}개', {count: String(quantity)})}
                                    {Number(item.depositAmount ?? 0) > 0 && t('shop.reservationForm.depositIncluded', ' · 보증금 {amount} 포함', {amount: won(Number(item.depositAmount) * quantity)})}</dd>
                            </>
                        )}
                    </>
                )}
            </dl>
            <form onSubmit={submit}>
                <fieldset>
                    <legend>{t('shop.reservationForm.applicantInfo', '신청자 정보')}</legend>
                    <Field label={t('admin.reservation.field.applicantName', '신청자')} name="applicantName" required value={form.applicantName}
                           onChange={(e) => setForm({...form, applicantName: e.target.value})}/>
                    <Field label={t('admin.order.field.phoneNumber', '연락처')} name="phoneNumber" type="tel" required value={form.phoneNumber}
                           onChange={(e) => setForm({...form, phoneNumber: e.target.value})}/>
                </fieldset>
                {item.reservationType === 'VISIT' && (
                    <fieldset>
                        <legend>{t('shop.reservationForm.visitInfo', '방문 정보')}</legend>
                        <div className="row align-end">
                            <Field label={t('admin.order.field.postalCode', '우편번호')} name="visitPostalCode" value={visit.visitPostalCode}
                                   onChange={(e) => setVisit({...visit, visitPostalCode: e.target.value})}/>
                            <PostcodeButton onFound={(result) => setVisit({...visit, visitPostalCode: result.zonecode,
                                visitAddress: result.roadAddress || result.jibunAddress})}/>
                        </div>
                        <Field label={t('shop.reservationForm.visitAddress', '방문 주소')} name="visitAddress" required value={visit.visitAddress}
                               onChange={(e) => setVisit({...visit, visitAddress: e.target.value})}/>
                        <Field label={t('admin.order.field.detailAddress', '상세 주소')} name="visitDetailAddress" value={visit.visitDetailAddress}
                               onChange={(e) => setVisit({...visit, visitDetailAddress: e.target.value})}/>
                        <div className="field">
                            <label htmlFor="requestMemo">{t('shop.reservationForm.requestMemo', '요청 사항')}</label>
                            <textarea id="requestMemo" rows={3} placeholder={t('shop.reservationForm.requestMemoPlaceholder', '주차, 출입 방법, 증상 등')} value={visit.requestMemo}
                                      onChange={(e) => setVisit({...visit, requestMemo: e.target.value})}/>
                        </div>
                    </fieldset>
                )}
                {!passId && (usableCoupons.length > 0 || pointBalance > 0) && (
                    <fieldset>
                        <legend>{t('shop.reservationForm.couponsAndPoints', '쿠폰·포인트')}</legend>
                        {usableCoupons.length > 0 && (
                            <div className="field">
                                <label htmlFor="couponIssueId">{t('admin.nav.coupons', '쿠폰')}</label>
                                <select id="couponIssueId" value={couponIssueId ?? ''}
                                        onChange={(e) => setCouponIssueId(e.target.value ? Number(e.target.value) : null)}>
                                    <option value="">{t('shop.reservationForm.noCoupon', '쿠폰 사용 안 함')}</option>
                                    {usableCoupons.map((c) => (
                                        <option key={c.id} value={c.id}>{c.couponName} ({couponBenefit(c, t)})</option>
                                    ))}
                                </select>
                            </div>
                        )}
                        {pointBalance > 0 && (
                            <div className="field">
                                <label htmlFor="usePoints">{t('shop.reservationForm.pointsBalance', '포인트 (보유 {balance}P)', {balance: pointBalance.toLocaleString('ko-KR')})}</label>
                                <div className="toolbar">
                                    <input id="usePoints" type="number" min={0} max={maxPoints} value={pointsText}
                                           placeholder="0" onChange={(e) => setPointsText(e.target.value)}/>
                                    <button type="button" onClick={() => setPointsText(String(maxPoints))}>{t('shop.reservationForm.useMax', '최대 사용')}</button>
                                </div>
                                <p className="muted small">{t('shop.reservationForm.maxPointsNotice', '최대 {max}P 쓸 수 있습니다.', {max: maxPoints.toLocaleString('ko-KR')})}</p>
                            </div>
                        )}
                    </fieldset>
                )}
                <div className="align-right">
                    <p className="price">{t('shop.reservationItem.paymentAmount', '결제 금액 {amount}', {amount: passId ? t('shop.reservationItem.usePass', '이용권 사용 (0원)') : item.reservationType === 'PROGRAM'
                        ? (Number(item.price) === 0 ? t('shop.common.free', '무료') : won(total))
                        : won(total)})}</p>
                    {!passId && <PriceNotice amount={total} tax={tax} rates={rates}/>}
                </div>
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    <button type="button" onClick={() => navigate(-1)}>{t('shop.checkoutForm.back', '이전')}</button>
                    <button type="submit" className="primary" disabled={submitting}>
                        <StableLabel current={submitting ? t('shop.reservationForm.applying', '신청 중…') : t('shop.reservationForm.submit', '예약 신청')}
                                     labels={[t('shop.reservationForm.applying', '신청 중…'), t('shop.reservationForm.submit', '예약 신청')]}/>
                    </button>
                </div>
            </form>
        </section>
    );
}
