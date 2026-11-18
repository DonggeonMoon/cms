import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {Receivable, SalesReportRow, SettlementRow} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {won} from '../../components/AdminCrudPage';
import {paymentMethod} from '../../components/labels';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

function todayMinus(days: number): string {
    const d = new Date();
    d.setDate(d.getDate() - days);
    return d.toISOString().slice(0, 10);
}

export function AccountingAdminPage() {
    const {t} = useI18n();
    const [from, setFrom] = useState(todayMinus(30));
    const [to, setTo] = useState(todayMinus(0));
    const [salesReport, setSalesReport] = useState<SalesReportRow[]>([]);
    const [settlements, setSettlements] = useState<SettlementRow[]>([]);
    const [memberIdFilter, setMemberIdFilter] = useState('');
    const [page, setPage] = useState(0);
    const [receivables, setReceivables] = useState<Page<Receivable> | null>(null);
    const [newMemberId, setNewMemberId] = useState('');
    const [newAmount, setNewAmount] = useState('');
    const [newNote, setNewNote] = useState('');
    const [collectAmounts, setCollectAmounts] = useState<Record<number, string>>({});
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    const reloadReports = useCallback(() => {
        adminApi.salesReport(from, to).then(setSalesReport).catch(setError);
        adminApi.settlements(from, to).then(setSettlements).catch(setError);
    }, [from, to]);
    useEffect(reloadReports, [reloadReports]);

    const reloadReceivables = useCallback(() => {
        adminApi.receivables.search(memberIdFilter ? Number(memberIdFilter) : null, page).then(setReceivables).catch(setError);
    }, [memberIdFilter, page]);
    useEffect(reloadReceivables, [reloadReceivables]);

    const totalSales = salesReport.reduce((sum, row) => sum + row.amount, 0);

    const createReceivable = async (e: FormEvent) => {
        e.preventDefault();
        if (!newMemberId || !newAmount || !newNote.trim()) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            await adminApi.receivables.create(Number(newMemberId), null, Number(newAmount), newNote.trim());
            setNewMemberId('');
            setNewAmount('');
            setNewNote('');
            setPage(0);
            reloadReceivables();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    const collect = async (id: number) => {
        const amount = Number(collectAmounts[id]);
        if (!amount || amount <= 0) {
            return;
        }
        setError(null);
        try {
            await adminApi.receivables.collect(id, amount);
            setCollectAmounts((prev) => ({...prev, [id]: ''}));
            reloadReceivables();
        } catch (err) {
            setError(err);
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.accounting.title', '회계')}</h1>
            <ErrorBanner error={error}/>

            <h2>{t('admin.accounting.salesReport', '매출 리포트 · 정산')}</h2>
            <div className="toolbar date-range">
                <label htmlFor="acc-from">{t('admin.accounting.period', '기간')}</label>
                <input id="acc-from" type="date" value={from} max={to} onChange={(e) => setFrom(e.target.value)}/>
                <span aria-hidden="true">~</span>
                <input type="date" value={to} min={from} onChange={(e) => setTo(e.target.value)}/>
            </div>
            <p>{t('admin.accounting.periodTotal', '기간 매출 합계:')} <strong>{won(totalSales)}</strong></p>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>{t('admin.product.stock.date', '날짜')}</th><th>{t('admin.accounting.count', '건수')}</th><th>{t('admin.accounting.netSales', '매출(순액)')}</th></tr></thead>
                    <tbody>
                    {salesReport.map((row) => (
                        <tr key={row.date}><td>{row.date}</td><td>{row.count}</td><td>{won(row.amount)}</td></tr>
                    ))}
                    {salesReport.length === 0 && <tr><td colSpan={3} className="muted">{t('admin.accounting.noSales', '기간 내 매출이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>{t('admin.accounting.paymentMethod', '결제수단')}</th><th>{t('admin.accounting.count', '건수')}</th><th>{t('admin.accounting.netSettlement', '정산액(순액)')}</th></tr></thead>
                    <tbody>
                    {settlements.map((row) => (
                        <tr key={row.method}><td>{paymentMethod(t)[row.method]}</td><td>{row.count}</td><td>{won(row.amount)}</td></tr>
                    ))}
                    {settlements.length === 0 && <tr><td colSpan={3} className="muted">{t('admin.accounting.noPayments', '기간 내 결제가 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>

            <h2>{t('admin.accounting.receivables', '외상(미수금)')}</h2>
            <p className="muted">{t('admin.accounting.receivablesDescription', '결제 없이 먼저 내준 뒤 나중에 받을 돈을 거래처(회원)별로 기록합니다.')}</p>
            <form onSubmit={createReceivable} className="inline-form toolbar">
                <input type="number" value={newMemberId} onChange={(e) => setNewMemberId(e.target.value)} placeholder={t('admin.accounting.memberId', '회원 ID')}/>
                <input type="number" value={newAmount} onChange={(e) => setNewAmount(e.target.value)} placeholder={t('admin.accounting.totalAmount', '외상 총액')}/>
                <input type="text" value={newNote} onChange={(e) => setNewNote(e.target.value)} placeholder={t('admin.accounting.memo', '메모')}/>
                <button type="submit" className="primary" disabled={busy}>{t('admin.accounting.register', '등록')}</button>
            </form>
            <div className="field">
                <label htmlFor="acc-member-filter">{t('admin.accounting.filterByMemberId', '회원 ID로 필터')}</label>
                <input id="acc-member-filter" type="number" value={memberIdFilter}
                       onChange={(e) => { setMemberIdFilter(e.target.value); setPage(0); }} placeholder={t('admin.account.allAreas', '전체')}/>
            </div>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.accounting.memberId', '회원 ID')}</th><th>{t('admin.accounting.total', '총액')}</th>
                        <th>{t('admin.accounting.collectedAmount', '수금액')}</th><th>{t('admin.accounting.remainingAmount', '남은 금액')}</th>
                        <th>{t('admin.accounting.memo', '메모')}</th><th>{t('admin.accounting.collect', '수금')}</th>
                    </tr></thead>
                    <tbody>
                    {receivables?.content.map((r) => (
                        <tr key={r.id}>
                            <td>{r.memberId}</td>
                            <td>{won(r.totalAmount)}</td>
                            <td>{won(r.collectedAmount)}</td>
                            <td>{r.settled ? <span className="badge ok">{t('admin.accounting.fullyPaid', '완납')}</span> : won(r.remaining)}</td>
                            <td>{r.note ?? '-'}</td>
                            <td>
                                {!r.settled && (
                                    <>
                                        <input type="number" value={collectAmounts[r.id] ?? ''}
                                               onChange={(e) => setCollectAmounts((prev) => ({...prev, [r.id]: e.target.value}))}
                                               placeholder={t('admin.accounting.amount', '금액')} style={{width: '7em'}}/>
                                        <button type="button" onClick={() => collect(r.id)}>{t('admin.accounting.collect', '수금')}</button>
                                    </>
                                )}
                            </td>
                        </tr>
                    ))}
                    {receivables?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('admin.accounting.noReceivables', '외상 내역이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {receivables && <Pagination page={page} totalPages={receivables.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
