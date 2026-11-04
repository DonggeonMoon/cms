import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useParams} from 'react-router';
import {memberAdminApi} from '../../api/members';
import type {MemberResponse} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {GRADE_CHANGE_REASON, gradeApi, type GradeHistory, type MyGrade, useGrades} from '../../api/grades';

export function MemberDetailPage() {
    const {id} = useParams();
    const [member, setMember] = useState<MemberResponse | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        memberAdminApi.get(Number(id)).then(setMember).catch(setError);
    }, [id]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!member) {
        return <p className="muted">불러오는 중…</p>;
    }

    const rows: [string, string | null | undefined][] = [
        ['아이디', member.username],
        ['닉네임', member.nickname],
        ['이름', member.name],
        ['생년월일', member.birthDate],
        ['성별', member.gender],
        ['이메일', member.email],
        ['전화번호', member.telephoneNumber],
        ['휴대폰', member.mobilePhoneNumber],
        ['주소', [member.postalCode && `(${member.postalCode})`, member.address, member.detailAddress].filter(Boolean).join(' ')],
        ['직장', [member.officeName, member.officeDepartment, member.officePosition].filter(Boolean).join(' / ')],
        ['수신 동의', [
            member.isMailReceptionAgreed && '메일',
            member.isSmsReceptionAgreed && 'SMS',
            member.isMarketingUsageAgreed && '마케팅',
        ].filter(Boolean).join(', ')],
        ['로그인 실패 횟수', String(member.loginTryCount)],
        ['가입일', formatDateTime(member.createdAt)],
        ['마지막 로그인', formatDateTime(member.lastLoginDateTime)],
    ];

    return (
        <section className="card">
            <h1>회원 상세</h1>
            <dl className="meta">
                {rows.map(([label, value]) => (
                    <div key={label} className="meta-row">
                        <dt>{label}</dt>
                        <dd>{value || '-'}</dd>
                    </div>
                ))}
            </dl>
            <MemberGradePanel memberId={member.id}/>
            <Link to="/admin/members">목록으로</Link>
        </section>
    );
}

function MemberGradePanel({memberId}: { memberId: number }) {
    const grades = useGrades();
    const [current, setCurrent] = useState<MyGrade | null>(null);
    const [history, setHistory] = useState<GradeHistory[]>([]);
    const [form, setForm] = useState({gradeId: '', expiresAt: ''});
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    const load = useCallback(() => {
        gradeApi.ofMember(memberId).then((grade) => {
            setCurrent(grade);
            setForm({gradeId: grade.gradeId ? String(grade.gradeId) : '', expiresAt: grade.expiresAt?.slice(0, 16) ?? ''});
        }).catch(setError);
        gradeApi.history(memberId).then(setHistory).catch(setError);
    }, [memberId]);
    useEffect(load, [load]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        setSaved(false);
        try {
            await gradeApi.assign(memberId, {
                gradeId: form.gradeId ? Number(form.gradeId) : null,
                expiresAt: form.expiresAt ? `${form.expiresAt}:00` : null,
            });
            setSaved(true);
            load();
        } catch (e) {
            setError(e);
        }
    };

    if (grades.length === 0) {
        return <p className="muted">회원 등급을 쓰려면 공통 &gt; 회원 등급에서 등급을 먼저 만드세요.</p>;
    }
    return (
        <section>
            <h2>회원 등급</h2>
            <p>
                지금 등급: <strong>{current?.gradeName ?? '-'}</strong>
                {current?.expiresAt && <> · {formatDateTime(current.expiresAt)}까지 (이후 {current.baseGradeName ?? '기본 등급'})</>}
            </p>
            <form className="toolbar" onSubmit={submit}>
                <select aria-label="등급" value={form.gradeId} onChange={(e) => setForm({...form, gradeId: e.target.value})}>
                    <option value="">기본 등급</option>
                    {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                </select>
                <input type="datetime-local" aria-label="만료 일시" value={form.expiresAt}
                       onChange={(e) => setForm({...form, expiresAt: e.target.value})}/>
                <button type="submit" className="primary">등급 저장</button>
                {saved && <span className="muted">저장했습니다.</span>}
            </form>
            <p className="muted">만료 일시를 비우면 무기한입니다. 만료 일시를 정하면 그때 지금의 무기한 등급으로 돌아갑니다.</p>
            <ErrorBanner error={error}/>
            {history.length > 0 && (
                <table>
                    <thead>
                    <tr><th>일시</th><th>변경</th><th>만료</th><th>사유</th><th>처리자</th></tr>
                    </thead>
                    <tbody>
                    {history.map((row) => (
                        <tr key={row.id}>
                            <td>{formatDateTime(row.createdAt)}</td>
                            <td>{row.fromGradeName ?? '기본'} → {row.toGradeName ?? '기본'}</td>
                            <td>{row.expiresAt ? formatDateTime(row.expiresAt) : '무기한'}</td>
                            <td>{GRADE_CHANGE_REASON[row.reason]}{row.orderId && ` (주문 #${row.orderId})`}</td>
                            <td>{row.changedBy ?? '자동'}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </section>
    );
}
