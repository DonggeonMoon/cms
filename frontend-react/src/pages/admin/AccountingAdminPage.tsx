import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {Receivable, SalesReportRow, SettlementRow} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {won} from '../../components/AdminCrudPage';
import {PAYMENT_METHOD} from '../../components/labels';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Pagination} from '../../components/Pagination';

function todayMinus(days: number): string {
    const d = new Date();
    d.setDate(d.getDate() - days);
    return d.toISOString().slice(0, 10);
}

export function AccountingAdminPage() {
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
            <h1>회계</h1>
            <ErrorBanner error={error}/>

            <h2>매출 리포트 · 정산</h2>
            <div className="field">
                <label htmlFor="acc-from">기간</label>
                <input id="acc-from" type="date" value={from} onChange={(e) => setFrom(e.target.value)}/>
                <span>~</span>
                <input type="date" value={to} onChange={(e) => setTo(e.target.value)}/>
            </div>
            <p>기간 매출 합계: <strong>{won(totalSales)}</strong></p>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>날짜</th><th>건수</th><th>매출(순액)</th></tr></thead>
                    <tbody>
                    {salesReport.map((row) => (
                        <tr key={row.date}><td>{row.date}</td><td>{row.count}</td><td>{won(row.amount)}</td></tr>
                    ))}
                    {salesReport.length === 0 && <tr><td colSpan={3} className="muted">기간 내 매출이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>결제수단</th><th>건수</th><th>정산액(순액)</th></tr></thead>
                    <tbody>
                    {settlements.map((row) => (
                        <tr key={row.method}><td>{PAYMENT_METHOD[row.method]}</td><td>{row.count}</td><td>{won(row.amount)}</td></tr>
                    ))}
                    {settlements.length === 0 && <tr><td colSpan={3} className="muted">기간 내 결제가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>

            <h2>외상(미수금)</h2>
            <p className="muted">결제 없이 먼저 내준 뒤 나중에 받을 돈을 거래처(회원)별로 기록합니다.</p>
            <form onSubmit={createReceivable} className="inline-form">
                <input type="number" value={newMemberId} onChange={(e) => setNewMemberId(e.target.value)} placeholder="회원 ID"/>
                <input type="number" value={newAmount} onChange={(e) => setNewAmount(e.target.value)} placeholder="외상 총액"/>
                <input type="text" value={newNote} onChange={(e) => setNewNote(e.target.value)} placeholder="메모"/>
                <button type="submit" className="primary" disabled={busy}>등록</button>
            </form>
            <div className="field">
                <label htmlFor="acc-member-filter">회원 ID로 필터</label>
                <input id="acc-member-filter" type="number" value={memberIdFilter}
                       onChange={(e) => { setMemberIdFilter(e.target.value); setPage(0); }} placeholder="전체"/>
            </div>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>회원 ID</th><th>총액</th><th>수금액</th><th>남은 금액</th><th>메모</th><th>수금</th></tr></thead>
                    <tbody>
                    {receivables?.content.map((r) => (
                        <tr key={r.id}>
                            <td>{r.memberId}</td>
                            <td>{won(r.totalAmount)}</td>
                            <td>{won(r.collectedAmount)}</td>
                            <td>{r.settled ? <span className="badge ok">완납</span> : won(r.remaining)}</td>
                            <td>{r.note ?? '-'}</td>
                            <td>
                                {!r.settled && (
                                    <>
                                        <input type="number" value={collectAmounts[r.id] ?? ''}
                                               onChange={(e) => setCollectAmounts((prev) => ({...prev, [r.id]: e.target.value}))}
                                               placeholder="금액" style={{width: '7em'}}/>
                                        <button type="button" onClick={() => collect(r.id)}>수금</button>
                                    </>
                                )}
                            </td>
                        </tr>
                    ))}
                    {receivables?.content.length === 0 && <tr><td colSpan={6} className="muted">외상 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {receivables && <Pagination page={page} totalPages={receivables.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
