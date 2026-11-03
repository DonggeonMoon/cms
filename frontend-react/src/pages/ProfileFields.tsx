import {useEffect, useState} from 'react';
import type {Policy, PolicyType} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {HtmlView} from '../components/HtmlView';
import type {JoinSettings, MemberProfile, PersonalInfoItem} from '../api/types';
import {Checkbox, Field} from '../components/Field';

interface Props<T extends MemberProfile> {
    value: T;
    onChange: (next: T) => void;
    errorOf: (field: string) => string | undefined;
    settings: JoinSettings;
}

export function ProfileFields<T extends MemberProfile>({value, onChange, errorOf, settings}: Props<T>) {
    const collects = (item: PersonalInfoItem) => settings.collectedItems.includes(item);
    const text = (field: keyof MemberProfile) => ({
        name: field,
        value: (value[field] as string | null | undefined) ?? '',
        error: errorOf(field),
        onChange: (e: { target: { value: string } }) => onChange({...value, [field]: e.target.value}),
    });

    return (
        <>
            <fieldset>
                <legend>기본 정보</legend>
                {settings.collectNickname && <Field label="닉네임" required maxLength={30} autoComplete="nickname" {...text('nickname')}/>}
                {settings.collectName && (settings.nameFormat === 'SPLIT'
                    ? (
                        <>
                            <Field label="성" required autoComplete="family-name" {...text('lastName')}/>
                            <Field label="이름" required autoComplete="given-name" {...text('firstName')}/>
                        </>
                    )
                    : <Field label="성명" required autoComplete="name" {...text('name')}/>)}
                {collects('BIRTH_DATE') && (
                    settings.birthDateInput === 'EIGHT_DIGITS' || settings.birthDateInput === 'SIX_DIGITS'
                        ? <BirthDateDigits digits={settings.birthDateInput === 'SIX_DIGITS' ? 6 : 8} value={value.birthDate ?? ''}
                                           error={errorOf('birthDate')} onChange={(birthDate) => onChange({...value, birthDate})}/>
                        : <BirthDateSelect value={value.birthDate ?? ''} error={errorOf('birthDate')}
                                           onChange={(birthDate) => onChange({...value, birthDate})}/>
                )}
                {collects('GENDER') && (
                    <div className="field">
                        <label htmlFor="gender">성별</label>
                        <select id="gender" name="gender" value={value.gender ?? ''}
                                onChange={(e) => onChange({...value, gender: e.target.value})}>
                            <option value="">선택 안 함</option>
                            <option value="남">남</option>
                            <option value="여">여</option>
                        </select>
                        {errorOf('gender') && <p className="field-error">{errorOf('gender')}</p>}
                    </div>
                )}
                {collects('EMAIL') && <Field label="이메일" type="email" autoComplete="email" {...text('email')}/>}
                {collects('TELEPHONE') && <Field label="전화번호" type="tel" {...text('telephoneNumber')}/>}
                {collects('MOBILE_PHONE') && <Field label="휴대폰 번호" type="tel" autoComplete="tel" {...text('mobilePhoneNumber')}/>}
            </fieldset>
            {collects('ADDRESS') && (
                <fieldset>
                    <legend>주소</legend>
                    <Field label="우편번호" autoComplete="postal-code" {...text('postalCode')}/>
                    <Field label="주소" {...text('address')}/>
                    <Field label="상세주소" {...text('detailAddress')}/>
                </fieldset>
            )}
            {collects('OFFICE') && (
                <fieldset>
                    <legend>직장 정보</legend>
                    <Field label="직장명" {...text('officeName')}/>
                    <Field label="부서" {...text('officeDepartment')}/>
                    <Field label="직위" {...text('officePosition')}/>
                    <Field label="직장 주소" {...text('officeAddress')}/>
                    <Field label="직장 상세주소" {...text('officeDetailAddress')}/>
                </fieldset>
            )}
            <OptionalConsents value={value} onChange={onChange}/>
        </>
    );
}

const OPTIONAL_CONSENTS: { type: PolicyType; field: 'isMailReceptionAgreed' | 'isSmsReceptionAgreed' | 'isMarketingUsageAgreed'; label: string }[] = [
    {type: 'MAIL_RECEPTION', field: 'isMailReceptionAgreed', label: '이메일 수신 동의 (선택)'},
    {type: 'SMS_RECEPTION', field: 'isSmsReceptionAgreed', label: 'SMS 수신 동의 (선택)'},
    {type: 'MARKETING_USAGE', field: 'isMarketingUsageAgreed', label: '마케팅 활용 동의 (선택)'},
];

function OptionalConsents<T extends MemberProfile>({value, onChange}: { value: T; onChange: (next: T) => void }) {
    const [policies, setPolicies] = useState<Partial<Record<PolicyType, Policy>>>({});
    const [open, setOpen] = useState<PolicyType | null>(null);

    useEffect(() => {
        Promise.all(OPTIONAL_CONSENTS.map((c) => siteApi.policy(c.type).catch(() => null)))
            .then((loaded) => setPolicies(Object.fromEntries(
                loaded.flatMap((policy, i) => (policy ? [[OPTIONAL_CONSENTS[i].type, policy]] : [])))));
    }, []);

    const shown = OPTIONAL_CONSENTS.filter((c) => policies[c.type]?.isUsing !== false);
    if (shown.length === 0) {
        return null;
    }
    return (
        <fieldset>
            <legend>선택 동의</legend>
            {shown.map((consent) => (
                <div key={consent.type} className="consent-item">
                    <div className="row space-between align-center">
                        <Checkbox label={consent.label} name={consent.field} checked={Boolean(value[consent.field])}
                                  onChange={(e) => onChange({...value, [consent.field]: e.target.checked})}/>
                        {policies[consent.type]?.contentHtml && (
                            <button type="button" className="link-button small" aria-expanded={open === consent.type}
                                    onClick={() => setOpen(open === consent.type ? null : consent.type)}>
                                {open === consent.type ? '접기' : '내용 보기'}
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
    const [text, setText] = useState(() => (value ? value.replaceAll('-', '').slice(digits === 6 ? 2 : 0) : ''));
    const invalid = text.length === digits && toIsoDate(text, digits) === null;
    return (
        <div className="field">
            <label htmlFor="birthDate">생년월일</label>
            <input id="birthDate" name="birthDate" inputMode="numeric" autoComplete="bday" maxLength={digits}
                   placeholder={digits === 8 ? '19900410' : '900410'} value={text} aria-invalid={invalid || Boolean(error)}
                   onChange={(e) => {
                       const next = e.target.value.replace(/\D/g, '').slice(0, digits);
                       setText(next);
                       onChange(toIsoDate(next, digits) ?? '');
                   }}/>
            <p className="muted small">{digits === 8 ? '숫자 8자리 (예: 19900410)' : '숫자 6자리 (예: 900410)'}</p>
            {(invalid || error) && <p className="field-error">{error ?? '올바른 생년월일을 입력하세요.'}</p>}
        </div>
    );
}

const pad = (n: number) => String(n).padStart(2, '0');

function BirthDateSelect({value, onChange, error}: { value: string; onChange: (date: string) => void; error?: string }) {
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
            <span id="birthDate-label" className="field-label">생년월일</span>
            <div className="row date-selects">
                <select aria-label="태어난 해" value={parts.year} onChange={(e) => update('year', e.target.value)}>
                    <option value="">년</option>
                    {years.map((y) => <option key={y} value={y}>{y}년</option>)}
                </select>
                <select aria-label="태어난 달" value={parts.month} onChange={(e) => update('month', e.target.value)}>
                    <option value="">월</option>
                    {Array.from({length: 12}, (_, i) => pad(i + 1)).map((m) => <option key={m} value={m}>{Number(m)}월</option>)}
                </select>
                <select aria-label="태어난 날" value={parts.day} onChange={(e) => update('day', e.target.value)}>
                    <option value="">일</option>
                    {days.map((d) => <option key={d} value={d}>{Number(d)}일</option>)}
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
