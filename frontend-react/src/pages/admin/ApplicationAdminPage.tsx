import {useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import type {ApplicationForm, FieldSummary, Submission, SubmissionStatus, SubmissionSummary} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {type Category, categorySelectField, useCategories} from '../../api/categories';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {submissionStatus, toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import {ProgressAndMessages, ProgressTemplateManager} from '../../components/ProgressPanel';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const applicationFields = (products: { value: string; label: string }[], categories: Category[], t: T): FieldSpec[] => [
    {name: 'isEnabled', label: t('admin.application.field.isEnabled', '공개'), type: 'checkbox', wide: true},
    {name: 'title', label: t('admin.content.field.title', '제목'), required: true, wide: true},
    ...categorySelectField(categories, t),
    {name: 'contentHtml', label: t('admin.application.field.contentHtml', '안내'), type: 'html'},
    {name: 'fields', label: t('admin.application.field.fields', '입력 항목'), type: 'formFields',
        help: t('admin.application.field.fields.help', '신청자가 입력할 항목입니다. 형식에 맞게 검사합니다(이메일·전화번호는 정규식 설정을 따름).')},
    {name: 'winnerCount', label: t('admin.application.field.winnerCount', '마감 후 추첨할 당첨 인원'), type: 'number', min: 1,
        help: t('admin.application.field.winnerCount.help', '정해두면 접수 마감 후 "추첨하기"로 접수(검토 중 포함)된 신청 중 무작위로 당첨자를 뽑을 수 있습니다. 비워두면 추첨 없이 건마다 수동으로 심사합니다.')},
    {name: 'admissionFeeProductId', label: t('admin.application.field.admissionFeeProductId', '선정 후 결제할 상품'), type: 'select', options: products, newRow: true,
        help: t('admin.application.field.admissionFeeProductId.help', '합격한 신청자가 마이페이지에서 이 상품(입학금·입회비 등)을 결제합니다. 선택 안 하면 결제 없이 결과만 안내합니다.')},
    {name: 'startDateTime', label: t('admin.application.field.startDateTime', '접수 시작'), type: 'datetime', newRow: true},
    {name: 'endDateTime', label: t('admin.application.field.endDateTime', '접수 마감'), type: 'datetime'},
    {name: 'maximumSubmissions', label: t('admin.application.field.maximumSubmissions', '최대 접수 건수'), type: 'number', min: 0, newRow: true, help: t('admin.application.field.maximumSubmissions.help', '비워두면 제한 없음')},
    {name: 'allowMultiple', label: t('admin.application.field.allowMultiple', '한 회원이 여러 번 신청 가능'), type: 'checkbox', wide: true},
];

const loadForms = (query: Query) => adminApi.applications.search(query);

const fileUrl = (value: unknown) => `/api/files/${value}/download`;

const toFormBody = (values: FormValues) => ({...values, categoryId: values.categoryId ? Number(values.categoryId) : null});

export function ApplicationAdminPage() {
    const {t} = useI18n();
    const [openId, setOpenId] = useState<number | null>(null);
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    const categories = useCategories();
    useEffect(() => {
        adminApi.products.search({size: 200})
            .then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    return (
        <AdminCrudPage<ApplicationForm>
            title={t('admin.nav.applications', '신청·지원')}
            description={<>
                <p>{t('admin.application.description', '교육·행사 신청, 지원 사업 접수처럼 신청서를 받습니다. 입력 항목을 직접 구성하고, 접수된 신청을 검토해 결과를 알립니다.')}</p>
                <ProgressTemplateManager targetType="APPLICATION"/>
            </>}
            load={loadForms}
            fields={applicationFields(products, categories, t)}
            emptyForm={{isEnabled: true, allowMultiple: false, contentHtml: '',
                fields: [{key: 'field1', label: t('admin.application.defaultFieldName', '이름'), type: 'TEXT', required: true, options: null, help: null}]}}
            create={(values: FormValues) => adminApi.applications.create(toFormBody(values))}
            update={(form, values) => adminApi.applications.update(form.id, toFormBody(values))}
            remove={(form) => adminApi.applications.remove(form.id)}
            columns={[
                {label: 'ID', render: (f) => f.id},
                {label: t('admin.content.field.title', '제목'), render: (f) => <Link to={`/applications/${f.id}`}>{f.title}</Link>},
                {label: t('admin.reservationItem.column.category', '카테고리'), render: (f) => categories.find((c) => c.id === f.categoryId)?.name ?? '-'},
                {label: t('admin.application.column.period', '접수 기간'), render: (f) => `${formatDateTime(f.startDateTime)} ~ ${formatDateTime(f.endDateTime)}`},
                {label: t('admin.application.column.submissions', '접수'), render: (f) => t('admin.application.submissionCount', '{count}{total}건', {count: String(f.submissionCount), total: f.maximumSubmissions ? ` / ${f.maximumSubmissions}` : ''})},
                {label: t('admin.application.column.draw', '추첨'), render: (f) => f.winnerCount
                    ? t('admin.application.winnerCount', '{count}명{done}', {count: String(f.winnerCount), done: f.drawnAt ? t('admin.application.drawDone', ' (추첨 완료)') : ''})
                    : <span className="muted">{t('admin.application.manualReview', '수동 심사')}</span>},
                {label: t('admin.reservationItem.column.isOpen', '접수 중'), render: (f) => <YesNo value={f.isOpen}/>},
            ]}
            actions={(f) => (
                <button type="button" onClick={() => setOpenId(openId === f.id ? null : f.id)}>
                    {openId === f.id ? t('admin.application.closeSubmissions', '접수 닫기') : t('admin.application.viewSubmissions', '접수 보기')}
                </button>
            )}
            expandedRow={(f) => openId === f.id && <Submissions form={f}/>}
        />
    );
}

function Submissions({form}: { form: ApplicationForm }) {
    const {t} = useI18n();
    const [drawnAt, setDrawnAt] = useState(form.drawnAt);
    const [drawMessage, setDrawMessage] = useState<string | null>(null);
    const [showSummary, setShowSummary] = useState(false);
    const [status, setStatus] = useState<SubmissionStatus | ''>('');
    const [page, setPage] = useState(0);
    const [openId, setOpenId] = useState<number | null>(null);
    const [progressId, setProgressId] = useState<number | null>(null);
    const csvUrl = `/api/admin/applications/${form.id}/submissions.csv${status ? `?status=${status}` : ''}`;
    const [data, setData] = useState<Page<Submission> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        adminApi.applications.submissions(form.id, {status, page}).then(setData).catch(setError);
    }, [form.id, status, page]);
    useEffect(reload, [reload]);

    const draw = async () => {
        if (!window.confirm(t('admin.application.confirmDraw', '접수된 신청 중 {count}명을 추첨합니다. 추첨하면 접수가 마감되고 다시 뽑을 수 없습니다.', {count: String(form.winnerCount)}))) {
            return;
        }
        setError(null);
        try {
            const result = await adminApi.applications.draw(form.id);
            setDrawnAt(result.drawnAt);
            setDrawMessage(t('admin.application.drawResult', '대상 {candidates}명 중 {winners}명이 당첨되었습니다.', {candidates: String(result.candidates), winners: String(result.winners)}));
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
            return <a href={fileUrl(value)}>{t('admin.application.attachedFile', '첨부 파일')}</a>;
        }
        if (Array.isArray(value)) {
            return value.join(', ');
        }
        return value === true ? t('common.yes', '예') : String(value);
    };

    return (
        <div className="submissions">
            <div className="toolbar">
                <select aria-label={t('admin.application.reviewStatus', '심사 상태')} value={status} onChange={(e) => setStatus(e.target.value as SubmissionStatus | '')}>
                    <option value="">{t('admin.common.allStatuses', '전체 상태')}</option>
                    {toOptions(submissionStatus(t)).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
                <button type="button" aria-pressed={showSummary} onClick={() => setShowSummary(!showSummary)}>
                    {showSummary ? t('admin.application.viewList', '목록 보기') : t('admin.application.viewSummary', '응답 집계')}
                </button>
                <a className="button" href={csvUrl} download>{t('admin.application.downloadCsv', 'CSV 내려받기')}</a>
                {form.winnerCount != null && (drawnAt
                    ? <span className="muted small">{t('admin.application.drawCompletedAt', '{date} 추첨 완료', {date: formatDateTime(drawnAt)})}</span>
                    : <button type="button" className="primary" onClick={draw}>{t('admin.application.drawAction', '추첨하기')}</button>)}
            </div>
            {drawMessage && <p className="notice-box">{drawMessage}</p>}
            <ErrorBanner error={error}/>
            {showSummary ? <SummaryView formId={form.id} status={status}/> : <>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>{t('admin.order.column.receiver', '신청자')}</th>
                        <th>{t('admin.application.appliedAt', '신청일')}</th>
                        <th>{t('admin.application.review', '심사')}</th>
                        <th>{t('admin.application.admissionPayment', '선정 후 결제')}</th>
                        <th>{t('admin.myOrders.column.progress', '진행·메시지')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.content.map((submission) => (
                        <>
                            <tr key={submission.id}>
                                <td>
                                    <button type="button" className="link-button"
                                            onClick={() => setOpenId(openId === submission.id ? null : submission.id)}>
                                        {submission.username}
                                    </button>
                                </td>
                                <td className="small">{formatDateTime(submission.createdAt)}</td>
                                <td><Review submission={submission} onSaved={reload}/></td>
                                <td className="small">{submission.admissionOrderId ? t('admin.application.orderNumber', '주문 #{id}', {id: String(submission.admissionOrderId)}) : '-'}</td>
                                <td>
                                    <button type="button" onClick={() => setProgressId(progressId === submission.id ? null : submission.id)}>
                                        {progressId === submission.id ? t('common.close', '닫기') : t('admin.application.view', '보기')}
                                    </button>
                                </td>
                            </tr>
                            {progressId === submission.id && (
                                <tr>
                                    <td colSpan={5}>
                                        <ProgressAndMessages
                                            getProgress={() => adminApi.progress.get('APPLICATION', submission.id)}
                                            advance={(stageCode, memo) => adminApi.progress.advance('APPLICATION', submission.id, {stageCode, memo})}
                                            listMessages={() => adminApi.messages.list('APPLICATION', submission.id)}
                                            postMessage={(content, fileIds) => adminApi.messages.post('APPLICATION', submission.id, {content, fileIds})}
                                        />
                                    </td>
                                </tr>
                            )}
                            {openId === submission.id && (
                                <tr>
                                    <td colSpan={4}>
                                        <dl className="meta">
                                            {form.fields.map((f) => (
                                                <>
                                                    <dt key={`${f.key}-label`}>{f.label}</dt>
                                                    <dd key={`${f.key}-value`}>{answer(submission, f.key)}</dd>
                                                </>
                                            ))}
                                        </dl>
                                    </td>
                                </tr>
                            )}
                        </>
                    ))}
                    {data?.content.length === 0 &&
                        <tr><td colSpan={5} className="muted">{t('admin.application.noSubmissions', '접수된 신청이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
            </>}
        </div>
    );
}

function SummaryView({formId, status}: { formId: number; status: SubmissionStatus | '' }) {
    const {t} = useI18n();
    const [summary, setSummary] = useState<SubmissionSummary | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        adminApi.applications.summary(formId, {status}).then(setSummary).catch(setError);
    }, [formId, status]);
    if (!summary) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <div className="summary">
            <p className="muted">{t('admin.application.summaryTotal', '응답 {count}건{excludeCanceled}', {count: summary.total.toLocaleString('ko-KR'), excludeCanceled: status ? '' : t('admin.application.excludeCanceled', ' (취소 제외)')})}</p>
            {summary.fields.map((field) => <FieldSummaryView key={field.key} field={field} total={summary.total}/>)}
        </div>
    );
}

function FieldSummaryView({field, total}: { field: FieldSummary; total: number }) {
    const {t} = useI18n();
    const number = (value: number | null) => (value == null ? '-' : value.toLocaleString('ko-KR'));
    return (
        <section className="summary-field">
            <h3>{field.label} <span className="muted small">{t('admin.application.answeredOf', '응답 {answered} / {total}', {answered: String(field.answered), total: String(total)})}</span></h3>
            {field.options.length > 0 && (
                <ul className="bar-chart">
                    {field.options.map((option) => (
                        <li key={option.option}>
                            <span className="bar-label">{option.option}</span>
                            <span className="bar-track" role="meter" aria-label={option.option} aria-valuenow={option.ratio}
                                  aria-valuemin={0} aria-valuemax={100}>
                                <span className="bar-fill" style={{width: `${Math.min(option.ratio, 100)}%`}}/>
                            </span>
                            <span className="bar-value">{t('admin.application.optionCount', '{count}건 · {ratio}%', {count: String(option.count), ratio: String(option.ratio)})}</span>
                        </li>
                    ))}
                </ul>
            )}
            {field.type === 'NUMBER' && (
                <p>{t('admin.application.average', '평균')} <strong>{number(field.average)}</strong> · {t('admin.application.minimum', '최소')} {number(field.minimum)} · {t('admin.application.maximum', '최대')} {number(field.maximum)}</p>
            )}
            {field.type === 'CHECKBOX' && field.options.length > 1 && <p className="muted small">{t('admin.application.multiSelectNotice', '여러 개를 고를 수 있어 비율의 합이 100%를 넘을 수 있습니다.')}</p>}
        </section>
    );
}

function Review({submission, onSaved}: { submission: Submission; onSaved: () => void }) {
    const {t} = useI18n();
    const [status, setStatus] = useState(submission.status);
    const [resultMessage, setResultMessage] = useState(submission.resultMessage ?? '');
    const [adminMemo, setAdminMemo] = useState(submission.adminMemo ?? '');
    const [error, setError] = useState<unknown>(null);
    if (submission.status === 'WITHDRAWN') {
        return <span className="badge">{submissionStatus(t).WITHDRAWN}</span>;
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
            <select aria-label={t('admin.application.reviewResult', '심사 결과')} value={status} onChange={(e) => setStatus(e.target.value as SubmissionStatus)}>
                {toOptions(submissionStatus(t)).filter((o) => o.value !== 'WITHDRAWN').map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
            <input aria-label={t('admin.application.applicantNotice', '신청자 안내')} placeholder={t('admin.application.applicantNoticePlaceholder', '신청자에게 보일 안내')}
                   value={resultMessage} onChange={(e) => setResultMessage(e.target.value)}/>
            <input aria-label={t('admin.application.adminMemo', '관리자 메모')} placeholder={t('admin.application.adminMemo', '관리자 메모')}
                   value={adminMemo} onChange={(e) => setAdminMemo(e.target.value)}/>
            <button type="button" onClick={save}>{t('common.save', '저장')}</button>
            <ErrorBanner error={error}/>
        </div>
    );
}
