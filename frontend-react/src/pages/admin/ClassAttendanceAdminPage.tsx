import {useEffect, useState} from 'react';
import type {AttendanceRosterEntry, ProductDetail} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';

const today = () => new Date().toISOString().slice(0, 10);

export function ClassAttendanceAdminPage() {
    const [products, setProducts] = useState<ProductDetail[]>([]);
    const [productId, setProductId] = useState('');
    const [date, setDate] = useState(today());
    const [roster, setRoster] = useState<AttendanceRosterEntry[]>([]);
    const [notes, setNotes] = useState<Record<number, string>>({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.products.search({size: 200}).then((page) => setProducts(page.content.filter((p) => p.saleType === 'SUBSCRIPTION')))
            .catch(() => setProducts([]));
    }, []);

    const loadRoster = (id: string, d: string) => {
        if (!id) {
            setRoster([]);
            return;
        }
        setLoading(true);
        setError(null);
        adminApi.classAttendance.roster(Number(id), d)
            .then((entries) => {
                setRoster(entries);
                setNotes(Object.fromEntries(entries.map((e) => [e.subscriptionId, e.note ?? ''])));
            })
            .catch(setError)
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadRoster(productId, date);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [productId, date]);

    const mark = async (subscriptionId: number, present: boolean) => {
        setError(null);
        try {
            const updated = await adminApi.classAttendance.mark(subscriptionId, date, present, notes[subscriptionId] ?? '');
            setRoster((prev) => prev.map((e) => e.subscriptionId === subscriptionId ? updated : e));
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>학원 출결</h1>
            <p className="muted">구독(수강료)으로 등록된 학생의 출결을 날짜별로 매깁니다.</p>
            <div className="toolbar">
                <select aria-label="수강료 상품" value={productId} onChange={(e) => setProductId(e.target.value)}>
                    <option value="">수강료 상품 선택</option>
                    {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
                <input type="date" aria-label="날짜" value={date} onChange={(e) => setDate(e.target.value)}/>
            </div>
            <ErrorBanner error={error}/>
            {loading && <p className="muted">불러오는 중...</p>}
            {!loading && productId && roster.length === 0 && <p className="muted">등록된 학생이 없습니다.</p>}
            {roster.length > 0 && (
                <table>
                    <thead>
                    <tr>
                        <th>학생</th>
                        <th>상태</th>
                        <th>비고</th>
                        <th>처리</th>
                    </tr>
                    </thead>
                    <tbody>
                    {roster.map((entry) => (
                        <tr key={entry.subscriptionId}>
                            <td>{entry.memberName ?? `#${entry.memberId}`}</td>
                            <td>{entry.present === null ? '미확인' : entry.present ? '출석' : '결석'}</td>
                            <td>
                                <input aria-label="비고" value={notes[entry.subscriptionId] ?? ''}
                                       onChange={(e) => setNotes((prev) => ({...prev, [entry.subscriptionId]: e.target.value}))}/>
                            </td>
                            <td className="toolbar">
                                <button type="button" onClick={() => mark(entry.subscriptionId, true)}>출석</button>
                                <button type="button" onClick={() => mark(entry.subscriptionId, false)}>결석</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </section>
    );
}
