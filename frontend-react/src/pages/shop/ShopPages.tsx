import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import type {GiftCard, MemberPass, MyLicenseKey, Order, OrderReturn, OrderReturnType, PaymentMethod, ProductDetail, ProductSaleType, ProductSummary, Program, Reservation, ReservationType, Review, ReviewTargetType, Subscription, WalletLedgerEntry} from '../../api/domain';
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
import {formatDate, formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {ListSearch, pageParams} from '../../components/ListSearch';
import {BILLING_CYCLE, LEDGER_TYPE, ORDER_STATUS, PASS_UNIT, QUEUE_STATUS, RESERVATION_STATUS, RESERVATION_TYPE, RETURN_STATUS, RETURN_TYPE, SALE_TYPE} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {Thumbnail} from '../../components/Thumbnail';
import {StableLabel} from '../../components/StableLabel';
import {useFeatureMenuName} from '../../components/SiteContext';
import {GradeBadge} from '../../components/GradeBadge';
import {ProgressAndMessages} from '../../components/ProgressPanel';
import {OptionPicker} from './OptionPicker';
import {CategoryFilters} from './CategoryFilters';
import {useCategories} from '../../api/categories';
import {OrderAmount, OrderItemsTable} from './OrderItems';
import {WishButton} from './WishButton';

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
    PASS: '구매하기',
    GIFT_CARD: '구매하기',
    PREORDER: '예약 주문하기',
    FUNDING: '펀딩 참여하기',
    INQUIRY: '상담 예약하기',
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

function FundingProgressBar({product}: { product: ProductDetail }) {
    const funding = product.funding;
    if (!funding) {
        return null;
    }
    const raised = Number(funding.raisedAmount);
    const goal = funding.goalAmount ? Number(funding.goalAmount) : null;
    return (
        <div className="donation-progress">
            {goal && <progress value={Math.min(raised, goal)} max={goal} aria-label="펀딩 달성률"/>}
            <p>
                <strong>{won(raised)}</strong> 펀딩{goal && <> · 목표 {won(goal)} ({Math.floor(raised * 100 / goal)}%)</>}
                <span className="muted small"> · 참여자 {funding.supporterCount}명</span>
            </p>
            <p className="notice-box">
                {funding.deadline && `마감 ${formatDateTime(funding.deadline)}`}
                {funding.resolved && (funding.succeeded ? ' · 목표를 달성했습니다.' : ' · 목표를 못 채워 모든 결제가 환불되었습니다.')}
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
    const now = new Date();
    const preorderClosed = product.preorder != null
        && ((product.preorder.saleStartAt != null && now < new Date(product.preorder.saleStartAt))
            || (product.preorder.saleEndAt != null && now > new Date(product.preorder.saleEndAt)));
    const fundingClosed = product.funding != null
        && (product.funding.resolved || (product.funding.deadline != null && now > new Date(product.funding.deadline)));
    const unavailable = (soldOut && product.saleType !== 'DONATION') || preorderClosed || fundingClosed;
    return (
        <>
            <section className="card product-detail">
                <div className="product-media">
                    <Thumbnail src={product.imageUrl} name={product.name}/>
                </div>
                <div>
                    <h1>{product.name} <GradeBadge gradeId={product.accessGradeId}/></h1>
                    <WishButton productId={product.id}/>
                    {product.description && <p className="muted">{product.description}</p>}
                    <p className="price">{priceLabel(product)}</p>
                    {product.donation && <DonationProgressBar product={product}/>}
                    {product.funding && <FundingProgressBar product={product}/>}
                    {product.saleType === 'SUBSCRIPTION' && product.commitmentMonths && (
                        <p className="notice-box">
                            {product.commitmentMonths}개월 약정 · 약정 중 해지하면 남은 기간 금액의 {product.earlyTerminationFeeRate}%를 위약금으로 청구합니다.
                        </p>
                    )}
                    {product.preorder && (
                        <p className="notice-box">
                            예약 판매{product.preorder.saleStartAt && ` · 판매 시작 ${formatDateTime(product.preorder.saleStartAt)}`}
                            {product.preorder.saleEndAt && ` · 판매 종료 ${formatDateTime(product.preorder.saleEndAt)}`}
                            {product.preorder.shipDate && ` · 출고 예정일 ${formatDate(product.preorder.shipDate)}`}
                        </p>
                    )}
                    {product.averageRating != null && <p>평점 {'★'.repeat(Math.round(product.averageRating))} {product.averageRating.toFixed(1)}</p>}
                    {product.saleType === 'INQUIRY' && (
                        <p className="notice-box">결제 없이 상담 예약으로 문의할 수 있는 상품입니다.</p>
                    )}
                    {product.saleType !== 'DONATION' && product.saleType !== 'INQUIRY' && (
                        <p className="muted small">재고 {product.availableQuantity ?? '충분'}</p>
                    )}
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
                    {product.saleType === 'INQUIRY' ? (
                        user?.userType === 'MEMBER' ? (
                            <Link className="button primary" to={`/reservations/new?programId=${product.inquiry?.programId}`}>
                                {ACTION_LABEL.INQUIRY}
                            </Link>
                        ) : (
                            <p className="muted"><Link to="/login" state={{from: `/products/${id}`}}>로그인</Link> 후 상담을 예약할 수 있습니다.</p>
                        )
                    ) : user?.userType === 'MEMBER' ? (
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
                                <button type="submit" className="primary" disabled={unavailable || needsOption}>
                                    <StableLabel current={soldOut && product.saleType !== 'DONATION' ? '품절'
                                        : preorderClosed ? '판매 기간이 아닙니다'
                                            : fundingClosed ? '펀딩이 마감되었습니다' : ACTION_LABEL[product.saleType ?? 'PURCHASE']}
                                                 labels={['품절', '판매 기간이 아닙니다', '펀딩이 마감되었습니다', ...Object.values(ACTION_LABEL)]}/>
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
        hint: '예약이 확정된 예약 상품에만 리뷰를 남길 수 있습니다.',
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
            <ListSearch label="예약 상품 검색" placeholder="예약 상품명 검색"/>
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((program) => <ProgramCard key={program.id} program={program}/>)}
            </div>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : '신청 가능한 예약 상품이 없습니다.'}</p>}
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
    ITEM_RENTAL: '1개·1일',
    VISIT: '1회',
    TABLE: '칸·예약 단위',
    TOUR: '인원 유형별',
};

export function ProgramDetailPage() {
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [program, setProgram] = useState<Program | null>(null);
    const [quantity, setQuantity] = useState(1);
    const [sessionId, setSessionId] = useState('');
    const [passes, setPasses] = useState<MemberPass[]>([]);
    const [passId, setPassId] = useState('');
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.program(id).then(setProgram).catch(setError);
    }, [id]);

    useEffect(() => {
        if (user?.userType === 'MEMBER') {
            shopApi.usablePasses(id).then(setPasses).catch(() => setPasses([]));
        }
    }, [id, user?.userType]);

    const sessions = (program?.sessions ?? []).filter((s) => s.isOpen);
    const chosenSession = sessions.find((s) => String(s.id) === sessionId) ?? null;
    const remaining = chosenSession?.remaining ?? null;

    const reserve = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({programId: String(id), quantity: String(quantity)});
        if (sessionId) {
            query.set('sessionId', sessionId);
        }
        if (passId) {
            query.set('passId', passId);
        }
        navigate(`/reservations/new?${query}`);
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
                    <dd>{program.reservationType === 'TOUR'
                        ? (program.priceTiers ?? []).map((t) => `${t.name} ${won(t.price)}`).join(' · ')
                        : Number(program.price) === 0 ? '무료' : `${won(program.price)} / ${PRICE_UNIT[program.reservationType]}`}</dd>
                    {program.reservationType !== 'TOUR' && (
                        <>
                            <dt>{program.reservationType === 'EVENT' ? '공연 일시' : program.reservationType === 'PROGRAM' ? '진행 일정' : '예약 가능 기간'}</dt>
                            <dd>{formatDateTime(program.startDateTime)}{program.endDateTime && ` ~ ${formatDateTime(program.endDateTime)}`}</dd>
                        </>
                    )}
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
                        {sessions.length > 0 && (
                            <div className="field">
                                <label htmlFor="sessionId">수업 선택</label>
                                <select id="sessionId" required value={sessionId} onChange={(e) => {
                                    setSessionId(e.target.value);
                                    setQuantity(1);
                                }}>
                                    <option value="">선택</option>
                                    {sessions.map((s) => (
                                        <option key={s.id} value={s.id} disabled={s.remaining === 0}>
                                            {s.name ? `${s.name} · ` : ''}{formatDateTime(s.startAt)}
                                            {s.remaining != null && ` (잔여 ${s.remaining}명)`}{s.remaining === 0 && ' 마감'}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}
                        <Field label="인원" name="quantity" type="number" min={1} max={remaining ?? undefined} value={quantity}
                               onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                        {passes.length > 0 && (
                            <div className="field">
                                <label htmlFor="passId">이용권으로 신청</label>
                                <select id="passId" value={passId} onChange={(e) => setPassId(e.target.value)}>
                                    <option value="">결제로 신청</option>
                                    {passes.map((pass) => (
                                        <option key={pass.id} value={pass.id}>
                                            {pass.productName}{pass.unit === 'COUNT' ? ` (잔여 ${pass.remainingCount}회)` : ''}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}
                        <p className="price align-right">
                            결제 금액 {passId ? '이용권 사용 (0원)' : won(Number(program.price) * quantity)}
                        </p>
                        <ErrorBanner error={error}/>
                        <div className="toolbar form-actions">
                            <button type="submit" className="primary" disabled={sessions.length > 0 && (!chosenSession || (remaining != null && quantity > remaining))}>
                                신청하기
                            </button>
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
    {value: 'PREPAID', label: '지갑(적립금)'},
];

export function CheckoutPage() {
    const params = useParams();
    const type = params.type === 'RESERVATION' ? 'RESERVATION' : params.type === 'RESERVATION_EXTENSION' ? 'RESERVATION_EXTENSION' : 'ORDER';
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
        } else if (type === 'RESERVATION_EXTENSION') {
            shopApi.myReservations(0).then((page) => {
                const reservation = page.content.find((r) => r.pendingExtensionId === id);
                if (reservation) {
                    setTarget({
                        title: `${reservation.programName} 연장료 (${reservation.reservationNumber})`,
                        amount: Number(reservation.pendingExtensionFee ?? 0),
                        payable: true,
                    });
                }
            }).catch(setError);
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
    const [returns, setReturns] = useState<Map<number, OrderReturn>>(new Map());
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.myOrders(page).then(setData).catch(setError);
        shopApi.myReturnRequests(0).then((returnPage) => {
            const byOrder = new Map<number, OrderReturn>();
            returnPage.content.forEach((r) => byOrder.set(r.orderId, r));
            setReturns(byOrder);
        }).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>주문 내역</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>주문번호</th><th>상품</th><th>금액</th><th>상태</th><th>주문일</th><th>진행·메시지</th><th>결제</th><th>반품·교환</th><th>취소</th></tr></thead>
                    <tbody>
                    {data?.content.map((order) => (
                        <tr key={order.id}>
                            <td><code>{order.orderNumber}</code>
                                {order.pickupNumber != null && (
                                    <div className="small"><strong>{order.pickupNumber}번</strong> · {order.fulfillmentType === 'DINE_IN' ? `${order.tableName} 테이블` : '포장'}</div>
                                )}
                            </td>
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
                                {order.shipDate && <div className="muted small">출고 예정일 {formatDate(order.shipDate)}</div>}
                                {order.quoteMessage && <div className="muted small">{order.quoteMessage}</div>}
                            </td>
                            <td>
                                {orderStatus(order.status, order.customStatus)}
                                {order.trackingNumber && <div className="muted small">{order.courier} {order.trackingNumber}</div>}
                            </td>
                            <td>{formatDateTime(order.createdAt)}</td>
                            <td>
                                <details>
                                    <summary>보기</summary>
                                    <ProgressAndMessages
                                        getProgress={() => shopApi.progress('ORDER', order.id)}
                                        listMessages={() => shopApi.messages.list('ORDER', order.id)}
                                        postMessage={(content, fileIds) => shopApi.messages.post('ORDER', order.id, {content, fileIds})}
                                    />
                                </details>
                            </td>
                            <td className="button-cell">
                                {order.status === 'PENDING_PAYMENT' && <Link className="button primary" to={`/checkout/ORDER/${order.id}`}>결제</Link>}
                                {order.status === 'QUOTED' && (
                                    <button type="button" className="primary" onClick={() => {
                                        shopApi.acceptQuote(order.id).then(() => navigate(`/checkout/ORDER/${order.id}`)).catch(setError);
                                    }}>견적 수락</button>
                                )}
                            </td>
                            <td className="button-cell">
                                {order.saleType === 'PURCHASE' && ['PAID', 'SHIPPING', 'PREPARING', 'READY', 'DELIVERED', 'COMPLETED'].includes(order.status) && (
                                    <ReturnCell order={order} existing={returns.get(order.id)} onDone={reload}/>
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
                    {data?.content.length === 0 && <tr><td colSpan={9} className="muted">주문 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function ReturnCell({order, existing, onDone}: { order: Order; existing: OrderReturn | undefined; onDone: () => void }) {
    const [open, setOpen] = useState(false);
    const [type, setType] = useState<OrderReturnType>('RETURN');
    const [reason, setReason] = useState('');
    const [exchangeOptionId, setExchangeOptionId] = useState('');
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (open && type === 'EXCHANGE' && !product) {
            shopApi.product(order.productId).then(setProduct).catch(setError);
        }
    }, [open, type, product, order.productId]);

    if (existing && existing.status !== 'REJECTED') {
        return (
            <span className="badge">{RETURN_TYPE[existing.type]} {RETURN_STATUS[existing.status]}</span>
        );
    }
    if (!open) {
        return <button type="button" onClick={() => setOpen(true)}>반품·교환 신청</button>;
    }
    return (
        <div className="small">
            {existing?.status === 'REJECTED' && <div className="muted">지난 신청 거절됨{existing.adminMemo ? `: ${existing.adminMemo}` : ''}</div>}
            <select value={type} onChange={(e) => setType(e.target.value as OrderReturnType)}>
                <option value="RETURN">반품</option>
                <option value="EXCHANGE">교환</option>
            </select>
            {type === 'EXCHANGE' && (
                <select value={exchangeOptionId} onChange={(e) => setExchangeOptionId(e.target.value)}>
                    <option value="">교환할 옵션</option>
                    {product?.options.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
                </select>
            )}
            <input value={reason} onChange={(e) => setReason(e.target.value)} placeholder="신청 이유"/>
            <button type="button" className="primary" disabled={submitting || !reason.trim() || (type === 'EXCHANGE' && !exchangeOptionId)}
                    onClick={() => {
                        setSubmitting(true);
                        shopApi.requestReturn(order.id, type, reason.trim(), type === 'EXCHANGE' ? Number(exchangeOptionId) : null)
                            .then(() => {
                                setOpen(false);
                                onDone();
                            }).catch(setError).finally(() => setSubmitting(false));
                    }}>신청하기</button>
            <ErrorBanner error={error}/>
        </div>
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
                    <thead><tr><th>예약번호</th><th>예약 상품</th><th>이용</th><th>수량</th><th>금액</th><th>상태</th><th>체크인</th><th>결제</th><th>연장</th><th>취소</th></tr></thead>
                    <tbody>
                    {data?.content.map((reservation) => (
                        <tr key={reservation.id}>
                            <td><code>{reservation.reservationNumber}</code></td>
                            <td><Link to={`/programs/${reservation.programId}`}>{reservation.programName}</Link></td>
                            <td>{reservationUsage(reservation)}</td>
                            <td>{reservation.quantity}</td>
                            <td>{won(reservation.totalPrice)}
                                {reservation.depositAmount != null && Number(reservation.depositAmount) > 0 && (
                                    <div className="muted small">보증금 {won(reservation.depositAmount)} 포함</div>
                                )}
                            </td>
                            <td>{reservationStatus(reservation.status, reservation.customStatus)}
                                {reservation.overdue && <div><span className="badge danger">반납 {reservation.lateDays}일 지연</span></div>}
                                {reservation.reservationType === 'VISIT' && (
                                    <div className="muted small">
                                        {reservation.workCompletedAt
                                            ? `${formatDate(reservation.workCompletedAt)} 작업 완료${reservation.workReport ? ` · ${reservation.workReport}` : ''}`
                                            : reservation.workerName
                                                ? `작업자 ${reservation.workerName}${reservation.workerPhone ? ` (${reservation.workerPhone})` : ''}`
                                                : '작업자 배정 전'}
                                    </div>
                                )}
                                {reservation.workPhotoUrls.length > 0 && (
                                    <div className="work-photos">
                                        {reservation.workPhotoUrls.map((url, i) => <a key={url} href={url}><img src={url} alt={`작업 사진 ${i + 1}`}/></a>)}
                                    </div>
                                )}
                                {reservation.returnedAt && (
                                    <div className="muted small">
                                        {formatDate(reservation.returnedAt)} 반납 · 보증금 {won(reservation.depositRefunded ?? 0)} 환불
                                        {reservation.returnMemo && ` (${reservation.returnMemo})`}
                                    </div>
                                )}
                            </td>
                            <td>
                                {reservation.checkedInAt ? (
                                    <>
                                        <span className="badge ok">{formatDateTime(reservation.checkedInAt)} 체크인</span>
                                        {reservation.queueNumber != null && (
                                            <div className="muted small">대기 {reservation.queueNumber}번 · {QUEUE_STATUS[reservation.queueStatus ?? 'WAITING']}</div>
                                        )}
                                    </>
                                ) : reservation.status === 'CONFIRMED' && reservation.checkInCode && (
                                    <details>
                                        <summary>체크인 코드</summary>
                                        <img alt="체크인 QR 코드" width={120} height={120}
                                             src={`https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${reservation.checkInCode}`}/>
                                        <div><code>{reservation.checkInCode}</code></div>
                                    </details>
                                )}
                            </td>
                            <td className="button-cell">
                                {reservation.status === 'PENDING_PAYMENT' && (
                                    <Link className="button primary" to={`/checkout/RESERVATION/${reservation.id}`}>결제</Link>
                                )}
                            </td>
                            <td className="button-cell">
                                {reservation.reservationType === 'ITEM_RENTAL' && reservation.status === 'CONFIRMED' && !reservation.returnedAt && (
                                    <ExtensionCell reservation={reservation} onDone={reload}/>
                                )}
                            </td>
                            <td className="button-cell">
                                {reservation.status !== 'CANCELED' && !reservation.returnedAt && !reservation.workCompletedAt && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('예약을 취소할까요?')) {
                                            shopApi.cancelReservation(reservation.id).then(reload).catch(setError);
                                        }
                                    }}>취소</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={9} className="muted">예약 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function ExtensionCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const [newEndAt, setNewEndAt] = useState('');
    const [error, setError] = useState<unknown>(null);

    if (reservation.pendingExtensionId) {
        return (
            <div className="small">
                <div>연장료 {won(reservation.pendingExtensionFee ?? 0)}</div>
                <Link className="button primary" to={`/checkout/RESERVATION_EXTENSION/${reservation.pendingExtensionId}`}>결제</Link>
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm('연장 신청을 취소할까요?')) {
                        shopApi.cancelExtension(reservation.id).then(onDone).catch(setError);
                    }
                }}>연장 취소</button>
                <ErrorBanner error={error}/>
            </div>
        );
    }
    return (
        <div className="small">
            <input type="date" aria-label="새 반납일" min={dateOf(reservation.endAt)} value={newEndAt}
                   onChange={(e) => setNewEndAt(e.target.value)}/>
            <button type="button" disabled={!newEndAt} onClick={() => {
                shopApi.extendReservation(reservation.id, `${newEndAt}T00:00:00`).then(onDone).catch(setError);
            }}>연장 신청</button>
            {reservation.unpaidExtensionFee != null && Number(reservation.unpaidExtensionFee) > 0 && (
                <div className="muted">미결제 연장료 {won(reservation.unpaidExtensionFee)}</div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

const dateOf = (value: string | null) => (value ? value.slice(0, 10) : '');

export function MyPassesPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MemberPass> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myPasses(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>이용권</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>이용권</th><th>단위</th><th>남은 횟수</th><th>만료일</th><th>상태</th></tr></thead>
                    <tbody>
                    {data?.content.map((pass) => (
                        <tr key={pass.id}>
                            <td>{pass.productName}</td>
                            <td>{PASS_UNIT[pass.unit]}</td>
                            <td>{pass.unit === 'COUNT' ? `${pass.remainingCount ?? 0}/${pass.totalCount ?? 0}` : '-'}</td>
                            <td>{pass.expiresAt ? formatDate(pass.expiresAt) : '-'}</td>
                            <td>
                                {pass.status === 'ACTIVE' && <span className="badge ok">사용 가능</span>}
                                {pass.status === 'USED_UP' && <span className="badge">다 씀</span>}
                                {pass.status === 'CANCELED' && <span className="badge">취소</span>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">이용권이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyLicenseKeysPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyLicenseKey> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myLicenseKeys(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>라이선스 키</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>상품</th><th>키</th><th>받은 날</th></tr></thead>
                    <tbody>
                    {data?.content.map((key) => (
                        <tr key={key.id}>
                            <td>{key.productName}</td>
                            <td><code>{key.code}</code></td>
                            <td>{key.assignedAt ? formatDateTime(key.assignedAt) : '-'}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={3} className="muted">받은 라이선스 키가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyWalletPage() {
    const [balance, setBalance] = useState(0);
    const [ledgerPage, setLedgerPage] = useState(0);
    const [ledger, setLedger] = useState<Page<WalletLedgerEntry> | null>(null);
    const [codesPage, setCodesPage] = useState(0);
    const [codes, setCodes] = useState<Page<GiftCard> | null>(null);
    const [issueAmount, setIssueAmount] = useState('');
    const [redeemCode, setRedeemCode] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    const reload = useCallback(() => {
        shopApi.wallet.balance().then(setBalance).catch(setError);
        shopApi.wallet.ledger(ledgerPage).then(setLedger).catch(setError);
        shopApi.wallet.myGiftCodes(codesPage).then(setCodes).catch(setError);
    }, [ledgerPage, codesPage]);
    useEffect(reload, [reload]);

    const issueCode = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            await shopApi.wallet.issueGiftCode(Number(issueAmount));
            setIssueAmount('');
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    const redeem = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            await shopApi.wallet.redeem(redeemCode);
            setRedeemCode('');
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>지갑</h1>
            <ErrorBanner error={error}/>
            <p className="price">{won(balance)}</p>

            <h2>상품권 코드로 충전</h2>
            <form onSubmit={redeem} className="inline-form">
                <input value={redeemCode} onChange={(e) => setRedeemCode(e.target.value)} placeholder="상품권 코드" required/>
                <button type="submit" className="primary" disabled={busy}>충전하기</button>
            </form>

            <h2>상품권 발급 (잔액에서 차감)</h2>
            <form onSubmit={issueCode} className="inline-form">
                <input type="number" min={1} value={issueAmount} onChange={(e) => setIssueAmount(e.target.value)} placeholder="금액(원)" required/>
                <button type="submit" className="primary" disabled={busy}>발급하기</button>
            </form>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>코드</th><th>금액</th><th>상태</th><th>발급일</th></tr></thead>
                    <tbody>
                    {codes?.content.map((card) => (
                        <tr key={card.id}>
                            <td><code>{card.code}</code></td>
                            <td>{won(card.amount)}</td>
                            <td>{card.status === 'UNUSED' ? <span className="badge ok">미사용</span> : <span className="badge">사용됨</span>}</td>
                            <td>{formatDateTime(card.createdAt)}</td>
                        </tr>
                    ))}
                    {codes?.content.length === 0 && <tr><td colSpan={4} className="muted">발급한 상품권이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {codes && <Pagination page={codesPage} totalPages={codes.page.totalPages} onChange={setCodesPage}/>}

            <h2>이용 내역</h2>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>구분</th><th>금액</th><th>잔액</th><th>메모</th><th>일시</th></tr></thead>
                    <tbody>
                    {ledger?.content.map((entry) => (
                        <tr key={entry.id}>
                            <td>{LEDGER_TYPE[entry.type]}</td>
                            <td>{won(entry.amount)}</td>
                            <td>{won(entry.balanceAfter)}</td>
                            <td>{entry.memo ?? '-'}</td>
                            <td>{formatDateTime(entry.createdAt)}</td>
                        </tr>
                    ))}
                    {ledger?.content.length === 0 && <tr><td colSpan={5} className="muted">내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {ledger && <Pagination page={ledgerPage} totalPages={ledger.page.totalPages} onChange={setLedgerPage}/>}
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
                    <thead><tr><th>상품</th><th>금액</th><th>상태</th><th>다음 결제일</th><th>시작일</th><th>관리</th></tr></thead>
                    <tbody>
                    {data?.content.map((item) => (
                        <tr key={item.id}>
                            <td><Link to={`/products/${item.productId}`}>{item.productName}</Link></td>
                            <td>{BILLING_CYCLE[item.billingCycle]} {won(item.price)}</td>
                            <td>
                                {item.status === 'ACTIVE' && <span className="badge ok">구독 중</span>}
                                {item.status === 'PAUSED' && <span className="badge">일시정지{item.resumeOn ? ` (${item.resumeOn}에 재개)` : ''}</span>}
                                {item.status === 'CANCELED' && <span className="badge">해지</span>}
                                {item.pendingProductId && <div className="muted small">다음 결제부터 {item.pendingProductName}로 변경</div>}
                                {item.commitmentEndDate && item.status !== 'CANCELED' && (
                                    <div className="muted small">
                                        {formatDate(item.commitmentEndDate)}까지 약정
                                        {item.earlyTerminationFee > 0 && ` (지금 해지하면 위약금 ${won(item.earlyTerminationFee)})`}
                                    </div>
                                )}
                            </td>
                            <td>{item.nextBillingDate ?? '-'}</td>
                            <td>{formatDateTime(item.startedAt)}</td>
                            <td className="button-cell">
                                <Link to={`/mypage/attendance/${item.id}`}>출결 보기</Link>
                                {item.status !== 'CANCELED' && <SubscriptionActions subscription={item} onDone={reload}/>}
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

function SubscriptionActions({subscription, onDone}: { subscription: Subscription; onDone: () => void }) {
    const [resumeOn, setResumeOn] = useState('');
    const [changing, setChanging] = useState(false);
    const [plans, setPlans] = useState<ProductSummary[] | null>(null);
    const [error, setError] = useState<unknown>(null);

    const run = (pending: Promise<unknown>) => {
        setError(null);
        pending.then(onDone).catch(setError);
    };

    const openPlans = () => {
        setChanging(true);
        shopApi.subscriptionPlans(subscription.id).then(setPlans).catch(setError);
    };

    return (
        <div className="review-cell">
            {subscription.status === 'ACTIVE' ? (
                <>
                    <input type="date" aria-label="재개일" value={resumeOn} onChange={(e) => setResumeOn(e.target.value)}/>
                    <button type="button" onClick={() => run(shopApi.pauseSubscription(subscription.id, resumeOn || null))}>
                        일시정지
                    </button>
                </>
            ) : (
                <button type="button" onClick={() => run(shopApi.resumeSubscription(subscription.id))}>재개</button>
            )}
            {subscription.pendingProductId ? (
                <button type="button" onClick={() => run(shopApi.cancelSubscriptionPlanChange(subscription.id))}>변경 취소</button>
            ) : (
                <button type="button" onClick={openPlans}>상품 변경</button>
            )}
            <button type="button" className="danger" onClick={() => {
                const message = subscription.earlyTerminationFee > 0
                    ? `구독을 해지할까요? 약정 기간이 남아 위약금 ${won(subscription.earlyTerminationFee)}이 바로 결제됩니다.`
                    : '구독을 해지할까요? 다음 결제일부터 결제되지 않습니다.';
                if (window.confirm(message)) {
                    run(shopApi.cancelSubscription(subscription.id));
                }
            }}>해지</button>
            {changing && plans && (
                <div className="dropdown-panel">
                    {plans.length === 0 && <p className="muted small">변경할 수 있는 다른 상품이 없습니다.</p>}
                    {plans.map((plan) => (
                        <button key={plan.id} type="button" className="link-button" onClick={() => {
                            setChanging(false);
                            run(shopApi.changeSubscriptionPlan(subscription.id, plan.id));
                        }}>
                            {plan.name} · {BILLING_CYCLE[plan.billingCycle ?? 'MONTHLY']} {won(plan.price)}
                        </button>
                    ))}
                    <button type="button" className="link-button" onClick={() => setChanging(false)}>닫기</button>
                </div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}
