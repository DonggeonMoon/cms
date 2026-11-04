import {useCallback, useMemo, useState} from 'react';
import {ATTRIBUTE_TYPE, categoryApi, categoryLabel, type ProductCategory} from '../../api/categories';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const toBody = (values: FormValues) => ({...values, parentId: values.parentId ? Number(values.parentId) : null});

export function CategoryAdminPage() {
    const [categories, setCategories] = useState<ProductCategory[]>([]);
    const load = useCallback(() => categoryApi.list().then((loaded) => {
        setCategories(loaded);
        return loaded;
    }), []);
    const fields = useMemo<FieldSpec[]>(() => [
        {name: 'name', label: '이름', required: true, wide: true},
        {name: 'parentId', label: '상위 카테고리', type: 'select', wide: true,
            options: categories.filter((c) => c.parentId == null).map((c) => ({value: String(c.id), label: c.name})),
            help: '두 단계까지 만들 수 있습니다. 하위 카테고리는 상위 카테고리의 속성을 물려받습니다.'},
        {name: 'sequence', label: '정렬 순서', type: 'number', wide: true, help: '작을수록 앞에 나옵니다.'},
        {name: 'attributes', label: '속성', type: 'categoryAttributes',
            help: '상품마다 입력할 사양입니다. "검색 조건"을 켠 선택형·숫자 속성은 상품 목록에서 조건으로 거를 수 있습니다.'},
    ], [categories]);

    return (
        <AdminCrudPage<ProductCategory>
            title="상품 카테고리"
            description="상품을 분류하고, 카테고리별로 상품 사양(속성)을 정합니다."
            searchable={false}
            load={load}
            fields={fields}
            emptyForm={{sequence: 0, attributes: []}}
            toForm={(category) => ({...category, parentId: category.parentId == null ? '' : String(category.parentId)})}
            create={(values) => categoryApi.create(toBody(values))}
            update={(category, values) => categoryApi.update(category.id, toBody(values))}
            remove={(category) => categoryApi.remove(category.id)}
            columns={[
                {label: '이름', render: (c) => categoryLabel(categories, c)},
                {label: '속성', render: (c) => c.effectiveAttributes.length === 0 ? '-'
                        : c.effectiveAttributes.map((a) => `${a.label}(${ATTRIBUTE_TYPE[a.type]})`).join(', ')},
                {label: '상품 수', render: (c) => c.productCount},
                {label: '순서', render: (c) => c.sequence},
            ]}
        />
    );
}
