import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import type {Order, PaymentMethod, ProductDetail, ProductSummary, Program, Reservation, Review, ReviewTargetType} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {useStatusLabels} from '../../api/statuses';
import type {Page} from '../../api/types';
import {useAuth} from '../../auth/AuthContext';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {ListSearch, pageParams} from '../../components/ListSearch';
import {ORDER_STATUS, RESERVATION_STATUS} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {Thumbnail} from '../../components/Thumbnail';
import {StableLabel} from '../../components/StableLabel';
import {useFeatureMenuName} from '../../components/SiteContext';

export function ProductCard({product}: { product: ProductSummary }) {
    return (
        <Link to={`/products/${product.id}`} className="product-card">
            <Thumbnail src={product.imageUrl} name={product.name}/>
            <strong className="product-name">{product.name}</strong>
            {product.description && <span className="product-summary muted small">{product.description}</span>}
            <span className="product-price">{won(product.price)}</span>
            {product.availableQuantity === 0 && <span className="badge danger">품절</span>}
        </Link>
    );
}

export function ProgramCard({program}: { program: Program }) {
    return (
        <Link to={`/programs/${program.id}`} className="product-card">
            <Thumbnail src={program.imageUrl} name={program.name}/>
            <strong className="product-name">{program.name}</strong>
            {program.startDateTime && <span className="muted small">{formatDateTime(program.startDateTime)}</span>}
            <span className="product-price">{Number(program.price) === 0 ? '무료' : won(program.price)}</span>
            {program.isOpen ? <span className="badge ok">접수 중</span> : <span className="badge">접수 마감</span>}
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

    useEffect(() => {
        shopApi.products(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{productTitle}</h1>
            <ListSearch label="상품 검색" placeholder="상품명 검색"/>
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((product) => <ProductCard key={product.id} product={product}/>)}
            </div>
            {data?.content.length === 0 && (
                <p className="muted">{keyword ? `'${keyword}'에 맞는 상품이 없습니다.` : '판매 중인 상품이 없습니다.'}</p>
            )}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(keyword ? {keyword, page: String(next)} : {page: String(next)})}/>}
        </section>
    );
}

function estimateOrder(product: ProductDetail, optionId: string, discountId: string, quantity: number): number {
    const option = product.options.find((o) => String(o.id) === optionId);
    const subtotal = (Number(product.price) + Number(option?.additionalPrice ?? 0)) * quantity;
    const discount = product.discounts.find((d) => String(d.id) === discountId);
    return discount ? Math.round(subtotal * (100 - Number(discount.rate)) / 100) : subtotal;
}

export function ProductDetailPage() {
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [optionId, setOptionId] = useState('');
    const [discountId, setDiscountId] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.product(id).then(setProduct).catch(setError);
    }, [id]);

    const estimate = useMemo(() => (product ? estimateOrder(product, optionId, discountId, quantity) : 0),
        [product, optionId, discountId, quantity]);

    const order = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({productId: String(id), quantity: String(quantity)});
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
                    <h1>{product.name}</h1>
                    {product.description && <p className="muted">{product.description}</p>}
                    <p className="price">{won(product.price)}</p>
                    {product.averageRating != null && <p>평점 {'★'.repeat(Math.round(product.averageRating))} {product.averageRating.toFixed(1)}</p>}
                    <p className="muted small">재고 {product.availableQuantity ?? '충분'}</p>
                    {user?.userType === 'MEMBER' ? (
                        <form onSubmit={order}>
                            {product.options.length > 0 && (
                                <div className="field">
                                    <label htmlFor="option">옵션</label>
                                    <select id="option" value={optionId} onChange={(e) => setOptionId(e.target.value)}>
                                        <option value="">기본</option>
                                        {product.options.map((o) => (
                                            <option key={o.id} value={o.id}>{o.name} (+{won(o.additionalPrice)})</option>
                                        ))}
                                    </select>
                                </div>
                            )}
                            {product.discounts.length > 0 && (
                                <div className="field">
                                    <label htmlFor="discount">할인</label>
                                    <select id="discount" value={discountId} onChange={(e) => setDiscountId(e.target.value)}>
                                        <option value="">적용 안 함</option>
                                        {product.discounts.map((d) => <option key={d.id} value={d.id}>{d.name} ({d.rate}%)</option>)}
                                    </select>
                                </div>
                            )}
                            <Field label="수량" name="quantity" type="number" min={1} value={quantity}
                                   onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                            <p className="price align-right">예상 결제 금액 {won(estimate)}</p>
                            <ErrorBanner error={error}/>
                            <div className="toolbar form-actions">
                                <button type="submit" className="primary" disabled={soldOut}>
                                    <StableLabel current={soldOut ? '품절' : '주문하기'} labels={['품절', '주문하기']}/>
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
    const programTitle = useFeatureMenuName('PROGRAM', '프로그램 신청');
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
                <h1>{program.name}</h1>
                {program.description && <p className="muted">{program.description}</p>}
                <dl className="meta">
                    <dt>참가비</dt>
                    <dd>{Number(program.price) === 0 ? '무료' : `${won(program.price)} / 1인`}</dd>
                    <dt>진행 일정</dt>
                    <dd>{formatDateTime(program.startDateTime)} ~ {formatDateTime(program.endDateTime)}</dd>
                    <dt>신청 기간</dt>
                    <dd>{formatDateTime(program.applicationStartDateTime)} ~ {formatDateTime(program.applicationEndDateTime)}</dd>
                    {program.averageRating != null && (
                        <>
                            <dt>평점</dt>
                            <dd>{'★'.repeat(Math.round(program.averageRating))} {program.averageRating.toFixed(1)}</dd>
                        </>
                    )}
                    <dt>잔여 인원</dt>
                    <dd>{program.maximumPerson ? `${program.availablePerson} / ${program.maximumPerson}명` : '제한 없음'}</dd>
                </dl>
                {!program.isOpen ? (
                    <p className="badge">지금은 신청할 수 없습니다.</p>
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
                            <td><Link to={`/products/${order.productId}`}>{order.productName}</Link> × {order.quantity}</td>
                            <td>{won(order.totalPrice)}</td>
                            <td>{ORDER_STATUS[order.status]}</td>
                            <td>{formatDateTime(order.createdAt)}</td>
                            <td className="button-cell">
                                {order.status === 'PENDING_PAYMENT' && <Link className="button primary" to={`/checkout/ORDER/${order.id}`}>결제</Link>}
                            </td>
                            <td className="button-cell">
                                {(order.status === 'PENDING_PAYMENT' || order.status === 'PAID') && (
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
                    <thead><tr><th>예약번호</th><th>프로그램</th><th>인원</th><th>금액</th><th>상태</th><th>결제</th><th>취소</th></tr></thead>
                    <tbody>
                    {data?.content.map((reservation) => (
                        <tr key={reservation.id}>
                            <td><code>{reservation.reservationNumber}</code></td>
                            <td><Link to={`/programs/${reservation.programId}`}>{reservation.programName}</Link></td>
                            <td>{reservation.quantity}명</td>
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
