import {type FormEvent, useEffect, useState} from 'react';
import type {TaxSetting} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';

export function TaxSettingPage() {
    const [setting, setSetting] = useState<TaxSetting | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        shopApi.taxSetting().then(setSetting).catch(setError);
    }, []);

    if (!setting) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            setSetting(await adminApi.taxSetting.save({...setting}));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>세금</h1>
            <p className="muted">세금계산서·현금영수증은 다루지 않습니다. 여기서는 영수증·결제 화면에 보여줄 부가세 안내와
                해외 배송 주문에 띄울 관세 안내 문구만 설정합니다. 상품가·결제 금액 자체는 바뀌지 않습니다.</p>
            <form onSubmit={submit}>
                <div className="schema-grid">
                    <div className="field">
                        <label htmlFor="vatRate">부가세율(%)</label>
                        <input id="vatRate" type="number" min={0} step="0.01" required value={setting.vatRate}
                               onChange={(e) => setSetting({...setting, vatRate: Number(e.target.value)})}/>
                    </div>
                    <div className="field">
                        <label htmlFor="priceIncludesVat">상품가에 부가세 포함</label>
                        <select id="priceIncludesVat" value={setting.priceIncludesVat ? 'Y' : 'N'}
                                onChange={(e) => setSetting({...setting, priceIncludesVat: e.target.value === 'Y'})}>
                            <option value="Y">포함가 (부가세 역산해서 표시)</option>
                            <option value="N">별도가 (부가세 더해서 표시)</option>
                        </select>
                    </div>
                </div>
                <div className="field">
                    <label htmlFor="customsNotice">해외 배송 관세 안내 문구</label>
                    <textarea id="customsNotice" rows={3} value={setting.customsNotice ?? ''}
                              placeholder="예: 관세·수입 부가세는 통관 시 구매자가 부담할 수 있습니다."
                              onChange={(e) => setSetting({...setting, customsNotice: e.target.value})}/>
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
