import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {Discount, Order, OrderStatus, Payment, ProductSummary, Program, Reservation, Review, ReviewTargetType} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {statusApi, useStatusLabels} from '../../api/statuses';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {formatDateTime} from '../../components/format';
import {reservationUsage} from '../../components/reservationUsage';
import {RegexBindingSelect} from '../../components/RegexBindingSelect';
import {ORDER_STATUS, RESERVATION_STATUS, toOptions} from '../../components/labels';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const PRODUCT_FIELDS: FieldSpec[] = [
    {name: 'name', label: '상품명', required: true, wide: true},
    {name: 'price', label: '가격(원)', type: 'number', required: true, wide: true},
    {name: 'availableQuantity', label: '재고', type: 'number', help: '비워두면 무제한', wide: true},
    {name: 'isEnabled', label: '판매 중', type: 'checkbox', wide: true},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image', wide: true},
    {name: 'optionsText', label: '옵션', type: 'textarea',
        help: '한 줄에 하나씩 "옵션명:추가금액" 형식으로 입력합니다. 예) 대형:2000'},
    {name: 'contentHtml', label: '상세 설명', type: 'html'},
];

const toProductBody = (values: FormValues) => ({
    ...values,
    options: String(values.optionsText ?? '').split('\n').map((line) => line.trim()).filter(Boolean).map((line) => {
        const [name, price] = line.split(':');
        return {name: name.trim(), additionalPrice: Number(price ?? 0) || 0};
    }),
});

const loadProducts = (query: Query) => adminApi.products.search(query);

export function ProductAdminPage() {
    return (
        <AdminCrudPage<ProductSummary>
            title="상품"
            load={loadProducts}
            fields={PRODUCT_FIELDS}
            emptyForm={{isEnabled: true, price: 0, contentHtml: '', optionsText: ''}}
            columns={[
                {label: '이미지', render: (p) => p.imageUrl ? <img className="thumb" src={p.imageUrl} alt={p.name}/> : '-'},
                {label: 'ID', render: (p) => p.id},
                {label: '상품명', render: (p) => <Link to={`/products/${p.id}`}>{p.name}</Link>},
                {label: '가격', render: (p) => won(p.price)},
                {label: '재고', render: (p) => p.availableQuantity ?? '무제한'},
                {label: '판매 중', render: (p) => <YesNo value={p.isEnabled}/>},
            ]}
            toForm={(p) => adminApi.products.get(p.id).then((full) => ({
                ...full,
                optionsText: full.options.map((o) => `${o.name}:${o.additionalPrice}`).join('\n'),
            }))}
            create={(v) => adminApi.products.create(toProductBody(v))}
            update={(p, v) => adminApi.products.update(p.id, toProductBody(v))}
            remove={(p) => adminApi.products.remove(p.id)}
        />
    );
}

const discountFields = (products: { value: string; label: string }[]): FieldSpec[] => [
    {name: 'name', label: '할인명', required: true, wide: true},
    {name: 'rate', label: '할인율(%)', type: 'number', required: true, wide: true},
    {name: 'productId', label: '대상 상품', type: 'select', options: products, wide: true,
        help: '선택 안 함이면 모든 상품에 적용됩니다.'},
    {name: 'isUsing', label: '사용', type: 'checkbox', wide: true},
    {name: 'description', label: '설명', wide: true},
];

const toDiscountBody = (values: FormValues) => ({...values, productId: values.productId ? Number(values.productId) : null});

const loadDiscounts = (query: Query) => adminApi.discounts.search(query);

export function DiscountAdminPage() {
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        adminApi.products.search({size: 200})
            .then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    const productName = (id: number) => products.find((p) => p.value === String(id))?.label ?? `상품 #${id}`;
    return (
        <AdminCrudPage<Discount>
            title="할인"
            searchable={false}
            load={loadDiscounts}
            fields={discountFields(products)}
            emptyForm={{isUsing: true}}
            columns={[
                {label: '할인명', render: (d) => d.name},
                {label: '할인율', render: (d) => `${d.rate}%`},
                {label: '대상', render: (d) => (d.productId ? productName(d.productId) : '전체 상품')},
                {label: '사용 여부', render: (d) => <YesNo value={d.isUsing}/>},
            ]}
            create={(v) => adminApi.discounts.create(toDiscountBody(v))}
            update={(d, v) => adminApi.discounts.update(d.id, toDiscountBody(v))}
            remove={(d) => adminApi.discounts.remove(d.id)}
        />
    );
}

const NEXT_ORDER_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {PAID: 'SHIPPING', SHIPPING: 'DELIVERED'};
const loadOrders = (query: Query) => adminApi.orders.search(query);

const PLEASE_CHOOSE = {value: '', label: '선택하세요'};
const PAID_FIELD: FieldSpec = {name: 'paid', label: '결제 완료로 등록', type: 'checkbox', wide: true,
    help: '현장·계좌이체 등으로 이미 받은 경우 체크합니다. 체크하지 않으면 결제 대기로 등록되고, 목록에서 결제 완료 처리할 수 있습니다.'};

async function loadProductChoices() {
    const products = await adminApi.products.search({size: 100});
    const details = await Promise.all(products.content.map((p) => adminApi.products.get(p.id)));
    return details.flatMap((p) => [
        {value: String(p.id), label: `${p.name} · ${won(p.price)}`},
        ...p.options.map((o) => ({value: `${p.id}:${o.id}`, label: `${p.name} (${o.name}) · ${won(p.price + o.additionalPrice)}`})),
    ]);
}

const orderFields = (productChoices: { value: string; label: string }[]): FieldSpec[] => [
    {name: 'product', label: '상품', type: 'select', required: true, options: [PLEASE_CHOOSE, ...productChoices], wide: true},
    {name: 'quantity', label: '수량', type: 'number', required: true, wide: true},
    {name: 'receiverName', label: '수령인', required: true, wide: true},
    {name: 'phoneNumber', label: '연락처', required: true, wide: true},
    {name: 'postalCode', label: '우편번호', wide: true},
    {name: 'address', label: '주소', help: '비워 두면 배송 없는 주문(현장 수령)으로 등록됩니다.', wide: true},
    {name: 'detailAddress', label: '상세 주소', wide: true},
    PAID_FIELD,
];

function createOrder({product, paid, ...rest}: FormValues) {
    const [productId, productOptionId] = String(product).split(':').map(Number);
    return adminApi.orders.create({...rest, productId, productOptionId: productOptionId ?? null}, Boolean(paid)).then(() => undefined);
}

export function OrderAdminPage() {
    const {statuses, label: statusLabel, reload: reloadStatuses} = useStatusLabels('ORDER', ORDER_STATUS);
    const [status, setStatus] = useState('');
    const [productChoices, setProductChoices] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        loadProductChoices().then(setProductChoices).catch(() => setProductChoices([]));
    }, []);
    return (
        <AdminCrudPage<Order>
            title="주문"
            description={<>
                <p>관리자가 전화·방문 주문을 직접 등록할 수 있습니다. 관리자 명의로 저장되며 회원 마이페이지에는 보이지 않습니다.</p>
                <RegexBindingSelect target="ORDER_NUMBER" label="주문번호 정규식"/>
                <StatusManager domain="ORDER" statuses={statuses} onChange={reloadStatuses}/>
            </>}
            load={loadOrders}
            fields={orderFields(productChoices)}
            emptyForm={{product: '', quantity: 1, paid: false}}
            create={createOrder}
            extraQuery={{status}}
            filters={
                <select aria-label="주문 상태" value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">전체 상태</option>
                    {toOptions(ORDER_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: '주문번호', render: (o) => <code>{o.orderNumber}</code>},
                {label: '상품', render: (o) => `${o.productName} × ${o.quantity}`},
                {label: '금액', render: (o) => won(o.totalPrice)},
                {label: '수령인', render: (o) => `${o.receiverName} (${o.phoneNumber})`},
                {label: '상태', render: (o, reload) => (
                    <>
                        {statusLabel(o.status, o.customStatus)}
                        <CustomStatusSelect statuses={statuses} status={o.status} customStatus={o.customStatus}
                                            onApply={(code) => void statusApi.applyToOrder(o.id, code).then(reload)}/>
                    </>
                )},
                {label: '주문일', render: (o) => formatDateTime(o.createdAt)},
                {label: '상태 처리', className: 'button-cell', render: (o, reload) => (
                    <>
                        {o.status === 'PENDING_PAYMENT' && (
                            <button type="button" onClick={() => {
                                if (window.confirm('결제를 받은 것으로 처리할까요? 결제 내역에는 기록되지 않습니다.')) {
                                    void adminApi.orders.markPaid(o.id).then(reload);
                                }
                            }}>결제 완료 처리</button>
                        )}
                        {NEXT_ORDER_STATUS[o.status] && (
                            <button type="button" onClick={() => adminApi.orders.changeStatus(o.id, NEXT_ORDER_STATUS[o.status]!).then(reload)}>
                                {statusLabel(NEXT_ORDER_STATUS[o.status]!)} 처리
                            </button>
                        )}
                    </>
                )},
                {label: '취소', className: 'button-cell', render: (o, reload) => (o.status === 'PENDING_PAYMENT' || o.status === 'PAID') && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm('주문을 취소할까요? 결제된 주문은 환불 처리됩니다.')) {
                            void adminApi.orders.changeStatus(o.id, 'CANCELED').then(reload);
                        }
                    }}>취소</button>
                )},
            ]}
        />
    );
}

const isType = (...types: ReservationType[]) => (values: FormValues) => types.includes(values.reservationType as ReservationType);

const PROGRAM_FIELDS: FieldSpec[] = [
    {name: 'reservationType', label: '예약 유형', type: 'select', required: true, wide: true,
        options: RESERVATION_TYPE_GROUPS.flatMap((group) => group.types.map((type) => ({value: type, label: `${group.label} · ${RESERVATION_TYPE[type]}`}))),
        help: '시간 예약은 회차나 시간대를, 공간 예약은 배치표의 좌석·객실·공간을 골라 예약합니다.'},
    {name: 'name', label: '이름', required: true, wide: true},
    {name: 'price', label: '기본 가격(원)', type: 'number', required: true, wide: true,
        help: '프로그램은 1인, 상담은 1회, 대관은 칸·예약 단위당, 숙박은 칸·1박당, 공연은 좌석당 가격입니다. 배치표에서 칸별 가격을 따로 줄 수 있습니다.'},
    {name: 'maximumPerson', label: '정원', type: 'number', wide: true, visibleWhen: isType('PROGRAM', 'CONSULTATION'),
        help: '프로그램은 전체 인원, 상담은 시간대별 인원입니다(비우면 프로그램은 제한 없음, 상담은 1명).'},
    {name: 'isEnabled', label: '공개', type: 'checkbox', wide: true},
    {name: 'startDateTime', label: '시작 일시', type: 'datetime', wide: true,
        help: '프로그램은 진행 시작, 상담·대관·숙박은 예약을 받는 기간의 시작, 공연은 공연 일시입니다.'},
    {name: 'endDateTime', label: '종료 일시', type: 'datetime', wide: true},
    {name: 'slotMinutes', label: '예약 단위(분)', type: 'number', wide: true, visibleWhen: isType('CONSULTATION', 'RENTAL'),
        help: '상담 1회 길이, 대관 최소 단위입니다.'},
    {name: 'openTime', label: '운영 시작 시각', type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'RENTAL')},
    {name: 'closeTime', label: '운영 종료 시각', type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'RENTAL')},
    {name: 'layout', label: '배치표', type: 'layout', visibleWhen: isType('RENTAL', 'LODGING', 'EVENT'),
        help: '좌석·객실·공간을 칠해 두면 사용자가 그 칸을 골라 예약합니다.'},
    {name: 'applicationStartDateTime', label: '신청 시작', type: 'datetime', wide: true},
    {name: 'applicationEndDateTime', label: '신청 마감', type: 'datetime', wide: true},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image', wide: true},
    {name: 'contentHtml', label: '상세 설명', type: 'html'},
];

const loadPrograms = (query: Query) => adminApi.programs.search(query);

export function ProgramAdminPage() {
    return (
        <AdminCrudPage<Program>
            title="예약"
            description="시간 예약(프로그램·상담)과 공간 예약(대관·숙박·공연)을 등록합니다. 예약 내역은 하위 메뉴에서 확인합니다."
            load={loadPrograms}
            fields={fields}
            emptyForm={{reservationType: 'PROGRAM', isEnabled: true, price: 0, contentHtml: '', layout: EMPTY_LAYOUT}}
            columns={[
                {label: 'ID', render: (p) => p.id},
                {label: '유형', render: (p) => <span className="badge">{RESERVATION_TYPE[p.reservationType]}</span>},
                {label: '이름', render: (p) => <Link to={`/programs/${p.id}`}>{p.name}</Link>},
                {label: '기본 가격', render: (p) => won(p.price)},
                {label: '잔여/정원', render: (p) => (p.reservationType !== 'PROGRAM' ? '-' : p.maximumPerson ? `${p.availablePerson}/${p.maximumPerson}` : '제한 없음')},
                {label: '신청 기간', render: (p) => `${formatDateTime(p.applicationStartDateTime)} ~ ${formatDateTime(p.applicationEndDateTime)}`},
                {label: '접수 중', render: (p) => <YesNo value={p.isOpen}/>},
            ]}
            toForm={(p) => adminApi.programs.get(p.id).then((full) => ({...full, layout: full.layout ?? EMPTY_LAYOUT}))}
            create={(v) => adminApi.programs.create(v)}
            update={(p, v) => adminApi.programs.update(p.id, v)}
            remove={(p) => adminApi.programs.remove(p.id)}
        />
    );
}

const loadReservations = (query: Query) => adminApi.reservations.search(query);

const reservationFields = (programChoices: { value: string; label: string }[]): FieldSpec[] => [
    {name: 'programId', label: '프로그램', type: 'select', required: true, options: [PLEASE_CHOOSE, ...programChoices],
        help: '신청 기간이 지났거나 비공개인 프로그램도 예약할 수 있습니다. 정원은 넘길 수 없습니다.', wide: true},
    {name: 'quantity', label: '인원', type: 'number', required: true, wide: true},
    {name: 'applicantName', label: '신청자', required: true, wide: true},
    {name: 'phoneNumber', label: '연락처', required: true, wide: true},
    {...PAID_FIELD, help: '무료 프로그램은 체크하지 않아도 바로 확정됩니다. 체크하지 않은 유료 예약은 결제 대기로 등록되고, 목록에서 확정 처리할 수 있습니다.'},
];

function createReservation({programId, paid, ...rest}: FormValues) {
    return adminApi.reservations.create({...rest, programId: Number(programId)}, Boolean(paid)).then(() => undefined);
}

export function ReservationAdminPage() {
    const {statuses, label: statusLabel, reload: reloadStatuses} = useStatusLabels('RESERVATION', RESERVATION_STATUS);
    const [status, setStatus] = useState('');
    const [programChoices, setProgramChoices] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        adminApi.programs.search({size: 100})
            .then((page) => setProgramChoices(page.content.map((p) => ({
                value: String(p.id),
                label: `${p.name} · ${won(p.price)}${p.maximumPerson ? ` · 잔여 ${p.availablePerson}/${p.maximumPerson}` : ''}`,
            }))))
            .catch(() => setProgramChoices([]));
    }, []);
    return (
        <AdminCrudPage<Reservation>
            title="예약"
            description={<>
                <p>관리자가 전화·현장 예약을 직접 등록할 수 있습니다. 관리자 명의로 저장되며 회원 마이페이지에는 보이지 않습니다. 예약번호나 프로그램명으로 검색할 수 있습니다.</p>
                <RegexBindingSelect target="RESERVATION_NUMBER" label="예약번호 정규식"/>
                <StatusManager domain="RESERVATION" statuses={statuses} onChange={reloadStatuses}/>
            </>}
            load={loadReservations}
            fields={reservationFields(programChoices)}
            emptyForm={{programId: '', quantity: 1, paid: false}}
            create={createReservation}
            extraQuery={{status}}
            filters={
                <select aria-label="예약 상태" value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">전체 상태</option>
                    {toOptions(RESERVATION_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: '예약번호', render: (r) => <code>{r.reservationNumber}</code>},
                {label: '예약 항목', render: (r) => <>{r.programName} <span className="badge">{RESERVATION_TYPE[r.reservationType]}</span></>},
                {label: '이용', render: (r) => reservationUsage(r)},
                {label: '수량', render: (r) => r.quantity},
                {label: '금액', render: (r) => won(r.totalPrice)},
                {label: '신청자', render: (r) => `${r.applicantName} (${r.phoneNumber})`},
                {label: '상태', render: (r, reload) => (
                    <>
                        {statusLabel(r.status, r.customStatus)}
                        <CustomStatusSelect statuses={statuses} status={r.status} customStatus={r.customStatus}
                                            onApply={(code) => void statusApi.applyToReservation(r.id, code).then(reload)}/>
                    </>
                )},
                {label: '상태 처리', className: 'button-cell', render: (r, reload) => r.status === 'PENDING_PAYMENT' && (
                    <button type="button" onClick={() => {
                        if (window.confirm('결제를 받은 것으로 보고 예약을 확정할까요? 결제 내역에는 기록되지 않습니다.')) {
                            void adminApi.reservations.confirm(r.id).then(reload);
                        }
                    }}>확정 처리</button>
                )},
                {label: '취소', className: 'button-cell', render: (r, reload) => r.status !== 'CANCELED' && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm('예약을 취소할까요?')) {
                            void adminApi.reservations.cancel(r.id).then(reload);
                        }
                    }}>취소</button>
                )},
            ]}
        />
    );
}

const loadPayments = (query: Query) => adminApi.payments(query);

export function PaymentAdminPage() {
    return (
        <AdminCrudPage<Payment>
            title="결제 내역"
            description={<>
                <p>결제는 목업 PG로 처리됩니다. 실제 결제 연동 전까지 모든 요청이 즉시 승인됩니다. 결제번호를 그대로 넣어 검색하면 그 결제만 찾습니다.</p>
                <RegexBindingSelect target="PAYMENT_NUMBER" label="결제번호 정규식"/>
            </>}
            load={loadPayments}
            columns={[
                {label: '결제번호', render: (p) => <code>{p.paymentNumber ?? '-'}</code>},
                {label: '거래 ID', render: (p) => <code className="small">{p.transactionId}</code>},
                {label: '대상', render: (p) => `${p.targetType === 'ORDER' ? '주문' : '예약'} #${p.targetId}`},
                {label: '금액', render: (p) => won(p.amount)},
                {label: '수단', render: (p) => p.method},
                {label: '상태', render: (p) => (p.status === 'APPROVED' ? '승인' : '취소')},
                {label: '승인 일시', render: (p) => formatDateTime(p.approvedAt)},
            ]}
        />
    );
}

const loadReviews = (query: Query) => adminApi.reviews(query);

const REVIEW_TABS: { value: ReviewTargetType; label: string }[] = [
    {value: 'PRODUCT', label: '상품 리뷰'},
    {value: 'PROGRAM', label: '프로그램 리뷰'},
];

export function ReviewAdminPage() {
    const [targetType, setTargetType] = useState<ReviewTargetType>('PRODUCT');
    const isProduct = targetType === 'PRODUCT';
    return (
        <>
            <div className="tabs" role="tablist" aria-label="리뷰 종류">
                {REVIEW_TABS.map((tab) => (
                    <button key={tab.value} type="button" role="tab" aria-selected={targetType === tab.value}
                            className={targetType === tab.value ? 'tab active' : 'tab'}
                            onClick={() => setTargetType(tab.value)}>
                        {tab.label}
                    </button>
                ))}
            </div>
            <AdminCrudPage<Review>
                key={targetType}
                title={isProduct ? '상품 리뷰' : '프로그램 리뷰'}
                searchable={false}
                load={loadReviews}
                extraQuery={{targetType}}
                columns={[
                    isProduct
                        ? {label: '상품', render: (r) => <Link to={`/products/${r.productId}`}>#{r.productId}</Link>}
                        : {label: '프로그램', render: (r) => <Link to={`/programs/${r.programId}`}>#{r.programId}</Link>},
                    {label: '평점', render: (r) => '★'.repeat(r.rating)},
                    {label: '제목', render: (r) => r.title},
                    {label: '작성자', render: (r) => r.authorName},
                    {label: '작성일', render: (r) => formatDateTime(r.createdAt)},
                ]}
                remove={(r) => shopApi.removeReview(r.id)}
            />
        </>
    );
}
