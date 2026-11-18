import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useParams} from 'react-router';
import {memberAdminApi} from '../../api/members';
import type {MemberResponse} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDate, formatDateTime} from '../../components/format';
import {pointType, pointApi, type PointLedger, type PointSummary} from '../../api/rewards';
import type {Page} from '../../api/types';
import {Pagination} from '../../components/Pagination';
import {gradeChangeReason, gradeApi, type GradeHistory, type MyGrade, useGrades} from '../../api/grades';
import type {CustomerRecord} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {useI18n} from '../../api/i18n';

export function MemberDetailPage() {
    const {t} = useI18n();
    const {id} = useParams();
    const [member, setMember] = useState<MemberResponse | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        memberAdminApi.get(Number(id)).then(setMember).catch(setError);
    }, [id]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!member) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }

    const rows: [string, string | null | undefined][] = [
        [t('admin.memberJoin.username', '아이디'), member.username],
        [t('admin.member.nickname', '닉네임'), member.nickname],
        [t('shop.board.name', '이름'), member.name],
        [t('admin.member.birthDate', '생년월일'), member.birthDate],
        [t('admin.member.gender', '성별'), member.gender],
        [t('admin.memberJoin.email', '이메일'), member.email],
        [t('admin.memberJoin.telephone', '전화번호'), member.telephoneNumber],
        [t('admin.memberJoin.mobilePhone', '휴대폰'), member.mobilePhoneNumber],
        [t('admin.order.field.address', '주소'), [member.postalCode && `(${member.postalCode})`, member.address, member.detailAddress].filter(Boolean).join(' ')],
        [t('admin.member.office', '직장'), [member.officeName, member.officeDepartment, member.officePosition].filter(Boolean).join(' / ')],
        [t('admin.member.receptionAgreement', '수신 동의'), [
            member.isMailReceptionAgreed && t('admin.member.mail', '메일'),
            member.isSmsReceptionAgreed && 'SMS',
            member.isMarketingUsageAgreed && t('admin.member.marketing', '마케팅'),
        ].filter(Boolean).join(', ')],
        [t('admin.member.loginTryCount', '로그인 실패 횟수'), String(member.loginTryCount)],
        [t('admin.member.joinedAt', '가입일'), formatDateTime(member.createdAt)],
        [t('admin.account.lastLogin', '마지막 로그인'), formatDateTime(member.lastLoginDateTime)],
    ];

    return (
        <section className="card">
            <h1>{t('admin.member.detailTitle', '회원 상세')}</h1>
            <dl className="meta">
                {rows.map(([label, value]) => (
                    <div key={label} className="meta-row">
                        <dt>{label}</dt>
                        <dd>{value || '-'}</dd>
                    </div>
                ))}
            </dl>
            <MemberGradePanel memberId={member.id}/>
            <MemberPointPanel memberId={member.id}/>
            <MemberRecordPanel memberId={member.id}/>
            <Link to="/admin/members">{t('shop.board.list', '목록으로')}</Link>
        </section>
    );
}

function MemberGradePanel({memberId}: { memberId: number }) {
    const {t} = useI18n();
    const grades = useGrades();
    const [current, setCurrent] = useState<MyGrade | null>(null);
    const [history, setHistory] = useState<GradeHistory[]>([]);
    const [form, setForm] = useState({gradeId: '', expiresAt: ''});
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    const load = useCallback(() => {
        gradeApi.ofMember(memberId).then((grade) => {
            setCurrent(grade);
            setForm({gradeId: grade.gradeId ? String(grade.gradeId) : '', expiresAt: grade.expiresAt?.slice(0, 16) ?? ''});
        }).catch(setError);
        gradeApi.history(memberId).then(setHistory).catch(setError);
    }, [memberId]);
    useEffect(load, [load]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            await gradeApi.assign(memberId, {
                gradeId: form.gradeId ? Number(form.gradeId) : null,
                expiresAt: form.expiresAt ? `${form.expiresAt}:00` : null,
            });
            setSaved(true);
            load();
        } catch (e) {
            setError(e);
        }
    };

    if (grades.length === 0) {
        return <p className="muted">{t('admin.member.noGradesNotice', '회원 등급을 쓰려면 공통 > 회원 등급에서 등급을 먼저 만드세요.')}</p>;
    }
    return (
        <section>
            <h2>{t('admin.nav.grades', '회원 등급')}</h2>
            <p>
                {t('admin.member.currentGrade', '지금 등급:')} <strong>{current?.gradeName ?? '-'}</strong>
                {current?.expiresAt && <> · {t('admin.member.gradeUntil', '{date}까지 (이후 {base})', {date: formatDateTime(current.expiresAt), base: current.baseGradeName ?? t('admin.member.defaultGrade', '기본 등급')})}</>}
            </p>
            <form className="toolbar" onSubmit={submit}>
                <select aria-label={t('admin.account.grade', '등급')} value={form.gradeId} onChange={(e) => setForm({...form, gradeId: e.target.value})}>
                    <option value="">{t('admin.member.defaultGrade', '기본 등급')}</option>
                    {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                </select>
                <input type="datetime-local" aria-label={t('admin.member.expiresAt', '만료 일시')} value={form.expiresAt}
                       onChange={(e) => setForm({...form, expiresAt: e.target.value})}/>
                <button type="submit" className="primary">{t('admin.member.saveGrade', '등급 저장')}</button>
                {saved && <span className="muted">{t('common.saved', '저장했습니다.')}</span>}
            </form>
            <p className="muted">{t('admin.member.gradeExpiresHelp', '만료 일시를 비우면 무기한입니다. 만료 일시를 정하면 그때 지금의 무기한 등급으로 돌아갑니다.')}</p>
            <ErrorBanner error={error}/>
            {history.length > 0 && (
                <table>
                    <thead>
                    <tr>
                        <th>{t('admin.product.stock.time', '일시')}</th><th>{t('admin.member.change', '변경')}</th>
                        <th>{t('admin.pass.column.expiresAt', '만료')}</th><th>{t('admin.member.reason', '사유')}</th><th>{t('admin.member.processedBy', '처리자')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {history.map((row) => (
                        <tr key={row.id}>
                            <td>{formatDateTime(row.createdAt)}</td>
                            <td>{row.fromGradeName ?? t('admin.member.defaultShort', '기본')} → {row.toGradeName ?? t('admin.member.defaultShort', '기본')}</td>
                            <td>{row.expiresAt ? formatDateTime(row.expiresAt) : t('admin.member.indefinite', '무기한')}</td>
                            <td>{gradeChangeReason(t)[row.reason]}{row.orderId && t('admin.member.orderNumber', ' (주문 #{id})', {id: String(row.orderId)})}</td>
                            <td>{row.changedBy ?? t('admin.member.automatic', '자동')}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </section>
    );
}

function MemberPointPanel({memberId}: { memberId: number }) {
    const {t} = useI18n();
    const [summary, setSummary] = useState<PointSummary | null>(null);
    const [page, setPage] = useState(0);
    const [history, setHistory] = useState<Page<PointLedger> | null>(null);
    const [form, setForm] = useState({amount: '', memo: ''});
    const [error, setError] = useState<unknown>(null);
    const load = useCallback(() => {
        pointApi.ofMember(memberId).then(setSummary).catch(setError);
        pointApi.historyOf(memberId, page).then(setHistory).catch(setError);
    }, [memberId, page]);
    useEffect(load, [load]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await pointApi.adjust(memberId, Number(form.amount), form.memo);
            setForm({amount: '', memo: ''});
            load();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <section>
            <h2>{t('admin.nav.points', '포인트')}</h2>
            <p>{t('admin.member.balance', '잔액')} <strong>{(summary?.balance ?? 0).toLocaleString('ko-KR')}P</strong>
                {Number(summary?.expiringSoon) > 0 && <span className="muted"> {t('admin.member.expiringSoon', '· 30일 안에 소멸 {amount}P', {amount: summary!.expiringSoon.toLocaleString('ko-KR')})}</span>}</p>
            <form className="toolbar" onSubmit={submit}>
                <input type="number" aria-label={t('admin.member.adjustPoints', '조정 포인트')} placeholder={t('admin.member.adjustPointsPlaceholder', '+지급 / -차감')} required value={form.amount}
                       onChange={(e) => setForm({...form, amount: e.target.value})}/>
                <input aria-label={t('admin.member.adjustReason', '조정 사유')} className="wide-input" placeholder={t('admin.member.reason', '사유')} required value={form.memo}
                       onChange={(e) => setForm({...form, memo: e.target.value})}/>
                <button type="submit" className="primary">{t('admin.member.adjustPointsAction', '포인트 조정')}</button>
            </form>
            <ErrorBanner error={error}/>
            <PointHistoryTable history={history}/>
            {history && <Pagination page={page} totalPages={history.page.totalPages} onChange={setPage}/>}
        </section>
    );
}

function MemberRecordPanel({memberId}: { memberId: number }) {
    const {t} = useI18n();
    const [records, setRecords] = useState<Page<CustomerRecord> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.customerRecords.search({memberId, size: 5}).then(setRecords).catch(setError);
    }, [memberId]);

    return (
        <section>
            <h2>{t('admin.member.customerRecord', '고객 기록')}</h2>
            <ErrorBanner error={error}/>
            {records && records.content.length > 0 ? (
                <ul className="plain-list">
                    {records.content.map((record) => (
                        <li key={record.id}>
                            {formatDate(record.recordedOn)} · {record.title}
                            {record.category && <span className="badge">{record.category}</span>}
                            {record.sharedWithMember && <span className="muted"> {t('admin.member.sharedWithMember', '· 회원 공개')}</span>}
                        </li>
                    ))}
                </ul>
            ) : <p className="muted">{t('admin.member.noRecords', '기록이 없습니다.')}</p>}
            <Link to={`/admin/customer-records?memberId=${memberId}`}>{t('admin.member.manageRecords', '고객 기록 관리 →')}</Link>
        </section>
    );
}

export function PointHistoryTable({history}: { history: Page<PointLedger> | null }) {
    const {t} = useI18n();
    return (
        <table className="compact">
            <thead><tr>
                <th>{t('admin.product.stock.time', '일시')}</th><th>{t('admin.wallet.type', '구분')}</th>
                <th>{t('admin.nav.points', '포인트')}</th><th>{t('shop.wallet.balanceAfter', '잔액')}</th>
                <th>{t('admin.member.expiresSoonColumn', '소멸 예정')}</th><th>{t('admin.content.field.contentHtml', '내용')}</th>
            </tr></thead>
            <tbody>
            {history?.content.map((row) => (
                <tr key={row.id}>
                    <td>{formatDateTime(row.createdAt)}</td>
                    <td>{pointType(t)[row.type]}</td>
                    <td className={row.amount < 0 ? 'danger-text' : 'ok'}>{row.amount > 0 ? '+' : ''}{row.amount.toLocaleString('ko-KR')}</td>
                    <td>{row.balanceAfter.toLocaleString('ko-KR')}</td>
                    <td>{row.expiresAt ? formatDateTime(row.expiresAt) : '-'}</td>
                    <td>{row.memo ?? (row.orderId ? t('admin.member.orderNumberShort', '주문 #{id}', {id: String(row.orderId)}) : '-')}</td>
                </tr>
            ))}
            {history?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('shop.wallet.noLedger', '내역이 없습니다.')}</td></tr>}
            </tbody>
        </table>
    );
}
