import {useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {Discount, Order, OrderStatus, Payment, ProductSummary, Program, Reservation, Review, ReviewTargetType} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {formatDateTime} from '../../components/format';
import {ORDER_STATUS, RESERVATION_STATUS, toOptions} from '../../components/labels';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const PRODUCT_FIELDS: FieldSpec[] = [
    {name: 'name', label: '상품명', required: true},
    {name: 'price', label: '가격(원)', type: 'number', required: true},
    {name: 'availableQuantity', label: '재고', type: 'number', help: '비워두면 무제한'},
    {name: 'isEnabled', label: '판매 중', type: 'checkbox'},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image'},
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
                {label: '판매', render: (p) => <YesNo value={p.isEnabled}/>},
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

const DISCOUNT_FIELDS: FieldSpec[] = [
    {name: 'name', label: '할인명', required: true},
    {name: 'rate', label: '할인율(%)', type: 'number', required: true},
    {name: 'productId', label: '상품 ID', type: 'number', help: '비워두면 모든 상품에 적용'},
    {name: 'isUsing', label: '사용', type: 'checkbox'},
    {name: 'description', label: '설명', wide: true},
];

const loadDiscounts = (query: Query) => adminApi.discounts.search(query);

export function DiscountAdminPage() {
    return (
        <AdminCrudPage<Discount>
            title="할인"
            searchable={false}
            load={loadDiscounts}
            fields={DISCOUNT_FIELDS}
            emptyForm={{isUsing: true}}
            columns={[
                {label: '할인명', render: (d) => d.name},
                {label: '할인율', render: (d) => `${d.rate}%`},
                {label: '대상', render: (d) => (d.productId ? `상품 #${d.productId}` : '전체 상품')},
                {label: '사용', render: (d) => <YesNo value={d.isUsing}/>},
            ]}
            create={(v) => adminApi.discounts.create(v)}
            update={(d, v) => adminApi.discounts.update(d.id, v)}
            remove={(d) => adminApi.discounts.remove(d.id)}
        />
    );
}

const NEXT_ORDER_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {PAID: 'SHIPPING', SHIPPING: 'DELIVERED'};
const loadOrders = (query: Query) => adminApi.orders.search(query);

export function OrderAdminPage() {
    const [status, setStatus] = useState('');
    return (
        <AdminCrudPage<Order>
            title="주문"
            load={loadOrders}
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
                {label: '상태', render: (o) => ORDER_STATUS[o.status]},
                {label: '주문일', render: (o) => formatDateTime(o.createdAt)},
            ]}
            actions={(o, reload) => (
                <>
                    {NEXT_ORDER_STATUS[o.status] && (
                        <button type="button" onClick={() => adminApi.orders.changeStatus(o.id, NEXT_ORDER_STATUS[o.status]!).then(reload)}>
                            {ORDER_STATUS[NEXT_ORDER_STATUS[o.status]!]} 처리
                        </button>
                    )}
                    {(o.status === 'PENDING_PAYMENT' || o.status === 'PAID') && (
                        <button type="button" className="danger" onClick={() => {
                            if (window.confirm('주문을 취소할까요? 결제된 주문은 환불 처리됩니다.')) {
                                void adminApi.orders.changeStatus(o.id, 'CANCELED').then(reload);
                            }
                        }}>취소</button>
                    )}
                </>
            )}
        />
    );
}

const PROGRAM_FIELDS: FieldSpec[] = [
    {name: 'name', label: '프로그램명', required: true},
    {name: 'price', label: '1인 참가비(원)', type: 'number', required: true},
    {name: 'maximumPerson', label: '정원', type: 'number', help: '비워두면 제한 없음'},
    {name: 'isEnabled', label: '공개', type: 'checkbox'},
    {name: 'startDateTime', label: '진행 시작', type: 'datetime'},
    {name: 'endDateTime', label: '진행 종료', type: 'datetime'},
    {name: 'applicationStartDateTime', label: '신청 시작', type: 'datetime'},
    {name: 'applicationEndDateTime', label: '신청 마감', type: 'datetime'},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image'},
    {name: 'contentHtml', label: '상세 설명', type: 'html'},
];

const loadPrograms = (query: Query) => adminApi.programs.search(query);

export function ProgramAdminPage() {
    return (
        <AdminCrudPage<Program>
            title="프로그램"
            load={loadPrograms}
            fields={PROGRAM_FIELDS}
            emptyForm={{isEnabled: true, price: 0, contentHtml: ''}}
            columns={[
                {label: 'ID', render: (p) => p.id},
                {label: '이름', render: (p) => <Link to={`/programs/${p.id}`}>{p.name}</Link>},
                {label: '참가비', render: (p) => won(p.price)},
                {label: '잔여/정원', render: (p) => (p.maximumPerson ? `${p.availablePerson}/${p.maximumPerson}` : '제한 없음')},
                {label: '신청 기간', render: (p) => `${formatDateTime(p.applicationStartDateTime)} ~ ${formatDateTime(p.applicationEndDateTime)}`},
                {label: '접수', render: (p) => <YesNo value={p.isOpen}/>},
            ]}
            toForm={(p) => adminApi.programs.get(p.id).then((full) => ({...full}))}
            create={(v) => adminApi.programs.create(v)}
            update={(p, v) => adminApi.programs.update(p.id, v)}
            remove={(p) => adminApi.programs.remove(p.id)}
        />
    );
}

const loadReservations = (query: Query) => adminApi.reservations.search(query);

export function ReservationAdminPage() {
    const [status, setStatus] = useState('');
    return (
        <AdminCrudPage<Reservation>
            title="예약"
            searchable={false}
            load={loadReservations}
            extraQuery={{status}}
            filters={
                <select aria-label="예약 상태" value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">전체 상태</option>
                    {toOptions(RESERVATION_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: '예약번호', render: (r) => <code>{r.reservationNumber}</code>},
                {label: '프로그램', render: (r) => r.programName},
                {label: '인원', render: (r) => r.quantity},
                {label: '금액', render: (r) => won(r.totalPrice)},
                {label: '신청자', render: (r) => `${r.applicantName} (${r.phoneNumber})`},
                {label: '상태', render: (r) => RESERVATION_STATUS[r.status]},
            ]}
            actions={(r, reload) => r.status !== 'CANCELED' && (
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm('예약을 취소할까요?')) {
                        void adminApi.reservations.cancel(r.id).then(reload);
                    }
                }}>취소</button>
            )}
        />
    );
}

const loadPayments = (query: Query) => adminApi.payments(query);

export function PaymentAdminPage() {
    return (
        <AdminCrudPage<Payment>
            title="결제 내역"
            description="결제는 목업 PG로 처리됩니다. 실제 결제 연동 전까지 모든 요청이 즉시 승인됩니다."
            searchable={false}
            load={loadPayments}
            columns={[
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
