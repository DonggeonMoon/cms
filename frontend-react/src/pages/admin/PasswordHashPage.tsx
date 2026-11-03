import {useEffect, useState} from 'react';
import {request} from '../../api/client';
import {ErrorBanner} from '../../components/ErrorBanner';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';

type Algorithm = 'BCRYPT' | 'ARGON2' | 'SCRYPT' | 'PBKDF2';

interface PasswordHash {
    algorithm: Algorithm;
    options: { value: Algorithm; label: string; description: string }[];
}

const passwordHashApi = {
    get: () => request<PasswordHash>('/api/admin/password-hash'),
    change: (algorithm: Algorithm) => request<PasswordHash>('/api/admin/password-hash', {method: 'PUT', body: {algorithm}}),
};

export function PasswordHashPage() {
    return <ReauthGate title="해시 알고리즘 관리">{(guard) => <PasswordHashPanel guard={guard}/>}</ReauthGate>;
}

function PasswordHashPanel({guard}: { guard: ReauthGuard }) {
    const [data, setData] = useState<PasswordHash | null>(null);
    const [selected, setSelected] = useState<Algorithm>('BCRYPT');
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        guard(passwordHashApi.get()).then((loaded) => {
            setData(loaded);
            setSelected(loaded.algorithm);
        }).catch(setError);
    }, [guard]);

    const save = async () => {
        const option = data?.options.find((o) => o.value === selected);
        if (!window.confirm(`새로 저장하는 비밀번호부터 ${option?.label ?? selected}(으)로 해시합니다. 계속할까요?`)) {
            return;
        }
        setError(null);
        setSaved(false);
        try {
            setData(await guard(passwordHashApi.change(selected)));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>해시 알고리즘 관리</h1>
            <div className="muted page-description">
                <p>회원·관리자 비밀번호와 비회원 글 비밀번호를 저장할 때 쓰는 해시 알고리즘입니다. Spring Security가 권장하는 안전한
                    알고리즘만 고를 수 있습니다.</p>
                <p>바꿔도 기존 비밀번호로 그대로 로그인할 수 있습니다. 회원·관리자 비밀번호는 다음 로그인에 성공할 때 새 알고리즘으로 다시 저장됩니다.</p>
            </div>
            <ErrorBanner error={error}/>
            {data && (
                <>
                    <div className="hash-options" role="radiogroup" aria-label="해시 알고리즘">
                        {data.options.map((option) => (
                            <label key={option.value} className={`hash-option${selected === option.value ? ' selected' : ''}`}>
                                <input type="radio" name="algorithm" checked={selected === option.value}
                                       onChange={() => setSelected(option.value)}/>
                                <span>
                                    <strong>{option.label}</strong>
                                    {data.algorithm === option.value && <span className="badge ok">사용 중</span>}
                                    <span className="muted small hash-description">{option.description}</span>
                                </span>
                            </label>
                        ))}
                    </div>
                    <div className="toolbar form-actions">
                        <button type="button" className="primary" disabled={selected === data.algorithm} onClick={save}>저장</button>
                        {saved && <span className="ok">저장했습니다.</span>}
                    </div>
                </>
            )}
        </section>
    );
}
