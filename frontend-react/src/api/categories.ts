import {useEffect, useState} from 'react';
import {request} from './client';
import type {FieldSpec, FormValues} from '../components/SchemaForm';

export type AttributeType = 'SELECT' | 'NUMBER' | 'TEXT';

type TFunc = (key: string, fallback: string, values?: Record<string, string>) => string;

export const attributeType = (t: TFunc): Record<AttributeType, string> => ({
    SELECT: t('label.attributeType.SELECT', '선택형'), NUMBER: t('label.attributeType.NUMBER', '숫자'), TEXT: t('label.attributeType.TEXT', '텍스트'),
});

export interface CategoryAttribute {
    key: string;
    label: string;
    type: AttributeType;
    options: string[] | null;
    unit: string | null;
    filterable: boolean;
}

export interface Category {
    id: number;
    name: string;
    parentId: number | null;
    sequence: number;
    attributes: CategoryAttribute[];
    effectiveAttributes: CategoryAttribute[];
    itemCount: number;
}

export interface Spec {
    key: string;
    label: string;
    value: string | number;
    unit: string | null;
}

export const categoryApi = {
    list: () => request<Category[]>('/api/categories'),
    create: (body: unknown) => request<void>('/api/admin/categories', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<void>(`/api/admin/categories/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/categories/${id}`, {method: 'DELETE'}),
};

/** 상품·예약 상품·신청·지원·스트리밍/교육과정이 공용으로 쓰는 하나의 카테고리 목록. */
export function useCategories(): Category[] {
    const [categories, setCategories] = useState<Category[]>([]);
    useEffect(() => {
        categoryApi.list().then((loaded) => setCategories(Array.isArray(loaded) ? loaded : [])).catch(() => setCategories([]));
    }, []);
    return categories;
}

export const categoryLabel = (categories: Category[], category: Category) => {
    const parent = categories.find((c) => c.id === category.parentId);
    return parent ? `${parent.name} > ${category.name}` : category.name;
};

/** 예약 상품·신청 폼·스트리밍/교육과정처럼 속성 없이 카테고리만 고르면 되는 입력 폼용 필드. */
export function categorySelectField(categories: Category[], t: TFunc): FieldSpec[] {
    if (categories.length === 0) {
        return [];
    }
    return [{name: 'categoryId', label: t('label.category.field', '카테고리'), type: 'select', wide: true,
        options: categories.map((category) => ({value: String(category.id), label: categoryLabel(categories, category)}))}];
}

const attributeFieldName = (categoryId: number, key: string) => `attr:${categoryId}:${key}`;

/** 상품 입력 폼: 카테고리 선택과, 고른 카테고리의 속성 입력칸. 카테고리마다 칸 이름을 나눠 다른 카테고리 값과 섞이지 않게 한다. */
export function productCategoryFields(categories: Category[], t: TFunc): FieldSpec[] {
    if (categories.length === 0) {
        return [];
    }
    return [
        {name: 'categoryId', label: t('label.category.field', '카테고리'), type: 'select', wide: true,
            options: categories.map((category) => ({value: String(category.id), label: categoryLabel(categories, category)}))},
        ...categories.flatMap((category) => category.effectiveAttributes.map((attribute): FieldSpec => ({
            name: attributeFieldName(category.id, attribute.key),
            label: attribute.unit ? `${attribute.label} (${attribute.unit})` : attribute.label,
            type: attribute.type === 'SELECT' ? 'select' : attribute.type === 'NUMBER' ? 'number' : 'text',
            options: attribute.type === 'SELECT' ? (attribute.options ?? []).map((option) => ({value: option, label: option})) : undefined,
            visibleWhen: (values) => String(values.categoryId ?? '') === String(category.id),
        }))),
    ];
}

export function attributesToForm(categoryId: number | null, attributes: Record<string, unknown> | undefined): FormValues {
    if (categoryId == null || !attributes) {
        return {};
    }
    return Object.fromEntries(Object.entries(attributes).map(([key, value]) => [attributeFieldName(categoryId, key), value]));
}

export function splitCategoryValues(values: FormValues): { rest: FormValues; categoryId: number | null; attributes: Record<string, unknown> } {
    const categoryId = values.categoryId ? Number(values.categoryId) : null;
    const prefix = categoryId == null ? null : `attr:${categoryId}:`;
    const rest: FormValues = {};
    const attributes: Record<string, unknown> = {};
    for (const [name, value] of Object.entries(values)) {
        if (!name.startsWith('attr:')) {
            rest[name] = value;
        } else if (prefix && name.startsWith(prefix) && value !== '' && value != null) {
            attributes[name.slice(prefix.length)] = value;
        }
    }
    return {rest, categoryId, attributes};
}
