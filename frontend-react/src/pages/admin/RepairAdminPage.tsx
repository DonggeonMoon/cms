import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {RepairRequest} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import type {Query} from '../../api/client';
import {ProgressAndMessages, ProgressTemplateManager} from '../../components/ProgressPanel';
import {useI18n} from '../../api/i18n';

const loadRepairs = (query: Query) => adminApi.repairs.search(query);

export function RepairAdminPage() {
    const {t} = useI18n();
    return (
        <AdminCrudPage<RepairRequest>
            title={t('shop.repair.title', '수리·AS 접수')}
            description={<>
                <p>{t('admin.repair.description', '회원이 보낸 수리·AS 요청입니다. 진행 단계로 접수→진단→완료를 알리고, 메시지로 증상을 더 물어본 뒤 견적 주문을 만들어 보냅니다.')}</p>
                <ProgressTemplateManager targetType="REPAIR"/>
            </>}
            searchable
            load={loadRepairs}
            columns={[
                {label: 'ID', render: (r) => r.id},
                {label: t('admin.repair.memberId', '회원 ID'), render: (r) => r.memberId},
                {label: t('admin.board.column.title', '제목'), render: (r) => r.title},
                {label: t('admin.repair.photos', '사진'), render: (r) => r.photoUrls.length > 0 ? t('admin.repair.photoCount', '{count}장', {count: String(r.photoUrls.length)}) : '-'},
                {label: t('admin.repair.quoteOrder', '견적 주문'), render: (r) => r.quoteOrderId ? <Link to={`/mypage/orders`}>{t('admin.repair.orderNumber', '주문 #{id}', {id: String(r.quoteOrderId)})}</Link> : '-'},
                {label: t('admin.application.appliedAt', '접수일'), render: (r) => formatDateTime(r.createdAt)},
            ]}
            expandedRow={(r) => <RepairDetail repair={r}/>}
        />
    );
}

function RepairDetail({repair}: { repair: RepairRequest }) {
    const {t} = useI18n();
    const [productId, setProductId] = useState('');
    const [quoteProducts, setQuoteProducts] = useState<{ value: string; label: string }[]>([]);
    const [quoteOrderId, setQuoteOrderId] = useState<number | null>(repair.quoteOrderId);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.products.search({size: 100})
            .then((page) => setQuoteProducts(page.content.filter((p) => p.saleType === 'QUOTE')
                .map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setQuoteProducts([]));
    }, []);

    const sendQuoteOrder = async () => {
        if (!productId) {
            return;
        }
        setError(null);
        try {
            const order = await adminApi.repairs.createQuoteOrder(repair.id, Number(productId));
            setQuoteOrderId(order.id);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="inline-form">
            <h3>{repair.title}</h3>
            <p className="pre-wrap">{repair.description}</p>
            {repair.photoUrls.length > 0 && (
                <div className="work-photos">
                    {repair.photoUrls.map((url, i) => <a key={url} href={url} target="_blank" rel="noreferrer">{t('admin.repair.photoOf', '사진 {index}', {index: String(i + 1)})}</a>)}
                </div>
            )}
            {quoteOrderId ? (
                <p className="muted small">{t('admin.repair.quoteOrderCreated', '견적 주문 #{id}을 만들었습니다. 주문 관리에서 견적 금액을 회신하세요.', {id: String(quoteOrderId)})}</p>
            ) : (
                <div className="toolbar">
                    <select aria-label={t('admin.repair.quoteProduct', '견적 상품')} value={productId} onChange={(e) => setProductId(e.target.value)}>
                        <option value="">{t('admin.repair.selectQuoteProduct', '견적(QUOTE) 상품 선택')}</option>
                        {quoteProducts.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
                    </select>
                    <button type="button" disabled={!productId} onClick={sendQuoteOrder}>{t('admin.repair.createQuoteOrder', '견적 주문 만들기')}</button>
                    {quoteProducts.length === 0 && <span className="muted small">{t('admin.repair.noQuoteProducts', '견적(QUOTE) 판매 방식 상품을 먼저 만드세요.')}</span>}
                </div>
            )}
            <ErrorBanner error={error}/>
            <ProgressAndMessages
                getProgress={() => adminApi.progress.get('REPAIR', repair.id)}
                advance={(stageCode, memo) => adminApi.progress.advance('REPAIR', repair.id, {stageCode, memo})}
                listMessages={() => adminApi.messages.list('REPAIR', repair.id)}
                postMessage={(content, fileIds) => adminApi.messages.post('REPAIR', repair.id, {content, fileIds})}
            />
        </div>
    );
}
