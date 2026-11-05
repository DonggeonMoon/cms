import {useCallback, useEffect, useState} from 'react';
import type {Page} from '../../api/types';
import type {WishlistEntry} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Pagination} from '../../components/Pagination';
import {ProductCard} from './ShopPages';

export function MyWishlistPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<WishlistEntry> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        shopApi.wishlist(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h1>찜한 상품</h1>
            <ErrorBanner error={error}/>
            <div className="product-grid">
                {data?.content.map((entry) => (
                    <div key={entry.productId} className="wish-item">
                        <ProductCard product={entry.product}/>
                        <button type="button" onClick={() => shopApi.unwish(entry.productId).then(reload).catch(setError)}>찜 해제</button>
                    </div>
                ))}
            </div>
            {data?.content.length === 0 && <p className="muted">찜한 상품이 없습니다. 상품 화면에서 ♡ 찜하기를 누르면 여기에 모입니다.</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
