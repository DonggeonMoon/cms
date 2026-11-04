import {useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {ApplicationForm, Submission, SubmissionStatus} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {SUBMISSION_STATUS, toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'title', label: '제목', required: true, wide: true},
    {name: 'isEnabled', label: '공개', type: 'checkbox', wide: true},
    {name: 'startDateTime', label: '접수 시작', type: 'datetime'},
    {name: 'endDateTime', label: '접수 마감', type: 'datetime'},
    {name: 'maximumSubmissions', label: '최대 접수 건수', type: 'number', help: '비워두면 제한 없음'},
    {name: 'allowMultiple', label: '한 회원이 여러 번 신청 가능', type: 'checkbox'},
    {name: 'fields', label: '입력 항목', type: 'formFields', help: '신청자가 입력할 항목입니다. 형식에 맞게 검사합니다(이메일·전화번호는 정규식 설정을 따름).'},
    {name: 'contentHtml', label: '안내', type: 'html'},
];

const loadForms = (query: Query) => adminApi.applications.search(query);

const fileUrl = (value: unknown) => `/api/files/${value}/download`;

export function ApplicationAdminPage() {
    const [openId, setOpenId] = useState<number | null>(null);
    return (
        <AdminCrudPage<ApplicationForm>
            title="신청·지원"
            description="교육·행사 신청, 지원 사업 접수처럼 신청서를 받습니다. 입력 항목을 직접 구성하고, 접수된 신청을 검토해 결과를 알립니다."
            load={loadForms}
            fields={FIELDS}
            emptyForm={{isEnabled: true, allowMultiple: false, contentHtml: '',
                fields: [{key: 'field1', label: '이름', type: 'TEXT', required: true, options: null, help: null}]}}
            toForm={(form) => ({...form})}
            create={(values: FormValues) => adminApi.applications.create(values)}
            update={(form, values) => adminApi.applications.update(form.id, values)}
            remove={(form) => adminApi.applications.remove(form.id)}
            columns={[
                {label: 'ID', render: (f) => f.id},
                {label: '제목', render: (f) => <Link to={`/applications/${f.id}`}>{f.title}</Link>},
                {label: '접수 기간', render: (f) => `${formatDateTime(f.startDateTime)} ~ ${formatDateTime(f.endDateTime)}`},
                {label: '접수', render: (f) => `${f.submissionCount}${f.maximumSubmissions ? ` / ${f.maximumSubmissions}` : ''}건`},
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
    const [status, setStatus] = useState<SubmissionStatus | ''>('');
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Submission> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        adminApi.applications.submissions(form.id, {status, page}).then(setData).catch(setError);
    }, [form.id, status, page]);
    useEffect(reload, [reload]);

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
            </div>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>신청자</th>
                        {form.fields.map((f) => <th key={f.key}>{f.label}</th>)}
                        <th>신청일</th>
                        <th>심사</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.content.map((submission) => (
                        <tr key={submission.id}>
                            <td>{submission.username}</td>
                            {form.fields.map((f) => <td key={f.key} className="small">{answer(submission, f.key)}</td>)}
                            <td className="small">{formatDateTime(submission.createdAt)}</td>
                            <td><Review submission={submission} onSaved={reload}/></td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={form.fields.length + 3} className="muted">접수된 신청이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </div>
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
