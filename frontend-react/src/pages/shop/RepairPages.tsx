import {type FormEvent, useEffect, useState} from 'react';
import type {RepairRequest} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {ProgressAndMessages} from '../../components/ProgressPanel';

export function MyRepairsPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<RepairRequest> | null>(null);
    const [form, setForm] = useState({title: '', description: ''});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    const reload = () => {
        shopApi.repairs.mine(page).then(setData).catch(setError);
    };
    useEffect(reload, [page]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        if (!form.title.trim() || !form.description.trim()) {
            return;
        }
        setSubmitting(true);
        setError(null);
        try {
            await shopApi.repairs.create(form);
            setForm({title: '', description: ''});
            reload();
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card">
            <h1>수리·AS 접수</h1>
            <form onSubmit={submit}>
                <div className="field">
                    <label htmlFor="repair-title">제목</label>
                    <input id="repair-title" required value={form.title} onChange={(e) => setForm({...form, title: e.target.value})}/>
                </div>
                <div className="field">
                    <label htmlFor="repair-description">증상·요청 내용</label>
                    <textarea id="repair-description" rows={4} required value={form.description}
                              onChange={(e) => setForm({...form, description: e.target.value})}/>
                </div>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary" disabled={submitting}>접수하기</button>
            </form>
            {data?.content.map((repair) => (
                <article key={repair.id} className="card">
                    <h2>{repair.title}</h2>
                    <p className="muted small">{formatDateTime(repair.createdAt)}</p>
                    <p className="pre-wrap">{repair.description}</p>
                    <ProgressAndMessages
                        getProgress={() => shopApi.progress('REPAIR', repair.id)}
                        listMessages={() => shopApi.messages.list('REPAIR', repair.id)}
                        postMessage={(content, fileIds) => shopApi.messages.post('REPAIR', repair.id, {content, fileIds})}
                    />
                </article>
            ))}
            {data?.content.length === 0 && <p className="muted">접수한 AS 요청이 없습니다.</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
