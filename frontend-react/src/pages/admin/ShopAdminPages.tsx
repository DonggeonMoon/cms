import {fileApi} from '../../api/files';
import {attributesToForm, categorySelectField, productCategoryFields, splitCategoryValues, useCategories} from '../../api/categories';
import type {FileInfo, Page} from '../../api/types';
import {type FormEvent, useCallback, useEffect, useMemo, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import {useI18n} from '../../api/i18n';
import type {Discount, MemberPass, Order, OrderReturn, OrderReturnStatus, OrderStatus, Payment, ProductSummary, ReservationItem, ReservationItemSession, Reservation, ReservationType, Review, ReviewPolicy, ReviewTargetType, StockMovement, Subscription} from '../../api/domain';
import {adminApi, shopApi} from '../../api/endpoints';
import {statusApi, useStatusLabels} from '../../api/statuses';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {ProgressAndMessages, ProgressTemplateManager} from '../../components/ProgressPanel';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDate, formatDateTime} from '../../components/format';
import {priceLabel} from '../../components/price';
import {reservationUsage} from '../../components/reservationUsage';
import {RegexBindingSelect} from '../../components/RegexBindingSelect';
import {CustomStatusSelect, StatusManager} from '../../components/StatusManager';
import {billingCycle, orderStatus as orderStatusLabels, passUnit, reservationStatus as reservationStatusLabels, reservationType, reservationTypeGroups, returnStatus, returnType, saleType, stockMovementType, toOptions} from '../../components/labels';
import {HtmlView} from '../../components/HtmlView';
import {EMPTY_LAYOUT} from '../../components/LayoutEditor';
import {Pagination} from '../../components/Pagination';
import {type FieldSpec, type FormValues, SchemaForm} from '../../components/SchemaForm';
import {recordContentFields, toRecordBody} from './CustomerRecordAdminPage';
import {type Grade, gradeSelectField, useGrades, useWithGradeFields} from '../../api/grades';
import type {GradePrice} from '../../api/domain';
import {EMPTY_OPTION_SET, type OptionSet, optionSetBody, toOptionSet} from '../../components/ProductOptionsEditor';
import {OrderAmount, OrderItemsTable} from '../shop/OrderItems';
import {LicenseKeyAdminPage} from './LicenseKeyAdminPage';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const productFields = (t: T): FieldSpec[] => [
    {name: 'name', label: t('admin.product.field.name', '상품명'), required: true, wide: true},
    {name: 'description', label: t('admin.product.field.description', '요약 설명'), wide: true},
    {name: 'saleType', label: t('admin.product.field.saleType', '판매 방식'), type: 'select', required: true, wide: true, options: toOptions(saleType(t)),
        help: t('admin.product.field.saleType.help', '구매: 한 번 결제 / 구독: 주기마다 결제 / 후원: 금액을 정해 후원 / 견적 문의: 요청을 받고 관리자가 금액을 회신 / 이용권: 예약에서 결제 대신 차감 / 상품권: 결제하면 가격만큼 지갑에 충전 / 예약 판매: 판매 기간에만 주문받고 정해둔 날짜에 출고 / 펀딩: 마감일까지 목표 금액을 못 채우면 전부 환불 / 문의만: 결제 없이 상담 예약으로만 연결(매물 게시 등)')},
    {name: 'preorderSaleStartAt', label: t('admin.product.field.preorderSaleStartAt', '판매 시작'), type: 'datetime', wide: true,
        help: t('admin.product.field.preorderSaleStartAt.help', '비워두면 지금부터 바로 받습니다.'), visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'preorderSaleEndAt', label: t('admin.product.field.preorderSaleEndAt', '판매 종료'), type: 'datetime', wide: true,
        help: t('admin.product.field.preorderSaleEndAt.help', '비워두면 계속 받습니다.'), visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'preorderShipDate', label: t('admin.product.field.preorderShipDate', '출고 예정일'), type: 'date', required: true, wide: true,
        help: t('admin.product.field.preorderShipDate.help', '주문한 회원에게 이 날짜를 안내합니다.'), visibleWhen: (v) => v.saleType === 'PREORDER'},
    {name: 'billingCycle', label: t('admin.product.field.billingCycle', '결제 주기'), type: 'select', required: true, wide: true, options: toOptions(billingCycle(t)),
        visibleWhen: (v) => v.saleType === 'SUBSCRIPTION'},
    {name: 'commitmentMonths', label: t('admin.product.field.commitmentMonths', '약정 기간(개월)'), type: 'number', min: 0, wide: true,
        help: t('admin.product.field.commitmentMonths.help', '비워두면 약정 없이 언제든 해지할 수 있습니다.'), visibleWhen: (v) => v.saleType === 'SUBSCRIPTION'},
    {name: 'earlyTerminationFeeRate', label: t('admin.product.field.earlyTerminationFeeRate', '약정 중 해지 위약금(%)'), type: 'number', min: 0, wide: true,
        help: t('admin.product.field.earlyTerminationFeeRate.help', '남은 약정 회차 금액의 이 비율만큼 해지할 때 바로 청구합니다.'),
        visibleWhen: (v) => v.saleType === 'SUBSCRIPTION' && Boolean(v.commitmentMonths)},
    {name: 'passUnit', label: t('admin.product.field.passUnit', '이용권 단위'), type: 'select', required: true, wide: true, options: toOptions(passUnit(t)),
        visibleWhen: (v) => v.saleType === 'PASS'},
    {name: 'passDays', label: t('admin.product.field.passDays', '사용 기간(일)'), type: 'number', min: 0, wide: true,
        help: t('admin.product.field.passDays.help', '결제일부터 셉니다.'), visibleWhen: (v) => v.saleType === 'PASS' && v.passUnit === 'PERIOD'},
    {name: 'passCount', label: t('admin.product.field.passCount', '횟수'), type: 'number', min: 0, wide: true,
        help: t('admin.product.field.passCount.help', '주문 수량만큼 곱해 발급합니다.'), visibleWhen: (v) => v.saleType === 'PASS' && v.passUnit === 'COUNT'},
    {name: 'passReservationItemId', label: t('admin.product.field.passReservationItemId', '쓸 수 있는 예약 상품'), type: 'select', required: true, wide: true,
        help: t('admin.product.field.passReservationItemId.help', '이 이용권으로 예약할 수 있는 예약 상품 하나를 고릅니다.'), visibleWhen: (v) => v.saleType === 'PASS'},
    {name: 'inquiryReservationItemId', label: t('admin.product.field.inquiryReservationItemId', '연결할 상담 예약 상품'), type: 'select', required: true, wide: true,
        help: t('admin.product.field.inquiryReservationItemId.help', '매물 등 문의만 받는 상품에서 "상담 예약하기"를 누르면 이 예약 상품의 예약 화면으로 이동합니다.'),
        visibleWhen: (v) => v.saleType === 'INQUIRY'},
    {name: 'price', label: t('admin.product.field.price', '가격(원)'), type: 'number', min: 0, required: true, wide: true,
        help: t('admin.product.field.price.help', '구독은 회차별 금액, 후원은 최소 후원 금액입니다. 견적 문의·문의만은 0으로 두면 됩니다.')},
    {name: 'donationPresetsText', label: t('admin.product.field.donationPresetsText', '추천 후원 금액'), wide: true, placeholder: '5000, 10000, 30000',
        help: t('admin.product.field.donationPresetsText.help', '쉼표로 구분합니다. 후원하는 사람이 버튼으로 고를 수 있습니다.'), visibleWhen: (v) => v.saleType === 'DONATION'},
    {name: 'goalAmount', label: t('admin.product.field.goalAmount', '목표 금액(원)'), type: 'number', min: 0, wide: true,
        help: t('admin.product.field.goalAmount.help', '후원은 비워두면 달성률을 표시하지 않습니다. 펀딩은 꼭 정해야 합니다.'),
        visibleWhen: (v) => v.saleType === 'DONATION' || v.saleType === 'FUNDING'},
    {name: 'fundingDeadline', label: t('admin.product.field.fundingDeadline', '펀딩 마감일시'), type: 'datetime', required: true, wide: true,
        help: t('admin.product.field.fundingDeadline.help', '마감일까지 목표 금액을 못 채우면 결제된 주문을 모두 취소하고 환불합니다.'),
        visibleWhen: (v) => v.saleType === 'FUNDING'},
    {name: 'availableQuantity', label: t('admin.product.field.availableQuantity', '재고'), type: 'number', min: 0,
        help: t('admin.product.field.availableQuantity.help', '상품 전체 재고입니다. 비워두면 무제한이고, 옵션 조합별 재고는 아래에서 따로 정합니다.'), wide: true},
    {name: 'requiresShipping', label: t('admin.product.field.requiresShipping', '배송 상품'), type: 'checkbox', wide: true,
        help: t('admin.product.field.requiresShipping.help', '디지털·서비스 상품은 끄세요. 배송 상품이 있는 주문에만 배송비를 받습니다.')},
    {name: 'isEnabled', label: t('admin.product.field.isEnabled', '판매 중'), type: 'checkbox', wide: true},
    {name: 'digitalFiles', label: t('admin.product.field.digitalFiles', '디지털 파일'), type: 'files',
        help: t('admin.product.field.digitalFiles.help', '결제한 회원이 마이페이지에서 내려받습니다. 여기에 넣은 파일은 공개 주소로 내려받을 수 없게 보호됩니다.')},
    {name: 'downloadLimit', label: t('admin.product.field.downloadLimit', '내려받기 횟수'), type: 'number', min: 1,
        help: t('admin.product.field.downloadLimit.help', '구매 한 건당 횟수입니다. 비워두면 무제한입니다.'),
        visibleWhen: (v) => ((v.digitalFiles as FileInfo[] | undefined) ?? []).length > 0},
    {name: 'downloadDays', label: t('admin.product.field.downloadDays', '내려받기 기간(일)'), type: 'number', min: 1,
        help: t('admin.product.field.downloadDays.help', '결제일부터 셉니다. 비워두면 무기한입니다.'),
        visibleWhen: (v) => ((v.digitalFiles as FileInfo[] | undefined) ?? []).length > 0},
    {name: 'imageFileId', label: t('admin.product.field.imageFileId', '대표 이미지'), type: 'image', wide: true},
    {name: 'optionSet', label: t('admin.product.field.optionSet', '옵션'), type: 'productOptions',
        help: t('admin.product.field.optionSet.help', '옵션 그룹(색상, 사이즈 등)을 정하면 모든 조합이 만들어지고 조합마다 추가 금액·재고·판매 여부를 정할 수 있습니다. 그룹 없이 단일 옵션만 둘 수도 있습니다.')},
    {name: 'contentHtml', label: t('admin.product.field.contentHtml', '상세 설명'), type: 'html'},
];

const productGradeFields = (grades: Grade[], t: T): FieldSpec[] => [
    gradeSelectField('accessGradeId', t('admin.product.field.accessGradeId', '구매 가능 등급'), grades,
        t('admin.product.field.accessGradeId.help', '이 등급 이상 회원만 주문할 수 있습니다.'), t),
    {name: 'grantGradeId', label: t('admin.product.field.grantGradeId', '결제 시 부여할 등급'), type: 'select', wide: true,
        options: grades.map((grade) => ({value: String(grade.id), label: grade.name})),
        help: t('admin.product.field.grantGradeId.help', '결제가 끝나면 이 등급을 줍니다. 구독은 다음 결제일까지 유지되고 해지하면 원래 등급으로 돌아갑니다.')},
    {name: 'grantGradeDays', label: t('admin.product.field.grantGradeDays', '등급 유지 일수'), type: 'number', wide: true, min: 1,
        help: t('admin.product.field.grantGradeDays.help', '비워두면 무기한입니다.'),
        visibleWhen: (v) => Boolean(v.grantGradeId) && v.saleType !== 'SUBSCRIPTION'},
];

const toProductBody = (form: FormValues) => {
    const {rest, categoryId, attributes} = splitCategoryValues(form);
    const {donationPresetsText, optionSet, digitalFiles, pass, inquiry, preorder, funding, ...values} = rest;
    return {
    ...values,
    categoryId,
    attributes,
    digitalFileIds: ((digitalFiles as FileInfo[] | undefined) ?? []).map((file) => file.id),
    donationPresets: String(donationPresetsText ?? '').split(',').map((v) => Number(v.trim())).filter((v) => v > 0),
    ...optionSetBody(optionSet as OptionSet | undefined),
    };
};

const loadProducts = (query: Query) => adminApi.products.search(query);

function GradePriceEditor({productId, onClose}: { productId: number; onClose: () => void }) {
    const {t} = useI18n();
    const grades = useGrades();
    const [prices, setPrices] = useState<GradePrice[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    useEffect(() => {
        adminApi.products.gradePrices(productId).then(setPrices).catch(setError);
    }, [productId]);

    if (!prices) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    const setRow = (index: number, patch: Partial<GradePrice>) =>
        setPrices(prices.map((row, i) => (i === index ? {...row, ...patch} : row)));
    const save = async () => {
        setBusy(true);
        setError(null);
        try {
            setPrices(await adminApi.products.saveGradePrices(productId, prices));
        } catch (e) {
            setError(e);
        } finally {
            setBusy(false);
        }
    };
    return (
        <section className="card">
            <h2>{t('admin.product.gradePrice.title', '거래처(등급)별 가격')}</h2>
            <p className="muted">{t('admin.product.gradePrice.help', '등록한 등급의 회원은 결제 때 기본가 대신 여기 가격을 적용받습니다. 등록 안 한 등급은 기본가 그대로입니다.')}</p>
            <ErrorBanner error={error}/>
            {prices.map((row, index) => (
                <div key={index} className="form-field-row">
                    <select aria-label={t('admin.product.gradePrice.gradeAt', '{index}번 등급', {index: String(index + 1)})} value={row.gradeId}
                            onChange={(e) => setRow(index, {gradeId: Number(e.target.value)})}>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                    </select>
                    <input aria-label={t('admin.product.gradePrice.priceAt', '{index}번 가격', {index: String(index + 1)})} type="number" min={0} required value={row.price}
                           onChange={(e) => setRow(index, {price: Number(e.target.value)})}/>
                    <button type="button" className="danger" onClick={() => setPrices(prices.filter((_, i) => i !== index))}>{t('common.delete', '삭제')}</button>
                </div>
            ))}
            <div className="toolbar">
                <button type="button" onClick={() => setPrices([...prices, {gradeId: grades[0]?.id ?? 0, price: 0}])}
                        disabled={grades.length === 0}>{t('admin.product.gradePrice.add', '등급 추가')}</button>
            </div>
            <div className="toolbar form-actions">
                <button type="button" className="primary" onClick={save} disabled={busy}>{t('common.save', '저장')}</button>
                <button type="button" onClick={onClose}>{t('common.close', '닫기')}</button>
            </div>
        </section>
    );
}

function StockMovementPanel({productId, onClose}: { productId: number; onClose: () => void }) {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<StockMovement> | null>(null);
    const [delta, setDelta] = useState('');
    const [note, setNote] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    const reload = useCallback(() => {
        adminApi.stockMovements.search(productId, page).then(setData).catch(setError);
    }, [productId, page]);
    useEffect(reload, [reload]);

    const adjust = async (e: FormEvent) => {
        e.preventDefault();
        if (!delta || !note.trim()) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            await adminApi.stockMovements.adjust(productId, null, Number(delta), note.trim());
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
            <h2>{t('admin.product.stock.title', '재고 이력')}</h2>
            <p className="muted">{t('admin.product.stock.help', '주문·취소·반품 때마다 자동으로 쌓이는 재고 변동 이력입니다. 실사 결과와 다르면 아래에서 직접 보정할 수 있습니다.')}</p>
            <ErrorBanner error={error}/>
            <form onSubmit={adjust} className="inline-form">
                <div className="toolbar">
                    <input type="number" value={delta} onChange={(e) => setDelta(e.target.value)}
                           placeholder={t('admin.product.stock.deltaPlaceholder', '증감 수량 (입고는 +, 출고는 -)')}/>
                    <input type="text" value={note} onChange={(e) => setNote(e.target.value)}
                           placeholder={t('admin.product.stock.notePlaceholder', '조정 이유 (예: 실사 보정)')}/>
                    <button type="submit" className="primary" disabled={busy}>{t('admin.product.stock.adjust', '조정하기')}</button>
                </div>
            </form>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.product.stock.type', '종류')}</th><th>{t('admin.product.stock.quantity', '수량')}</th>
                        <th>{t('admin.product.stock.after', '조정 후 재고')}</th><th>{t('admin.product.stock.order', '관련 주문')}</th>
                        <th>{t('admin.product.stock.note', '메모')}</th><th>{t('admin.product.stock.time', '시각')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((m) => (
                        <tr key={m.id}>
                            <td>{stockMovementType(t)[m.type]}</td>
                            <td>{m.type === 'IN' ? '+' : '-'}{m.quantity}</td>
                            <td>{m.quantityAfter ?? t('admin.product.stock.unlimited', '무제한')}</td>
                            <td>{m.orderId ?? '-'}</td>
                            <td>{m.note ?? '-'}</td>
                            <td>{formatDateTime(m.createdAt)}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('admin.product.stock.noData', '재고 변동 이력이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
            <div className="toolbar form-actions">
                <button type="button" onClick={onClose}>{t('common.close', '닫기')}</button>
            </div>
        </section>
    );
}

export function ProductAdminPage() {
    const {t} = useI18n();
    const categories = useCategories();
    const [reservationItemChoices, setReservationItemChoices] = useState<{ value: string; label: string }[]>([]);
    const [fundingNotice, setFundingNotice] = useState<string | null>(null);
    const [gradePricing, setGradePricing] = useState<number | null>(null);
    const [stockHistoryFor, setStockHistoryFor] = useState<number | null>(null);
    useEffect(() => {
        adminApi.reservationItems.search({size: 100}).then((page) => setReservationItemChoices(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setReservationItemChoices([]));
    }, []);
    const base = useMemo(() => {
        let fields = productFields(t).map((field) => field.name === 'passReservationItemId' || field.name === 'inquiryReservationItemId'
            ? {...field, options: reservationItemChoices} : field);
        const at = fields.findIndex((field) => field.name === 'requiresShipping');
        fields = [...fields.slice(0, at), ...productCategoryFields(categories, t), ...fields.slice(at)];
        return fields;
    }, [categories, reservationItemChoices, t]);
    const fields = useWithGradeFields(base, (grades) => productGradeFields(grades, t));
    return (
        <AdminCrudPage<ProductSummary>
            title={t('admin.product.title', '상품')}
            description={<div className="toolbar">
                <button type="button" onClick={async () => {
                    const result = await adminApi.products.resolveFunding();
                    setFundingNotice(t('admin.product.fundingResolved', '마감된 펀딩 중 목표를 못 채운 {count}건을 환불 처리했습니다.', {count: String(result.failed)}));
                }}>{t('admin.product.resolveFunding', '펀딩 마감 처리')}</button>
                {fundingNotice && <span className="ok">{fundingNotice}</span>}
            </div>}
            load={loadProducts}
            fields={fields}
            emptyForm={{isEnabled: true, requiresShipping: true, saleType: 'PURCHASE', price: 0, contentHtml: '', optionSet: EMPTY_OPTION_SET}}
            columns={[
                {label: t('admin.product.column.image', '이미지'), render: (p) => p.imageUrl ? <img className="thumb" src={p.imageUrl} alt={p.name}/> : '-'},
                {label: 'ID', render: (p) => p.id},
                {label: t('admin.product.column.name', '상품명'), render: (p) => <Link to={`/products/${p.id}`}>{p.name}</Link>},
                {label: t('admin.product.column.saleType', '판매 방식'), render: (p) => <span className="badge">{saleType(t)[p.saleType]}</span>},
                {label: t('admin.product.column.price', '가격'), render: (p) => priceLabel(p, t)},
                {label: t('admin.product.column.stock', '재고'), render: (p) => (
                    <button type="button" onClick={() => {
                        setGradePricing(null);
                        setStockHistoryFor(stockHistoryFor === p.id ? null : p.id);
                    }}>{p.availableQuantity ?? t('admin.product.stock.unlimited', '무제한')}</button>
                )},
                {label: t('admin.product.column.isEnabled', '판매 중'), render: (p) => <YesNo value={p.isEnabled}/>},
                {label: t('admin.product.column.gradePrice', '거래처 가격'), render: (p) => (
                    <button type="button" onClick={() => {
                        setStockHistoryFor(null);
                        setGradePricing(gradePricing === p.id ? null : p.id);
                    }}>{t('admin.product.gradePrice.configure', '설정')}</button>
                )},
            ]}
            toForm={(p) => adminApi.products.get(p.id).then((full) => ({
                ...full,
                optionSet: toOptionSet(full.optionGroups ?? [], full.options),
                donationPresetsText: full.donationPresets.join(', '),
                digitalFiles: full.digitalFiles ?? [],
                categoryId: full.categoryId == null ? '' : String(full.categoryId),
                passUnit: full.pass?.unit ?? '',
                passDays: full.pass?.days ?? '',
                passCount: full.pass?.count ?? '',
                passReservationItemId: full.pass?.reservationItemId == null ? '' : String(full.pass.reservationItemId),
                inquiryReservationItemId: full.inquiry?.reservationItemId == null ? '' : String(full.inquiry.reservationItemId),
                preorderSaleStartAt: full.preorder?.saleStartAt ?? '',
                preorderSaleEndAt: full.preorder?.saleEndAt ?? '',
                preorderShipDate: full.preorder?.shipDate ?? '',
                fundingDeadline: full.funding?.deadline ?? '',
                ...attributesToForm(full.categoryId, full.attributes),
            }))}
            create={(v) => adminApi.products.create(toProductBody(v))}
            update={(p, v) => adminApi.products.update(p.id, toProductBody(v))}
            remove={(p) => adminApi.products.remove(p.id)}
            expandedRow={(p) => {
                if (gradePricing === p.id) {
                    return <GradePriceEditor productId={p.id} onClose={() => setGradePricing(null)}/>;
                }
                if (stockHistoryFor === p.id) {
                    return <StockMovementPanel productId={p.id} onClose={() => setStockHistoryFor(null)}/>;
                }
                return null;
            }}
        />
    );
}

const discountFields = (products: { value: string; label: string }[], t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.discount.field.isUsing', '사용'), type: 'checkbox', wide: true},
    {name: 'name', label: t('admin.discount.field.name', '할인명'), required: true, wide: true},
    {name: 'description', label: t('admin.discount.field.description', '설명'), wide: true},
    {name: 'rate', label: t('admin.discount.field.rate', '할인율(%)'), type: 'number', min: 0, required: true, wide: true},
    {name: 'productId', label: t('admin.discount.field.productId', '대상 상품'), type: 'select', options: products, wide: true,
        emptyOptionLabel: t('admin.discount.field.productId.all', '전체'),
        help: t('admin.discount.field.productId.help', '선택 안 함이면 모든 상품에 적용됩니다.')},
];

const toDiscountBody = (values: FormValues) => ({...values, productId: values.productId ? Number(values.productId) : null});

const loadDiscounts = (query: Query) => adminApi.discounts.search(query);

export function DiscountAdminPage() {
    const {t} = useI18n();
    const [products, setProducts] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        adminApi.products.search({size: 200})
            .then((page) => setProducts(page.content.map((p) => ({value: String(p.id), label: p.name}))))
            .catch(() => setProducts([]));
    }, []);
    const productName = (id: number) => products.find((p) => p.value === String(id))?.label ?? t('admin.discount.productFallback', '상품 #{id}', {id: String(id)});
    return (
        <AdminCrudPage<Discount>
            title={t('admin.discount.title', '할인')}
            searchable={false}
            load={loadDiscounts}
            fields={discountFields(products, t)}
            emptyForm={{isUsing: true}}
            columns={[
                {label: t('admin.discount.column.name', '할인명'), render: (d) => d.name},
                {label: t('admin.discount.column.rate', '할인율'), render: (d) => `${d.rate}%`},
                {label: t('admin.discount.column.target', '대상'), render: (d) => (d.productId ? productName(d.productId) : t('admin.discount.allProducts', '전체 상품'))},
                {label: t('admin.discount.column.isUsing', '사용 여부'), render: (d) => <YesNo value={d.isUsing}/>},
            ]}
            create={(v) => adminApi.discounts.create(toDiscountBody(v))}
            update={(d, v) => adminApi.discounts.update(d.id, toDiscountBody(v))}
            remove={(d) => adminApi.discounts.remove(d.id)}
        />
    );
}

const NEXT_ORDER_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {PAID: 'SHIPPING', SHIPPING: 'DELIVERED'};
const loadOrders = (query: Query) => adminApi.orders.search(query);

const pleaseChoose = (t: T) => ({value: '', label: t('admin.common.pleaseChoose', '선택하세요')});
const paidField = (t: T): FieldSpec => ({name: 'paid', label: t('admin.common.paidField', '결제 완료로 등록'), type: 'checkbox', wide: true,
    help: t('admin.common.paidField.help', '현장·계좌이체 등으로 이미 받은 경우 체크합니다. 체크하지 않으면 결제 대기로 등록되고, 목록에서 결제 완료 처리할 수 있습니다.')});

async function loadProductChoices() {
    const products = await adminApi.products.search({size: 100});
    const details = await Promise.all(products.content.map((p) => adminApi.products.get(p.id)));
    return details.flatMap((p) => [
        {value: String(p.id), label: `${p.name} · ${won(p.price)}`},
        ...p.options.map((o) => ({value: `${p.id}:${o.id}`, label: `${p.name} (${o.name}) · ${won(p.price + o.additionalPrice)}`})),
    ]);
}

const orderFields = (productChoices: { value: string; label: string }[], t: T): FieldSpec[] => [
    {name: 'product', label: t('admin.order.field.product', '상품'), type: 'select', required: true, options: [pleaseChoose(t), ...productChoices], wide: true},
    {name: 'quantity', label: t('admin.order.field.quantity', '수량'), type: 'number', min: 0, required: true, wide: true},
    {name: 'receiverName', label: t('admin.order.field.receiverName', '수령인'), required: true, wide: true},
    {name: 'phoneNumber', label: t('admin.order.field.phoneNumber', '연락처'), required: true, wide: true},
    {name: 'postalCode', label: t('admin.order.field.postalCode', '우편번호'), type: 'postcode', wide: true},
    {name: 'address', label: t('admin.order.field.address', '주소'), help: t('admin.order.field.address.help', '비워 두면 배송 없는 주문(현장 수령)으로 등록됩니다.'), wide: true},
    {name: 'detailAddress', label: t('admin.order.field.detailAddress', '상세 주소'), wide: true},
    paidField(t),
];

function createOrder({product, paid, ...rest}: FormValues) {
    const [productId, productOptionId] = String(product).split(':').map(Number);
    return adminApi.orders.create({...rest, productId, productOptionId: productOptionId ?? null}, Boolean(paid)).then(() => undefined);
}

const CANCELABLE: OrderStatus[] = ['QUOTE_REQUESTED', 'QUOTED', 'PENDING_PAYMENT', 'PAID'];

function QuoteForm({order, onDone}: { order: Order; onDone: () => void }) {
    const {t} = useI18n();
    const [amount, setAmount] = useState(order.totalPrice?.toString() ?? '');
    const [message, setMessage] = useState(order.quoteMessage ?? '');
    const [error, setError] = useState<unknown>(null);
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await adminApi.orders.quote(order.id, Number(amount), message);
            onDone();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <form className="inline-form" onSubmit={submit}>
            <h3>{t('admin.order.quote.title', '견적 회신')}</h3>
            <p className="muted">{t('admin.order.quote.request', '요청 내용: {message}', {message: order.requestMessage ?? t('admin.order.quote.noRequest', '(내용 없음)')})}</p>
            <div className="schema-grid">
                <div className="field">
                    <label htmlFor={`quote-amount-${order.id}`}>{t('admin.order.quote.amount', '견적 금액(원)')}</label>
                    <input id={`quote-amount-${order.id}`} type="number" min={0} required value={amount} onChange={(e) => setAmount(e.target.value)}/>
                </div>
                <div className="field wide">
                    <label htmlFor={`quote-message-${order.id}`}>{t('admin.order.quote.message', '안내 메시지')}</label>
                    <textarea id={`quote-message-${order.id}`} rows={3} value={message} onChange={(e) => setMessage(e.target.value)}/>
                </div>
            </div>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">{t('admin.order.quote.send', '견적 보내기')}</button>
            </div>
        </form>
    );
}

function OrderDetailPanel({order, onChange}: { order: Order; onChange: () => void }) {
    const {t} = useI18n();
    const [courier, setCourier] = useState(order.courier ?? '');
    const [trackingNumber, setTrackingNumber] = useState(order.trackingNumber ?? '');
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        if (!order.courier) {
            shopApi.shippingPolicy().then((policy) => setCourier((current) => current || policy.defaultCourier || '')).catch(() => undefined);
        }
    }, [order.courier]);
    const run = async (action: () => Promise<unknown>) => {
        setError(null);
        try {
            await action();
            onChange();
        } catch (e) {
            setError(e);
        }
    };
    const activeCount = order.items.filter((item) => item.status === 'ORDERED').length;
    const partiallyCancelable = order.status === 'PENDING_PAYMENT' || order.status === 'PAID';
    return (
        <div className="inline-form">
            <h3>{t('admin.order.detail.title', '주문 상세')}</h3>
            <OrderItemsTable order={order} onCancel={partiallyCancelable && activeCount > 1
                ? (item) => window.confirm(t('admin.order.detail.confirmCancelItem', "'{name}' 항목을 취소할까요? 결제된 주문은 이 항목 금액을 환불합니다.", {name: item.productName}))
                    && void run(() => adminApi.orders.cancelItem(order.id, item.id))
                : undefined}/>
            <p className="muted">
                {order.address
                    ? t('admin.order.detail.address', '배송지: ({postalCode}) {address} {detailAddress}',
                        {postalCode: order.postalCode ?? '-', address: order.address, detailAddress: order.detailAddress ?? ''})
                    : t('admin.order.detail.noAddress', '배송지 없음 (방문 수령 등)')}
            </p>
            {(order.status === 'PAID' || order.status === 'SHIPPING') && (
                <form className="toolbar" onSubmit={(e) => {
                    e.preventDefault();
                    void run(() => adminApi.orders.ship(order.id, courier, trackingNumber));
                }}>
                    <input aria-label={t('admin.order.detail.courier', '택배사')} placeholder={t('admin.order.detail.courier', '택배사')} required
                           value={courier} onChange={(e) => setCourier(e.target.value)}/>
                    <input aria-label={t('admin.order.detail.trackingNumber', '송장 번호')} placeholder={t('admin.order.detail.trackingNumber', '송장 번호')} required
                           value={trackingNumber} onChange={(e) => setTrackingNumber(e.target.value)}/>
                    <button type="submit" className="primary">
                        {order.status === 'PAID' ? t('admin.order.detail.shipAction', '송장 입력 후 배송 처리') : t('admin.order.detail.editTracking', '송장 수정')}
                    </button>
                </form>
            )}
            <ErrorBanner error={error}/>
            <ProgressAndMessages
                getProgress={() => adminApi.progress.get('ORDER', order.id)}
                advance={(stageCode, memo) => adminApi.progress.advance('ORDER', order.id, {stageCode, memo})}
                listMessages={() => adminApi.messages.list('ORDER', order.id)}
                postMessage={(content, fileIds) => adminApi.messages.post('ORDER', order.id, {content, fileIds})}
            />
        </div>
    );
}

export function OrderAdminPage() {
    const {t} = useI18n();
    const {statuses, label: statusLabel, reload: reloadStatuses} = useStatusLabels('ORDER', orderStatusLabels(t));
    const [status, setStatus] = useState('');
    const [quoting, setQuoting] = useState<number | null>(null);
    const [detail, setDetail] = useState<number | null>(null);
    const [productChoices, setProductChoices] = useState<{ value: string; label: string }[]>([]);
    useEffect(() => {
        loadProductChoices().then(setProductChoices).catch(() => setProductChoices([]));
    }, []);
    return (
        <AdminCrudPage<Order>
            title={t('admin.order.title', '주문')}
            description={<>
                <p>{t('admin.order.description', '관리자가 전화·방문 주문을 직접 등록할 수 있습니다. 관리자 명의로 저장되며 회원 마이페이지에는 보이지 않습니다.')}</p>
                <RegexBindingSelect target="ORDER_NUMBER" label={t('admin.order.regexLabel', '주문번호 정규식')}/>
                <StatusManager domain="ORDER" statuses={statuses} onChange={reloadStatuses}/>
                <ProgressTemplateManager targetType="ORDER"/>
            </>}
            load={loadOrders}
            fields={orderFields(productChoices, t)}
            emptyForm={{product: '', quantity: 1, paid: false}}
            create={createOrder}
            extraQuery={{status}}
            filters={
                <select aria-label={t('admin.order.statusFilter', '주문 상태')} value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">{t('admin.common.allStatuses', '전체 상태')}</option>
                    {toOptions(orderStatusLabels(t)).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: t('admin.order.column.orderNumber', '주문번호'), render: (o) => <code>{o.orderNumber}</code>},
                {label: t('admin.order.column.product', '상품'), render: (o) => (
                    <button type="button" className="link-button" onClick={() => setDetail(detail === o.id ? null : o.id)}>
                        {o.productName} × {o.quantity}
                    </button>
                )},
                {label: t('admin.order.column.saleType', '판매 방식'), render: (o) => <span className="badge">{saleType(t)[o.saleType]}</span>},
                {label: t('admin.order.column.shipDate', '출고예정일'), render: (o) => o.shipDate ? formatDate(o.shipDate) : '-'},
                {label: t('admin.order.column.amount', '금액'), render: (o) => <OrderAmount order={o}/>},
                {label: t('admin.order.column.receiver', '수령인'), render: (o) => `${o.receiverName} (${o.phoneNumber})`},
                {label: t('admin.order.column.status', '상태'), render: (o, reload) => (
                    <span className="row align-center">
                        {statusLabel(o.status, o.customStatus)}
                        <CustomStatusSelect statuses={statuses} status={o.status} customStatus={o.customStatus}
                                            onApply={(code) => void statusApi.applyToOrder(o.id, code).then(reload)}/>
                    </span>
                )},
                {label: t('admin.order.column.createdAt', '주문일'), render: (o) => formatDateTime(o.createdAt)},
                {label: t('admin.order.column.statusAction', '상태 처리'), className: 'button-cell', render: (o, reload) => (
                    <>
                        {o.status === 'PENDING_PAYMENT' && (
                            <button type="button" onClick={() => {
                                if (window.confirm(t('admin.order.confirmMarkPaid', '결제를 받은 것으로 처리할까요? 결제 내역에는 기록되지 않습니다.'))) {
                                    void adminApi.orders.markPaid(o.id).then(reload);
                                }
                            }}>{t('admin.order.markPaid', '결제 완료 처리')}</button>
                        )}
                        {NEXT_ORDER_STATUS[o.status] && (
                            <button type="button" onClick={() => adminApi.orders.changeStatus(o.id, NEXT_ORDER_STATUS[o.status]!).then(reload)}>
                                {t('admin.order.advanceStatus', '{status} 처리', {status: statusLabel(NEXT_ORDER_STATUS[o.status]!)})}
                            </button>
                        )}
                    </>
                )},
                {label: t('admin.order.column.quote', '견적'), className: 'button-cell', render: (o) => (o.status === 'QUOTE_REQUESTED' || o.status === 'QUOTED') && (
                    <button type="button" onClick={() => setQuoting(quoting === o.id ? null : o.id)}>
                        {quoting === o.id ? t('common.close', '닫기') : o.status === 'QUOTED' ? t('admin.order.editQuote', '견적 수정') : t('admin.order.quote.title', '견적 회신')}
                    </button>
                )},
                {label: t('admin.order.column.cancel', '취소'), className: 'button-cell', render: (o, reload) => CANCELABLE.includes(o.status) && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm(t('admin.order.confirmCancel', '주문을 취소할까요? 결제된 주문은 환불 처리됩니다.'))) {
                            void adminApi.orders.changeStatus(o.id, 'CANCELED').then(reload);
                        }
                    }}>{t('common.cancel', '취소')}</button>
                )},
            ]}
            expandedRow={(o, reload) => (
                <>
                    {quoting === o.id && <QuoteForm order={o} onDone={() => {
                        setQuoting(null);
                        reload();
                    }}/>}
                    {detail === o.id && <OrderDetailPanel order={o} onChange={reload}/>}
                </>
            )}
        />
    );
}

const loadReturnRequests = (query: Query) => adminApi.returnRequests.search(query);

export function OrderReturnAdminPage() {
    const {t} = useI18n();
    const [status, setStatus] = useState<OrderReturnStatus | ''>('');
    return (
        <AdminCrudPage<OrderReturn>
            title={t('admin.orderReturn.title', '반품·교환')}
            searchable={false}
            description={t('admin.orderReturn.description', '주문이 결제·배송 완료된 뒤 회원이 신청한 반품·교환입니다. 승인하면 반품은 환불·재고 복구, 교환은 새 옵션으로 결제 완료된 주문을 바로 만듭니다.')}
            load={loadReturnRequests}
            extraQuery={{status}}
            filters={
                <select aria-label={t('admin.orderReturn.statusFilter', '처리 상태')} value={status} onChange={(e) => setStatus(e.target.value as OrderReturnStatus | '')}>
                    <option value="">{t('admin.common.allStatuses', '전체 상태')}</option>
                    {toOptions(returnStatus(t)).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: t('admin.orderReturn.column.orderNumber', '주문번호'), render: (r) => <code>{r.orderNumber}</code>},
                {label: t('admin.orderReturn.column.type', '구분'), render: (r) => <span className="badge">{returnType(t)[r.type]}</span>},
                {label: t('admin.orderReturn.column.reason', '이유'), render: (r) => r.reason},
                {label: t('admin.orderReturn.column.status', '상태'), render: (r) => r.status === 'REQUESTED' ? <span className="badge">{returnStatus(t)[r.status]}</span>
                        : r.status === 'APPROVED' ? <span className="badge ok">{returnStatus(t)[r.status]}</span>
                            : <span className="badge danger">{returnStatus(t)[r.status]}</span>},
                {label: t('admin.orderReturn.column.adminMemo', '관리자 메모'), render: (r) => r.adminMemo ?? '-'},
                {label: t('admin.orderReturn.column.createdAt', '신청일'), render: (r) => formatDateTime(r.createdAt)},
            ]}
            actions={(r, reload) => r.status === 'REQUESTED' && (
                <>
                    <button type="button" onClick={() => {
                        if (window.confirm(r.type === 'EXCHANGE' ? t('admin.orderReturn.confirmApproveExchange', '교환을 승인할까요? 원래 주문은 취소·환불되고 새 옵션으로 결제 완료된 주문이 만들어집니다.')
                            : t('admin.orderReturn.confirmApproveReturn', '반품을 승인할까요? 주문이 취소되고 환불·재고 복구가 이루어집니다.'))) {
                            void adminApi.returnRequests.approve(r.id).then(reload);
                        }
                    }}>{t('admin.orderReturn.approve', '승인')}</button>
                    <button type="button" className="danger" onClick={() => {
                        const memo = window.prompt(t('admin.orderReturn.rejectPrompt', '거절 이유를 입력해 주세요.'));
                        if (memo != null) {
                            void adminApi.returnRequests.reject(r.id, memo).then(reload);
                        }
                    }}>{t('admin.orderReturn.reject', '거절')}</button>
                </>
            )}
        />
    );
}

const loadSubscriptions = (query: Query) => adminApi.subscriptions.search(query);

export function SubscriptionAdminPage() {
    const {t} = useI18n();
    const [notice, setNotice] = useState<string | null>(null);
    const [version, setVersion] = useState(0);
    return (
        <AdminCrudPage<Subscription>
            key={version}
            title={t('admin.subscription.title', '구독')}
            searchable={false}
            description={<>
                <p>{t('admin.subscription.description', '구독 상품을 결제하면 구독이 시작되고, 결제일마다 다음 회차 주문이 만들어져 자동 결제됩니다(목업 PG). 매일 새벽 3시에 처리합니다.')}</p>
                <div className="toolbar">
                    <button type="button" onClick={async () => {
                        const result = await adminApi.subscriptions.renewDue();
                        setNotice(t('admin.subscription.renewed', '결제일이 된 구독 {count}건을 갱신했습니다.', {count: String(result.renewed)}));
                        setVersion((v) => v + 1);
                    }}>{t('admin.subscription.renewNow', '지금 갱신 처리')}</button>
                    {notice && <span className="ok">{notice}</span>}
                </div>
            </>}
            load={loadSubscriptions}
            columns={[
                {label: 'ID', render: (s) => s.id},
                {label: t('admin.subscription.column.product', '상품'), render: (s) => s.productName},
                {label: t('admin.subscription.column.memberId', '회원 ID'), render: (s) => s.memberId},
                {label: t('admin.subscription.column.amount', '금액'), render: (s) => `${billingCycle(t)[s.billingCycle]} ${won(s.price)}`},
                {label: t('admin.subscription.column.status', '상태'), render: (s) => (
                    s.status === 'ACTIVE' ? <span className="badge ok">{t('admin.subscription.active', '구독 중')}</span>
                        : s.status === 'PAUSED'
                            ? <span className="badge">
                                {t('admin.subscription.paused', '일시정지')}{s.resumeOn ? t('admin.subscription.resumeOn', ' ({date}에 재개)', {date: s.resumeOn}) : ''}
                            </span>
                        : <span className="badge">{t('admin.subscription.canceled', '해지')}</span>
                )},
                {label: t('admin.subscription.column.nextBilling', '다음 결제일'), render: (s) => s.nextBillingDate ?? '-'},
                {label: t('admin.subscription.column.commitment', '약정'), render: (s) => s.commitmentEndDate
                    ? <>{t('admin.subscription.commitmentUntil', '{date}까지', {date: formatDate(s.commitmentEndDate)})}
                        {s.earlyTerminationFee > 0 && t('admin.subscription.terminationFee', ' · 위약금 {fee}', {fee: won(s.earlyTerminationFee)})}</>
                    : '-'},
                {label: t('admin.subscription.column.startedAt', '시작일'), render: (s) => formatDateTime(s.startedAt)},
            ]}
            actions={(s, reload) => s.status !== 'CANCELED' && (
                <>
                    {s.status === 'ACTIVE' ? (
                        <button type="button" onClick={() => {
                            const resumeOn = window.prompt(t('admin.subscription.resumePrompt', '재개일을 입력하세요 (YYYY-MM-DD, 비우면 직접 재개할 때까지 정지)')) ?? undefined;
                            if (resumeOn !== undefined) {
                                void adminApi.subscriptions.pause(s.id, resumeOn || null).then(reload);
                            }
                        }}>{t('admin.subscription.pause', '일시정지')}</button>
                    ) : (
                        <button type="button" onClick={() => void adminApi.subscriptions.resume(s.id).then(reload)}>{t('admin.subscription.resume', '재개')}</button>
                    )}
                    <button type="button" className="danger" onClick={() => {
                        const message = s.earlyTerminationFee > 0
                            ? t('admin.subscription.confirmCancelWithFee', '구독을 해지할까요? 약정 기간이 남아 위약금 {fee}이 바로 결제됩니다.', {fee: won(s.earlyTerminationFee)})
                            : t('admin.subscription.confirmCancel', '구독을 해지할까요? 다음 결제일부터 결제되지 않습니다.');
                        if (window.confirm(message)) {
                            void adminApi.subscriptions.cancel(s.id).then(reload);
                        }
                    }}>{t('admin.subscription.cancelAction', '해지')}</button>
                </>
            )}
        />
    );
}

const loadPasses = (query: Query) => adminApi.passes.search(query);

export function PassAdminPage() {
    const {t} = useI18n();
    return (
        <AdminCrudPage<MemberPass>
            title={t('admin.pass.title', '이용권')}
            searchable={false}
            description={t('admin.pass.description', '회원이 이용권 상품을 결제하면 발급됩니다. 예약할 때 이용권을 고르면 결제 대신 기간·횟수가 차감됩니다.')}
            load={loadPasses}
            columns={[
                {label: 'ID', render: (p) => p.id},
                {label: t('admin.pass.column.memberId', '회원 ID'), render: (p) => p.memberId},
                {label: t('admin.pass.column.pass', '이용권'), render: (p) => <>{p.productName} <span className="badge">{passUnit(t)[p.unit]}</span></>},
                {label: t('admin.pass.column.remaining', '남은 횟수'), render: (p) => p.unit === 'COUNT' ? `${p.remainingCount ?? 0}/${p.totalCount ?? 0}` : '-'},
                {label: t('admin.pass.column.expiresAt', '만료일'), render: (p) => p.expiresAt ? formatDateTime(p.expiresAt) : '-'},
                {label: t('admin.pass.column.status', '상태'), render: (p) => p.status === 'ACTIVE' ? <span className="badge ok">{t('admin.pass.usable', '사용 가능')}</span>
                        : p.status === 'USED_UP' ? <span className="badge">{t('admin.pass.usedUp', '다 씀')}</span> : <span className="badge">{t('common.cancel', '취소')}</span>},
                {label: t('admin.pass.column.createdAt', '발급일'), render: (p) => formatDateTime(p.createdAt)},
            ]}
            actions={(p, reload) => p.status === 'ACTIVE' && (
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm(t('admin.pass.confirmCancel', '이 이용권을 취소할까요? 더 이상 쓸 수 없게 됩니다.'))) {
                        void adminApi.passes.cancel(p.id).then(reload);
                    }
                }}>{t('common.cancel', '취소')}</button>
            )}
        />
    );
}

type OrdersTab = 'orders' | 'subscriptions' | 'license-keys';

export function OrdersAdminPage() {
    const {t} = useI18n();
    const ordersTabs: { value: OrdersTab; label: string }[] = [
        {value: 'orders', label: t('admin.order.title', '주문')},
        {value: 'subscriptions', label: t('admin.subscription.title', '구독')},
        {value: 'license-keys', label: t('admin.ordersAdmin.licenseKeysTab', '라이선스 키')},
    ];
    const [tab, setTab] = useState<OrdersTab>('orders');
    return (
        <>
            <div className="tabs" role="tablist" aria-label={t('admin.ordersAdmin.tabsLabel', '주문 관리')}>
                {ordersTabs.map((tabItem) => (
                    <button key={tabItem.value} type="button" role="tab" aria-selected={tab === tabItem.value}
                            className={tab === tabItem.value ? 'tab active' : 'tab'} onClick={() => setTab(tabItem.value)}>
                        {tabItem.label}
                    </button>
                ))}
            </div>
            {tab === 'orders' && <OrderAdminPage/>}
            {tab === 'subscriptions' && <SubscriptionAdminPage/>}
            {tab === 'license-keys' && <LicenseKeyAdminPage/>}
        </>
    );
}

const isType = (...types: ReservationType[]) => (values: FormValues) => types.includes(values.reservationType as ReservationType);

const reservationItemFields = (t: T): FieldSpec[] => [
    {name: 'reservationType', label: t('admin.reservationItem.field.reservationType', '예약 유형'), type: 'select', required: true, wide: true,
        options: reservationTypeGroups(t).flatMap((group) => group.types.map((type) => ({value: type, label: `${group.label} · ${reservationType(t)[type]}`}))),
        help: t('admin.reservationItem.field.reservationType.help', '시간 예약은 회차나 시간대를, 공간 예약은 배치표의 좌석·객실·공간을 골라 예약합니다.')},
    {name: 'name', label: t('admin.reservationItem.field.name', '이름'), required: true, wide: true},
    {name: 'description', label: t('admin.reservationItem.field.description', '요약 설명'), wide: true},
    {name: 'price', label: t('admin.reservationItem.field.price', '기본 가격(원)'), type: 'number', min: 0, required: true, wide: true,
        help: t('admin.reservationItem.field.price.help', '프로그램은 1인, 상담은 1회, 대관·식당 테이블은 칸·예약 단위당, 숙박은 칸·1박당, 공연은 좌석당, 물품 대여는 1개·1일당 가격입니다. 배치표에서 칸별 가격을 따로 줄 수 있습니다. 식당 테이블은 보통 0원으로 둡니다. 여행·투어는 아래 인원 유형별 요금을 쓰므로 이 값은 사용하지 않습니다(0으로 둡니다).')},
    {name: 'priceTiersText', label: t('admin.reservationItem.field.priceTiersText', '인원 유형별 요금'), wide: true, required: true, placeholder: '성인:100000, 아동:50000',
        help: t('admin.reservationItem.field.priceTiersText.help', '이름:가격 형태로 쉼표로 구분해 적습니다. 예약할 때 유형별 인원을 입력받아 합산합니다.'), visibleWhen: isType('TOUR')},
    {name: 'maximumPerson', label: t('admin.reservationItem.field.maximumPerson', '정원·수량'), type: 'number', min: 0, wide: true,
        visibleWhen: isType('PROGRAM', 'CONSULTATION', 'VISIT', 'ITEM_RENTAL'),
        help: t('admin.reservationItem.field.maximumPerson.help', '프로그램은 전체 인원, 상담·방문은 시간대별 동시 예약 수(비우면 프로그램은 제한 없음, 상담·방문은 1건), 물품 대여는 보유 수량(필수)입니다.')},
    {name: 'depositAmount', label: t('admin.reservationItem.field.depositAmount', '보증금(원, 1개당)'), type: 'number', min: 0, wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: t('admin.reservationItem.field.depositAmount.help', '결제 금액에 더해 받고, 반납 처리할 때 차감액을 뺀 만큼 돌려줍니다.')},
    {name: 'maxRentalDays', label: t('admin.reservationItem.field.maxRentalDays', '최대 대여 일수'), type: 'number', min: 1, wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: t('admin.reservationItem.field.maxRentalDays.help', '비워두면 제한이 없습니다.')},
    {name: 'lateFeePerDay', label: t('admin.reservationItem.field.lateFeePerDay', '연체료(원, 1일당)'), type: 'number', min: 0, wide: true, visibleWhen: isType('ITEM_RENTAL'),
        help: t('admin.reservationItem.field.lateFeePerDay.help', '반납일을 넘기면 하루당 이 금액을 보증금에서 뗍니다. 비워두면 연체료를 받지 않습니다.')},
    {name: 'supportsQueue', label: t('admin.reservationItem.field.supportsQueue', '접수·대기 사용'), type: 'checkbox', wide: true, visibleWhen: isType('CONSULTATION'),
        help: t('admin.reservationItem.field.supportsQueue.help', '체크인할 때 대기 순번을 매기고, 관리자 "접수·대기" 메뉴에서 호출·완료를 관리합니다. (진료 접수 등)')},
    {name: 'isEnabled', label: t('admin.reservationItem.field.isEnabled', '공개'), type: 'checkbox', wide: true},
    {name: 'startDateTime', label: t('admin.reservationItem.field.startDateTime', '시작 일시'), type: 'datetime', wide: true,
        help: t('admin.reservationItem.field.startDateTime.help', '프로그램은 진행 시작, 상담·방문·대관·숙박은 예약을 받는 기간의 시작, 공연은 공연 일시입니다.')},
    {name: 'endDateTime', label: t('admin.reservationItem.field.endDateTime', '종료 일시'), type: 'datetime', wide: true},
    {name: 'slotMinutes', label: t('admin.reservationItem.field.slotMinutes', '예약 단위(분)'), type: 'number', min: 0, wide: true,
        visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE'), help: t('admin.reservationItem.field.slotMinutes.help', '상담·방문 1회 길이, 대관·식당 테이블 최소 단위입니다.')},
    {name: 'openTime', label: t('admin.reservationItem.field.openTime', '운영 시작 시각'), type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE')},
    {name: 'closeTime', label: t('admin.reservationItem.field.closeTime', '운영 종료 시각'), type: 'time', wide: true, visibleWhen: isType('CONSULTATION', 'VISIT', 'RENTAL', 'TABLE')},
    {name: 'layout', label: t('admin.reservationItem.field.layout', '배치표'), type: 'layout', visibleWhen: isType('RENTAL', 'LODGING', 'EVENT', 'TABLE'),
        help: t('admin.reservationItem.field.layout.help', '좌석·객실·공간을 칠해 두면 사용자가 그 칸을 골라 예약합니다. 식당 테이블은 칸별로 인원(좌석 수)을 함께 정해 두면 예약할 때 인원을 넘지 못하게 막습니다.')},
    {name: 'applicationStartDateTime', label: t('admin.reservationItem.field.applicationStartDateTime', '신청 시작'), type: 'datetime', wide: true},
    {name: 'applicationEndDateTime', label: t('admin.reservationItem.field.applicationEndDateTime', '신청 마감'), type: 'datetime', wide: true},
    {name: 'imageFileId', label: t('admin.reservationItem.field.imageFileId', '대표 이미지'), type: 'image', wide: true},
    {name: 'contentHtml', label: t('admin.reservationItem.field.contentHtml', '상세 설명'), type: 'html'},
];

const reservationItemGradeFields = (grades: Grade[], t: T) => [
    gradeSelectField('accessGradeId', t('admin.reservationItem.field.accessGradeId', '예약 가능 등급'), grades,
        t('admin.reservationItem.field.accessGradeId.help', '이 등급 이상 회원만 예약할 수 있습니다.'), t),
];

const toReservationItemBody = ({priceTiersText, categoryId, ...values}: FormValues) => ({
    ...values,
    categoryId: categoryId ? Number(categoryId) : null,
    priceTiers: values.reservationType === 'TOUR'
        ? String(priceTiersText ?? '').split(',').map((part) => part.trim()).filter(Boolean).map((part) => {
            const [name, price] = part.split(':');
            return {name: (name ?? '').trim(), price: Number((price ?? '').trim())};
        })
        : null,
});

const loadReservationItems = (query: Query) => adminApi.reservationItems.search(query);

export function ReservationItemAdminPage() {
    const {t} = useI18n();
    const categories = useCategories();
    const base = useMemo(() => {
        const fields = reservationItemFields(t);
        const at = fields.findIndex((field) => field.name === 'description') + 1;
        return [...fields.slice(0, at), ...categorySelectField(categories, t), ...fields.slice(at)];
    }, [categories, t]);
    const fields = useWithGradeFields(base, (grades) => reservationItemGradeFields(grades, t));
    return (
        <AdminCrudPage<ReservationItem>
            title={t('admin.reservationItem.title', '예약')}
            description={<>
                <p>{t('admin.reservationItem.description', '시간 예약(프로그램·상담·출장·방문·여행·투어), 공간 예약(대관·숙박·공연·식당 테이블), 물품 대여를 등록합니다. 예약 내역은 "예약" 메뉴에서 확인합니다.')}</p>
                <p className="toolbar">
                    <Link className="button" to="/admin/check-in">{t('admin.reservationItem.checkIn', '체크인')}</Link>
                    <Link className="button" to="/admin/reception-queue">{t('admin.reservationItem.receptionQueue', '접수·대기')}</Link>
                </p>
            </>}
            load={loadReservationItems}
            fields={fields}
            emptyForm={{reservationType: 'PROGRAM', isEnabled: true, price: 0, contentHtml: '', layout: EMPTY_LAYOUT}}
            columns={[
                {label: 'ID', render: (p) => p.id},
                {label: t('admin.reservationItem.column.type', '유형'), render: (p) => <span className="badge">{reservationType(t)[p.reservationType]}</span>},
                {label: t('admin.reservationItem.column.name', '이름'), render: (p) => <Link to={`/reservation-items/${p.id}`}>{p.name}</Link>},
                {label: t('admin.reservationItem.column.category', '카테고리'), render: (p) => categories.find((c) => c.id === p.categoryId)?.name ?? '-'},
                {label: t('admin.reservationItem.column.price', '기본 가격'), render: (p) => won(p.price)},
                {label: t('admin.reservationItem.column.capacity', '잔여/정원'), render: (p) => (p.reservationType !== 'PROGRAM' ? '-'
                    : p.maximumPerson ? `${p.availablePerson}/${p.maximumPerson}` : t('admin.reservationItem.noLimit', '제한 없음'))},
                {label: t('admin.reservationItem.column.applicationPeriod', '신청 기간'), render: (p) => `${formatDateTime(p.applicationStartDateTime)} ~ ${formatDateTime(p.applicationEndDateTime)}`},
                {label: t('admin.reservationItem.column.isOpen', '접수 중'), render: (p) => <YesNo value={p.isOpen}/>},
            ]}
            toForm={(p) => adminApi.reservationItems.get(p.id).then((full) => ({...full, layout: full.layout ?? EMPTY_LAYOUT,
                priceTiersText: (full.priceTiers ?? []).map((t) => `${t.name}:${t.price}`).join(', ')}))}
            create={(v) => adminApi.reservationItems.create(toReservationItemBody(v))}
            update={(p, v) => adminApi.reservationItems.update(p.id, toReservationItemBody(v))}
            remove={(p) => adminApi.reservationItems.remove(p.id)}
            expandedRow={(p) => (p.reservationType === 'PROGRAM' || p.reservationType === 'EVENT' || p.reservationType === 'TOUR')
                ? <SessionManager item={p}/> : undefined}
        />
    );
}

function SessionManager({item}: { item: ReservationItem }) {
    const {t} = useI18n();
    const [sessions, setSessions] = useState<ReservationItemSession[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [editing, setEditing] = useState<{ id: number | null; name: string; startAt: string; endAt: string; capacity: string } | null>(null);

    const reload = useCallback(() => {
        adminApi.reservationItems.get(item.id).then((full) => setSessions(full.sessions)).catch(setError);
    }, [item.id]);
    useEffect(reload, [reload]);

    const startNew = () => setEditing({id: null, name: '', startAt: '', endAt: '', capacity: ''});
    const startEdit = (session: ReservationItemSession) => setEditing({
        id: session.id, name: session.name ?? '', startAt: session.startAt.slice(0, 16),
        endAt: session.endAt?.slice(0, 16) ?? '', capacity: session.capacity != null ? String(session.capacity) : '',
    });

    const save = async () => {
        if (!editing || !editing.startAt) {
            return;
        }
        const body = {
            name: editing.name || null, startAt: `${editing.startAt}:00`, endAt: editing.endAt ? `${editing.endAt}:00` : null,
            capacity: editing.capacity ? Number(editing.capacity) : null,
        };
        try {
            if (editing.id) {
                await adminApi.sessions.update(item.id, editing.id, body);
            } else {
                await adminApi.sessions.create(item.id, body);
            }
            setEditing(null);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const remove = async (session: ReservationItemSession) => {
        if (!window.confirm(t('admin.session.confirmDelete', '이 회차를 지울까요?'))) {
            return;
        }
        try {
            await adminApi.sessions.remove(item.id, session.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div>
            <h3>{item.reservationType === 'TOUR' ? t('admin.session.departureDate', '출발일') : t('admin.session.title', '회차')}</h3>
            <ErrorBanner error={error}/>
            <table className="compact">
                <thead><tr>
                    <th>{t('admin.session.name', '이름')}</th><th>{t('admin.session.startAtHeader', '시작')}</th>
                    <th>{t('admin.session.endAtHeader', '종료')}</th><th>{t('admin.session.capacity', '정원/예약')}</th><th/>
                </tr></thead>
                <tbody>
                {sessions?.map((session) => (
                    <tr key={session.id}>
                        <td>{session.name || '-'}</td>
                        <td>{formatDateTime(session.startAt)}</td>
                        <td>{session.endAt ? formatDateTime(session.endAt) : '-'}</td>
                        <td>{session.capacity != null ? `${session.reservedCount}/${session.capacity}` : `${session.reservedCount}/${t('admin.reservationItem.noLimit', '제한 없음')}`}</td>
                        <td className="actions">
                            <button type="button" onClick={() => startEdit(session)}>{t('common.edit', '수정')}</button>
                            <button type="button" className="danger" onClick={() => remove(session)}>{t('common.delete', '삭제')}</button>
                        </td>
                    </tr>
                ))}
                {sessions?.length === 0 && <tr><td colSpan={5} className="muted">{t('admin.session.noData', '회차가 없습니다.')}</td></tr>}
                </tbody>
            </table>
            {editing ? (
                <div className="toolbar">
                    <input aria-label={t('admin.session.name', '이름')} placeholder={t('admin.session.name', '이름')} value={editing.name}
                           onChange={(e) => setEditing({...editing, name: e.target.value})}/>
                    <input type="datetime-local" aria-label={t('admin.session.startAt', '시작 일시')} required value={editing.startAt}
                           onChange={(e) => setEditing({...editing, startAt: e.target.value})}/>
                    <input type="datetime-local" aria-label={t('admin.session.endAt', '종료 일시')} value={editing.endAt}
                           onChange={(e) => setEditing({...editing, endAt: e.target.value})}/>
                    <input type="number" aria-label={t('admin.session.capacityField', '정원')} placeholder={t('admin.session.capacityField', '정원')} min={1}
                           value={editing.capacity} onChange={(e) => setEditing({...editing, capacity: e.target.value})}/>
                    <button type="button" className="primary" onClick={save}>{t('common.save', '저장')}</button>
                    <button type="button" onClick={() => setEditing(null)}>{t('common.cancel', '취소')}</button>
                </div>
            ) : <button type="button" onClick={startNew}>{t('admin.session.add', '회차 추가')}</button>}
        </div>
    );
}

const loadReservations = (query: Query) => adminApi.reservations.search(query);

const reservationFields = (reservationItemChoices: { value: string; label: string }[], t: T): FieldSpec[] => [
    {name: 'reservationItemId', label: t('admin.reservation.field.reservationItemId', '예약 상품'), type: 'select', required: true,
        options: [pleaseChoose(t), ...reservationItemChoices],
        help: t('admin.reservation.field.reservationItemId.help', '신청 기간이 지났거나 비공개인 예약 상품도 예약할 수 있습니다. 정원은 넘길 수 없습니다.'), wide: true},
    {name: 'quantity', label: t('admin.reservation.field.quantity', '인원'), type: 'number', min: 0, required: true, wide: true},
    {name: 'applicantName', label: t('admin.reservation.field.applicantName', '신청자'), required: true, wide: true},
    {name: 'phoneNumber', label: t('admin.reservation.field.phoneNumber', '연락처'), required: true, wide: true},
    {...paidField(t), help: t('admin.reservation.field.paid.help', '무료 예약 상품은 체크하지 않아도 바로 확정됩니다. 체크하지 않은 유료 예약은 결제 대기로 등록되고, 목록에서 확정 처리할 수 있습니다.')},
];

function createReservation({reservationItemId, paid, ...rest}: FormValues) {
    return adminApi.reservations.create({...rest, reservationItemId: Number(reservationItemId)}, Boolean(paid)).then(() => undefined);
}

type ReservationsTab = 'reservations' | 'passes';

export function ReservationAdminPage() {
    const {t} = useI18n();
    const reservationsTabs: { value: ReservationsTab; label: string }[] = [
        {value: 'reservations', label: t('admin.reservation.title', '예약')},
        {value: 'passes', label: t('admin.pass.title', '이용권')},
    ];
    const [tab, setTab] = useState<ReservationsTab>('reservations');
    return (
        <>
            <div className="tabs" role="tablist" aria-label={t('admin.reservationAdmin.tabsLabel', '예약 관리')}>
                {reservationsTabs.map((tabItem) => (
                    <button key={tabItem.value} type="button" role="tab" aria-selected={tab === tabItem.value}
                            className={tab === tabItem.value ? 'tab active' : 'tab'} onClick={() => setTab(tabItem.value)}>
                        {tabItem.label}
                    </button>
                ))}
            </div>
            {tab === 'reservations' && <ReservationListAdminPage/>}
            {tab === 'passes' && <PassAdminPage/>}
        </>
    );
}

function ReservationListAdminPage() {
    const {t} = useI18n();
    const {statuses, label: statusLabel, reload: reloadStatuses} = useStatusLabels('RESERVATION', reservationStatusLabels(t));
    const [status, setStatus] = useState('');
    const [reservationItemChoices, setReservationItemChoices] = useState<{ value: string; label: string }[]>([]);
    const [recordingFor, setRecordingFor] = useState<number | null>(null);
    useEffect(() => {
        adminApi.reservationItems.search({size: 100})
            .then((page) => setReservationItemChoices(page.content.map((p) => ({
                value: String(p.id),
                label: `${p.name} · ${won(p.price)}${p.maximumPerson ? ` · 잔여 ${p.availablePerson}/${p.maximumPerson}` : ''}`,
            }))))
            .catch(() => setReservationItemChoices([]));
    }, []);
    return (
        <AdminCrudPage<Reservation>
            title={t('admin.reservation.title', '예약')}
            description={<>
                <p>{t('admin.reservation.description', '관리자가 전화·현장 예약을 직접 등록할 수 있습니다. 관리자 명의로 저장되며 회원 마이페이지에는 보이지 않습니다. 예약번호나 예약 상품명으로 검색할 수 있습니다.')}</p>
                <RegexBindingSelect target="RESERVATION_NUMBER" label={t('admin.reservation.regexLabel', '예약번호 정규식')}/>
                <StatusManager domain="RESERVATION" statuses={statuses} onChange={reloadStatuses}/>
            </>}
            load={loadReservations}
            fields={reservationFields(reservationItemChoices, t)}
            emptyForm={{reservationItemId: '', quantity: 1, paid: false}}
            create={createReservation}
            extraQuery={{status}}
            filters={
                <select aria-label={t('admin.reservation.statusFilter', '예약 상태')} value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="">{t('admin.common.allStatuses', '전체 상태')}</option>
                    {toOptions(reservationStatusLabels(t)).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
            }
            columns={[
                {label: t('admin.reservation.column.number', '예약번호'), render: (r) => <code>{r.reservationNumber}</code>},
                {label: t('admin.reservation.column.item', '예약 상품'), render: (r) => <>{r.reservationItemName} <span className="badge">{reservationType(t)[r.reservationType]}</span></>},
                {label: t('admin.reservation.column.usage', '이용'), render: (r) => reservationUsage(r, t)},
                {label: t('admin.reservation.column.quantity', '수량'), render: (r) => r.quantity},
                {label: t('admin.reservation.column.amount', '금액'), render: (r) => won(r.totalPrice)},
                {label: t('admin.reservation.column.applicant', '신청자'), render: (r) => `${r.applicantName} (${r.phoneNumber})`},
                {label: t('admin.reservation.column.status', '상태'), render: (r, reload) => (
                    <span className="row align-center">
                        {statusLabel(r.status, r.customStatus)}
                        <CustomStatusSelect statuses={statuses} status={r.status} customStatus={r.customStatus}
                                            onApply={(code) => void statusApi.applyToReservation(r.id, code).then(reload)}/>
                    </span>
                )},
                {label: t('admin.reservation.column.returnVisit', '반납·방문'), render: (r, reload) => r.reservationType === 'ITEM_RENTAL' ? <ReturnCell reservation={r} onDone={reload}/>
                        : r.reservationType === 'VISIT' && <VisitCell reservation={r} onDone={reload}/>},
                {label: t('admin.reservation.column.statusAction', '상태 처리'), className: 'button-cell', render: (r, reload) => r.status === 'PENDING_PAYMENT' && (
                    <button type="button" onClick={() => {
                        if (window.confirm(t('admin.reservation.confirmConfirm', '결제를 받은 것으로 보고 예약을 확정할까요? 결제 내역에는 기록되지 않습니다.'))) {
                            void adminApi.reservations.confirm(r.id).then(reload);
                        }
                    }}>{t('admin.reservation.confirmAction', '확정 처리')}</button>
                )},
                {label: t('admin.reservation.column.cancel', '취소'), className: 'button-cell', render: (r, reload) => r.status !== 'CANCELED' && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm(t('admin.reservation.confirmCancel', '예약을 취소할까요?'))) {
                            void adminApi.reservations.cancel(r.id).then(reload);
                        }
                    }}>{t('common.cancel', '취소')}</button>
                )},
            ]}
            actions={(r) => r.userType === 'MEMBER' && (
                <RecordAction reservation={r} recording={recordingFor === r.id}
                              onToggle={() => setRecordingFor(recordingFor === r.id ? null : r.id)}/>
            )}
            expandedRow={(r, reload) => recordingFor === r.id && (
                <SchemaForm
                    title={t('admin.reservation.recordForm.title', '고객 기록 남기기')}
                    fields={recordContentFields(t)}
                    initial={{recordedOn: new Date().toISOString().slice(0, 10), sharedWithMember: false, photos: []}}
                    onCancel={() => setRecordingFor(null)}
                    onSubmit={async (values) => {
                        await adminApi.customerRecords.create({...toRecordBody(values), memberId: r.userId, reservationId: r.id});
                        setRecordingFor(null);
                        reload();
                    }}
                />
            )}
        />
    );
}

function RecordAction({reservation, recording, onToggle}: { reservation: Reservation; recording: boolean; onToggle: () => void }) {
    const {t} = useI18n();
    const [hasRecord, setHasRecord] = useState(false);
    useEffect(() => {
        adminApi.customerRecords.search({reservationId: reservation.id, size: 1})
            .then((page) => setHasRecord(page.content.length > 0))
            .catch(() => setHasRecord(false));
    }, [reservation]);
    if (hasRecord) {
        return <Link to={`/admin/customer-records?reservationId=${reservation.id}`}>{t('admin.reservation.viewRecord', '기록 보기')}</Link>;
    }
    return (
        <button type="button" onClick={onToggle}>
            {recording ? t('common.cancel', '취소') : t('admin.reservation.addRecord', '기록 남기기')}
        </button>
    );
}

function VisitCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const {t} = useI18n();
    const [workerName, setWorkerName] = useState(reservation.workerName ?? '');
    const [workerPhone, setWorkerPhone] = useState(reservation.workerPhone ?? '');
    const [report, setReport] = useState('');
    const [photoIds, setPhotoIds] = useState<number[]>([]);
    const [error, setError] = useState<unknown>(null);
    const run = (pending: Promise<unknown>) => {
        setError(null);
        pending.then(onDone).catch(setError);
    };
    const address = [reservation.visitPostalCode && `(${reservation.visitPostalCode})`, reservation.visitAddress, reservation.visitDetailAddress]
        .filter(Boolean).join(' ');
    return (
        <div className="small">
            <div>{address}</div>
            {reservation.requestMemo && <div className="muted">{t('admin.reservation.visit.request', '요청: {memo}', {memo: reservation.requestMemo})}</div>}
            {reservation.workCompletedAt ? (
                <div>{t('admin.reservation.visit.completed', '{time} 완료 · {worker}', {time: formatDateTime(reservation.workCompletedAt), worker: reservation.workerName ?? ''})}
                    {reservation.workReport && ` · ${reservation.workReport}`}</div>
            ) : reservation.status === 'CONFIRMED' && (
                <div className="review-cell">
                    <input aria-label={t('admin.reservation.visit.workerName', '작업자 이름')} placeholder={t('admin.reservation.visit.worker', '작업자')}
                           value={workerName} onChange={(e) => setWorkerName(e.target.value)}/>
                    <input aria-label={t('admin.reservation.visit.workerPhone', '작업자 연락처')} placeholder={t('admin.order.field.phoneNumber', '연락처')}
                           value={workerPhone} onChange={(e) => setWorkerPhone(e.target.value)}/>
                    <button type="button" disabled={!workerName.trim()}
                            onClick={() => run(adminApi.reservations.assignWorker(reservation.id, {workerName, workerPhone}))}>
                        {reservation.workerName ? t('admin.reservation.visit.changeWorker', '작업자 변경') : t('admin.reservation.visit.assignWorker', '작업자 배정')}
                    </button>
                    {reservation.workerName && (
                        <>
                            <input aria-label={t('admin.reservation.visit.completionReport', '완료 보고')} placeholder={t('admin.reservation.visit.workContent', '작업 내용')}
                                   value={report} onChange={(e) => setReport(e.target.value)}/>
                            <input type="file" accept="image/*" multiple aria-label={t('admin.reservation.visit.completionPhoto', '완료 사진')} onChange={(e) => {
                                const files = Array.from(e.target.files ?? []);
                                Promise.all(files.map((file) => fileApi.upload(file)))
                                    .then((uploaded) => setPhotoIds(uploaded.map((file) => file.id)))
                                    .catch(setError);
                            }}/>
                            <button type="button" className="primary" onClick={() => {
                                if (window.confirm(t('admin.reservation.visit.confirmComplete', '작업 완료로 처리할까요? 완료한 예약은 취소할 수 없습니다.'))) {
                                    run(adminApi.reservations.completeWork(reservation.id, {report, photoFileIds: photoIds}));
                                }
                            }}>{t('admin.reservation.visit.complete', '작업 완료')}</button>
                        </>
                    )}
                </div>
            )}
            <ErrorBanner error={error}/>
        </div>
    );
}

function ReturnCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const {t} = useI18n();
    const [deduction, setDeduction] = useState('');
    const [memo, setMemo] = useState('');
    const [error, setError] = useState<unknown>(null);
    if (reservation.returnedAt) {
        return (
            <span className="small">
                {t('admin.reservation.return.returnedAt', '{time} 반납', {time: formatDateTime(reservation.returnedAt)})}
                {reservation.lateDays > 0 && t('admin.reservation.return.lateDays', ' · {days}일 연체', {days: String(reservation.lateDays)})}
                <br/>{t('admin.reservation.return.refunded', '보증금 {amount} 환불', {amount: won(reservation.depositRefunded ?? 0)})}
                {reservation.returnMemo && ` · ${reservation.returnMemo}`}
            </span>
        );
    }
    if (reservation.status !== 'CONFIRMED') {
        return <span className="muted small">-</span>;
    }
    const submit = async () => {
        const amount = Number(deduction || 0);
        if (!window.confirm(t('admin.reservation.return.confirm', '보증금 {deposit} 중 {amount}을 차감하고 반납 처리할까요?',
            {deposit: won(reservation.depositAmount ?? 0), amount: won(amount)}))) {
            return;
        }
        setError(null);
        try {
            await adminApi.reservations.returnItems(reservation.id, {deduction: amount, memo});
            onDone();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="review-cell">
            {reservation.overdue && <span className="badge danger">{t('admin.reservation.return.overdue', '{days}일 연체', {days: String(reservation.lateDays)})}</span>}
            {Number(reservation.expectedLateFee ?? 0) > 0 && <div className="muted small">{t('admin.reservation.return.expectedLateFee', '예상 연체료 {fee}', {fee: won(reservation.expectedLateFee)})}</div>}
            {Number(reservation.unpaidExtensionFee ?? 0) > 0 && <div className="muted small">{t('admin.reservation.return.unpaidExtensionFee', '미결제 연장료 {fee}', {fee: won(reservation.unpaidExtensionFee)})}</div>}
            {Number(reservation.outstandingAmount ?? 0) > 0 && <div className="muted small">{t('admin.reservation.return.outstanding', '미수금 {amount}', {amount: won(reservation.outstandingAmount)})}</div>}
            <input type="number" min={0} aria-label={t('admin.reservation.return.deduction', '보증금 차감액')} placeholder={t('admin.reservation.return.deductionPlaceholder', '차감액(원)')}
                   value={deduction} onChange={(e) => setDeduction(e.target.value)}/>
            <input aria-label={t('admin.reservation.return.memo', '반납 메모')} placeholder={t('admin.reservation.return.memoPlaceholder', '메모(파손 등)')}
                   value={memo} onChange={(e) => setMemo(e.target.value)}/>
            <button type="button" onClick={submit}>{t('admin.reservation.return.submit', '반납 처리')}</button>
            <ExtensionAdminCell reservation={reservation} onDone={onDone}/>
            <ErrorBanner error={error}/>
        </div>
    );
}

function ExtensionAdminCell({reservation, onDone}: { reservation: Reservation; onDone: () => void }) {
    const {t} = useI18n();
    const [newEndAt, setNewEndAt] = useState('');
    const [paid, setPaid] = useState(false);
    const [error, setError] = useState<unknown>(null);

    if (reservation.pendingExtensionId) {
        return (
            <>
                <span className="muted small">{t('admin.reservation.extension.pending', '연장료 {fee} 결제 대기', {fee: won(reservation.pendingExtensionFee ?? 0)})}</span>
                <button type="button" className="danger" onClick={() => {
                    if (window.confirm(t('admin.reservation.extension.confirmCancel', '연장을 취소할까요?'))) {
                        void adminApi.reservations.cancelExtension(reservation.id).then(onDone).catch(setError);
                    }
                }}>{t('admin.reservation.extension.cancel', '연장 취소')}</button>
                <ErrorBanner error={error}/>
            </>
        );
    }
    return (
        <>
            <input type="date" aria-label={t('admin.reservation.extension.newEndAt', '연장할 반납일')} value={newEndAt} onChange={(e) => setNewEndAt(e.target.value)}/>
            <label className="checkbox small">
                <input type="checkbox" checked={paid} onChange={(e) => setPaid(e.target.checked)}/>{t('admin.reservation.extension.paid', '결제됨')}
            </label>
            <button type="button" disabled={!newEndAt} onClick={() => {
                void adminApi.reservations.extend(reservation.id, `${newEndAt}T00:00:00`, paid).then(onDone).catch(setError);
            }}>{t('admin.reservation.extension.extend', '연장')}</button>
            <ErrorBanner error={error}/>
        </>
    );
}

const loadPayments = (query: Query) => adminApi.payments.search(query);

const paymentTargetLabel = (t: T): Record<Payment['targetType'], string> => ({
    ORDER: t('admin.order.title', '주문'), RESERVATION: t('admin.reservation.title', '예약'),
    RESERVATION_EXTENSION: t('admin.payment.targetType.reservationExtension', '예약 연장'),
});

export function PaymentAdminPage() {
    const {t} = useI18n();
    const [detail, setDetail] = useState<number | null>(null);
    const targetLabel = paymentTargetLabel(t);
    return (
        <AdminCrudPage<Payment>
            title={t('admin.payment.title', '결제 내역')}
            description={<>
                <p>{t('admin.payment.description', '결제는 목업 PG로 처리됩니다. 실제 결제 연동 전까지 모든 요청이 즉시 승인됩니다. 결제번호를 그대로 넣어 검색하면 그 결제만 찾습니다.')}</p>
                <RegexBindingSelect target="PAYMENT_NUMBER" label={t('admin.payment.regexLabel', '결제번호 정규식')}/>
            </>}
            load={loadPayments}
            columns={[
                {label: t('admin.payment.column.number', '결제번호'), render: (p) => <code>{p.paymentNumber ?? '-'}</code>},
                {label: t('admin.payment.column.transactionId', '거래 ID'), render: (p) => <code className="small">{p.transactionId}</code>},
                {label: t('admin.payment.column.target', '대상'), render: (p) => (
                    <button type="button" className="link-button" onClick={() => setDetail(detail === p.id ? null : p.id)}>
                        {targetLabel[p.targetType]} #{p.targetId}
                    </button>
                )},
                {label: t('admin.payment.column.amount', '금액'), render: (p) => won(p.amount)},
                {label: t('admin.payment.column.method', '수단'), render: (p) => p.method},
                {label: t('admin.payment.column.status', '상태'), render: (p) => (p.status === 'APPROVED' ? t('admin.payment.approved', '승인') : t('common.cancel', '취소'))},
                {label: t('admin.payment.column.approvedAt', '승인 일시'), render: (p) => formatDateTime(p.approvedAt)},
                {label: t('admin.payment.column.cancel', '취소'), className: 'button-cell', render: (p, reload) => p.status === 'APPROVED' && (
                    <button type="button" className="danger" onClick={() => {
                        if (window.confirm(t('admin.payment.confirmCancel', '결제를 취소할까요? 대상 주문·예약도 함께 취소되고 환불 처리됩니다.'))) {
                            void adminApi.payments.cancel(p.id).then(reload);
                        }
                    }}>{t('common.cancel', '취소')}</button>
                )},
            ]}
            expandedRow={(p, reload) => detail === p.id && <PaymentTargetPanel payment={p} onChange={reload}/>}
        />
    );
}

function PaymentTargetPanel({payment, onChange}: { payment: Payment; onChange: () => void }) {
    const {t} = useI18n();
    const [order, setOrder] = useState<Order | null>(null);
    const [reservation, setReservation] = useState<Reservation | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setOrder(null);
        setReservation(null);
        setError(null);
        if (payment.targetType === 'ORDER') {
            adminApi.orders.get(payment.targetId).then(setOrder).catch(setError);
        } else if (payment.targetType === 'RESERVATION') {
            adminApi.reservations.get(payment.targetId).then(setReservation).catch(setError);
        } else {
            adminApi.reservations.getByExtension(payment.targetId).then(setReservation).catch(setError);
        }
    }, [payment.targetType, payment.targetId]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (payment.targetType === 'ORDER') {
        return order ? <OrderDetailPanel order={order} onChange={onChange}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    if (!reservation) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            {payment.targetType === 'RESERVATION_EXTENSION' && <p className="muted small">{t('admin.payment.extensionNotice', '연장료가 속한 예약입니다.')}</p>}
            <dl className="meta">
                <dt>{t('admin.reservation.column.number', '예약번호')}</dt><dd><code>{reservation.reservationNumber}</code></dd>
                <dt>{t('admin.reservation.column.item', '예약 상품')}</dt><dd>{reservation.reservationItemName}</dd>
                <dt>{t('admin.reservation.column.applicant', '신청자')}</dt><dd>{reservation.applicantName} ({reservation.phoneNumber})</dd>
                <dt>{t('admin.reservation.column.quantity', '수량')}</dt><dd>{reservation.quantity}</dd>
                <dt>{t('admin.reservation.column.amount', '금액')}</dt><dd>{won(reservation.totalPrice)}</dd>
                <dt>{t('admin.reservation.column.status', '상태')}</dt><dd>{reservation.status}</dd>
            </dl>
        </>
    );
}

const loadReviews = (query: Query) => adminApi.reviews(query);

function ReviewPolicyForm() {
    const {t} = useI18n();
    const [policy, setPolicy] = useState<ReviewPolicy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        shopApi.reviewPolicy().then(setPolicy).catch(setError);
    }, []);

    if (!policy) {
        return error ? <ErrorBanner error={error}/> : null;
    }

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            setPolicy(await adminApi.reviewPolicy.save({...policy}));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h2>{t('admin.review.policy.title', '리뷰 작성 설정')}</h2>
            <form onSubmit={submit}>
                <div className="field">
                    <label htmlFor="minimumContentLength">{t('admin.review.policy.minimumContentLength', '리뷰 내용 최소 글자 수')}</label>
                    <input id="minimumContentLength" type="number" min={0} required value={policy.minimumContentLength}
                           onChange={(e) => setPolicy({...policy, minimumContentLength: Number(e.target.value)})}/>
                    <p className="muted small">{t('admin.review.policy.help', '공백을 뺀 글자 수 기준입니다. 0이면 글자 수 제한 없이 등록할 수 있습니다.')}</p>
                </div>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">{t('admin.review.policy.saved', '저장했습니다.')}</p>}
                <div className="toolbar form-actions">
                    <button type="submit" className="primary">{t('common.save', '저장')}</button>
                </div>
            </form>
        </section>
    );
}

export function ReviewAdminPage() {
    const {t} = useI18n();
    const reviewTabs: { value: ReviewTargetType; label: string }[] = [
        {value: 'PRODUCT', label: t('admin.review.tab.product', '상품 리뷰')},
        {value: 'RESERVATION_ITEM', label: t('admin.review.tab.reservationItem', '예약 상품 리뷰')},
    ];
    const [targetType, setTargetType] = useState<ReviewTargetType>('PRODUCT');
    const isProduct = targetType === 'PRODUCT';
    return (
        <>
            <ReviewPolicyForm/>
            <div className="tabs" role="tablist" aria-label={t('admin.review.tabsLabel', '리뷰 종류')}>
                {reviewTabs.map((tab) => (
                    <button key={tab.value} type="button" role="tab" aria-selected={targetType === tab.value}
                            className={targetType === tab.value ? 'tab active' : 'tab'}
                            onClick={() => setTargetType(tab.value)}>
                        {tab.label}
                    </button>
                ))}
            </div>
            <AdminCrudPage<Review>
                key={targetType}
                title={isProduct ? t('admin.review.tab.product', '상품 리뷰') : t('admin.review.tab.reservationItem', '예약 상품 리뷰')}
                searchable={false}
                load={loadReviews}
                extraQuery={{targetType}}
                columns={[
                    isProduct
                        ? {label: t('admin.review.column.product', '상품'), render: (r) => <Link to={`/products/${r.productId}`}>#{r.productId}</Link>}
                        : {label: t('admin.review.column.reservationItem', '예약 상품'), render: (r) => <Link to={`/reservation-items/${r.reservationItemId}`}>#{r.reservationItemId}</Link>},
                    {label: t('admin.review.column.rating', '평점'), render: (r) => '★'.repeat(r.rating)},
                    {label: t('admin.review.column.title', '제목'), render: (r) => r.title},
                    {label: t('admin.review.column.content', '내용'), render: (r) => <HtmlView html={r.contentHtml}/>},
                    {label: t('admin.review.column.author', '작성자'), render: (r) => r.authorName},
                    {label: t('admin.review.column.createdAt', '작성일'), render: (r) => formatDateTime(r.createdAt)},
                ]}
                remove={(r) => shopApi.removeReview(r.id)}
            />
        </>
    );
}
