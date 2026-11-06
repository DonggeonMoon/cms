import {createBrowserRouter} from 'react-router';
import {AuthZone} from './auth/AuthZone';
import {RequireRole} from './auth/RequireRole';
import {AdminLayout} from './components/AdminLayout';
import {RequireFeature} from './components/SiteContext';
import {SiteLayout} from './components/SiteLayout';
import {CodePage} from './pages/admin/CodePage';
import {ApiKeyPage} from './pages/admin/ApiKeyPage';
import {AllowedIpPage} from './pages/admin/AllowedIpPage';
import {CustomerRecordAdminPage} from './pages/admin/CustomerRecordAdminPage';
import {ClassAttendanceAdminPage} from './pages/admin/ClassAttendanceAdminPage';
import {CheckInAdminPage} from './pages/admin/CheckInAdminPage';
import {ReceptionQueueAdminPage} from './pages/admin/ReceptionQueueAdminPage';
import {MyWishlistPage} from './pages/shop/WishlistPages';
import {MyRecordsPage, MyAttendancePage} from './pages/shop/RecordPages';
import {MyRepairsPage} from './pages/shop/RepairPages';
import {MyVotesPage} from './pages/shop/VotePages';
import {MyNotificationsPage} from './pages/shop/NotificationPages';
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
import {RepairAdminPage} from './pages/admin/RepairAdminPage';
import {VoteAdminPage} from './pages/admin/VoteAdminPage';
import {MapAdminPage} from './pages/admin/MapAdminPage';
import {StreamingAdminPage} from './pages/admin/StreamingAdminPage';
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
import {TaxSettingPage} from './pages/admin/TaxSettingPage';
import {ExchangeRateAdminPage} from './pages/admin/ExchangeRateAdminPage';
import {MarketingAdminPage} from './pages/admin/MarketingAdminPage';
import {StockMovementAdminPage} from './pages/admin/StockMovementAdminPage';
import {AccountingAdminPage} from './pages/admin/AccountingAdminPage';
import {
    DiscountAdminPage,
    OrderReturnAdminPage,
    OrdersAdminPage,
    PaymentAdminPage,
    ProductAdminPage,
    ReservationItemAdminPage,
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
import {CategoryAdminPage} from './pages/admin/CategoryAdminPage';
import {AdminAccountPage} from './pages/admin/AdminAccountPage';
import {StoreOrderQueuePage, StoreSettingPage} from './pages/admin/StoreAdminPages';
import {JoinPage} from './pages/JoinPage';
import {SocialJoinPage} from './pages/SocialJoinPage';
import {AdminLoginPage, LoginPage} from './pages/LoginPage';
import {ApplicationDetailPage, ApplicationJudgingPage, ApplicationListPage, MyApplicationsPage} from './pages/ApplicationPages';
import {MapPage} from './pages/MapPage';
import {StreamingDetailPage, StreamingListPage, MyStreamingPage} from './pages/StreamingPages';
import {CourseDetailPage, CourseListPage, MyCoursesPage} from './pages/CoursePages';
import {CourseAdminPage} from './pages/admin/CourseAdminPage';
import {MyPage, PasswordChangePage, QuitPage} from './pages/MyPage';
import {PolicyDocumentPage, PrivacyPolicyPage} from './pages/PolicyPage';
import {MyReportsPage, ReportDetailPage, ReportWritePage} from './pages/ReportPages';
import {
    CheckoutPage,
    MyOrdersPage,
    MyPassesPage,
    MyLicenseKeysPage,
    MyReservationsPage,
    MyWalletPage,
    MySubscriptionsPage,
    ProductDetailPage,
    ProductListPage,
    ReservationItemDetailPage,
    ReservationItemListPage,
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
                    {path: '/terms', element: <PolicyDocumentPage type="TERMS_OF_SERVICE"/>},
                    {path: '/operation-policy', element: <PolicyDocumentPage type="OPERATION_POLICY"/>},
                    {path: '/youth-protection', element: <PolicyDocumentPage type="YOUTH_PROTECTION_POLICY"/>},
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
                            {path: '/reservation-items', element: <ReservationItemListPage/>},
                            {path: '/reservation-items/:id', element: <ReservationItemDetailPage/>},
                        ],
                    },
                    {
                        element: <RequireFeature feature="MEDIA"/>,
                        children: [
                            {path: '/streaming', element: <StreamingListPage/>},
                            {path: '/streaming/:id', element: <StreamingDetailPage/>},
                            {path: '/courses', element: <CourseListPage/>},
                            {path: '/courses/:id', element: <CourseDetailPage/>},
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
                            {path: '/mypage/notifications', element: <MyNotificationsPage/>},
                            {path: '/mypage/password', element: <PasswordChangePage/>},
                            {path: '/mypage/quit', element: <QuitPage/>},
                            {
                                element: <RequireFeature feature="PRODUCT"/>,
                                children: [
                                    {path: '/mypage/orders', element: <MyOrdersPage/>},
                                    {path: '/mypage/subscriptions', element: <MySubscriptionsPage/>},
                                    {path: '/mypage/passes', element: <MyPassesPage/>},
                                    {path: '/mypage/wallet', element: <MyWalletPage/>},
                                    {path: '/mypage/license-keys', element: <MyLicenseKeysPage/>},
                                    {path: '/mypage/wishlist', element: <MyWishlistPage/>},
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
                                    {path: '/mypage/records', element: <MyRecordsPage/>},
                                    {path: '/mypage/attendance/:subscriptionId', element: <MyAttendancePage/>},
                                    {path: '/reservations/new', element: <ReservationFormPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="REPORT"/>,
                                children: [{path: '/mypage/reports', element: <MyReportsPage/>}],
                            },
                            {
                                element: <RequireFeature feature="APPLICATION"/>,
                                children: [
                                    {path: '/mypage/applications', element: <MyApplicationsPage/>},
                                    {path: '/mypage/repairs', element: <MyRepairsPage/>},
                                    {path: '/mypage/votes', element: <MyVotesPage/>},
                                    {path: '/applications/:id/judging', element: <ApplicationJudgingPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="MEDIA"/>,
                                children: [{path: '/mypage/streaming', element: <MyStreamingPage/>}, {path: '/mypage/courses', element: <MyCoursesPage/>}],
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
                                    {path: 'product-categories', element: <CategoryAdminPage/>},
                                    {path: 'discounts', element: <DiscountAdminPage/>},
                                    {path: 'shipping', element: <ShippingPolicyPage/>},
                                    {path: 'tax', element: <TaxSettingPage/>},
                                    {path: 'exchange-rates', element: <ExchangeRateAdminPage/>},
                                    {path: 'marketing', element: <MarketingAdminPage/>},
                                    {path: 'store', element: <StoreSettingPage/>},
                                    {path: 'store-orders', element: <StoreOrderQueuePage/>},
                                    {path: 'orders', element: <OrdersAdminPage/>},
                                    {path: 'order-returns', element: <OrderReturnAdminPage/>},
                                    {path: 'stock-movements', element: <StockMovementAdminPage/>},
                                    {path: 'accounting', element: <AccountingAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="PROGRAM"/>,
                                children: [
                                    {path: 'reservation-items', element: <ReservationItemAdminPage/>},
                                    {path: 'reservations', element: <ReservationAdminPage/>},
                                    {path: 'customer-records', element: <CustomerRecordAdminPage/>},
                                    {path: 'check-in', element: <CheckInAdminPage/>},
                                    {path: 'reception-queue', element: <ReceptionQueueAdminPage/>},
                                    {path: 'class-attendance', element: <ClassAttendanceAdminPage/>},
                                ],
                            },
                            {
                                element: <RequireFeature feature="MEDIA"/>,
                                children: [{path: 'streaming', element: <StreamingAdminPage/>}, {path: 'courses', element: <CourseAdminPage/>}],
                            },
                            {
                                element: <RequireFeature feature="APPLICATION"/>,
                                children: [
                                    {path: 'applications', element: <ApplicationAdminPage/>},
                                    {path: 'repairs', element: <RepairAdminPage/>},
                                    {path: 'votes', element: <VoteAdminPage/>},
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
                            {path: 'allowed-ips', element: <AllowedIpPage/>},
                            {path: 'admins', element: <AdminAccountPage/>},
                        ],
                    },
                ],
            },
        ],
    },
];

export const router = createBrowserRouter(routes);
