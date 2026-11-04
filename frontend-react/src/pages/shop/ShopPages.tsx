import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import type {Order, PaymentMethod, ProductDetail, ProductSaleType, ProductSummary, Program, Reservation, ReservationType, Review, ReviewTargetType, Subscription} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {useStatusLabels} from '../../api/statuses';
import type {Page} from '../../api/types';
import {useAuth} from '../../auth/AuthContext';
import {won} from '../../components/AdminCrudPage';
import {priceLabel} from '../../components/price';
import {reservationUsage} from '../../components/reservationUsage';
import {ReservationPicker} from './ReservationPicker';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {ListSearch, pageParams} from '../../components/ListSearch';
import {BILLING_CYCLE, ORDER_STATUS, RESERVATION_STATUS, RESERVATION_TYPE, SALE_TYPE} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {Thumbnail} from '../../components/Thumbnail';
import {StableLabel} from '../../components/StableLabel';
import {useFeatureMenuName} from '../../components/SiteContext';
import {GradeBadge} from '../../components/GradeBadge';
import {OptionPicker} from './OptionPicker';
import {CategoryFilters} from './CategoryFilters';
import {useCategories} from '../../api/categories';
import {OrderAmount, OrderItemsTable} from './OrderItems';

export function ProductCard({product}: { product: ProductSummary }) {
    return (
        <Link to={`/products/${product.id}`} className="product-card">
            <Thumbnail src={product.imageUrl} name={product.name}/>
            <strong className="product-name">{product.name}</strong>
            {product.description && <span className="product-summary muted small">{product.description}</span>}
            <span className="product-price">{priceLabel(product)}</span>
            {product.saleType !== 'PURCHASE' && <span className="badge">{SALE_TYPE[product.saleType]}</span>}
            {product.availableQuantity === 0 && <span className="badge danger">품절</span>}
            <GradeBadge gradeId={product.accessGradeId}/>
        </Link>
    );
}

export function ProgramCard({program}: { program: Program }) {
    return (
        <Link to={`/programs/${program.id}`} className="product-card">
            <Thumbnail src={program.imageUrl} name={program.name}/>
            <strong className="product-name">{program.name}</strong>
            <span className="badge">{RESERVATION_TYPE[program.reservationType]}</span>
            {program.startDateTime && <span className="muted small">{formatDateTime(program.startDateTime)}</span>}
            <span className="product-price">{Number(program.price) === 0 ? '무료' : won(program.price)}</span>
            {program.isOpen ? <span className="badge ok">접수 중</span> : <span className="badge">접수 마감</span>}
            <GradeBadge gradeId={program.accessGradeId}/>
        </Link>
    );
}

export function ProductListPage() {
    const productTitle = useFeatureMenuName('PRODUCT', '상품');
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<ProductSummary> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const categories = useCategories();
    const query = params.toString();
    const filtered = [...params.keys()].some((key) => key === 'categoryId' || /^(attr|min|max)\./.test(key));

    useEffect(() => {
        shopApi.searchProducts(new URLSearchParams(query)).then(setData).catch(setError);
    }, [query]);

    return (
        <section className="card">
            <h1>{productTitle}</h1>
            <ListSearch label="상품 검색" placeholder="상품명 검색" keepFilters/>
            {categories.length > 0 && <CategoryFilters categories={categories}/>}
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((product) => <ProductCard key={product.id} product={product}/>)}
            </div>
            {data?.content.length === 0 && (
                <p className="muted">{keyword || filtered ? '조건에 맞는 상품이 없습니다.' : '판매 중인 상품이 없습니다.'}</p>
            )}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => {
                const nextParams = new URLSearchParams(params);
                nextParams.set('page', String(next));
                setParams(nextParams);
            }}/>}
        </section>
    );
}

function estimateOrder(product: ProductDetail, optionId: string, discountId: string, quantity: number): number {
    const option = product.options.find((o) => String(o.id) === optionId);
    const subtotal = (Number(product.price) + Number(option?.additionalPrice ?? 0)) * quantity;
    const discount = product.discounts.find((d) => String(d.id) === discountId);
    return discount ? Math.round(subtotal * (100 - Number(discount.rate)) / 100) : subtotal;
}

const ACTION_LABEL: Record<ProductSaleType, string> = {
    PURCHASE: '주문하기',
    SUBSCRIPTION: '구독하기',
    DONATION: '후원하기',
    QUOTE: '견적 문의하기',
};

function DonationProgressBar({product}: { product: ProductDetail }) {
    const raised = Number(product.donation?.raisedAmount ?? 0);
    const goal = product.goalAmount ? Number(product.goalAmount) : null;
    return (
        <div className="donation-progress">
            {goal && <progress value={Math.min(raised, goal)} max={goal} aria-label="모금 달성률"/>}
            <p>
                <strong>{won(raised)}</strong> 모금{goal && <> · 목표 {won(goal)} ({Math.floor(raised * 100 / goal)}%)</>}
                <span className="muted small"> · 후원자 {product.donation?.supporterCount ?? 0}명</span>
            </p>
        </div>
    );
}

export function ProductDetailPage() {
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [optionId, setOptionId] = useState('');
    const [discountId, setDiscountId] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [amount, setAmount] = useState(0);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.product(id).then((loaded) => {
            setProduct(loaded);
            setAmount(loaded.donationPresets[0] ?? Number(loaded.price));
        }).catch(setError);
    }, [id]);

    const estimate = useMemo(() => (product ? estimateOrder(product, optionId, discountId, quantity) : 0),
        [product, optionId, discountId, quantity]);

    const [cartNotice, setCartNotice] = useState<string | null>(null);
    const needsOption = (product?.optionGroups?.length ?? 0) > 0 && !optionId && product?.saleType !== 'DONATION';

    const addToCart = async () => {
        setError(null);
        setCartNotice(null);
        try {
            await shopApi.addToCart({productId: id, productOptionId: optionId ? Number(optionId) : null, quantity});
            setCartNotice('장바구니에 담았습니다.');
        } catch (e) {
            setError(e);
        }
    };

    const order = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({productId: String(id), quantity: String(quantity)});
        if (product?.saleType === 'DONATION') {
            query.set('amount', String(amount));
        }
        if (optionId) {
            query.set('optionId', optionId);
        }
        if (discountId) {
            query.set('discountId', discountId);
        }
        navigate(`/orders/new?${query}`);
    };

    if (error && !product) {
        return <ErrorBanner error={error}/>;
    }
    if (!product) {
        return <p className="muted">불러오는 중…</p>;
    }
    const soldOut = product.availableQuantity === 0;
    return (
        <>
            <section className="card product-detail">
                <div className="product-media">
                    <Thumbnail src={product.imageUrl} name={product.name}/>
                </div>
                <div>
                    <h1>{product.name} <GradeBadge gradeId={product.accessGradeId}/></h1>
                    {product.description && <p className="muted">{product.description}</p>}
                    <p className="price">{priceLabel(product)}</p>
                    {product.donation && <DonationProgressBar product={product}/>}
                    {product.averageRating != null && <p>평점 {'★'.repeat(Math.round(product.averageRating))} {product.averageRating.toFixed(1)}</p>}
                    {product.saleType !== 'DONATION' && <p className="muted small">재고 {product.availableQuantity ?? '충분'}</p>}
                    {product.specs.length > 0 && (
                        <table className="spec-table">
                            <caption>상품 사양</caption>
                            <tbody>
                            {product.specs.map((spec) => (
                                <tr key={spec.key}><th scope="row">{spec.label}</th><td>{String(spec.value)}{spec.unit ? ` ${spec.unit}` : ''}</td></tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                    {product.digitalFileCount > 0 && (
                        <p className="notice-box">
                            결제하면 마이페이지 내려받기에서 파일 {product.digitalFileCount}개를 받을 수 있습니다.
                            {product.downloadLimit != null && ` 최대 ${product.downloadLimit}회`}
                            {product.downloadDays != null && `${product.downloadLimit != null ? ',' : ''} 결제 후 ${product.downloadDays}일 이내`}
                            {(product.downloadLimit != null || product.downloadDays != null) && ' 받을 수 있습니다.'}
                        </p>
                    )}
                    {user?.userType === 'MEMBER' ? (
                        <form onSubmit={order}>
                            {product.saleType === 'DONATION' && (
                                <div className="field">
                                    <label htmlFor="amount">후원 금액(원)</label>
                                    {product.donationPresets.length > 0 && (
                                        <div className="toolbar">
                                            {product.donationPresets.map((preset) => (
                                                <button key={preset} type="button" className={amount === preset ? 'primary' : ''}
                                                        onClick={() => setAmount(preset)}>{won(preset)}</button>
                                            ))}
                                        </div>
                                    )}
                                    <input id="amount" type="number" min={Number(product.price)} step={1000} required value={amount}
                                           onChange={(e) => setAmount(Number(e.target.value))}/>
                                    <p className="muted small">{won(product.price)} 이상 후원할 수 있습니다.</p>
                                </div>
                            )}
                            {product.saleType !== 'DONATION' && (
                                <OptionPicker product={product} optionId={optionId} onChange={setOptionId}/>
                            )}
                            {product.saleType !== 'DONATION' && product.saleType !== 'QUOTE' && product.discounts.length > 0 && (
                                <div className="field">
                                    <label htmlFor="discount">할인</label>
                                    <select id="discount" value={discountId} onChange={(e) => setDiscountId(e.target.value)}>
                                        <option value="">적용 안 함</option>
                                        {product.discounts.map((d) => <option key={d.id} value={d.id}>{d.name} ({d.rate}%)</option>)}
                                    </select>
                                </div>
                            )}
                            {product.saleType !== 'DONATION' && (
                                <Field label="수량" name="quantity" type="number" min={1} value={quantity}
                                       onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                            )}
                            {product.saleType === 'PURCHASE' && <p className="price align-right">예상 결제 금액 {won(estimate)}</p>}
                            {product.saleType === 'SUBSCRIPTION' && product.billingCycle && (
                                <p className="price align-right">{BILLING_CYCLE[product.billingCycle]} {won(estimate)} 결제</p>
                            )}
                            <ErrorBanner error={error}/>
                            {cartNotice && <p className="ok">{cartNotice} <Link to="/cart">장바구니 보기</Link></p>}
                            <div className="toolbar form-actions">
                                {product.saleType === 'PURCHASE' && (
                                    <button type="button" disabled={soldOut || needsOption} onClick={addToCart}>장바구니 담기</button>
                                )}
                                <button type="submit" className="primary" disabled={(soldOut && product.saleType !== 'DONATION') || needsOption}>
                                    <StableLabel current={soldOut && product.saleType !== 'DONATION' ? '품절' : ACTION_LABEL[product.saleType ?? 'PURCHASE']}
                                                 labels={['품절', ...Object.values(ACTION_LABEL)]}/>
                                </button>
                            </div>
                        </form>
                    ) : (
                        <p className="muted"><Link to="/login" state={{from: `/products/${id}`}}>로그인</Link> 후 주문할 수 있습니다.</p>
                    )}
                </div>
            </section>
            <section className="card">
                <h2>상세 정보</h2>
                <HtmlView html={product.contentHtml}/>
            </section>
            <Reviews target="PRODUCT" targetId={id}/>
        </>
    );
}

const REVIEW_TARGETS = {
    PRODUCT: {
        load: shopApi.reviews,
        add: shopApi.addReview,
        hint: '구매한 상품에만 리뷰를 남길 수 있습니다.',
    },
    PROGRAM: {
        load: shopApi.programReviews,
        add: shopApi.addProgramReview,
        hint: '예약이 확정된 프로그램에만 리뷰를 남길 수 있습니다.',
    },
};

function Reviews({target, targetId}: { target: ReviewTargetType; targetId: number }) {
    const {load, add, hint} = REVIEW_TARGETS[target];
    const {user} = useAuth();
    const [reviews, setReviews] = useState<Page<Review> | null>(null);
    const [form, setForm] = useState({rating: 5, title: '', contentHtml: ''});
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        load(targetId, 0).then(setReviews).catch(setError);
    }, [load, targetId]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h2>리뷰 {reviews?.page.totalElements ?? 0}</h2>
            <ul className="comments">
                {reviews?.content.map((review) => (
                    <li key={review.id}>
                        <div className="row space-between">
                            <strong>{'★'.repeat(review.rating)} {review.title}</strong>
                            <span className="muted small">{review.authorName} · {formatDateTime(review.createdAt)}</span>
                        </div>
                        <HtmlView html={review.contentHtml}/>
                        {review.canDelete && (
                            <button type="button" className="link-button" onClick={() => shopApi.removeReview(review.id).then(reload).catch(setError)}>
                                삭제
                            </button>
                        )}
                    </li>
                ))}
            </ul>
            {user?.userType === 'MEMBER' && (
                <form className="comment-form" onSubmit={async (e) => {
                    e.preventDefault();
                    try {
                        setError(null);
                        await add(targetId, {...form, contentHtml: form.contentHtml ? `<p>${escapeHtml(form.contentHtml)}</p>` : ''});
                        setForm({rating: 5, title: '', contentHtml: ''});
                        reload();
                    } catch (err) {
                        setError(err);
                    }
                }}>
                    <p className="muted small">{hint}</p>
                    <div className="field">
                        <label htmlFor="review-rating">평점</label>
                        <select id="review-rating" value={form.rating} onChange={(e) => setForm({...form, rating: Number(e.target.value)})}>
                            {[5, 4, 3, 2, 1].map((r) => <option key={r} value={r}>{'★'.repeat(r)}</option>)}
                        </select>
                    </div>
                    <Field label="리뷰 제목" name="reviewTitle" required value={form.title}
                           onChange={(e) => setForm({...form, title: e.target.value})}/>
                    <div className="field">
                        <label htmlFor="review-content">리뷰 내용</label>
                        <textarea id="review-content" rows={3} value={form.contentHtml}
                                  onChange={(e) => setForm({...form, contentHtml: e.target.value})}/>
                    </div>
                    <div className="toolbar form-actions">
                        <button type="submit" className="primary">리뷰 등록</button>
                    </div>
                </form>
            )}
            <ErrorBanner error={error}/>
        </section>
    );
}

function escapeHtml(text: string) {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');
}

export function ProgramListPage() {
    const programTitle = useFeatureMenuName('PROGRAM', '예약');
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<Program> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.programs(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{programTitle}</h1>
            <ListSearch label="프로그램 검색" placeholder="프로그램명 검색"/>
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((program) => <ProgramCard key={program.id} program={program}/>)}
            </div>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : '신청 가능한 프로그램이 없습니다.'}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

const PRICE_UNIT: Record<ReservationType, string> = {
    PROGRAM: '1인',
    CONSULTATION: '1회',
    RENTAL: '칸·예약 단위',
    LODGING: '칸·1박',
    EVENT: '좌석',
};

export function ProgramDetailPage() {
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [program, setProgram] = useState<Program | null>(null);
    const [quantity, setQuantity] = useState(1);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.program(id).then(setProgram).catch(setError);
    }, [id]);

    const reserve = (event: FormEvent) => {
        event.preventDefault();
        navigate(`/reservations/new?${new URLSearchParams({programId: String(id), quantity: String(quantity)})}`);
    };

    if (error && !program) {
        return <ErrorBanner error={error}/>;
    }
    if (!program) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <>
            <section className="card product-detail">
                <div className="product-media">
                    <Thumbnail src={program.imageUrl} name={program.name}/>
                </div>
                <div>
                <h1>{program.name} <GradeBadge gradeId={program.accessGradeId}/></h1>
                {program.description && <p className="muted">{program.description}</p>}
                <dl className="meta">
                    <dt>유형</dt>
                    <dd>{RESERVATION_TYPE[program.reservationType]}</dd>
                    <dt>가격</dt>
                    <dd>{Number(program.price) === 0 ? '무료' : `${won(program.price)} / ${PRICE_UNIT[program.reservationType]}`}</dd>
                    <dt>{program.reservationType === 'EVENT' ? '공연 일시' : program.reservationType === 'PROGRAM' ? '진행 일정' : '예약 가능 기간'}</dt>
                    <dd>{formatDateTime(program.startDateTime)}{program.endDateTime && ` ~ ${formatDateTime(program.endDateTime)}`}</dd>
                    <dt>신청 기간</dt>
                    <dd>{formatDateTime(program.applicationStartDateTime)} ~ {formatDateTime(program.applicationEndDateTime)}</dd>
                    {program.averageRating != null && (
                        <>
                            <dt>평점</dt>
                            <dd>{'★'.repeat(Math.round(program.averageRating))} {program.averageRating.toFixed(1)}</dd>
                        </>
                    )}
                    {program.reservationType === 'PROGRAM' && (
                        <>
                            <dt>잔여 인원</dt>
                            <dd>{program.maximumPerson ? `${program.availablePerson} / ${program.maximumPerson}명` : '제한 없음'}</dd>
                        </>
                    )}
                </dl>
                {!program.isOpen ? (
                    <p className="badge">지금은 신청할 수 없습니다.</p>
                ) : user?.userType === 'MEMBER' && program.reservationType !== 'PROGRAM' ? (
                    <ReservationPicker program={program}/>
                ) : user?.userType === 'MEMBER' ? (
                    <form onSubmit={reserve}>
                        <Field label="인원" name="quantity" type="number" min={1} value={quantity}
                               onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                        <p className="price align-right">결제 금액 {won(Number(program.price) * quantity)}</p>
                        <ErrorBanner error={error}/>
                        <div className="toolbar form-actions">
                            <button type="submit" className="primary">신청하기</button>
                        </div>
                    </form>
                ) : (
                    <p className="muted"><Link to="/login" state={{from: `/programs/${id}`}}>로그인</Link> 후 신청할 수 있습니다.</p>
                )}
                </div>
            </section>
            <section className="card">
                <HtmlView html={program.contentHtml}/>
            </section>
            <Reviews target="PROGRAM" targetId={id}/>
        </>
    );
}

const METHODS: { value: PaymentMethod; label: string }[] = [
    {value: 'CARD', label: '신용카드'},
    {value: 'BANK_TRANSFER', label: '계좌이체'},
    {value: 'VIRTUAL_ACCOUNT', label: '가상계좌'},
    {value: 'MOBILE', label: '휴대폰'},
];

export function CheckoutPage() {
    const params = useParams();
    const type = params.type === 'RESERVATION' ? 'RESERVATION' : 'ORDER';
    const id = Number(params.id);
    const navigate = useNavigate();
    const [target, setTarget] = useState<{ title: string; amount: number; payable: boolean } | null>(null);
    const [method, setMethod] = useState<PaymentMethod>('CARD');
    const [error, setError] = useState<unknown>(null);
    const [paying, setPaying] = useState(false);

    useEffect(() => {
        if (type === 'ORDER') {
            shopApi.myOrder(id).then((order) => setTarget({
                title: `${order.productName} × ${order.quantity} (${order.orderNumber})`,
                amount: Number(order.totalPrice),
                payable: order.status === 'PENDING_PAYMENT',
            })).catch(setError);
        } else {
            shopApi.myReservations(0).then((page) => {
                const reservation = page.content.find((r) => r.id === id);
                if (reservation) {
                    setTarget({
                        title: `${reservation.programName} ${reservation.quantity}명 (${reservation.reservationNumber})`,
                        amount: Number(reservation.totalPrice),
                        payable: reservation.status === 'PENDING_PAYMENT',
                    });
                }
            }).catch(setError);
        }
    }, [type, id]);

    const pay = async () => {
        if (!target) {
            return;
        }
        setPaying(true);
        try {
            await shopApi.pay(type, id, target.amount, method);
            navigate(type === 'ORDER' ? '/mypage/orders' : '/mypage/reservations');
        } catch (e) {
            setError(e);
        } finally {
            setPaying(false);
        }
    };

    return (
        <section className="card narrow">
            <h1>결제</h1>
            <p className="notice-box">테스트용 가상 결제입니다. 실제로 결제되지 않으며 즉시 승인됩니다.</p>
            <ErrorBanner error={error}/>
            {target && (
                <>
                    <p>{target.title}</p>
                    <p className="price">{won(target.amount)}</p>
                    {target.payable ? (
                        <>
                            <div className="field">
                                <label htmlFor="method">결제 수단</label>
                                <select id="method" value={method} onChange={(e) => setMethod(e.target.value as PaymentMethod)}>
                                    {METHODS.map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
                                </select>
                            </div>
                            <button type="button" className="primary" disabled={paying} onClick={pay}>
                                {paying ? '결제 중…' : `${won(target.amount)} 결제하기`}
                            </button>
                        </>
                    ) : <p className="muted">이미 처리된 건입니다.</p>}
                </>
            )}
        </section>
    );
}

export function MyOrdersPage() {
    const {label: orderStatus} = useStatusLabels('ORDER', ORDER_STATUS);
    const navigate = useNavigate();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Order> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.myOrders(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>주문 내역</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>주문번호</th><th>상품</th><th>금액</th><th>상태</th><th>주문일</th><th>결제</th><th>취소</th></tr></thead>
                    <tbody>
                    {data?.content.map((order) => (
                        <tr key={order.id}>
                            <td><code>{order.orderNumber}</code></td>
                            <td>
                                {order.items.length > 1 ? order.productName
                                    : <><Link to={`/products/${order.productId}`}>{order.productName}</Link> × {order.quantity}</>}
                                {order.items.length > 1 && (
                                    <details>
                                        <summary>주문 상품 {order.items.length}개</summary>
                                        <OrderItemsTable order={order} onCancel={['PENDING_PAYMENT', 'PAID'].includes(order.status)
                                        && order.items.filter((item) => item.status === 'ORDERED').length > 1
                                            ? (item) => {
                                                if (window.confirm(`'${item.productName}'만 취소할까요? 결제한 주문은 이 상품 금액을 환불합니다. 남은 금액이 무료 배송 기준보다 적어지면 배송비를 빼고 환불합니다.`)) {
                                                    shopApi.cancelOrderItem(order.id, item.id).then(reload).catch(setError);
                                                }
                                            }
                                            : undefined}/>
                                    </details>
                                )}
                            </td>
                            <td>
                                <OrderAmount order={order}/>
                                {order.saleType !== 'PURCHASE' && <span className="badge">{SALE_TYPE[order.saleType]}</span>}
                                {order.quoteMessage && <div className="muted small">{order.quoteMessage}</div>}
                            </td>
                            <td>
                                {orderStatus(order.status, order.customStatus)}
                                {order.trackingNumber && <div className="muted small">{order.courier} {order.trackingNumber}</div>}
                            </td>
                            <td>{formatDateTime(order.createdAt)}</td>
                            <td className="button-cell">
                                {order.status === 'PENDING_PAYMENT' && <Link className="button primary" to={`/checkout/ORDER/${order.id}`}>결제</Link>}
                                {order.status === 'QUOTED' && (
                                    <button type="button" className="primary" onClick={() => {
                                        shopApi.acceptQuote(order.id).then(() => navigate(`/checkout/ORDER/${order.id}`)).catch(setError);
                                    }}>견적 수락</button>
                                )}
                            </td>
                            <td className="button-cell">
                                {['QUOTE_REQUESTED', 'QUOTED', 'PENDING_PAYMENT', 'PAID'].includes(order.status) && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('주문을 취소할까요?')) {
                                            shopApi.cancelOrder(order.id).then(reload).catch(setError);
                                        }
                                    }}>취소</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={7} className="muted">주문 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyReservationsPage() {
    const {label: reservationStatus} = useStatusLabels('RESERVATION', RESERVATION_STATUS);
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Reservation> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.myReservations(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>예약 내역</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>예약번호</th><th>예약 항목</th><th>이용</th><th>수량</th><th>금액</th><th>상태</th><th>결제</th><th>취소</th></tr></thead>
                    <tbody>
                    {data?.content.map((reservation) => (
                        <tr key={reservation.id}>
                            <td><code>{reservation.reservationNumber}</code></td>
                            <td><Link to={`/programs/${reservation.programId}`}>{reservation.programName}</Link></td>
                            <td>{reservationUsage(reservation)}</td>
                            <td>{reservation.quantity}</td>
                            <td>{won(reservation.totalPrice)}</td>
                            <td>{reservationStatus(reservation.status, reservation.customStatus)}</td>
                            <td className="button-cell">
                                {reservation.status === 'PENDING_PAYMENT' && (
                                    <Link className="button primary" to={`/checkout/RESERVATION/${reservation.id}`}>결제</Link>
                                )}
                            </td>
                            <td className="button-cell">
                                {reservation.status !== 'CANCELED' && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('예약을 취소할까요?')) {
                                            shopApi.cancelReservation(reservation.id).then(reload).catch(setError);
                                        }
                                    }}>취소</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={7} className="muted">예약 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MySubscriptionsPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Subscription> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.mySubscriptions(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>구독</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>상품</th><th>금액</th><th>상태</th><th>다음 결제일</th><th>시작일</th><th>해지</th></tr></thead>
                    <tbody>
                    {data?.content.map((item) => (
                        <tr key={item.id}>
                            <td><Link to={`/products/${item.productId}`}>{item.productName}</Link></td>
                            <td>{BILLING_CYCLE[item.billingCycle]} {won(item.price)}</td>
                            <td>{item.status === 'ACTIVE' ? <span className="badge ok">구독 중</span> : <span className="badge">해지</span>}</td>
                            <td>{item.nextBillingDate ?? '-'}</td>
                            <td>{formatDateTime(item.startedAt)}</td>
                            <td className="button-cell">
                                {item.status === 'ACTIVE' && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('구독을 해지할까요? 다음 결제일부터 결제되지 않습니다.')) {
                                            shopApi.cancelSubscription(item.id).then(reload).catch(setError);
                                        }
                                    }}>해지</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">구독 중인 상품이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
