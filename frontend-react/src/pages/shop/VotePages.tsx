import {useEffect, useState} from 'react';
import type {MyVote} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function MyVotesPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyVote> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState<number | null>(null);

    const reload = () => {
        shopApi.votes(page).then(setData).catch(setError);
    };
    useEffect(reload, [page]);

    const vote = async (voteId: number, optionIndex: number) => {
        setBusy(voteId);
        setError(null);
        try {
            await shopApi.castBallot(voteId, optionIndex);
            reload();
        } catch (e) {
            setError(e);
        } finally {
            setBusy(null);
        }
    };

    return (
        <section className="card">
            <h1>투표</h1>
            <ErrorBanner error={error}/>
            {data?.content.map((item) => (
                <article key={item.id} className="card">
                    <h2>{item.title}</h2>
                    <p className="muted small">{formatDateTime(item.createdAt)} · {item.open ? '진행중' : '마감'}</p>
                    {item.description && <p className="pre-wrap">{item.description}</p>}
                    {!item.eligible && <p className="muted">투표할 수 있는 회원 등급이 아닙니다.</p>}
                    {item.eligible && item.myOptionIndex == null && item.open && (
                        <div className="inline-form">
                            {item.options.map((option, index) => (
                                <button key={index} type="button" disabled={busy === item.id}
                                        onClick={() => vote(item.id, index)}>{option}</button>
                            ))}
                        </div>
                    )}
                    {item.myOptionIndex != null && (
                        <p>내가 선택한 안: <strong>{item.options[item.myOptionIndex]}</strong></p>
                    )}
                    {!item.open && item.resultCounts && (
                        <div>
                            <h3>결과 (총 {item.totalBallots}표)</h3>
                            {item.options.map((option, index) => (
                                <div key={index}>{option}: {item.resultCounts?.[index] ?? 0}표</div>
                            ))}
                        </div>
                    )}
                </article>
            ))}
            {data?.content.length === 0 && <p className="muted">등록된 투표가 없습니다.</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
