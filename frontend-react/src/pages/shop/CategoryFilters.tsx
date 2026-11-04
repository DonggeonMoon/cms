import {type FormEvent, useEffect, useState} from 'react';
import {useSearchParams} from 'react-router';
import type {CategoryAttribute, ProductCategory} from '../../api/categories';

/** 카테고리와 속성 조건을 주소(쿼리)에 담아, 새로고침·뒤로가기·공유해도 같은 목록이 나오게 한다. */
export function CategoryFilters({categories}: { categories: ProductCategory[] }) {
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
            <nav className="toolbar" aria-label="카테고리">
                <button type="button" className={selectedId == null ? 'primary' : ''} aria-pressed={selectedId == null}
                        onClick={() => selectCategory(null)}>전체</button>
                {tops.map((category) => (
                    <button key={category.id} type="button" className={topId === category.id ? 'primary' : ''}
                            aria-pressed={selectedId === category.id} onClick={() => selectCategory(category.id)}>{category.name}</button>
                ))}
            </nav>
            {children.length > 0 && (
                <nav className="toolbar sub-categories" aria-label="하위 카테고리">
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
                <input type="number" step="any" aria-label={`${attribute.label} 최소`} placeholder="최소" value={min}
                       onChange={(e) => setMin(e.target.value)}/>
                <span aria-hidden="true"> ~ </span>
                <input type="number" step="any" aria-label={`${attribute.label} 최대`} placeholder="최대" value={max}
                       onChange={(e) => setMax(e.target.value)}/>
                <button type="submit">적용</button>
            </fieldset>
        </form>
    );
}
