import {type FormEvent, useEffect, useState} from 'react';
import type {CountryFee, RegionFee, ShippingPolicy} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';

const JEJU: RegionFee = {name: '제주', postalCodeFrom: '63000', postalCodeTo: '63644', fee: 3000};

export function ShippingPolicyPage() {
    const [policy, setPolicy] = useState<ShippingPolicy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        shopApi.shippingPolicy().then(setPolicy).catch(setError);
    }, []);

    if (!policy) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    const setRegion = (index: number, patch: Partial<RegionFee>) =>
        setPolicy({...policy, regionFees: policy.regionFees.map((r, i) => (i === index ? {...r, ...patch} : r))});
    const setCountry = (index: number, patch: Partial<CountryFee>) =>
        setPolicy({...policy, internationalFees: policy.internationalFees.map((c, i) => (i === index ? {...c, ...patch} : c))});

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            setPolicy(await adminApi.shippingPolicy.save({...policy}));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>배송비</h1>
            <p className="muted">배송 상품이 있는 주문에 배송비를 받습니다. 무료 배송 기준은 기본 배송비에만 적용되고,
                지역 추가 배송비(제주·도서산간 등)는 우편번호 범위에 따라 늘 더해집니다.</p>
            <form onSubmit={submit}>
                <div className="schema-grid">
                    <div className="field">
                        <label htmlFor="baseFee">기본 배송비(원)</label>
                        <input id="baseFee" type="number" min={0} required value={policy.baseFee}
                               onChange={(e) => setPolicy({...policy, baseFee: Number(e.target.value)})}/>
                    </div>
                    <div className="field">
                        <label htmlFor="freeThreshold">무료 배송 기준(원)</label>
                        <input id="freeThreshold" type="number" min={0} placeholder="없음" value={policy.freeThreshold ?? ''}
                               onChange={(e) => setPolicy({...policy, freeThreshold: e.target.value === '' ? null : Number(e.target.value)})}/>
                    </div>
                    <div className="field">
                        <label htmlFor="defaultCourier">기본 택배사</label>
                        <input id="defaultCourier" value={policy.defaultCourier ?? ''} placeholder="송장 입력 때 미리 채웁니다"
                               onChange={(e) => setPolicy({...policy, defaultCourier: e.target.value})}/>
                    </div>
                </div>
                <h2>지역 추가 배송비</h2>
                {policy.regionFees.map((region, index) => (
                    <div key={index} className="form-field-row">
                        <input aria-label={`${index + 1}번 지역 이름`} placeholder="지역 이름" required value={region.name}
                               onChange={(e) => setRegion(index, {name: e.target.value})}/>
                        <input aria-label={`${index + 1}번 시작 우편번호`} placeholder="시작 우편번호" required pattern="\d{5}"
                               value={region.postalCodeFrom} onChange={(e) => setRegion(index, {postalCodeFrom: e.target.value})}/>
                        <input aria-label={`${index + 1}번 끝 우편번호`} placeholder="끝 우편번호" required pattern="\d{5}"
                               value={region.postalCodeTo} onChange={(e) => setRegion(index, {postalCodeTo: e.target.value})}/>
                        <input aria-label={`${index + 1}번 추가 배송비`} type="number" min={0} required value={region.fee}
                               onChange={(e) => setRegion(index, {fee: Number(e.target.value)})}/>
                        <button type="button" className="danger"
                                onClick={() => setPolicy({...policy, regionFees: policy.regionFees.filter((_, i) => i !== index)})}>삭제</button>
                    </div>
                ))}
                <div className="toolbar">
                    <button type="button" onClick={() => setPolicy({...policy, regionFees: [...policy.regionFees,
                            policy.regionFees.length === 0 ? JEJU : {name: '', postalCodeFrom: '', postalCodeTo: '', fee: 0}]})}>
                        지역 추가
                    </button>
                </div>
                <h2>해외 배송</h2>
                <p className="muted">여기 등록된 나라로만 해외 배송 주문을 받을 수 있습니다. 나라 코드는 체크아웃 화면에서 구매자가 입력하는 값과 같아야 합니다(예: US, JP).</p>
                {policy.internationalFees.map((country, index) => (
                    <div key={index} className="form-field-row">
                        <input aria-label={`${index + 1}번 나라 코드`} placeholder="나라 코드 (예: US)" required value={country.country}
                               onChange={(e) => setCountry(index, {country: e.target.value.toUpperCase()})}/>
                        <input aria-label={`${index + 1}번 해외 배송비`} type="number" min={0} required value={country.fee}
                               onChange={(e) => setCountry(index, {fee: Number(e.target.value)})}/>
                        <button type="button" className="danger"
                                onClick={() => setPolicy({...policy, internationalFees: policy.internationalFees.filter((_, i) => i !== index)})}>삭제</button>
                    </div>
                ))}
                <div className="toolbar">
                    <button type="button" onClick={() => setPolicy({...policy,
                            internationalFees: [...policy.internationalFees, {country: '', fee: 0}]})}>
                        나라 추가
                    </button>
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
