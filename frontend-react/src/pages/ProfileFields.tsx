import type {MemberProfile} from '../api/types';
import {Checkbox, Field} from '../components/Field';

interface Props<T extends MemberProfile> {
    value: T;
    onChange: (next: T) => void;
    errorOf: (field: string) => string | undefined;
}

export function ProfileFields<T extends MemberProfile>({value, onChange, errorOf}: Props<T>) {
    const text = (field: keyof MemberProfile) => ({
        name: field,
        value: (value[field] as string | null | undefined) ?? '',
        error: errorOf(field),
        onChange: (e: { target: { value: string } }) => onChange({...value, [field]: e.target.value}),
    });
    const check = (field: keyof MemberProfile) => ({
        name: field,
        checked: Boolean(value[field]),
        onChange: (e: { target: { checked: boolean } }) => onChange({...value, [field]: e.target.checked}),
    });

    return (
        <>
            <fieldset>
                <legend>기본 정보</legend>
                <Field label="이름" required {...text('name')}/>
                <div className="row">
                    <Field label="성" {...text('lastName')}/>
                    <Field label="이름(성 제외)" {...text('firstName')}/>
                </div>
                <div className="row">
                    <Field label="생년월일" type="date" {...text('birthDate')}/>
                    <Field label="성별" placeholder="예: 남, 여" {...text('gender')}/>
                </div>
                <Checkbox label="음력 생일" {...check('isLunarBirthDate')}/>
                <Field label="이메일" type="email" {...text('email')}/>
                <div className="row">
                    <Field label="전화번호" {...text('telephoneNumber')}/>
                    <Field label="휴대폰 번호" {...text('mobilePhoneNumber')}/>
                </div>
            </fieldset>
            <fieldset>
                <legend>주소</legend>
                <Field label="우편번호" {...text('postalCode')}/>
                <Field label="주소" {...text('address')}/>
                <Field label="상세주소" {...text('detailAddress')}/>
            </fieldset>
            <fieldset>
                <legend>직장 정보</legend>
                <div className="row">
                    <Field label="직장명" {...text('officeName')}/>
                    <Field label="부서" {...text('officeDepartment')}/>
                    <Field label="직위" {...text('officePosition')}/>
                </div>
                <Field label="직장 주소" {...text('officeAddress')}/>
                <Field label="직장 상세주소" {...text('officeDetailAddress')}/>
            </fieldset>
            <fieldset>
                <legend>수신 동의</legend>
                <Checkbox label="메일 수신 동의" {...check('isMailReceptionAgreed')}/>
                <Checkbox label="SMS 수신 동의" {...check('isSmsReceptionAgreed')}/>
                <Checkbox label="마케팅 활용 동의" {...check('isMarketingUsageAgreed')}/>
            </fieldset>
        </>
    );
}

export const EMPTY_PROFILE: MemberProfile = {
    name: '',
    isLunarBirthDate: false,
    isMailReceptionAgreed: false,
    isSmsReceptionAgreed: false,
    isMarketingUsageAgreed: false,
};

// the API expects null rather than "" for an empty date
export function normalizeProfile<T extends MemberProfile>(profile: T): T {
    return {...profile, birthDate: profile.birthDate || null};
}
