import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import type {Archive, ArchiveArticleDetail, ArchiveArticleSummary, ArchiveComment, Content, Satisfaction} from '../api/domain';
import {archiveApi, govApi, siteApi} from '../api/endpoints';
import type {Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {CommentEditor} from '../components/CommentEditor';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDate, formatDateTime, rowNumber} from '../components/format';
import {useSite} from '../components/SiteContext';
import {HtmlView} from '../components/HtmlView';
import {ListSearch, pageParams} from '../components/ListSearch';
import {Pagination} from '../components/Pagination';
import {AttachmentList} from './board/BoardPages';

export function ContentPage() {
    const id = Number(useParams().id);
    const menuId = Number(useSearchParams()[0].get('menuId')) || null;
    const {isEnabled} = useSite();
    const [content, setContent] = useState<Content | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        siteApi.content(id).then(setContent).catch(setError);
    }, [id]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!content) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <>
            <article className="card">
                <h1>{content.title}</h1>
                <HtmlView html={content.contentHtml}/>
                <p className="muted small align-right">최종 수정 {formatDateTime(content.modifiedAt)}</p>
            </article>
            {menuId && isEnabled('SATISFACTION') && <SatisfactionWidget menuId={menuId}/>}
        </>
    );
}

const SCORES = [5, 4, 3, 2, 1];
const SCORE_LABELS: Record<number, string> = {5: '매우 만족', 4: '만족', 3: '보통', 2: '불만족', 1: '매우 불만족'};

export function SatisfactionWidget({menuId}: { menuId: number }) {
    const {user} = useAuth();
    const [data, setData] = useState<Satisfaction | null>(null);
    const [score, setScore] = useState(5);
    const [opinion, setOpinion] = useState('');
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        govApi.satisfaction(menuId).then(setData).catch(setError);
    }, [menuId]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setError(null);
            setData(await govApi.vote(menuId, score, opinion));
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card satisfaction">
            <h2>이 페이지에 만족하셨나요?</h2>
            {data && <p className="muted">평균 {data.average.toFixed(2)}점 · {data.total}명 참여</p>}
            {data?.myScore != null ? (
                <p className="ok">참여해 주셔서 감사합니다. ({SCORE_LABELS[data.myScore]})</p>
            ) : user?.userType === 'MEMBER' ? (
                <form onSubmit={submit}>
                    <div className="row">
                        {SCORES.map((value) => (
                            <label key={value} className="checkbox">
                                <input type="radio" name="score" checked={score === value} onChange={() => setScore(value)}/>
                                {SCORE_LABELS[value]}
                            </label>
                        ))}
                    </div>
                    <input aria-label="의견" placeholder="의견 (선택)" value={opinion} onChange={(e) => setOpinion(e.target.value)}/>
                    <button type="submit" className="primary">평가하기</button>
                </form>
            ) : (
                <p className="muted"><Link to="/login">로그인</Link> 후 참여할 수 있습니다.</p>
            )}
            <ErrorBanner error={error}/>
        </section>
    );
}

export function ArchiveListPage() {
    const archiveId = Number(useParams().archiveId);
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [archive, setArchive] = useState<Archive | null>(null);
    const [data, setData] = useState<Page<ArchiveArticleSummary> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        archiveApi.get(archiveId).then(setArchive).catch(setError);
    }, [archiveId]);
    useEffect(() => {
        archiveApi.articles(archiveId, keyword, page).then(setData).catch(setError);
    }, [archiveId, keyword, page]);

    return (
        <section className="card">
            <h1>{archive?.name ?? '아카이브'}</h1>
            {archive?.description && <p className="muted">{archive.description}</p>}
            <ErrorBanner error={error}/>
            {data && (
                archive?.type === 'GALLERY' ? (
                    <div className="gallery">
                        {data.content.map((article) => (
                            <Link key={article.id} to={`/archives/${archiveId}/articles/${article.id}`} className="gallery-item">
                                {article.thumbnailUrl ? <img src={article.thumbnailUrl} alt={article.title}/> : <div className="gallery-empty">이미지 없음</div>}
                                <span>{article.title}</span>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="post-table">
                            <thead>
                            <tr>
                                <th className="col-no">순번</th>
                                <th>제목</th>
                                <th className="col-fit">작성자</th>
                                <th className="col-fit">작성일</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.content.map((article, index) => (
                                <tr key={article.id}>
                                    <td className="col-no">{rowNumber(data.page, index)}</td>
                                    <td className="col-title">
                                        <Link to={`/archives/${archiveId}/articles/${article.id}`}>{article.title}</Link>
                                        {article.status === 'DRAFT' && <span className="badge">임시 저장</span>}
                                    </td>
                                    <td className="col-fit">{article.authorName}</td>
                                    <td className="col-fit">{formatDate(article.createdAt)}</td>
                                </tr>
                            ))}
                            {data.content.length === 0 && (
                                <tr><td colSpan={4} className="muted">{keyword ? '검색 결과가 없습니다.' : '게시물이 없습니다.'}</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )
            )}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
            <ListSearch label={`${archive?.name ?? '아카이브'} 검색`} placeholder="제목 검색" placement="bottom"/>
        </section>
    );
}

export function ArchiveArticlePage() {
    const params = useParams();
    const archiveId = Number(params.archiveId);
    const articleId = Number(params.articleId);
    const [archive, setArchive] = useState<Archive | null>(null);
    const [article, setArticle] = useState<ArchiveArticleDetail | null>(null);
    const [error, setError] = useState<unknown>(null);
    const {user} = useAuth();

    const like = async () => {
        try {
            const result = await archiveApi.like(archiveId, articleId);
            setArticle((prev) => (prev ? {...prev, likes: result.likes} : prev));
        } catch (e) {
            setError(e);
        }
    };

    useEffect(() => {
        archiveApi.get(archiveId).then(setArchive).catch(setError);
        archiveApi.article(archiveId, articleId).then(setArticle).catch(setError);
    }, [archiveId, articleId]);

    if (error && !article) {
        return <ErrorBanner error={error}/>;
    }
    if (!article) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <>
            <article className="card">
                <Link to={`/archives/${archiveId}`} className="muted">← {archive?.name ?? '목록'}</Link>
                <h1>{article.title}</h1>
                <p className="muted small align-right">{article.authorName} · {formatDateTime(article.createdAt)} · 조회 {article.hit}</p>
                <HtmlView html={article.contentHtml}/>
                <AttachmentList files={article.files}/>
                {archive?.supportsLike && (
                    <div className="toolbar form-actions">
                        {user?.userType === 'MEMBER'
                            ? <button type="button" onClick={like}>👍 추천 {article.likes}</button>
                            : <span className="muted small">👍 추천 {article.likes} · <Link to="/login">로그인</Link> 후 추천할 수 있습니다.</span>}
                    </div>
                )}
            </article>
            {archive?.supportsComment && <ArchiveComments archiveId={archiveId} articleId={articleId}/>}
        </>
    );
}

function ArchiveComments({archiveId, articleId}: { archiveId: number; articleId: number }) {
    const {user} = useAuth();
    const [comments, setComments] = useState<ArchiveComment[]>([]);
    const [editing, setEditing] = useState<number | null>(null);
    const [replyTo, setReplyTo] = useState<number | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        archiveApi.comments(archiveId, articleId).then(setComments).catch(setError);
    }, [archiveId, articleId]);
    useEffect(reload, [reload]);

    return (
        <section className="card">
            <h2>댓글 {comments.filter((c) => !c.deleted).length}</h2>
            <ErrorBanner error={error}/>
            <ul className="comments">
                {comments.map((comment) => (
                    <li key={comment.id} className={comment.depth > 0 ? 'reply' : ''}>
                        <div className="comment-meta">
                            <span className="comment-avatar" aria-hidden="true">{comment.authorName?.slice(0, 1) ?? '·'}</span>
                            <strong>{comment.authorName ?? ''}</strong>
                            <span className="muted small">{formatDateTime(comment.createdAt)}</span>
                        </div>
                        {editing === comment.id ? (
                            <CommentEditor initial={comment.content} onCancel={() => setEditing(null)}
                                           onSave={async (text) => {
                                               await archiveApi.editComment(archiveId, articleId, comment.id, text);
                                               setEditing(null);
                                               reload();
                                           }}/>
                        ) : (
                            <>
                                <p className={comment.deleted ? 'muted' : ''}>{comment.content}</p>
                                <div className="toolbar comment-actions">
                                    {user && comment.depth === 0 && !comment.deleted && (
                                        <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>답글</button>
                                    )}
                                    {comment.canEdit && (
                                        <button type="button" className="link-button" onClick={() => setEditing(comment.id)}>수정</button>
                                    )}
                                    {comment.canDelete && (
                                        <button type="button" className="link-button" onClick={() =>
                                            archiveApi.removeComment(archiveId, articleId, comment.id).then(reload).catch(setError)}>삭제</button>
                                    )}
                                </div>
                            </>
                        )}
                        {replyTo === comment.id && (
                            <ArchiveCommentForm archiveId={archiveId} articleId={articleId} parentId={comment.id}
                                                onCancel={() => setReplyTo(null)}
                                                onDone={() => {
                                                    setReplyTo(null);
                                                    reload();
                                                }}/>
                        )}
                    </li>
                ))}
            </ul>
            {user
                ? <ArchiveCommentForm archiveId={archiveId} articleId={articleId} parentId={null} onDone={reload}/>
                : <p className="muted"><Link to="/login">로그인</Link> 후 댓글을 남길 수 있습니다.</p>}
        </section>
    );
}

function ArchiveCommentForm({archiveId, articleId, parentId, onDone, onCancel}: {
    archiveId: number; articleId: number; parentId: number | null; onDone: () => void; onCancel?: () => void;
}) {
    const [content, setContent] = useState('');
    const [error, setError] = useState<unknown>(null);
    return (
        <form className="comment-form" onSubmit={async (e) => {
            e.preventDefault();
            try {
                setError(null);
                await archiveApi.addComment(archiveId, articleId, content, parentId);
                setContent('');
                onDone();
            } catch (err) {
                setError(err);
            }
        }}>
            <textarea aria-label="댓글 내용" rows={3} required value={content}
                      placeholder={parentId ? '답글을 입력하세요' : '댓글을 입력하세요'} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">{parentId ? '답글 등록' : '댓글 등록'}</button>
                {onCancel && <button type="button" onClick={onCancel}>취소</button>}
            </div>
        </form>
    );
}
