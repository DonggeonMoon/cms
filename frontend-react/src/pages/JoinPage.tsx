import {type FormEvent, useEffect, useState} from 'react';
import {useNavigate} from 'react-router';
import {ApiError} from '../api/client';
import type {Policy} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {memberApi} from '../api/members';
import type {MemberJoinRequest} from '../api/types';
import {ErrorBanner} from '../components/ErrorBanner';
import {Checkbox, Field} from '../components/Field';
import {HtmlView} from '../components/HtmlView';
import {useSite} from '../components/SiteContext';
import {EMPTY_PROFILE, normalizeProfile, ProfileFields} from './ProfileFields';

export function JoinPage() {
    const [agreed, setAgreed] = useState(false);
    return agreed ? <JoinForm/> : <ConsentStep onAgree={() => setAgreed(true)}/>;
}

function ConsentStep({onAgree}: { onAgree: () => void }) {
    const [policy, setPolicy] = useState<Policy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [checked, setChecked] = useState(false);

    useEffect(() => {
        siteApi.policy('PRIVACY_CONSENT').then(setPolicy).catch(setError);
    }, []);

    return (
        <section className="card form-page">
            <JoinTitle/>
            <h2>{policy?.title ?? '개인정보 수집·이용 동의'}</h2>
            <ErrorBanner error={error}/>
            <div className="policy-box">{policy ? <HtmlView html={policy.contentHtml}/> : <p className="muted">불러오는 중…</p>}</div>
            <Checkbox label="위 개인정보 수집·이용에 동의합니다. (필수)" name="privacyConsent" checked={checked}
                      onChange={(e) => setChecked(e.target.checked)}/>
            <div className="toolbar form-actions">
                <button type="button" className="primary" disabled={!checked || !policy} onClick={onAgree}>동의하고 계속</button>
            </div>
        </section>
    );
}

function JoinForm() {
    const navigate = useNavigate();
    const [form, setForm] = useState<MemberJoinRequest>({
        ...EMPTY_PROFILE,
        username: '',
        password: '',
        passwordCheck: '',
        isPrivacyConsentAgreed: true,
    });
    const [usernameStatus, setUsernameStatus] = useState<'unknown' | 'available' | 'taken'>('unknown');
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        memberApi.joinSettings().then(setSettings).catch(setError);
    }, []);

    const errorOf = (field: string) => (error instanceof ApiError ? error.fieldMessage(field) : undefined);
    const passwordMismatch = form.passwordCheck !== '' && form.password !== form.passwordCheck;

    const checkUsername = async () => {
        if (!form.username) {
            return;
        }
        try {
            setUsernameStatus(await memberApi.isUsernameAvailable(form.username) ? 'available' : 'taken');
        } catch (e) {
            setError(e);
        }
    };

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        if (passwordMismatch) {
            return;
        }
        setSubmitting(true);
        setError(null);
        try {
            await memberApi.join(normalizeProfile(form));
            navigate('/login', {state: {joined: true}});
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card form-page">
            <JoinTitle/>
            <form onSubmit={handleSubmit}>
                <fieldset>
                    <legend>계정</legend>
                    <div className="row align-end">
                        <Field label="아이디" name="username" required value={form.username} autoComplete="username"
                               error={errorOf('username')} placeholder="영문 소문자, 숫자, _ 4~20자"
                               onChange={(e) => {
                                   setForm({...form, username: e.target.value});
                                   setUsernameStatus('unknown');
                               }}/>
                        <button type="button" onClick={checkUsername}>중복 확인</button>
                    </div>
                    {usernameStatus === 'available' && <p className="ok">사용할 수 있는 아이디입니다.</p>}
                    {usernameStatus === 'taken' && <p className="field-error">이미 사용 중인 아이디입니다.</p>}
                    <div className="row">
                        <Field label="비밀번호" name="password" type="password" required autoComplete="new-password"
                               value={form.password} error={errorOf('password')} placeholder="8~64자"
                               onChange={(e) => setForm({...form, password: e.target.value})}/>
                        <Field label="비밀번호 확인" name="passwordCheck" type="password" required
                               autoComplete="new-password" value={form.passwordCheck}
                               error={passwordMismatch ? '비밀번호가 일치하지 않습니다.' : errorOf('passwordCheck')}
                               onChange={(e) => setForm({...form, passwordCheck: e.target.value})}/>
                    </div>
                </fieldset>
                {settings
                    ? <ProfileFields value={form} onChange={setForm} errorOf={errorOf} settings={settings}/>
                    : <p className="muted">불러오는 중…</p>}
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting || passwordMismatch || !settings}>
                    <StableLabel current={submitting ? '가입 중…' : '가입하기'} labels={['가입 중…', '가입하기']}/>
                </button>
            </form>
        </section>
    );
}
