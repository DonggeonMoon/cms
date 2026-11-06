import {type FormEvent, useEffect, useState} from 'react';
import {Link} from 'react-router';
import {memberAdminApi} from '../../api/members';
import {type MemberRegexTarget, regexAdminApi, type RegexOption} from '../../api/regex';
import {type AdminJoinSettings, PERSONAL_INFO_ITEMS, type PersonalInfoItem} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox} from '../../components/Field';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';
import {StableLabel} from '../../components/StableLabel';
import {SocialLoginSettings} from './SocialLoginSettings';

const ITEM_REGEX: Partial<Record<PersonalInfoItem, { target: MemberRegexTarget; label: string }>> = {
    EMAIL: {target: 'EMAIL', label: '이메일'},
    TELEPHONE: {target: 'PHONE_NUMBER', label: '전화번호'},
    MOBILE_PHONE: {target: 'MOBILE_PHONE', label: '휴대폰 번호'},
    ADDRESS: {target: 'POSTAL_CODE', label: '우편번호'},
};

export function MemberJoinSettingPage() {
    return (
        <ReauthGate title="회원가입 설정">
            {(guard) => (
                <>
                    <MemberJoinSettingForm guard={guard}/>
                    <SocialLoginSettings guard={guard}/>
                </>
            )}
        </ReauthGate>
    );
}

function MemberJoinSettingForm({guard}: { guard: ReauthGuard }) {
    const [form, setForm] = useState<AdminJoinSettings | null>(null);
    const [regexOptions, setRegexOptions] = useState<RegexOption[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        guard(memberAdminApi.joinSettings()).then(setForm).catch(setError);
        regexAdminApi.options().then(setRegexOptions).catch(setError);
    }, [guard]);

    const regexSelect = (target: MemberRegexTarget, label: string) => form && (
        <select aria-label={`${label} 정규식`} value={form.regexes[target]?.toString() ?? ''}
                onChange={(e) => setForm({...form, regexes: {...form.regexes, [target]: e.target.value ? Number(e.target.value) : null}})}>
            <option value="">검사 안 함</option>
            {regexOptions.map((option) => <option key={option.id} value={option.id}>{option.name} ({option.pattern})</option>)}
        </select>
    );

    const toggle = (item: PersonalInfoItem, checked: boolean) => form && setForm({
        ...form,
        collectedItems: checked ? [...form.collectedItems, item] : form.collectedItems.filter((i) => i !== item),
    });

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        if (!form) {
            return;
        }
        setSaving(true);
        setSaved(false);
        setError(null);
        try {
            setForm(await guard(memberAdminApi.saveJoinSettings(form)));
            setSaved(true);
        } catch (e) {
            setError(e);
        } finally {
            setSaving(false);
        }
    };

    return (
        <section className="card">
            <h1>회원가입 설정</h1>
            <div className="muted page-description">
                <p>회원가입과 마이페이지에서 받을 개인정보를 정합니다. 선택하지 않은 항목은 입력란이 보이지 않고, 보내더라도 저장하지 않습니다.
                    항목을 바꾸면 <strong>개인정보 수집·이용 동의</strong> 문구도 함께 고쳐 주세요.</p>
            </div>
            <ErrorBanner error={error}/>
            {form && (
                <form onSubmit={handleSubmit}>
                    <p className="muted small">항목 옆 선택 상자에서 입력값을 검사할 정규식을 고릅니다. 정규식은 <Link to="/admin/regexes">정규식</Link> 메뉴에서
                        등록·수정하며, 전화번호·이메일 정규식은 주문·예약·질문답변·신고의 연락처에도 쓰입니다.</p>
                    <fieldset>
                        <legend>계정</legend>
                        <div className="collect-row"><span>아이디</span>{regexSelect('USERNAME', '아이디')}</div>
                        <div className="collect-row"><span>비밀번호</span>{regexSelect('PASSWORD', '비밀번호')}</div>
                    </fieldset>
                    <fieldset>
                        <legend>이름·닉네임</legend>
                        <Checkbox label="이름 받기" name="collectName" checked={form.collectName}
                                  onChange={(e) => setForm({...form, collectName: e.target.checked})}/>
                        {form.collectName && (
                            <div className="indented">
                                <label className="checkbox">
                                    <input type="radio" name="nameFormat" checked={form.nameFormat === 'FULL'}
                                           onChange={() => setForm({...form, nameFormat: 'FULL'})}/>
                                    성명 한 칸으로 받기 (예: 홍길동)
                                </label>
                                <label className="checkbox">
                                    <input type="radio" name="nameFormat" checked={form.nameFormat === 'SPLIT'}
                                           onChange={() => setForm({...form, nameFormat: 'SPLIT'})}/>
                                    성과 이름을 따로 받기 (예: 홍 / 길동)
                                </label>
                            </div>
                        )}
                        <div className="collect-row">
                            <Checkbox label="닉네임 받기 (중복 불가)" name="collectNickname" checked={form.collectNickname}
                                      onChange={(e) => setForm({...form, collectNickname: e.target.checked})}/>
                            {form.collectNickname && regexSelect('NICKNAME', '닉네임')}
                        </div>
                        <p className="muted small">사이트에 표시되는 이름은 닉네임 → 아이디 → 이름 순으로 정해집니다.</p>
                    </fieldset>
                    <fieldset>
                        <legend>생년월일 입력 방식</legend>
                        {([
                            ['SELECT', '년 / 월 / 일 셀렉트 박스로 고르기'],
                            ['EIGHT_DIGITS', '숫자 8자리로 입력 (예: 19900410)'],
                            ['SIX_DIGITS', '숫자 6자리로 입력 (예: 900410)'],
                        ] as const).map(([value, label]) => (
                            <label key={value} className="checkbox">
                                <input type="radio" name="birthDateInput" checked={form.birthDateInput === value}
                                       onChange={() => setForm({...form, birthDateInput: value})}/>
                                {label}
                            </label>
                        ))}
                        <p className="muted small">생년월일을 수집할 때만 적용됩니다. 6자리는 올해 기준으로 1900년대·2000년대를 구분합니다.</p>
                    </fieldset>
                    <fieldset>
                        <legend>수집할 개인정보 항목</legend>
                        {PERSONAL_INFO_ITEMS.map((item) => {
                            const target = ITEM_REGEX[item.value];
                            const checked = form.collectedItems.includes(item.value);
                            return (
                                <div key={item.value}>
                                    <div className="collect-row">
                                        <Checkbox label={item.label} name={item.value} checked={checked}
                                                  onChange={(e) => toggle(item.value, e.target.checked)}/>
                                        {target && checked && regexSelect(target.target, target.label)}
                                    </div>
                                    {item.value === 'MOBILE_PHONE' && checked && (
                                        <div className="indented">
                                            <Checkbox label="국가 코드 입력란 추가 (예: +82)" name="useMobilePhoneCountryCode"
                                                      checked={form.useMobilePhoneCountryCode}
                                                      onChange={(e) => setForm({...form, useMobilePhoneCountryCode: e.target.checked})}/>
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </fieldset>
                    {saved && <p className="ok">저장했습니다.</p>}
                    <div className="toolbar form-actions">
                        <button type="submit" className="primary" disabled={saving}>
                            <StableLabel current={saving ? '저장 중…' : '저장'} labels={['저장 중…', '저장']}/>
                        </button>
                    </div>
                </form>
            )}
        </section>
    );
}
