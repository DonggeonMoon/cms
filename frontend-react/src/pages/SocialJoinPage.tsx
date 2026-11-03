import {type FormEvent, useEffect, useState} from 'react';
import {ApiError} from '../api/client';
import {memberApi} from '../api/members';
import {useRegexHints} from '../api/regex';
import {type PendingSocialSignup, socialApi, type SocialJoinRequest} from '../api/social';
import type {JoinSettings} from '../api/types';
import {ErrorBanner} from '../components/ErrorBanner';
import {Field} from '../components/Field';
import {StableLabel} from '../components/StableLabel';
import {ConsentStep, JoinTitle} from './JoinPage';
import {EMPTY_PROFILE, normalizeProfile, ProfileFields} from './ProfileFields';

export function SocialJoinPage() {
    const [pending, setPending] = useState<PendingSocialSignup | null>(null);
    const [settings, setSettings] = useState<JoinSettings | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [agreed, setAgreed] = useState(false);

    useEffect(() => {
        Promise.all([socialApi.pending(), memberApi.joinSettings()])
            .then(([loaded, joinSettings]) => {
                setPending(loaded);
                setSettings(joinSettings);
            })
            .catch(setError);
    }, []);

    if (error && !pending) {
        return (
            <section className="card form-page">
                <JoinTitle/>
                <ErrorBanner error={error}/>
                <p><a href="/login">로그인 화면으로</a></p>
            </section>
        );
    }
    if (!pending || !settings) {
        return <p className="muted">불러오는 중…</p>;
    }
    if (!agreed) {
        return <ConsentStep social={false} onAgree={() => setAgreed(true)}/>;
    }
    return <SocialJoinForm pending={pending} settings={settings}/>;
}

function SocialJoinForm({pending, settings}: { pending: PendingSocialSignup; settings: JoinSettings }) {
    const hints = useRegexHints();
    const [form, setForm] = useState<SocialJoinRequest>({
        ...EMPTY_PROFILE,
        name: pending.name ?? '',
        nickname: pending.nickname,
        email: pending.email,
        mobilePhoneNumber: pending.mobilePhoneNumber,
        username: pending.suggestedUsername,
        isPrivacyConsentAgreed: true,
    });
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);
    const errorOf = (field: string) => (error instanceof ApiError ? error.fieldMessage(field) : undefined);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            const result = await socialApi.signup(normalizeProfile(form));
            // 전체 새로고침으로 로그인 상태를 앱 전체에 반영한다.
            window.location.assign(result.redirect);
        } catch (e) {
            setError(e);
            setSubmitting(false);
        }
    };

    return (
        <section className="card form-page">
            <JoinTitle/>
            <p className="notice-box">{pending.label} 계정으로 가입합니다. 다음부터는 {pending.label} 로그인 버튼으로 바로 로그인할 수 있습니다.</p>
            <form onSubmit={handleSubmit}>
                <fieldset>
                    <legend>계정</legend>
                    <Field label="아이디" name="username" required value={form.username} autoComplete="username"
                           error={errorOf('username')} placeholder={hints.USERNAME}
                           onChange={(e) => setForm({...form, username: e.target.value})}/>
                </fieldset>
                <ProfileFields value={form} onChange={setForm} errorOf={errorOf} settings={settings}/>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting}>
                    <StableLabel current={submitting ? '가입 중…' : '가입하기'} labels={['가입 중…', '가입하기']}/>
                </button>
            </form>
        </section>
    );
}
