import {useCallback, useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import type {ReportDetail, ReportStatus, ReportSummary, Satisfaction} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {REPORT_STATUS, toOptions} from '../../components/labels';
import {SchemaForm} from '../../components/SchemaForm';

const loadReports = (query: Query) => adminApi.reports.search(query);

export function ReportAdminPage() {
    const [status, setStatus] = useState('');
    const [openId, setOpenId] = useState<number | null>(null);
    return (
        <>
            <AdminCrudPage<ReportSummary>
                title="신고"
                load={loadReports}
                extraQuery={{status}}
                filters={
                    <select aria-label="처리 상태" value={status} onChange={(e) => setStatus(e.target.value)}>
                        <option value="">전체 상태</option>
                        {toOptions(REPORT_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </select>
                }
                columns={[
                    {label: '번호', render: (r) => r.id},
                    {label: '제목', render: (r) => r.title},
                    {label: '신고자', render: (r) => r.reporterName},
                    {label: '상태', render: (r) => REPORT_STATUS[r.status]},
                    {label: '접수일', render: (r) => formatDateTime(r.createdAt)},
                ]}
                actions={(r) => <button type="button" onClick={() => setOpenId(r.id)}>처리</button>}
            />
            {openId && <ReportPanel key={openId} id={openId} onClose={() => setOpenId(null)}/>}
        </>
    );
}

function ReportPanel({id, onClose}: { id: number; onClose: () => void }) {
    const [report, setReport] = useState<ReportDetail | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        adminApi.reports.get(id).then(setReport).catch(setError);
    }, [id]);
    useEffect(reload, [reload]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!report) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <section className="card">
            <div className="row space-between">
                <h2>#{report.id} {report.title}</h2>
                <button type="button" onClick={onClose}>닫기</button>
            </div>
            <dl className="meta">
                <dt>신고자</dt>
                <dd>{report.reporterName}</dd>
                <dt>연락처</dt>
                <dd>{[report.phoneNumber, report.email].filter(Boolean).join(' / ') || '-'}</dd>
                <dt>접수일</dt>
                <dd>{formatDateTime(report.createdAt)}</dd>
                <dt>상태</dt>
                <dd>
                    <select aria-label="상태 변경" value={report.status} onChange={async (e) => {
                        await adminApi.reports.changeStatus(id, e.target.value as ReportStatus);
                        reload();
                    }}>
                        {toOptions(REPORT_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </select>
                </dd>
            </dl>
            <HtmlView html={report.contentHtml}/>
            <h3>답변</h3>
            {report.answers.length === 0 && <p className="muted">등록된 답변이 없습니다.</p>}
            {report.answers.map((answer) => (
                <article key={answer.id} className="answer">
                    <div className="row space-between">
                        <strong>{answer.title}</strong>
                        <button type="button" className="danger" onClick={async () => {
                            await adminApi.reports.deleteAnswer(id, answer.id);
                            reload();
                        }}>삭제</button>
                    </div>
                    <HtmlView html={answer.contentHtml}/>
                    <p className="muted small">{answer.authorName} · {formatDateTime(answer.createdAt)}</p>
                </article>
            ))}
            <SchemaForm
                key={report.answers.length}
                title="답변 등록"
                fields={[
                    {name: 'title', label: '제목', required: true},
                    {name: 'contentHtml', label: '내용', type: 'html', required: true},
                ]}
                initial={{title: '처리 결과 안내', contentHtml: ''}}
                submitLabel="답변 등록"
                onSubmit={async (values) => {
                    await adminApi.reports.answer(id, String(values.title), String(values.contentHtml ?? ''));
                    reload();
                }}
            />
        </section>
    );
}

export function SatisfactionAdminPage() {
    const [items, setItems] = useState<Satisfaction[]>([]);
    const [opinions, setOpinions] = useState<{ menuId: number; rows: { score: number; opinion: string; createdAt: string }[] } | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        adminApi.satisfactions.list().then(setItems).catch(setError);
    }, []);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>만족도 조사</h1>
            <p className="muted page-description">메뉴별 만족도 집계입니다. 회원은 메뉴마다 한 번만 참여할 수 있습니다.</p>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>메뉴</th>
                        <th>참여</th>
                        <th>평균</th>
                        <th>5 / 4 / 3 / 2 / 1점</th>
                        <th/>
                    </tr>
                    </thead>
                    <tbody>
                    {items.map((s) => (
                        <tr key={s.menuId}>
                            <td>{s.menuName}</td>
                            <td>{s.total}명</td>
                            <td><strong>{s.average.toFixed(2)}</strong></td>
                            <td>{s.fivePoint} / {s.fourPoint} / {s.threePoint} / {s.twoPoint} / {s.onePoint}</td>
                            <td className="actions">
                                <button type="button" onClick={() => adminApi.satisfactions.opinions(s.menuId)
                                    .then((page) => setOpinions({menuId: s.menuId, rows: page.content}))}>의견 보기</button>
                                <button type="button" className="danger" onClick={async () => {
                                    if (window.confirm('이 메뉴의 만족도 결과를 초기화할까요?')) {
                                        await adminApi.satisfactions.reset(s.menuId);
                                        reload();
                                    }
                                }}>초기화</button>
                            </td>
                        </tr>
                    ))}
                    {items.length === 0 && <tr><td colSpan={5} className="muted">아직 참여 결과가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {opinions && (
                <div className="inline-form">
                    <h2>메뉴 #{opinions.menuId} 의견</h2>
                    {opinions.rows.length === 0 && <p className="muted">의견이 없습니다.</p>}
                    <ul>
                        {opinions.rows.map((row, index) => (
                            <li key={index}>{'★'.repeat(row.score)} {row.opinion} <span className="muted small">{formatDateTime(row.createdAt)}</span></li>
                        ))}
                    </ul>
                </div>
            )}
        </section>
    );
}
