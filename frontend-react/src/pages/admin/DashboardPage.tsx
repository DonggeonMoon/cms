import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {DailyVisits, Dashboard, SiteFeature} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {useSite} from '../../components/SiteContext';
import {useI18n} from '../../api/i18n';

export function DashboardPage() {
    const {isEnabled} = useSite();
    const {t} = useI18n();
    const [data, setData] = useState<Dashboard | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.dashboard().then(setData).catch(setError);
    }, []);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!data) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }

    const allTiles: { label: string; value: string; to: string; feature?: SiteFeature }[] = [
        {label: t('admin.dashboard.todayVisits', '오늘 방문'), value: data.todayVisits.toLocaleString('ko-KR'), to: '/admin/statistics'},
        {label: t('admin.dashboard.articleCount', '게시글'), value: data.articleCount.toLocaleString('ko-KR'), to: '/admin/boards', feature: 'BOARD'},
        {label: t('admin.dashboard.unansweredQuestions', '미답변 질문'), value: data.unansweredQuestions.toLocaleString('ko-KR'), to: '/admin/questions?unanswered=true', feature: 'QNA'},
        {label: t('admin.dashboard.pendingPaymentOrders', '결제 대기 주문'), value: data.pendingPaymentOrders.toLocaleString('ko-KR'), to: '/admin/orders', feature: 'PRODUCT'},
        {label: t('admin.dashboard.paidOrders', '결제 완료 주문'), value: data.paidOrders.toLocaleString('ko-KR'), to: '/admin/orders', feature: 'PRODUCT'},
        {label: t('admin.dashboard.confirmedReservations', '확정 예약'), value: data.confirmedReservations.toLocaleString('ko-KR'), to: '/admin/reservations', feature: 'PROGRAM'},
        {label: t('admin.dashboard.revenueLast30Days', '최근 30일 매출'), value: won(data.revenueLast30Days), to: '/admin/payments', feature: 'PRODUCT'},
        {label: t('admin.dashboard.receivedReports', '미처리 신고'), value: data.receivedReports.toLocaleString('ko-KR'), to: '/admin/reports', feature: 'REPORT'},
        {label: t('admin.dashboard.memberCount', '회원'), value: data.memberCount.toLocaleString('ko-KR'), to: '/admin/members'},
    ];
    const tiles = allTiles.filter((tile) => !tile.feature || isEnabled(tile.feature));

    return (
        <>
            <section className="card">
                <h1>{t('admin.dashboard.title', '대시보드')}</h1>
                <div className="tiles">
                    {tiles.map((tile) => (
                        <Link key={tile.label} to={tile.to} className="tile">
                            <span className="muted small">{tile.label}</span>
                            <strong>{tile.value}</strong>
                        </Link>
                    ))}
                </div>
            </section>
            <section className="card">
                <h2>{t('admin.dashboard.last7Days', '최근 7일 방문')}</h2>
                <VisitBars data={data.last7DaysVisits}/>
            </section>
        </>
    );
}

export function VisitBars({data, formatLabel = (date) => date.slice(5), ariaLabel}: {
    data: DailyVisits[];
    formatLabel?: (date: string) => string;
    ariaLabel?: string;
}) {
    const {t} = useI18n();
    const label = ariaLabel ?? t('admin.dashboard.visitChart', '일별 방문 수 막대 그래프');
    const max = Math.max(1, ...data.map((row) => row.visits));
    return (
        <div className="bars" role="img" aria-label={label}>
            {data.map((row) => (
                <div key={row.date} className="bar-col" title={t('admin.dashboard.visitTitle', '{date}: {count}회', {date: formatLabel(row.date), count: String(row.visits)})}>
                    <span className="bar-value">{row.visits}</span>
                    <div className="bar" style={{height: `${(row.visits / max) * 100}%`}}/>
                    <span className="bar-label">{formatLabel(row.date)}</span>
                </div>
            ))}
        </div>
    );
}
