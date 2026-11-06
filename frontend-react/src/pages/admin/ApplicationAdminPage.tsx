import {useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {ApplicationForm, FieldSummary, ScoreDetail, Submission, SubmissionStatus, SubmissionSummary} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {SELECTION_METHOD, SUBMISSION_STATUS, toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {ProgressAndMessages, ProgressTemplateManager} from '../../components/ProgressPanel';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const applicationFields = (products: { value: string; label: string }[]): FieldSpec[] => [
    {name: 'title', label: '제목', required: true, wide: true},
    {name: 'isEnabled', label: '공개', type: 'checkbox', wide: true},
    {name: 'startDateTime', label: '접수 시작', type: 'datetime'},
    {name: 'endDateTime', label: '접수 마감', type: 'datetime'},
    {name: 'maximumSubmissions', label: '최대 접수 건수', type: 'number', min: 0, help: '비워두면 제한 없음'},
    {name: 'allowMultiple', label: '한 회원이 여러 번 신청 가능', type: 'checkbox'},
    {name: 'selectionMethod', label: '선정 방식', type: 'select', required: true, options: toOptions(SELECTION_METHOD), newRow: true,
        help: '추첨은 접수(검토 중 포함)된 신청 중에서 무작위로 뽑고, 공모전은 심사위원이 매긴 평균 점수가 높은 순으로 뽑습니다. 둘 다 선정하면 접수를 마감합니다.'},
    {name: 'winnerCount', label: '당첨·선정 인원', type: 'number', min: 1,
        visibleWhen: (v) => v.selectionMethod === 'LOTTERY' || v.selectionMethod === 'CONTEST'},
    {name: 'judgeUsernamesText', label: '심사위원 아이디', type: 'text', wide: true, visibleWhen: (v) => v.selectionMethod === 'CONTEST',
        help: '심사위원으로 지정할 회원 아이디를 쉼표로 구분해 입력합니다. 지정된 회원만 접수마다 점수를 매길 수 있습니다.'},
    {name: 'admissionFeeProductId', label: '선정 후 결제할 상품', type: 'select', options: products, newRow: true,
        help: '합격한 신청자가 마이페이지에서 이 상품(입학금·입회비 등)을 결제합니다. 선택 안 하면 결제 없이 결과만 안내합니다.'},
    {name: 'fields', label: '입력 항목', type: 'formFields', help: '신청자가 입력할 항목입니다. 형식에 맞게 검사합니다(이메일·전화번호는 정규식 설정을 따름).'},
    {name: 'contentHtml', label: '안내', type: 'html'},
];

const loadForms = (query: Query) => adminApi.applications.search(query);

const fileUrl = (value: unknown) => `/api/files/${value}/download`;

const toRequestBody = (values: FormValues): FormValues => {
    const {judgeUsernamesText, ...rest} = values;
    return {...rest, judgeUsernames: typeof judgeUsernamesText === 'string'
            ? judgeUsernamesText.split(',').map((s) => s.trim()).filter(Boolean) : null};
};

export function ApplicationAdminPage() {
    const [openId, setOpenId] = useState<number | null>(null);
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        adminApi.products.search({size: 200})
            .then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    return (
        <AdminCrudPage<ApplicationForm>
            title="신청·지원"
            description={<>
                <p>교육·행사 신청, 지원 사업 접수처럼 신청서를 받습니다. 입력 항목을 직접 구성하고, 접수된 신청을 검토해 결과를 알립니다.</p>
                <ProgressTemplateManager targetType="APPLICATION"/>
            </>}
            load={loadForms}
            fields={applicationFields(products)}
            emptyForm={{isEnabled: true, allowMultiple: false, selectionMethod: 'REVIEW', contentHtml: '',
                fields: [{key: 'field1', label: '이름', type: 'TEXT', required: true, options: null, help: null}]}}
            toForm={(form) => ({...form, judgeUsernamesText: (form.judgeUsernames ?? []).join(', ')})}
            create={(values: FormValues) => adminApi.applications.create(toRequestBody(values))}
            update={(form, values) => adminApi.applications.update(form.id, toRequestBody(values))}
            remove={(form) => adminApi.applications.remove(form.id)}
            columns={[
                {label: 'ID', render: (f) => f.id},
                {label: '제목', render: (f) => <Link to={`/applications/${f.id}`}>{f.title}</Link>},
                {label: '접수 기간', render: (f) => `${formatDateTime(f.startDateTime)} ~ ${formatDateTime(f.endDateTime)}`},
                {label: '접수', render: (f) => `${f.submissionCount}${f.maximumSubmissions ? ` / ${f.maximumSubmissions}` : ''}건`},
                {label: '선정', render: (f) => {
                    if (f.selectionMethod === 'LOTTERY') {
                        return `추첨 ${f.winnerCount}명${f.drawnAt ? ' (추첨 완료)' : ''}`;
                    }
                    if (f.selectionMethod === 'CONTEST') {
                        return `공모전 ${f.winnerCount}명${f.drawnAt ? ' (선정 완료)' : ''}`;
                    }
                    return SELECTION_METHOD.REVIEW;
                }},
                {label: '접수 중', render: (f) => <YesNo value={f.isOpen}/>},
            ]}
            actions={(f) => (
                <button type="button" onClick={() => setOpenId(openId === f.id ? null : f.id)}>{openId === f.id ? '접수 닫기' : '접수 보기'}</button>
            )}
            expandedRow={(f) => openId === f.id && <Submissions form={f}/>}
        />
    );
}

function Submissions({form}: { form: ApplicationForm }) {
    const [drawnAt, setDrawnAt] = useState(form.drawnAt);
    const [drawMessage, setDrawMessage] = useState<string | null>(null);
    const [showSummary, setShowSummary] = useState(false);
    const [status, setStatus] = useState<SubmissionStatus | ''>('');
    const [page, setPage] = useState(0);
    const csvUrl = `/api/admin/applications/${form.id}/submissions.csv${status ? `?status=${status}` : ''}`;
    const [data, setData] = useState<Page<Submission> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        adminApi.applications.submissions(form.id, {status, page}).then(setData).catch(setError);
    }, [form.id, status, page]);
    useEffect(reload, [reload]);

    const draw = async () => {
        if (!window.confirm(`접수된 신청 중 ${form.winnerCount}명을 추첨합니다. 추첨하면 접수가 마감되고 다시 뽑을 수 없습니다.`)) {
            return;
        }
        setError(null);
        try {
            const result = await adminApi.applications.draw(form.id);
            setDrawnAt(result.drawnAt);
            setDrawMessage(`대상 ${result.candidates}명 중 ${result.winners}명이 당첨되었습니다.`);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const finalizeContest = async () => {
        if (!window.confirm(`심사위원 점수가 높은 순으로 ${form.winnerCount}명을 선정합니다. 선정하면 접수가 마감되고 다시 되돌릴 수 없습니다.`)) {
            return;
        }
        setError(null);
        try {
            const result = await adminApi.applications.finalizeContest(form.id);
            setDrawnAt(result.drawnAt);
            setDrawMessage(`대상 ${result.candidates}명 중 ${result.winners}명이 선정되었습니다.`);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const answer = (submission: Submission, key: string) => {
        const field = form.fields.find((f) => f.key === key)!;
        const value = submission.answers[key];
        if (value == null) {
            return '-';
        }
        if (field.type === 'FILE') {
            return <a href={fileUrl(value)}>첨부 파일</a>;
        }
        if (Array.isArray(value)) {
            return value.join(', ');
        }
        return value === true ? '예' : String(value);
    };

    return (
        <div className="submissions">
            <div className="toolbar">
                <select aria-label="심사 상태" value={status} onChange={(e) => setStatus(e.target.value as SubmissionStatus | '')}>
                    <option value="">전체 상태</option>
                    {toOptions(SUBMISSION_STATUS).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
                <button type="button" aria-pressed={showSummary} onClick={() => setShowSummary(!showSummary)}>
                    {showSummary ? '목록 보기' : '응답 집계'}
                </button>
                <a className="button" href={csvUrl} download>CSV 내려받기</a>
                {form.selectionMethod === 'LOTTERY' && (drawnAt
                    ? <span className="muted small">{formatDateTime(drawnAt)} 추첨 완료</span>
                    : <button type="button" className="primary" onClick={draw}>추첨하기</button>)}
                {form.selectionMethod === 'CONTEST' && (drawnAt
                    ? <span className="muted small">{formatDateTime(drawnAt)} 선정 완료</span>
                    : <button type="button" className="primary" onClick={finalizeContest}>공모전 선정하기</button>)}
            </div>
            {drawMessage && <p className="notice-box">{drawMessage}</p>}
            <ErrorBanner error={error}/>
            {showSummary ? <SummaryView formId={form.id} status={status}/> : <>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>신청자</th>
                        {form.fields.map((f) => <th key={f.key}>{f.label}</th>)}
                        <th>신청일</th>
                        {form.selectionMethod === 'CONTEST' && <th>점수</th>}
                        <th>심사</th>
                        <th>선정 후 결제</th>
                        <th>진행·메시지</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.content.map((submission) => (
                        <tr key={submission.id}>
                            <td>{submission.username}</td>
                            {form.fields.map((f) => <td key={f.key} className="small">{answer(submission, f.key)}</td>)}
                            <td className="small">{formatDateTime(submission.createdAt)}</td>
                            {form.selectionMethod === 'CONTEST' && <td className="small"><ScoreCell submission={submission}/></td>}
                            <td><Review submission={submission} onSaved={reload}/></td>
                            <td className="small">{submission.admissionOrderId ? `주문 #${submission.admissionOrderId}` : '-'}</td>
                            <td>
                                <details>
                                    <summary>보기</summary>
                                    <ProgressAndMessages
                                        getProgress={() => adminApi.progress.get('APPLICATION', submission.id)}
                                        advance={(stageCode, memo) => adminApi.progress.advance('APPLICATION', submission.id, {stageCode, memo})}
                                        listMessages={() => adminApi.messages.list('APPLICATION', submission.id)}
                                        postMessage={(content, fileIds) => adminApi.messages.post('APPLICATION', submission.id, {content, fileIds})}
                                    />
                                </details>
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 &&
                        <tr><td colSpan={form.fields.length + 5 + (form.selectionMethod === 'CONTEST' ? 1 : 0)} className="muted">접수된 신청이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
            </>}
        </div>
    );
}

function SummaryView({formId, status}: { formId: number; status: SubmissionStatus | '' }) {
    const [summary, setSummary] = useState<SubmissionSummary | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        adminApi.applications.summary(formId, {status}).then(setSummary).catch(setError);
    }, [formId, status]);
    if (!summary) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    return (
        <div className="summary">
            <p className="muted">응답 {summary.total.toLocaleString('ko-KR')}건{status ? '' : ' (취소 제외)'}</p>
            {summary.fields.map((field) => <FieldSummaryView key={field.key} field={field} total={summary.total}/>)}
        </div>
    );
}

function FieldSummaryView({field, total}: { field: FieldSummary; total: number }) {
    const number = (value: number | null) => (value == null ? '-' : value.toLocaleString('ko-KR'));
    return (
        <section className="summary-field">
            <h3>{field.label} <span className="muted small">응답 {field.answered} / {total}</span></h3>
            {field.options.length > 0 && (
                <ul className="bar-chart">
                    {field.options.map((option) => (
                        <li key={option.option}>
                            <span className="bar-label">{option.option}</span>
                            <span className="bar-track" role="meter" aria-label={option.option} aria-valuenow={option.ratio}
                                  aria-valuemin={0} aria-valuemax={100}>
                                <span className="bar-fill" style={{width: `${Math.min(option.ratio, 100)}%`}}/>
                            </span>
                            <span className="bar-value">{option.count}건 · {option.ratio}%</span>
                        </li>
                    ))}
                </ul>
            )}
            {field.type === 'NUMBER' && (
                <p>평균 <strong>{number(field.average)}</strong> · 최소 {number(field.minimum)} · 최대 {number(field.maximum)}</p>
            )}
            {field.type === 'CHECKBOX' && field.options.length > 1 && <p className="muted small">여러 개를 고를 수 있어 비율의 합이 100%를 넘을 수 있습니다.</p>}
        </section>
    );
}

function ScoreCell({submission}: { submission: Submission }) {
    const [details, setDetails] = useState<ScoreDetail[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const load = () => {
        if (details) {
            return;
        }
        adminApi.applications.scores(submission.id).then(setDetails).catch(setError);
    };
    return (
        <details onToggle={load}>
            <summary>{submission.averageScore == null ? '점수 없음' : `평균 ${submission.averageScore}점 (${submission.scoreCount}명)`}</summary>
            <ErrorBanner error={error}/>
            {details && (
                <ul className="muted small">
                    {details.map((d) => (
                        <li key={d.judgeMemberId}>{d.judgeUsername}: {d.score}점{d.comment ? ` - ${d.comment}` : ''}</li>
                    ))}
                    {details.length === 0 && <li>아직 매긴 점수가 없습니다.</li>}
                </ul>
            )}
        </details>
    );
}

function Review({submission, onSaved}: { submission: Submission; onSaved: () => void }) {
    const [status, setStatus] = useState(submission.status);
    const [resultMessage, setResultMessage] = useState(submission.resultMessage ?? '');
    const [adminMemo, setAdminMemo] = useState(submission.adminMemo ?? '');
    const [error, setError] = useState<unknown>(null);
    if (submission.status === 'WITHDRAWN') {
        return <span className="badge">{SUBMISSION_STATUS.WITHDRAWN}</span>;
    }
    const save = async () => {
        setError(null);
        try {
            await adminApi.applications.review(submission.id, {status, resultMessage, adminMemo});
            onSaved();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="review-cell">
            <select aria-label="심사 결과" value={status} onChange={(e) => setStatus(e.target.value as SubmissionStatus)}>
                {toOptions(SUBMISSION_STATUS).filter((o) => o.value !== 'WITHDRAWN').map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
            <input aria-label="신청자 안내" placeholder="신청자에게 보일 안내" value={resultMessage} onChange={(e) => setResultMessage(e.target.value)}/>
            <input aria-label="관리자 메모" placeholder="관리자 메모" value={adminMemo} onChange={(e) => setAdminMemo(e.target.value)}/>
            <button type="button" onClick={save}>저장</button>
            <ErrorBanner error={error}/>
        </div>
    );
}
