import {useCallback, useEffect, useState} from 'react';
import type {Program, QueueStatus, Reservation} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatTime} from '../../components/format';
import {QUEUE_STATUS} from '../../components/labels';

const REFRESH_MS = 10_000;

export function ReceptionQueueAdminPage() {
    const [programs, setPrograms] = useState<Program[] | null>(null);
    const [programId, setProgramId] = useState<number | null>(null);
    const [queue, setQueue] = useState<Reservation[] | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.programs.search({size: 100}).then((page) => {
            const supported = page.content.filter((p) => p.supportsQueue);
            setPrograms(supported);
            setProgramId((current) => current ?? supported[0]?.id ?? null);
        }).catch(setError);
    }, []);

    const reload = useCallback(() => {
        if (programId == null) {
            setQueue([]);
            return;
        }
        adminApi.reservations.queue(programId).then((loaded) => {
            setQueue(loaded);
            setError(null);
        }).catch(setError);
    }, [programId]);

    useEffect(() => {
        reload();
        const timer = window.setInterval(reload, REFRESH_MS);
        return () => window.clearInterval(timer);
    }, [reload]);

    const changeStatus = (id: number, status: QueueStatus) => {
        adminApi.reservations.changeQueueStatus(id, status).then(reload).catch(setError);
    };

    return (
        <section className="card">
            <h1>접수·대기</h1>
            <p className="muted small">접수·대기를 켠 상담 예약 상품에서 체크인한 손님의 대기 순번입니다. 10초마다 새로 불러옵니다.</p>
            <ErrorBanner error={error}/>
            {programs?.length === 0 && <p className="muted">접수·대기를 켠 예약 상품이 없습니다. 예약 메뉴에서 상담 유형 예약 상품의 "접수·대기 사용"을 켜 주세요.</p>}
            {programs != null && programs.length > 0 && (
                <div className="toolbar">
                    <label>
                        예약 상품
                        <select value={programId ?? ''} onChange={(e) => setProgramId(Number(e.target.value))}>
                            {programs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                        </select>
                    </label>
                </div>
            )}
            <table>
                <thead>
                <tr>
                    <th>순번</th>
                    <th>이름</th>
                    <th>연락처</th>
                    <th>체크인 시각</th>
                    <th>상태</th>
                    <th/>
                </tr>
                </thead>
                <tbody>
                {queue?.map((r) => (
                    <tr key={r.id}>
                        <td className="price">{r.queueNumber}</td>
                        <td>{r.applicantName}</td>
                        <td>{r.phoneNumber}</td>
                        <td>{formatTime(r.checkedInAt)}</td>
                        <td>{QUEUE_STATUS[r.queueStatus ?? 'WAITING']}</td>
                        <td>
                            {r.queueStatus === 'WAITING' && (
                                <button type="button" className="primary" onClick={() => changeStatus(r.id, 'CALLED')}>호출</button>
                            )}
                            {r.queueStatus === 'CALLED' && (
                                <>
                                    <button type="button" className="primary" onClick={() => changeStatus(r.id, 'DONE')}>완료</button>{' '}
                                    <button type="button" onClick={() => changeStatus(r.id, 'SKIPPED')}>건너뜀</button>
                                </>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            {queue?.length === 0 && <p className="muted">오늘 대기 중인 손님이 없습니다.</p>}
        </section>
    );
}
