import {useEffect, useState} from 'react';
import {Link, NavLink, Outlet, useNavigate} from 'react-router';
import {type AdminArea, useMyPermissions} from '../api/admins';
import {getApiSiteCode, setApiSiteCode} from '../api/client';
import {ColorModeToggle, useColorMode} from './ColorMode';
import {adminApi} from '../api/endpoints';
import type {Site, SiteFeature} from '../api/domain';
import {useAuth} from '../auth/AuthContext';
import {I18nProvider, LANGUAGE_NAMES, useI18n} from '../api/i18n';
import {SiteProvider, useSite} from './SiteContext';

const ADMIN_SITE_CODE_KEY = 'cms.admin.siteCode';

function readSavedSiteCode(): string | null {
    try {
        return localStorage.getItem(ADMIN_SITE_CODE_KEY);
    } catch {
        return null;
    }
}

// 라우터가 /admin 경로에서 setApiSiteCode(null)로 초기화하므로, 관리자가 고른 사이트를 모듈 로드 시점에 복원한다.
const savedAdminSiteCode = readSavedSiteCode();
if (savedAdminSiteCode) {
    setApiSiteCode(savedAdminSiteCode);
}

interface MenuItem {
    to: string;
    label: string;
    end?: boolean;
    sub?: boolean;
    features?: SiteFeature[];
    area?: AdminArea;
    superOnly?: boolean;
}

type T = (key: string, fallback: string) => string;

const menuGroups = (t: T): { title: string; area?: AdminArea; items: MenuItem[] }[] => [
    {
        title: t('admin.navGroup.status', '현황'),
        area: 'STATUS',
        items: [
            {to: '/admin', label: t('admin.nav.dashboard', '대시보드'), end: true},
            {to: '/admin/statistics', label: t('admin.nav.visitStats', '방문 통계')},
        ],
    },
    {
        title: t('admin.navGroup.site', '사이트'),
        area: 'SITE',
        items: [
            {to: '/admin/sites', label: t('admin.nav.sites', '사이트')},
            {to: '/admin/menus', label: t('admin.nav.menus', '메뉴')},
            {to: '/admin/banners', label: t('admin.nav.banners', '배너·팝업')},
            {to: '/admin/configs', label: t('admin.nav.configs', '설정')},
        ],
    },
    {
        title: t('admin.navGroup.content', '컨텐츠'),
        area: 'CONTENT',
        items: [
            {to: '/admin/contents', label: t('admin.nav.contents', '컨텐츠'), features: ['CONTENT']},
            {to: '/admin/boards', label: t('admin.nav.boards', '게시판'), features: ['BOARD']},
            {to: '/admin/archives', label: t('admin.nav.archives', '아카이브'), features: ['ARCHIVE']},
            {to: '/admin/questions', label: t('admin.nav.questions', '질문답변'), features: ['QNA']},
            {to: '/admin/votes', label: t('admin.nav.votes', '투표'), area: 'BUSINESS', features: ['APPLICATION']},
            {to: '/admin/map', label: t('admin.nav.map', '지도'), features: ['MAP']},
        ],
    },
    {
        title: t('admin.navGroup.business', '비즈니스'),
        area: 'BUSINESS',
        items: [
            {to: '/admin/categories', label: t('admin.nav.categories', '카테고리'), features: ['PRODUCT', 'PROGRAM', 'APPLICATION', 'MEDIA']},
            {to: '/admin/products', label: t('admin.nav.products', '상품'), features: ['PRODUCT']},
            {to: '/admin/reservation-items', label: t('admin.nav.reservationItems', '예약 상품'), features: ['PROGRAM']},
            {to: '/admin/streaming', label: t('admin.nav.streaming', '스트리밍/교육과정'), features: ['MEDIA']},
            {to: '/admin/discounts', label: t('admin.nav.discounts', '할인'), features: ['PRODUCT']},
            {to: '/admin/coupons', label: t('admin.nav.coupons', '쿠폰'), features: ['PRODUCT']},
            {to: '/admin/shipping', label: t('admin.nav.shipping', '배송비'), features: ['PRODUCT']},
            {to: '/admin/points', label: t('admin.nav.points', '포인트'), features: ['PRODUCT']},
            {to: '/admin/tax', label: t('admin.nav.tax', '세금'), features: ['PRODUCT']},
            {to: '/admin/exchange-rates', label: t('admin.nav.exchangeRates', '환율'), features: ['PRODUCT']},
            {to: '/admin/applications', label: t('admin.nav.applications', '신청·지원'), features: ['APPLICATION']},
            {to: '/admin/orders', label: t('admin.nav.orders', '주문'), features: ['PRODUCT']},
            {to: '/admin/reservations', label: t('admin.nav.reservations', '예약'), features: ['PROGRAM']},
            {to: '/admin/payments', label: t('admin.nav.payments', '결제'), features: ['PRODUCT', 'PROGRAM']},
            {to: '/admin/accounting', label: t('admin.nav.accounting', '회계'), features: ['PRODUCT']},
            {to: '/admin/reviews', label: t('admin.nav.reviews', '리뷰'), features: ['PRODUCT', 'PROGRAM']},
            {to: '/admin/marketing', label: t('admin.nav.marketing', '알림')},
        ],
    },
    {
        title: t('admin.navGroup.cs', 'CS'),
        area: 'CS',
        items: [
            {to: '/admin/order-returns', label: t('admin.nav.orderReturns', '반품·교환'), features: ['PRODUCT']},
            {to: '/admin/repairs', label: t('admin.nav.repairs', '수리·AS 접수'), features: ['APPLICATION']},
        ],
    },
    {
        title: t('admin.navGroup.public', '공공'),
        area: 'PUBLIC',
        items: [
            {to: '/admin/reports', label: t('admin.nav.reports', '신고'), features: ['REPORT']},
            {to: '/admin/satisfactions', label: t('admin.nav.satisfactions', '만족도 조사'), features: ['SATISFACTION']},
        ],
    },
    {
        title: t('admin.navGroup.common', '공통'),
        area: 'COMMON',
        items: [
            {to: '/admin/members', label: t('admin.nav.members', '회원'), area: 'MEMBER'},
            {to: '/admin/grades', label: t('admin.nav.grades', '회원 등급'), sub: true, area: 'MEMBER'},
            {to: '/admin/join-settings', label: t('admin.nav.joinSettings', '회원가입 설정'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/privacy-policy', label: t('admin.nav.privacyPolicy', '개인정보처리방침'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/privacy-consent', label: t('admin.nav.privacyConsent', '개인정보 수집·이용 동의'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/terms-of-service', label: t('admin.nav.termsOfService', '이용약관'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/operation-policy', label: t('admin.nav.operationPolicy', '운영정책'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/youth-protection-policy', label: t('admin.nav.youthProtectionPolicy', '청소년보호정책'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/mail-reception', label: t('admin.nav.mailReception', '이메일 수신 동의'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/sms-reception', label: t('admin.nav.smsReception', 'SMS 수신 동의'), sub: true, area: 'MEMBER'},
            {to: '/admin/policies/marketing-usage', label: t('admin.nav.marketingUsage', '마케팅 활용 동의'), sub: true, area: 'MEMBER'},
            {to: '/admin/chatbot', label: t('admin.nav.chatbot', '챗봇'), features: ['CHATBOT']},
            {to: '/admin/regexes', label: t('admin.nav.regexes', '정규식')},
            {to: '/admin/codes', label: t('admin.nav.codes', '공통 코드')},
            {to: '/admin/i18n', label: t('admin.nav.i18n', '국제화')},
            {to: '/admin/files', label: t('admin.nav.files', '파일')},
            {to: '/admin/audit-logs', label: t('admin.nav.auditLogs', '작업 로그')},
            {to: '/admin/identity-verification', label: t('admin.nav.identityVerification', '본인인증')},
            {to: '/admin/postcode', label: t('admin.nav.postcode', '우편번호 API')},
        ],
    },
    {
        title: t('admin.navGroup.security', '보안'),
        area: 'SECURITY',
        items: [
            {to: '/admin/admins', label: t('admin.nav.admins', '관리자 관리'), superOnly: true},
            {to: '/admin/allowed-ips', label: t('admin.nav.allowedIps', '허용 IP')},
            {to: '/admin/login-security', label: t('admin.nav.loginSecurity', '로그인 보안')},
            {to: '/admin/api-keys', label: t('admin.nav.apiKeys', 'API 키 관리')},
            {to: '/admin/password-hash', label: t('admin.nav.passwordHash', '해시 알고리즘 관리')},
            {to: '/admin/crypto-key', label: t('admin.nav.cryptoKey', '암호화 키 관리')},
        ],
    },
];

export function AdminLayout() {
    return (
        <I18nProvider storageKey="cms.adminLanguage">
            <SiteProvider>
                <AdminFrame/>
            </SiteProvider>
        </I18nProvider>
    );
}

/** 핵심 콘텐츠(메뉴·배너·컨텐츠·게시판·강의·예약상품)를 관리할 사이트를 고른다. 바꾸면 그 사이트 기준으로 다시 불러오려고 새로고침한다. */
function SiteSwitcher() {
    const {t} = useI18n();
    const [sites, setSites] = useState<Site[]>([]);
    useEffect(() => {
        adminApi.sites.search({size: 200}).then((page) => setSites(page?.content ?? [])).catch(() => setSites([]));
    }, []);
    if (sites.length <= 1) {
        return null;
    }
    const current = getApiSiteCode() ?? '';
    const handleChange = (code: string) => {
        try {
            if (code) {
                localStorage.setItem(ADMIN_SITE_CODE_KEY, code);
            } else {
                localStorage.removeItem(ADMIN_SITE_CODE_KEY);
            }
        } catch {
            // 브라우저 저장소를 못 쓰면 이 새로고침 동안만 적용된다.
        }
        window.location.reload();
    };
    return (
        <select className="admin-site-switcher" value={current} onChange={(e) => handleChange(e.target.value)}
                aria-label={t('admin.layout.siteSwitcher', '작업 사이트')}>
            {sites.map((site) => <option key={site.id} value={site.code ?? ''}>{site.name}</option>)}
        </select>
    );
}

function AdminFrame() {
    const {user, logout} = useAuth();
    const {name, isEnabled} = useSite();
    const {t, language, languages, setLanguage} = useI18n();
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
    const visibleGroups = menuGroups(t)
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
                <Link to="/admin" className="brand">{name} {t('admin.layout.title', '관리자')}</Link>
                <div className="admin-account">
                    <SiteSwitcher/>
                    <ColorModeToggle mode={colorMode} onChange={setColorMode}/>
                    <span className="muted">{user?.name}</span>
                    <a href={getApiSiteCode() ? `/s/${getApiSiteCode()}/` : '/'}>{t('admin.layout.goToSite', '사이트로 이동')}</a>
                    <button type="button" onClick={handleLogout}>{t('nav.logout', '로그아웃')}</button>
                    {languages.length > 1 && (
                        <select className="language-select" aria-label={t('language.label', '언어')} value={language}
                                onChange={(e) => setLanguage(e.target.value)}>
                            {languages.map((code) => <option key={code} value={code}>{LANGUAGE_NAMES[code] ?? code}</option>)}
                        </select>
                    )}
                </div>
            </header>
            <aside className="admin-sidebar">
                <nav aria-label={t('admin.layout.menuLabel', '관리자 메뉴')}>
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
