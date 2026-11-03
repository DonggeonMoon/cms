import {useEffect} from 'react';
import {Link, NavLink, Outlet, useLocation, useNavigate} from 'react-router';
import type {Menu, SiteFeature} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {useAuth} from '../auth/AuthContext';
import {FEATURE_PATHS, menuHref, SiteProvider, useSite} from './SiteContext';
import {ChatbotWidget} from './ChatbotWidget';
import {TopBar} from './TopBar';

export function SiteLayout() {
    return (
        <SiteProvider>
            <SiteFrame/>
        </SiteProvider>
    );
}

function SiteFrame() {
    const {user, logout} = useAuth();
    const {site, name, menus, unlistedBoards, unlistedArchives, configs, isEnabled} = useSite();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        siteApi.trackVisit(location.pathname + location.search, document.referrer).catch(() => undefined);
    }, [location.pathname, location.search]);

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    return (
        <div className="site" data-theme={site?.theme || undefined}>
            <TopBar/>
            <header className="site-header">
                <Link to="/" className="brand">{name}</Link>
                <nav className="site-nav main-nav" aria-label="주 메뉴">
                    {menus.map((menu) => menu.type === 'FEATURE'
                        ? <FeatureMenu key={menu.id} menu={menu} listings={{
                            BOARD: unlistedBoards,
                            ARCHIVE: unlistedArchives,
                        }}/>
                        : <MenuItem key={menu.id} menu={menu}/>)}
                </nav>
                <nav className="site-nav" aria-label="계정">
                    {user?.userType === 'MEMBER' && (
                        <>
                            <span className="muted">{user.name}님</span>
                            <NavLink to="/mypage">마이페이지</NavLink>
                            <button type="button" className="link-button" onClick={handleLogout}>로그아웃</button>
                        </>
                    )}
                    {!user && (
                        <>
                            <NavLink to="/login">로그인</NavLink>
                            <NavLink to="/join">회원가입</NavLink>
                        </>
                    )}
                </nav>
            </header>
            {user?.userType === 'MEMBER' && location.pathname.startsWith('/mypage') && (
                <nav className="sub-nav" aria-label="마이페이지">
                    <NavLink to="/mypage" end>회원정보</NavLink>
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/orders">주문 내역</NavLink>}
                    {isEnabled('PROGRAM') && <NavLink to="/mypage/reservations">예약 내역</NavLink>}
                    {isEnabled('REPORT') && <NavLink to="/mypage/reports">신고</NavLink>}
                </nav>
            )}
            <main className="site-main">
                <Outlet/>
            </main>
            <footer className="site-footer">
                <nav className="footer-links" aria-label="사이트 정보">
                    <Link to="/privacy"><strong>개인정보처리방침</strong></Link>
                </nav>
                <div>{configs['site.footer'] ?? `ⓒ ${new Date().getFullYear()}. dgmoonlabs All Rights Reserved`}</div>
            </footer>
            {isEnabled('CHATBOT') && <ChatbotWidget/>}
        </div>
    );
}

const FEATURE_PATHS: Partial<Record<SiteFeature, string>> = {
    PRODUCT: '/products', PROGRAM: '/programs', QNA: '/qna', REPORT: '/reports/new', MAP: '/map',
};
const FEATURE_LISTING_PATHS: Partial<Record<SiteFeature, string>> = {BOARD: '/boards', ARCHIVE: '/archives'};

function FeatureMenu({menu, listings}: { menu: Menu; listings: Partial<Record<SiteFeature, { id: number; name: string }[]>> }) {
    const feature = menu.feature!;
    const listingPath = FEATURE_LISTING_PATHS[feature];
    if (listingPath) {
        return <ListingMenu label={menu.name} basePath={listingPath} items={listings[feature] ?? []}/>;
    }
    const path = FEATURE_PATHS[feature];
    return path ? <NavLink to={path}>{menu.name}</NavLink> : null;
}

function ListingMenu({label, basePath, items}: { label: string; basePath: string; items: { id: number; name: string }[] }) {
    if (items.length === 0) {
        return null;
    }
    const first = <NavLink to={`${basePath}/${items[0].id}`}>{label}</NavLink>;
    if (items.length === 1) {
        return first;
    }
    return (
        <div className="dropdown">
            {first}
            <div className="dropdown-menu">
                {items.map((item) => <NavLink key={item.id} to={`${basePath}/${item.id}`}>{item.name}</NavLink>)}
            </div>
        </div>
    );
}

function MenuItem({menu}: { menu: Menu }) {
    const href = menuHref(menu);
    const link = menu.type === 'LINK' && /^https?:\/\//.test(href)
        ? <a href={href} target="_blank" rel="noreferrer">{menu.name}</a>
        : <NavLink to={href}>{menu.name}</NavLink>;
    if (menu.children.length === 0) {
        return link;
    }
    return (
        <div className="dropdown">
            {link}
            <div className="dropdown-menu">
                {menu.children.map((child) => <MenuItem key={child.id} menu={child}/>)}
            </div>
        </div>
    );
}
