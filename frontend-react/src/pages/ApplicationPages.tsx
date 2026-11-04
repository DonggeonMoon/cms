import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import {ApiError} from '../api/client';
import type {ApplicationForm, FormField, Submission} from '../api/domain';
import {applicationApi} from '../api/endpoints';
import {fileApi} from '../api/files';
import type {Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime} from '../components/format';
import {HtmlView} from '../components/HtmlView';
import {SUBMISSION_STATUS} from '../components/labels';
import {ListSearch, pageParams} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';
import {useFeatureMenuName} from '../components/SiteContext';
import {StableLabel} from '../components/StableLabel';

const period = (form: ApplicationForm) =>
    form.startDateTime || form.endDateTime ? `${formatDateTime(form.startDateTime)} ~ ${formatDateTime(form.endDateTime)}` : '상시 접수';

export function ApplicationListPage() {
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const title = useFeatureMenuName('APPLICATION', '신청·지원');
    const [data, setData] = useState<Page<ApplicationForm> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        applicationApi.list(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{title}</h1>
            <ListSearch label={`${title} 검색`} placeholder="제목 검색"/>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((form) => (
                    <li key={form.id}>
                        <Link to={`/applications/${form.id}`}><strong>{form.title}</strong></Link>{' '}
                        {form.isOpen ? <span className="badge ok">접수 중</span> : <span className="badge">접수 마감</span>}
                        <span className="muted small"> {period(form)}</span>
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : '진행 중인 신청이 없습니다.'}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

export function ApplicationDetailPage() {
    const id = Number(useParams().id);
    const {user} = useAuth();
    const [form, setForm] = useState<ApplicationForm | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        applicationApi.get(id).then(setForm).catch(setError);
    }, [id]);

    if (!form) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    return (
        <section className="card form-page">
            <h1>{form.title}</h1>
            <p className="muted">
                접수 기간 {period(form)}
                {form.maximumSubmissions && ` · 모집 ${form.maximumSubmissions}명 (접수 ${form.submissionCount}명)`}
            </p>
            {form.contentHtml && <HtmlView html={form.contentHtml}/>}
            {!form.isOpen ? (
                <p className="notice-box">지금은 신청을 받지 않습니다.</p>
            ) : user?.userType !== 'MEMBER' ? (
                <MemberOnlyNotice message="회원만 신청할 수 있습니다."/>
            ) : form.applied && !form.allowMultiple ? (
                <p className="notice-box">이미 신청했습니다. <Link to="/mypage/applications">신청 내역 보기</Link></p>
            ) : (
                <ApplicationFormView form={form}/>
            )}
        </section>
    );
}

function ApplicationFormView({form}: { form: ApplicationForm }) {
    const navigate = useNavigate();
    const [answers, setAnswers] = useState<Record<string, unknown>>({});
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);
    const errorOf = (key: string) => (error instanceof ApiError ? error.fieldMessage(key) : undefined);
    const set = (key: string, value: unknown) => setAnswers((current) => ({...current, [key]: value}));

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await applicationApi.submit(form.id, answers);
            navigate('/mypage/applications');
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={submit}>
            <fieldset>
                <legend>신청서</legend>
                {form.fields.map((field) => (
                    <AnswerInput key={field.key} field={field} value={answers[field.key]} onChange={(value) => set(field.key, value)}
                                 error={errorOf(field.key)}/>
                ))}
            </fieldset>
            <ErrorBanner error={error instanceof ApiError && error.code === 'INVALID_INPUT' ? null : error}/>
            {error instanceof ApiError && error.code === 'INVALID_INPUT' && <p className="field-error">입력 내용을 확인해 주세요.</p>}
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={submitting}>
                    <StableLabel current={submitting ? '보내는 중…' : '신청하기'} labels={['보내는 중…', '신청하기']}/>
                </button>
            </div>
        </form>
    );
}

function AnswerInput({field, value, onChange, error}: {
    field: FormField; value: unknown; onChange: (value: unknown) => void; error?: string;
}) {
    const id = `answer-${field.key}`;
    const options = field.options ?? [];
    const label = <label htmlFor={id}>{field.label}{field.required && <span className="required"> *</span>}</label>;
    const help = (
        <>
            {field.help && <p className="muted small">{field.help}</p>}
            {error && <p className="field-error">{error}</p>}
        </>
    );
    const [uploading, setUploading] = useState(false);
    const [fileName, setFileName] = useState<string | null>(null);
    const [uploadError, setUploadError] = useState<unknown>(null);

    switch (field.type) {
        case 'TEXTAREA':
            return <div className="field">{label}<textarea id={id} rows={5} value={(value as string) ?? ''}
                                                          onChange={(e) => onChange(e.target.value)}/>{help}</div>;
        case 'SELECT':
            return (
                <div className="field">{label}
                    <select id={id} value={(value as string) ?? ''} onChange={(e) => onChange(e.target.value)}>
                        <option value="">선택해 주세요</option>
                        {options.map((option) => <option key={option} value={option}>{option}</option>)}
                    </select>{help}
                </div>
            );
        case 'RADIO':
            return (
                <fieldset className="field">
                    <legend>{field.label}{field.required && <span className="required"> *</span>}</legend>
                    {options.map((option) => (
                        <label key={option} className="checkbox">
                            <input type="radio" name={id} checked={value === option} onChange={() => onChange(option)}/> {option}
                        </label>
                    ))}
                    {help}
                </fieldset>
            );
        case 'CHECKBOX':
            if (options.length === 0) {
                return (
                    <div className="field">
                        <label className="checkbox">
                            <input id={id} type="checkbox" checked={value === true} onChange={(e) => onChange(e.target.checked)}/>
                            {field.label}{field.required && <span className="required"> *</span>}
                        </label>
                        {help}
                    </div>
                );
            }
            return (
                <fieldset className="field">
                    <legend>{field.label}{field.required && <span className="required"> *</span>}</legend>
                    {options.map((option) => {
                        const picked = (value as string[] | undefined) ?? [];
                        return (
                            <label key={option} className="checkbox">
                                <input type="checkbox" checked={picked.includes(option)} onChange={(e) =>
                                    onChange(e.target.checked ? [...picked, option] : picked.filter((p) => p !== option))}/> {option}
                            </label>
                        );
                    })}
                    {help}
                </fieldset>
            );
        case 'FILE':
            return (
                <div className="field">{label}
                    <input id={id} type="file" disabled={uploading} onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file) {
                            return;
                        }
                        setUploading(true);
                        setUploadError(null);
                        try {
                            const uploaded = await fileApi.upload(file);
                            setFileName(uploaded.originalName);
                            onChange(uploaded.id);
                        } catch (err) {
                            setUploadError(err);
                        } finally {
                            setUploading(false);
                        }
                    }}/>
                    {uploading && <p className="muted small">올리는 중…</p>}
                    {fileName && <p className="muted small">첨부: {fileName}</p>}
                    <ErrorBanner error={uploadError}/>
                    {help}
                </div>
            );
        default:
            return (
                <div className="field">{label}
                    <input id={id} value={(value as string) ?? ''} onChange={(e) => onChange(e.target.value)}
                           type={field.type === 'NUMBER' ? 'number' : field.type === 'DATE' ? 'date' : field.type === 'EMAIL' ? 'email'
                               : field.type === 'PHONE' ? 'tel' : 'text'}/>
                    {help}
                </div>
            );
    }
}

export function MyApplicationsPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Submission> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        applicationApi.mine(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>신청 내역</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>신청</th><th>신청일</th><th>상태</th><th>안내</th><th/></tr></thead>
                    <tbody>
                    {data?.content.map((submission) => (
                        <tr key={submission.id}>
                            <td><Link to={`/applications/${submission.formId}`}>{submission.formTitle}</Link></td>
                            <td>{formatDateTime(submission.createdAt)}</td>
                            <td><span className={`badge${submission.status === 'ACCEPTED' ? ' ok' : submission.status === 'REJECTED' ? ' danger' : ''}`}>
                                {SUBMISSION_STATUS[submission.status]}</span></td>
                            <td className="small">{submission.resultMessage ?? '-'}</td>
                            <td className="button-cell">
                                {submission.status === 'RECEIVED' && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('신청을 취소할까요?')) {
                                            applicationApi.withdraw(submission.id).then(reload).catch(setError);
                                        }
                                    }}>취소</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">신청 내역이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
