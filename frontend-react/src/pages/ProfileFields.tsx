import {useEffect, useState} from 'react';
import type {Policy, PolicyType} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {HtmlView} from '../components/HtmlView';
import type {JoinSettings, MemberProfile, PersonalInfoItem} from '../api/types';
import {Checkbox, Field} from '../components/Field';
import {PostcodeButton} from '../components/PostcodeButton';
import {useI18n} from '../api/i18n';

type TFunc = (key: string, fallback: string, values?: Record<string, string>) => string;

interface Props<P extends MemberProfile> {
    value: P;
    onChange: (next: P) => void;
    errorOf: (field: string) => string | undefined;
    settings: JoinSettings;
}

export function ProfileFields<P extends MemberProfile>({value, onChange, errorOf, settings}: Props<P>) {
    const {t} = useI18n();
    const collects = (item: PersonalInfoItem) => settings.collectedItems.includes(item);
    const text = (field: keyof MemberProfile) => ({
        name: field,
        value: (value[field] as string | null | undefined) ?? '',
        error: errorOf(field),
        onChange: (e: { target: { value: string } }) => onChange({...value, [field]: e.target.value}),
    });

    useEffect(() => {
        if (settings.useMobilePhoneCountryCode && collects('MOBILE_PHONE') && !value.mobilePhoneCountryCode) {
            onChange({...value, mobilePhoneCountryCode: countryCallingCodes(t)[0].code});
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [settings.useMobilePhoneCountryCode, settings.collectedItems]);

    return (
        <>
            <fieldset>
                <legend>{t('shop.profile.basicInfo', '기본 정보')}</legend>
                {settings.collectNickname && <Field label={t('admin.member.nickname', '닉네임')} required maxLength={30} autoComplete="nickname" {...text('nickname')}/>}
                {settings.collectName && (settings.nameFormat === 'SPLIT'
                    ? (
                        <>
                            <Field label={t('shop.profile.lastName', '성')} required autoComplete="family-name" {...text('lastName')}/>
                            <Field label={t('shop.profile.firstName', '이름')} required autoComplete="given-name" {...text('firstName')}/>
                        </>
                    )
                    : <Field label={t('shop.profile.fullName', '성명')} required autoComplete="name" {...text('name')}/>)}
                {collects('BIRTH_DATE') && (
                    settings.birthDateInput === 'EIGHT_DIGITS' || settings.birthDateInput === 'SIX_DIGITS'
                        ? <BirthDateDigits digits={settings.birthDateInput === 'SIX_DIGITS' ? 6 : 8} value={value.birthDate ?? ''}
                                           error={errorOf('birthDate')} onChange={(birthDate) => onChange({...value, birthDate})}/>
                        : <BirthDateSelect value={value.birthDate ?? ''} error={errorOf('birthDate')}
                                           onChange={(birthDate) => onChange({...value, birthDate})}/>
                )}
                {collects('GENDER') && (
                    <div className="field">
                        <label htmlFor="gender">{t('admin.member.gender', '성별')}</label>
                        <select id="gender" name="gender" value={value.gender ?? ''}
                                onChange={(e) => onChange({...value, gender: e.target.value})}>
                            <option value="">{t('admin.social.notSelected', '선택 안 함')}</option>
                            <option value="남">{t('shop.profile.male', '남')}</option>
                            <option value="여">{t('shop.profile.female', '여')}</option>
                        </select>
                        {errorOf('gender') && <p className="field-error">{errorOf('gender')}</p>}
                    </div>
                )}
                {collects('EMAIL') && <Field label={t('admin.memberJoin.email', '이메일')} type="email" autoComplete="email" {...text('email')}/>}
                {collects('TELEPHONE') && <Field label={t('admin.memberJoin.telephone', '전화번호')} type="tel" {...text('telephoneNumber')}/>}
                {collects('MOBILE_PHONE') && (
                    <>
                        {settings.useMobilePhoneCountryCode && (
                            <div className="field">
                                <label htmlFor="mobilePhoneCountryCode">{t('shop.profile.countryCode', '국가 코드')}</label>
                                <select id="mobilePhoneCountryCode" name="mobilePhoneCountryCode"
                                        value={value.mobilePhoneCountryCode ?? countryCallingCodes(t)[0].code}
                                        onChange={(e) => onChange({...value, mobilePhoneCountryCode: e.target.value})}>
                                    {countryCallingCodes(t).map((c) => (
                                        <option key={c.code} value={c.code}>{c.code} {c.label}</option>
                                    ))}
                                </select>
                                {errorOf('mobilePhoneCountryCode') && <p className="field-error">{errorOf('mobilePhoneCountryCode')}</p>}
                            </div>
                        )}
                        <Field label={t('shop.profile.mobilePhoneNumber', '휴대폰 번호')} type="tel" autoComplete="tel" {...text('mobilePhoneNumber')}/>
                    </>
                )}
            </fieldset>
            {collects('ADDRESS') && (
                <fieldset>
                    <legend>{t('admin.order.field.address', '주소')}</legend>
                    <div className="row align-end">
                        <Field label={t('shop.profile.postalCode', '우편번호')} autoComplete="postal-code" {...text('postalCode')}/>
                        <PostcodeButton onFound={(result) => onChange({...value, postalCode: result.zonecode, address: result.roadAddress || result.jibunAddress})}/>
                    </div>
                    <Field label={t('admin.order.field.address', '주소')} {...text('address')}/>
                    <Field label={t('shop.profile.detailAddress', '상세주소')} {...text('detailAddress')}/>
                </fieldset>
            )}
            {collects('OFFICE') && (
                <fieldset>
                    <legend>{t('shop.profile.officeInfo', '직장 정보')}</legend>
                    <Field label={t('shop.profile.officeName', '직장명')} {...text('officeName')}/>
                    <Field label={t('shop.profile.officeDepartment', '부서')} {...text('officeDepartment')}/>
                    <Field label={t('shop.profile.officePosition', '직위')} {...text('officePosition')}/>
                    <Field label={t('shop.profile.officeAddress', '직장 주소')} {...text('officeAddress')}/>
                    <Field label={t('shop.profile.officeDetailAddress', '직장 상세주소')} {...text('officeDetailAddress')}/>
                </fieldset>
            )}
            <OptionalConsents value={value} onChange={onChange}/>
        </>
    );
}

const countryCallingCodes = (t: TFunc): { code: string; label: string }[] => [
    {code: '+82', label: t('shop.profile.country.kr', '대한민국')},
    {code: '+1', label: t('shop.profile.country.us', '미국/캐나다')},
    {code: '+81', label: t('shop.profile.country.jp', '일본')},
    {code: '+86', label: t('shop.profile.country.cn', '중국')},
    {code: '+852', label: t('shop.profile.country.hk', '홍콩')},
    {code: '+886', label: t('shop.profile.country.tw', '대만')},
    {code: '+65', label: t('shop.profile.country.sg', '싱가포르')},
    {code: '+84', label: t('shop.profile.country.vn', '베트남')},
    {code: '+66', label: t('shop.profile.country.th', '태국')},
    {code: '+63', label: t('shop.profile.country.ph', '필리핀')},
    {code: '+62', label: t('shop.profile.country.id', '인도네시아')},
    {code: '+91', label: t('shop.profile.country.in', '인도')},
    {code: '+44', label: t('shop.profile.country.gb', '영국')},
    {code: '+49', label: t('shop.profile.country.de', '독일')},
    {code: '+33', label: t('shop.profile.country.fr', '프랑스')},
    {code: '+61', label: t('shop.profile.country.au', '호주')},
];

const optionalConsents = (t: TFunc): { type: PolicyType; field: 'isMailReceptionAgreed' | 'isSmsReceptionAgreed' | 'isMarketingUsageAgreed'; label: string }[] => [
    {type: 'MAIL_RECEPTION', field: 'isMailReceptionAgreed', label: t('shop.profile.mailConsent', '이메일 수신 동의 (선택)')},
    {type: 'SMS_RECEPTION', field: 'isSmsReceptionAgreed', label: t('shop.profile.smsConsent', 'SMS 수신 동의 (선택)')},
    {type: 'MARKETING_USAGE', field: 'isMarketingUsageAgreed', label: t('shop.profile.marketingConsent', '마케팅 활용 동의 (선택)')},
];

function OptionalConsents<P extends MemberProfile>({value, onChange}: { value: P; onChange: (next: P) => void }) {
    const {t} = useI18n();
    const consentDefs = optionalConsents(t);
    const [policies, setPolicies] = useState<Partial<Record<PolicyType, Policy>>>({});
    const [open, setOpen] = useState<PolicyType | null>(null);

    useEffect(() => {
        Promise.all(consentDefs.map((c) => siteApi.policy(c.type).catch(() => null)))
            .then((loaded) => setPolicies(Object.fromEntries(
                loaded.flatMap((policy, i) => (policy ? [[consentDefs[i].type, policy]] : [])))));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const shown = consentDefs.filter((c) => policies[c.type]?.isUsing !== false);
    if (shown.length === 0) {
        return null;
    }
    return (
        <fieldset>
            <legend>{t('shop.profile.optionalConsents', '선택 동의')}</legend>
            {shown.map((consent) => (
                <div key={consent.type} className="consent-item">
                    <div className="row space-between align-center">
                        <Checkbox label={consent.label} name={consent.field} checked={Boolean(value[consent.field])}
                                  onChange={(e) => onChange({...value, [consent.field]: e.target.checked})}/>
                        {policies[consent.type]?.contentHtml && (
                            <button type="button" className="link-button small" aria-expanded={open === consent.type}
                                    onClick={() => setOpen(open === consent.type ? null : consent.type)}>
                                {open === consent.type ? t('shop.profile.collapse', '접기') : t('shop.profile.viewContent', '내용 보기')}
                            </button>
                        )}
                    </div>
                    {open === consent.type && policies[consent.type] && (
                        <div className="policy-box"><HtmlView html={policies[consent.type]!.contentHtml}/></div>
                    )}
                </div>
            ))}
        </fieldset>
    );
}

export function toIsoDate(digits: string, length: 6 | 8): string | null {
    if (digits.length !== length) {
        return null;
    }
    const thisYear = new Date().getFullYear();
    let year: number;
    if (length === 8) {
        year = Number(digits.slice(0, 4));
    } else {
        const yy = Number(digits.slice(0, 2));
        year = yy <= thisYear % 100 ? 2000 + yy : 1900 + yy;
    }
    const rest = digits.slice(length - 4);
    const month = Number(rest.slice(0, 2));
    const day = Number(rest.slice(2, 4));
    const date = new Date(year, month - 1, day);
    const valid = year >= 1900 && date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day
        && date.getTime() <= Date.now();
    return valid ? `${year}-${pad(month)}-${pad(day)}` : null;
}

function BirthDateDigits({digits, value, onChange, error}: {
    digits: 6 | 8; value: string; onChange: (date: string) => void; error?: string;
}) {
    const {t} = useI18n();
    const [text, setText] = useState(() => (value ? value.replaceAll('-', '').slice(digits === 6 ? 2 : 0) : ''));
    const invalid = text.length === digits && toIsoDate(text, digits) === null;
    return (
        <div className="field">
            <label htmlFor="birthDate">{t('shop.profile.birthDate', '생년월일')}</label>
            <input id="birthDate" name="birthDate" inputMode="numeric" autoComplete="bday" maxLength={digits}
                   placeholder={digits === 8 ? '19900410' : '900410'} value={text} aria-invalid={invalid || Boolean(error)}
                   onChange={(e) => {
                       const next = e.target.value.replace(/\D/g, '').slice(0, digits);
                       setText(next);
                       onChange(toIsoDate(next, digits) ?? '');
                   }}/>
            <p className="muted small">{digits === 8 ? t('shop.profile.birthDateHelp8', '숫자 8자리 (예: 19900410)') : t('shop.profile.birthDateHelp6', '숫자 6자리 (예: 900410)')}</p>
            {(invalid || error) && <p className="field-error">{error ?? t('shop.profile.birthDateInvalid', '올바른 생년월일을 입력하세요.')}</p>}
        </div>
    );
}

const pad = (n: number) => String(n).padStart(2, '0');

function BirthDateSelect({value, onChange, error}: { value: string; onChange: (date: string) => void; error?: string }) {
    const {t} = useI18n();
    const [parts, setParts] = useState(() => {
        const [year = '', month = '', day = ''] = value ? value.split('-') : [];
        return {year, month, day};
    });
    const thisYear = new Date().getFullYear();
    const years = Array.from({length: thisYear - 1899}, (_, i) => String(thisYear - i));
    const daysInMonth = parts.year && parts.month ? new Date(Number(parts.year), Number(parts.month), 0).getDate() : 31;
    const days = Array.from({length: daysInMonth}, (_, i) => pad(i + 1));

    const update = (key: 'year' | 'month' | 'day', next: string) => {
        const updated = {...parts, [key]: next};
        if (updated.year && updated.month && updated.day) {
            const lastDay = new Date(Number(updated.year), Number(updated.month), 0).getDate();
            if (Number(updated.day) > lastDay) {
                updated.day = '';
            }
        }
        setParts(updated);
        onChange(updated.year && updated.month && updated.day ? `${updated.year}-${updated.month}-${updated.day}` : '');
    };

    return (
        <div className="field" role="group" aria-labelledby="birthDate-label">
            <span id="birthDate-label" className="field-label">{t('shop.profile.birthDate', '생년월일')}</span>
            <div className="row date-selects">
                <select aria-label={t('shop.profile.birthYear', '태어난 해')} value={parts.year} onChange={(e) => update('year', e.target.value)}>
                    <option value="">{t('shop.profile.year', '년')}</option>
                    {years.map((y) => <option key={y} value={y}>{t('shop.profile.yearOf', '{year}년', {year: y})}</option>)}
                </select>
                <select aria-label={t('shop.profile.birthMonth', '태어난 달')} value={parts.month} onChange={(e) => update('month', e.target.value)}>
                    <option value="">{t('shop.profile.month', '월')}</option>
                    {Array.from({length: 12}, (_, i) => pad(i + 1)).map((m) => <option key={m} value={m}>{t('shop.profile.monthOf', '{month}월', {month: String(Number(m))})}</option>)}
                </select>
                <select aria-label={t('shop.profile.birthDay', '태어난 날')} value={parts.day} onChange={(e) => update('day', e.target.value)}>
                    <option value="">{t('shop.profile.day', '일')}</option>
                    {days.map((d) => <option key={d} value={d}>{t('shop.profile.dayOf', '{day}일', {day: String(Number(d))})}</option>)}
                </select>
            </div>
            {error && <p className="field-error">{error}</p>}
        </div>
    );
}

export const EMPTY_PROFILE: MemberProfile = {
    name: '',
    isMailReceptionAgreed: false,
    isSmsReceptionAgreed: false,
    isMarketingUsageAgreed: false,
};

export function normalizeProfile<T extends MemberProfile>(profile: T): T {
    return {...profile, birthDate: profile.birthDate || null, gender: profile.gender || null};
}
