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
import {useI18n} from '../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

export function ContentPage() {
    const {t} = useI18n();
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            <article className="card">
                <h1>{content.title}</h1>
                <HtmlView html={content.contentHtml}/>
                <p className="muted small align-right">{t('shop.policy.lastModified', '최종 수정 {date}', {date: formatDateTime(content.modifiedAt)})}</p>
            </article>
            {menuId && isEnabled('SATISFACTION') && <SatisfactionWidget menuId={menuId}/>}
        </>
    );
}

const SCORES = [5, 4, 3, 2, 1];
const scoreLabels = (t: T): Record<number, string> => ({
    5: t('shop.satisfaction.veryGood', '매우 만족'), 4: t('shop.satisfaction.good', '만족'), 3: t('shop.satisfaction.neutral', '보통'),
    2: t('shop.satisfaction.bad', '불만족'), 1: t('shop.satisfaction.veryBad', '매우 불만족'),
});

export function SatisfactionWidget({menuId}: { menuId: number }) {
    const {t} = useI18n();
    const SCORE_LABELS = scoreLabels(t);
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
            <h2>{t('shop.satisfaction.question', '이 페이지에 만족하셨나요?')}</h2>
            {data && <p className="muted">{t('shop.satisfaction.summary', '평균 {average}점 · {total}명 참여', {average: data.average.toFixed(2), total: String(data.total)})}</p>}
            {data?.myScore != null ? (
                <p className="ok">{t('shop.satisfaction.thanks', '참여해 주셔서 감사합니다. ({score})', {score: SCORE_LABELS[data.myScore]})}</p>
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
                    <input aria-label={t('shop.satisfaction.opinion', '의견')} placeholder={t('shop.satisfaction.opinionPlaceholder', '의견 (선택)')} value={opinion} onChange={(e) => setOpinion(e.target.value)}/>
                    <button type="submit" className="primary">{t('shop.satisfaction.submit', '평가하기')}</button>
                </form>
            ) : (
                <p className="muted"><Link to="/login">{t('nav.login', '로그인')}</Link> {t('shop.satisfaction.loginNotice', '후 참여할 수 있습니다.')}</p>
            )}
            <ErrorBanner error={error}/>
        </section>
    );
}

export function ArchiveListPage() {
    const {t} = useI18n();
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
            <h1>{archive?.name ?? t('shop.archive.title', '아카이브')}</h1>
            {archive?.description && <p className="muted">{archive.description}</p>}
            <ErrorBanner error={error}/>
            {data && (
                archive?.type === 'GALLERY' ? (
                    <div className="gallery">
                        {data.content.map((article) => (
                            <Link key={article.id} to={`/archives/${archiveId}/articles/${article.id}`} className="gallery-item">
                                {article.thumbnailUrl ? <img src={article.thumbnailUrl} alt={article.title}/> : <div className="gallery-empty">{t('shop.archive.noImage', '이미지 없음')}</div>}
                                <span>{article.title}</span>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="post-table">
                            <thead>
                            <tr>
                                <th className="col-no">{t('admin.board.column.rowNumber', '순번')}</th>
                                <th>{t('admin.board.column.title', '제목')}</th>
                                <th className="col-fit">{t('shop.board.author', '작성자')}</th>
                                <th className="col-fit">{t('shop.board.createdAt', '작성일')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.content.map((article, index) => (
                                <tr key={article.id}>
                                    <td className="col-no">{rowNumber(data.page, index)}</td>
                                    <td className="col-title">
                                        <Link to={`/archives/${archiveId}/articles/${article.id}`}>{article.title}</Link>
                                        {article.status === 'DRAFT' && <span className="badge">{t('shop.archive.draft', '임시 저장')}</span>}
                                    </td>
                                    <td className="col-fit">{article.authorName}</td>
                                    <td className="col-fit">{formatDate(article.createdAt)}</td>
                                </tr>
                            ))}
                            {data.content.length === 0 && (
                                <tr><td colSpan={4} className="muted">{keyword ? t('shop.common.noSearchResults', '검색 결과가 없습니다.') : t('shop.archive.noArticles', '게시물이 없습니다.')}</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )
            )}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
            <ListSearch label={t('shop.course.searchLabel', '{title} 검색', {title: archive?.name ?? t('shop.archive.title', '아카이브')})} placeholder={t('shop.application.searchPlaceholder', '제목 검색')} placement="bottom"/>
        </section>
    );
}

export function ArchiveArticlePage() {
    const {t} = useI18n();
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            <article className="card">
                <Link to={`/archives/${archiveId}`} className="muted">← {archive?.name ?? t('shop.archive.list', '목록')}</Link>
                <h1>{article.title}</h1>
                <p className="muted small align-right">{t('shop.archive.meta', '{author} · {date} · 조회 {hit}', {author: article.authorName, date: formatDateTime(article.createdAt), hit: String(article.hit)})}</p>
                <HtmlView html={article.contentHtml}/>
                <AttachmentList files={article.files}/>
                {archive?.supportsLike && (
                    <div className="toolbar form-actions">
                        {user?.userType === 'MEMBER'
                            ? <button type="button" onClick={like}>{t('shop.archive.like', '👍 추천 {count}', {count: String(article.likes)})}</button>
                            : <span className="muted small">{t('shop.archive.like', '👍 추천 {count}', {count: String(article.likes)})} · <Link to="/login">{t('nav.login', '로그인')}</Link> {t('shop.archive.loginToLike', '후 추천할 수 있습니다.')}</span>}
                    </div>
                )}
            </article>
            {archive?.supportsComment && (
                <ArchiveComments archiveId={archiveId} articleId={articleId} guestCommentable={archive.isGuestCommentable}/>
            )}
        </>
    );
}

function ArchiveComments({archiveId, articleId, guestCommentable}: {
    archiveId: number; articleId: number; guestCommentable: boolean;
}) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [comments, setComments] = useState<ArchiveComment[]>([]);
    const [editing, setEditing] = useState<number | null>(null);
    const [replyTo, setReplyTo] = useState<number | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        archiveApi.comments(archiveId, articleId).then(setComments).catch(setError);
    }, [archiveId, articleId]);
    useEffect(reload, [reload]);

    const canWrite = guestCommentable || Boolean(user);

    const remove = async (comment: ArchiveComment) => {
        const password = comment.authorType === 'GUEST' && !user ? window.prompt(t('shop.archive.commentPasswordPrompt', '댓글 비밀번호를 입력하세요')) ?? undefined : undefined;
        try {
            await archiveApi.removeComment(archiveId, articleId, comment.id, password);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h2>{t('shop.archive.commentsCount', '댓글 {count}', {count: String(comments.filter((c) => !c.deleted).length)})}</h2>
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
                            <CommentEditor initial={comment.content} needsPassword={comment.authorType === 'GUEST' && !user}
                                           onCancel={() => setEditing(null)}
                                           onSave={async (text, password) => {
                                               await archiveApi.editComment(archiveId, articleId, comment.id, text, password);
                                               setEditing(null);
                                               reload();
                                           }}/>
                        ) : (
                            <>
                                <p className={comment.deleted ? 'muted' : ''}>{comment.content}</p>
                                <div className="toolbar comment-actions">
                                    {canWrite && comment.depth === 0 && !comment.deleted && (
                                        <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>{t('shop.archive.reply', '답글')}</button>
                                    )}
                                    {comment.canEdit && (
                                        <button type="button" className="link-button" onClick={() => setEditing(comment.id)}>{t('common.edit', '수정')}</button>
                                    )}
                                    {comment.canDelete && (
                                        <button type="button" className="link-button" onClick={() => remove(comment)}>{t('common.delete', '삭제')}</button>
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
            {canWrite
                ? <ArchiveCommentForm archiveId={archiveId} articleId={articleId} parentId={null} onDone={reload}/>
                : <p className="muted"><Link to="/login">{t('nav.login', '로그인')}</Link> {t('shop.archive.loginToComment', '후 댓글을 남길 수 있습니다.')}</p>}
        </section>
    );
}

function ArchiveCommentForm({archiveId, articleId, parentId, onDone, onCancel}: {
    archiveId: number; articleId: number; parentId: number | null; onDone: () => void; onCancel?: () => void;
}) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [content, setContent] = useState('');
    const [authorName, setAuthorName] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);
    return (
        <form className="comment-form" onSubmit={async (e) => {
            e.preventDefault();
            try {
                setError(null);
                await archiveApi.addComment(archiveId, articleId, {parentId, content, authorName, password});
                setContent('');
                onDone();
            } catch (err) {
                setError(err);
            }
        }}>
            {!user && (
                <>
                    <input aria-label={t('shop.board.name', '이름')} placeholder={t('shop.board.name', '이름')} value={authorName} onChange={(e) => setAuthorName(e.target.value)}/>
                    <input aria-label={t('admin.memberJoin.password', '비밀번호')} type="password" placeholder={t('admin.memberJoin.password', '비밀번호')} value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                </>
            )}
            <textarea aria-label={t('shop.archive.commentContent', '댓글 내용')} rows={3} required value={content}
                      placeholder={parentId ? t('shop.archive.replyPlaceholder', '답글을 입력하세요') : t('shop.archive.commentPlaceholder', '댓글을 입력하세요')} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">{parentId ? t('shop.archive.submitReply', '답글 등록') : t('shop.archive.submitComment', '댓글 등록')}</button>
                {onCancel && <button type="button" onClick={onCancel}>{t('common.cancel', '취소')}</button>}
            </div>
        </form>
    );
}
