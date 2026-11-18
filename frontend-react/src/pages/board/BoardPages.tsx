import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useLocation, useNavigate, useParams, useSearchParams} from 'react-router';
import {ApiError} from '../../api/client';
import type {ArticleDetail, ArticleSummary, Board, Comment} from '../../api/domain';
import {boardApi} from '../../api/endpoints';
import type {FileInfo, Page} from '../../api/types';
import {useAuth} from '../../auth/AuthContext';
import {won} from '../../components/AdminCrudPage';
import {CommentEditor} from '../../components/CommentEditor';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatBytes, formatDate, formatDateTime, rowNumber} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {ListSearch} from '../../components/ListSearch';
import {Pagination} from '../../components/Pagination';
import {type FieldSpec, SchemaForm} from '../../components/SchemaForm';
import {StableLabel} from '../../components/StableLabel';
import {useI18n} from '../../api/i18n';

function useBoard(boardId: number) {
    const [board, setBoard] = useState<Board | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        boardApi.get(boardId).then(setBoard).catch(setError);
    }, [boardId]);
    return {board, error};
}

export function BoardListPage() {
    const {t} = useI18n();
    const boardId = Number(useParams().boardId);
    const {board, error: boardError} = useBoard(boardId);
    const [params, setParams] = useSearchParams();
    const keyword = params.get('keyword') ?? '';
    const page = Number(params.get('page') ?? 0);
    const [data, setData] = useState<{ notices: ArticleSummary[]; articles: Page<ArticleSummary> } | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setError(null);
        boardApi.articles(boardId, keyword, page).then(setData).catch(setError);
    }, [boardId, keyword, page]);

    if (boardError) {
        return <ErrorBanner error={boardError}/>;
    }
    const gallery = board?.type === 'GALLERY';
    return (
        <section className="card">
            <h1>{board?.name ?? t('shop.board.title', '게시판')}</h1>
            {(board?.description || board?.permissions.canWrite) && (
                <div className="list-head">
                    <p className="muted">{board?.description}</p>
                    {board?.permissions.canWrite && <Link className="button primary" to={`/boards/${boardId}/write`}>{t('shop.board.write', '글쓰기')}</Link>}
                </div>
            )}
            <ErrorBanner error={error}/>
            {data && (
                gallery ? (
                    <div className="gallery">
                        {[...data.notices, ...data.articles.content].map((article) => (
                            <Link key={article.id} to={`/boards/${boardId}/articles/${article.id}`} className="gallery-item">
                                {article.thumbnailUrl
                                    ? <img src={article.thumbnailUrl} alt={article.title}/>
                                    : <div className="gallery-empty">{t('shop.board.noImage', '이미지 없음')}</div>}
                                <span>{article.title}</span>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="post-table">
                            <thead>
                            <tr>
                                <th className="col-no">{t('shop.board.rowNumber', '순번')}</th>
                                <th>{t('admin.content.field.title', '제목')}</th>
                                <th className="col-fit">{t('admin.order.column.receiver', '작성자')}</th>
                                <th className="col-fit">{t('shop.board.createdAt', '작성일')}</th>
                                <th className="col-fit">{t('shop.board.hit', '조회')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.notices.map((article) => <ArticleRow key={`n-${article.id}`} boardId={boardId} article={article} notice/>)}
                            {data.articles.content.map((article, index) => (
                                <ArticleRow key={article.id} boardId={boardId} article={article} no={rowNumber(data.articles.page, index)}/>
                            ))}
                            {data.notices.length + data.articles.content.length === 0 && (
                                <tr><td colSpan={5} className="muted">{t('shop.board.noArticles', '게시글이 없습니다.')}</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )
            )}
            {data && <Pagination page={page} totalPages={data.articles.page.totalPages}
                                 onChange={(next) => setParams({...(keyword && {keyword}), page: String(next)})}/>}
            <ListSearch label={t('shop.board.searchLabel', '검색어')} placeholder={t('shop.board.searchPlaceholder', '제목·내용 검색')} placement="bottom"/>
        </section>
    );
}

function ArticleRow({boardId, article, notice, no}: { boardId: number; article: ArticleSummary; notice?: boolean; no?: number }) {
    const {t} = useI18n();
    return (
        <tr className={notice ? 'notice-row' : ''}>
            <td className="col-no">{notice ? <span className="badge danger">{t('shop.board.notice', '공지')}</span> : no}</td>
            <td className="col-title">
                <Link to={`/boards/${boardId}/articles/${article.id}`}>{article.title}</Link>
                {article.isSecret && <span className="muted small"> 🔒</span>}
                {article.paid && <span className="badge">{t('shop.board.paid', '유료')}</span>}
                {article.isHidden && <span className="badge">{t('shop.board.hidden', '숨김')}</span>}
                {article.commentCount > 0 && <span className="muted small"> [{article.commentCount}]</span>}
            </td>
            <td className="col-fit">{article.authorName}</td>
            <td className="col-fit">{formatDate(article.createdAt)}</td>
            <td className="col-fit">{article.hit}</td>
        </tr>
    );
}

export function BoardArticlePage() {
    const {t} = useI18n();
    const params = useParams();
    const boardId = Number(params.boardId);
    const articleId = Number(params.articleId);
    const navigate = useNavigate();
    const {user} = useAuth();
    const {board} = useBoard(boardId);
    const [article, setArticle] = useState<ArticleDetail | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [needsPassword, setNeedsPassword] = useState(false);
    const [password, setPassword] = useState('');

    useEffect(() => {
        boardApi.article(boardId, articleId).then(setArticle).catch((e: unknown) => {
            if (e instanceof ApiError && e.code === 'ARTICLE_PASSWORD_MISMATCH') {
                setNeedsPassword(true);
            } else {
                setError(e);
            }
        });
    }, [boardId, articleId]);

    const unlock = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setError(null);
            setArticle(await boardApi.access(boardId, articleId, password));
            setNeedsPassword(false);
        } catch (e) {
            setError(e);
        }
    };

    const remove = async () => {
        const guestPassword = article?.authorType === 'GUEST' && !user ? window.prompt(t('shop.board.enterPassword', '비밀번호를 입력하세요')) ?? undefined : password || undefined;
        if (!window.confirm(t('shop.board.confirmDelete', '게시글을 삭제할까요?'))) {
            return;
        }
        try {
            await boardApi.remove(boardId, articleId, guestPassword);
            navigate(`/boards/${boardId}`);
        } catch (e) {
            setError(e);
        }
    };

    const like = async () => {
        try {
            const result = await boardApi.like(boardId, articleId);
            setArticle((prev) => (prev ? {...prev, likes: result.likes} : prev));
        } catch (e) {
            setError(e);
        }
    };

    if (needsPassword) {
        return (
            <section className="card narrow">
                <h1>{t('shop.board.secretArticle', '비밀글입니다')}</h1>
                <form onSubmit={unlock}>
                    <input type="password" aria-label={t('shop.board.articlePassword', '게시글 비밀번호')} placeholder={t('shop.board.passwordAtWrite', '작성 시 입력한 비밀번호')} value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                    <ErrorBanner error={error}/>
                    <button type="submit" className="primary">{t('shop.board.confirm', '확인')}</button>
                </form>
            </section>
        );
    }
    if (error && !article) {
        return <ErrorBanner error={error}/>;
    }
    if (!article) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            <article className="card">
                <Link to={`/boards/${boardId}`} className="muted">← {board?.name ?? t('shop.board.list', '목록')}</Link>
                <h1>{article.isNotice && <span className="badge danger">{t('shop.board.notice', '공지')}</span>} {article.title}</h1>
                <p className="muted small align-right">
                    {article.authorName} · {formatDateTime(article.createdAt)} · {t('shop.board.hit', '조회')} {article.hit}
                </p>
                {article.paid && !article.purchased ? (
                    <div className="notice-box">
                        <p><strong>{won(article.price ?? 0)}</strong> · {t('shop.board.purchaseNotice', '결제하면 바로 전체 내용을 볼 수 있습니다.')}</p>
                        {user?.userType === 'MEMBER'
                            ? <Link className="button primary" to={`/orders/new?productId=${article.productId}&quantity=1`}>{t('shop.action.buyPass', '구매하기')}</Link>
                            : <p className="muted small">{t('shop.streaming.loginToBuy', '회원으로 로그인한 뒤 구매해 주세요.')}</p>}
                    </div>
                ) : (
                    <>
                        <HtmlView html={article.contentHtml ?? ''}/>
                        <AttachmentList files={article.files}/>
                    </>
                )}
                <ErrorBanner error={error}/>
                <div className="toolbar form-actions">
                    {board?.supportsLike && <button type="button" onClick={like}>👍 {t('shop.board.like', '추천')} {article.likes}</button>}
                    {article.canModify && (
                        <>
                            <Link className="button" to={`/boards/${boardId}/articles/${articleId}/edit`}
                                  state={{password}}>{t('common.edit', '수정')}</Link>
                            <button type="button" className="danger" onClick={remove}>{t('common.delete', '삭제')}</button>
                        </>
                    )}
                </div>
            </article>
            {board?.supportsComment && <Comments boardId={boardId} articleId={articleId} canWrite={board.permissions.canWrite}/>}
        </>
    );
}

export function AttachmentList({files}: { files: FileInfo[] }) {
    const {t} = useI18n();
    if (files.length === 0) {
        return null;
    }
    return (
        <div className="attachments">
            <strong>{t('shop.board.attachments', '첨부파일')}</strong>
            <ul className="file-list">
                {files.map((file) => (
                    <li key={file.id}>
                        <a href={file.downloadUrl}>{file.originalName}</a> <span className="muted small">({formatBytes(file.size)})</span>
                    </li>
                ))}
            </ul>
        </div>
    );
}

function Comments({boardId, articleId, canWrite}: { boardId: number; articleId: number; canWrite: boolean }) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [comments, setComments] = useState<Comment[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [replyTo, setReplyTo] = useState<number | null>(null);
    const [editing, setEditing] = useState<number | null>(null);

    const reload = useCallback(() => {
        boardApi.comments(boardId, articleId).then(setComments).catch(setError);
    }, [boardId, articleId]);
    useEffect(reload, [reload]);

    const remove = async (comment: Comment) => {
        const password = comment.authorType === 'GUEST' && !user ? window.prompt(t('shop.board.commentPasswordPrompt', '댓글 비밀번호를 입력하세요')) ?? undefined : undefined;
        try {
            await boardApi.removeComment(boardId, articleId, comment.id, password);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h2>{t('shop.board.comments', '댓글')} {comments.filter((c) => !c.deleted).length}</h2>
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
                                           onSave={async (content, password) => {
                                               await boardApi.editComment(boardId, articleId, comment.id, content, password);
                                               setEditing(null);
                                               reload();
                                           }}/>
                        ) : (
                            <>
                                <p className={comment.deleted ? 'muted' : ''}>{comment.content}</p>
                                <div className="toolbar comment-actions">
                                    {canWrite && comment.depth === 0 && !comment.deleted && (
                                        <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>{t('shop.course.reply', '답글')}</button>
                                    )}
                                    {comment.canEdit && <button type="button" className="link-button" onClick={() => setEditing(comment.id)}>{t('common.edit', '수정')}</button>}
                                    {comment.canDelete && <button type="button" className="link-button" onClick={() => remove(comment)}>{t('common.delete', '삭제')}</button>}
                                </div>
                            </>
                        )}
                        {replyTo === comment.id && (
                            <CommentForm boardId={boardId} articleId={articleId} parentId={comment.id}
                                         onDone={() => {
                                             setReplyTo(null);
                                             reload();
                                         }}/>
                        )}
                    </li>
                ))}
            </ul>
            {canWrite && <CommentForm boardId={boardId} articleId={articleId} parentId={null} onDone={reload}/>}
        </section>
    );
}

function CommentForm({boardId, articleId, parentId, onDone}: {
    boardId: number; articleId: number; parentId: number | null; onDone: () => void;
}) {
    const {t} = useI18n();
    const {user} = useAuth();
    const [content, setContent] = useState('');
    const [authorName, setAuthorName] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setError(null);
            await boardApi.addComment(boardId, articleId, {parentId, content, authorName, password});
            setContent('');
            onDone();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <form className="comment-form" onSubmit={submit}>
            {!user && (
                <>
                    <input aria-label={t('shop.board.name', '이름')} placeholder={t('shop.board.name', '이름')} value={authorName} onChange={(e) => setAuthorName(e.target.value)}/>
                    <input aria-label={t('admin.memberJoin.password', '비밀번호')} type="password" placeholder={t('admin.memberJoin.password', '비밀번호')} value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                </>
            )}
            <textarea aria-label={t('shop.board.commentContent', '댓글 내용')} rows={3} required
                      placeholder={parentId ? t('shop.course.replyPlaceholder', '답글을 입력하세요') : t('shop.board.commentPlaceholder', '댓글을 입력하세요')}
                      value={content} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <button type="submit" className="primary">
                <StableLabel current={parentId ? t('shop.course.submitReply', '답글 등록') : t('shop.board.submitComment', '댓글 등록')}
                             labels={[t('shop.course.submitReply', '답글 등록'), t('shop.board.submitComment', '댓글 등록')]}/>
            </button>
        </form>
    );
}

export function BoardWritePage() {
    const {t} = useI18n();
    const params = useParams();
    const boardId = Number(params.boardId);
    const articleId = params.articleId ? Number(params.articleId) : null;
    const navigate = useNavigate();
    const location = useLocation();
    const {user} = useAuth();
    const {board, error} = useBoard(boardId);
    const [initial, setInitial] = useState<Record<string, unknown> | null>(articleId ? null : {contentHtml: '', files: []});
    const [loadError, setLoadError] = useState<unknown>(null);
    const passedPassword = (location.state as { password?: string } | null)?.password ?? '';

    useEffect(() => {
        if (articleId) {
            (passedPassword ? boardApi.access(boardId, articleId, passedPassword) : boardApi.article(boardId, articleId))
                .then((article) => setInitial({...article, password: passedPassword}))
                .catch(setLoadError);
        }
    }, [boardId, articleId, passedPassword]);

    if (error || loadError) {
        return <ErrorBanner error={error ?? loadError}/>;
    }
    if (!board || !initial) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }

    const isAdmin = board.permissions.isAdmin;
    const fields: FieldSpec[] = [
        {name: 'title', label: t('admin.content.field.title', '제목'), required: true, wide: true},
        ...(!user ? [
            {name: 'authorName', label: t('shop.board.authorName', '작성자 이름'), required: !articleId, wide: true},
            {name: 'password', label: t('admin.memberJoin.password', '비밀번호'), type: 'password', required: true, wide: true, help: t('shop.board.passwordHelp', '수정·삭제할 때 필요합니다.')},
        ] as FieldSpec[] : []),
        {name: 'contentHtml', label: t('admin.content.field.contentHtml', '내용'), type: 'html'},
        ...(board.supportsAttachment ? [{name: 'files', label: t('shop.board.attachmentsMax', '첨부파일 (최대 {count}개)', {count: String(board.maximumFileCount)}), type: 'files'} as FieldSpec] : []),
        ...(board.supportsSecret ? [{name: 'isSecret', label: t('shop.board.isSecret', '비밀글'), type: 'checkbox'} as FieldSpec] : []),
        ...(isAdmin && board.supportsNotice ? [
            {name: 'isNotice', label: t('shop.board.registerAsNotice', '공지로 등록'), type: 'checkbox'},
            {name: 'noticeStartDateTime', label: t('shop.board.noticeStart', '공지 시작'), type: 'datetime'},
            {name: 'noticeEndDateTime', label: t('shop.board.noticeEnd', '공지 종료'), type: 'datetime'},
        ] as FieldSpec[] : []),
        ...(isAdmin ? [{name: 'isHidden', label: t('shop.board.isHiddenAdminOnly', '숨김(관리자만 보기)'), type: 'checkbox'} as FieldSpec] : []),
        ...(isAdmin && board.supportsPaidContent ? [
            {name: 'paid', label: t('shop.board.paidContent', '유료 글(페이월)'), type: 'checkbox', help: t('shop.board.paidContentHelp', '결제한 회원만 본문을 볼 수 있습니다.')},
            {name: 'price', label: t('admin.course.field.price', '가격(원)'), type: 'number', visibleWhen: (v) => Boolean(v.paid)},
        ] as FieldSpec[] : []),
    ];

    return (
        <section className="card">
            <h1>{board.name} · {articleId ? t('shop.board.editArticle', '글 수정') : t('shop.board.write', '글쓰기')}</h1>
            <SchemaForm
                fields={fields}
                initial={initial}
                submitLabel={articleId ? t('common.edit', '수정') : t('shop.board.register', '등록')}
                onCancel={() => navigate(-1)}
                onSubmit={async (values) => {
                    const body = {...values, fileIds: ((values.files as FileInfo[] | undefined) ?? []).map((f) => f.id)};
                    if (articleId) {
                        await boardApi.update(boardId, articleId, body);
                        navigate(`/boards/${boardId}/articles/${articleId}`);
                    } else {
                        await boardApi.create(boardId, body);
                        navigate(`/boards/${boardId}`);
                    }
                }}
            />
        </section>
    );
}
