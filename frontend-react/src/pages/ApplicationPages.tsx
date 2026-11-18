import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import {ApiError} from '../api/client';
import type {ApplicationForm, FormField, ProductDetail, Submission} from '../api/domain';
import {applicationApi, shopApi} from '../api/endpoints';
import {fileApi} from '../api/files';
import type {Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime} from '../components/format';
import {HtmlView} from '../components/HtmlView';
import {submissionStatus} from '../components/labels';
import {ListSearch, pageParams} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';
import {ProgressAndMessages} from '../components/ProgressPanel';
import {useFeatureMenuName} from '../components/SiteContext';
import {StableLabel} from '../components/StableLabel';
import {type DiscountChoice, DiscountSelector, NO_DISCOUNT} from './shop/DiscountSelector';
import {useI18n} from '../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const period = (form: ApplicationForm, t: T) =>
    form.startDateTime || form.endDateTime ? `${formatDateTime(form.startDateTime)} ~ ${formatDateTime(form.endDateTime)}` : t('shop.application.alwaysOpen', '상시 접수');

export function ApplicationListPage() {
    const {t} = useI18n();
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const title = useFeatureMenuName('APPLICATION', t('admin.nav.applications', '신청·지원'));
    const [data, setData] = useState<Page<ApplicationForm> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        applicationApi.list(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{title}</h1>
            <ListSearch label={t('shop.course.searchLabel', '{title} 검색', {title})} placeholder={t('shop.application.searchPlaceholder', '제목 검색')}/>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((form) => (
                    <li key={form.id}>
                        <Link to={`/applications/${form.id}`}><strong>{form.title}</strong></Link>{' '}
                        {form.isOpen ? <span className="badge ok">{t('shop.reservationItem.open', '접수 중')}</span> : <span className="badge">{t('shop.reservationItem.closed', '접수 마감')}</span>}
                        <span className="muted small"> {period(form, t)}</span>
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">{keyword ? t('shop.common.noSearchResults', '검색 결과가 없습니다.') : t('shop.application.noneInProgress', '진행 중인 신청이 없습니다.')}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

export function ApplicationDetailPage() {
    const {t} = useI18n();
    const id = Number(useParams().id);
    const {user} = useAuth();
    const [form, setForm] = useState<ApplicationForm | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        applicationApi.get(id).then(setForm).catch(setError);
    }, [id]);

    if (!form) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <section className="card form-page">
            <h1>{form.title}</h1>
            <p className="muted">
                {t('shop.application.period', '접수 기간 {period}', {period: period(form, t)})}
                {form.maximumSubmissions && t('shop.application.recruiting', ' · 모집 {max}명 (접수 {count}명)', {max: String(form.maximumSubmissions), count: String(form.submissionCount)})}
                {form.winnerCount != null && t('shop.application.winnerCount', ' · 마감 후 추첨으로 {count}명 선정', {count: String(form.winnerCount)})}
            </p>
            {form.contentHtml && <HtmlView html={form.contentHtml}/>}
            {form.drawnAt ? (
                <p className="notice-box">
                    {t('shop.application.drawnNotice1', '추첨을 마쳤습니다. 결과는')} <Link to="/mypage/applications">{t('shop.application.myApplications', '신청 내역')}</Link>{t('shop.application.drawnNotice2', '에서 확인하세요.')}
                </p>
            ) : !form.isOpen ? (
                <p className="notice-box">{t('shop.application.notAcceptingNow', '지금은 신청을 받지 않습니다.')}</p>
            ) : user?.userType !== 'MEMBER' ? (
                <MemberOnlyNotice message={t('shop.application.memberOnly', '회원만 신청할 수 있습니다.')}/>
            ) : form.applied && !form.allowMultiple ? (
                <p className="notice-box">{t('shop.application.alreadyApplied', '이미 신청했습니다.')} <Link to="/mypage/applications">{t('shop.application.viewMyApplications', '신청 내역 보기')}</Link></p>
            ) : (
                <ApplicationFormView form={form}/>
            )}
        </section>
    );
}

function ApplicationFormView({form}: { form: ApplicationForm }) {
    const {t} = useI18n();
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
                <legend>{t('shop.application.form', '신청서')}</legend>
                {form.fields.map((field) => (
                    <AnswerInput key={field.key} field={field} value={answers[field.key]} onChange={(value) => set(field.key, value)}
                                 error={errorOf(field.key)}/>
                ))}
            </fieldset>
            <ErrorBanner error={error instanceof ApiError && error.code === 'INVALID_INPUT' ? null : error}/>
            {error instanceof ApiError && error.code === 'INVALID_INPUT' && <p className="field-error">{t('shop.application.checkInput', '입력 내용을 확인해 주세요.')}</p>}
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={submitting}>
                    <StableLabel current={submitting ? t('shop.checkoutForm.sending', '보내는 중…') : t('shop.application.apply', '신청하기')}
                                 labels={[t('shop.checkoutForm.sending', '보내는 중…'), t('shop.application.apply', '신청하기')]}/>
                </button>
            </div>
        </form>
    );
}

function AnswerInput({field, value, onChange, error}: {
    field: FormField; value: unknown; onChange: (value: unknown) => void; error?: string;
}) {
    const {t} = useI18n();
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
                        <option value="">{t('shop.application.pleaseSelect', '선택해 주세요')}</option>
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
                    {uploading && <p className="muted small">{t('shop.application.uploading', '올리는 중…')}</p>}
                    {fileName && <p className="muted small">{t('shop.application.attachment', '첨부: {name}', {name: fileName})}</p>}
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
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Submission> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        applicationApi.mine(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>{t('shop.application.myApplications', '신청 내역')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('shop.application.column', '신청')}</th><th>{t('admin.application.appliedAt', '신청일')}</th>
                        <th>{t('admin.order.column.status', '상태')}</th><th>{t('shop.application.guide', '안내')}</th>
                        <th>{t('admin.application.admissionPayment', '선정 후 결제')}</th><th>{t('admin.myOrders.column.progress', '진행·메시지')}</th><th/>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((submission) => (
                        <tr key={submission.id}>
                            <td><Link to={`/applications/${submission.formId}`}>{submission.formTitle}</Link></td>
                            <td>{formatDateTime(submission.createdAt)}</td>
                            <td><span className={`badge${submission.status === 'ACCEPTED' ? ' ok' : submission.status === 'REJECTED' ? ' danger' : ''}`}>
                                {submissionStatus(t)[submission.status]}</span></td>
                            <td className="small">{submission.resultMessage ?? '-'}</td>
                            <td><AdmissionFeeCell submission={submission}/></td>
                            <td>
                                <details>
                                    <summary>{t('admin.application.view', '보기')}</summary>
                                    <ProgressAndMessages
                                        getProgress={() => shopApi.progress('APPLICATION', submission.id)}
                                        listMessages={() => shopApi.messages.list('APPLICATION', submission.id)}
                                        postMessage={(content, fileIds) => shopApi.messages.post('APPLICATION', submission.id, {content, fileIds})}
                                    />
                                </details>
                            </td>
                            <td className="button-cell">
                                {submission.status === 'RECEIVED' && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(t('shop.application.confirmWithdraw', '신청을 취소할까요?'))) {
                                            applicationApi.withdraw(submission.id).then(reload).catch(setError);
                                        }
                                    }}>{t('common.cancel', '취소')}</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={7} className="muted">{t('shop.application.noMyApplications', '신청 내역이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function AdmissionFeeCell({submission}: { submission: Submission }) {
    const {t} = useI18n();
    const [feeProductId, setFeeProductId] = useState<number | null>(null);
    const needsCheck = submission.status === 'ACCEPTED' && !submission.admissionOrderId;

    useEffect(() => {
        if (!needsCheck) {
            return;
        }
        applicationApi.get(submission.formId).then((form) => setFeeProductId(form.admissionFeeProductId)).catch(() => setFeeProductId(null));
    }, [needsCheck, submission.formId]);

    if (submission.admissionOrderId) {
        return <Link to={`/checkout/ORDER/${submission.admissionOrderId}`}>{t('shop.application.viewPayment', '결제 보기')}</Link>;
    }
    if (!needsCheck || !feeProductId) {
        return <span className="muted">-</span>;
    }
    return <Link className="button primary" to={`/applications/submissions/${submission.id}/pay?productId=${feeProductId}`}>{t('shop.application.payAdmissionFee', '입학금 결제하기')}</Link>;
}

export function AdmissionPaymentFormPage() {
    const {t} = useI18n();
    const submissionId = Number(useParams().id);
    const [params] = useSearchParams();
    const productId = Number(params.get('productId'));
    const navigate = useNavigate();
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [discountId, setDiscountId] = useState<number | null>(null);
    const [rewards, setRewards] = useState<DiscountChoice>(NO_DISCOUNT);
    const [error, setError] = useState<unknown>(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        shopApi.product(productId).then(setProduct).catch(setError);
    }, [productId]);

    if (error && !product) {
        return <ErrorBanner error={error}/>;
    }
    if (!product) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }

    const pay = async () => {
        setSubmitting(true);
        setError(null);
        try {
            const order = await applicationApi.payAdmission(submissionId,
                {discountId, couponIssueId: rewards.couponIssueId, usePoints: rewards.usePoints});
            navigate(`/checkout/ORDER/${order.id}`);
        } catch (e) {
            setError(e);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="card narrow">
            <h1>{t('shop.application.admissionFeeTitle', '입학금·입회비 결제')}</h1>
            <p>{product.name}</p>
            {product.discounts.length > 0 && (
                <div className="field">
                    <label htmlFor="discountId">{t('shop.product.discount', '할인')}</label>
                    <select id="discountId" value={discountId ?? ''}
                            onChange={(e) => setDiscountId(e.target.value ? Number(e.target.value) : null)}>
                        <option value="">{t('shop.application.noDiscount', '할인 선택 안 함')}</option>
                        {product.discounts.map((d) => <option key={d.id} value={d.id}>{d.name} ({d.rate}%)</option>)}
                    </select>
                </div>
            )}
            <DiscountSelector postalCode={null} value={rewards} onChange={setRewards}
                               lines={[{productId, productOptionId: null, quantity: 1, discountId}]}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="button" onClick={() => navigate(-1)}>{t('shop.checkoutForm.back', '이전')}</button>
                <button type="button" className="primary" disabled={submitting} onClick={pay}>{t('shop.application.proceedPayment', '결제 진행')}</button>
            </div>
        </section>
    );
}
