import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import {useI18n} from '../../api/i18n';
import type {ExchangeRate, GiftCard, MemberPass, MyLicenseKey, Order, OrderReturn, OrderReturnType, PaymentMethod, ProductDetail, ProductSaleType, ProductSummary, ReservationItem, Reservation, ReservationType, Review, ReviewTargetType, Subscription, TaxSetting, WalletLedgerEntry} from '../../api/domain';
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
import {billingCycle, ledgerType, orderStatus as orderStatusLabels, passUnit, queueStatus, reservationStatus as reservationStatusLabels, reservationType, returnStatus, returnType, saleType} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {PriceNotice} from '../../components/PriceNotice';
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
    const {t} = useI18n();
    return (
        <Link to={`/products/${product.id}`} className="product-card">
            <Thumbnail src={product.imageUrl} name={product.name}/>
            <strong className="product-name">{product.name}</strong>
            {product.description && <span className="product-summary muted small">{product.description}</span>}
            <span className="product-price">{priceLabel(product, t)}</span>
            {product.saleType !== 'PURCHASE' && <span className="badge">{saleType(t)[product.saleType]}</span>}
            {product.availableQuantity === 0 && <span className="badge danger">{t('shop.product.soldOut', '품절')}</span>}
            <GradeBadge gradeId={product.accessGradeId}/>
        </Link>
    );
}

export function ReservationItemCard({item}: { item: ReservationItem }) {
    const {t} = useI18n();
    return (
        <Link to={`/reservation-items/${item.id}`} className="product-card">
            <Thumbnail src={item.imageUrl} name={item.name}/>
            <strong className="product-name">{item.name}</strong>
            <span className="badge">{reservationType(t)[item.reservationType]}</span>
            {item.startDateTime && <span className="muted small">{formatDateTime(item.startDateTime)}</span>}
            <span className="product-price">{Number(item.price) === 0 ? t('shop.common.free', '무료') : won(item.price)}</span>
            {item.isOpen ? <span className="badge ok">{t('shop.reservationItem.open', '접수 중')}</span> : <span className="badge">{t('shop.reservationItem.closed', '접수 마감')}</span>}
            <GradeBadge gradeId={item.accessGradeId}/>
        </Link>
    );
}

export function ProductListPage() {
    const {t} = useI18n();
    const productTitle = useFeatureMenuName('PRODUCT', t('shop.product.title', '상품'));
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
            <ListSearch label={t('shop.product.searchLabel', '상품 검색')} placeholder={t('shop.product.searchPlaceholder', '상품명 검색')} keepFilters/>
            {categories.length > 0 && <CategoryFilters categories={categories}/>}
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((product) => <ProductCard key={product.id} product={product}/>)}
            </div>
            {data?.content.length === 0 && (
                <p className="muted">{keyword || filtered ? t('shop.product.noMatch', '조건에 맞는 상품이 없습니다.') : t('shop.product.noneOnSale', '판매 중인 상품이 없습니다.')}</p>
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

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const actionLabel = (t: T): Record<ProductSaleType, string> => ({
    PURCHASE: t('shop.action.purchase', '주문하기'),
    SUBSCRIPTION: t('shop.action.subscribe', '구독하기'),
    DONATION: t('shop.action.donate', '후원하기'),
    QUOTE: t('shop.action.quote', '견적 문의하기'),
    PASS: t('shop.action.buyPass', '구매하기'),
    GIFT_CARD: t('shop.action.buyGiftCard', '구매하기'),
    PREORDER: t('shop.action.preorder', '예약 주문하기'),
    FUNDING: t('shop.action.fund', '펀딩 참여하기'),
    INQUIRY: t('shop.action.inquiry', '상담 예약하기'),
});

function DonationProgressBar({product}: { product: ProductDetail }) {
    const {t} = useI18n();
    const raised = Number(product.donation?.raisedAmount ?? 0);
    const goal = product.goalAmount ? Number(product.goalAmount) : null;
    return (
        <div className="donation-progress">
            {goal && <progress value={Math.min(raised, goal)} max={goal} aria-label={t('shop.donation.progressLabel', '모금 달성률')}/>}
            <p>
                <strong>{won(raised)}</strong> {t('shop.donation.raised', '모금')}
                {goal && <> · {t('shop.donation.goal', '목표 {amount} ({percent}%)', {amount: won(goal), percent: String(Math.floor(raised * 100 / goal))})}</>}
                <span className="muted small"> · {t('shop.donation.supporterCount', '후원자 {count}명', {count: String(product.donation?.supporterCount ?? 0)})}</span>
            </p>
        </div>
    );
}

function FundingProgressBar({product}: { product: ProductDetail }) {
    const {t} = useI18n();
    const funding = product.funding;
    if (!funding) {
        return null;
    }
    const raised = Number(funding.raisedAmount);
    const goal = funding.goalAmount ? Number(funding.goalAmount) : null;
    return (
        <div className="donation-progress">
            {goal && <progress value={Math.min(raised, goal)} max={goal} aria-label={t('shop.funding.progressLabel', '펀딩 달성률')}/>}
            <p>
                <strong>{won(raised)}</strong> {t('shop.funding.raised', '펀딩')}
                {goal && <> · {t('shop.donation.goal', '목표 {amount} ({percent}%)', {amount: won(goal), percent: String(Math.floor(raised * 100 / goal))})}</>}
                <span className="muted small"> · {t('shop.funding.supporterCount', '참여자 {count}명', {count: String(funding.supporterCount)})}</span>
            </p>
            <p className="notice-box">
                {funding.deadline && t('shop.funding.deadline', '마감 {date}', {date: formatDateTime(funding.deadline)})}
                {funding.resolved && (funding.succeeded ? ` · ${t('shop.funding.succeeded', '목표를 달성했습니다.')}` : ` · ${t('shop.funding.failed', '목표를 못 채워 모든 결제가 환불되었습니다.')}`)}
            </p>
        </div>
    );
}

export function ProductDetailPage() {
    const {t} = useI18n();
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
            setCartNotice(t('shop.product.addedToCart', '장바구니에 담았습니다.'));
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
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
                    <p className="price">{priceLabel(product, t)}</p>
                    {product.yourPrice !== product.price && (
                        <p className="notice-box">{t('shop.product.yourPrice', '거래처 가격:')} <strong>{won(product.yourPrice)}</strong></p>
                    )}
                    {product.donation && <DonationProgressBar product={product}/>}
                    {product.funding && <FundingProgressBar product={product}/>}
                    {product.saleType === 'SUBSCRIPTION' && product.commitmentMonths && (
                        <p className="notice-box">
                            {t('shop.product.commitment', '{months}개월 약정 · 약정 중 해지하면 남은 기간 금액의 {rate}%를 위약금으로 청구합니다.',
                                {months: String(product.commitmentMonths), rate: String(product.earlyTerminationFeeRate)})}
                        </p>
                    )}
                    {product.preorder && (
                        <p className="notice-box">
                            {t('shop.product.preorder', '예약 판매')}
                            {product.preorder.saleStartAt && t('shop.product.preorder.saleStart', ' · 판매 시작 {date}', {date: formatDateTime(product.preorder.saleStartAt)})}
                            {product.preorder.saleEndAt && t('shop.product.preorder.saleEnd', ' · 판매 종료 {date}', {date: formatDateTime(product.preorder.saleEndAt)})}
                            {product.preorder.shipDate && t('shop.product.preorder.shipDate', ' · 출고 예정일 {date}', {date: formatDate(product.preorder.shipDate)})}
                        </p>
                    )}
                    {product.averageRating != null && (
                        <p>{t('shop.product.rating', '평점 {stars} {score}', {stars: '★'.repeat(Math.round(product.averageRating)), score: product.averageRating.toFixed(1)})}</p>
                    )}
                    {product.saleType === 'INQUIRY' && (
                        <p className="notice-box">{t('shop.product.inquiryNotice', '결제 없이 상담 예약으로 문의할 수 있는 상품입니다.')}</p>
                    )}
                    {product.saleType !== 'DONATION' && product.saleType !== 'INQUIRY' && (
                        <p className="muted small">{t('shop.product.stock', '재고 {amount}', {amount: product.availableQuantity != null ? String(product.availableQuantity) : t('shop.product.stockSufficient', '충분')})}</p>
                    )}
                    {product.specs.length > 0 && (
                        <table className="spec-table">
                            <caption>{t('shop.product.specs', '상품 사양')}</caption>
                            <tbody>
                            {product.specs.map((spec) => (
                                <tr key={spec.key}><th scope="row">{spec.label}</th><td>{String(spec.value)}{spec.unit ? ` ${spec.unit}` : ''}</td></tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                    {product.digitalFileCount > 0 && (
                        <p className="notice-box">
                            {t('shop.product.digitalFiles', '결제하면 마이페이지 내려받기에서 파일 {count}개를 받을 수 있습니다.', {count: String(product.digitalFileCount)})}
                            {product.downloadLimit != null && t('shop.product.downloadLimit', ' 최대 {count}회', {count: String(product.downloadLimit)})}
                            {product.downloadDays != null && (product.downloadLimit != null ? ',' : '') + t('shop.product.downloadDays', ' 결제 후 {days}일 이내', {days: String(product.downloadDays)})}
                            {(product.downloadLimit != null || product.downloadDays != null) && ` ${t('shop.product.canReceive', '받을 수 있습니다.')}`}
                        </p>
                    )}
                    {product.saleType === 'INQUIRY' ? (
                        user?.userType === 'MEMBER' ? (
                            <Link className="button primary" to={`/reservations/new?reservationItemId=${product.inquiry?.reservationItemId}`}>
                                {actionLabel(t).INQUIRY}
                            </Link>
                        ) : (
                            <p className="muted"><Link to="/login" state={{from: `/products/${id}`}}>{t('shop.common.login', '로그인')}</Link> {t('shop.product.loginToConsult', '후 상담을 예약할 수 있습니다.')}</p>
                        )
                    ) : user?.userType === 'MEMBER' ? (
                        <form onSubmit={order}>
                            {product.saleType === 'DONATION' && (
                                <div className="field">
                                    <label htmlFor="amount">{t('shop.product.donationAmount', '후원 금액(원)')}</label>
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
                                    <p className="muted small">{t('shop.product.donationMin', '{amount} 이상 후원할 수 있습니다.', {amount: won(product.price)})}</p>
                                </div>
                            )}
                            {product.saleType !== 'DONATION' && (
                                <OptionPicker product={product} optionId={optionId} onChange={setOptionId}/>
                            )}
                            {product.saleType !== 'DONATION' && product.saleType !== 'QUOTE' && product.discounts.length > 0 && (
                                <div className="field">
                                    <label htmlFor="discount">{t('shop.product.discount', '할인')}</label>
                                    <select id="discount" value={discountId} onChange={(e) => setDiscountId(e.target.value)}>
                                        <option value="">{t('shop.product.discountNone', '적용 안 함')}</option>
                                        {product.discounts.map((d) => <option key={d.id} value={d.id}>{d.name} ({d.rate}%)</option>)}
                                    </select>
                                </div>
                            )}
                            {product.saleType !== 'DONATION' && (
                                <Field label={t('shop.product.quantity', '수량')} name="quantity" type="number" min={1} value={quantity}
                                       onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                            )}
                            {product.saleType === 'PURCHASE' && <p className="price align-right">{t('shop.product.estimatedAmount', '예상 결제 금액 {amount}', {amount: won(estimate)})}</p>}
                            {product.saleType === 'SUBSCRIPTION' && product.billingCycle && (
                                <p className="price align-right">{t('shop.product.subscriptionAmount', '{cycle} {amount} 결제', {cycle: billingCycle(t)[product.billingCycle], amount: won(estimate)})}</p>
                            )}
                            <ErrorBanner error={error}/>
                            {cartNotice && <p className="ok">{cartNotice} <Link to="/cart">{t('shop.product.viewCart', '장바구니 보기')}</Link></p>}
                            <div className="toolbar form-actions">
                                {product.saleType === 'PURCHASE' && (
                                    <button type="button" disabled={soldOut || needsOption} onClick={addToCart}>{t('shop.product.addToCart', '장바구니 담기')}</button>
                                )}
                                <button type="submit" className="primary" disabled={unavailable || needsOption}>
                                    <StableLabel current={soldOut && product.saleType !== 'DONATION' ? t('shop.product.soldOut', '품절')
                                        : preorderClosed ? t('shop.product.notOnSalePeriod', '판매 기간이 아닙니다')
                                            : fundingClosed ? t('shop.product.fundingClosed', '펀딩이 마감되었습니다') : actionLabel(t)[product.saleType ?? 'PURCHASE']}
                                                 labels={[t('shop.product.soldOut', '품절'), t('shop.product.notOnSalePeriod', '판매 기간이 아닙니다'),
                                                     t('shop.product.fundingClosed', '펀딩이 마감되었습니다'), ...Object.values(actionLabel(t))]}/>
                                </button>
                            </div>
                        </form>
                    ) : (
                        <p className="muted"><Link to="/login" state={{from: `/products/${id}`}}>{t('shop.common.login', '로그인')}</Link> {t('shop.product.loginToOrder', '후 주문할 수 있습니다.')}</p>
                    )}
                </div>
            </section>
            <section className="card">
                <h2>{t('shop.product.detailInfo', '상세 정보')}</h2>
                <HtmlView html={product.contentHtml}/>
            </section>
            <Reviews target="PRODUCT" targetId={id}/>
        </>
    );
}

const reviewTargets = (t: T) => ({
    PRODUCT: {
        load: shopApi.reviews,
        add: shopApi.addReview,
        hint: t('shop.review.hint.product', '구매한 상품에만 리뷰를 남길 수 있습니다.'),
    },
    RESERVATION_ITEM: {
        load: shopApi.reservationItemReviews,
        add: shopApi.addReservationItemReview,
        hint: t('shop.review.hint.reservationItem', '예약이 확정된 예약 상품에만 리뷰를 남길 수 있습니다.'),
    },
});

function Reviews({target, targetId}: { target: ReviewTargetType; targetId: number }) {
    const {t} = useI18n();
    const {load, add, hint} = reviewTargets(t)[target];
    const {user} = useAuth();
    const [reviews, setReviews] = useState<Page<Review> | null>(null);
    const [form, setForm] = useState({rating: 5, title: '', contentHtml: ''});
    const [minimumContentLength, setMinimumContentLength] = useState(0);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        load(targetId, 0).then(setReviews).catch(setError);
    }, [load, targetId]);
    useEffect(reload, [reload]);
    useEffect(() => {
        shopApi.reviewPolicy().then((policy) => setMinimumContentLength(policy.minimumContentLength)).catch(() => undefined);
    }, []);

    return (
        <section className="card">
            <h2>{t('shop.review.title', '리뷰 {count}', {count: String(reviews?.page.totalElements ?? 0)})}</h2>
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
                                {t('common.delete', '삭제')}
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
                        <label htmlFor="review-rating">{t('shop.review.rating', '평점')}</label>
                        <select id="review-rating" value={form.rating} onChange={(e) => setForm({...form, rating: Number(e.target.value)})}>
                            {[5, 4, 3, 2, 1].map((r) => <option key={r} value={r}>{'★'.repeat(r)}</option>)}
                        </select>
                    </div>
                    <Field label={t('shop.review.titleField', '리뷰 제목')} name="reviewTitle" required value={form.title}
                           onChange={(e) => setForm({...form, title: e.target.value})}/>
                    <div className="field">
                        <label htmlFor="review-content">{t('shop.review.content', '리뷰 내용')}</label>
                        <textarea id="review-content" rows={3} value={form.contentHtml} required={minimumContentLength > 0}
                                  minLength={minimumContentLength || undefined}
                                  onChange={(e) => setForm({...form, contentHtml: e.target.value})}/>
                        {minimumContentLength > 0 && <p className="muted small">{t('shop.review.minLength', '최소 {count}자 이상 입력해 주세요.', {count: String(minimumContentLength)})}</p>}
                    </div>
                    <div className="toolbar form-actions">
                        <button type="submit" className="primary">{t('shop.review.submit', '리뷰 등록')}</button>
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

export function ReservationItemListPage() {
    const {t} = useI18n();
    const title = useFeatureMenuName('PROGRAM', t('shop.reservationItem.title', '예약 상품'));
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<ReservationItem> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.reservationItems(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{title}</h1>
            <ListSearch label={t('shop.reservationItem.searchLabel', '예약 상품 검색')} placeholder={t('shop.reservationItem.searchPlaceholder', '예약 상품명 검색')}/>
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((item) => <ReservationItemCard key={item.id} item={item}/>)}
            </div>
            {data?.content.length === 0 && <p className="muted">{keyword ? t('shop.common.noSearchResults', '검색 결과가 없습니다.') : t('shop.reservationItem.noneAvailable', '신청 가능한 예약 상품이 없습니다.')}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

const priceUnit = (t: T): Record<ReservationType, string> => ({
    PROGRAM: t('shop.priceUnit.program', '1인'),
    CONSULTATION: t('shop.priceUnit.consultation', '1회'),
    RENTAL: t('shop.priceUnit.rental', '칸·예약 단위'),
    LODGING: t('shop.priceUnit.lodging', '칸·1박'),
    EVENT: t('shop.priceUnit.event', '좌석'),
    ITEM_RENTAL: t('shop.priceUnit.itemRental', '1개·1일'),
    VISIT: t('shop.priceUnit.visit', '1회'),
    TABLE: t('shop.priceUnit.table', '칸·예약 단위'),
    TOUR: t('shop.priceUnit.tour', '인원 유형별'),
});

export function ReservationItemDetailPage() {
    const {t} = useI18n();
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [item, setItem] = useState<ReservationItem | null>(null);
    const [quantity, setQuantity] = useState(1);
    const [sessionId, setSessionId] = useState('');
    const [passes, setPasses] = useState<MemberPass[]>([]);
    const [passId, setPassId] = useState('');
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        shopApi.reservationItem(id).then(setItem).catch(setError);
    }, [id]);

    useEffect(() => {
        if (user?.userType === 'MEMBER') {
            shopApi.usablePasses(id).then(setPasses).catch(() => setPasses([]));
        }
    }, [id, user?.userType]);

    const sessions = (item?.sessions ?? []).filter((s) => s.isOpen);
    const chosenSession = sessions.find((s) => String(s.id) === sessionId) ?? null;
    const remaining = chosenSession?.remaining ?? null;

    const reserve = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({reservationItemId: String(id), quantity: String(quantity)});
        if (sessionId) {
            query.set('sessionId', sessionId);
        }
        if (passId) {
            query.set('passId', passId);
        }
        navigate(`/reservations/new?${query}`);
    };

    if (error && !item) {
        return <ErrorBanner error={error}/>;
    }
    if (!item) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            <section className="card product-detail">
                <div className="product-media">
                    <Thumbnail src={item.imageUrl} name={item.name}/>
                </div>
                <div>
                <h1>{item.name} <GradeBadge gradeId={item.accessGradeId}/></h1>
                {item.description && <p className="muted">{item.description}</p>}
                <dl className="meta">
                    <dt>{t('shop.reservationItem.type', '유형')}</dt>
                    <dd>{reservationType(t)[item.reservationType]}</dd>
                    <dt>{t('shop.reservationItem.price', '가격')}</dt>
                    <dd>{item.reservationType === 'TOUR'
                        ? (item.priceTiers ?? []).map((tier) => `${tier.name} ${won(tier.price)}`).join(' · ')
                        : Number(item.price) === 0 ? t('shop.common.free', '무료') : `${won(item.price)} / ${priceUnit(t)[item.reservationType]}`}</dd>
                    {item.reservationType !== 'TOUR' && (
                        <>
                            <dt>{item.reservationType === 'EVENT' ? t('shop.reservationItem.eventDateTime', '공연 일시')
                                : item.reservationType === 'PROGRAM' ? t('shop.reservationItem.schedule', '진행 일정') : t('shop.reservationItem.availablePeriod', '예약 가능 기간')}</dt>
                            <dd>{formatDateTime(item.startDateTime)}{item.endDateTime && ` ~ ${formatDateTime(item.endDateTime)}`}</dd>
                        </>
                    )}
                    <dt>{t('shop.reservationItem.applicationPeriod', '신청 기간')}</dt>
                    <dd>{formatDateTime(item.applicationStartDateTime)} ~ {formatDateTime(item.applicationEndDateTime)}</dd>
                    {item.averageRating != null && (
                        <>
                            <dt>{t('shop.review.rating', '평점')}</dt>
                            <dd>{'★'.repeat(Math.round(item.averageRating))} {item.averageRating.toFixed(1)}</dd>
                        </>
                    )}
                    {item.reservationType === 'PROGRAM' && (
                        <>
                            <dt>{t('shop.reservationItem.remainingPerson', '잔여 인원')}</dt>
                            <dd>{item.maximumPerson ? t('shop.reservationItem.remainingOf', '{available} / {max}명', {available: String(item.availablePerson), max: String(item.maximumPerson)}) : t('admin.reservationItem.noLimit', '제한 없음')}</dd>
                        </>
                    )}
                </dl>
                {!item.isOpen ? (
                    <p className="badge">{t('shop.reservationItem.notOpenNow', '지금은 신청할 수 없습니다.')}</p>
                ) : user?.userType === 'MEMBER' && item.reservationType !== 'PROGRAM' ? (
                    <ReservationPicker item={item}/>
                ) : user?.userType === 'MEMBER' ? (
                    <form onSubmit={reserve}>
                        {sessions.length > 0 && (
                            <div className="field">
                                <label htmlFor="sessionId">{t('shop.reservationItem.chooseClass', '수업 선택')}</label>
                                <select id="sessionId" required value={sessionId} onChange={(e) => {
                                    setSessionId(e.target.value);
                                    setQuantity(1);
                                }}>
                                    <option value="">{t('shop.common.select', '선택')}</option>
                                    {sessions.map((s) => (
                                        <option key={s.id} value={s.id} disabled={s.remaining === 0}>
                                            {s.name ? `${s.name} · ` : ''}{formatDateTime(s.startAt)}
                                            {s.remaining != null && t('shop.reservationItem.remainingPeople', ' (잔여 {count}명)', {count: String(s.remaining)})}
                                            {s.remaining === 0 && ` ${t('shop.reservationItem.full', '마감')}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}
                        <Field label={t('shop.reservationItem.partySize', '인원')} name="quantity" type="number" min={1} max={remaining ?? undefined} value={quantity}
                               onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                        {passes.length > 0 && (
                            <div className="field">
                                <label htmlFor="passId">{t('shop.reservationItem.useWithPass', '이용권으로 신청')}</label>
                                <select id="passId" value={passId} onChange={(e) => setPassId(e.target.value)}>
                                    <option value="">{t('shop.reservationItem.useWithPayment', '결제로 신청')}</option>
                                    {passes.map((pass) => (
                                        <option key={pass.id} value={pass.id}>
                                            {pass.productName}{pass.unit === 'COUNT' ? t('shop.reservationItem.passRemaining', ' (잔여 {count}회)', {count: String(pass.remainingCount)}) : ''}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}
                        <p className="price align-right">
                            {t('shop.reservationItem.paymentAmount', '결제 금액 {amount}', {amount: passId ? t('shop.reservationItem.usePass', '이용권 사용 (0원)') : won(Number(item.price) * quantity)})}
                        </p>
                        <ErrorBanner error={error}/>
                        <div className="toolbar form-actions">
                            <button type="submit" className="primary" disabled={sessions.length > 0 && (!chosenSession || (remaining != null && quantity > remaining))}>
                                {t('shop.reservationItem.apply', '신청하기')}
                            </button>
                        </div>
                    </form>
                ) : (
                    <p className="muted"><Link to="/login" state={{from: `/reservation-items/${id}`}}>{t('shop.common.login', '로그인')}</Link> {t('shop.reservationItem.loginToApply', '후 신청할 수 있습니다.')}</p>
                )}
                </div>
            </section>
            <section className="card">
                <HtmlView html={item.contentHtml}/>
            </section>
            <Reviews target="RESERVATION_ITEM" targetId={id}/>
        </>
    );
}

const methods = (t: T): { value: PaymentMethod; label: string }[] => [
    {value: 'CARD', label: t('shop.payment.method.card', '신용카드')},
    {value: 'BANK_TRANSFER', label: t('shop.payment.method.bankTransfer', '계좌이체')},
    {value: 'VIRTUAL_ACCOUNT', label: t('shop.payment.method.virtualAccount', '가상계좌')},
    {value: 'MOBILE', label: t('shop.payment.method.mobile', '휴대폰')},
    {value: 'PREPAID', label: t('shop.payment.method.prepaid', '지갑(적립금)')},
];

export function CheckoutPage() {
    const {t} = useI18n();
    const params = useParams();
    const type = params.type === 'RESERVATION' ? 'RESERVATION' : params.type === 'RESERVATION_EXTENSION' ? 'RESERVATION_EXTENSION' : 'ORDER';
    const id = Number(params.id);
    const navigate = useNavigate();
    const [target, setTarget] = useState<{ title: string; amount: number; payable: boolean } | null>(null);
    const [method, setMethod] = useState<PaymentMethod>('CARD');
    const [error, setError] = useState<unknown>(null);
    const [paying, setPaying] = useState(false);
    const [tax, setTax] = useState<TaxSetting | null>(null);
    const [rates, setRates] = useState<ExchangeRate[]>([]);

    useEffect(() => {
        shopApi.taxSetting().then((setting) => setTax(setting ?? null)).catch(() => setTax(null));
        shopApi.exchangeRates().then((r) => setRates(r ?? [])).catch(() => setRates([]));
    }, []);

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
                        title: t('shop.checkout.extensionFeeTitle', '{name} 연장료 ({number})', {name: reservation.reservationItemName, number: reservation.reservationNumber}),
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
                        title: t('shop.checkout.reservationTitle', '{name} {count}명 ({number})',
                            {name: reservation.reservationItemName, count: String(reservation.quantity), number: reservation.reservationNumber}),
                        amount: Number(reservation.totalPrice),
                        payable: reservation.status === 'PENDING_PAYMENT',
                    });
                }
            }).catch(setError);
        }
    }, [type, id, t]);

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
            <h1>{t('shop.checkout.title', '결제')}</h1>
            <p className="notice-box">{t('shop.checkout.mockNotice', '테스트용 가상 결제입니다. 실제로 결제되지 않으며 즉시 승인됩니다.')}</p>
            <ErrorBanner error={error}/>
            {target && (
                <>
                    <p>{target.title}</p>
                    <p className="price">{won(target.amount)}</p>
                    <PriceNotice amount={target.amount} tax={tax} rates={rates}/>
                    {target.payable ? (
                        <>
                            <div className="field">
                                <label htmlFor="method">{t('shop.checkout.method', '결제 수단')}</label>
                                <select id="method" value={method} onChange={(e) => setMethod(e.target.value as PaymentMethod)}>
                                    {methods(t).map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
                                </select>
                            </div>
                            <button type="button" className="primary" disabled={paying} onClick={pay}>
                                {paying ? t('shop.checkout.paying', '결제 중…') : t('shop.checkout.payAction', '{amount} 결제하기', {amount: won(target.amount)})}
                            </button>
                        </>
                    ) : <p className="muted">{t('shop.checkout.alreadyProcessed', '이미 처리된 건입니다.')}</p>}
                </>
            )}
        </section>
    );
}

export function MyOrdersPage() {
    const {t} = useI18n();
    const {label: orderStatus} = useStatusLabels('ORDER', orderStatusLabels(t));
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
            <h1>{t('shop.myOrders.title', '주문 내역')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('shop.myOrders.column.number', '주문번호')}</th><th>{t('shop.myOrders.column.product', '상품')}</th>
                        <th>{t('shop.myOrders.column.amount', '금액')}</th><th>{t('shop.myOrders.column.status', '상태')}</th>
                        <th>{t('shop.myOrders.column.createdAt', '주문일')}</th><th>{t('shop.myOrders.column.progress', '진행·메시지')}</th>
                        <th>{t('shop.myOrders.column.payment', '결제')}</th><th>{t('shop.myOrders.column.return', '반품·교환')}</th>
                        <th>{t('shop.myOrders.column.cancel', '취소')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((order) => (
                        <tr key={order.id}>
                            <td><code>{order.orderNumber}</code></td>
                            <td>
                                {order.items.length > 1 ? order.productName
                                    : <><Link to={`/products/${order.productId}`}>{order.productName}</Link> × {order.quantity}</>}
                                {order.items.length > 1 && (
                                    <details>
                                        <summary>{t('shop.myOrders.itemCount', '주문 상품 {count}개', {count: String(order.items.length)})}</summary>
                                        <OrderItemsTable order={order} onCancel={['PENDING_PAYMENT', 'PAID'].includes(order.status)
                                        && order.items.filter((item) => item.status === 'ORDERED').length > 1
                                            ? (item) => {
                                                if (window.confirm(t('shop.myOrders.confirmCancelItem',
                                                    "'{name}'만 취소할까요? 결제한 주문은 이 상품 금액을 환불합니다. 남은 금액이 무료 배송 기준보다 적어지면 배송비를 빼고 환불합니다.", {name: item.productName}))) {
                                                    shopApi.cancelOrderItem(order.id, item.id).then(reload).catch(setError);
                                                }
                                            }
                                            : undefined}/>
                                    </details>
                                )}
                            </td>
                            <td>
                                <OrderAmount order={order}/>
                                {order.saleType !== 'PURCHASE' && <span className="badge">{saleType(t)[order.saleType]}</span>}
                                {order.shipDate && <div className="muted small">{t('shop.myOrders.shipDate', '출고 예정일 {date}', {date: formatDate(order.shipDate)})}</div>}
                                {order.quoteMessage && <div className="muted small">{order.quoteMessage}</div>}
                            </td>
                            <td>
                                {orderStatus(order.status, order.customStatus)}
                                {order.trackingNumber && <div className="muted small">{order.courier} {order.trackingNumber}</div>}
                            </td>
                            <td>{formatDateTime(order.createdAt)}</td>
                            <td>
                                <details>
                                    <summary>{t('shop.myOrders.view', '보기')}</summary>
                                    <ProgressAndMessages
                                        getProgress={() => shopApi.progress('ORDER', order.id)}
                                        listMessages={() => shopApi.messages.list('ORDER', order.id)}
                                        postMessage={(content, fileIds) => shopApi.messages.post('ORDER', order.id, {content, fileIds})}
                                    />
                                </details>
                            </td>
                            <td className="button-cell">
                                {order.status === 'PENDING_PAYMENT' && <Link className="button primary" to={`/checkout/ORDER/${order.id}`}>{t('admin.payment.title', '결제')}</Link>}
                                {order.status === 'QUOTED' && (
                                    <button type="button" className="primary" onClick={() => {
                                        shopApi.acceptQuote(order.id).then(() => navigate(`/checkout/ORDER/${order.id}`)).catch(setError);
                                    }}>{t('shop.myOrders.acceptQuote', '견적 수락')}</button>
                                )}
                            </td>
                            <td className="button-cell">
                                {order.saleType === 'PURCHASE' && ['PAID', 'SHIPPING', 'DELIVERED'].includes(order.status) && (
                                    <ReturnCell order={order} existing={returns.get(order.id)} onDone={reload}/>
                                )}
                            </td>
                            <td className="button-cell">
                                {['QUOTE_REQUESTED', 'QUOTED', 'PENDING_PAYMENT', 'PAID'].includes(order.status) && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(t('shop.myOrders.confirmCancel', '주문을 취소할까요?'))) {
                                            shopApi.cancelOrder(order.id).then(reload).catch(setError);
                                        }
                                    }}>{t('common.cancel', '취소')}</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={9} className="muted">{t('shop.myOrders.noData', '주문 내역이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function ReturnCell({order, existing, onDone}: { order: Order; existing: OrderReturn | undefined; onDone: () => void }) {
    const {t} = useI18n();
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
            <span className="badge">{returnType(t)[existing.type]} {returnStatus(t)[existing.status]}</span>
        );
    }
    if (!open) {
        return <button type="button" onClick={() => setOpen(true)}>{t('shop.orderReturn.request', '반품·교환 신청')}</button>;
    }
    return (
        <div className="small">
            {existing?.status === 'REJECTED' && (
                <div className="muted">
                    {t('shop.orderReturn.lastRejected', '지난 신청 거절됨')}{existing.adminMemo ? `: ${existing.adminMemo}` : ''}
                </div>
            )}
            <select value={type} onChange={(e) => setType(e.target.value as OrderReturnType)}>
                <option value="RETURN">{t('shop.orderReturn.return', '반품')}</option>
                <option value="EXCHANGE">{t('shop.orderReturn.exchange', '교환')}</option>
            </select>
            {type === 'EXCHANGE' && (
                <select value={exchangeOptionId} onChange={(e) => setExchangeOptionId(e.target.value)}>
                    <option value="">{t('shop.orderReturn.exchangeOption', '교환할 옵션')}</option>
                    {product?.options.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
                </select>
            )}
            <input value={reason} onChange={(e) => setReason(e.target.value)} placeholder={t('shop.orderReturn.reason', '신청 이유')}/>
            <button type="button" className="primary" disabled={submitting || !reason.trim() || (type === 'EXCHANGE' && !exchangeOptionId)}
                    onClick={() => {
                        setSubmitting(true);
                        shopApi.requestReturn(order.id, type, reason.trim(), type === 'EXCHANGE' ? Number(exchangeOptionId) : null)
                            .then(() => {
                                setOpen(false);
                                onDone();
                            }).catch(setError).finally(() => setSubmitting(false));
                    }}>{t('shop.reservationItem.apply', '신청하기')}</button>
            <ErrorBanner error={error}/>
        </div>
    );
}

export function MyReservationsPage() {
    const {t} = useI18n();
    const {label: reservationStatus} = useStatusLabels('RESERVATION', reservationStatusLabels(t));
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Reservation> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.myReservations(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>{t('shop.myReservations.title', '예약 내역')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.reservation.column.number', '예약번호')}</th><th>{t('admin.reservation.column.item', '예약 상품')}</th>
                        <th>{t('admin.reservation.column.usage', '이용')}</th><th>{t('admin.reservation.column.quantity', '수량')}</th>
                        <th>{t('admin.reservation.column.amount', '금액')}</th><th>{t('admin.reservation.column.status', '상태')}</th>
                        <th>{t('shop.myReservations.checkIn', '체크인')}</th><th>{t('admin.payment.title', '결제')}</th>
                        <th>{t('admin.reservation.extension.extend', '연장')}</th><th>{t('admin.reservation.column.cancel', '취소')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((reservation) => (
                        <tr key={reservation.id}>
                            <td><code>{reservation.reservationNumber}</code></td>
                            <td><Link to={`/reservation-items/${reservation.reservationItemId}`}>{reservation.reservationItemName}</Link></td>
                            <td>{reservationUsage(reservation, t)}</td>
                            <td>{reservation.quantity}</td>
                            <td>{won(reservation.totalPrice)}
                                {reservation.depositAmount != null && Number(reservation.depositAmount) > 0 && (
                                    <div className="muted small">{t('shop.myReservations.depositIncluded', '보증금 {amount} 포함', {amount: won(reservation.depositAmount)})}</div>
                                )}
                            </td>
                            <td>{reservationStatus(reservation.status, reservation.customStatus)}
                                {reservation.overdue && <div><span className="badge danger">{t('shop.myReservations.returnOverdue', '반납 {days}일 지연', {days: String(reservation.lateDays)})}</span></div>}
                                {reservation.reservationType === 'VISIT' && (
                                    <div className="muted small">
                                        {reservation.workCompletedAt
                                            ? t('shop.myReservations.workCompleted', '{date} 작업 완료', {date: formatDate(reservation.workCompletedAt)}) + (reservation.workReport ? ` · ${reservation.workReport}` : '')
                                            : reservation.workerName
                                                ? t('shop.myReservations.worker', '작업자 {name}', {name: reservation.workerName}) + (reservation.workerPhone ? ` (${reservation.workerPhone})` : '')
                                                : t('shop.myReservations.workerNotAssigned', '작업자 배정 전')}
                                    </div>
                                )}
                                {reservation.workPhotoUrls.length > 0 && (
                                    <div className="work-photos">
                                        {reservation.workPhotoUrls.map((url, i) => (
                                            <a key={url} href={url}><img src={url} alt={t('shop.myReservations.workPhoto', '작업 사진 {index}', {index: String(i + 1)})}/></a>
                                        ))}
                                    </div>
                                )}
                                {reservation.returnedAt && (
                                    <div className="muted small">
                                        {t('shop.myReservations.returned', '{date} 반납 · 보증금 {amount} 환불', {date: formatDate(reservation.returnedAt), amount: won(reservation.depositRefunded ?? 0)})}
                                        {reservation.returnMemo && ` (${reservation.returnMemo})`}
                                    </div>
                                )}
                            </td>
                            <td>
                                {reservation.checkedInAt ? (
                                    <>
                                        <span className="badge ok">{t('shop.myReservations.checkedInAt', '{time} 체크인', {time: formatDateTime(reservation.checkedInAt)})}</span>
                                        {reservation.queueNumber != null && (
                                            <div className="muted small">{t('shop.myReservations.queue', '대기 {number}번 · {status}', {number: String(reservation.queueNumber), status: queueStatus(t)[reservation.queueStatus ?? 'WAITING']})}</div>
                                        )}
                                    </>
                                ) : reservation.status === 'CONFIRMED' && reservation.checkInCode && (
                                    <details>
                                        <summary>{t('shop.myReservations.checkInCode', '체크인 코드')}</summary>
                                        <img alt={t('shop.myReservations.checkInQr', '체크인 QR 코드')} width={120} height={120}
                                             src={`https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${reservation.checkInCode}`}/>
                                        <div><code>{reservation.checkInCode}</code></div>
                                    </details>
                                )}
                            </td>
                            <td className="button-cell">
                                {reservation.status === 'PENDING_PAYMENT' && (
                                    <Link className="button primary" to={`/checkout/RESERVATION/${reservation.id}`}>{t('admin.payment.title', '결제')}</Link>
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
                                        if (window.confirm(t('admin.reservation.confirmCancel', '예약을 취소할까요?'))) {
                                            shopApi.cancelReservation(reservation.id).then(reload).catch(setError);
                                        }
                                    }}>{t('common.cancel', '취소')}</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={9} className="muted">{t('shop.myReservations.noData', '예약 내역이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function ExtensionCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const {t} = useI18n();
    const [newEndAt, setNewEndAt] = useState('');
    const [error, setError] = useState<unknown>(null);

    if (reservation.pendingExtensionId) {
        return (
            <div className="small">
                <div>{t('admin.reservation.extension.pending.short', '연장료 {fee}', {fee: won(reservation.pendingExtensionFee ?? 0)})}</div>
                <Link className="button primary" to={`/checkout/RESERVATION_EXTENSION/${reservation.pendingExtensionId}`}>{t('admin.payment.title', '결제')}</Link>
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm(t('shop.myReservations.confirmCancelExtension', '연장 신청을 취소할까요?'))) {
                        shopApi.cancelExtension(reservation.id).then(onDone).catch(setError);
                    }
                }}>{t('admin.reservation.extension.cancel', '연장 취소')}</button>
                <ErrorBanner error={error}/>
            </div>
        );
    }
    return (
        <div className="small">
            <input type="date" aria-label={t('shop.myReservations.newEndAt', '새 반납일')} min={dateOf(reservation.endAt)} value={newEndAt}
                   onChange={(e) => setNewEndAt(e.target.value)}/>
            <button type="button" disabled={!newEndAt} onClick={() => {
                shopApi.extendReservation(reservation.id, `${newEndAt}T00:00:00`).then(onDone).catch(setError);
            }}>{t('shop.myReservations.requestExtension', '연장 신청')}</button>
            {reservation.unpaidExtensionFee != null && Number(reservation.unpaidExtensionFee) > 0 && (
                <div className="muted">{t('admin.reservation.return.unpaidExtensionFee', '미결제 연장료 {fee}', {fee: won(reservation.unpaidExtensionFee)})}</div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

const dateOf = (value: string | null) => (value ? value.slice(0, 10) : '');

export function MyPassesPage() {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MemberPass> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myPasses(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>{t('admin.pass.title', '이용권')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.pass.column.pass', '이용권')}</th><th>{t('shop.myPasses.unit', '단위')}</th>
                        <th>{t('admin.pass.column.remaining', '남은 횟수')}</th><th>{t('admin.pass.column.expiresAt', '만료일')}</th>
                        <th>{t('admin.pass.column.status', '상태')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((pass) => (
                        <tr key={pass.id}>
                            <td>{pass.productName}</td>
                            <td>{passUnit(t)[pass.unit]}</td>
                            <td>{pass.unit === 'COUNT' ? `${pass.remainingCount ?? 0}/${pass.totalCount ?? 0}` : '-'}</td>
                            <td>{pass.expiresAt ? formatDate(pass.expiresAt) : '-'}</td>
                            <td>
                                {pass.status === 'ACTIVE' && <span className="badge ok">{t('admin.pass.usable', '사용 가능')}</span>}
                                {pass.status === 'USED_UP' && <span className="badge">{t('admin.pass.usedUp', '다 씀')}</span>}
                                {pass.status === 'CANCELED' && <span className="badge">{t('common.cancel', '취소')}</span>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">{t('shop.myPasses.noData', '이용권이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyLicenseKeysPage() {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyLicenseKey> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myLicenseKeys(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>{t('shop.myLicenseKeys.title', '라이선스 키')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>{t('admin.order.column.product', '상품')}</th><th>{t('shop.myLicenseKeys.key', '키')}</th><th>{t('shop.myLicenseKeys.assignedAt', '받은 날')}</th></tr></thead>
                    <tbody>
                    {data?.content.map((key) => (
                        <tr key={key.id}>
                            <td>{key.productName}</td>
                            <td><code>{key.code}</code></td>
                            <td>{key.assignedAt ? formatDateTime(key.assignedAt) : '-'}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={3} className="muted">{t('shop.myLicenseKeys.noData', '받은 라이선스 키가 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyWalletPage() {
    const {t} = useI18n();
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
            <h1>{t('shop.wallet.title', '지갑')}</h1>
            <ErrorBanner error={error}/>
            <p className="price">{won(balance)}</p>

            <h2>{t('shop.wallet.redeemTitle', '상품권 코드로 충전')}</h2>
            <form onSubmit={redeem} className="inline-form toolbar">
                <input value={redeemCode} onChange={(e) => setRedeemCode(e.target.value)} placeholder={t('shop.wallet.giftCardCode', '상품권 코드')} required/>
                <button type="submit" className="primary" disabled={busy}>{t('shop.wallet.redeem', '충전하기')}</button>
            </form>

            <h2>{t('shop.wallet.issueTitle', '상품권 발급 (잔액에서 차감)')}</h2>
            <form onSubmit={issueCode} className="inline-form toolbar">
                <input type="number" min={1} value={issueAmount} onChange={(e) => setIssueAmount(e.target.value)} placeholder={t('shop.wallet.amount', '금액(원)')} required/>
                <button type="submit" className="primary" disabled={busy}>{t('shop.wallet.issue', '발급하기')}</button>
            </form>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('shop.wallet.code', '코드')}</th><th>{t('admin.subscription.column.amount', '금액')}</th>
                        <th>{t('admin.subscription.column.status', '상태')}</th><th>{t('shop.wallet.issuedAt', '발급일')}</th>
                    </tr></thead>
                    <tbody>
                    {codes?.content.map((card) => (
                        <tr key={card.id}>
                            <td><code>{card.code}</code></td>
                            <td>{won(card.amount)}</td>
                            <td>{card.status === 'UNUSED' ? <span className="badge ok">{t('shop.wallet.unused', '미사용')}</span> : <span className="badge">{t('shop.wallet.used', '사용됨')}</span>}</td>
                            <td>{formatDateTime(card.createdAt)}</td>
                        </tr>
                    ))}
                    {codes?.content.length === 0 && <tr><td colSpan={4} className="muted">{t('shop.wallet.noGiftCards', '발급한 상품권이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {codes && <Pagination page={codesPage} totalPages={codes.page.totalPages} onChange={setCodesPage}/>}

            <h2>{t('shop.wallet.ledgerTitle', '이용 내역')}</h2>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('shop.wallet.type', '구분')}</th><th>{t('admin.subscription.column.amount', '금액')}</th>
                        <th>{t('shop.wallet.balanceAfter', '잔액')}</th><th>{t('shop.wallet.memo', '메모')}</th><th>{t('shop.wallet.at', '일시')}</th>
                    </tr></thead>
                    <tbody>
                    {ledger?.content.map((entry) => (
                        <tr key={entry.id}>
                            <td>{ledgerType(t)[entry.type]}</td>
                            <td>{won(entry.amount)}</td>
                            <td>{won(entry.balanceAfter)}</td>
                            <td>{entry.memo ?? '-'}</td>
                            <td>{formatDateTime(entry.createdAt)}</td>
                        </tr>
                    ))}
                    {ledger?.content.length === 0 && <tr><td colSpan={5} className="muted">{t('shop.wallet.noLedger', '내역이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {ledger && <Pagination page={ledgerPage} totalPages={ledger.page.totalPages} onChange={setLedgerPage}/>}
        </section>
    );
}

export function MySubscriptionsPage() {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Subscription> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.mySubscriptions(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>{t('admin.subscription.title', '구독')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.subscription.column.product', '상품')}</th><th>{t('admin.subscription.column.amount', '금액')}</th>
                        <th>{t('admin.subscription.column.status', '상태')}</th><th>{t('admin.subscription.column.nextBilling', '다음 결제일')}</th>
                        <th>{t('admin.subscription.column.startedAt', '시작일')}</th><th>{t('shop.mySubscriptions.manage', '관리')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((item) => (
                        <tr key={item.id}>
                            <td><Link to={`/products/${item.productId}`}>{item.productName}</Link></td>
                            <td>{billingCycle(t)[item.billingCycle]} {won(item.price)}</td>
                            <td>
                                {item.status === 'ACTIVE' && <span className="badge ok">{t('admin.subscription.active', '구독 중')}</span>}
                                {item.status === 'PAUSED' && (
                                    <span className="badge">
                                        {t('admin.subscription.paused', '일시정지')}{item.resumeOn ? t('admin.subscription.resumeOn', ' ({date}에 재개)', {date: item.resumeOn}) : ''}
                                    </span>
                                )}
                                {item.status === 'CANCELED' && <span className="badge">{t('admin.subscription.canceled', '해지')}</span>}
                                {item.pendingProductId && <div className="muted small">{t('shop.mySubscriptions.pendingChange', '다음 결제부터 {name}로 변경', {name: item.pendingProductName ?? ''})}</div>}
                                {item.commitmentEndDate && item.status !== 'CANCELED' && (
                                    <div className="muted small">
                                        {t('admin.subscription.commitmentUntil', '{date}까지 약정', {date: formatDate(item.commitmentEndDate)})}
                                        {item.earlyTerminationFee > 0 && t('shop.mySubscriptions.cancelFeeNotice', ' (지금 해지하면 위약금 {fee})', {fee: won(item.earlyTerminationFee)})}
                                    </div>
                                )}
                            </td>
                            <td>{item.nextBillingDate ?? '-'}</td>
                            <td>{formatDateTime(item.startedAt)}</td>
                            <td className="button-cell">
                                {item.status !== 'CANCELED' && <SubscriptionActions subscription={item} onDone={reload}/>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('shop.mySubscriptions.noData', '구독 중인 상품이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function SubscriptionActions({subscription, onDone}: { subscription: Subscription; onDone: () => void }) {
    const {t} = useI18n();
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
                    <input type="date" aria-label={t('shop.mySubscriptions.resumeOn', '재개일')} value={resumeOn} onChange={(e) => setResumeOn(e.target.value)}/>
                    <button type="button" onClick={() => run(shopApi.pauseSubscription(subscription.id, resumeOn || null))}>
                        {t('admin.subscription.pause', '일시정지')}
                    </button>
                </>
            ) : (
                <button type="button" onClick={() => run(shopApi.resumeSubscription(subscription.id))}>{t('admin.subscription.resume', '재개')}</button>
            )}
            {subscription.pendingProductId ? (
                <button type="button" onClick={() => run(shopApi.cancelSubscriptionPlanChange(subscription.id))}>{t('shop.mySubscriptions.cancelChange', '변경 취소')}</button>
            ) : (
                <button type="button" onClick={openPlans}>{t('shop.mySubscriptions.changePlan', '상품 변경')}</button>
            )}
            <button type="button" className="danger" onClick={() => {
                const message = subscription.earlyTerminationFee > 0
                    ? t('admin.subscription.confirmCancelWithFee', '구독을 해지할까요? 약정 기간이 남아 위약금 {fee}이 바로 결제됩니다.', {fee: won(subscription.earlyTerminationFee)})
                    : t('admin.subscription.confirmCancel', '구독을 해지할까요? 다음 결제일부터 결제되지 않습니다.');
                if (window.confirm(message)) {
                    run(shopApi.cancelSubscription(subscription.id));
                }
            }}>{t('admin.subscription.cancelAction', '해지')}</button>
            {changing && plans && (
                <div className="dropdown-panel">
                    {plans.length === 0 && <p className="muted small">{t('shop.mySubscriptions.noOtherPlans', '변경할 수 있는 다른 상품이 없습니다.')}</p>}
                    {plans.map((plan) => (
                        <button key={plan.id} type="button" className="link-button" onClick={() => {
                            setChanging(false);
                            run(shopApi.changeSubscriptionPlan(subscription.id, plan.id));
                        }}>
                            {plan.name} · {billingCycle(t)[plan.billingCycle ?? 'MONTHLY']} {won(plan.price)}
                        </button>
                    ))}
                    <button type="button" className="link-button" onClick={() => setChanging(false)}>{t('common.close', '닫기')}</button>
                </div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}
