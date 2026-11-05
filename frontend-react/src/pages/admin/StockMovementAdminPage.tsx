import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {ProductSummary, StockMovement} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {STOCK_MOVEMENT_TYPE} from '../../components/labels';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function StockMovementAdminPage() {
    const [products, setProducts] = useState<ProductSummary[]>([]);
    const [productId, setProductId] = useState('');
    const [delta, setDelta] = useState('');
    const [note, setNote] = useState('');
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<StockMovement> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        adminApi.products.search({size: 100}).then((p) => setProducts(p.content)).catch(setError);
    }, []);

    const reload = useCallback(() => {
        if (!productId) {
            setData(null);
            return;
        }
        adminApi.stockMovements.search(Number(productId), page).then(setData).catch(setError);
    }, [productId, page]);
    useEffect(reload, [reload]);

    const adjust = async (e: FormEvent) => {
        e.preventDefault();
        if (!productId || !delta || !note.trim()) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            await adminApi.stockMovements.adjust(Number(productId), null, Number(delta), note.trim());
            setDelta('');
            setNote('');
            setPage(0);
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>재고 이력</h1>
            <p className="muted">주문·취소·반품 때마다 자동으로 쌓이는 재고 변동 이력입니다. 실사 결과와 다르면 아래에서 직접 보정할 수 있습니다.</p>
            <ErrorBanner error={error}/>
            <div className="field">
                <label htmlFor="stock-product">상품</label>
                <select id="stock-product" value={productId} onChange={(e) => {
                    setProductId(e.target.value);
                    setPage(0);
                }}>
                    <option value="">상품을 고르세요</option>
                    {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
            </div>
            {productId && (
                <>
                    <form onSubmit={adjust} className="inline-form">
                        <h2>수동 조정</h2>
                        <input type="number" value={delta} onChange={(e) => setDelta(e.target.value)}
                               placeholder="증감 수량 (입고는 +, 출고는 -)"/>
                        <input type="text" value={note} onChange={(e) => setNote(e.target.value)} placeholder="조정 이유 (예: 실사 보정)"/>
                        <button type="submit" className="primary" disabled={busy}>조정하기</button>
                    </form>
                    <div className="table-wrap">
                        <table>
                            <thead><tr><th>종류</th><th>수량</th><th>조정 후 재고</th><th>관련 주문</th><th>메모</th><th>시각</th></tr></thead>
                            <tbody>
                            {data?.content.map((m) => (
                                <tr key={m.id}>
                                    <td>{STOCK_MOVEMENT_TYPE[m.type]}</td>
                                    <td>{m.type === 'IN' ? '+' : '-'}{m.quantity}</td>
                                    <td>{m.quantityAfter ?? '무제한'}</td>
                                    <td>{m.orderId ?? '-'}</td>
                                    <td>{m.note ?? '-'}</td>
                                    <td>{formatDateTime(m.createdAt)}</td>
                                </tr>
                            ))}
                            {data?.content.length === 0 && <tr><td colSpan={6} className="muted">재고 변동 이력이 없습니다.</td></tr>}
                            </tbody>
                        </table>
                    </div>
                    {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
                </>
            )}
        </section>
    );
}
