import {type FormEvent, Fragment, type ReactNode, useCallback, useEffect, useState} from 'react';
import type {Query} from '../api/client';
import type {Page} from '../api/types';
import {ErrorBanner} from './ErrorBanner';
import {Pagination} from './Pagination';
import {type FieldSpec, type FormValues, SchemaForm} from './SchemaForm';

export interface Column<T> {
    label: string;
    render: (item: T, reload: () => void) => ReactNode;
    className?: string;
}

interface Props<T extends { id: number }> {
    title: string;
    columns: Column<T>[];
    fields?: FieldSpec[];
    load: (query: Query) => Promise<Page<T> | T[]>;
    emptyForm?: FormValues;
    toForm?: (item: T) => FormValues | Promise<FormValues>;
    create?: (values: FormValues) => Promise<void>;
    update?: (item: T, values: FormValues) => Promise<void>;
    remove?: (item: T) => Promise<void>;
    searchable?: boolean;
    extraQuery?: Query;
    filters?: ReactNode;
    actions?: (item: T, reload: () => void) => ReactNode;
    expandedRow?: (item: T, reload: () => void) => ReactNode;
    description?: ReactNode;
}

function asPage<T>(result: Page<T> | T[]): Page<T> {
    return Array.isArray(result)
        ? {content: result, page: {size: result.length, number: 0, totalElements: result.length, totalPages: 1}}
        : result;
}

export function AdminCrudPage<T extends { id: number }>(props: Props<T>) {
    const {title, columns, fields, load, emptyForm, toForm, create, update, remove, searchable = true,
        extraQuery, filters, actions, expandedRow, description} = props;
    const [keyword, setKeyword] = useState('');
    const [input, setInput] = useState('');
    const [page, setPage] = useState(0);
    const [result, setResult] = useState<Page<T> | null>(null);
    const [editing, setEditing] = useState<{ item: T | null; values: FormValues } | null>(null);
    const [error, setError] = useState<unknown>(null);

    const extraKey = JSON.stringify(extraQuery ?? {});
    const reload = useCallback(() => {
        setError(null);
        load({keyword, page, size: 20, ...(JSON.parse(extraKey) as Query)})
            .then((r) => setResult(asPage(r)))
            .catch(setError);
    }, [load, keyword, page, extraKey]);

    useEffect(reload, [reload]);

    const startEdit = async (item: T) => {
        try {
            setEditing({item, values: toForm ? await toForm(item) : {...item}});
        } catch (e) {
            setError(e);
        }
    };

    const handleRemove = async (item: T) => {
        if (!remove || !window.confirm('삭제할까요?')) {
            return;
        }
        try {
            await remove(item);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const handleSearch = (event: FormEvent) => {
        event.preventDefault();
        setPage(0);
        setKeyword(input);
    };

    return (
        <section className="card">
            <h1>{title}</h1>
            {description && <div className="muted page-description">{description}</div>}
            <div className="toolbar list-toolbar">
                {searchable && (
                    <form className="toolbar inline" onSubmit={handleSearch}>
                        <input aria-label="검색어" placeholder="검색어" value={input} onChange={(e) => setInput(e.target.value)}/>
                        <button type="submit">검색</button>
                    </form>
                )}
                {filters}
                {create && fields && (
                    <button type="button" className="primary push-right"
                            onClick={() => setEditing({item: null, values: {...emptyForm}})}>
                        새로 등록
                    </button>
                )}
            </div>
            {editing && fields && (
                <SchemaForm
                    key={editing.item?.id ?? 'new'}
                    title={editing.item ? '수정' : '새로 등록'}
                    fields={fields}
                    initial={editing.values}
                    onCancel={() => setEditing(null)}
                    onSubmit={async (values) => {
                        if (editing.item) {
                            await update?.(editing.item, values);
                        } else {
                            await create?.(values);
                        }
                        setEditing(null);
                        reload();
                    }}
                />
            )}
            <ErrorBanner error={error}/>
            {result && (
                <>
                    <p className="muted">총 {result.page.totalElements}건</p>
                    <div className="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                {columns.map((column) => <th key={column.label}>{column.label}</th>)}
                                <th/>
                            </tr>
                            </thead>
                            <tbody>
                            {result.content.map((item) => {
                                const expanded = expandedRow?.(item, reload);
                                return (
                                    <Fragment key={item.id}>
                                        <tr>
                                            {columns.map((column) => <td key={column.label} className={column.className}>{column.render(item, reload)}</td>)}
                                            <td className="actions">
                                                {actions?.(item, reload)}
                                                {update && fields && <button type="button" onClick={() => startEdit(item)}>수정</button>}
                                                {remove && <button type="button" className="danger" onClick={() => handleRemove(item)}>삭제</button>}
                                            </td>
                                        </tr>
                                        {expanded && (
                                            <tr className="expanded-row">
                                                <td colSpan={columns.length + 1}>{expanded}</td>
                                            </tr>
                                        )}
                                    </Fragment>
                                );
                            })}
                            {result.content.length === 0 && (
                                <tr>
                                    <td colSpan={columns.length + 1} className="muted">데이터가 없습니다.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                    <Pagination page={page} totalPages={result.page.totalPages} onChange={setPage}/>
                </>
            )}
        </section>
    );
}

export function YesNo({value}: { value: boolean }) {
    return value ? <span className="badge ok">예</span> : <span className="badge">아니오</span>;
}

export const won = (value: number | null | undefined) =>
    value == null ? '-' : `${Number(value).toLocaleString('ko-KR')}원`;
