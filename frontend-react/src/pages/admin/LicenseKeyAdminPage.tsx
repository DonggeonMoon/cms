import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {LicenseKey, ProductSummary} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function LicenseKeyAdminPage() {
    const [products, setProducts] = useState<ProductSummary[]>([]);
    const [productId, setProductId] = useState('');
    const [codesText, setCodesText] = useState('');
    const [available, setAvailable] = useState<number | null>(null);
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<LicenseKey> | null>(null);
    const [result, setResult] = useState<{ added: number; duplicate: number } | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        adminApi.products.search({size: 100}).then((p) => setProducts(p.content)).catch(setError);
    }, []);

    const reload = useCallback(() => {
        if (!productId) {
            setData(null);
            setAvailable(null);
            return;
        }
        const id = Number(productId);
        adminApi.licenseKeys.search(id, page).then(setData).catch(setError);
        adminApi.licenseKeys.availableCount(id).then(setAvailable).catch(setError);
    }, [productId, page]);
    useEffect(reload, [reload]);

    const addKeys = async (e: FormEvent) => {
        e.preventDefault();
        if (!productId) {
            return;
        }
        const codes = codesText.split('\n').map((line) => line.trim()).filter((line) => line.length > 0);
        if (codes.length === 0) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            setResult(await adminApi.licenseKeys.addBulk(Number(productId), codes));
            setCodesText('');
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>라이선스 키</h1>
            <p className="muted">상품에 키 목록을 등록해 두면, 결제가 끝날 때마다 수량만큼 안 쓴 키를 하나씩 배정합니다. 회원은 마이페이지에서 받은 키를 봅니다.</p>
            <ErrorBanner error={error}/>
            <div className="field">
                <label htmlFor="license-product">상품</label>
                <select id="license-product" value={productId} onChange={(e) => {
                    setProductId(e.target.value);
                    setPage(0);
                    setResult(null);
                }}>
                    <option value="">상품을 고르세요</option>
                    {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
            </div>
            {productId && (
                <>
                    <p>등록된 키 {data?.page.totalElements ?? 0}개 중 안 쓴 키 <strong>{available ?? 0}</strong>개</p>
                    <form onSubmit={addKeys} className="inline-form">
                        <h2>키 추가 (한 줄에 하나씩)</h2>
                        <textarea rows={6} value={codesText} onChange={(e) => setCodesText(e.target.value)}
                                  placeholder={'AAAA-BBBB-CCCC\nDDDD-EEEE-FFFF'}/>
                        <button type="submit" className="primary" disabled={busy}>추가하기</button>
                        {result && <p className="muted">{result.added}개 추가, {result.duplicate}개는 이미 있어 건너뜀</p>}
                    </form>
                    <div className="table-wrap">
                        <table>
                            <thead><tr><th>코드</th><th>상태</th><th>배정 주문</th><th>배정일</th><th>삭제</th></tr></thead>
                            <tbody>
                            {data?.content.map((key) => (
                                <tr key={key.id}>
                                    <td><code>{key.code}</code></td>
                                    <td>{key.orderId ? <span className="badge ok">배정됨</span> : <span className="badge">남음</span>}</td>
                                    <td>{key.orderId ?? '-'}</td>
                                    <td>{key.assignedAt ? formatDateTime(key.assignedAt) : '-'}</td>
                                    <td>
                                        {!key.orderId && (
                                            <button type="button" className="danger" onClick={() => {
                                                if (window.confirm('이 키를 삭제할까요?')) {
                                                    adminApi.licenseKeys.remove(key.id).then(reload).catch(setError);
                                                }
                                            }}>삭제</button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            {data?.content.length === 0 && <tr><td colSpan={5} className="muted">등록된 키가 없습니다.</td></tr>}
                            </tbody>
                        </table>
                    </div>
                    {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
                </>
            )}
        </section>
    );
}
