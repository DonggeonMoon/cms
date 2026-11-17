import {useCallback, useMemo, useState} from 'react';
import {ATTRIBUTE_TYPE, type Category, categoryApi, type CategoryDomain, categoryLabel} from '../../api/categories';
import type {SiteFeature} from '../../api/domain';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {useSite} from '../../components/SiteContext';

const DOMAIN_TABS: { value: CategoryDomain; label: string; feature: SiteFeature }[] = [
    {value: 'PRODUCT', label: '상품', feature: 'PRODUCT'},
    {value: 'RESERVATION_ITEM', label: '예약 상품', feature: 'PROGRAM'},
    {value: 'APPLICATION', label: '신청·지원', feature: 'APPLICATION'},
    {value: 'COURSE', label: '스트리밍/교육과정', feature: 'MEDIA'},
];

export function CategoryAdminPage() {
    const {isEnabled} = useSite();
    const tabs = DOMAIN_TABS.filter((t) => isEnabled(t.feature));
    const [domain, setDomain] = useState<CategoryDomain | null>(null);
    const activeDomain = (domain && tabs.some((t) => t.value === domain) ? domain : tabs[0]?.value) ?? 'PRODUCT';

    return (
        <>
            {tabs.length > 1 && (
                <div className="tabs" role="tablist" aria-label="카테고리 종류">
                    {tabs.map((t) => (
                        <button key={t.value} type="button" role="tab" aria-selected={activeDomain === t.value}
                                className={activeDomain === t.value ? 'tab active' : 'tab'} onClick={() => setDomain(t.value)}>
                            {t.label}
                        </button>
                    ))}
                </div>
            )}
            <CategoryCrudPage domain={activeDomain} key={activeDomain}/>
        </>
    );
}

const toBody = (values: FormValues) => ({...values, parentId: values.parentId ? Number(values.parentId) : null});

function CategoryCrudPage({domain}: { domain: CategoryDomain }) {
    const [categories, setCategories] = useState<Category[]>([]);
    const load = useCallback(() => categoryApi.list(domain).then((loaded) => {
        setCategories(loaded);
        return loaded;
    }), [domain]);
    const isProduct = domain === 'PRODUCT';
    const fields = useMemo<FieldSpec[]>(() => [
        {name: 'name', label: '이름', required: true, wide: true},
        {name: 'parentId', label: '상위 카테고리', type: 'select', wide: true,
            options: categories.filter((c) => c.parentId == null).map((c) => ({value: String(c.id), label: c.name})),
            help: '두 단계까지 만들 수 있습니다. 하위 카테고리는 상위 카테고리의 속성을 물려받습니다.'},
        {name: 'sequence', label: '정렬 순서', type: 'number', min: 0, wide: true, help: '작을수록 앞에 나옵니다.'},
        ...(isProduct ? [{name: 'attributes', label: '속성', type: 'categoryAttributes',
            help: '상품마다 입력할 사양입니다. "검색 조건"을 켠 선택형·숫자 속성은 상품 목록에서 조건으로 거를 수 있습니다.'} as FieldSpec] : []),
    ], [categories, isProduct]);

    return (
        <AdminCrudPage<Category>
            title="카테고리"
            description="항목을 분류합니다. 상품 카테고리는 카테고리별로 상품 사양(속성)도 정할 수 있습니다."
            searchable={false}
            load={load}
            fields={fields}
            emptyForm={{sequence: 0, attributes: []}}
            toForm={(category) => ({...category, parentId: category.parentId == null ? '' : String(category.parentId)})}
            create={(values) => categoryApi.create({...toBody(values), domain})}
            update={(category, values) => categoryApi.update(category.id, {...toBody(values), domain})}
            remove={(category) => categoryApi.remove(category.id)}
            columns={[
                {label: '이름', render: (c) => categoryLabel(categories, c)},
                ...(isProduct ? [{label: '속성', render: (c: Category) => c.effectiveAttributes.length === 0 ? '-'
                        : c.effectiveAttributes.map((a) => `${a.label}(${ATTRIBUTE_TYPE[a.type]})`).join(', ')}] : []),
                {label: '사용 중인 항목 수', render: (c) => c.itemCount},
                {label: '순서', render: (c) => c.sequence},
            ]}
        />
    );
}
