import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {ApiError} from '../../api/client';
import {codeAdminApi} from '../../api/codes';
import type {Code, CodeSaveRequest, Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox, Field} from '../../components/Field';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

const EMPTY_FORM: Omit<CodeSaveRequest, 'parentId'> = {
    order: 1,
    name: '',
    englishName: '',
    description: '',
    isUsing: true,
};

export function CodePage() {
    const {t} = useI18n();
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
        if (!window.confirm(t('admin.code.confirmDelete', "'{name}' 코드를 삭제할까요?", {name: code.name}))) {
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
            <h1>{t('admin.code.title', '공통 코드')}</h1>
            <div className="muted page-description">
                <p>{t('admin.code.description', '분류·목록용 코드를 직접 만들어 여러 단계(최상위 → 하위)로 묶어 관리합니다. 이름을 누르면 그 코드의 하위 코드로 들어갑니다. 사용 중인 코드만 공개 API(')}<code>GET /api/codes?parentId=</code>{t('admin.code.description2', ')로 내려가므로, 웹사이트나 외부 연동에서 고정되지 않은 선택 목록(지역, 분류 등)이 필요할 때 여기서 코드를 만들어 가져다 쓸 수 있습니다.')}</p>
            </div>
            <div className="toolbar list-toolbar">
                {trail.length > 0 && (
                    <nav className="breadcrumb" aria-label={t('admin.code.path', '코드 경로')}>
                        <button type="button" className="link-button" onClick={() => goTo(0)}>{t('admin.code.root', '최상위')}</button>
                        {trail.map((code, index) => (
                            <span key={code.id}>
                                {' › '}
                                <button type="button" className="link-button" onClick={() => goTo(index + 1)}>{code.name}</button>
                            </span>
                        ))}
                    </nav>
                )}
                <button type="button" className="primary push-right" onClick={() => setEditing('new')}>
                    {parent ? t('admin.code.addChildOf', "'{name}' 하위 코드 추가", {name: parent.name}) : t('admin.code.addRoot', '최상위 코드 추가')}
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
                            <th>{t('admin.code.order', '순서')}</th>
                            <th>{t('admin.board.column.name', '이름')}</th>
                            <th>{t('admin.code.englishName', '영문 이름')}</th>
                            <th>{t('admin.board.field.description', '설명')}</th>
                            <th>{t('admin.code.isUsing', '사용 여부')}</th>
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
                                <td>{code.isUsing ? t('admin.board.field.isUsing', '사용') : <span className="badge">{t('admin.code.notUsing', '미사용')}</span>}</td>
                                <td className="actions">
                                    <button type="button" onClick={() => setEditing(code)}>{t('common.edit', '수정')}</button>
                                    <button type="button" className="danger" onClick={() => remove(code)}>{t('common.delete', '삭제')}</button>
                                </td>
                            </tr>
                        ))}
                        {result.content.length === 0 && (
                            <tr>
                                <td colSpan={6} className="muted">{t('admin.code.none', '코드가 없습니다.')}</td>
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
    const {t} = useI18n();
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
            <h2>{code ? t('admin.code.editCode', '코드 수정') : t('admin.code.addCode', '코드 추가')}</h2>
            <Checkbox label={t('admin.board.field.isUsing', '사용')} name="isUsing" checked={form.isUsing}
                      onChange={(e) => setForm({...form, isUsing: e.target.checked})}/>
            <div className="row">
                <Field label={t('admin.board.column.name', '이름')} name="name" required value={form.name} error={errorOf('name')}
                       onChange={(e) => setForm({...form, name: e.target.value})}/>
                <Field label={t('admin.code.englishName', '영문 이름')} name="englishName" value={form.englishName} error={errorOf('englishName')}
                       onChange={(e) => setForm({...form, englishName: e.target.value})}/>
                <Field label={t('admin.code.order', '순서')} name="order" type="number" min={1} value={form.order} error={errorOf('order')}
                       onChange={(e) => setForm({...form, order: Number(e.target.value)})}/>
            </div>
            <Field label={t('admin.board.field.description', '설명')} name="description" value={form.description} error={errorOf('description')}
                   onChange={(e) => setForm({...form, description: e.target.value})}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">{t('common.save', '저장')}</button>
                <button type="button" onClick={onCancel}>{t('common.cancel', '취소')}</button>
            </div>
        </form>
    );
}
