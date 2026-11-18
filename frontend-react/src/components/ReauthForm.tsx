import {type FormEvent, useState} from 'react';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from './ErrorBanner';
import {Field} from './Field';
import {StableLabel} from './StableLabel';
import {useI18n} from '../api/i18n';

interface Props {
    title: string;
    verify: (password: string) => Promise<void>;
    onVerified: () => void;
    reason?: string | null;
}

export function ReauthForm({title, verify, onVerified, reason}: Props) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await verify(password);
            onVerified();
        } catch (e) {
            setError(e);
            setPassword('');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card narrow">
            <h1>{title}</h1>
            <p className="muted">{reason ?? t('admin.reauth.defaultReason', '보안을 위해 비밀번호를 한 번 더 입력해 주세요.')}</p>
            <form onSubmit={handleSubmit}>
                <Field label={t('admin.memberJoin.username', '아이디')} name="username" value={user?.username ?? ''} autoComplete="username" readOnly/>
                <Field label={t('admin.memberJoin.password', '비밀번호')} name="password" type="password" value={password} autoComplete="current-password"
                       required autoFocus onChange={(e) => setPassword(e.target.value)}/>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting}>
                    <StableLabel current={submitting ? t('admin.reauth.checking', '확인 중…') : t('admin.reauth.confirm', '확인')}
                                 labels={[t('admin.reauth.checking', '확인 중…'), t('admin.reauth.confirm', '확인')]}/>
                </button>
            </form>
        </section>
    );
}
