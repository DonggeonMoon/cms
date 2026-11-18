import {type FormEvent, type ReactNode, useEffect, useState} from 'react';
import {Link, useNavigate} from 'react-router';
import type {Archive, Board, ProductSummary, ReservationItem} from '../api/domain';
import {archiveApi, boardApi, shopApi} from '../api/endpoints';
import {useAuth} from '../auth/AuthContext';
import {ProductCard} from '../pages/shop/ShopPages';
import {formatDateTime} from './format';
import {useSite} from './SiteContext';
import {useI18n} from '../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

export const homeBlocks = (t: T): Record<string, { label: string; render: () => ReactNode }> => ({
    'product-search': {label: t('shop.homeBlocks.productSearch', '상품 검색창'), render: () => <ProductSearch/>},
    'greeting': {label: t('shop.homeBlocks.greeting', '로그인 안내·환영 문구'), render: () => <Greeting/>},
    'shortcuts': {label: t('shop.homeBlocks.shortcuts', '바로가기 버튼'), render: () => <Shortcuts/>},
    'new-products': {label: t('shop.homeBlocks.newProducts', '새로 들어온 상품 8개'), render: () => <NewProducts/>},
    'open-programs': {label: t('shop.homeBlocks.openPrograms', '모집 중인 프로그램'), render: () => <OpenReservationItems/>},
    'latest-posts': {label: t('shop.homeBlocks.latestPosts', '첫 번째 아카이브(없으면 게시판)의 최근 글'), render: () => <LatestPosts/>},
});

export function ProductSearch() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const navigate = useNavigate();
    const [keyword, setKeyword] = useState('');
    if (!isEnabled('PRODUCT')) {
        return null;
    }
    const search = (event: FormEvent) => {
        event.preventDefault();
        navigate(keyword.trim() ? `/products?keyword=${encodeURIComponent(keyword.trim())}` : '/products');
    };
    return (
        <form className="hero-search" role="search" onSubmit={search}>
            <input aria-label={t('shop.homeBlocks.productSearchLabel', '상품 검색')} placeholder={t('shop.homeBlocks.productSearchPlaceholder', '어떤 물건을 찾고 계세요?')} value={keyword}
                   onChange={(e) => setKeyword(e.target.value)}/>
            <button type="submit" className="primary">{t('common.search', '검색')}</button>
        </form>
    );
}

export function Greeting() {
    const {t} = useI18n();
    const {user} = useAuth();
    return (
        <p className="hero-greeting">
            {user ? t('shop.homeBlocks.welcome', '{name}님, 환영합니다.', {name: user.name}) : (
                <>
                    <Link to="/login">{t('nav.login', '로그인')}</Link>{t('shop.homeBlocks.orJoin1', '하거나')} <Link to="/join">{t('shop.common.join', '회원가입')}</Link>{t('shop.homeBlocks.orJoin2', '하세요.')}
                </>
            )}
        </p>
    );
}

export function Shortcuts() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const [boards, setBoards] = useState<Board[]>([]);
    useEffect(() => {
        boardApi.list().then((list) => setBoards(Array.isArray(list) ? list : [])).catch(() => setBoards([]));
    }, []);

    const shortcuts = [
        ...(isEnabled('BOARD') ? boards.map((board) => ({to: `/boards/${board.id}`, label: board.name})) : []),
        ...(isEnabled('PRODUCT') ? [{to: '/products', label: t('shop.homeBlocks.product', '상품')}] : []),
        ...(isEnabled('PROGRAM') ? [{to: '/reservation-items', label: t('shop.homeBlocks.programApply', '프로그램 신청')}] : []),
        ...(isEnabled('QNA') ? [{to: '/qna', label: t('shop.homeBlocks.qna', '질문답변')}] : []),
        ...(isEnabled('REPORT') ? [{to: '/reports/new', label: t('shop.homeBlocks.report', '신고')}] : []),
        ...(isEnabled('MAP') ? [{to: '/map', label: t('shop.homeBlocks.map', '오시는 길·지점 안내')}] : []),
    ];
    if (shortcuts.length === 0) {
        return null;
    }
    return (
        <nav className="chip-row" aria-label={t('shop.homeBlocks.shortcuts', '바로가기')}>
            {shortcuts.map((shortcut) => <Link key={shortcut.to} to={shortcut.to} className="chip">{shortcut.label}</Link>)}
        </nav>
    );
}

export function NewProducts() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const enabled = isEnabled('PRODUCT');
    const [products, setProducts] = useState<ProductSummary[]>([]);
    useEffect(() => {
        if (enabled) {
            shopApi.products('', 0).then((page) => setProducts(page?.content?.slice(0, 8) ?? [])).catch(() => setProducts([]));
        }
    }, [enabled]);

    if (!enabled || products.length === 0) {
        return null;
    }
    return (
        <section className="home-section">
            <div className="section-head">
                <h2>{t('shop.homeBlocks.newProductsTitle', '새로 들어온 상품')}</h2>
                <Link to="/products">{t('shop.homeBlocks.viewAll', '전체 보기')}</Link>
            </div>
            <div className="product-grid">
                {products.map((product) => <ProductCard key={product.id} product={product}/>)}
            </div>
        </section>
    );
}

export function OpenReservationItems() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const enabled = isEnabled('PROGRAM');
    const [items, setItems] = useState<ReservationItem[]>([]);
    useEffect(() => {
        if (enabled) {
            shopApi.reservationItems('', 0).then((page) => setItems(page?.content?.filter((p) => p.isOpen).slice(0, 3) ?? []))
                .catch(() => setItems([]));
        }
    }, [enabled]);

    if (!enabled || items.length === 0) {
        return null;
    }
    return (
        <section className="home-section">
            <div className="section-head">
                <h2>{t('shop.homeBlocks.openProgramsTitle', '모집 중인 프로그램')}</h2>
                <Link to="/reservation-items">{t('shop.homeBlocks.viewAll', '전체 보기')}</Link>
            </div>
            <ul className="list home-list">
                {items.map((item) => (
                    <li key={item.id}>
                        <Link to={`/reservation-items/${item.id}`}>{item.name}</Link>
                        <span className="muted small">{item.startDateTime ? formatDateTime(item.startDateTime) : ''}</span>
                    </li>
                ))}
            </ul>
        </section>
    );
}

interface LatestPost {
    id: number;
    title: string;
    isNotice: boolean;
    createdAt: string;
}

export function LatestPosts() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const [boards, setBoards] = useState<Board[]>([]);
    const [archives, setArchives] = useState<Archive[]>([]);
    const [posts, setPosts] = useState<LatestPost[]>([]);
    useEffect(() => {
        boardApi.list().then((list) => setBoards(Array.isArray(list) ? list : [])).catch(() => setBoards([]));
        archiveApi.list().then((list) => setArchives(Array.isArray(list) ? list : [])).catch(() => setArchives([]));
    }, []);

    const archive = isEnabled('ARCHIVE') ? archives[0] : undefined;
    const board = !archive && isEnabled('BOARD') ? boards[0] : undefined;
    const news = archive ? {name: archive.name, path: `/archives/${archive.id}`}
        : board ? {name: board.name, path: `/boards/${board.id}`} : undefined;
    useEffect(() => {
        if (archive) {
            archiveApi.articles(archive.id, '', 0)
                .then((page) => setPosts((page?.content ?? []).slice(0, 5).map((a) => ({...a, isNotice: false}))))
                .catch(() => setPosts([]));
        } else if (board) {
            boardApi.articles(board.id, '', 0)
                .then((result) => setPosts([...(result?.notices ?? []), ...(result?.articles?.content ?? [])].slice(0, 5)))
                .catch(() => setPosts([]));
        }
    }, [archive, board]);

    if (!news || posts.length === 0) {
        return null;
    }
    return (
        <section className="home-section">
            <div className="section-head">
                <h2>{news.name}</h2>
                <Link to={news.path}>{t('shop.homeBlocks.viewAll', '전체 보기')}</Link>
            </div>
            <ul className="list home-list">
                {posts.map((post) => (
                    <li key={post.id}>
                        <Link to={`${news.path}/articles/${post.id}`}>
                            {post.isNotice && <span className="badge">{t('shop.board.notice', '공지')}</span>} {post.title}
                        </Link>
                        <span className="muted small">{post.createdAt.slice(0, 10)}</span>
                    </li>
                ))}
            </ul>
        </section>
    );
}
