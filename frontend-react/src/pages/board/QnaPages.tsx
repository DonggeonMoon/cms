import {type FormEvent, useEffect, useState} from 'react';
import {Link, useNavigate, useParams, useSearchParams} from 'react-router';
import {ApiError} from '../../api/client';
import type {GuestPostingPolicy, QuestionDetail, QuestionSummary} from '../../api/domain';
import {qnaApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {useAuth} from '../../auth/AuthContext';
import {ListSearch} from '../../components/ListSearch';
import {MemberOnlyNotice} from '../../components/MemberOnlyNotice';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDate, formatDateTime, rowNumber} from '../../components/format';
import {HtmlView} from '../../components/HtmlView';
import {Pagination} from '../../components/Pagination';
import {type FieldSpec, SchemaForm} from '../../components/SchemaForm';
import {useFeatureMenuName} from '../../components/SiteContext';

export function QnaListPage() {
    const title = useFeatureMenuName('QNA', '질문답변');
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<QuestionSummary> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        qnaApi.list(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);

    return (
        <section className="card">
            <h1>{title}</h1>
            <div className="list-toolbar-row">
                <ListSearch label="검색어" placeholder="제목 검색"/>
                <Link className="button primary" to="/qna/write">질문하기</Link>
            </div>
            <ErrorBanner error={error}/>
            {data && (
                <>
                    <div className="table-wrap">
                        <table className="post-table">
                            <thead>
                            <tr>
                                <th className="col-no">순번</th>
                                <th>제목</th>
                                <th className="col-fit">작성자</th>
                                <th className="col-fit">작성일</th>
                                <th className="col-fit">상태</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.content.map((q, index) => (
                                <tr key={q.id}>
                                    <td className="col-no">{rowNumber(data.page, index)}</td>
                                    <td className="col-title"><Link to={`/qna/${q.id}`}>{q.title}</Link>{q.isSecret && <span className="muted small"> 🔒</span>}</td>
                                    <td className="col-fit">{q.authorName}</td>
                                    <td className="col-fit">{formatDate(q.createdAt)}</td>
                                    <td className="col-fit">{q.answered ? <span className="badge ok">답변 완료</span> : <span className="badge">답변 대기</span>}</td>
                                </tr>
                            ))}
                            {data.content.length === 0 && <tr><td colSpan={5} className="muted">질문이 없습니다.</td></tr>}
                            </tbody>
                        </table>
                    </div>
                    <Pagination page={page} totalPages={data.page.totalPages}
                                onChange={(next) => setParams({...(keyword && {keyword}), page: String(next)})}/>
                </>
            )}
        </section>
    );
}

export function QnaDetailPage() {
    const id = Number(useParams().id);
    const navigate = useNavigate();
    const {user} = useAuth();
    const [question, setQuestion] = useState<QuestionDetail | null>(null);
    const [needsPassword, setNeedsPassword] = useState(false);
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        qnaApi.get(id).then(setQuestion).catch((e: unknown) => {
            if (e instanceof ApiError && e.code === 'ARTICLE_PASSWORD_MISMATCH') {
                setNeedsPassword(true);
            } else {
                setError(e);
            }
        });
    }, [id]);

    const unlock = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setQuestion(await qnaApi.access(id, password));
            setNeedsPassword(false);
            setError(null);
        } catch (e) {
            setError(e);
        }
    };

    if (needsPassword) {
        return (
            <section className="card narrow">
                <h1>비밀 질문입니다</h1>
                <form onSubmit={unlock}>
                    <input type="password" aria-label="질문 비밀번호" value={password} onChange={(e) => setPassword(e.target.value)}/>
                    <ErrorBanner error={error}/>
                    <button type="submit" className="primary">확인</button>
                </form>
            </section>
        );
    }
    if (error && !question) {
        return <ErrorBanner error={error}/>;
    }
    if (!question) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <article className="card">
            <Link to="/qna" className="muted">← 질문 목록</Link>
            <h1>{question.title}</h1>
            <p className="muted small align-right">{question.authorName} · {formatDateTime(question.createdAt)}{question.email && ` · ${question.email}`}</p>
            <HtmlView html={question.contentHtml}/>
            <div className="answer">
                <h2>답변</h2>
                {question.answer
                    ? <>
                        <HtmlView html={question.answer.contentHtml}/>
                        <p className="muted small align-right">{question.answer.authorName} · {formatDateTime(question.answer.modifiedAt)}</p>
                    </>
                    : <p className="muted">아직 답변이 등록되지 않았습니다.</p>}
            </div>
            <ErrorBanner error={error}/>
            {question.canModify && (
                <div className="toolbar form-actions">
                    {!question.answer && <Link className="button" to={`/qna/${id}/edit`} state={{password}}>수정</Link>}
                    <button type="button" className="danger" onClick={async () => {
                        const pw = !user ? password || window.prompt('비밀번호를 입력하세요') || undefined : undefined;
                        if (!window.confirm('질문을 삭제할까요?')) {
                            return;
                        }
                        try {
                            await qnaApi.remove(id, pw);
                            navigate('/qna');
                        } catch (e) {
                            setError(e);
                        }
                    }}>삭제</button>
                </div>
            )}
        </article>
    );
}

export function QnaWritePage() {
    const params = useParams();
    const id = params.id ? Number(params.id) : null;
    const navigate = useNavigate();
    const {user} = useAuth();
    const [guest, setGuest] = useState<GuestPostingPolicy>({allowed: true, collectPhone: false, collectEmail: true});
    useEffect(() => {
        qnaApi.guestPosting().then(setGuest).catch(() => undefined);
    }, []);
    const [initial, setInitial] = useState<Record<string, unknown> | null>(id ? null : {isSecret: true, contentHtml: ''});
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        if (id) {
            qnaApi.get(id).then((q) => setInitial({...q})).catch(setError);
        }
    }, [id]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!initial) {
        return <p className="muted">불러오는 중…</p>;
    }
    if (!id && !user && !guest.allowed) {
        return (
            <section className="card">
                <h1>질문하기</h1>
                <MemberOnlyNotice message="질문은 회원만 등록할 수 있습니다."/>
            </section>
        );
    }
    const fields: FieldSpec[] = [
        ...(!user ? [
            {name: 'authorName', label: '이름', required: true, wide: true},
            {name: 'password', label: '비밀번호', type: 'password', required: true, wide: true, help: '질문 확인·수정에 필요합니다.'},
            ...(guest.collectPhone ? [{name: 'phoneNumber', label: '연락처', required: true, wide: true}] : []),
            ...(guest.collectEmail ? [{name: 'email', label: '답변 받을 이메일', required: true, wide: true}] : []),
        ] as FieldSpec[] : []),
        {name: 'isSecret', label: '비밀 질문', type: 'checkbox', wide: true},
        {name: 'title', label: '제목', required: true, wide: true},
        {name: 'contentHtml', label: '내용', type: 'html'},
    ];
    return (
        <section className="card">
            <h1>{id ? '질문 수정' : '질문하기'}</h1>
            <SchemaForm fields={fields} initial={initial} submitLabel={id ? '수정' : '등록'} onCancel={() => navigate(-1)}
                        onSubmit={async (values) => {
                            if (id) {
                                await qnaApi.update(id, values);
                                navigate(`/qna/${id}`);
                            } else {
                                await qnaApi.create(values);
                                navigate('/qna');
                            }
                        }}/>
        </section>
    );
}
