import {type FormEvent, useEffect, useState} from 'react';
import {Link, useNavigate, useSearchParams} from 'react-router';
import type {ProductDetail, Program} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {memberApi} from '../../api/members';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDateTime} from '../../components/format';
import {StableLabel} from '../../components/StableLabel';

const positive = (value: string | null, fallback = 1) => Math.max(fallback, Number(value) || fallback);

export function OrderFormPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const productId = Number(params.get('productId'));
    const optionId = params.get('optionId') ?? '';
    const discountId = params.get('discountId') ?? '';
    const quantity = positive(params.get('quantity'));
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [receiver, setReceiver] = useState({receiverName: '', phoneNumber: '', postalCode: '', address: '', detailAddress: ''});
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
    const total = discount ? Math.round(subtotal * (100 - Number(discount.rate)) / 100) : subtotal;

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
                ...receiver,
            });
            navigate(`/checkout/ORDER/${created.id}`);
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>주문 정보 입력</h1>
            <dl className="meta">
                <dt>상품</dt>
                <dd><Link to={`/products/${product.id}`}>{product.name}</Link></dd>
                {option && (<><dt>옵션</dt><dd>{option.name} (+{won(option.additionalPrice)})</dd></>)}
                {discount && (<><dt>할인</dt><dd>{discount.name} ({discount.rate}%)</dd></>)}
                <dt>수량</dt>
                <dd>{quantity}개</dd>
            </dl>
            <form onSubmit={submit}>
                <fieldset>
                    <legend>배송 정보</legend>
                    <Field label="수령인" name="receiverName" required value={receiver.receiverName}
                           onChange={(e) => setReceiver({...receiver, receiverName: e.target.value})}/>
                    <Field label="연락처" name="phoneNumber" type="tel" required value={receiver.phoneNumber}
                           onChange={(e) => setReceiver({...receiver, phoneNumber: e.target.value})}/>
                    <Field label="우편번호" name="postalCode" value={receiver.postalCode}
                           onChange={(e) => setReceiver({...receiver, postalCode: e.target.value})}/>
                    <Field label="주소" name="address" value={receiver.address}
                           onChange={(e) => setReceiver({...receiver, address: e.target.value})}/>
                    <Field label="상세 주소" name="detailAddress" value={receiver.detailAddress}
                           onChange={(e) => setReceiver({...receiver, detailAddress: e.target.value})}/>
                </fieldset>
                <p className="price align-right">결제 예정 금액 {won(total)}</p>
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    <button type="button" onClick={() => navigate(-1)}>이전</button>
                    <button type="submit" className="primary" disabled={submitting}>
                        <StableLabel current={submitting ? '주문 중…' : '주문하고 결제하기'} labels={['주문 중…', '주문하고 결제하기']}/>
                    </button>
                </div>
            </form>
        </section>
    );
}

export function ReservationFormPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();
    const programId = Number(params.get('programId'));
    const quantity = positive(params.get('quantity'));
    const [program, setProgram] = useState<Program | null>(null);
    const [form, setForm] = useState({applicantName: '', phoneNumber: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        shopApi.program(programId).then(setProgram).catch(setError);
        memberApi.me().then((me) => setForm((current) => ({
            applicantName: current.applicantName || me.name || '',
            phoneNumber: current.phoneNumber || me.mobilePhoneNumber || me.telephoneNumber || '',
        }))).catch(() => undefined);
    }, [programId]);

    if (error && !program) {
        return <ErrorBanner error={error}/>;
    }
    if (!program) {
        return <p className="muted">불러오는 중…</p>;
    }

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const reservation = await shopApi.reserve({programId, quantity, ...form});
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
                <dt>프로그램</dt>
                <dd><Link to={`/programs/${program.id}`}>{program.name}</Link></dd>
                <dt>진행 일정</dt>
                <dd>{formatDateTime(program.startDateTime)} ~ {formatDateTime(program.endDateTime)}</dd>
                <dt>인원</dt>
                <dd>{quantity}명</dd>
            </dl>
            <form onSubmit={submit}>
                <fieldset>
                    <legend>신청자 정보</legend>
                    <Field label="신청자" name="applicantName" required value={form.applicantName}
                           onChange={(e) => setForm({...form, applicantName: e.target.value})}/>
                    <Field label="연락처" name="phoneNumber" type="tel" required value={form.phoneNumber}
                           onChange={(e) => setForm({...form, phoneNumber: e.target.value})}/>
                </fieldset>
                <p className="price align-right">결제 금액 {Number(program.price) === 0 ? '무료' : won(Number(program.price) * quantity)}</p>
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
