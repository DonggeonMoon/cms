import {type FormEvent, useState} from 'react';
import {Link, useLocation, useNavigate} from 'react-router';
import type {UserType} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {Field} from '../components/Field';

export function LoginPage({userType}: { userType: UserType }) {
    const {login} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    const isAdmin = userType === 'ADMIN';
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

    return (
        <section className="card narrow">
            <h1>{isAdmin ? '관리자 로그인' : '로그인'}</h1>
            <form onSubmit={handleSubmit}>
                <Field label="아이디" name="username" value={username} autoComplete="username" required
                       onChange={(e) => setUsername(e.target.value)}/>
                <Field label="비밀번호" name="password" type="password" value={password}
                       autoComplete="current-password" required onChange={(e) => setPassword(e.target.value)}/>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting}>
                    {submitting ? '로그인 중…' : '로그인'}
                </button>
            </form>
            {!isAdmin && <p className="muted">계정이 없으신가요? <Link to="/join">회원가입</Link></p>}
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
