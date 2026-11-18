import {Fragment, type FormEvent, useCallback, useEffect, useState} from 'react';
import {adminArea, ADMIN_AREA_VALUES, type AdminAccount, adminAccountApi, type AdminArea, type AdminGrade} from '../../api/admins';
import {useAuth} from '../../auth/AuthContext';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDateTime} from '../../components/format';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';
import {useI18n} from '../../api/i18n';

const AREAS = ADMIN_AREA_VALUES;

export function AdminAccountPage() {
    const {t} = useI18n();
    return <ReauthGate title={t('admin.nav.admins', '관리자 관리')}>{(guard) => <AdminAccounts guard={guard}/>}</ReauthGate>;
}

function AdminAccounts({guard}: { guard: ReauthGuard }) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [admins, setAdmins] = useState<AdminAccount[]>([]);
    const [grades, setGrades] = useState<AdminGrade[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [addingAdmin, setAddingAdmin] = useState(false);
    const [editingGrade, setEditingGrade] = useState<number | 'new' | null>(null);
    const reload = useCallback(() => {
        setError(null);
        Promise.all([guard(adminAccountApi.admins()), guard(adminAccountApi.grades())])
            .then(([loadedAdmins, loadedGrades]) => {
                setAdmins(loadedAdmins);
                setGrades(loadedGrades);
            })
            .catch(setError);
    }, [guard]);
    useEffect(reload, [reload]);

    const run = (pending: Promise<unknown>) => guard(pending).then(reload).catch(setError);

    return (
        <section className="card">
            <h1>{t('admin.nav.admins', '관리자 관리')}</h1>
            <p className="muted">{t('admin.account.description', '최고 관리자만 쓸 수 있습니다. 관리자 등급마다 들어갈 수 있는 관리 영역을 정하고, 관리자에게 등급을 줍니다. 등급이 없는 관리자는 보안 영역을 뺀 모든 영역을 쓸 수 있습니다.')}</p>
            <ErrorBanner error={error}/>

            <h2>{t('admin.account.admins', '관리자')}</h2>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.memberJoin.username', '아이디')}</th><th>{t('admin.board.column.name', '이름')}</th>
                        <th>{t('admin.account.grade', '등급')}</th><th>{t('admin.account.lastLogin', '마지막 로그인')}</th>
                        <th>{t('admin.order.column.status', '상태')}</th><th>{t('common.delete', '삭제')}</th>
                    </tr></thead>
                    <tbody>
                    {admins.map((admin) => (
                        <tr key={admin.id}>
                            <td>{admin.username}{admin.id === user?.id && <span className="muted small"> {t('admin.account.me', '(나)')}</span>}</td>
                            <td>{admin.name}</td>
                            <td>
                                <select aria-label={t('admin.account.gradeOf', '{username} 등급', {username: admin.username})} value={admin.gradeId ?? ''}
                                        onChange={(e) => void run(adminAccountApi.assignGrade(admin.id, e.target.value ? Number(e.target.value) : null))}>
                                    <option value="">{t('admin.account.noGrade', '등급 없음')}</option>
                                    {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                                </select>
                            </td>
                            <td className="small">{formatDateTime(admin.lastLoginDateTime)}</td>
                            <td>{admin.locked
                                ? <button type="button" onClick={() => void run(adminAccountApi.unlock(admin.id))}>{t('admin.account.unlock', '잠금 풀기')}</button>
                                : <span className="muted small">{t('admin.account.normal', '정상')}</span>}</td>
                            <td className="button-cell">
                                {admin.id !== user?.id && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(t('admin.account.confirmDeleteAdmin', '{username} 관리자를 삭제할까요?', {username: admin.username}))) {
                                            void run(adminAccountApi.removeAdmin(admin.id));
                                        }
                                    }}>{t('common.delete', '삭제')}</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <div className="toolbar">
                <button type="button" className="primary" onClick={() => setAddingAdmin(true)}>{t('admin.account.addAdmin', '관리자 추가')}</button>
            </div>
            {addingAdmin && (
                <NewAdminForm grades={grades} onCancel={() => setAddingAdmin(false)}
                              onCreate={(body) => guard(adminAccountApi.createAdmin(body)).then(() => {
                                  setAddingAdmin(false);
                                  reload();
                              })}/>
            )}

            <h2>{t('admin.account.grades', '관리자 등급')}</h2>
            {editingGrade === 'new' && (
                <GradeEditor onCancel={() => setEditingGrade(null)}
                             onSave={(body) => guard(adminAccountApi.createGrade(body)).then(() => {
                                 setEditingGrade(null);
                                 reload();
                             })}/>
            )}
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.account.gradeName', '등급 이름')}</th><th>{t('admin.board.field.description', '설명')}</th>
                        <th>{t('admin.account.managedAreas', '관리 영역')}</th><th>{t('admin.account.adminCount', '관리자 수')}</th><th/>
                    </tr></thead>
                    <tbody>
                    {grades.map((grade) => (
                        <Fragment key={grade.id}>
                            <tr>
                                <td>{grade.name}{grade.superAdmin && <span className="muted small"> {t('admin.account.superAdmin', '(최고 관리자)')}</span>}</td>
                                <td>{grade.description || '-'}</td>
                                <td>{grade.superAdmin ? t('admin.account.allAreas', '전체') : (grade.areas.map((area) => adminArea(t)[area]).join(', ') || '-')}</td>
                                <td>{t('admin.marketing.peopleCount', '{count}명', {count: String(grade.adminCount)})}</td>
                                <td className="button-cell">
                                    <button type="button" onClick={() => setEditingGrade(editingGrade === grade.id ? null : grade.id)}>
                                        {editingGrade === grade.id ? t('common.close', '닫기') : t('common.edit', '수정')}
                                    </button>
                                    {!grade.superAdmin && (
                                        <button type="button" className="danger" onClick={() => {
                                            if (window.confirm(t('admin.account.confirmDeleteGrade', '{name} 등급을 삭제할까요?', {name: grade.name}))) {
                                                void run(adminAccountApi.removeGrade(grade.id));
                                            }
                                        }}>{t('common.delete', '삭제')}</button>
                                    )}
                                </td>
                            </tr>
                            {editingGrade === grade.id && (
                                <tr className="expanded-row">
                                    <td colSpan={5}>
                                        <GradeEditor grade={grade} onCancel={() => setEditingGrade(null)}
                                                     onSave={(body) => guard(adminAccountApi.updateGrade(grade.id, body)).then(() => {
                                                         setEditingGrade(null);
                                                         reload();
                                                     })}/>
                                    </td>
                                </tr>
                            )}
                        </Fragment>
                    ))}
                    {grades.length === 0 && <tr><td colSpan={5} className="muted">{t('admin.account.noGrades', '등록된 등급이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            <div className="toolbar">
                <button type="button" className="primary" onClick={() => setEditingGrade('new')}>{t('admin.account.addGrade', '등급 추가')}</button>
            </div>
        </section>
    );
}

function NewAdminForm({grades, onCreate, onCancel}: {
    grades: AdminGrade[]; onCreate: (body: object) => Promise<void>; onCancel: () => void;
}) {
    const {t} = useI18n();
    const empty = {username: '', password: '', name: '', gradeId: ''};
    const [form, setForm] = useState(empty);
    const [error, setError] = useState<unknown>(null);
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await onCreate({...form, gradeId: form.gradeId ? Number(form.gradeId) : null});
        } catch (e) {
            setError(e);
        }
    };
    return (
        <form className="inline-form" onSubmit={submit} aria-label={t('admin.account.addAdmin', '관리자 추가')}>
            <fieldset>
                <legend>{t('admin.account.addAdmin', '관리자 추가')}</legend>
                <Field label={t('admin.memberJoin.username', '아이디')} name="new-admin-username" required value={form.username} autoComplete="off"
                       onChange={(e) => setForm({...form, username: e.target.value})}/>
                <Field label={t('admin.memberJoin.password', '비밀번호')} name="new-admin-password" type="password" required value={form.password} autoComplete="new-password"
                       onChange={(e) => setForm({...form, password: e.target.value})}/>
                <Field label={t('shop.board.name', '이름')} name="new-admin-name" required value={form.name}
                       onChange={(e) => setForm({...form, name: e.target.value})}/>
                <div className="field">
                    <label htmlFor="new-admin-grade">{t('admin.account.grade', '등급')}</label>
                    <select id="new-admin-grade" value={form.gradeId} onChange={(e) => setForm({...form, gradeId: e.target.value})}>
                        <option value="">{t('admin.account.noGrade', '등급 없음')}</option>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                    </select>
                </div>
                <ErrorBanner error={error}/>
                <div className="toolbar">
                    <button type="submit" className="primary">{t('admin.allowedIp.add', '추가')}</button>
                    <button type="button" onClick={onCancel}>{t('common.cancel', '취소')}</button>
                </div>
            </fieldset>
        </form>
    );
}

function GradeEditor({grade, onSave, onCancel}: {
    grade?: AdminGrade; onSave: (body: object) => Promise<void>; onCancel: () => void;
}) {
    const {t} = useI18n();
    const [name, setName] = useState(grade?.name ?? '');
    const [description, setDescription] = useState(grade?.description ?? '');
    const [areas, setAreas] = useState<AdminArea[]>(grade?.areas ?? []);
    const [error, setError] = useState<unknown>(null);
    const locked = grade?.superAdmin === true;
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await onSave({name, description, areas});
        } catch (e) {
            setError(e);
        }
    };
    const title = grade ? t('admin.account.gradeTitle', '{name} 등급', {name: grade.name}) : t('admin.account.newGrade', '새 등급');
    return (
        <form className="grade-editor" onSubmit={submit} aria-label={title}>
            <fieldset>
                <legend>{title}</legend>
                <Field label={t('admin.account.gradeName', '등급 이름')} name={`grade-name-${grade?.id ?? 'new'}`} required value={name} onChange={(e) => setName(e.target.value)}/>
                <Field label={t('admin.board.field.description', '설명')} name={`grade-description-${grade?.id ?? 'new'}`} value={description}
                       onChange={(e) => setDescription(e.target.value)}/>
                <div className="area-checks" role="group" aria-label={t('admin.account.gradeAreasLabel', '{title} 관리 영역', {title})}>
                    {AREAS.map((area) => (
                        <label key={area} className="checkbox">
                            <input type="checkbox" disabled={locked} checked={locked || areas.includes(area)}
                                   onChange={(e) => setAreas(e.target.checked ? [...areas, area] : areas.filter((a) => a !== area))}/> {adminArea(t)[area]}
                        </label>
                    ))}
                    {locked && <p className="muted small">{t('admin.account.superAdminNotice', '최고 관리자 등급은 모든 영역과 관리자 관리를 쓸 수 있고 삭제할 수 없습니다.')}</p>}
                </div>
                <ErrorBanner error={error}/>
                <div className="toolbar">
                    <button type="submit" className="primary">{grade ? t('common.save', '저장') : t('admin.account.addGrade', '등급 추가')}</button>
                    <button type="button" onClick={onCancel}>{t('common.cancel', '취소')}</button>
                </div>
            </fieldset>
        </form>
    );
}
