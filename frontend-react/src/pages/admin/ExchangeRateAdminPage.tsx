import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {ExchangeRate} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {useI18n} from '../../api/i18n';

export function ExchangeRateAdminPage() {
    const {t} = useI18n();
    const [rates, setRates] = useState<ExchangeRate[]>([]);
    const [currencyCode, setCurrencyCode] = useState('');
    const [rate, setRate] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    const reload = useCallback(() => {
        adminApi.exchangeRates.search().then(setRates).catch(setError);
    }, []);
    useEffect(reload, [reload]);

    const save = async (e: FormEvent) => {
        e.preventDefault();
        if (!currencyCode.trim() || !rate) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            await adminApi.exchangeRates.save(currencyCode.trim().toUpperCase(), Number(rate), true);
            setCurrencyCode('');
            setRate('');
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    const toggle = async (item: ExchangeRate) => {
        await adminApi.exchangeRates.save(item.currencyCode, item.rate, !item.enabled);
        reload();
    };

    return (
        <section className="card">
            <h1>{t('admin.exchangeRate.title', '환율(다통화 표시)')}</h1>
            <p className="muted">{t('admin.exchangeRate.description', '결제는 항상 원화로 이뤄집니다. 여기 등록한 환율은 상품가를 안내용으로 환산해서 보여줄 때만 씁니다.')}</p>
            <form onSubmit={save} className="inline-form toolbar">
                <input type="text" value={currencyCode} onChange={(e) => setCurrencyCode(e.target.value)} placeholder={t('admin.exchangeRate.currencyCode', '통화 코드 (예: USD)')}/>
                <input type="number" step="0.01" value={rate} onChange={(e) => setRate(e.target.value)} placeholder={t('admin.exchangeRate.ratePerUnit', '1단위당 원화')}/>
                <button type="submit" className="primary" disabled={busy}>{t('admin.exchangeRate.register', '등록·수정')}</button>
            </form>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.exchangeRate.currency', '통화')}</th><th>{t('admin.exchangeRate.ratePerUnit', '1단위당 원화')}</th>
                        <th>{t('admin.exchangeRate.displayEnabled', '표시 사용')}</th><th>{t('common.delete', '삭제')}</th>
                    </tr></thead>
                    <tbody>
                    {rates.map((item) => (
                        <tr key={item.id}>
                            <td>{item.currencyCode}</td>
                            <td>{item.rate}</td>
                            <td>
                                <button type="button" onClick={() => toggle(item)}>
                                    {item.enabled ? <span className="badge ok">{t('admin.board.field.isUsing', '사용')}</span> : <span className="badge">{t('admin.exchangeRate.stopped', '중지')}</span>}
                                </button>
                            </td>
                            <td>
                                <button type="button" className="danger" onClick={() => {
                                    if (window.confirm(t('admin.exchangeRate.confirmDelete', '이 환율을 삭제할까요?'))) {
                                        adminApi.exchangeRates.remove(item.id).then(reload).catch(setError);
                                    }
                                }}>{t('common.delete', '삭제')}</button>
                            </td>
                        </tr>
                    ))}
                    {rates.length === 0 && <tr><td colSpan={4} className="muted">{t('admin.exchangeRate.none', '등록된 환율이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
