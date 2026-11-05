import {useEffect, useState} from 'react';
import {useParams} from 'react-router';
import type {MyAttendance, MyCustomerRecord} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDate} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function MyRecordsPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyCustomerRecord> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myRecords(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>이용 기록</h1>
            <p className="muted">시술·상담을 받은 뒤 담당자가 남겨 준 기록입니다.</p>
            <ErrorBanner error={error}/>
            {data?.content.map((record) => (
                <article key={record.id} className="card record-card">
                    <h2>{record.title} {record.category && <span className="badge">{record.category}</span>}</h2>
                    <p className="muted small">{formatDate(record.recordedOn)}</p>
                    {record.content && <p className="pre-wrap">{record.content}</p>}
                    {record.photoUrls.length > 0 && (
                        <div className="work-photos">
                            {record.photoUrls.map((url, i) => <a key={url} href={url}><img src={url} alt={`기록 사진 ${i + 1}`}/></a>)}
                        </div>
                    )}
                </article>
            ))}
            {data?.content.length === 0 && <p className="muted">아직 받은 기록이 없습니다.</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

export function MyAttendancePage() {
    const {subscriptionId} = useParams();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyAttendance> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        shopApi.myAttendance(Number(subscriptionId), page).then(setData).catch(setError);
    }, [subscriptionId, page]);

    return (
        <section className="card">
            <h1>출결</h1>
            <p className="muted">등록한 수업의 날짜별 출결입니다.</p>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>날짜</th><th>상태</th><th>비고</th></tr></thead>
                    <tbody>
                    {data?.content.map((entry) => (
                        <tr key={entry.attendedOn}>
                            <td>{formatDate(entry.attendedOn)}</td>
                            <td>{entry.present ? <span className="badge ok">출석</span> : <span className="badge">결석</span>}</td>
                            <td>{entry.note || '-'}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={3} className="muted">출결 기록이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
