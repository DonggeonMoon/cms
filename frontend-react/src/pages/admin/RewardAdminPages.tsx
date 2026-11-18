import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {adminApi} from '../../api/endpoints';
import {useGrades} from '../../api/grades';
import {type Coupon, couponIssueType, couponStatus, couponApi, type CouponIssue, couponBenefit, type PointPolicy, pointApi} from '../../api/rewards';
import type {Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {useI18n} from '../../api/i18n';
import {toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

export function PointPolicyPage() {
    const {t} = useI18n();
    const [policy, setPolicy] = useState<PointPolicy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);
    useEffect(() => {
        pointApi.policy().then(setPolicy).catch(setError);
    }, []);
    if (!policy) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    const number = (key: keyof PointPolicy, label: string, help: string, step = 1) => (
        <div className="field">
            <label htmlFor={key}>{label}</label>
            <input id={key} type="number" min={0} step={step} required value={Number(policy[key])}
                   onChange={(e) => setPolicy({...policy, [key]: Number(e.target.value)})}/>
            <p className="muted small">{help}</p>
        </div>
    );
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            setPolicy(await pointApi.savePolicy(policy));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };
    return (
        <section className="card">
            <h1>{t('admin.nav.points', '포인트')}</h1>
            <p className="muted">{t('admin.point.description', '결제가 끝나면 쿠폰·포인트를 뺀 상품 금액(배송비 제외)에 적립률을 곱해 적립합니다. 주문을 취소하면 쓴 포인트는 돌려주고 적립한 포인트는 거둡니다. 회원별 잔액 조정은 회원 상세에서 합니다.')}</p>
            <form onSubmit={submit}>
                <label className="checkbox">
                    <input type="checkbox" checked={policy.isEnabled} onChange={(e) => setPolicy({...policy, isEnabled: e.target.checked})}/>
                    {t('admin.point.isEnabled', '포인트 사용')}
                </label>
                <div className="schema-grid">
                    {number('earnRate', t('admin.point.earnRate', '적립률(%)'), t('admin.point.earnRate.help', '예: 1 이면 1만 원 결제 시 100포인트'), 0.1)}
                    {number('expireDays', t('admin.point.expireDays', '유효 기간(일)'), t('admin.point.expireDays.help', '적립일부터 이 기간이 지나면 소멸합니다.'))}
                    {number('minUsePoints', t('admin.point.minUsePoints', '최소 사용 포인트'), t('admin.point.minUsePoints.help', '0 이면 제한 없음'))}
                    {number('maxUseRate', t('admin.point.maxUseRate', '최대 사용 비율(%)'), t('admin.point.maxUseRate.help', '쿠폰을 뺀 상품 금액 중 포인트로 낼 수 있는 비율'))}
                    {number('signupPoints', t('admin.point.signupPoints', '가입 적립 포인트'), t('admin.point.signupPoints.help', '0 이면 주지 않습니다.'))}
                    {number('referralPoints', t('admin.point.referralPoints', '추천인 적립 포인트'), t('admin.point.referralPoints.help', '추천받아 가입한 회원이 첫 결제를 마치면 추천인에게 지급합니다. 0 이면 주지 않습니다.'))}
                </div>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">{t('common.saved', '저장했습니다.')}</p>}
                <div className="toolbar form-actions">
                    <button type="submit" className="primary">{t('common.save', '저장')}</button>
                </div>
            </form>
        </section>
    );
}

const couponFields = (products: { value: string; label: string }[], grades: { value: string; label: string }[], t: T): FieldSpec[] => [
    {name: 'isEnabled', label: t('admin.coupon.field.isEnabled', '사용'), type: 'checkbox', wide: true},
    {name: 'name', label: t('admin.coupon.field.name', '쿠폰 이름'), required: true, wide: true},
    {name: 'description', label: t('admin.coupon.field.description', '설명'), wide: true},
    {name: 'productId', label: t('admin.coupon.field.productId', '적용 상품'), type: 'select', wide: true, options: products,
        emptyOptionLabel: t('admin.discount.field.productId.all', '전체'), help: t('admin.discount.field.productId.help', '선택 안 함이면 모든 상품에 적용됩니다.')},
    {name: 'discountType', label: t('admin.coupon.field.discountType', '할인 방식'), type: 'select', required: true, wide: true,
        options: [{value: 'AMOUNT', label: t('admin.coupon.discountType.amount', '정액 (원)')}, {value: 'RATE', label: t('admin.coupon.discountType.rate', '정률 (%)')}]},
    {name: 'discountValue', label: t('admin.coupon.field.discountValue', '할인 금액·할인율'), type: 'number', min: 0, required: true, wide: true},
    {name: 'maxDiscountAmount', label: t('admin.coupon.field.maxDiscountAmount', '최대 할인 금액(원)'), type: 'number', min: 0, wide: true, visibleWhen: (v) => v.discountType === 'RATE'},
    {name: 'minOrderAmount', label: t('admin.coupon.field.minOrderAmount', '최소 주문 금액(원)'), type: 'number', min: 0, wide: true, help: t('admin.coupon.field.minOrderAmount.help', '적용 상품 금액 합계 기준입니다.')},
    {name: 'issueType', label: t('admin.coupon.field.issueType', '발급 방식'), type: 'select', required: true, wide: true, options: toOptions(couponIssueType(t))},
    {name: 'targetGradeId', label: t('admin.coupon.field.targetGradeId', '지급할 등급'), type: 'select', required: true, wide: true, options: grades,
        visibleWhen: (v) => v.issueType === 'GRADE', help: t('admin.coupon.field.targetGradeId.help', '이 등급이 되면 한 번 지급합니다.')},
    {name: 'issueStartAt', label: t('admin.coupon.field.issueStartAt', '발급 시작'), type: 'datetime', wide: true},
    {name: 'issueEndAt', label: t('admin.coupon.field.issueEndAt', '발급 종료'), type: 'datetime', wide: true},
    {name: 'validDays', label: t('admin.coupon.field.validDays', '받은 뒤 사용 가능 일수'), type: 'number', min: 0, wide: true, help: t('admin.coupon.field.validDays.help', '비우면 사용 종료 일시까지 쓸 수 있습니다.')},
    {name: 'validUntil', label: t('admin.coupon.field.validUntil', '사용 종료 일시'), type: 'datetime', wide: true},
    {name: 'totalQuantity', label: t('admin.coupon.field.totalQuantity', '총 발급 수량'), type: 'number', min: 0, wide: true, help: t('admin.coupon.field.totalQuantity.help', '비우면 무제한')},
];

const loadCoupons = () => couponApi.list();

function CouponIssuesPanel({coupon, onChange}: { coupon: Coupon; onChange: () => void }) {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [issues, setIssues] = useState<Page<CouponIssue> | null>(null);
    const [usernames, setUsernames] = useState('');
    const [result, setResult] = useState<string | null>(null);
    const [error, setError] = useState<unknown>(null);
    const load = useCallback(() => {
        couponApi.issues(coupon.id, page).then(setIssues).catch(setError);
    }, [coupon.id, page]);
    useEffect(load, [load]);

    const issue = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            const done = await couponApi.issue(coupon.id, usernames.split(/[\s,]+/).filter(Boolean));
            setResult(t('admin.coupon.issueResult', '{count}명에게 지급했습니다.', {count: String(done.issued)})
                + (done.skipped.length ? t('admin.coupon.issueSkipped', ' 건너뜀: {names}', {names: done.skipped.join(', ')}) : ''));
            setUsernames('');
            load();
            onChange();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="inline-form">
            <h3>{t('admin.coupon.issuesTitle', '발급 내역')}</h3>
            <form className="toolbar" onSubmit={issue}>
                <input aria-label={t('admin.coupon.issueTarget', '지급할 회원 아이디')} className="wide-input" placeholder={t('admin.coupon.issueTargetPlaceholder', '회원 아이디 (쉼표·공백으로 여러 명)')} required
                       value={usernames} onChange={(e) => setUsernames(e.target.value)}/>
                <button type="submit" className="primary">{t('admin.coupon.issueDirect', '직접 지급')}</button>
                {result && <span className="ok">{result}</span>}
            </form>
            <ErrorBanner error={error}/>
            <table className="compact">
                <thead><tr>
                    <th>{t('admin.course.member', '회원')}</th><th>{t('admin.order.column.status', '상태')}</th>
                    <th>{t('admin.coupon.validUntilColumn', '사용 기한')}</th><th>{t('admin.coupon.usedOrder', '사용 주문')}</th>
                    <th>{t('admin.coupon.receivedAt', '받은 일시')}</th>
                </tr></thead>
                <tbody>
                {issues?.content.map((row) => (
                    <tr key={row.id}>
                        <td>{row.username ?? `#${row.memberId}`}</td>
                        <td>{couponStatus(t)[row.status]}</td>
                        <td>{row.expiresAt ? formatDateTime(row.expiresAt) : t('admin.reservationItem.noLimit', '제한 없음')}</td>
                        <td>{row.orderId ?? '-'}</td>
                        <td>{formatDateTime(row.createdAt)}</td>
                    </tr>
                ))}
                {issues?.content.length === 0 && <tr><td colSpan={5} className="muted">{t('admin.coupon.noIssues', '아직 발급하지 않았습니다.')}</td></tr>}
                </tbody>
            </table>
            {issues && <Pagination page={page} totalPages={issues.page.totalPages} onChange={setPage}/>}
        </div>
    );
}

export function CouponAdminPage() {
    const {t} = useI18n();
    const grades = useGrades();
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    const [opened, setOpened] = useState<number | null>(null);
    useEffect(() => {
        adminApi.products.search({size: 200}).then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    const fields = useMemo(() => couponFields(products, grades.map((g) => ({value: String(g.id), label: g.name})), t), [products, grades, t]);
    const toBody = (values: FormValues) => ({...values});
    return (
        <AdminCrudPage<Coupon>
            title={t('admin.nav.coupons', '쿠폰')}
            description={<p>{t('admin.coupon.description', '다운로드 쿠폰은 사용자 쿠폰함에서 받고, 가입·등급 쿠폰은 조건이 되면 자동으로 지급합니다. 관리자 지급 쿠폰은 발급 내역에서 회원 아이디로 줍니다. 주문 전체를 취소하면 쓴 쿠폰을 돌려줍니다.')}</p>}
            searchable={false}
            load={loadCoupons}
            fields={fields}
            emptyForm={{discountType: 'AMOUNT', issueType: 'DOWNLOAD', isEnabled: true}}
            columns={[
                {label: t('admin.nav.coupons', '쿠폰'), render: (c) => (
                    <button type="button" className="link-button" onClick={() => setOpened(opened === c.id ? null : c.id)}>{c.name}</button>
                )},
                {label: t('admin.coupon.benefit', '혜택'), render: (c) => couponBenefit(c, t)},
                {label: t('admin.coupon.field.issueType', '발급 방식'), render: (c) => couponIssueType(t)[c.issueType]},
                {label: t('admin.coupon.issuedCount', '발급'), render: (c) => `${c.issuedCount}${c.totalQuantity ? ` / ${c.totalQuantity}` : ''}`},
                {label: t('admin.coupon.validUntilColumn', '사용 기한'), render: (c) => c.validDays ? t('admin.coupon.validDaysAfterReceipt', '받은 뒤 {days}일', {days: String(c.validDays)})
                    : c.validUntil ? t('admin.coupon.validUntilDate', '{date}까지', {date: formatDateTime(c.validUntil)}) : t('admin.reservationItem.noLimit', '제한 없음')},
                {label: t('admin.coupon.field.isEnabled', '사용'), render: (c) => <YesNo value={c.isEnabled}/>},
            ]}
            create={async (v) => {
                await couponApi.create(toBody(v));
            }}
            update={async (c, v) => {
                await couponApi.update(c.id, toBody(v));
            }}
            remove={(c) => couponApi.remove(c.id)}
            expandedRow={(c, reload) => opened === c.id && <CouponIssuesPanel coupon={c} onChange={reload}/>}
        />
    );
}
