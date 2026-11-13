import {type FormEvent, useEffect, useState} from 'react';
import {loginSecurityApi, type LoginSecuritySettings} from '../../api/loginSecurity';
import {ErrorBanner} from '../../components/ErrorBanner';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';

export function LoginSecurityPage() {
    return <ReauthGate title="로그인 보안">{(guard) => <LoginSecurityEditor guard={guard}/>}</ReauthGate>;
}

function LoginSecurityEditor({guard}: { guard: ReauthGuard }) {
    const [settings, setSettings] = useState<LoginSecuritySettings | null>(null);
    const [maxLoginTryCount, setMaxLoginTryCount] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        loginSecurityApi.get().then((s) => {
            setSettings(s);
            setMaxLoginTryCount(String(s.maxLoginTryCount));
        }).catch(setError);
    }, []);

    const save = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            setSettings(await guard(loginSecurityApi.update(Number(maxLoginTryCount))));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    if (!settings) {
        return <section className="card"><h1>로그인 보안</h1><ErrorBanner error={error}/></section>;
    }
    return (
        <section className="card">
            <h1>로그인 보안</h1>
            <div className="muted page-description">
                <p>회원·관리자 모두 로그인에 거듭 실패하면 계정이 잠깁니다. 잠긴 계정은 관리자가 각 관리 화면에서 잠금 해제해야 다시 로그인할 수 있습니다.</p>
            </div>
            <form className="toolbar" onSubmit={save}>
                <label htmlFor="maxLoginTryCount">로그인 실패 허용 횟수</label>
                <input id="maxLoginTryCount" type="number" min={1} max={1000} required value={maxLoginTryCount}
                       onChange={(e) => setMaxLoginTryCount(e.target.value)}/>
                <button type="submit" className="primary">저장</button>
                {saved && <span className="badge ok">저장됨</span>}
            </form>
            <ErrorBanner error={error}/>
        </section>
    );
}
