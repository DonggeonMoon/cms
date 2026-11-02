import {useEffect, useState} from 'react';
import type {DailyVisits} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {VisitBars} from './DashboardPage';

const isoDate = (date: Date) => date.toISOString().slice(0, 10);
const DIMENSIONS = [
    {value: 'URL', label: '페이지'},
    {value: 'BROWSER', label: '브라우저'},
    {value: 'OS', label: '운영체제'},
] as const;

export function StatisticsPage() {
    const today = new Date();
    const [from, setFrom] = useState(isoDate(new Date(today.getTime() - 13 * 86_400_000)));
    const [to, setTo] = useState(isoDate(today));
    const [dimension, setDimension] = useState<'URL' | 'BROWSER' | 'OS'>('URL');
    const [daily, setDaily] = useState<DailyVisits[]>([]);
    const [breakdown, setBreakdown] = useState<{ label: string; visits: number }[]>([]);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setError(null);
        Promise.all([adminApi.statistics.daily(from, to), adminApi.statistics.breakdown(dimension, from, to)])
            .then(([dailyRows, breakdownRows]) => {
                setDaily(dailyRows);
                setBreakdown(breakdownRows);
            })
            .catch(setError);
    }, [from, to, dimension]);

    const total = daily.reduce((sum, row) => sum + row.visits, 0);

    return (
        <section className="card">
            <h1>방문 통계</h1>
            <div className="toolbar">
                <label>시작 <input type="date" value={from} max={to} onChange={(e) => setFrom(e.target.value)}/></label>
                <label>종료 <input type="date" value={to} min={from} onChange={(e) => setTo(e.target.value)}/></label>
                <span className="muted">기간 합계 {total.toLocaleString('ko-KR')}회</span>
            </div>
            <ErrorBanner error={error}/>
            <VisitBars data={daily}/>
            <h2>항목별</h2>
            <div className="toolbar">
                {DIMENSIONS.map((d) => (
                    <button key={d.value} type="button" className={dimension === d.value ? 'primary' : ''}
                            onClick={() => setDimension(d.value)}>{d.label}</button>
                ))}
            </div>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>{DIMENSIONS.find((d) => d.value === dimension)?.label}</th>
                        <th>방문</th>
                        <th>비율</th>
                    </tr>
                    </thead>
                    <tbody>
                    {breakdown.map((row) => (
                        <tr key={row.label}>
                            <td><code>{row.label}</code></td>
                            <td>{row.visits.toLocaleString('ko-KR')}</td>
                            <td>{total ? `${((row.visits / total) * 100).toFixed(1)}%` : '-'}</td>
                        </tr>
                    ))}
                    {breakdown.length === 0 && <tr><td colSpan={3} className="muted">데이터가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
