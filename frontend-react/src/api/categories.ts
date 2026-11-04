import {useEffect, useState} from 'react';
import {request} from './client';
import type {FieldSpec, FormValues} from '../components/SchemaForm';

export type AttributeType = 'SELECT' | 'NUMBER' | 'TEXT';

export const ATTRIBUTE_TYPE: Record<AttributeType, string> = {SELECT: '선택형', NUMBER: '숫자', TEXT: '텍스트'};

export interface CategoryAttribute {
    key: string;
    label: string;
    type: AttributeType;
    options: string[] | null;
    unit: string | null;
    filterable: boolean;
}

export interface ProductCategory {
    id: number;
    name: string;
    parentId: number | null;
    sequence: number;
    attributes: CategoryAttribute[];
    effectiveAttributes: CategoryAttribute[];
    productCount: number;
}

export interface Spec {
    key: string;
    label: string;
    value: string | number;
    unit: string | null;
}

export const categoryApi = {
    list: () => request<ProductCategory[]>('/api/product-categories'),
    create: (body: unknown) => request<void>('/api/admin/product-categories', {method: 'POST', body}),
    update: (id: number, body: unknown) => request<void>(`/api/admin/product-categories/${id}`, {method: 'PUT', body}),
    remove: (id: number) => request<void>(`/api/admin/product-categories/${id}`, {method: 'DELETE'}),
};

export function useCategories(): ProductCategory[] {
    const [categories, setCategories] = useState<ProductCategory[]>([]);
    useEffect(() => {
        categoryApi.list().then((loaded) => setCategories(Array.isArray(loaded) ? loaded : [])).catch(() => setCategories([]));
    }, []);
    return categories;
}

export const categoryLabel = (categories: ProductCategory[], category: ProductCategory) => {
    const parent = categories.find((c) => c.id === category.parentId);
    return parent ? `${parent.name} > ${category.name}` : category.name;
};

const attributeFieldName = (categoryId: number, key: string) => `attr:${categoryId}:${key}`;

/** 상품 입력 폼: 카테고리 선택과, 고른 카테고리의 속성 입력칸. 카테고리마다 칸 이름을 나눠 다른 카테고리 값과 섞이지 않게 한다. */
export function productCategoryFields(categories: ProductCategory[]): FieldSpec[] {
    if (categories.length === 0) {
        return [];
    }
    return [
        {name: 'categoryId', label: '카테고리', type: 'select', wide: true,
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
