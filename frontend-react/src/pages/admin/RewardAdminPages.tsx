import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {adminApi} from '../../api/endpoints';
import {useGrades} from '../../api/grades';
import {type Coupon, COUPON_ISSUE_TYPE, COUPON_STATUS, couponApi, type CouponIssue, couponBenefit, type PointPolicy, pointApi} from '../../api/rewards';
import type {Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

export function PointPolicyPage() {
    const [policy, setPolicy] = useState<PointPolicy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);
    useEffect(() => {
        pointApi.policy().then(setPolicy).catch(setError);
    }, []);
    if (!policy) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
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
            <h1>포인트</h1>
            <p className="muted">결제가 끝나면 쿠폰·포인트를 뺀 상품 금액(배송비 제외)에 적립률을 곱해 적립합니다.
                주문을 취소하면 쓴 포인트는 돌려주고 적립한 포인트는 거둡니다. 회원별 잔액 조정은 회원 상세에서 합니다.</p>
            <form onSubmit={submit}>
                <label className="checkbox">
                    <input type="checkbox" checked={policy.isEnabled} onChange={(e) => setPolicy({...policy, isEnabled: e.target.checked})}/>
                    포인트 사용
                </label>
                <div className="schema-grid">
                    {number('earnRate', '적립률(%)', '예: 1 이면 1만 원 결제 시 100포인트', 0.1)}
                    {number('expireDays', '유효 기간(일)', '적립일부터 이 기간이 지나면 소멸합니다.')}
                    {number('minUsePoints', '최소 사용 포인트', '0 이면 제한 없음')}
                    {number('maxUseRate', '최대 사용 비율(%)', '쿠폰을 뺀 상품 금액 중 포인트로 낼 수 있는 비율')}
                    {number('signupPoints', '가입 적립 포인트', '0 이면 주지 않습니다.')}
                </div>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">저장했습니다.</p>}
                <div className="toolbar form-actions">
                    <button type="submit" className="primary">저장</button>
                </div>
            </form>
        </section>
    );
}

const couponFields = (products: { value: string; label: string }[], grades: { value: string; label: string }[]): FieldSpec[] => [
    {name: 'name', label: '쿠폰 이름', required: true, wide: true},
    {name: 'description', label: '설명', wide: true},
    {name: 'discountType', label: '할인 방식', type: 'select', required: true, wide: true,
        options: [{value: 'AMOUNT', label: '정액 (원)'}, {value: 'RATE', label: '정률 (%)'}]},
    {name: 'discountValue', label: '할인 금액·할인율', type: 'number', required: true, wide: true},
    {name: 'maxDiscountAmount', label: '최대 할인 금액(원)', type: 'number', wide: true, visibleWhen: (v) => v.discountType === 'RATE'},
    {name: 'minOrderAmount', label: '최소 주문 금액(원)', type: 'number', wide: true, help: '적용 상품 금액 합계 기준입니다.'},
    {name: 'productId', label: '적용 상품', type: 'select', wide: true, options: products, help: '선택 안 함이면 모든 상품에 쓸 수 있습니다.'},
    {name: 'issueType', label: '발급 방식', type: 'select', required: true, wide: true, options: toOptions(COUPON_ISSUE_TYPE)},
    {name: 'targetGradeId', label: '지급할 등급', type: 'select', required: true, wide: true, options: grades,
        visibleWhen: (v) => v.issueType === 'GRADE', help: '이 등급이 되면 한 번 지급합니다.'},
    {name: 'issueStartAt', label: '발급 시작', type: 'datetime', wide: true},
    {name: 'issueEndAt', label: '발급 종료', type: 'datetime', wide: true},
    {name: 'validDays', label: '받은 뒤 사용 가능 일수', type: 'number', wide: true, help: '비우면 사용 종료 일시까지 쓸 수 있습니다.'},
    {name: 'validUntil', label: '사용 종료 일시', type: 'datetime', wide: true},
    {name: 'totalQuantity', label: '총 발급 수량', type: 'number', wide: true, help: '비우면 무제한'},
    {name: 'isEnabled', label: '사용', type: 'checkbox', wide: true},
];

const loadCoupons = () => couponApi.list();

function CouponIssuesPanel({coupon, onChange}: { coupon: Coupon; onChange: () => void }) {
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
            setResult(`${done.issued}명에게 지급했습니다.${done.skipped.length ? ` 건너뜀: ${done.skipped.join(', ')}` : ''}`);
            setUsernames('');
            load();
            onChange();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="inline-form">
            <h3>발급 내역</h3>
            <form className="toolbar" onSubmit={issue}>
                <input aria-label="지급할 회원 아이디" className="wide-input" placeholder="회원 아이디 (쉼표·공백으로 여러 명)" required
                       value={usernames} onChange={(e) => setUsernames(e.target.value)}/>
                <button type="submit" className="primary">직접 지급</button>
                {result && <span className="ok">{result}</span>}
            </form>
            <ErrorBanner error={error}/>
            <table className="compact">
                <thead><tr><th>회원</th><th>상태</th><th>사용 기한</th><th>사용 주문</th><th>받은 일시</th></tr></thead>
                <tbody>
                {issues?.content.map((row) => (
                    <tr key={row.id}>
                        <td>{row.username ?? `#${row.memberId}`}</td>
                        <td>{COUPON_STATUS[row.status]}</td>
                        <td>{row.expiresAt ? formatDateTime(row.expiresAt) : '제한 없음'}</td>
                        <td>{row.orderId ?? '-'}</td>
                        <td>{formatDateTime(row.createdAt)}</td>
                    </tr>
                ))}
                {issues?.content.length === 0 && <tr><td colSpan={5} className="muted">아직 발급하지 않았습니다.</td></tr>}
                </tbody>
            </table>
            {issues && <Pagination page={page} totalPages={issues.page.totalPages} onChange={setPage}/>}
        </div>
    );
}

export function CouponAdminPage() {
    const grades = useGrades();
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    const [opened, setOpened] = useState<number | null>(null);
    useEffect(() => {
        adminApi.products.search({size: 200}).then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    const fields = useMemo(() => couponFields(products, grades.map((g) => ({value: String(g.id), label: g.name}))), [products, grades]);
    const toBody = (values: FormValues) => ({...values});
    return (
        <AdminCrudPage<Coupon>
            title="쿠폰"
            description={<p>다운로드 쿠폰은 사용자 쿠폰함에서 받고, 가입·등급 쿠폰은 조건이 되면 자동으로 지급합니다.
                관리자 지급 쿠폰은 발급 내역에서 회원 아이디로 줍니다. 주문 전체를 취소하면 쓴 쿠폰을 돌려줍니다.</p>}
            searchable={false}
            load={loadCoupons}
            fields={fields}
            emptyForm={{discountType: 'AMOUNT', issueType: 'DOWNLOAD', isEnabled: true}}
            columns={[
                {label: '쿠폰', render: (c) => (
                    <button type="button" className="link-button" onClick={() => setOpened(opened === c.id ? null : c.id)}>{c.name}</button>
                )},
                {label: '혜택', render: (c) => couponBenefit(c)},
                {label: '발급 방식', render: (c) => COUPON_ISSUE_TYPE[c.issueType]},
                {label: '발급', render: (c) => `${c.issuedCount}${c.totalQuantity ? ` / ${c.totalQuantity}` : ''}`},
                {label: '사용 기한', render: (c) => c.validDays ? `받은 뒤 ${c.validDays}일` : c.validUntil ? `${formatDateTime(c.validUntil)}까지` : '제한 없음'},
                {label: '사용', render: (c) => <YesNo value={c.isEnabled}/>},
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
