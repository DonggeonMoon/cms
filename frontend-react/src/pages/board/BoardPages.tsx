import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useLocation, useNavigate, useParams, useSearchParams} from 'react-router';
import {ApiError} from '../../api/client';
import type {ArticleDetail, ArticleSummary, Board, Comment} from '../../api/domain';
import {boardApi} from '../../api/endpoints';
import type {FileInfo, Page} from '../../api/types';
import {useAuth} from '../../auth/AuthContext';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatBytes, formatDateTime} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {Pagination} from '../../components/Pagination';
import {type FieldSpec, SchemaForm} from '../../components/SchemaForm';

function useBoard(boardId: number) {
    const [board, setBoard] = useState<Board | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        boardApi.get(boardId).then(setBoard).catch(setError);
    }, [boardId]);
    return {board, error};
}

export function BoardListPage() {
    const boardId = Number(useParams().boardId);
    const {board, error: boardError} = useBoard(boardId);
    const [params, setParams] = useSearchParams();
    const keyword = params.get('keyword') ?? '';
    const page = Number(params.get('page') ?? 0);
    const [input, setInput] = useState(keyword);
    const [data, setData] = useState<{ notices: ArticleSummary[]; articles: Page<ArticleSummary> } | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setError(null);
        boardApi.articles(boardId, keyword, page).then(setData).catch(setError);
    }, [boardId, keyword, page]);

    const handleSearch = (event: FormEvent) => {
        event.preventDefault();
        setParams(input ? {keyword: input} : {});
    };

    if (boardError) {
        return <ErrorBanner error={boardError}/>;
    }
    const gallery = board?.type === 'GALLERY';
    return (
        <section className="card">
            <h1>{board?.name ?? '게시판'}</h1>
            {board?.description && <p className="muted">{board.description}</p>}
            <div className="toolbar space-between">
                <form className="toolbar inline" onSubmit={handleSearch}>
                    <input aria-label="검색어" placeholder="제목·내용 검색" value={input} onChange={(e) => setInput(e.target.value)}/>
                    <button type="submit">검색</button>
                </form>
                {board?.permissions.canWrite && <Link className="button primary" to={`/boards/${boardId}/write`}>글쓰기</Link>}
            </div>
            <ErrorBanner error={error}/>
            {data && (
                gallery ? (
                    <div className="gallery">
                        {[...data.notices, ...data.articles.content].map((article) => (
                            <Link key={article.id} to={`/boards/${boardId}/articles/${article.id}`} className="gallery-item">
                                {article.thumbnailUrl
                                    ? <img src={article.thumbnailUrl} alt={article.title}/>
                                    : <div className="gallery-empty">이미지 없음</div>}
                                <span>{article.title}</span>
                            </Link>
                        ))}
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>제목</th>
                                <th>작성자</th>
                                <th>조회</th>
                                <th>작성일</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.notices.map((article) => <ArticleRow key={`n-${article.id}`} boardId={boardId} article={article} notice/>)}
                            {data.articles.content.map((article) => <ArticleRow key={article.id} boardId={boardId} article={article}/>)}
                            {data.notices.length + data.articles.content.length === 0 && (
                                <tr><td colSpan={4} className="muted">게시글이 없습니다.</td></tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )
            )}
            {data && <Pagination page={page} totalPages={data.articles.page.totalPages}
                                 onChange={(next) => setParams({...(keyword && {keyword}), page: String(next)})}/>}
        </section>
    );
}

function ArticleRow({boardId, article, notice}: { boardId: number; article: ArticleSummary; notice?: boolean }) {
    return (
        <tr className={notice ? 'notice-row' : ''}>
            <td>
                {notice && <span className="badge danger">공지</span>}{' '}
                <Link to={`/boards/${boardId}/articles/${article.id}`}>{article.title}</Link>
                {article.isSecret && <span className="muted small"> 🔒</span>}
                {article.isHidden && <span className="badge">숨김</span>}
                {article.commentCount > 0 && <span className="muted small"> [{article.commentCount}]</span>}
            </td>
            <td>{article.authorName}</td>
            <td>{article.hit}</td>
            <td>{formatDateTime(article.createdAt)}</td>
        </tr>
    );
}

export function BoardArticlePage() {
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
        const guestPassword = article?.authorType === 'GUEST' && !user ? window.prompt('비밀번호를 입력하세요') ?? undefined : password || undefined;
        if (!window.confirm('게시글을 삭제할까요?')) {
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
                <h1>비밀글입니다</h1>
                <form onSubmit={unlock}>
                    <input type="password" aria-label="게시글 비밀번호" placeholder="작성 시 입력한 비밀번호" value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                    <ErrorBanner error={error}/>
                    <button type="submit" className="primary">확인</button>
                </form>
            </section>
        );
    }
    if (error && !article) {
        return <ErrorBanner error={error}/>;
    }
    if (!article) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <>
            <article className="card">
                <Link to={`/boards/${boardId}`} className="muted">← {board?.name ?? '목록'}</Link>
                <h1>{article.isNotice && <span className="badge danger">공지</span>} {article.title}</h1>
                <p className="muted small">
                    {article.authorName} · {formatDateTime(article.createdAt)} · 조회 {article.hit}
                </p>
                <HtmlView html={article.contentHtml}/>
                <AttachmentList files={article.files}/>
                <ErrorBanner error={error}/>
                <div className="toolbar">
                    {board?.supportsLike && <button type="button" onClick={like}>👍 추천 {article.likes}</button>}
                    {article.canModify && (
                        <>
                            <Link className="button" to={`/boards/${boardId}/articles/${articleId}/edit`}
                                  state={{password}}>수정</Link>
                            <button type="button" className="danger" onClick={remove}>삭제</button>
                        </>
                    )}
                </div>
            </article>
            {board?.supportsComment && <Comments boardId={boardId} articleId={articleId} canWrite={board.permissions.canWrite}/>}
        </>
    );
}

export function AttachmentList({files}: { files: FileInfo[] }) {
    if (files.length === 0) {
        return null;
    }
    return (
        <div className="attachments">
            <strong>첨부파일</strong>
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
    const {user} = useAuth();
    const [comments, setComments] = useState<Comment[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [replyTo, setReplyTo] = useState<number | null>(null);

    const reload = useCallback(() => {
        boardApi.comments(boardId, articleId).then(setComments).catch(setError);
    }, [boardId, articleId]);
    useEffect(reload, [reload]);

    const remove = async (comment: Comment) => {
        const password = comment.authorType === 'GUEST' && !user ? window.prompt('댓글 비밀번호를 입력하세요') ?? undefined : undefined;
        try {
            await boardApi.removeComment(boardId, articleId, comment.id, password);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h2>댓글 {comments.filter((c) => !c.deleted).length}</h2>
            <ErrorBanner error={error}/>
            <ul className="comments">
                {comments.map((comment) => (
                    <li key={comment.id} className={comment.depth > 0 ? 'reply' : ''}>
                        <div className="row space-between">
                            <strong>{comment.authorName ?? ''}</strong>
                            <span className="muted small">{formatDateTime(comment.createdAt)}</span>
                        </div>
                        <p className={comment.deleted ? 'muted' : ''}>{comment.content}</p>
                        <div className="toolbar">
                            {canWrite && comment.depth === 0 && !comment.deleted && (
                                <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>답글</button>
                            )}
                            {comment.canDelete && <button type="button" className="link-button" onClick={() => remove(comment)}>삭제</button>}
                        </div>
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
                <div className="row">
                    <input aria-label="이름" placeholder="이름" value={authorName} onChange={(e) => setAuthorName(e.target.value)}/>
                    <input aria-label="비밀번호" type="password" placeholder="비밀번호" value={password}
                           onChange={(e) => setPassword(e.target.value)}/>
                </div>
            )}
            <textarea aria-label="댓글 내용" rows={3} required placeholder={parentId ? '답글을 입력하세요' : '댓글을 입력하세요'}
                      value={content} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <button type="submit" className="primary">{parentId ? '답글 등록' : '댓글 등록'}</button>
        </form>
    );
}

export function BoardWritePage() {
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
        return <p className="muted">불러오는 중…</p>;
    }

    const isAdmin = board.permissions.isAdmin;
    const fields: FieldSpec[] = [
        {name: 'title', label: '제목', required: true, wide: true},
        ...(!user ? [
            {name: 'authorName', label: '작성자 이름', required: !articleId},
            {name: 'password', label: '비밀번호', required: true, help: '수정·삭제할 때 필요합니다.'},
        ] as FieldSpec[] : []),
        {name: 'contentHtml', label: '내용', type: 'html'},
        ...(board.supportsAttachment ? [{name: 'files', label: `첨부파일 (최대 ${board.maximumFileCount}개)`, type: 'files'} as FieldSpec] : []),
        ...(board.supportsSecret ? [{name: 'isSecret', label: '비밀글', type: 'checkbox'} as FieldSpec] : []),
        ...(isAdmin && board.supportsNotice ? [
            {name: 'isNotice', label: '공지로 등록', type: 'checkbox'},
            {name: 'noticeStartDateTime', label: '공지 시작', type: 'datetime'},
            {name: 'noticeEndDateTime', label: '공지 종료', type: 'datetime'},
        ] as FieldSpec[] : []),
        ...(isAdmin ? [{name: 'isHidden', label: '숨김(관리자만 보기)', type: 'checkbox'} as FieldSpec] : []),
    ];

    return (
        <section className="card">
            <h1>{board.name} · {articleId ? '글 수정' : '글쓰기'}</h1>
            <SchemaForm
                fields={fields}
                initial={initial}
                submitLabel={articleId ? '수정' : '등록'}
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
