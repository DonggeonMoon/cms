import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {Vote} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {gradeNameOf, useGrades} from '../../api/grades';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

export function VoteAdminPage() {
    const grades = useGrades();
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [optionsText, setOptionsText] = useState('');
    const [accessGradeId, setAccessGradeId] = useState('');
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Vote> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);

    const reload = useCallback(() => {
        adminApi.votes.search({page}).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    const create = async (e: FormEvent) => {
        e.preventDefault();
        const options = optionsText.split('\n').map((line) => line.trim()).filter((line) => line.length > 0);
        setBusy(true);
        setError(null);
        try {
            await adminApi.votes.create({
                title, description: description || null, options,
                accessGradeId: accessGradeId ? Number(accessGradeId) : null,
            });
            setTitle('');
            setDescription('');
            setOptionsText('');
            setAccessGradeId('');
            setPage(0);
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>투표</h1>
            <p className="muted">회원 단위 총회 안건을 올려 투표를 받습니다. 결과는 마감 전까지 회원에게 공개되지 않습니다.</p>
            <ErrorBanner error={error}/>
            <form onSubmit={create} className="inline-form">
                <h2>안건 등록</h2>
                <div className="field">
                    <label htmlFor="vote-title">제목</label>
                    <input id="vote-title" value={title} onChange={(e) => setTitle(e.target.value)} required/>
                </div>
                <div className="field">
                    <label htmlFor="vote-description">설명</label>
                    <textarea id="vote-description" rows={3} value={description}
                              onChange={(e) => setDescription(e.target.value)}/>
                </div>
                <div className="field">
                    <label htmlFor="vote-options">선택지 (한 줄에 하나씩, 2개 이상)</label>
                    <textarea id="vote-options" rows={4} value={optionsText}
                              onChange={(e) => setOptionsText(e.target.value)}
                              placeholder={'찬성\n반대'}/>
                </div>
                <div className="field">
                    <label htmlFor="vote-grade">투표 가능 등급</label>
                    <select id="vote-grade" value={accessGradeId} onChange={(e) => setAccessGradeId(e.target.value)}>
                        <option value="">전체 회원</option>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name} 이상</option>)}
                    </select>
                </div>
                <button type="submit" className="primary" disabled={busy}>등록하기</button>
            </form>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr><th>제목</th><th>등급</th><th>상태</th><th>결과</th><th>등록일</th><th>관리</th></tr>
                    </thead>
                    <tbody>
                    {data?.content.map((vote) => (
                        <tr key={vote.id}>
                            <td>{vote.title}</td>
                            <td>{gradeNameOf(grades, vote.accessGradeId) ? `${gradeNameOf(grades, vote.accessGradeId)} 이상` : '전체 회원'}</td>
                            <td>{vote.open ? <span className="badge">진행중</span> : <span className="badge ok">마감</span>}</td>
                            <td>
                                {vote.options.map((option, index) => (
                                    <div key={index}>{option}: {vote.resultCounts[index] ?? 0}표</div>
                                ))}
                                <div className="muted">총 {vote.totalBallots}표</div>
                            </td>
                            <td>{formatDateTime(vote.createdAt)}</td>
                            <td>
                                {vote.open && (
                                    <button type="button" onClick={() => {
                                        if (window.confirm('투표를 마감할까요? 마감하면 회원에게 결과가 공개됩니다.')) {
                                            adminApi.votes.close(vote.id).then(reload).catch(setError);
                                        }
                                    }}>마감하기</button>
                                )}
                                {vote.totalBallots === 0 && (
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm('이 안건을 삭제할까요?')) {
                                            adminApi.votes.remove(vote.id).then(reload).catch(setError);
                                        }
                                    }}>삭제</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">등록된 투표가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
