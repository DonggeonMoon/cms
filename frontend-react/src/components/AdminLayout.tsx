import {Link, NavLink, Outlet, useNavigate} from 'react-router';
import {type AdminArea, useMyPermissions} from '../api/admins';
import {ColorModeToggle, useColorMode} from './ColorMode';
import type {SiteFeature} from '../api/domain';
import {useAuth} from '../auth/AuthContext';
import {SiteProvider, useSite} from './SiteContext';

interface MenuItem {
    to: string;
    label: string;
    end?: boolean;
    sub?: boolean;
    features?: SiteFeature[];
    area?: AdminArea;
    superOnly?: boolean;
}

const MENU_GROUPS: { title: string; area?: AdminArea; items: MenuItem[] }[] = [
    {
        title: '현황',
        area: 'STATUS',
        items: [
            {to: '/admin', label: '대시보드', end: true},
            {to: '/admin/statistics', label: '방문 통계'},
        ],
    },
    {
        title: '사이트',
        area: 'SITE',
        items: [
            {to: '/admin/sites', label: '사이트'},
            {to: '/admin/menus', label: '메뉴'},
            {to: '/admin/banners', label: '배너·팝업'},
            {to: '/admin/configs', label: '설정'},
        ],
    },
    {
        title: '컨텐츠',
        area: 'CONTENT',
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
        area: 'BUSINESS',
        items: [
            {to: '/admin/products', label: '상품', features: ['PRODUCT']},
            {to: '/admin/product-categories', label: '카테고리', sub: true, features: ['PRODUCT']},
            {to: '/admin/discounts', label: '할인', sub: true, features: ['PRODUCT']},
            {to: '/admin/points', label: '포인트', sub: true, features: ['PRODUCT']},
            {to: '/admin/coupons', label: '쿠폰', sub: true, features: ['PRODUCT']},
            {to: '/admin/shipping', label: '배송비', sub: true, features: ['PRODUCT']},
            {to: '/admin/orders', label: '주문', sub: true, features: ['PRODUCT']},
            {to: '/admin/subscriptions', label: '구독', sub: true, features: ['PRODUCT']},
            {to: '/admin/media', label: '스트리밍', features: ['MEDIA']},
            {to: '/admin/courses', label: '교육 과정', sub: true, features: ['MEDIA']},
            {to: '/admin/store', label: '매장 주문', features: ['PRODUCT']},
            {to: '/admin/store-orders', label: '주문 접수', sub: true, features: ['PRODUCT']},
            {to: '/admin/programs', label: '예약', features: ['PROGRAM']},
            {to: '/admin/reservations', label: '예약 내역', sub: true, features: ['PROGRAM']},
            {to: '/admin/applications', label: '신청·지원', features: ['APPLICATION']},
            {to: '/admin/payments', label: '결제', features: ['PRODUCT', 'PROGRAM']},
            {to: '/admin/reviews', label: '리뷰', features: ['PRODUCT', 'PROGRAM']},
        ],
    },
    {
        title: '공공',
        area: 'PUBLIC',
        items: [
            {to: '/admin/reports', label: '신고', features: ['REPORT']},
            {to: '/admin/satisfactions', label: '만족도 조사', features: ['SATISFACTION']},
        ],
    },
    {
        title: '공통',
        area: 'COMMON',
        items: [
            {to: '/admin/members', label: '회원', area: 'MEMBER'},
            {to: '/admin/grades', label: '회원 등급', sub: true, area: 'MEMBER'},
            {to: '/admin/join-settings', label: '회원가입 설정', sub: true, area: 'MEMBER'},
            {to: '/admin/policies/privacy-policy', label: '개인정보처리방침', sub: true, area: 'MEMBER'},
            {to: '/admin/policies/privacy-consent', label: '개인정보 수집·이용 동의', sub: true, area: 'MEMBER'},
            {to: '/admin/policies/mail-reception', label: '이메일 수신 동의', sub: true, area: 'MEMBER'},
            {to: '/admin/policies/sms-reception', label: 'SMS 수신 동의', sub: true, area: 'MEMBER'},
            {to: '/admin/policies/marketing-usage', label: '마케팅 활용 동의', sub: true, area: 'MEMBER'},
            {to: '/admin/chatbot', label: '챗봇', features: ['CHATBOT']},
            {to: '/admin/regexes', label: '정규식'},
            {to: '/admin/codes', label: '공통 코드'},
            {to: '/admin/i18n', label: '국제화'},
            {to: '/admin/files', label: '파일'},
            {to: '/admin/audit-logs', label: '작업 로그'},
        ],
    },
    {
        title: '보안',
        area: 'SECURITY',
        items: [
            {to: '/admin/admins', label: '관리자 관리', superOnly: true},
            {to: '/admin/allowed-ips', label: '허용 IP'},
            {to: '/admin/api-keys', label: 'API 키 관리'},
            {to: '/admin/password-hash', label: '해시 알고리즘 관리'},
            {to: '/admin/crypto-key', label: '암호화 키 관리'},
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
    const permissions = useMyPermissions();
    const [colorMode, setColorMode] = useColorMode('cms-admin-color-mode');
    const allowed = (item: MenuItem, groupArea?: AdminArea) => {
        if (item.superOnly) {
            return permissions?.superAdmin === true;
        }
        const area = item.area ?? groupArea;
        return !permissions || permissions.superAdmin || !area || permissions.areas.includes(area);
    };
    const visibleGroups = MENU_GROUPS
        .map((group) => ({
            ...group,
            items: group.items.filter((item) => allowed(item, group.area)
                && (!item.features || item.features.some((feature) => isEnabled(feature)))),
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
                    <ColorModeToggle mode={colorMode} onChange={setColorMode}/>
                    <span className="muted">{user?.name}</span>
                    <Link to="/">사이트로 이동</Link>
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
