import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {ADMIN_AREA, type AdminAccount, adminAccountApi, type AdminArea, type AdminGrade} from '../../api/admins';
import {useAuth} from '../../auth/AuthContext';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';
import {formatDateTime} from '../../components/format';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';

const AREAS = Object.keys(ADMIN_AREA) as AdminArea[];

export function AdminAccountPage() {
    return <ReauthGate title="관리자 관리">{(guard) => <AdminAccounts guard={guard}/>}</ReauthGate>;
}

function AdminAccounts({guard}: { guard: ReauthGuard }) {
    const {user} = useAuth();
    const [admins, setAdmins] = useState<AdminAccount[]>([]);
    const [grades, setGrades] = useState<AdminGrade[]>([]);
    const [error, setError] = useState<unknown>(null);
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
            <h1>관리자 관리</h1>
            <p className="muted">최고 관리자만 쓸 수 있습니다. 관리자 등급마다 들어갈 수 있는 관리 영역을 정하고, 관리자에게 등급을 줍니다.
                등급이 없는 관리자는 보안 영역을 뺀 모든 영역을 쓸 수 있습니다.</p>
            <ErrorBanner error={error}/>

            <h2>관리자</h2>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>아이디</th><th>이름</th><th>등급</th><th>마지막 로그인</th><th>상태</th><th>삭제</th></tr></thead>
                    <tbody>
                    {admins.map((admin) => (
                        <tr key={admin.id}>
                            <td>{admin.username}{admin.id === user?.id && <span className="muted small"> (나)</span>}</td>
                            <td>{admin.name}</td>
                            <td>
                                <select aria-label={`${admin.username} 등급`} value={admin.gradeId ?? ''}
                                        onChange={(e) => void run(adminAccountApi.assignGrade(admin.id, e.target.value ? Number(e.target.value) : null))}>
                                    <option value="">등급 없음</option>
                                    {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                                </select>
                            </td>
                            <td className="small">{formatDateTime(admin.lastLoginDateTime)}</td>
                            <td>{admin.locked
                                ? <button type="button" onClick={() => void run(adminAccountApi.unlock(admin.id))}>잠금 풀기</button>
                                : <span className="muted small">정상</span>}</td>
                            <td className="button-cell">
                                {admin.id !== user?.id && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(`${admin.username} 관리자를 삭제할까요?`)) {
                                            void run(adminAccountApi.removeAdmin(admin.id));
                                        }
                                    }}>삭제</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <NewAdminForm grades={grades} onCreate={(body) => guard(adminAccountApi.createAdmin(body)).then(reload)}/>

            <h2>관리자 등급</h2>
            {grades.map((grade) => (
                <GradeEditor key={grade.id} grade={grade}
                             onSave={(body) => guard(adminAccountApi.updateGrade(grade.id, body)).then(reload)}
                             onRemove={() => guard(adminAccountApi.removeGrade(grade.id)).then(reload)}/>
            ))}
            <GradeEditor onSave={(body) => guard(adminAccountApi.createGrade(body)).then(reload)}/>
        </section>
    );
}

function NewAdminForm({grades, onCreate}: { grades: AdminGrade[]; onCreate: (body: object) => Promise<void> }) {
    const empty = {username: '', password: '', name: '', gradeId: ''};
    const [form, setForm] = useState(empty);
    const [error, setError] = useState<unknown>(null);
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await onCreate({...form, gradeId: form.gradeId ? Number(form.gradeId) : null});
            setForm(empty);
        } catch (e) {
            setError(e);
        }
    };
    return (
        <form className="inline-form" onSubmit={submit} aria-label="관리자 추가">
            <fieldset>
                <legend>관리자 추가</legend>
                <Field label="아이디" name="new-admin-username" required value={form.username} autoComplete="off"
                       onChange={(e) => setForm({...form, username: e.target.value})}/>
                <Field label="비밀번호" name="new-admin-password" type="password" required value={form.password} autoComplete="new-password"
                       onChange={(e) => setForm({...form, password: e.target.value})}/>
                <Field label="이름" name="new-admin-name" required value={form.name}
                       onChange={(e) => setForm({...form, name: e.target.value})}/>
                <div className="field">
                    <label htmlFor="new-admin-grade">등급</label>
                    <select id="new-admin-grade" value={form.gradeId} onChange={(e) => setForm({...form, gradeId: e.target.value})}>
                        <option value="">등급 없음</option>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                    </select>
                </div>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary">추가</button>
            </fieldset>
        </form>
    );
}

function GradeEditor({grade, onSave, onRemove}: {
    grade?: AdminGrade; onSave: (body: object) => Promise<void>; onRemove?: () => Promise<void>;
}) {
    const [name, setName] = useState(grade?.name ?? '');
    const [description, setDescription] = useState(grade?.description ?? '');
    const [areas, setAreas] = useState<AdminArea[]>(grade?.areas ?? []);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);
    const locked = grade?.superAdmin === true;
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            await onSave({name, description, areas});
            if (!grade) {
                setName('');
                setDescription('');
                setAreas([]);
            }
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };
    const title = grade ? `${grade.name} 등급` : '새 등급';
    return (
        <form className="grade-editor" onSubmit={submit} aria-label={title}>
            <fieldset>
                <legend>{grade ? <>{grade.name} <span className="muted small">관리자 {grade.adminCount}명</span></> : '새 등급'}</legend>
                <Field label="등급 이름" name={`grade-name-${grade?.id ?? 'new'}`} required value={name} onChange={(e) => setName(e.target.value)}/>
                <Field label="설명" name={`grade-description-${grade?.id ?? 'new'}`} value={description}
                       onChange={(e) => setDescription(e.target.value)}/>
                <div className="area-checks" role="group" aria-label={`${title} 관리 영역`}>
                    {AREAS.map((area) => (
                        <label key={area} className="checkbox">
                            <input type="checkbox" disabled={locked} checked={locked || areas.includes(area)}
                                   onChange={(e) => setAreas(e.target.checked ? [...areas, area] : areas.filter((a) => a !== area))}/> {ADMIN_AREA[area]}
                        </label>
                    ))}
                    {locked && <p className="muted small">최고 관리자 등급은 모든 영역과 관리자 관리를 쓸 수 있고 삭제할 수 없습니다.</p>}
                </div>
                <ErrorBanner error={error}/>
                {saved && <p className="muted small" role="status">저장했습니다.</p>}
                <div className="toolbar">
                    <button type="submit" className="primary">{grade ? '저장' : '등급 추가'}</button>
                    {grade && !locked && onRemove && (
                        <button type="button" className="danger" onClick={() => {
                            if (window.confirm(`${grade.name} 등급을 삭제할까요?`)) {
                                onRemove().catch(setError);
                            }
                        }}>삭제</button>
                    )}
                </div>
            </fieldset>
        </form>
    );
}
