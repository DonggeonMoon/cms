import {type FormEvent, useEffect, useState} from 'react';
import {useSearchParams} from 'react-router';
import type {Category, CategoryAttribute} from '../../api/categories';
import {useI18n} from '../../api/i18n';

export function CategoryFilters({categories}: { categories: Category[] }) {
    const {t} = useI18n();
    const [params, setParams] = useSearchParams();
    const selectedId = params.get('categoryId') ? Number(params.get('categoryId')) : null;
    const selected = categories.find((category) => category.id === selectedId) ?? null;
    const topId = selected?.parentId ?? selected?.id ?? null;
    const tops = categories.filter((category) => category.parentId == null);
    const children = topId == null ? [] : categories.filter((category) => category.parentId === topId);
    const filters = (selected?.effectiveAttributes ?? []).filter((attribute) => attribute.filterable && attribute.type !== 'TEXT');

    const update = (change: (next: URLSearchParams) => void) => {
        const next = new URLSearchParams(params);
        next.delete('page');
        change(next);
        setParams(next);
    };
    const selectCategory = (id: number | null) => update((next) => {
        [...next.keys()].filter((key) => /^(attr|min|max)\./.test(key)).forEach((key) => next.delete(key));
        if (id == null) {
            next.delete('categoryId');
        } else {
            next.set('categoryId', String(id));
        }
    });

    return (
        <div className="category-filters">
            <nav className="toolbar" aria-label={t('shop.categoryFilters.category', '카테고리')}>
                <button type="button" className={selectedId == null ? 'primary' : ''} aria-pressed={selectedId == null}
                        onClick={() => selectCategory(null)}>{t('admin.account.allAreas', '전체')}</button>
                {tops.map((category) => (
                    <button key={category.id} type="button" className={topId === category.id ? 'primary' : ''}
                            aria-pressed={selectedId === category.id} onClick={() => selectCategory(category.id)}>{category.name}</button>
                ))}
            </nav>
            {children.length > 0 && (
                <nav className="toolbar sub-categories" aria-label={t('shop.categoryFilters.subCategory', '하위 카테고리')}>
                    {children.map((category) => (
                        <button key={category.id} type="button" className={selectedId === category.id ? 'primary' : ''}
                                aria-pressed={selectedId === category.id} onClick={() => selectCategory(category.id)}>{category.name}</button>
                    ))}
                </nav>
            )}
            {filters.length > 0 && (
                <div className="attribute-filters">
                    {filters.map((attribute) => attribute.type === 'SELECT'
                        ? <ChoiceFilter key={attribute.key} attribute={attribute} params={params} update={update}/>
                        : <RangeFilter key={attribute.key} attribute={attribute} params={params} update={update}/>)}
                </div>
            )}
        </div>
    );
}

interface FilterProps {
    attribute: CategoryAttribute;
    params: URLSearchParams;
    update: (change: (next: URLSearchParams) => void) => void;
}

function ChoiceFilter({attribute, params, update}: FilterProps) {
    const name = `attr.${attribute.key}`;
    const picked = params.getAll(name);
    const toggle = (option: string, on: boolean) => update((next) => {
        const values = on ? [...picked, option] : picked.filter((value) => value !== option);
        next.delete(name);
        values.forEach((value) => next.append(name, value));
    });
    return (
        <fieldset className="attribute-filter">
            <legend>{attribute.label}</legend>
            {(attribute.options ?? []).map((option) => (
                <label key={option} className="checkbox">
                    <input type="checkbox" checked={picked.includes(option)} onChange={(e) => toggle(option, e.target.checked)}/> {option}
                </label>
            ))}
        </fieldset>
    );
}

function RangeFilter({attribute, params, update}: FilterProps) {
    const {t} = useI18n();
    const minName = `min.${attribute.key}`;
    const maxName = `max.${attribute.key}`;
    const [min, setMin] = useState(params.get(minName) ?? '');
    const [max, setMax] = useState(params.get(maxName) ?? '');
    useEffect(() => {
        setMin(params.get(minName) ?? '');
        setMax(params.get(maxName) ?? '');
    }, [params, minName, maxName]);
    const apply = (event: FormEvent) => {
        event.preventDefault();
        update((next) => {
            if (min.trim()) next.set(minName, min.trim()); else next.delete(minName);
            if (max.trim()) next.set(maxName, max.trim()); else next.delete(maxName);
        });
    };
    const unit = attribute.unit ? ` (${attribute.unit})` : '';
    return (
        <form className="attribute-filter" onSubmit={apply}>
            <fieldset>
                <legend>{attribute.label}{unit}</legend>
                <input type="number" step="any" aria-label={t('shop.categoryFilters.minOf', '{label} 최소', {label: attribute.label})} placeholder={t('shop.categoryFilters.min', '최소')} value={min}
                       onChange={(e) => setMin(e.target.value)}/>
                <span aria-hidden="true"> ~ </span>
                <input type="number" step="any" aria-label={t('shop.categoryFilters.maxOf', '{label} 최대', {label: attribute.label})} placeholder={t('shop.categoryFilters.max', '최대')} value={max}
                       onChange={(e) => setMax(e.target.value)}/>
                <button type="submit">{t('shop.categoryFilters.apply', '적용')}</button>
            </fieldset>
        </form>
    );
}
