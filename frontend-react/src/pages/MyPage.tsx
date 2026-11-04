import {type FormEvent, useEffect, useState} from 'react';
import {useNavigate} from 'react-router';
import {ApiError} from '../api/client';
import {memberApi} from '../api/members';
import {useRegexHints} from '../api/regex';
import type {JoinSettings, MemberProfile, MemberResponse} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {Field} from '../components/Field';
import {formatDateTime} from '../components/format';
import {normalizeProfile, ProfileFields} from './ProfileFields';
import {gradeApi, type MyGrade} from '../api/grades';

export function MyPage() {
    const [member, setMember] = useState<MemberResponse | null>(null);
    const [settings, setSettings] = useState<JoinSettings | null>(null);
    const [loadError, setLoadError] = useState<unknown>(null);

    useEffect(() => {
        Promise.all([memberApi.me(), memberApi.joinSettings()])
            .then(([me, joinSettings]) => {
                setMember(me);
                setSettings(joinSettings);
            })
            .catch(setLoadError);
    }, []);

    if (loadError) {
        return <ErrorBanner error={loadError}/>;
    }
    if (!member || !settings) {
        return <p className="muted">불러오는 중…</p>;
    }
    return <ProfileSection member={member} settings={settings} onSaved={setMember}/>;
}

function ProfileSection({member, settings, onSaved}: {
    member: MemberResponse;
    settings: JoinSettings;
    onSaved: (m: MemberResponse) => void;
}) {
    const [form, setForm] = useState<MemberProfile>(member);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);
    const [grade, setGrade] = useState<MyGrade | null>(null);

    useEffect(() => {
        gradeApi.mine().then(setGrade).catch(() => setGrade(null));
    }, []);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            await memberApi.modify(normalizeProfile(form));
            onSaved(await memberApi.me());
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>마이페이지</h1>
            <dl className="meta">
                <dt>아이디</dt>
                <dd>{member.username}</dd>
                <dt>가입일</dt>
                <dd>{formatDateTime(member.createdAt)}</dd>
                <dt>마지막 로그인</dt>
                <dd>{formatDateTime(member.lastLoginDateTime)}</dd>
                {grade?.gradeName && (
                    <>
                        <dt>회원 등급</dt>
                        <dd>{grade.gradeName}{grade.expiresAt && (
                            <span className="muted"> · {formatDateTime(grade.expiresAt)}까지 (이후 {grade.baseGradeName ?? '기본 등급'})</span>)}</dd>
                    </>
                )}
            </dl>
            <form onSubmit={handleSubmit}>
                <ProfileFields value={form} onChange={setForm} settings={settings}
                               errorOf={(field) => (error instanceof ApiError ? error.fieldMessage(field) : undefined)}/>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">저장했습니다.</p>}
                <button type="submit" className="primary">정보 저장</button>
            </form>
        </section>
    );
}

export function PasswordChangePage() {
    const [form, setForm] = useState({currentPassword: '', newPassword: '', newPasswordCheck: ''});
    const hints = useRegexHints();
    const [error, setError] = useState<unknown>(null);
    const [done, setDone] = useState(false);
    const mismatch = form.newPasswordCheck !== '' && form.newPassword !== form.newPasswordCheck;

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setDone(false);
        try {
            await memberApi.changePassword(form.currentPassword, form.newPassword, form.newPasswordCheck);
            setForm({currentPassword: '', newPassword: '', newPasswordCheck: ''});
            setDone(true);
        } catch (e) {
            setError(e);
        }
    };

    const errorOf = (field: string) => (error instanceof ApiError ? error.fieldMessage(field) : undefined);
    return (
        <section className="card">
            <h1>비밀번호 변경</h1>
            <form onSubmit={handleSubmit}>
                <Field label="현재 비밀번호" name="currentPassword" type="password" required
                       autoComplete="current-password" value={form.currentPassword}
                       onChange={(e) => setForm({...form, currentPassword: e.target.value})}/>
                <Field label="새 비밀번호" name="newPassword" type="password" required autoComplete="new-password"
                       value={form.newPassword} error={errorOf('newPassword')} placeholder={hints.PASSWORD}
                       onChange={(e) => setForm({...form, newPassword: e.target.value})}/>
                <Field label="새 비밀번호 확인" name="newPasswordCheck" type="password" required
                       autoComplete="new-password" value={form.newPasswordCheck}
                       error={mismatch ? '비밀번호가 일치하지 않습니다.' : undefined}
                       onChange={(e) => setForm({...form, newPasswordCheck: e.target.value})}/>
                <ErrorBanner error={error}/>
                {done && <p className="ok">비밀번호를 변경했습니다.</p>}
                <button type="submit" disabled={mismatch}>비밀번호 변경</button>
            </form>
        </section>
    );
}

export function QuitPage() {
    const {clear} = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState<unknown>(null);

    const handleQuit = async () => {
        if (!window.confirm('정말 탈퇴하시겠습니까? 탈퇴한 아이디로는 다시 가입할 수 없습니다.')) {
            return;
        }
        try {
            await memberApi.quit();
            clear();
            navigate('/');
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>회원 탈퇴</h1>
            <ErrorBanner error={error}/>
            <button type="button" className="danger" onClick={handleQuit}>탈퇴하기</button>
        </section>
    );
}
