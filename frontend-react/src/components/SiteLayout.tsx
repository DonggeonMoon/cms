import {useEffect, useState} from 'react';
import {ColorModeToggle, useColorMode} from './ColorMode';
import {Link, NavLink, Outlet, useLocation, useNavigate} from 'react-router';
import type {Menu, SiteFeature} from '../api/domain';
import {shopApi, siteApi} from '../api/endpoints';
import {useAuth} from '../auth/AuthContext';
import {FEATURE_PATHS, menuHref, SiteProvider, useSite} from './SiteContext';
import {I18nProvider, LANGUAGE_NAMES, useI18n} from '../api/i18n';
import {ChatbotWidget} from './ChatbotWidget';
import {TopBar} from './TopBar';

export function SiteLayout() {
    return (
        <I18nProvider>
            <SiteProvider>
                <SiteFrame/>
            </SiteProvider>
        </I18nProvider>
    );
}

function SiteFrame() {
    const {user, logout} = useAuth();
    const {site, name, menus, configs, isEnabled} = useSite();
    const {t, language, languages, setLanguage} = useI18n();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        siteApi.trackVisit(location.pathname + location.search, document.referrer).catch(() => undefined);
    }, [location.pathname, location.search]);

    const [unreadNotifications, setUnreadNotifications] = useState(0);
    useEffect(() => {
        if (user?.userType !== 'MEMBER') {
            return;
        }
        shopApi.unreadNotificationCount().then(setUnreadNotifications).catch(() => undefined);
    }, [user, location.pathname]);

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    const darkModeEnabled = site?.darkModeEnabled !== false;
    const [colorMode, setColorMode] = useColorMode('cms-site-color-mode', darkModeEnabled);
    return (
        <div className="site" data-theme={site?.theme || undefined}>
            <TopBar/>
            <header className="site-header">
                <Link to="/" className="brand">{name}</Link>
                <nav className="site-nav main-nav" aria-label="주 메뉴">
                    {menus.map((menu) => <MenuNode key={menu.id} menu={menu}/>)}
                </nav>
                <nav className="site-nav" aria-label="계정">
                    {user?.userType === 'MEMBER' && (
                        <>
                            <span className="muted">{t('nav.greeting', '{name}님', {name: user.name})}</span>
                            {isEnabled('PRODUCT') && <NavLink to="/cart">{t('nav.cart', '장바구니')}</NavLink>}
                            <NavLink to="/mypage/notifications">
                                {t('nav.notifications', '알림')}{unreadNotifications > 0 && ` (${unreadNotifications})`}
                            </NavLink>
                            <NavLink to="/mypage">{t('nav.mypage', '마이페이지')}</NavLink>
                            <button type="button" className="link-button" onClick={handleLogout}>{t('nav.logout', '로그아웃')}</button>
                        </>
                    )}
                    {!user && <NavLink to="/login">{t('nav.login', '로그인')}</NavLink>}
                    {darkModeEnabled && <ColorModeToggle mode={colorMode} onChange={setColorMode}/>}
                    {languages.length > 1 && (
                        <select className="language-select" aria-label={t('language.label', '언어')} value={language}
                                onChange={(e) => setLanguage(e.target.value)}>
                            {languages.map((code) => <option key={code} value={code}>{LANGUAGE_NAMES[code] ?? code}</option>)}
                        </select>
                    )}
                </nav>
            </header>
            {user?.userType === 'MEMBER' && location.pathname.startsWith('/mypage') && (
                <nav className="sub-nav" aria-label="마이페이지">
                    <NavLink to="/mypage" end>{t('mypage.profile', '회원정보')}</NavLink>
                    <NavLink to="/mypage/notifications">{t('mypage.notifications', '알림')}</NavLink>
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/orders">{t('mypage.orders', '주문 내역')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/wishlist">{t('mypage.wishlist', '찜한 상품')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/subscriptions">{t('mypage.subscriptions', '구독')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/passes">{t('mypage.passes', '이용권')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/wallet">{t('mypage.wallet', '지갑')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/license-keys">{t('mypage.licenseKeys', '라이선스 키')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/points">{t('mypage.points', '포인트')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/coupons">{t('mypage.coupons', '쿠폰함')}</NavLink>}
                    {isEnabled('PRODUCT') && <NavLink to="/mypage/downloads">{t('mypage.downloads', '내려받기')}</NavLink>}
                    {isEnabled('PROGRAM') && <NavLink to="/mypage/reservations">{t('mypage.reservations', '예약 내역')}</NavLink>}
                    {isEnabled('PROGRAM') && <NavLink to="/mypage/records">{t('mypage.records', '이용 기록')}</NavLink>}
                    {isEnabled('APPLICATION') && <NavLink to="/mypage/applications">{t('mypage.applications', '신청 내역')}</NavLink>}
                    {isEnabled('APPLICATION') && <NavLink to="/mypage/repairs">{t('mypage.repairs', '수리·AS 접수')}</NavLink>}
                    {isEnabled('APPLICATION') && <NavLink to="/mypage/votes">{t('mypage.votes', '투표')}</NavLink>}
                    {isEnabled('REPORT') && <NavLink to="/mypage/reports">{t('mypage.reports', '신고')}</NavLink>}
                    {isEnabled('MEDIA') && <NavLink to="/mypage/media">{t('mypage.media', '수강 내역')}</NavLink>}
                    {isEnabled('MEDIA') && <NavLink to="/mypage/courses">{t('mypage.courses', '교육 과정')}</NavLink>}
                    <NavLink to="/mypage/password">{t('mypage.password', '비밀번호 변경')}</NavLink>
                    <NavLink to="/mypage/quit">{t('mypage.quit', '회원 탈퇴')}</NavLink>
                </nav>
            )}
            <main className="site-main">
                <Outlet/>
            </main>
            <footer className="site-footer">
                <nav className="footer-links" aria-label="사이트 정보">
                    <Link to="/privacy"><strong>{t('footer.privacy', '개인정보처리방침')}</strong></Link>
                </nav>
                <div>{configs['site.footer'] ?? `ⓒ ${new Date().getFullYear()}. dgmoonlabs All Rights Reserved`}</div>
            </footer>
            {isEnabled('CHATBOT') && <ChatbotWidget/>}
        </div>
    );
}

const FEATURE_LISTING_PATHS: Partial<Record<SiteFeature, string>> = {BOARD: '/boards', ARCHIVE: '/archives'};

function MenuNode({menu}: { menu: Menu }) {
    return menu.type === 'FEATURE' ? <FeatureMenu menu={menu}/> : <MenuItem menu={menu}/>;
}

function FeatureMenu({menu}: { menu: Menu }) {
    const {unlistedBoards, unlistedArchives} = useSite();
    const listings: Partial<Record<SiteFeature, { id: number; name: string }[]>> = {BOARD: unlistedBoards, ARCHIVE: unlistedArchives};
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
                {menu.children.map((child) => <MenuNode key={child.id} menu={child}/>)}
            </div>
        </div>
    );
}
