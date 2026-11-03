import {useEffect, useState} from 'react';
import type {DailyVisits, PeriodUnit} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {VisitBars} from './DashboardPage';

const pad = (value: number) => String(value).padStart(2, '0');
const isoDate = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const DIMENSIONS = [
    {value: 'URL', label: '페이지'},
    {value: 'BROWSER', label: '브라우저'},
    {value: 'OS', label: '운영체제'},
] as const;

const PERIODS: { value: PeriodUnit; label: string; unitLabel: string; formatLabel: (date: string) => string }[] = [
    {value: 'DAY', label: '일간', unitLabel: '일별', formatLabel: (date) => date.slice(5)},
    {value: 'WEEK', label: '주간', unitLabel: '주별', formatLabel: (date) => `${date.slice(5)}~`},
    {value: 'MONTH', label: '월간', unitLabel: '월별', formatLabel: (date) => date.slice(0, 7)},
];

function defaultRange(unit: PeriodUnit): { from: string; to: string } {
    const today = new Date();
    const from = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    if (unit === 'DAY') {
        from.setDate(from.getDate() - 13);
    } else if (unit === 'WEEK') {
        const mondayOffset = (from.getDay() + 6) % 7;
        from.setDate(from.getDate() - mondayOffset - 7 * 11);
    } else {
        from.setMonth(from.getMonth() - 11, 1);
    }
    return {from: isoDate(from), to: isoDate(today)};
}

export function StatisticsPage() {
    const [unit, setUnit] = useState<PeriodUnit>('DAY');
    const [{from, to}, setRange] = useState(() => defaultRange('DAY'));
    const [dimension, setDimension] = useState<'URL' | 'BROWSER' | 'OS'>('URL');
    const [series, setSeries] = useState<DailyVisits[]>([]);
    const [breakdown, setBreakdown] = useState<{ label: string; visits: number }[]>([]);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setError(null);
        Promise.all([adminApi.statistics.series(unit, from, to), adminApi.statistics.breakdown(dimension, from, to)])
            .then(([seriesRows, breakdownRows]) => {
                setSeries(seriesRows);
                setBreakdown(breakdownRows);
            })
            .catch(setError);
    }, [unit, from, to, dimension]);

    const changeUnit = (next: PeriodUnit) => {
        setUnit(next);
        setRange(defaultRange(next));
    };

    const period = PERIODS.find((p) => p.value === unit)!;
    const total = series.reduce((sum, row) => sum + row.visits, 0);

    return (
        <section className="card">
            <h1>방문 통계</h1>
            <div className="tabs" role="tablist" aria-label="집계 단위">
                {PERIODS.map((p) => (
                    <button key={p.value} type="button" role="tab" aria-selected={unit === p.value}
                            className={unit === p.value ? 'tab active' : 'tab'} onClick={() => changeUnit(p.value)}>
                        {p.label}
                    </button>
                ))}
            </div>
            <div className="toolbar date-range">
                <input type="date" aria-label="시작일" value={from} max={to}
                       onChange={(e) => setRange({from: e.target.value, to})}/>
                <span aria-hidden="true">~</span>
                <input type="date" aria-label="종료일" value={to} min={from}
                       onChange={(e) => setRange({from, to: e.target.value})}/>
            </div>
            <p className="muted">기간 합계 {total.toLocaleString('ko-KR')}회</p>
            <ErrorBanner error={error}/>
            <VisitBars data={series} formatLabel={period.formatLabel} ariaLabel={`${period.unitLabel} 방문 수 막대 그래프`}/>
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
