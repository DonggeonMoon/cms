import {Link, NavLink, Outlet, useNavigate} from 'react-router';
import type {SiteFeature} from '../api/domain';
import {useAuth} from '../auth/AuthContext';
import {SiteProvider, useSite} from './SiteContext';

interface MenuItem {
    to: string;
    label: string;
    end?: boolean;
    sub?: boolean;
    // the item is listed while at least one of these site features is switched on
    features?: SiteFeature[];
}

const MENU_GROUPS: { title: string; items: MenuItem[] }[] = [
    {
        title: '현황',
        items: [
            {to: '/admin', label: '대시보드', end: true},
            {to: '/admin/statistics', label: '방문 통계'},
        ],
    },
    {
        title: '사이트',
        items: [
            {to: '/admin/sites', label: '사이트'},
            {to: '/admin/menus', label: '메뉴'},
            {to: '/admin/banners', label: '배너·팝업'},
            {to: '/admin/configs', label: '설정'},
        ],
    },
    {
        title: '컨텐츠',
        items: [
            {to: '/admin/contents', label: '컨텐츠', features: ['CONTENT']},
            {to: '/admin/boards', label: '게시판', features: ['BOARD']},
            {to: '/admin/archives', label: '아카이브', features: ['ARCHIVE']},
            {to: '/admin/questions', label: '질문답변', features: ['QNA']},
            {to: '/admin/map', label: '지도', features: ['MAP']},
        ],
    },
    {
        title: '비즈니스',
        items: [
            {to: '/admin/products', label: '상품', features: ['PRODUCT']},
            {to: '/admin/discounts', label: '할인', sub: true, features: ['PRODUCT']},
            {to: '/admin/orders', label: '주문', sub: true, features: ['PRODUCT']},
            {to: '/admin/programs', label: '프로그램', features: ['PROGRAM']},
            {to: '/admin/reservations', label: '예약', sub: true, features: ['PROGRAM']},
            {to: '/admin/payments', label: '결제', features: ['PRODUCT', 'PROGRAM']},
            {to: '/admin/reviews', label: '리뷰', features: ['PRODUCT', 'PROGRAM']},
        ],
    },
    {
        title: '공공',
        items: [
            {to: '/admin/reports', label: '신고', features: ['REPORT']},
            {to: '/admin/satisfactions', label: '만족도 조사', features: ['SATISFACTION']},
        ],
    },
    {
        title: '공통',
        items: [
            {to: '/admin/members', label: '회원'},
            {to: '/admin/policies/privacy-consent', label: '개인정보 수집·이용 동의', sub: true},
            {to: '/admin/policies/privacy-policy', label: '개인정보처리방침', sub: true},
            {to: '/admin/codes', label: '공통코드'},
            {to: '/admin/files', label: '파일'},
            {to: '/admin/audit-logs', label: '작업 로그'},
        ],
    },
];

export function AdminLayout() {
    return (
        <SiteProvider>
            <AdminFrame/>
        </SiteProvider>
    );
}

function AdminFrame() {
    const {user, logout} = useAuth();
    const {name, isEnabled} = useSite();
    const navigate = useNavigate();
    const visibleGroups = MENU_GROUPS
        .map((group) => ({
            ...group,
            items: group.items.filter((item) => !item.features || item.features.some((feature) => isEnabled(feature))),
        }))
        .filter((group) => group.items.length > 0);

    const handleLogout = async () => {
        await logout();
        navigate('/admin/login');
    };

    return (
        <div className="admin">
            <header className="admin-header">
                <Link to="/admin" className="brand">{name} 관리자</Link>
                <div className="admin-account">
                    <span className="muted">{user?.name}</span>
                    <Link to="/">사이트로</Link>
                    <button type="button" onClick={handleLogout}>로그아웃</button>
                </div>
            </header>
            <aside className="admin-sidebar">
                <nav aria-label="관리자 메뉴">
                    {visibleGroups.map((group) => (
                        <div key={group.title} className="nav-group">
                            <span className="nav-group-title">{group.title}</span>
                            {group.items.map((item) => (
                                <NavLink key={item.to} to={item.to} end={item.end}
                                         className={({isActive}) => [isActive ? 'active' : '', item.sub ? 'nav-sub' : ''].join(' ').trim()}>
                                    {item.label}
                                </NavLink>
                            ))}
                        </div>
                    ))}
                </nav>
            </aside>
            <main className="admin-main">
                <Outlet/>
            </main>
        </div>
    );
}
