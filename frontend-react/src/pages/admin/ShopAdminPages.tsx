import {fileApi} from '../../api/files';
import {attributesToForm, productCategoryFields, splitCategoryValues, useCategories} from '../../api/categories';
import type {FileInfo} from '../../api/types';
import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {Discount, MemberPass, Order, OrderReturn, OrderReturnStatus, OrderStatus, Payment, ProductSummary, Program, ProgramSession, Reservation, ReservationType, Review, ReviewTargetType, Subscription} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {statusApi, useStatusLabels} from '../../api/statuses';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {ProgressAndMessages, ProgressTemplateManager} from '../../components/ProgressPanel';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDate, formatDateTime} from '../../components/format';
import {priceLabel} from '../../components/price';
import {reservationUsage} from '../../components/reservationUsage';
import {RegexBindingSelect} from '../../components/RegexBindingSelect';
import {CustomStatusSelect, StatusManager} from '../../components/StatusManager';
import {BILLING_CYCLE, ORDER_STATUS, PASS_UNIT, RESERVATION_STATUS, RESERVATION_TYPE, RESERVATION_TYPE_GROUPS, RETURN_STATUS, RETURN_TYPE, SALE_TYPE, toOptions} from '../../components/labels';
import {EMPTY_LAYOUT} from '../../components/LayoutEditor';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import {EMPTY_OPTION_SET, type OptionSet, optionSetBody, toOptionSet} from '../../components/ProductOptionsEditor';
import {OrderAmount, OrderItemsTable} from '../shop/OrderItems';

const PRODUCT_FIELDS: FieldSpec[] = [
    {name: 'name', label: '상품명', required: true, wide: true},
    {name: 'saleType', label: '판매 방식', type: 'select', required: true, wide: true, options: toOptions(SALE_TYPE),
        help: '구매: 한 번 결제 / 구독: 주기마다 결제 / 후원: 금액을 정해 후원 / 견적 문의: 요청을 받고 관리자가 금액을 회신 / 이용권: 예약에서 결제 대신 차감 / 상품권: 결제하면 가격만큼 지갑에 충전 / 예약 판매: 판매 기간에만 주문받고 정해둔 날짜에 출고 / 펀딩: 마감일까지 목표 금액을 못 채우면 전부 환불 / 문의만: 결제 없이 상담 예약으로만 연결(매물 게시 등)'},
    {name: 'preorderSaleStartAt', label: '판매 시작', type: 'datetime', wide: true, help: '비워두면 지금부터 바로 받습니다.',
        visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'preorderSaleEndAt', label: '판매 종료', type: 'datetime', wide: true, help: '비워두면 계속 받습니다.',
        visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'preorderShipDate', label: '출고 예정일', type: 'date', required: true, wide: true,
        help: '주문한 회원에게 이 날짜를 안내합니다.', visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'billingCycle', label: '결제 주기', type: 'select', required: true, wide: true, options: toOptions(BILLING_CYCLE),
        visibleWhen: (v) => v.saleType === 'SUBSCRIPTION'},
    {name: 'commitmentMonths', label: '약정 기간(개월)', type: 'number', wide: true, help: '비워두면 약정 없이 언제든 해지할 수 있습니다.',
        visibleWhen: (v) => v.saleType === 'SUBSCRIPTION'},
    {name: 'earlyTerminationFeeRate', label: '약정 중 해지 위약금(%)', type: 'number', wide: true,
        help: '남은 약정 회차 금액의 이 비율만큼 해지할 때 바로 청구합니다.',
        visibleWhen: (v) => v.saleType === 'SUBSCRIPTION' && Boolean(v.commitmentMonths)},
    {name: 'passUnit', label: '이용권 단위', type: 'select', required: true, wide: true, options: toOptions(PASS_UNIT),
        visibleWhen: (v) => v.saleType === 'PASS'},
    {name: 'passDays', label: '사용 기간(일)', type: 'number', wide: true, help: '결제일부터 셉니다.',
        visibleWhen: (v) => v.saleType === 'PASS' && v.passUnit === 'PERIOD'},
    {name: 'passCount', label: '횟수', type: 'number', wide: true, help: '주문 수량만큼 곱해 발급합니다.',
        visibleWhen: (v) => v.saleType === 'PASS' && v.passUnit === 'COUNT'},
    {name: 'passProgramId', label: '쓸 수 있는 예약 상품', type: 'select', required: true, wide: true,
        help: '이 이용권으로 예약할 수 있는 예약 상품 하나를 고릅니다.', visibleWhen: (v) => v.saleType === 'PASS'},
    {name: 'inquiryProgramId', label: '연결할 상담 예약 상품', type: 'select', required: true, wide: true,
        help: '매물 등 문의만 받는 상품에서 "상담 예약하기"를 누르면 이 예약 상품의 예약 화면으로 이동합니다.',
        visibleWhen: (v) => v.saleType === 'INQUIRY'},
    {name: 'price', label: '가격(원)', type: 'number', required: true, wide: true,
        help: '구독은 회차별 금액, 후원은 최소 후원 금액입니다. 견적 문의·문의만은 0으로 두면 됩니다.'},
    {name: 'donationPresetsText', label: '추천 후원 금액', wide: true, placeholder: '5000, 10000, 30000',
        help: '쉼표로 구분합니다. 후원하는 사람이 버튼으로 고를 수 있습니다.', visibleWhen: (v) => v.saleType === 'DONATION'},
    {name: 'goalAmount', label: '목표 금액(원)', type: 'number', wide: true,
        help: '후원은 비워두면 달성률을 표시하지 않습니다. 펀딩은 꼭 정해야 합니다.',
        visibleWhen: (v) => v.saleType === 'DONATION' || v.saleType === 'FUNDING'},
    {name: 'fundingDeadline', label: '펀딩 마감일시', type: 'datetime', required: true, wide: true,
        help: '마감일까지 목표 금액을 못 채우면 결제된 주문을 모두 취소하고 환불합니다.',
        visibleWhen: (v) => v.saleType === 'FUNDING'},
    {name: 'availableQuantity', label: '재고', type: 'number', help: '상품 전체 재고입니다. 비워두면 무제한이고, 옵션 조합별 재고는 아래에서 따로 정합니다.', wide: true},
    {name: 'storeOrder', label: '매장 주문 전용 (포장·매장 식사)', type: 'checkbox', wide: true,
        help: '켜면 배송 주문은 받지 않고, 매장 주문 설정에 따라 포장이나 매장 식사로만 주문받습니다.'},
    {name: 'requiresShipping', label: '배송 상품', type: 'checkbox', wide: true, visibleWhen: (v) => !v.storeOrder,
        help: '디지털·서비스 상품은 끄세요. 배송 상품이 있는 주문에만 배송비를 받습니다.'},
    {name: 'isEnabled', label: '판매 중', type: 'checkbox', wide: true},
    {name: 'digitalFiles', label: '디지털 파일', type: 'files',
        help: '결제한 회원이 마이페이지에서 내려받습니다. 여기에 넣은 파일은 공개 주소로 내려받을 수 없게 보호됩니다.'},
    {name: 'downloadLimit', label: '내려받기 횟수', type: 'number', min: 1, help: '구매 한 건당 횟수입니다. 비워두면 무제한입니다.',
        visibleWhen: (v) => ((v.digitalFiles as FileInfo[] | undefined) ?? []).length > 0},
    {name: 'downloadDays', label: '내려받기 기간(일)', type: 'number', min: 1, help: '결제일부터 셉니다. 비워두면 무기한입니다.',
        visibleWhen: (v) => ((v.digitalFiles as FileInfo[] | undefined) ?? []).length > 0},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image', wide: true},
    {name: 'optionSet', label: '옵션', type: 'productOptions',
        help: '옵션 그룹(색상, 사이즈 등)을 정하면 모든 조합이 만들어지고 조합마다 추가 금액·재고·판매 여부를 정할 수 있습니다. 그룹 없이 단일 옵션만 둘 수도 있습니다.'},
    {name: 'contentHtml', label: '상세 설명', type: 'html'},
];

const productGradeFields = (grades: Grade[]): FieldSpec[] => [
    gradeSelectField('accessGradeId', '구매 가능 등급', grades, '이 등급 이상 회원만 주문할 수 있습니다.'),
    {name: 'grantGradeId', label: '결제 시 부여할 등급', type: 'select', wide: true,
        options: grades.map((grade) => ({value: String(grade.id), label: grade.name})),
        help: '결제가 끝나면 이 등급을 줍니다. 구독은 다음 결제일까지 유지되고 해지하면 원래 등급으로 돌아갑니다.'},
    {name: 'grantGradeDays', label: '등급 유지 일수', type: 'number', wide: true, min: 1, help: '비워두면 무기한입니다.',
        visibleWhen: (v) => Boolean(v.grantGradeId) && v.saleType !== 'SUBSCRIPTION'},
];

const toProductBody = (form: FormValues) => {
    const {rest, categoryId, attributes} = splitCategoryValues(form);
    const {donationPresetsText, optionSet, digitalFiles, pass, inquiry, preorder, funding, ...values} = rest;
    return {
    ...values,
    categoryId,
    attributes,
    digitalFileIds: ((digitalFiles as FileInfo[] | undefined) ?? []).map((file) => file.id),
    donationPresets: String(donationPresetsText ?? '').split(',').map((v) => Number(v.trim())).filter((v) => v > 0),
    ...optionSetBody(optionSet as OptionSet | undefined),
    };
};

const loadProducts = (query: Query) => adminApi.products.search(query);

export function ProductAdminPage() {
    const categories = useCategories();
    const [programChoices, setProgramChoices] = useState<{ value: string; label: string }[]>([]);
    const [fundingNotice, setFundingNotice] = useState<string | null>(null);
    useEffect(() => {
        adminApi.programs.search({size: 100}).then((page) => setProgramChoices(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProgramChoices([]));
    }, []);
    const base = useMemo(() => {
        let fields = PRODUCT_FIELDS.map((field) => field.name === 'passProgramId' || field.name === 'inquiryProgramId'
            ? {...field, options: programChoices} : field);
        const at = fields.findIndex((field) => field.name === 'requiresShipping');
        fields = [...fields.slice(0, at), ...productCategoryFields(categories), ...fields.slice(at)];
        return fields;
    }, [categories, programChoices]);
    const fields = useWithGradeFields(base, productGradeFields);
    return (
        <AdminCrudPage<ProductSummary>
            title="상품"
            description={<div className="toolbar">
                <button type="button" onClick={async () => {
                    const result = await adminApi.products.resolveFunding();
                    setFundingNotice(`마감된 펀딩 중 목표를 못 채운 ${result.failed}건을 환불 처리했습니다.`);
                }}>펀딩 마감 처리</button>
                {fundingNotice && <span className="ok">{fundingNotice}</span>}
            </div>}
            load={loadProducts}
            fields={fields}
            emptyForm={{isEnabled: true, requiresShipping: true, saleType: 'PURCHASE', price: 0, contentHtml: '', optionSet: EMPTY_OPTION_SET}}
            columns={[
                {label: '이미지', render: (p) => p.imageUrl ? <img className="thumb" src={p.imageUrl} alt={p.name}/> : '-'},
                {label: 'ID', render: (p) => p.id},
                {label: '상품명', render: (p) => <Link to={`/products/${p.id}`}>{p.name}</Link>},
                {label: '판매 방식', render: (p) => <span className="badge">{SALE_TYPE[p.saleType]}</span>},
                {label: '가격', render: (p) => priceLabel(p)},
                {label: '재고', render: (p) => p.availableQuantity ?? '무제한'},
                {label: '판매 중', render: (p) => <YesNo value={p.isEnabled}/>},
            ]}
            toForm={(p) => adminApi.products.get(p.id).then((full) => ({
                ...full,
                optionSet: toOptionSet(full.optionGroups ?? [], full.options),
                donationPresetsText: full.donationPresets.join(', '),
                digitalFiles: full.digitalFiles ?? [],
                categoryId: full.categoryId == null ? '' : String(full.categoryId),
                passUnit: full.pass?.unit ?? '',
                passDays: full.pass?.days ?? '',
                passCount: full.pass?.count ?? '',
                passProgramId: full.pass?.programId == null ? '' : String(full.pass.programId),
                inquiryProgramId: full.inquiry?.programId == null ? '' : String(full.inquiry.programId),
                preorderSaleStartAt: full.preorder?.saleStartAt ?? '',
                preorderSaleEndAt: full.preorder?.saleEndAt ?? '',
                preorderShipDate: full.preorder?.shipDate ?? '',
                fundingDeadline: full.funding?.deadline ?? '',
                ...attributesToForm(full.categoryId, full.attributes),
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

const CANCELABLE: OrderStatus[] = ['QUOTE_REQUESTED', 'QUOTED', 'PENDING_PAYMENT', 'PAID'];

function QuoteForm({order, onDone}: { order: Order; onDone: () => void }) {
    const [amount, setAmount] = useState(order.totalPrice?.toString() ?? '');
    const [message, setMessage] = useState(order.quoteMessage ?? '');
    const [error, setError] = useState<unknown>(null);
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await adminApi.orders.quote(order.id, Number(amount), message);
            onDone();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <form className="inline-form" onSubmit={submit}>
            <h3>견적 회신</h3>
            <p className="muted">요청 내용: {order.requestMessage ?? '(내용 없음)'}</p>
            <div className="schema-grid">
                <div className="field">
                    <label htmlFor={`quote-amount-${order.id}`}>견적 금액(원)</label>
                    <input id={`quote-amount-${order.id}`} type="number" min={0} required value={amount} onChange={(e) => setAmount(e.target.value)}/>
                </div>
                <div className="field wide">
                    <label htmlFor={`quote-message-${order.id}`}>안내 메시지</label>
                    <textarea id={`quote-message-${order.id}`} rows={3} value={message} onChange={(e) => setMessage(e.target.value)}/>
                </div>
            </div>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">견적 보내기</button>
            </div>
        </form>
    );
}

function OrderDetailPanel({order, onChange}: { order: Order; onChange: () => void }) {
    const [courier, setCourier] = useState(order.courier ?? '');
    const [trackingNumber, setTrackingNumber] = useState(order.trackingNumber ?? '');
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        if (!order.courier) {
            shopApi.shippingPolicy().then((policy) => setCourier((current) => current || policy.defaultCourier || '')).catch(() => undefined);
        }
    }, [order.courier]);
    const run = async (action: () => Promise<unknown>) => {
        setError(null);
        try {
            await action();
            onChange();
        } catch (e) {
            setError(e);
        }
    };
    const activeCount = order.items.filter((item) => item.status === 'ORDERED').length;
    const partiallyCancelable = order.status === 'PENDING_PAYMENT' || order.status === 'PAID';
    return (
        <div className="inline-form">
            <h3>주문 상세</h3>
            <OrderItemsTable order={order} onCancel={partiallyCancelable && activeCount > 1
                ? (item) => window.confirm(`'${item.productName}' 항목을 취소할까요? 결제된 주문은 이 항목 금액을 환불합니다.`)
                    && void run(() => adminApi.orders.cancelItem(order.id, item.id))
                : undefined}/>
            <p className="muted">
                {order.address ? `배송지: (${order.postalCode ?? '-'}) ${order.address} ${order.detailAddress ?? ''}` : '배송지 없음 (방문 수령 등)'}
            </p>
            {(order.status === 'PAID' || order.status === 'SHIPPING') && (
                <form className="toolbar" onSubmit={(e) => {
                    e.preventDefault();
                    void run(() => adminApi.orders.ship(order.id, courier, trackingNumber));
                }}>
                    <input aria-label="택배사" placeholder="택배사" required value={courier} onChange={(e) => setCourier(e.target.value)}/>
                    <input aria-label="송장 번호" placeholder="송장 번호" required value={trackingNumber}
                           onChange={(e) => setTrackingNumber(e.target.value)}/>
                    <button type="submit" className="primary">{order.status === 'PAID' ? '송장 입력 후 배송 처리' : '송장 수정'}</button>
                </form>
            )}
            <ErrorBanner error={error}/>
            <ProgressAndMessages
                getProgress={() => adminApi.progress.get('ORDER', order.id)}
                advance={(stageCode, memo) => adminApi.progress.advance('ORDER', order.id, {stageCode, memo})}
                listMessages={() => adminApi.messages.list('ORDER', order.id)}
                postMessage={(content, fileIds) => adminApi.messages.post('ORDER', order.id, {content, fileIds})}
            />
        </div>
    );
}

export function OrderAdminPage() {
    const {statuses, label: statusLabel, reload: reloadStatuses} = useStatusLabels('ORDER', ORDER_STATUS);
    const [status, setStatus] = useState('');
    const [quoting, setQuoting] = useState<number | null>(null);
    const [detail, setDetail] = useState<number | null>(null);
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
                <ProgressTemplateManager targetType="ORDER"/>
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
                {label: '상품', render: (o) => (
                    <button type="button" className="link-button" onClick={() => setDetail(detail === o.id ? null : o.id)}>
                        {o.productName} × {o.quantity}
                    </button>
                )},
                {label: '판매 방식', render: (o) => <span className="badge">{SALE_TYPE[o.saleType]}</span>},
                {label: '출고예정일', render: (o) => o.shipDate ? formatDate(o.shipDate) : '-'},
                {label: '금액', render: (o) => <OrderAmount order={o}/>},
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
                {label: '견적', className: 'button-cell', render: (o) => (o.status === 'QUOTE_REQUESTED' || o.status === 'QUOTED') && (
                    <button type="button" onClick={() => setQuoting(quoting === o.id ? null : o.id)}>
                        {quoting === o.id ? '닫기' : o.status === 'QUOTED' ? '견적 수정' : '견적 회신'}
                    </button>
                )},
                {label: '취소', className: 'button-cell', render: (o, reload) => CANCELABLE.includes(o.status) && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm('주문을 취소할까요? 결제된 주문은 환불 처리됩니다.')) {
                            void adminApi.orders.changeStatus(o.id, 'CANCELED').then(reload);
                        }
                    }}>취소</button>
                )},
            ]}
            expandedRow={(o, reload) => (
                <>
                    {quoting === o.id && <QuoteForm order={o} onDone={() => {
                        setQuoting(null);
                        reload();
                    }}/>}
                    {detail === o.id && <OrderDetailPanel order={o} onChange={reload}/>}
                </>
            )}
        />
    );
}

const loadReturnRequests = (query: Query) => adminApi.returnRequests.search(query);

export function OrderReturnAdminPage() {
    const [status, setStatus] = useState<OrderReturnStatus | ''>('');
    return (
        <AdminCrudPage<OrderReturn>
            title="반품·교환"
            searchable={false}
            description="주문이 결제·배송 완료된 뒤 회원이 신청한 반품·교환입니다. 승인하면 반품은 환불·재고 복구, 교환은 새 옵션으로 결제 완료된 주문을 바로 만듭니다."
            load={loadReturnRequests}
            extraQuery={{status}}
            filters={
                <select aria-label="처리 상태" value={status} onChange={(e) => setStatus(e.target.value as OrderReturnStatus | '')}>
                    <option value="">전체 상태</option>
                    {toOptions(RETURN_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: '주문번호', render: (r) => <code>{r.orderNumber}</code>},
                {label: '구분', render: (r) => <span className="badge">{RETURN_TYPE[r.type]}</span>},
                {label: '이유', render: (r) => r.reason},
                {label: '상태', render: (r) => r.status === 'REQUESTED' ? <span className="badge">{RETURN_STATUS[r.status]}</span>
                        : r.status === 'APPROVED' ? <span className="badge ok">{RETURN_STATUS[r.status]}</span>
                            : <span className="badge danger">{RETURN_STATUS[r.status]}</span>},
                {label: '관리자 메모', render: (r) => r.adminMemo ?? '-'},
                {label: '신청일', render: (r) => formatDateTime(r.createdAt)},
            ]}
            actions={(r, reload) => r.status === 'REQUESTED' && (
                <>
                    <button type="button" onClick={() => {
                        if (window.confirm(r.type === 'EXCHANGE' ? '교환을 승인할까요? 원래 주문은 취소·환불되고 새 옵션으로 결제 완료된 주문이 만들어집니다.'
                            : '반품을 승인할까요? 주문이 취소되고 환불·재고 복구가 이루어집니다.')) {
                            void adminApi.returnRequests.approve(r.id).then(reload);
                        }
                    }}>승인</button>
                    <button type="button" className="danger" onClick={() => {
                        const memo = window.prompt('거절 이유를 입력해 주세요.');
                        if (memo != null) {
                            void adminApi.returnRequests.reject(r.id, memo).then(reload);
                        }
                    }}>거절</button>
                </>
            )}
        />
    );
}

const loadSubscriptions = (query: Query) => adminApi.subscriptions.search(query);

export function SubscriptionAdminPage() {
    const [notice, setNotice] = useState<string | null>(null);
    const [version, setVersion] = useState(0);
    return (
        <AdminCrudPage<Subscription>
            key={version}
            title="구독"
            searchable={false}
            description={<>
                <p>구독 상품을 결제하면 구독이 시작되고, 결제일마다 다음 회차 주문이 만들어져 자동 결제됩니다(목업 PG). 매일 새벽 3시에 처리합니다.</p>
                <div className="toolbar">
                    <button type="button" onClick={async () => {
                        const result = await adminApi.subscriptions.renewDue();
                        setNotice(`결제일이 된 구독 ${result.renewed}건을 갱신했습니다.`);
                        setVersion((v) => v + 1);
                    }}>지금 갱신 처리</button>
                    {notice && <span className="ok">{notice}</span>}
                </div>
            </>}
            load={loadSubscriptions}
            columns={[
                {label: 'ID', render: (s) => s.id},
                {label: '상품', render: (s) => s.productName},
                {label: '회원 ID', render: (s) => s.memberId},
                {label: '금액', render: (s) => `${BILLING_CYCLE[s.billingCycle]} ${won(s.price)}`},
                {label: '상태', render: (s) => (
                    s.status === 'ACTIVE' ? <span className="badge ok">구독 중</span>
                        : s.status === 'PAUSED' ? <span className="badge">일시정지{s.resumeOn ? ` (${s.resumeOn}에 재개)` : ''}</span>
                        : <span className="badge">해지</span>
                )},
                {label: '다음 결제일', render: (s) => s.nextBillingDate ?? '-'},
                {label: '약정', render: (s) => s.commitmentEndDate
                    ? <>{formatDate(s.commitmentEndDate)}까지{s.earlyTerminationFee > 0 && ` · 위약금 ${won(s.earlyTerminationFee)}`}</>
                    : '-'},
                {label: '시작일', render: (s) => formatDateTime(s.startedAt)},
            ]}
            actions={(s, reload) => s.status !== 'CANCELED' && (
                <>
                    {s.status === 'ACTIVE' ? (
                        <button type="button" onClick={() => {
                            const resumeOn = window.prompt('재개일을 입력하세요 (YYYY-MM-DD, 비우면 직접 재개할 때까지 정지)') ?? undefined;
                            if (resumeOn !== undefined) {
                                void adminApi.subscriptions.pause(s.id, resumeOn || null).then(reload);
                            }
                        }}>일시정지</button>
                    ) : (
                        <button type="button" onClick={() => void adminApi.subscriptions.resume(s.id).then(reload)}>재개</button>
                    )}
                    <button type="button" className="danger" onClick={() => {
                        const message = s.earlyTerminationFee > 0
                            ? `구독을 해지할까요? 약정 기간이 남아 위약금 ${won(s.earlyTerminationFee)}이 바로 결제됩니다.`
                            : '구독을 해지할까요? 다음 결제일부터 결제되지 않습니다.';
                        if (window.confirm(message)) {
                            void adminApi.subscriptions.cancel(s.id).then(reload);
                        }
                    }}>해지</button>
                </>
            )}
        />
    );
}

const loadPasses = (query: Query) => adminApi.passes.search(query);

export function PassAdminPage() {
    return (
        <AdminCrudPage<MemberPass>
            title="이용권"
            searchable={false}
            description="회원이 이용권 상품을 결제하면 발급됩니다. 예약할 때 이용권을 고르면 결제 대신 기간·횟수가 차감됩니다."
            load={loadPasses}
            columns={[
                {label: 'ID', render: (p) => p.id},
                {label: '회원 ID', render: (p) => p.memberId},
                {label: '이용권', render: (p) => <>{p.productName} <span className="badge">{PASS_UNIT[p.unit]}</span></>},
                {label: '남은 횟수', render: (p) => p.unit === 'COUNT' ? `${p.remainingCount ?? 0}/${p.totalCount ?? 0}` : '-'},
                {label: '만료일', render: (p) => p.expiresAt ? formatDateTime(p.expiresAt) : '-'},
                {label: '상태', render: (p) => p.status === 'ACTIVE' ? <span className="badge ok">사용 가능</span>
                        : p.status === 'USED_UP' ? <span className="badge">다 씀</span> : <span className="badge">취소</span>},
                {label: '발급일', render: (p) => formatDateTime(p.createdAt)},
            ]}
            actions={(p, reload) => p.status === 'ACTIVE' && (
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm('이 이용권을 취소할까요? 더 이상 쓸 수 없게 됩니다.')) {
                        void adminApi.passes.cancel(p.id).then(reload);
                    }
                }}>취소</button>
            )}
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
        help: '프로그램은 1인, 상담은 1회, 대관·식당 테이블은 칸·예약 단위당, 숙박은 칸·1박당, 공연은 좌석당, 물품 대여는 1개·1일당 가격입니다. 배치표에서 칸별 가격을 따로 줄 수 있습니다. 식당 테이블은 보통 0원으로 둡니다. 여행·투어는 아래 인원 유형별 요금을 쓰므로 이 값은 사용하지 않습니다(0으로 둡니다).'},
    {name: 'priceTiersText', label: '인원 유형별 요금', wide: true, required: true, placeholder: '성인:100000, 아동:50000',
        help: '이름:가격 형태로 쉼표로 구분해 적습니다. 예약할 때 유형별 인원을 입력받아 합산합니다.', visibleWhen: isType('TOUR')},
    {name: 'maximumPerson', label: '정원·수량', type: 'number', wide: true, visibleWhen: isType('PROGRAM', 'CONSULTATION', 'VISIT', 'ITEM_RENTAL'),
        help: '프로그램은 전체 인원, 상담·방문은 시간대별 동시 예약 수(비우면 프로그램은 제한 없음, 상담·방문은 1건), 물품 대여는 보유 수량(필수)입니다.'},
    {name: 'depositAmount', label: '보증금(원, 1개당)', type: 'number', wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: '결제 금액에 더해 받고, 반납 처리할 때 차감액을 뺀 만큼 돌려줍니다.'},
    {name: 'maxRentalDays', label: '최대 대여 일수', type: 'number', min: 1, wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: '비워두면 제한이 없습니다.'},
    {name: 'lateFeePerDay', label: '연체료(원, 1일당)', type: 'number', wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: '반납일을 넘기면 하루당 이 금액을 보증금에서 뗍니다. 비워두면 연체료를 받지 않습니다.'},
    {name: 'supportsQueue', label: '접수·대기 사용', type: 'checkbox', wide: true, visibleWhen: isType('CONSULTATION'),
        help: '체크인할 때 대기 순번을 매기고, 관리자 "접수·대기" 메뉴에서 호출·완료를 관리합니다. (진료 접수 등)'},
    {name: 'isEnabled', label: '공개', type: 'checkbox', wide: true},
    {name: 'startDateTime', label: '시작 일시', type: 'datetime', wide: true,
        help: '프로그램은 진행 시작, 상담·방문·대관·숙박은 예약을 받는 기간의 시작, 공연은 공연 일시입니다.'},
    {name: 'endDateTime', label: '종료 일시', type: 'datetime', wide: true},
    {name: 'slotMinutes', label: '예약 단위(분)', type: 'number', wide: true, visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE'),
        help: '상담·방문 1회 길이, 대관·식당 테이블 최소 단위입니다.'},
    {name: 'openTime', label: '운영 시작 시각', type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE')},
    {name: 'closeTime', label: '운영 종료 시각', type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE')},
    {name: 'layout', label: '배치표', type: 'layout', visibleWhen: isType('RENTAL', 'LODGING', 'EVENT', 'TABLE'),
        help: '좌석·객실·공간을 칠해 두면 사용자가 그 칸을 골라 예약합니다. 식당 테이블은 칸별로 인원(좌석 수)을 함께 정해 두면 예약할 때 인원을 넘지 못하게 막습니다.'},
    {name: 'applicationStartDateTime', label: '신청 시작', type: 'datetime', wide: true},
    {name: 'applicationEndDateTime', label: '신청 마감', type: 'datetime', wide: true},
    {name: 'description', label: '요약 설명', wide: true},
    {name: 'imageFileId', label: '대표 이미지', type: 'image', wide: true},
    {name: 'contentHtml', label: '상세 설명', type: 'html'},
];

const programGradeFields = (grades: Grade[]) => [
    gradeSelectField('accessGradeId', '예약 가능 등급', grades, '이 등급 이상 회원만 예약할 수 있습니다.'),
];

const toProgramBody = ({priceTiersText, ...values}: FormValues) => ({
    ...values,
    priceTiers: values.reservationType === 'TOUR'
        ? String(priceTiersText ?? '').split(',').map((part) => part.trim()).filter(Boolean).map((part) => {
            const [name, price] = part.split(':');
            return {name: (name ?? '').trim(), price: Number((price ?? '').trim())};
        })
        : null,
});

const loadPrograms = (query: Query) => adminApi.programs.search(query);

export function ProgramAdminPage() {
    const fields = useWithGradeFields(PROGRAM_FIELDS, programGradeFields);
    return (
        <AdminCrudPage<Program>
            title="예약"
            description="시간 예약(프로그램·상담), 공간 예약(대관·숙박·공연·식당 테이블), 물품 대여, 패키지 예약(여행·투어)을 등록합니다. 예약 내역은 하위 메뉴에서 확인합니다."
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
            toForm={(p) => adminApi.programs.get(p.id).then((full) => ({...full, layout: full.layout ?? EMPTY_LAYOUT,
                priceTiersText: (full.priceTiers ?? []).map((t) => `${t.name}:${t.price}`).join(', ')}))}
            create={(v) => adminApi.programs.create(toProgramBody(v))}
            update={(p, v) => adminApi.programs.update(p.id, toProgramBody(v))}
            remove={(p) => adminApi.programs.remove(p.id)}
            expandedRow={(p) => (p.reservationType === 'PROGRAM' || p.reservationType === 'EVENT' || p.reservationType === 'TOUR')
                ? <SessionManager program={p}/> : undefined}
        />
    );
}

function SessionManager({program}: { program: Program }) {
    const [sessions, setSessions] = useState<ProgramSession[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [editing, setEditing] = useState<{ id: number | null; name: string; startAt: string; endAt: string; capacity: string } | null>(null);

    const reload = useCallback(() => {
        adminApi.programs.get(program.id).then((full) => setSessions(full.sessions)).catch(setError);
    }, [program.id]);
    useEffect(reload, [reload]);

    const startNew = () => setEditing({id: null, name: '', startAt: '', endAt: '', capacity: ''});
    const startEdit = (session: ProgramSession) => setEditing({
        id: session.id, name: session.name ?? '', startAt: session.startAt.slice(0, 16),
        endAt: session.endAt?.slice(0, 16) ?? '', capacity: session.capacity != null ? String(session.capacity) : '',
    });

    const save = async () => {
        if (!editing || !editing.startAt) {
            return;
        }
        const body = {
            name: editing.name || null, startAt: `${editing.startAt}:00`, endAt: editing.endAt ? `${editing.endAt}:00` : null,
            capacity: editing.capacity ? Number(editing.capacity) : null,
        };
        try {
            if (editing.id) {
                await adminApi.sessions.update(program.id, editing.id, body);
            } else {
                await adminApi.sessions.create(program.id, body);
            }
            setEditing(null);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const remove = async (session: ProgramSession) => {
        if (!window.confirm('이 회차를 지울까요?')) {
            return;
        }
        try {
            await adminApi.sessions.remove(program.id, session.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div>
            <h3>{program.reservationType === 'TOUR' ? '출발일' : '회차'}</h3>
            <ErrorBanner error={error}/>
            <table className="compact">
                <thead><tr><th>이름</th><th>시작</th><th>종료</th><th>정원/예약</th><th/></tr></thead>
                <tbody>
                {sessions?.map((session) => (
                    <tr key={session.id}>
                        <td>{session.name || '-'}</td>
                        <td>{formatDateTime(session.startAt)}</td>
                        <td>{session.endAt ? formatDateTime(session.endAt) : '-'}</td>
                        <td>{session.capacity != null ? `${session.reservedCount}/${session.capacity}` : `${session.reservedCount}/제한 없음`}</td>
                        <td className="actions">
                            <button type="button" onClick={() => startEdit(session)}>수정</button>
                            <button type="button" className="danger" onClick={() => remove(session)}>삭제</button>
                        </td>
                    </tr>
                ))}
                {sessions?.length === 0 && <tr><td colSpan={5} className="muted">회차가 없습니다.</td></tr>}
                </tbody>
            </table>
            {editing ? (
                <div className="toolbar">
                    <input aria-label="회차 이름" placeholder="이름" value={editing.name} onChange={(e) => setEditing({...editing, name: e.target.value})}/>
                    <input type="datetime-local" aria-label="시작 일시" required value={editing.startAt}
                           onChange={(e) => setEditing({...editing, startAt: e.target.value})}/>
                    <input type="datetime-local" aria-label="종료 일시" value={editing.endAt}
                           onChange={(e) => setEditing({...editing, endAt: e.target.value})}/>
                    <input type="number" aria-label="정원" placeholder="정원" min={1} value={editing.capacity}
                           onChange={(e) => setEditing({...editing, capacity: e.target.value})}/>
                    <button type="button" className="primary" onClick={save}>저장</button>
                    <button type="button" onClick={() => setEditing(null)}>취소</button>
                </div>
            ) : <button type="button" onClick={startNew}>회차 추가</button>}
        </div>
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
                <p>관리자가 전화·현장 예약을 직접 등록할 수 있습니다. 관리자 명의로 저장되며 회원 마이페이지에는 보이지 않습니다. 예약번호나 예약 상품명으로 검색할 수 있습니다.</p>
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
                {label: '예약 상품', render: (r) => <>{r.programName} <span className="badge">{RESERVATION_TYPE[r.reservationType]}</span></>},
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
                {label: '반납·방문', render: (r, reload) => r.reservationType === 'ITEM_RENTAL' ? <ReturnCell reservation={r} onDone={reload}/>
                        : r.reservationType === 'VISIT' && <VisitCell reservation={r} onDone={reload}/>},
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
            actions={(r) => r.userType === 'MEMBER' && (
                <Link to={`/admin/customer-records?memberId=${r.userId}&reservationId=${r.id}`}>기록 남기기</Link>
            )}
        />
    );
}

function VisitCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const [workerName, setWorkerName] = useState(reservation.workerName ?? '');
    const [workerPhone, setWorkerPhone] = useState(reservation.workerPhone ?? '');
    const [report, setReport] = useState('');
    const [photoIds, setPhotoIds] = useState<number[]>([]);
    const [error, setError] = useState<unknown>(null);
    const run = (pending: Promise<unknown>) => {
        setError(null);
        pending.then(onDone).catch(setError);
    };
    const address = [reservation.visitPostalCode && `(${reservation.visitPostalCode})`, reservation.visitAddress, reservation.visitDetailAddress]
        .filter(Boolean).join(' ');
    return (
        <div className="small">
            <div>{address}</div>
            {reservation.requestMemo && <div className="muted">요청: {reservation.requestMemo}</div>}
            {reservation.workCompletedAt ? (
                <div>{formatDateTime(reservation.workCompletedAt)} 완료 · {reservation.workerName}{reservation.workReport && ` · ${reservation.workReport}`}</div>
            ) : reservation.status === 'CONFIRMED' && (
                <div className="review-cell">
                    <input aria-label="작업자 이름" placeholder="작업자" value={workerName} onChange={(e) => setWorkerName(e.target.value)}/>
                    <input aria-label="작업자 연락처" placeholder="연락처" value={workerPhone} onChange={(e) => setWorkerPhone(e.target.value)}/>
                    <button type="button" disabled={!workerName.trim()}
                            onClick={() => run(adminApi.reservations.assignWorker(reservation.id, {workerName, workerPhone}))}>
                        {reservation.workerName ? '작업자 변경' : '작업자 배정'}
                    </button>
                    {reservation.workerName && (
                        <>
                            <input aria-label="완료 보고" placeholder="작업 내용" value={report} onChange={(e) => setReport(e.target.value)}/>
                            <input type="file" accept="image/*" multiple aria-label="완료 사진" onChange={(e) => {
                                const files = Array.from(e.target.files ?? []);
                                Promise.all(files.map((file) => fileApi.upload(file)))
                                    .then((uploaded) => setPhotoIds(uploaded.map((file) => file.id)))
                                    .catch(setError);
                            }}/>
                            <button type="button" className="primary" onClick={() => {
                                if (window.confirm('작업 완료로 처리할까요? 완료한 예약은 취소할 수 없습니다.')) {
                                    run(adminApi.reservations.completeWork(reservation.id, {report, photoFileIds: photoIds}));
                                }
                            }}>작업 완료</button>
                        </>
                    )}
                </div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

function ReturnCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const [deduction, setDeduction] = useState('');
    const [memo, setMemo] = useState('');
    const [error, setError] = useState<unknown>(null);
    if (reservation.returnedAt) {
        return (
            <span className="small">
                {formatDateTime(reservation.returnedAt)} 반납{reservation.lateDays > 0 && ` · ${reservation.lateDays}일 연체`}
                <br/>보증금 {won(reservation.depositRefunded ?? 0)} 환불{reservation.returnMemo && ` · ${reservation.returnMemo}`}
            </span>
        );
    }
    if (reservation.status !== 'CONFIRMED') {
        return <span className="muted small">-</span>;
    }
    const submit = async () => {
        const amount = Number(deduction || 0);
        if (!window.confirm(`보증금 ${won(reservation.depositAmount ?? 0)} 중 ${won(amount)}을 차감하고 반납 처리할까요?`)) {
            return;
        }
        setError(null);
        try {
            await adminApi.reservations.returnItems(reservation.id, {deduction: amount, memo});
            onDone();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="review-cell">
            {reservation.overdue && <span className="badge danger">{reservation.lateDays}일 연체</span>}
            {Number(reservation.expectedLateFee ?? 0) > 0 && <div className="muted small">예상 연체료 {won(reservation.expectedLateFee)}</div>}
            {Number(reservation.unpaidExtensionFee ?? 0) > 0 && <div className="muted small">미결제 연장료 {won(reservation.unpaidExtensionFee)}</div>}
            {Number(reservation.outstandingAmount ?? 0) > 0 && <div className="muted small">미수금 {won(reservation.outstandingAmount)}</div>}
            <input type="number" min={0} aria-label="보증금 차감액" placeholder="차감액(원)" value={deduction}
                   onChange={(e) => setDeduction(e.target.value)}/>
            <input aria-label="반납 메모" placeholder="메모(파손 등)" value={memo} onChange={(e) => setMemo(e.target.value)}/>
            <button type="button" onClick={submit}>반납 처리</button>
            <ExtensionAdminCell reservation={reservation} onDone={onDone}/>
            <ErrorBanner error={error}/>
        </div>
    );
}

function ExtensionAdminCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const [newEndAt, setNewEndAt] = useState('');
    const [paid, setPaid] = useState(false);
    const [error, setError] = useState<unknown>(null);

    if (reservation.pendingExtensionId) {
        return (
            <>
                <span className="muted small">연장료 {won(reservation.pendingExtensionFee ?? 0)} 결제 대기</span>
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm('연장을 취소할까요?')) {
                        void adminApi.reservations.cancelExtension(reservation.id).then(onDone).catch(setError);
                    }
                }}>연장 취소</button>
                <ErrorBanner error={error}/>
            </>
        );
    }
    return (
        <>
            <input type="date" aria-label="연장할 반납일" value={newEndAt} onChange={(e) => setNewEndAt(e.target.value)}/>
            <label className="checkbox small">
                <input type="checkbox" checked={paid} onChange={(e) => setPaid(e.target.checked)}/>결제됨
            </label>
            <button type="button" disabled={!newEndAt} onClick={() => {
                void adminApi.reservations.extend(reservation.id, `${newEndAt}T00:00:00`, paid).then(onDone).catch(setError);
            }}>연장</button>
            <ErrorBanner error={error}/>
        </>
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
                {label: '대상', render: (p) => `${p.targetType === 'ORDER' ? '주문' : p.targetType === 'RESERVATION_EXTENSION' ? '예약 연장' : '예약'} #${p.targetId}`},
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
    {value: 'PROGRAM', label: '예약 상품 리뷰'},
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
                title={isProduct ? '상품 리뷰' : '예약 상품 리뷰'}
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
