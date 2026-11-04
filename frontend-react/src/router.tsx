import {createBrowserRouter} from 'react-router';
import {AuthZone} from './auth/AuthZone';
import {RequireRole} from './auth/RequireRole';
import {AdminLayout} from './components/AdminLayout';
import {RequireFeature} from './components/SiteContext';
import {SiteLayout} from './components/SiteLayout';
import {CodePage} from './pages/admin/CodePage';
import {ApiKeyPage} from './pages/admin/ApiKeyPage';
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
import {ApplicationAdminPage} from './pages/admin/ApplicationAdminPage';
import {MapAdminPage} from './pages/admin/MapAdminPage';
import {MediaAdminPage} from './pages/admin/MediaAdminPage';
import {ReportAdminPage, SatisfactionAdminPage} from './pages/admin/GovAdminPages';
import {MemberDetailPage} from './pages/admin/MemberDetailPage';
import {MemberJoinSettingPage} from './pages/admin/MemberJoinSettingPage';
import {ChatbotAdminPage} from './pages/admin/ChatbotAdminPage';
import {MemberListPage} from './pages/admin/MemberListPage';
import {I18nAdminPage} from './pages/admin/I18nAdminPage';
import {PasswordHashPage} from './pages/admin/PasswordHashPage';
import {PolicyAdminPage} from './pages/admin/PolicyAdminPage';
import {RegexAdminPage} from './pages/admin/RegexAdminPage';
import {GradeAdminPage} from './pages/admin/GradeAdminPage';
import {ShippingPolicyPage} from './pages/admin/ShippingPolicyPage';
import {
    DiscountAdminPage,
    OrderAdminPage,
    SubscriptionAdminPage,
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
import {OrderFormPage, ReservationFormPage} from './pages/shop/CheckoutFormPages';
import {CartOrderPage, CartPage} from './pages/shop/CartPages';
import {MyCouponsPage, MyPointsPage} from './pages/shop/RewardPages';
import {MyDownloadsPage} from './pages/shop/DigitalPages';
import {CouponAdminPage, PointPolicyPage} from './pages/admin/RewardAdminPages';
import {JoinPage} from './pages/JoinPage';
import {SocialJoinPage} from './pages/SocialJoinPage';
import {AdminLoginPage, LoginPage} from './pages/LoginPage';
import {ApplicationDetailPage, ApplicationListPage, MyApplicationsPage} from './pages/ApplicationPages';
import {MapPage} from './pages/MapPage';
import {MediaDetailPage, MediaListPage, MyMediaPage} from './pages/MediaPages';
import {MyPage, PasswordChangePage, QuitPage} from './pages/MyPage';
import {PrivacyPolicyPage} from './pages/PolicyPage';
import {MyReportsPage, ReportDetailPage, ReportWritePage} from './pages/ReportPages';
import {
    CheckoutPage,
    MyOrdersPage,
    MyReservationsPage,
    MySubscriptionsPage,
    ProductDetailPage,
    ProductListPage,
    ProgramDetailPage,
    ProgramListPage,
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
                    {path: '/join/social', element: <SocialJoinPage/>},
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
                        element: <RequireFeature feature="MEDIA"/>,
                        children: [
                            {path: '/media', element: <MediaListPage/>},
                            {path: '/media/:id', element: <MediaDetailPage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="APPLICATION"/>,
                        children: [
                            {path: '/applications', element: <ApplicationListPage/>},
                            {path: '/applications/:id', element: <ApplicationDetailPage/>},
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
                            {path: '/mypage/password', element: <PasswordChangePage/>},
                            {path: '/mypage/quit', element: <QuitPage/>},
                            {
                                element: <RequireFeature feature="PRODUCT"/>,
                                children: [
                                    {path: '/mypage/orders', element: <MyOrdersPage/>},
                                    {path: '/mypage/subscriptions', element: <MySubscriptionsPage/>},
                                    {path: '/orders/new', element: <OrderFormPage/>},
                                    {path: '/cart', element: <CartPage/>},
                                    {path: '/mypage/points', element: <MyPointsPage/>},
                                    {path: '/mypage/coupons', element: <MyCouponsPage/>},
                                    {path: '/mypage/downloads', element: <MyDownloadsPage/>},
                                    {path: '/orders/cart', element: <CartOrderPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="PROGRAM"/>,
                                children: [
                                    {path: '/mypage/reservations', element: <MyReservationsPage/>},
                                    {path: '/reservations/new', element: <ReservationFormPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="REPORT"/>,
                                children: [{path: '/mypage/reports', element: <MyReportsPage/>}],
                            },
                            {
                                element: <RequireFeature feature="APPLICATION"/>,
                                children: [{path: '/mypage/applications', element: <MyApplicationsPage/>}],
                            },
                            {
                                element: <RequireFeature feature="MEDIA"/>,
                                children: [{path: '/mypage/media', element: <MyMediaPage/>}],
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
                                    {path: 'shipping', element: <ShippingPolicyPage/>},
                                    {path: 'orders', element: <OrderAdminPage/>},
                                    {path: 'subscriptions', element: <SubscriptionAdminPage/>},
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
                                element: <RequireFeature feature="MEDIA"/>,
                                children: [{path: 'media', element: <MediaAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="APPLICATION"/>,
                                children: [{path: 'applications', element: <ApplicationAdminPage/>}],
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
                            {path: 'join-settings', element: <MemberJoinSettingPage/>},
                            {path: 'grades', element: <GradeAdminPage/>},
                            {path: 'points', element: <PointPolicyPage/>},
                            {path: 'coupons', element: <CouponAdminPage/>},
                            {path: 'chatbot', element: <ChatbotAdminPage/>},
                            {path: 'policies/:type', element: <PolicyAdminPage/>},
                            {path: 'regexes', element: <RegexAdminPage/>},
                            {path: 'codes', element: <CodePage/>},
                            {path: 'i18n', element: <I18nAdminPage/>},
                            {path: 'files', element: <FilePage/>},
                            {path: 'audit-logs', element: <AuditLogPage/>},
                            {path: 'password-hash', element: <PasswordHashPage/>},
                            {path: 'crypto-key', element: <CryptoKeyPage/>},
                            {path: 'api-keys', element: <ApiKeyPage/>},
                        ],
                    },
                ],
            },
        ],
    },
];

export const router = createBrowserRouter(routes);
