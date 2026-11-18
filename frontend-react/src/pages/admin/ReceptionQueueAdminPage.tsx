import {useCallback, useEffect, useState} from 'react';
import type {QueueStatus, Reservation, ReservationItem} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatTime} from '../../components/format';
import {queueStatus} from '../../components/labels';
import {useI18n} from '../../api/i18n';

const REFRESH_MS = 10_000;

export function ReceptionQueueAdminPage() {
    const {t} = useI18n();
    const [reservationItems, setReservationItems] = useState<ReservationItem[] | null>(null);
    const [programId, setProgramId] = useState<number | null>(null);
    const [queue, setQueue] = useState<Reservation[] | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.reservationItems.search({size: 100}).then((page) => {
            const supported = page.content.filter((p) => p.supportsQueue);
            setReservationItems(supported);
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
            <h1>{t('admin.receptionQueue.title', '접수·대기')}</h1>
            <p className="muted small">{t('admin.receptionQueue.description', '접수·대기를 켠 상담 예약 상품에서 체크인한 손님의 대기 순번입니다. 10초마다 새로 불러옵니다.')}</p>
            <ErrorBanner error={error}/>
            {reservationItems?.length === 0 && <p className="muted">{t('admin.receptionQueue.noItems', '접수·대기를 켠 예약 상품이 없습니다. 예약 메뉴에서 상담 유형 예약 상품의 "접수·대기 사용"을 켜 주세요.')}</p>}
            {reservationItems != null && reservationItems.length > 0 && (
                <div className="toolbar">
                    <label>{t('admin.receptionQueue.reservationItem', '예약 상품')}{' '}
                        <select value={programId ?? ''} onChange={(e) => setProgramId(Number(e.target.value))}>
                            {reservationItems.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                        </select>
                    </label>
                </div>
            )}
            <table>
                <thead>
                <tr>
                    <th>{t('admin.receptionQueue.number', '순번')}</th>
                    <th>{t('shop.board.name', '이름')}</th>
                    <th>{t('admin.guestPosting.phone', '연락처')}</th>
                    <th>{t('admin.receptionQueue.checkedInAt', '체크인 시각')}</th>
                    <th>{t('admin.order.column.status', '상태')}</th>
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
                        <td>{queueStatus(t)[r.queueStatus ?? 'WAITING']}</td>
                        <td>
                            {r.queueStatus === 'WAITING' && (
                                <button type="button" className="primary" onClick={() => changeStatus(r.id, 'CALLED')}>{t('admin.receptionQueue.call', '호출')}</button>
                            )}
                            {r.queueStatus === 'CALLED' && (
                                <>
                                    <button type="button" className="primary" onClick={() => changeStatus(r.id, 'DONE')}>{t('admin.receptionQueue.done', '완료')}</button>{' '}
                                    <button type="button" onClick={() => changeStatus(r.id, 'SKIPPED')}>{t('admin.receptionQueue.skip', '건너뜀')}</button>
                                </>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            {queue?.length === 0 && <p className="muted">{t('admin.receptionQueue.noWaiting', '오늘 대기 중인 손님이 없습니다.')}</p>}
        </section>
    );
}
