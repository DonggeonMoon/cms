import {useMemo, useState} from 'react';
import {attributeType, type Category, categoryApi, categoryLabel} from '../../api/categories';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {useI18n} from '../../api/i18n';

const toBody = (values: FormValues) => ({...values, parentId: values.parentId ? Number(values.parentId) : null});

export function CategoryAdminPage() {
    const {t} = useI18n();
    const [categories, setCategories] = useState<Category[]>([]);
    const load = () => categoryApi.list().then((loaded) => {
        setCategories(loaded);
        return loaded;
    });
    const fields = useMemo<FieldSpec[]>(() => [
        {name: 'name', label: t('admin.board.column.name', '이름'), required: true, wide: true},
        {name: 'parentId', label: t('admin.category.parent', '상위 카테고리'), type: 'select', wide: true,
            options: categories.filter((c) => c.parentId == null).map((c) => ({value: String(c.id), label: c.name})),
            help: t('admin.category.parent.help', '두 단계까지 만들 수 있습니다. 하위 카테고리는 상위 카테고리의 속성을 물려받습니다.')},
        {name: 'sequence', label: t('admin.category.sequence', '정렬 순서'), type: 'number', min: 0, wide: true, help: t('admin.category.sequence.help', '작을수록 앞에 나옵니다.')},
        {name: 'attributes', label: t('admin.categoryAttributes.title', '속성'), type: 'categoryAttributes',
            help: t('admin.category.attributes.help', '상품마다 입력할 사양입니다. "검색 조건"을 켠 선택형·숫자 속성은 상품 목록에서 조건으로 거를 수 있습니다.')},
    ], [categories, t]);

    return (
        <AdminCrudPage<Category>
            title={t('admin.nav.categories', '카테고리')}
            description={t('admin.category.description', '상품·예약 상품·신청·지원·스트리밍/교육과정이 함께 쓰는 분류입니다. 상품에 쓸 때는 카테고리별로 상품 사양(속성)도 정할 수 있습니다.')}
            searchable={false}
            load={load}
            fields={fields}
            emptyForm={{sequence: 0, attributes: []}}
            toForm={(category) => ({...category, parentId: category.parentId == null ? '' : String(category.parentId)})}
            create={(values) => categoryApi.create(toBody(values))}
            update={(category, values) => categoryApi.update(category.id, toBody(values))}
            remove={(category) => categoryApi.remove(category.id)}
            columns={[
                {label: t('admin.board.column.name', '이름'), render: (c) => categoryLabel(categories, c)},
                {label: t('admin.categoryAttributes.title', '속성'), render: (c) => c.effectiveAttributes.length === 0 ? '-'
                        : c.effectiveAttributes.map((a) => `${a.label}(${attributeType(t)[a.type]})`).join(', ')},
                {label: t('admin.category.itemCount', '사용 중인 항목 수'), render: (c) => c.itemCount},
                {label: t('admin.category.order', '순서'), render: (c) => c.sequence},
            ]}
        />
    );
}
