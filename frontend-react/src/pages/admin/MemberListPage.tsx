import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useSearchParams} from 'react-router';
import {memberAdminApi} from '../../api/members';
import type {MemberSummary, Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function MemberListPage() {
    const [params, setParams] = useSearchParams();
    const keyword = params.get('keyword') ?? '';
    const page = Number(params.get('page') ?? 0);
    const [input, setInput] = useState(keyword);
    const [result, setResult] = useState<Page<MemberSummary> | null>(null);
    const [error, setError] = useState<unknown>(null);

    const load = useCallback(() => {
        memberAdminApi.search(keyword, page).then(setResult).catch(setError);
    }, [keyword, page]);

    useEffect(load, [load]);

    const handleSearch = (event: FormEvent) => {
        event.preventDefault();
        setParams(input ? {keyword: input} : {});
    };

    const act = async (action: () => Promise<void>) => {
        setError(null);
        try {
            await action();
            load();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>회원 관리</h1>
            <form className="toolbar" onSubmit={handleSearch}>
                <input aria-label="검색어" placeholder="아이디, 이름, 이메일" value={input}
                       onChange={(e) => setInput(e.target.value)}/>
                <button type="submit">검색</button>
            </form>
            <ErrorBanner error={error}/>
            {result && (
                <>
                    <p className="muted">총 {result.page.totalElements}명</p>
                    <table>
                        <thead>
                        <tr>
                            <th>아이디</th>
                            <th>이름</th>
                            <th>이메일</th>
                            <th>상태</th>
                            <th>마지막 로그인</th>
                            <th>가입일</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {result.content.map((member) => (
                            <tr key={member.id}>
                                <td><Link to={`/admin/members/${member.id}`}>{member.username}</Link></td>
                                <td>{member.name}</td>
                                <td>{member.email ?? '-'}</td>
                                <td>{member.isLocked ? <span className="badge danger">잠김</span> : '정상'}</td>
                                <td>{formatDateTime(member.lastLoginDateTime)}</td>
                                <td>{formatDateTime(member.createdAt)}</td>
                                <td className="actions">
                                    {member.isLocked && (
                                        <button type="button" onClick={() => act(() => memberAdminApi.unlock(member.id))}>
                                            잠금 해제
                                        </button>
                                    )}
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(`${member.username} 회원을 탈퇴 처리할까요?`)) {
                                            void act(() => memberAdminApi.remove(member.id));
                                        }
                                    }}>탈퇴 처리
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {result.content.length === 0 && (
                            <tr>
                                <td colSpan={7} className="muted">회원이 없습니다.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={result.page.totalPages}
                                onChange={(next) => setParams({...(keyword && {keyword}), page: String(next)})}/>
                </>
            )}
        </section>
    );
}
