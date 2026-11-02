import {createBrowserRouter} from 'react-router';
import {AuthZone} from './auth/AuthZone';
import {RequireRole} from './auth/RequireRole';
import {AdminLayout} from './components/AdminLayout';
import {RequireFeature} from './components/SiteContext';
import {SiteLayout} from './components/SiteLayout';
import {CodePage} from './pages/admin/CodePage';
import {CryptoKeyPage} from './pages/admin/CryptoKeyPage';
import {
    ArchiveAdminPage,
    ArchiveArticleAdminPage,
    BoardAdminPage,
    ContentAdminPage,
    QuestionAdminPage
} from './pages/admin/ContentAdminPages';
import {DashboardPage} from './pages/admin/DashboardPage';
import {FilePage} from './pages/admin/FilePage';
import {MapAdminPage} from './pages/admin/MapAdminPage';
import {ReportAdminPage, SatisfactionAdminPage} from './pages/admin/GovAdminPages';
import {MemberDetailPage} from './pages/admin/MemberDetailPage';
import {MemberListPage} from './pages/admin/MemberListPage';
import {PolicyAdminPage} from './pages/admin/PolicyAdminPage';
import {
    DiscountAdminPage,
    OrderAdminPage,
    PaymentAdminPage,
    ProductAdminPage,
    ProgramAdminPage,
    ReservationAdminPage,
    ReviewAdminPage
} from './pages/admin/ShopAdminPages';
import {AuditLogPage, BannerAdminPage, ConfigAdminPage, MenuAdminPage, SiteAdminPage} from './pages/admin/SiteAdminPages';
import {StatisticsPage} from './pages/admin/StatisticsPage';
import {BoardArticlePage, BoardListPage, BoardWritePage} from './pages/board/BoardPages';
import {QnaDetailPage, QnaListPage, QnaWritePage} from './pages/board/QnaPages';
import {ArchiveArticlePage, ArchiveListPage, ContentPage} from './pages/ContentPages';
import {HomePage, NotFoundPage} from './pages/HomePage';
import {JoinPage} from './pages/JoinPage';
import {AdminLoginPage, LoginPage} from './pages/LoginPage';
import {MapPage} from './pages/MapPage';
import {MyPage} from './pages/MyPage';
import {PrivacyPolicyPage} from './pages/PolicyPage';
import {MyReportsPage, ReportDetailPage, ReportWritePage} from './pages/ReportPages';
import {
    CheckoutPage,
    MyOrdersPage,
    MyReservationsPage,
    ProductDetailPage,
    ProductListPage,
    ProgramDetailPage,
    ProgramListPage
} from './pages/shop/ShopPages';

export const routes = [
    {
        element: <AuthZone zone="site"/>,
        children: [
            {
                element: <SiteLayout/>,
                children: [
                    {path: '/', element: <HomePage/>},
                    {path: '/login', element: <LoginPage userType="MEMBER"/>},
                    {path: '/join', element: <JoinPage/>},
                    {path: '/privacy', element: <PrivacyPolicyPage/>},
                    {
                        element: <RequireFeature feature="CONTENT"/>,
                        children: [{path: '/contents/:id', element: <ContentPage/>}],
                    },
                    {
                        element: <RequireFeature feature="BOARD"/>,
                        children: [
                            {path: '/boards/:boardId', element: <BoardListPage/>},
                            {path: '/boards/:boardId/write', element: <BoardWritePage/>},
                            {path: '/boards/:boardId/articles/:articleId', element: <BoardArticlePage/>},
                            {path: '/boards/:boardId/articles/:articleId/edit', element: <BoardWritePage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="QNA"/>,
                        children: [
                            {path: '/qna', element: <QnaListPage/>},
                            {path: '/qna/write', element: <QnaWritePage/>},
                            {path: '/qna/:id', element: <QnaDetailPage/>},
                            {path: '/qna/:id/edit', element: <QnaWritePage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="ARCHIVE"/>,
                        children: [
                            {path: '/archives/:archiveId', element: <ArchiveListPage/>},
                            {path: '/archives/:archiveId/articles/:articleId', element: <ArchiveArticlePage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="MAP"/>,
                        children: [{path: '/map', element: <MapPage/>}],
                    },
                    {
                        element: <RequireFeature feature="PRODUCT"/>,
                        children: [
                            {path: '/products', element: <ProductListPage/>},
                            {path: '/products/:id', element: <ProductDetailPage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="PROGRAM"/>,
                        children: [
                            {path: '/programs', element: <ProgramListPage/>},
                            {path: '/programs/:id', element: <ProgramDetailPage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="REPORT"/>,
                        children: [
                            {path: '/reports/new', element: <ReportWritePage/>},
                            {path: '/reports/:id', element: <ReportDetailPage/>},
                        ],
                    },
                    {
                        element: <RequireRole role="MEMBER"/>,
                        children: [
                            {path: '/mypage', element: <MyPage/>},
                            {
                                element: <RequireFeature feature="PRODUCT"/>,
                                children: [{path: '/mypage/orders', element: <MyOrdersPage/>}],
                            },
                            {
                                element: <RequireFeature feature="PROGRAM"/>,
                                children: [{path: '/mypage/reservations', element: <MyReservationsPage/>}],
                            },
                            {
                                element: <RequireFeature feature="REPORT"/>,
                                children: [{path: '/mypage/reports', element: <MyReportsPage/>}],
                            },
                            {path: '/checkout/:type/:id', element: <CheckoutPage/>},
                        ],
                    },
                    {path: '*', element: <NotFoundPage/>},
                ],
            },
        ],
    },
    {
        element: <AuthZone zone="admin"/>,
        children: [
            {path: '/admin/login', element: <AdminLoginPage/>},
            {
                element: <RequireRole role="ADMIN"/>,
                children: [
                    {
                        path: '/admin',
                        element: <AdminLayout/>,
                        children: [
                            {index: true, element: <DashboardPage/>},
                            {path: 'statistics', element: <StatisticsPage/>},
                            {path: 'sites', element: <SiteAdminPage/>},
                            {path: 'menus', element: <MenuAdminPage/>},
                            {path: 'banners', element: <BannerAdminPage/>},
                            {path: 'configs', element: <ConfigAdminPage/>},
                            {
                                element: <RequireFeature feature="CONTENT"/>,
                                children: [{path: 'contents', element: <ContentAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="BOARD"/>,
                                children: [{path: 'boards', element: <BoardAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="QNA"/>,
                                children: [{path: 'questions', element: <QuestionAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="ARCHIVE"/>,
                                children: [
                                    {path: 'archives', element: <ArchiveAdminPage/>},
                                    {path: 'archives/:id', element: <ArchiveArticleAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="MAP"/>,
                                children: [{path: 'map', element: <MapAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="PRODUCT"/>,
                                children: [
                                    {path: 'products', element: <ProductAdminPage/>},
                                    {path: 'discounts', element: <DiscountAdminPage/>},
                                    {path: 'orders', element: <OrderAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="PROGRAM"/>,
                                children: [
                                    {path: 'programs', element: <ProgramAdminPage/>},
                                    {path: 'reservations', element: <ReservationAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature={['PRODUCT', 'PROGRAM']}/>,
                                children: [
                                    {path: 'payments', element: <PaymentAdminPage/>},
                                    {path: 'reviews', element: <ReviewAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="REPORT"/>,
                                children: [{path: 'reports', element: <ReportAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="SATISFACTION"/>,
                                children: [{path: 'satisfactions', element: <SatisfactionAdminPage/>}],
                            },
                            {path: 'members', element: <MemberListPage/>},
                            {path: 'members/:id', element: <MemberDetailPage/>},
                            {path: 'policies/:type', element: <PolicyAdminPage/>},
                            {path: 'codes', element: <CodePage/>},
                            {path: 'files', element: <FilePage/>},
                            {path: 'audit-logs', element: <AuditLogPage/>},
                            {path: 'crypto-key', element: <CryptoKeyPage/>},
                        ],
                    },
                ],
            },
        ],
    },
];

export const router = createBrowserRouter(routes);
