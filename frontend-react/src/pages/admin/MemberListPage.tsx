import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useSearchParams} from 'react-router';
import {memberAdminApi} from '../../api/members';
import type {MemberSummary, Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

export function MemberListPage() {
    const {t} = useI18n();
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
            <h1>{t('admin.memberList.title', '회원 관리')}</h1>
            <form className="toolbar" onSubmit={handleSearch}>
                <input aria-label={t('common.searchKeyword', '검색어')} placeholder={t('admin.memberJoin.username', '아이디')} value={input}
                       onChange={(e) => setInput(e.target.value)}/>
                <button type="submit">{t('common.search', '검색')}</button>
            </form>
            <ErrorBanner error={error}/>
            {result && (
                <>
                    <p className="muted">{t('admin.memberList.totalCount', '총 {count}명', {count: String(result.page.totalElements)})}</p>
                    <table>
                        <thead>
                        <tr>
                            <th>{t('admin.memberJoin.username', '아이디')}</th>
                            <th>{t('admin.member.nickname', '닉네임')}</th>
                            <th>{t('shop.board.name', '이름')}</th>
                            <th>{t('admin.memberJoin.email', '이메일')}</th>
                            <th>{t('admin.account.grade', '등급')}</th>
                            <th>{t('admin.order.column.status', '상태')}</th>
                            <th>{t('admin.member.joinedAt', '가입일')}</th>
                            <th>{t('admin.account.lastLogin', '마지막 로그인')}</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {result.content.map((member) => (
                            <tr key={member.id}>
                                <td><Link to={`/admin/members/${member.id}`}>{member.username}</Link></td>
                                <td>{member.nickname ?? '-'}</td>
                                <td>{member.name ?? '-'}</td>
                                <td>{member.email ?? '-'}</td>
                                <td>{member.gradeName ?? '-'}{member.gradeExpiresAt && (
                                    <span className="muted"> ~{formatDateTime(member.gradeExpiresAt)}</span>)}</td>
                                <td>{member.isLocked ? <span className="badge danger">{t('admin.memberList.locked', '잠김')}</span> : t('admin.account.normal', '정상')}</td>
                                <td>{formatDateTime(member.createdAt)}</td>
                                <td>{formatDateTime(member.lastLoginDateTime)}</td>
                                <td className="actions">
                                    {member.isLocked && (
                                        <button type="button" onClick={() => act(() => memberAdminApi.unlock(member.id))}>
                                            {t('admin.memberList.unlock', '잠금 해제')}
                                        </button>
                                    )}
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(t('admin.memberList.confirmQuit', '{username} 회원을 탈퇴 처리할까요?', {username: member.username}))) {
                                            void act(() => memberAdminApi.remove(member.id));
                                        }
                                    }}>{t('admin.memberList.processQuit', '탈퇴 처리')}
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {result.content.length === 0 && (
                            <tr>
                                <td colSpan={8} className="muted">{t('admin.memberList.none', '회원이 없습니다.')}</td>
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
