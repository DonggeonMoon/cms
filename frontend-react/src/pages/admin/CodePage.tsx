import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {ApiError} from '../../api/client';
import {codeAdminApi} from '../../api/codes';
import type {Code, CodeSaveRequest, Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox, Field} from '../../components/Field';
import {Pagination} from '../../components/Pagination';

const EMPTY_FORM: Omit<CodeSaveRequest, 'parentId'> = {
    order: 1,
    name: '',
    englishName: '',
    description: '',
    isUsing: true,
};

export function CodePage() {
    const [trail, setTrail] = useState<Code[]>([]);
    const [page, setPage] = useState(0);
    const [result, setResult] = useState<Page<Code> | null>(null);
    const [editing, setEditing] = useState<Code | 'new' | null>(null);
    const [error, setError] = useState<unknown>(null);
    const parent = trail.at(-1);

    const load = useCallback(() => {
        codeAdminApi.search({parentId: parent?.id, rootOnly: !parent, page})
            .then(setResult)
            .catch(setError);
    }, [parent, page]);

    useEffect(load, [load]);

    const openChildren = (code: Code) => {
        setTrail([...trail, code]);
        setPage(0);
        setEditing(null);
    };

    const goTo = (depth: number) => {
        setTrail(trail.slice(0, depth));
        setPage(0);
        setEditing(null);
    };

    const remove = async (code: Code) => {
        if (!window.confirm(`'${code.name}' 코드를 삭제할까요?`)) {
            return;
        }
        setError(null);
        try {
            await codeAdminApi.remove(code.id);
            load();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>공통 코드</h1>
            <div className="toolbar list-toolbar">
                {trail.length > 0 && (
                    <nav className="breadcrumb" aria-label="코드 경로">
                        <button type="button" className="link-button" onClick={() => goTo(0)}>최상위</button>
                        {trail.map((code, index) => (
                            <span key={code.id}>
                                {' › '}
                                <button type="button" className="link-button" onClick={() => goTo(index + 1)}>{code.name}</button>
                            </span>
                        ))}
                    </nav>
                )}
                <button type="button" className="primary push-right" onClick={() => setEditing('new')}>
                    {parent ? `'${parent.name}' 하위 코드 추가` : '최상위 코드 추가'}
                </button>
            </div>
            <ErrorBanner error={error}/>
            {editing && (
                <CodeForm parentId={parent?.id ?? null} code={editing === 'new' ? null : editing}
                          onDone={() => {
                              setEditing(null);
                              load();
                          }}
                          onCancel={() => setEditing(null)}/>
            )}
            {result && (
                <>
                    <table>
                        <thead>
                        <tr>
                            <th>순서</th>
                            <th>이름</th>
                            <th>영문 이름</th>
                            <th>설명</th>
                            <th>사용 여부</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {result.content.map((code) => (
                            <tr key={code.id}>
                                <td>{code.order}</td>
                                <td>
                                    <button type="button" className="link-button" onClick={() => openChildren(code)}>
                                        {code.name}
                                    </button>
                                </td>
                                <td>{code.englishName ?? '-'}</td>
                                <td>{code.description ?? '-'}</td>
                                <td>{code.isUsing ? '사용' : <span className="badge">미사용</span>}</td>
                                <td className="actions">
                                    <button type="button" onClick={() => setEditing(code)}>수정</button>
                                    <button type="button" className="danger" onClick={() => remove(code)}>삭제</button>
                                </td>
                            </tr>
                        ))}
                        {result.content.length === 0 && (
                            <tr>
                                <td colSpan={6} className="muted">코드가 없습니다.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={result.page.totalPages} onChange={setPage}/>
                </>
            )}
        </section>
    );
}

interface CodeFormProps {
    parentId: number | null;
    code: Code | null;
    onDone: () => void;
    onCancel: () => void;
}

function CodeForm({parentId, code, onDone, onCancel}: CodeFormProps) {
    const [form, setForm] = useState(code ? {
        order: code.order,
        name: code.name,
        englishName: code.englishName ?? '',
        description: code.description ?? '',
        isUsing: code.isUsing,
    } : EMPTY_FORM);
    const [error, setError] = useState<unknown>(null);
    const errorOf = (field: string) => (error instanceof ApiError ? error.fieldMessage(field) : undefined);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            const body = {...form, parentId: code ? code.parentId : parentId};
            await (code ? codeAdminApi.update(code.id, body) : codeAdminApi.create(body));
            onDone();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <form className="inline-form" onSubmit={handleSubmit}>
            <h2>{code ? '코드 수정' : '코드 추가'}</h2>
            <Checkbox label="사용" name="isUsing" checked={form.isUsing}
                      onChange={(e) => setForm({...form, isUsing: e.target.checked})}/>
            <div className="row">
                <Field label="이름" name="name" required value={form.name} error={errorOf('name')}
                       onChange={(e) => setForm({...form, name: e.target.value})}/>
                <Field label="영문 이름" name="englishName" value={form.englishName} error={errorOf('englishName')}
                       onChange={(e) => setForm({...form, englishName: e.target.value})}/>
                <Field label="순서" name="order" type="number" min={1} value={form.order} error={errorOf('order')}
                       onChange={(e) => setForm({...form, order: Number(e.target.value)})}/>
            </div>
            <Field label="설명" name="description" value={form.description} error={errorOf('description')}
                   onChange={(e) => setForm({...form, description: e.target.value})}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">저장</button>
                <button type="button" onClick={onCancel}>취소</button>
            </div>
        </form>
    );
}
