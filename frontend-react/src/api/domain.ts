import type {Spec} from './categories';
import type {FulfillmentType} from './store';
import type {FileInfo, MediaType} from './types';

export type MenuType = 'CONTENT' | 'BOARD' | 'ARCHIVE' | 'LINK' | 'FEATURE';

export interface Menu {
    id: number;
    parentId: number | null;
    name: string;
    type: MenuType;
    depth: number;
    sequence: number;
    link: string | null;
    targetId: number | null;
    feature: SiteFeature | null;
    isUsing: boolean;
    children: Menu[];
}

export type SiteFeature = 'CONTENT' | 'BOARD' | 'ARCHIVE' | 'QNA' | 'MAP' | 'PRODUCT' | 'PROGRAM' | 'REPORT' | 'SATISFACTION' | 'APPLICATION' | 'MEDIA' | 'CHATBOT';

export const SITE_FEATURES: { value: SiteFeature; label: string }[] = [
    {value: 'CONTENT', label: '소개 페이지(컨텐츠)'},
    {value: 'BOARD', label: '게시판'},
    {value: 'ARCHIVE', label: '아카이브'},
    {value: 'QNA', label: '질문답변'},
    {value: 'MAP', label: '지도'},
    {value: 'PRODUCT', label: '상품'},
    {value: 'PROGRAM', label: '예약'},
    {value: 'APPLICATION', label: '신청·지원'},
    {value: 'MEDIA', label: '스트리밍'},
    {value: 'REPORT', label: '신고'},
    {value: 'SATISFACTION', label: '만족도 조사'},
    {value: 'CHATBOT', label: '챗봇'},
];

export type EnrollmentStatus = 'IN_PROGRESS' | 'COMPLETED';

export interface MediaProgress {
    status: EnrollmentStatus;
    progressRate: number;
    watchedSeconds: number;
    lastPositionSeconds: number;
    startedAt: string;
    lastWatchedAt: string | null;
    completedAt: string | null;
}

export interface MediaItem {
    id: number;
    accessGradeId: number | null;
    title: string;
    description: string | null;
    fileId: number;
    fileName: string | null;
    mediaType: MediaType | null;
    mediaUrl: string;
    thumbnailFileId: number | null;
    thumbnailUrl: string | null;
    posterUrl: string | null;
    durationSeconds: number;
    completionRate: number;
    isUsing: boolean;
    createdAt: string;
    myProgress: MediaProgress | null;
}

export interface MyMedia {
    mediaId: number;
    title: string;
    mediaType: MediaType | null;
    thumbnailUrl: string | null;
    durationSeconds: number;
    available: boolean;
    progress: MediaProgress | null;
}

export interface Learner {
    memberId: number;
    username: string;
    status: EnrollmentStatus;
    progressRate: number;
    watchedSeconds: number;
    startedAt: string;
    lastWatchedAt: string | null;
    completedAt: string | null;
}

export interface Site {
    id: number;
    name: string;
    description: string | null;
    domain: string | null;
    theme: string | null;
    type: MenuType | null;
    locale: string | null;
    isDefault: boolean;
    homeContentId: number | null;
    features: Record<SiteFeature, boolean>;
    darkModeEnabled?: boolean;
    faviconFileId?: number | null;
    faviconUrl?: string | null;
    seoTitle?: string | null;
    metaDescription?: string | null;
    metaKeywords?: string | null;
    ogImageFileId?: number | null;
    ogImageUrl?: string | null;
    searchIndexing?: boolean;
    googleVerification?: string | null;
    naverVerification?: string | null;
}

export type OptionType = 'TEXT' | 'RADIO' | 'CHECKBOX' | 'TEXTAREA';

export interface GuestPostingPolicy {
    allowed: boolean;
    collectPhone: boolean;
    collectEmail: boolean;
}

export type ChatbotProvider = 'OPENAI' | 'GEMINI' | 'CLAUDE';

export interface ChatbotSetting {
    enabled: boolean;
    provider: ChatbotProvider;
    apiKeyId: number | null;
    model: string | null;
    instructions: string;
    welcomeMessage: string | null;
    dailyLimitPerVisitor: number;
    defaultInstructions: string;
}

export interface ChatMessage {
    role: 'USER' | 'ASSISTANT';
    content: string;
}

export interface ApiKeyOption {
    id: number;
    name: string;
}

export interface ApiKey {
    id: number;
    name: string;
    value: string;
    description: string;
    modifiedAt: string;
}

export interface Config {
    id: number;
    key: string;
    value: string;
    description: string;
    type: OptionType;
    isHidden: boolean;
}

export interface Banner {
    id: number;
    type: 'BANNER' | 'POPUP';
    order: number;
    name: string;
    description: string | null;
    link: string | null;
    opensNewWindow: boolean;
    startDateTime: string | null;
    endDateTime: string | null;
    isUsing: boolean;
    width: number | null;
    height: number | null;
    top: number | null;
    left: number | null;
    fileId: number | null;
    imageUrl: string | null;
    backgroundColor: string | null;
    textColor: string | null;
    isModal: boolean;
    dismissDays: number;
}

export interface AuditLog {
    id: number;
    username: string;
    uri: string;
    handler: string;
    content: string;
    ipAddress: string;
    createdAt: string;
}

export type BoardType = 'NORMAL' | 'GALLERY';

export interface Board {
    id: number;
    readGradeId: number | null;
    writeGradeId: number | null;
    name: string;
    description: string | null;
    type: BoardType;
    isMemberAccessible: boolean;
    isMemberReadable: boolean;
    isMemberWritable: boolean;
    isGuestAccessible: boolean;
    isGuestReadable: boolean;
    isGuestWritable: boolean;
    supportsNotice: boolean;
    supportsSecret: boolean;
    supportsComment: boolean;
    supportsLike: boolean;
    supportsAttachment: boolean;
    maximumFileCount: number;
    pageSize: number;
    permissions: { canList: boolean; canRead: boolean; canWrite: boolean; isAdmin: boolean };
}

export interface ArticleSummary {
    id: number;
    title: string;
    authorName: string;
    isNotice: boolean;
    isSecret: boolean;
    isHidden: boolean;
    hit: number;
    likes: number;
    commentCount: number;
    thumbnailUrl: string | null;
    createdAt: string;
}

export interface ArticleDetail {
    id: number;
    boardId: number;
    title: string;
    contentHtml: string;
    authorName: string;
    authorType: 'ADMIN' | 'MEMBER' | 'GUEST';
    isNotice: boolean;
    noticeStartDateTime: string | null;
    noticeEndDateTime: string | null;
    isSecret: boolean;
    isHidden: boolean;
    hit: number;
    likes: number;
    files: FileInfo[];
    canModify: boolean;
    createdAt: string;
    modifiedAt: string;
}

export interface Comment {
    id: number;
    parentId: number | null;
    depth: number;
    content: string;
    authorName: string | null;
    authorType: 'ADMIN' | 'MEMBER' | 'GUEST';
    deleted: boolean;
    canDelete: boolean;
    canEdit: boolean;
    createdAt: string;
}

export interface QuestionSummary {
    id: number;
    title: string;
    authorName: string;
    isSecret: boolean;
    answered: boolean;
    hit: number;
    createdAt: string;
}

export interface QuestionDetail {
    id: number;
    title: string;
    contentHtml: string;
    authorName: string;
    email: string | null;
    isSecret: boolean;
    hit: number;
    canModify: boolean;
    answer: { contentHtml: string; authorName: string; createdAt: string; modifiedAt: string } | null;
    createdAt: string;
}

export type PublishStatus = 'DRAFT' | 'PUBLISHED';

export interface ContentSummary {
    id: number;
    title: string;
}

export type ContentStatus = 'DRAFT' | 'IN_REVIEW' | 'REJECTED' | 'SCHEDULED' | 'PUBLISHED';

export interface Content {
    id: number;
    accessGradeId: number | null;
    title: string;
    contentHtml: string;
    status: ContentStatus;
    reviewRequestedBy: string | null;
    reviewRequestedAt: string | null;
    reviewedBy: string | null;
    reviewComment: string | null;
    publishAt: string | null;
    publishedAt: string | null;
    version: number;
    lastWorker: string | null;
    memo: string;
    createdAt: string;
    modifiedAt: string;
}

export interface Archive {
    id: number;
    accessGradeId: number | null;
    name: string;
    description: string | null;
    type: BoardType;
    supportsComment: boolean;
    supportsLike: boolean;
    pageSize: number;
}

export interface ArchiveArticleSummary {
    id: number;
    title: string;
    authorName: string;
    status: PublishStatus;
    hit: number;
    thumbnailUrl: string | null;
    createdAt: string;
}

export interface ArchiveArticleDetail {
    id: number;
    archiveId: number;
    title: string;
    contentHtml: string;
    authorName: string;
    status: PublishStatus;
    hit: number;
    likes: number;
    files: FileInfo[];
    createdAt: string;
    modifiedAt: string;
}

export interface ArchiveComment {
    id: number;
    parentId: number | null;
    depth: number;
    content: string;
    authorName: string | null;
    deleted: boolean;
    canDelete: boolean;
    canEdit: boolean;
    createdAt: string;
}

export interface Discount {
    id: number;
    productId: number | null;
    name: string;
    description: string | null;
    rate: number;
    isUsing: boolean;
}

export type ProductSaleType = 'PURCHASE' | 'SUBSCRIPTION' | 'DONATION' | 'QUOTE';
export type BillingCycle = 'MONTHLY' | 'YEARLY';

export interface ProductSummary {
    id: number;
    accessGradeId: number | null;
    grantGradeId: number | null;
    grantGradeDays: number | null;
    name: string;
    description: string | null;
    price: number;
    saleType: ProductSaleType;
    billingCycle: BillingCycle | null;
    availableQuantity: number | null;
    isEnabled: boolean;
    imageUrl: string | null;
    categoryId: number | null;
    storeOrder: boolean;
    createdAt: string;
}

export interface OptionGroup {
    name: string;
    values: string[];
}

export interface ProductOption {
    id: number;
    name: string;
    description: string | null;
    additionalPrice: number;
    optionValues: string[];
    stock: number | null;
    isEnabled: boolean;
}

export interface ProductDetail extends ProductSummary {
    imageFileId: number | null;
    contentHtml: string;
    optionGroups: OptionGroup[];
    requiresShipping: boolean;
    digitalFileCount: number;
    downloadLimit: number | null;
    downloadDays: number | null;
    digitalFiles: FileInfo[] | null;
    attributes: Record<string, string | number>;
    specs: Spec[];
    options: ProductOption[];
    discounts: Discount[];
    averageRating: number | null;
    donationPresets: number[];
    goalAmount: number | null;
    donation: { raisedAmount: number; supporterCount: number } | null;
}

export interface DigitalFile {
    id: number;
    name: string;
    size: number;
    downloadUrl: string;
}

export interface DigitalAccess {
    id: number;
    orderId: number;
    productId: number;
    productName: string;
    files: DigitalFile[];
    downloadLimit: number | null;
    downloadCount: number;
    remainingDownloads: number | null;
    expiresAt: string | null;
    revokedAt: string | null;
    usable: boolean;
    createdAt: string;
}

export interface Subscription {
    id: number;
    memberId: number;
    productId: number;
    productName: string;
    firstOrderId: number;
    price: number;
    billingCycle: BillingCycle;
    status: 'ACTIVE' | 'CANCELED';
    nextBillingDate: string | null;
    startedAt: string;
    canceledAt: string | null;
}

export type OrderStatus = 'QUOTE_REQUESTED' | 'QUOTED' | 'PENDING_PAYMENT' | 'PAID' | 'SHIPPING' | 'DELIVERED' | 'PREPARING' | 'READY'
    | 'COMPLETED' | 'CANCELED';

export interface Order {
    id: number;
    orderNumber: string;
    userId: number;
    productId: number;
    productName: string;
    saleType: ProductSaleType;
    subscriptionId: number | null;
    productOptionId: number | null;
    quantity: number;
    discountId: number | null;
    totalPrice: number | null;
    status: OrderStatus;
    customStatus: string | null;
    fulfillmentType: FulfillmentType;
    tableName: string | null;
    pickupNumber: number | null;
    requestMessage: string | null;
    quoteMessage: string | null;
    quotedAt: string | null;
    receiverName: string;
    phoneNumber: string;
    postalCode: string | null;
    address: string | null;
    detailAddress: string | null;
    createdAt: string;
    items: OrderItem[];
    itemsAmount: number | null;
    shippingFee: number | null;
    refundedAmount: number | null;
    courier: string | null;
    trackingNumber: string | null;
    shippedAt: string | null;
    couponIssueId: number | null;
    couponDiscount: number | null;
    pointsUsed: number;
    pointsEarned: number;
}

export interface OrderItem {
    id: number;
    productId: number;
    productOptionId: number | null;
    productName: string;
    optionName: string | null;
    unitPrice: number;
    quantity: number;
    amount: number;
    status: 'ORDERED' | 'CANCELED';
    refundedAmount: number | null;
    canceledAt: string | null;
    couponDiscount: number;
    pointsUsed: number;
    pointsEarned: number;
}

export interface CartItem {
    id: number;
    productId: number;
    productOptionId: number | null;
    productName: string;
    optionName: string | null;
    imageUrl: string | null;
    unitPrice: number;
    quantity: number;
    amount: number;
    stock: number | null;
    requiresShipping: boolean;
    storeOrder: boolean;
    available: boolean;
}

export interface Cart {
    items: CartItem[];
    itemsAmount: number;
    shippingFee: number;
    shippingRegion: string | null;
    freeShippingThreshold: number | null;
    totalPrice: number;
}

export interface RegionFee {
    name: string;
    postalCodeFrom: string;
    postalCodeTo: string;
    fee: number;
}

export interface ShippingPolicy {
    baseFee: number;
    freeThreshold: number | null;
    regionFees: RegionFee[];
    defaultCourier: string | null;
}

export type ReservationType = 'PROGRAM' | 'CONSULTATION' | 'RENTAL' | 'LODGING' | 'EVENT' | 'ITEM_RENTAL' | 'VISIT';
export type CellKind = 'SEAT' | 'ROOM' | 'SPACE' | 'AISLE' | 'STAGE' | 'EMPTY';

export interface LayoutCell {
    key: string;
    row: number;
    col: number;
    kind: CellKind;
    label: string;
    price: number | null;
}

export interface SpaceLayout {
    rows: number;
    cols: number;
    cells: LayoutCell[];
}

export interface ReservationSlot {
    startAt: string;
    endAt: string;
    remaining: number;
}

export interface Availability {
    takenCells: string[];
    slots: ReservationSlot[];
    remainingItems: number | null;
}

export interface Program {
    id: number;
    accessGradeId: number | null;
    name: string;
    description: string | null;
    imageFileId: number | null;
    imageUrl: string | null;
    price: number;
    reservationType: ReservationType;
    slotMinutes: number | null;
    openTime: string | null;
    closeTime: string | null;
    layout: SpaceLayout | null;
    maximumPerson: number | null;
    availablePerson: number | null;
    startDateTime: string | null;
    endDateTime: string | null;
    applicationStartDateTime: string | null;
    applicationEndDateTime: string | null;
    contentHtml: string;
    isEnabled: boolean;
    isOpen: boolean;
    averageRating: number | null;
    depositAmount: number | null;
    maxRentalDays: number | null;
}

export type ReservationStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELED';

export interface Reservation {
    id: number;
    reservationNumber: string;
    userId: number;
    programId: number;
    programName: string;
    reservationType: ReservationType;
    startAt: string | null;
    endAt: string | null;
    cells: string[];
    quantity: number;
    totalPrice: number;
    status: ReservationStatus;
    customStatus: string | null;
    applicantName: string;
    phoneNumber: string;
    depositAmount: number | null;
    returnedAt: string | null;
    depositRefunded: number | null;
    returnMemo: string | null;
    overdue: boolean;
    lateDays: number;
    visitPostalCode: string | null;
    visitAddress: string | null;
    visitDetailAddress: string | null;
    requestMemo: string | null;
    workerName: string | null;
    workerPhone: string | null;
    workerAssignedAt: string | null;
    workCompletedAt: string | null;
    workReport: string | null;
    workPhotoUrls: string[];
    createdAt: string;
}

export type PaymentTargetType = 'ORDER' | 'RESERVATION';
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'VIRTUAL_ACCOUNT' | 'MOBILE';

export interface Payment {
    id: number;
    paymentNumber: string | null;
    targetType: PaymentTargetType;
    targetId: number;
    memberId: number;
    amount: number;
    method: PaymentMethod;
    status: 'APPROVED' | 'CANCELED';
    transactionId: string;
    approvedAt: string;
    canceledAt: string | null;
}

export type ReviewTargetType = 'PRODUCT' | 'PROGRAM';

export interface Review {
    id: number;
    targetType: ReviewTargetType;
    productId: number | null;
    programId: number | null;
    rating: number;
    title: string;
    contentHtml: string;
    authorName: string;
    canDelete: boolean;
    createdAt: string;
}

export type ReportStatus = 'RECEIVED' | 'IN_PROGRESS' | 'ANSWERED' | 'CLOSED';

export interface ReportSummary {
    id: number;
    title: string;
    reporterName: string;
    status: ReportStatus;
    createdAt: string;
}

export interface ReportDetail extends ReportSummary {
    contentHtml: string;
    email: string | null;
    phoneNumber: string | null;
    answers: { id: number; title: string; contentHtml: string; authorName: string; createdAt: string }[];
}

export interface Satisfaction {
    menuId: number;
    menuName: string;
    fivePoint: number;
    fourPoint: number;
    threePoint: number;
    twoPoint: number;
    onePoint: number;
    total: number;
    average: number;
    myScore: number | null;
}

export type PeriodUnit = 'DAY' | 'WEEK' | 'MONTH';

export interface DailyVisits {
    date: string;
    visits: number;
}

export interface Dashboard {
    memberCount: number;
    articleCount: number;
    unansweredQuestions: number;
    pendingPaymentOrders: number;
    paidOrders: number;
    revenueLast30Days: number;
    confirmedReservations: number;
    receivedReports: number;
    todayVisits: number;
    last7DaysVisits: DailyVisits[];
}

export type MapProvider = 'KAKAO' | 'NAVER' | 'GOOGLE';

export interface Office {
    name: string | null;
    address: string | null;
    detailAddress: string | null;
    latitude: number | null;
    longitude: number | null;
    phoneNumber: string | null;
    businessHours: string | null;
    description: string | null;
}

export interface Branch {
    id: number;
    name: string;
    address: string;
    detailAddress: string | null;
    latitude: number;
    longitude: number;
    phoneNumber: string | null;
    businessHours: string | null;
    description: string | null;
    sequence: number;
    isUsing: boolean;
}

export interface PublicMap {
    provider: MapProvider;
    apiKey: string | null;
    directions: Office | null;
    branches: Branch[] | null;
}

export interface MapSetting {
    provider: MapProvider;
    kakaoApiKeyId: number | null;
    naverApiKeyId: number | null;
    googleApiKeyId: number | null;
    apiKey: string | null;
    directionsEnabled: boolean;
    branchesEnabled: boolean;
    office: Office;
}

export type PolicyType = 'PRIVACY_CONSENT' | 'PRIVACY_POLICY' | 'MAIL_RECEPTION' | 'SMS_RECEPTION' | 'MARKETING_USAGE';

export interface Policy {
    type: PolicyType;
    title: string;
    contentHtml: string;
    isUsing: boolean;
    optional: boolean;
    version: number;
    lastWorker: string | null;
    modifiedAt: string | null;
}

export type CryptoAlgorithm = 'AES' | 'SEED' | 'ARIA';

export interface CryptoKey {
    initialized: boolean;
    algorithm: CryptoAlgorithm | null;
    key: string | null;
    createdAt: string | null;
    modifiedAt: string | null;
}

export interface ReencryptResult {
    key: CryptoKey;
    reencryptedValues: number;
}

export type FormFieldType = 'TEXT' | 'TEXTAREA' | 'NUMBER' | 'DATE' | 'EMAIL' | 'PHONE' | 'SELECT' | 'RADIO' | 'CHECKBOX' | 'FILE';
export type SelectionMethod = 'REVIEW' | 'LOTTERY';
export type SubmissionStatus = 'RECEIVED' | 'REVIEWING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export interface FormField {
    key: string;
    label: string;
    type: FormFieldType;
    required: boolean;
    options: string[] | null;
    help: string | null;
}

export interface ApplicationForm {
    id: number;
    title: string;
    contentHtml: string | null;
    fields: FormField[];
    startDateTime: string | null;
    endDateTime: string | null;
    maximumSubmissions: number | null;
    submissionCount: number;
    allowMultiple: boolean;
    isEnabled: boolean;
    selectionMethod: SelectionMethod;
    winnerCount: number | null;
    drawnAt: string | null;
    isOpen: boolean;
    applied: boolean;
    createdAt: string;
}

export interface OptionCount {
    option: string;
    count: number;
    ratio: number;
}

export interface FieldSummary {
    key: string;
    label: string;
    type: FormFieldType;
    answered: number;
    options: OptionCount[];
    average: number | null;
    minimum: number | null;
    maximum: number | null;
}

export interface SubmissionSummary {
    total: number;
    fields: FieldSummary[];
}

export interface DrawResult {
    candidates: number;
    winners: number;
    drawnAt: string;
}

export interface Submission {
    id: number;
    formId: number;
    formTitle: string;
    memberId: number;
    username: string | null;
    answers: Record<string, unknown>;
    status: SubmissionStatus;
    resultMessage: string | null;
    adminMemo: string | null;
    createdAt: string;
}

export type StatusDomain = 'ORDER' | 'RESERVATION';

export interface StatusDefinition {
    id: number;
    code: string;
    label: string;
    baseStatus: string;
    system: boolean;
    sequence: number;
}
