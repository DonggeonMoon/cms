import {type FormEvent, useEffect, useState} from 'react';
import {type AllowedIp, allowedIpApi, type AllowedIpSettings} from '../../api/allowedIps';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';

export function AllowedIpPage() {
    return <ReauthGate title="허용 IP">{(guard) => <AllowedIpEditor guard={guard}/>}</ReauthGate>;
}

function AllowedIpEditor({guard}: { guard: ReauthGuard }) {
    const [settings, setSettings] = useState<AllowedIpSettings | null>(null);
    const [address, setAddress] = useState('');
    const [memo, setMemo] = useState('');
    const [editing, setEditing] = useState<{ id: number; address: string; memo: string } | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        allowedIpApi.get().then(setSettings).catch(setError);
    }, []);

    const run = async (action: () => Promise<AllowedIpSettings>) => {
        setError(null);
        try {
            setSettings(await guard(action()));
            return true;
        } catch (e) {
            setError(e);
            return false;
        }
    };

    const add = async (event: FormEvent) => {
        event.preventDefault();
        if (await run(() => allowedIpApi.create(address, memo))) {
            setAddress('');
            setMemo('');
        }
    };

    if (!settings) {
        return <section className="card"><h1>허용 IP</h1><ErrorBanner error={error}/></section>;
    }
    const currentListed = settings.currentIpAllowed;
    return (
        <section className="card">
            <h1>허용 IP</h1>
            <div className="muted page-description">
                <p>켜면 아래 목록에 있는 IP에서만 관리자 화면에 로그인하고 관리 기능을 쓸 수 있습니다. 회원이 쓰는 사이트는 막지 않습니다.</p>
                <p>IP는 컴퓨터가 아니라 인터넷 회선(공유기)을 구분합니다. 같은 사무실 공유기를 쓰는 컴퓨터는 모두 같은 IP로 보이고,
                    집·이동 중에는 IP가 바뀌어 접속이 막힐 수 있습니다. 고정 IP가 있는 곳을 등록하세요.</p>
            </div>

            <div className="toolbar">
                <span>지금 접속한 IP: <code>{settings.currentIp}</code></span>
                {currentListed ? <span className="badge ok">목록에 있음</span> : <span className="badge danger">목록에 없음</span>}
                {!currentListed && (
                    <button type="button" onClick={() => void run(() => allowedIpApi.create(settings.currentIp, '현재 접속 IP'))}>
                        현재 IP 추가
                    </button>
                )}
            </div>

            {settings.forceDisabled && (
                <p className="badge danger">서버 설정(CMS_ADMIN_IP_RESTRICTION_DISABLED)으로 제한이 꺼져 있어, 켜도 검사하지 않습니다.</p>
            )}
            <label className="checkbox">
                <input type="checkbox" checked={settings.enabled} onChange={(e) => {
                    const enabled = e.target.checked;
                    if (!enabled || window.confirm('허용 IP에서만 관리자 접속을 받을까요? 목록에 없는 곳에서는 바로 접속이 끊깁니다.')) {
                        void run(() => allowedIpApi.changeEnabled(enabled));
                    }
                }}/>
                허용 IP에서만 관리자 접속 받기
            </label>
            <p className="muted small">지금 접속한 IP가 목록에 없으면 켜거나 지울 수 없습니다. 그래도 접속이 막히면 서버에 환경 변수
                CMS_ADMIN_IP_RESTRICTION_DISABLED=true 를 넣고 다시 시작하세요.</p>

            <form className="toolbar" onSubmit={add}>
                <input aria-label="IP 주소" placeholder="203.0.113.7 또는 203.0.113.0/24" required value={address}
                       onChange={(e) => setAddress(e.target.value)}/>
                <input aria-label="메모" placeholder="메모 (사무실 등)" value={memo} onChange={(e) => setMemo(e.target.value)}/>
                <button type="submit" className="primary">추가</button>
            </form>
            <ErrorBanner error={error}/>

            <div className="table-wrap">
                <table>
                    <thead><tr><th>IP·대역</th><th>메모</th><th>등록일</th><th/></tr></thead>
                    <tbody>
                    {settings.entries.map((entry: AllowedIp) => editing?.id === entry.id ? (
                        <tr key={entry.id}>
                            <td><input aria-label="수정할 IP 주소" value={editing.address}
                                       onChange={(e) => setEditing({...editing, address: e.target.value})}/></td>
                            <td><input aria-label="수정할 메모" value={editing.memo}
                                       onChange={(e) => setEditing({...editing, memo: e.target.value})}/></td>
                            <td>{formatDateTime(entry.createdAt)}</td>
                            <td className="actions">
                                <button type="button" className="primary" onClick={async () => {
                                    if (await run(() => allowedIpApi.update(entry.id, editing.address, editing.memo))) {
                                        setEditing(null);
                                    }
                                }}>저장</button>
                                <button type="button" onClick={() => setEditing(null)}>취소</button>
                            </td>
                        </tr>
                    ) : (
                        <tr key={entry.id}>
                            <td><code>{entry.address}</code></td>
                            <td>{entry.memo || '-'}</td>
                            <td>{formatDateTime(entry.createdAt)}</td>
                            <td className="actions">
                                <button type="button" onClick={() => setEditing({id: entry.id, address: entry.address, memo: entry.memo})}>수정</button>
                                <button type="button" className="danger" onClick={() => {
                                    if (window.confirm(`${entry.address} 을(를) 목록에서 지울까요?`)) {
                                        void run(() => allowedIpApi.remove(entry.id));
                                    }
                                }}>삭제</button>
                            </td>
                        </tr>
                    ))}
                    {settings.entries.length === 0 && <tr><td colSpan={4} className="muted">등록한 IP가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
