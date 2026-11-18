import {useEffect, useState} from 'react';
import type {DailyVisits, PeriodUnit} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {VisitBars} from './DashboardPage';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const pad = (value: number) => String(value).padStart(2, '0');
const isoDate = (date: Date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

const dimensions = (t: T) => [
    {value: 'URL', label: t('admin.statistics.dimension.url', '페이지')},
    {value: 'BROWSER', label: t('admin.statistics.dimension.browser', '브라우저')},
    {value: 'OS', label: t('admin.statistics.dimension.os', '운영체제')},
    {value: 'NATION', label: t('admin.statistics.dimension.nation', '국가')},
] as const;

const periods = (t: T): { value: PeriodUnit; label: string; unitLabel: string; formatLabel: (date: string) => string }[] => [
    {value: 'DAY', label: t('admin.statistics.period.day', '일간'), unitLabel: t('admin.statistics.period.dayUnit', '일별'), formatLabel: (date) => date.slice(5)},
    {value: 'WEEK', label: t('admin.statistics.period.week', '주간'), unitLabel: t('admin.statistics.period.weekUnit', '주별'), formatLabel: (date) => `${date.slice(5)}~`},
    {value: 'MONTH', label: t('admin.statistics.period.month', '월간'), unitLabel: t('admin.statistics.period.monthUnit', '월별'), formatLabel: (date) => date.slice(0, 7)},
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
    const {t} = useI18n();
    const [unit, setUnit] = useState<PeriodUnit>('DAY');
    const [{from, to}, setRange] = useState(() => defaultRange('DAY'));
    const [dimension, setDimension] = useState<'URL' | 'BROWSER' | 'OS' | 'NATION'>('URL');
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

    const PERIODS = periods(t);
    const DIMENSIONS = dimensions(t);
    const period = PERIODS.find((p) => p.value === unit)!;
    const total = series.reduce((sum, row) => sum + row.visits, 0);

    return (
        <section className="card">
            <h1>{t('admin.statistics.title', '방문 통계')}</h1>
            <div className="tabs" role="tablist" aria-label={t('admin.statistics.unit', '집계 단위')}>
                {PERIODS.map((p) => (
                    <button key={p.value} type="button" role="tab" aria-selected={unit === p.value}
                            className={unit === p.value ? 'tab active' : 'tab'} onClick={() => changeUnit(p.value)}>
                        {p.label}
                    </button>
                ))}
            </div>
            <div className="toolbar date-range">
                <input type="date" aria-label={t('admin.statistics.from', '시작일')} value={from} max={to}
                       onChange={(e) => setRange({from: e.target.value, to})}/>
                <span aria-hidden="true">~</span>
                <input type="date" aria-label={t('admin.statistics.to', '종료일')} value={to} min={from}
                       onChange={(e) => setRange({from, to: e.target.value})}/>
            </div>
            <p className="muted">{t('admin.statistics.total', '기간 합계 {count}회', {count: total.toLocaleString('ko-KR')})}</p>
            <ErrorBanner error={error}/>
            <VisitBars data={series} formatLabel={period.formatLabel}
                       ariaLabel={t('admin.statistics.chart', '{unit} 방문 수 막대 그래프', {unit: period.unitLabel})}/>
            <h2>{t('admin.statistics.byItem', '항목별')}</h2>
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
                        <th>{t('admin.statistics.visits', '방문')}</th>
                        <th>{t('admin.statistics.ratio', '비율')}</th>
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
                    {breakdown.length === 0 && <tr><td colSpan={3} className="muted">{t('common.noData', '데이터가 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
