import {useCallback, useEffect, useState} from 'react';
import {type Coupon, COUPON_STATUS, couponApi, type CouponIssue, couponBenefit, pointApi, type PointLedger, type PointSummary} from '../../api/rewards';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {PointHistoryTable} from '../admin/MemberDetailPage';

export function MyPointsPage() {
    const [summary, setSummary] = useState<PointSummary | null>(null);
    const [page, setPage] = useState(0);
    const [history, setHistory] = useState<Page<PointLedger> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        pointApi.mine().then(setSummary).catch(setError);
    }, []);
    useEffect(() => {
        pointApi.myHistory(page).then(setHistory).catch(setError);
    }, [page]);
    return (
        <section className="card">
            <h1>포인트</h1>
            <ErrorBanner error={error}/>
            <p className="price">{(summary?.balance ?? 0).toLocaleString('ko-KR')}P</p>
            {Number(summary?.expiringSoon) > 0 && (
                <p className="muted">{formatDateTime(summary!.expiringBy)}까지 {summary!.expiringSoon.toLocaleString('ko-KR')}P가 소멸됩니다.</p>
            )}
            <PointHistoryTable history={history}/>
            {history && <Pagination page={page} totalPages={history.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyCouponsPage() {
    const [mine, setMine] = useState<CouponIssue[]>([]);
    const [available, setAvailable] = useState<{ coupon: Coupon; downloaded: boolean; soldOut: boolean }[]>([]);
    const [error, setError] = useState<unknown>(null);
    const load = useCallback(() => {
        couponApi.mine().then(setMine).catch(setError);
        couponApi.downloadable().then(setAvailable).catch(setError);
    }, []);
    useEffect(load, [load]);

    const download = async (id: number) => {
        setError(null);
        try {
            await couponApi.download(id);
            load();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <section className="card">
            <h1>쿠폰함</h1>
            <ErrorBanner error={error}/>
            {available.length > 0 && (
                <>
                    <h2>받을 수 있는 쿠폰</h2>
                    <ul className="coupon-list">
                        {available.map(({coupon, downloaded, soldOut}) => (
                            <li key={coupon.id}>
                                <div>
                                    <strong>{coupon.name}</strong>
                                    <span className="muted small">{couponBenefit(coupon)}</span>
                                </div>
                                <button type="button" className="primary" disabled={downloaded || soldOut} onClick={() => download(coupon.id)}>
                                    {downloaded ? '받음' : soldOut ? '마감' : '받기'}
                                </button>
                            </li>
                        ))}
                    </ul>
                </>
            )}
            <h2>내 쿠폰</h2>
            <ul className="coupon-list">
                {mine.map((issue) => (
                    <li key={issue.id} className={issue.status === 'AVAILABLE' ? undefined : 'unavailable'}>
                        <div>
                            <strong>{issue.couponName}</strong>
                            <span className="muted small">{couponBenefit(issue)}</span>
                            <span className="muted small">{issue.expiresAt ? `${formatDateTime(issue.expiresAt)}까지` : '기한 없음'}</span>
                        </div>
                        <span className={issue.status === 'AVAILABLE' ? 'badge ok' : 'badge'}>{COUPON_STATUS[issue.status]}</span>
                    </li>
                ))}
                {mine.length === 0 && <li className="muted">가진 쿠폰이 없습니다.</li>}
            </ul>
        </section>
    );
}
