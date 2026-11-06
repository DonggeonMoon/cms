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
    {value: 'MEDIA', label: '스트리밍'},
    {value: 'PROGRAM', label: '예약 상품'},
    {value: 'APPLICATION', label: '신청·지원'},
    {value: 'REPORT', label: '신고'},
    {value: 'SATISFACTION', label: '만족도 조사'},
    {value: 'CHATBOT', label: '챗봇'},
];

export type EnrollmentStatus = 'IN_PROGRESS' | 'COMPLETED';

export interface StreamingProgress {
    status: EnrollmentStatus;
    progressRate: number;
    watchedSeconds: number;
    lastPositionSeconds: number;
    startedAt: string;
    lastWatchedAt: string | null;
    completedAt: string | null;
}

export interface StreamingItem {
    id: number;
    accessGradeId: number | null;
    title: string;
    description: string | null;
    fileId: number;
    fileName: string | null;
    mediaType: MediaType | null;
    streamingUrl: string | null;
    thumbnailFileId: number | null;
    thumbnailUrl: string | null;
    posterUrl: string | null;
    durationSeconds: number;
    completionRate: number;
    trackProgress: boolean;
    isUsing: boolean;
    createdAt: string;
    myProgress: StreamingProgress | null;
    paid: boolean;
    price: number | null;
    accessDays: number | null;
    productId: number | null;
    playable: boolean;
    accessExpiresAt: string | null;
}

export interface MyStreaming {
    streamingId: number;
    title: string;
    mediaType: MediaType | null;
    thumbnailUrl: string | null;
    durationSeconds: number;
    available: boolean;
    progress: StreamingProgress | null;
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
    code: string | null;
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
    supportsPaidContent: boolean;
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
    paid: boolean;
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
    contentHtml: string | null;
    authorName: string;
    authorType: 'ADMIN' | 'MEMBER' | 'GUEST';
    isNotice: boolean;
    noticeStartDateTime: string | null;
    noticeEndDateTime: string | null;
    isSecret: boolean;
    isHidden: boolean;
    paid: boolean;
    price: number | null;
    productId: number | null;
    purchased: boolean;
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

export type ProductSaleType = 'PURCHASE' | 'SUBSCRIPTION' | 'DONATION' | 'QUOTE' | 'PASS' | 'GIFT_CARD' | 'PREORDER' | 'FUNDING' | 'INQUIRY';
export type BillingCycle = 'MONTHLY' | 'YEARLY';
export type PassUnit = 'PERIOD' | 'COUNT';

export type LedgerType = 'CHARGE' | 'GIFT_ISSUED' | 'GIFT_REDEEMED' | 'PAYMENT' | 'REFUND' | 'ADMIN';

export interface WalletLedgerEntry {
    id: number;
    type: LedgerType;
    amount: number;
    balanceAfter: number;
    memo: string | null;
    createdAt: string;
}

export type GiftCardStatus = 'UNUSED' | 'REDEEMED';

export interface GiftCard {
    id: number;
    code: string;
    amount: number;
    status: GiftCardStatus;
    redeemedAt: string | null;
    createdAt: string;
}

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
    commitmentMonths: number | null;
    earlyTerminationFeeRate: number | null;
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
    yourPrice: number;
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
    funding: { goalAmount: number | null; deadline: string | null; raisedAmount: number; supporterCount: number;
        resolved: boolean; succeeded: boolean } | null;
    pass: { unit: PassUnit; days: number | null; count: number | null; reservationItemId: number | null } | null;
    inquiry: { reservationItemId: number | null } | null;
    preorder: { saleStartAt: string | null; saleEndAt: string | null; shipDate: string | null } | null;
}

export interface MemberPass {
    id: number;
    memberId: number;
    productId: number;
    productName: string;
    unit: PassUnit;
    reservationItemId: number;
    totalCount: number | null;
    remainingCount: number | null;
    expiresAt: string | null;
    status: 'ACTIVE' | 'USED_UP' | 'CANCELED';
    createdAt: string;
}

export interface LicenseKey {
    id: number;
    productId: number;
    code: string;
    orderId: number | null;
    memberId: number | null;
    assignedAt: string | null;
    createdAt: string;
}

export interface MyLicenseKey {
    id: number;
    productId: number;
    productName: string;
    code: string;
    orderId: number | null;
    assignedAt: string | null;
}

export type StockMovementType = 'OUT' | 'IN' | 'ADJUST';

export interface StockMovement {
    id: number;
    productId: number;
    productOptionId: number | null;
    type: StockMovementType;
    quantity: number;
    orderId: number | null;
    quantityAfter: number | null;
    note: string | null;
    createdAt: string;
}

export interface GradePrice {
    gradeId: number;
    price: number;
}

export interface Receivable {
    id: number;
    memberId: number;
    orderId: number | null;
    totalAmount: number;
    collectedAmount: number;
    remaining: number;
    settled: boolean;
    note: string | null;
    createdAt: string;
}

export interface SalesReportRow {
    date: string;
    count: number;
    amount: number;
}

export interface SettlementRow {
    method: PaymentMethod;
    count: number;
    amount: number;
}

export interface MarketingCampaign {
    id: number;
    title: string;
    content: string;
    targetGradeId: number | null;
    channels: NotificationChannel[];
    recipientCount: number;
    sentAt: string;
}

export interface Vote {
    id: number;
    title: string;
    description: string | null;
    options: string[];
    accessGradeId: number | null;
    open: boolean;
    closedAt: string | null;
    totalBallots: number;
    resultCounts: Record<string, number>;
    createdAt: string;
}

export interface MyVote {
    id: number;
    title: string;
    description: string | null;
    options: string[];
    open: boolean;
    eligible: boolean;
    myOptionIndex: number | null;
    totalBallots: number | null;
    resultCounts: Record<string, number> | null;
    createdAt: string;
}

export type NotificationChannel = 'IN_APP' | 'EMAIL' | 'SMS';

export type NotificationType = 'MEMBER_JOINED' | 'ORDER_PAID' | 'ORDER_REFUNDED';

export interface NotificationTypeSetting {
    type: NotificationType;
    label: string;
    enabled: boolean;
}

export interface AppNotification {
    id: number;
    channel: NotificationChannel;
    title: string;
    content: string;
    link: string | null;
    read: boolean;
    createdAt: string;
}

export interface AdminNotification {
    id: number;
    memberId: number;
    memberName: string | null;
    channel: NotificationChannel;
    title: string;
    content: string;
    link: string | null;
    read: boolean;
    createdAt: string;
}

export type ProgressTargetType = 'ORDER' | 'REPAIR' | 'APPLICATION';

export interface RepairRequest {
    id: number;
    memberId: number;
    productId: number | null;
    title: string;
    description: string;
    photoUrls: string[];
    quoteOrderId: number | null;
    createdAt: string;
}

export interface ProgressStageTemplate {
    id: number;
    code: string;
    label: string;
    sequence: number;
}

export interface ProgressStageEvent {
    id: number;
    stageCode: string;
    stageLabel: string;
    memo: string | null;
    actorName: string | null;
    createdAt: string;
}

export interface Progress {
    templates: ProgressStageTemplate[];
    history: ProgressStageEvent[];
    currentStageCode: string | null;
}

export interface CustomerMessage {
    id: number;
    senderType: 'MEMBER' | 'ADMIN';
    senderName: string;
    content: string;
    fileUrls: string[];
    createdAt: string;
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
    status: 'ACTIVE' | 'PAUSED' | 'CANCELED';
    nextBillingDate: string | null;
    startedAt: string;
    canceledAt: string | null;
    pausedOn: string | null;
    resumeOn: string | null;
    pendingProductId: number | null;
    pendingProductName: string | null;
    pendingPrice: number | null;
    pendingBillingCycle: BillingCycle | null;
    commitmentEndDate: string | null;
    earlyTerminationFeeRate: number | null;
    earlyTerminationFee: number;
}

export interface WishlistEntry {
    productId: number;
    wishedAt: string;
    product: ProductSummary;
}

export interface CustomerRecord {
    id: number;
    memberId: number;
    reservationId: number | null;
    recordedOn: string;
    category: string | null;
    title: string;
    content: string | null;
    photoFileIds: number[];
    photoUrls: string[];
    sharedWithMember: boolean;
    authorName: string | null;
    createdAt: string;
    modifiedAt: string;
}

export type MyCustomerRecord = Pick<CustomerRecord, 'id' | 'reservationId' | 'recordedOn' | 'category' | 'title' | 'content' | 'photoUrls'>;

export interface AttendanceRosterEntry {
    subscriptionId: number;
    memberId: number;
    memberName: string | null;
    present: boolean | null;
    note: string | null;
}

export interface MyAttendance {
    attendedOn: string;
    present: boolean;
    note: string | null;
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
    deliverySlotAt: string | null;
    shipDate: string | null;
    requestMessage: string | null;
    quoteMessage: string | null;
    quotedAt: string | null;
    receiverName: string;
    phoneNumber: string;
    postalCode: string | null;
    address: string | null;
    detailAddress: string | null;
    country: string | null;
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

export type OrderReturnType = 'RETURN' | 'EXCHANGE';
export type OrderReturnStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED';

export interface OrderReturn {
    id: number;
    orderId: number;
    orderNumber: string | null;
    memberId: number;
    type: OrderReturnType;
    reason: string;
    exchangeOptionId: number | null;
    status: OrderReturnStatus;
    adminMemo: string | null;
    processedAt: string | null;
    createdAt: string;
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

export interface CountryFee {
    country: string;
    fee: number;
}

export interface ShippingPolicy {
    baseFee: number;
    freeThreshold: number | null;
    regionFees: RegionFee[];
    defaultCourier: string | null;
    internationalFees: CountryFee[];
}

export interface TaxSetting {
    vatRate: number;
    priceIncludesVat: boolean;
    customsNotice: string | null;
}

export interface ExchangeRate {
    id: number;
    currencyCode: string;
    rate: number;
    enabled: boolean;
}

export type ReservationType = 'PROGRAM' | 'CONSULTATION' | 'RENTAL' | 'LODGING' | 'EVENT' | 'ITEM_RENTAL' | 'VISIT' | 'TABLE' | 'TOUR';

export interface PriceTier {
    name: string;
    price: number;
}
export type CellKind = 'SEAT' | 'ROOM' | 'SPACE' | 'AISLE' | 'STAGE' | 'EMPTY';

export interface LayoutCell {
    key: string;
    row: number;
    col: number;
    kind: CellKind;
    label: string;
    price: number | null;
    capacity: number | null;
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

export interface ReservationItem {
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
    lateFeePerDay: number | null;
    sessions: ReservationItemSession[];
    supportsQueue: boolean;
    priceTiers: PriceTier[] | null;
}

export interface ReservationItemSession {
    id: number;
    name: string | null;
    startAt: string;
    endAt: string | null;
    capacity: number | null;
    reservedCount: number;
    remaining: number | null;
    isOpen: boolean;
}

export interface ReservationExtension {
    id: number;
    reservationId: number;
    previousEndAt: string;
    newEndAt: string;
    days: number;
    fee: number;
    status: 'PENDING_PAYMENT' | 'PAID' | 'CANCELED' | 'DEDUCTED';
    createdAt: string;
}

export type ReservationStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELED';
export type QueueStatus = 'WAITING' | 'CALLED' | 'DONE' | 'SKIPPED';

export interface Reservation {
    id: number;
    reservationNumber: string;
    userId: number;
    reservationItemId: number;
    reservationItemName: string;
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
    userType: 'MEMBER' | 'ADMIN';
    sessionId: number | null;
    lateFeePerDay: number | null;
    expectedLateFee: number | null;
    lateFee: number | null;
    unpaidExtensionFee: number | null;
    outstandingAmount: number | null;
    pendingExtensionId: number | null;
    pendingExtensionFee: number | null;
    passId: number | null;
    checkInCode: string | null;
    checkedInAt: string | null;
    queueNumber: number | null;
    queueStatus: QueueStatus | null;
    partySize: number | null;
    partyBreakdown: Record<string, number> | null;
}

export type PaymentTargetType = 'ORDER' | 'RESERVATION' | 'RESERVATION_EXTENSION';
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'VIRTUAL_ACCOUNT' | 'MOBILE' | 'PREPAID';

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

export type ReviewTargetType = 'PRODUCT' | 'RESERVATION_ITEM';

export interface Review {
    id: number;
    targetType: ReviewTargetType;
    productId: number | null;
    reservationItemId: number | null;
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

export type PolicyType = 'PRIVACY_CONSENT' | 'PRIVACY_POLICY' | 'TERMS_OF_SERVICE' | 'OPERATION_POLICY'
    | 'YOUTH_PROTECTION_POLICY' | 'MAIL_RECEPTION' | 'SMS_RECEPTION' | 'MARKETING_USAGE';

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
export type SelectionMethod = 'REVIEW' | 'LOTTERY' | 'CONTEST';
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
    admissionFeeProductId: number | null;
    judgeUsernames: string[];
    isJudge: boolean;
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
    admissionOrderId: number | null;
    averageScore: number | null;
    scoreCount: number | null;
    createdAt: string;
}

export interface ScoreDetail {
    judgeMemberId: number;
    judgeUsername: string;
    score: number;
    comment: string | null;
    updatedAt: string;
}

export interface JudgeSubmission {
    id: number;
    formId: number;
    answers: Record<string, unknown>;
    myScore: number | null;
    myComment: string | null;
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
