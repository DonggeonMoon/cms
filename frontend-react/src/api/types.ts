export type UserType = 'ADMIN' | 'MEMBER' | 'GUEST';

export interface LoginUser {
    id: number;
    username: string;
    name: string;
    userType: UserType;
}

export interface Page<T> {
    content: T[];
    page: {
        size: number;
        number: number;
        totalElements: number;
        totalPages: number;
    };
}

export interface FieldError {
    field: string;
    message: string;
}

export interface ErrorBody {
    code: string;
    message: string;
    fieldErrors: FieldError[];
}

export interface MemberProfile {
    nickname?: string | null;
    name: string;
    lastName?: string | null;
    firstName?: string | null;
    birthDate?: string | null;
    gender?: string | null;
    email?: string | null;
    telephoneNumber?: string | null;
    mobilePhoneNumber?: string | null;
    postalCode?: string | null;
    address?: string | null;
    detailAddress?: string | null;
    isMailReceptionAgreed: boolean;
    isSmsReceptionAgreed: boolean;
    isMarketingUsageAgreed: boolean;
    officeName?: string | null;
    officeDepartment?: string | null;
    officePosition?: string | null;
    officeAddress?: string | null;
    officeDetailAddress?: string | null;
}

export type NameFormat = 'FULL' | 'SPLIT';

export type PersonalInfoItem = 'BIRTH_DATE' | 'GENDER' | 'EMAIL' | 'TELEPHONE' | 'MOBILE_PHONE' | 'ADDRESS' | 'OFFICE';

export const PERSONAL_INFO_ITEMS: { value: PersonalInfoItem; label: string }[] = [
    {value: 'BIRTH_DATE', label: '생년월일'},
    {value: 'GENDER', label: '성별'},
    {value: 'EMAIL', label: '이메일'},
    {value: 'TELEPHONE', label: '전화번호'},
    {value: 'MOBILE_PHONE', label: '휴대폰 번호'},
    {value: 'ADDRESS', label: '주소'},
    {value: 'OFFICE', label: '직장 정보'},
];

export type BirthDateInput = 'SELECT' | 'EIGHT_DIGITS' | 'SIX_DIGITS';

export interface JoinSettings {
    collectName: boolean;
    nameFormat: NameFormat;
    collectNickname: boolean;
    birthDateInput: BirthDateInput;
    collectedItems: PersonalInfoItem[];
}

export interface AdminJoinSettings extends JoinSettings {
export interface MemberJoinRequest extends MemberProfile {
    username: string;
    password: string;
    passwordCheck: string;
    isPrivacyConsentAgreed: boolean;
}

export interface MemberResponse extends MemberProfile {
    id: number;
    username: string;
    loginTryCount: number;
    lastLoginDateTime: string | null;
    createdAt: string;
}

export interface MemberSummary {
    id: number;
    username: string;
    nickname: string | null;
    name: string | null;
    email: string | null;
    isLocked: boolean;
    lastLoginDateTime: string | null;
    createdAt: string;
}

export interface Code {
    id: number;
    parentId: number | null;
    depth: number;
    order: number;
    name: string;
    englishName: string | null;
    description: string | null;
    isUsing: boolean;
}

export interface CodeSaveRequest {
    parentId: number | null;
    order: number;
    name: string;
    englishName: string;
    description: string;
    isUsing: boolean;
}

export type MediaType = 'DOCUMENT' | 'IMAGE' | 'VIDEO' | 'ETC';

export interface FileInfo {
    id: number;
    originalName: string;
    extension: string;
    mimeType: string;
    mediaType: MediaType;
    size: number;
    alternativeText: string | null;
    downloadCount: number;
    downloadUrl: string;
    thumbnailUrl: string | null;
    createdAt: string;
}
