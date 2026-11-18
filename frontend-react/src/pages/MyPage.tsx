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
import {useI18n} from '../api/i18n';

export function MyPage() {
    const {t} = useI18n();
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return <ProfileSection member={member} settings={settings} onSaved={setMember}/>;
}

function ProfileSection({member, settings, onSaved}: {
    member: MemberResponse;
    settings: JoinSettings;
    onSaved: (m: MemberResponse) => void;
}) {
    const {t} = useI18n();
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
            <h1>{t('shop.myPage.title', '마이페이지')}</h1>
            <dl className="meta">
                <dt>{t('admin.memberJoin.username', '아이디')}</dt>
                <dd>{member.username}</dd>
                <dt>{t('admin.member.joinedAt', '가입일')}</dt>
                <dd>{formatDateTime(member.createdAt)}</dd>
                <dt>{t('admin.account.lastLogin', '마지막 로그인')}</dt>
                <dd>{formatDateTime(member.lastLoginDateTime)}</dd>
                {grade?.gradeName && (
                    <>
                        <dt>{t('admin.nav.grades', '회원 등급')}</dt>
                        <dd>{grade.gradeName}{grade.expiresAt && (
                            <span className="muted"> {t('admin.member.gradeUntil', '{date}까지 (이후 {base})', {date: formatDateTime(grade.expiresAt), base: grade.baseGradeName ?? t('admin.member.defaultGrade', '기본 등급')})}</span>)}</dd>
                    </>
                )}
                <dt>{t('shop.myPage.referralLink', '추천 링크')}</dt>
                <dd><code>{`${window.location.origin}/join?ref=${member.id}`}</code>
                    <p className="muted small">{t('shop.myPage.referralLinkHelp', '이 링크로 가입한 분이 첫 결제를 마치면 추천 포인트를 받을 수 있습니다.')}</p></dd>
            </dl>
            <form onSubmit={handleSubmit}>
                <ProfileFields value={form} onChange={setForm} settings={settings}
                               errorOf={(field) => (error instanceof ApiError ? error.fieldMessage(field) : undefined)}/>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">{t('common.saved', '저장했습니다.')}</p>}
                <button type="submit" className="primary">{t('shop.myPage.saveInfo', '정보 저장')}</button>
            </form>
        </section>
    );
}

export function PasswordChangePage() {
    const {t} = useI18n();
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
            <h1>{t('shop.myPage.changePassword', '비밀번호 변경')}</h1>
            <form onSubmit={handleSubmit}>
                <Field label={t('shop.myPage.currentPassword', '현재 비밀번호')} name="currentPassword" type="password" required
                       autoComplete="current-password" value={form.currentPassword}
                       onChange={(e) => setForm({...form, currentPassword: e.target.value})}/>
                <Field label={t('shop.myPage.newPassword', '새 비밀번호')} name="newPassword" type="password" required autoComplete="new-password"
                       value={form.newPassword} error={errorOf('newPassword')} placeholder={hints.PASSWORD}
                       onChange={(e) => setForm({...form, newPassword: e.target.value})}/>
                <Field label={t('shop.myPage.newPasswordCheck', '새 비밀번호 확인')} name="newPasswordCheck" type="password" required
                       autoComplete="new-password" value={form.newPasswordCheck}
                       error={mismatch ? t('shop.join.passwordMismatch', '비밀번호가 일치하지 않습니다.') : undefined}
                       onChange={(e) => setForm({...form, newPasswordCheck: e.target.value})}/>
                <ErrorBanner error={error}/>
                {done && <p className="ok">{t('shop.myPage.passwordChanged', '비밀번호를 변경했습니다.')}</p>}
                <button type="submit" disabled={mismatch}>{t('shop.myPage.changePassword', '비밀번호 변경')}</button>
            </form>
        </section>
    );
}

export function QuitPage() {
    const {t} = useI18n();
    const {clear} = useAuth();
    const navigate = useNavigate();
    const [error, setError] = useState<unknown>(null);

    const handleQuit = async () => {
        if (!window.confirm(t('shop.myPage.confirmQuit', '정말 탈퇴하시겠습니까? 탈퇴한 아이디로는 다시 가입할 수 없습니다.'))) {
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
            <h1>{t('shop.myPage.quit', '회원 탈퇴')}</h1>
            <ErrorBanner error={error}/>
            <button type="button" className="danger" onClick={handleQuit}>{t('shop.myPage.quitAction', '탈퇴하기')}</button>
        </section>
    );
}
