import {type FormEvent, useState} from 'react';
import {Link, useLocation, useNavigate, useSearchParams} from 'react-router';
import type {UserType} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {Field} from '../components/Field';
import {socialErrors, SocialLoginButtons} from '../components/SocialLoginButtons';
import {StableLabel} from '../components/StableLabel';
import {useI18n} from '../api/i18n';

export function LoginPage({userType}: { userType: UserType }) {
    const {t} = useI18n();
    const {login} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    const isAdmin = userType === 'ADMIN';
    const [params] = useSearchParams();
    const socialError = params.get('socialError');
    const redirectTo = (location.state as { from?: string } | null)?.from ?? (isAdmin ? '/admin' : '/');

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await login(username, password, userType);
            navigate(redirectTo, {replace: true});
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    const errors = socialErrors(t);
    return (
        <section className="card narrow">
            <h1>{isAdmin ? t('admin.login.title', '관리자 로그인') : t('nav.login', '로그인')}</h1>
            <form onSubmit={handleSubmit}>
                <Field label={t('admin.memberJoin.username', '아이디')} name="username" value={username} autoComplete="username" required
                       onChange={(e) => setUsername(e.target.value)}/>
                <Field label={t('admin.memberJoin.password', '비밀번호')} name="password" type="password" value={password}
                       autoComplete="current-password" required onChange={(e) => setPassword(e.target.value)}/>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting}>
                    <StableLabel current={submitting ? t('shop.login.loggingIn', '로그인 중…') : t('nav.login', '로그인')}
                                 labels={[t('shop.login.loggingIn', '로그인 중…'), t('nav.login', '로그인')]}/>
                </button>
            </form>
            {!isAdmin && socialError && <p className="field-error">{errors[socialError] ?? errors.failed}</p>}
            {!isAdmin && <SocialLoginButtons redirect={redirectTo}/>}
            {!isAdmin && <p className="muted">{t('shop.login.noAccount', '계정이 없으신가요?')} <Link to="/join">{t('shop.common.join', '회원가입')}</Link></p>}
        </section>
    );
}

export function AdminLoginPage() {
    return (
        <main className="auth-page">
            <LoginPage userType="ADMIN"/>
        </main>
    );
}
