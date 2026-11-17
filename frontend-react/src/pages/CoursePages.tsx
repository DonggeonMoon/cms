import {type FormEvent, useCallback, useEffect, useRef, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import {type Course, courseApi, type CourseLesson, type MaterialInfo, type QuizResult, type QuizStartResponse} from '../api/courses';
import type {Comment} from '../api/domain';
import type {Page} from '../api/types';
import {won} from '../components/AdminCrudPage';
import {useAuth} from '../auth/AuthContext';
import {CommentEditor} from '../components/CommentEditor';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../components/format';
import {ENROLLMENT_STATUS} from '../components/labels';
import {ListSearch, pageParams} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';

function CourseOrSeriesListPage({isCourse, title, basePath}: { isCourse: boolean; title: string; basePath: string }) {
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<Course> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        courseApi.list(keyword, page, isCourse).then(setData).catch(setError);
    }, [keyword, page, isCourse]);
    return (
        <section className="card">
            <h1>{title}</h1>
            <ListSearch label={`${title} 검색`} placeholder="이름 검색"/>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((course) => (
                    <li key={course.id}>
                        <Link to={`${basePath}/${course.id}`}><strong>{course.title}</strong></Link>
                        <span className="muted small"> {course.lessonCount}차시</span>
                        {course.description && <p className="muted small">{course.description}</p>}
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : `열린 ${title}이 없습니다.`}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

export function CourseListPage() {
    return <CourseOrSeriesListPage isCourse={true} title="교육과정" basePath="/courses"/>;
}

export function SeriesListPage() {
    return <CourseOrSeriesListPage isCourse={false} title="시리즈" basePath="/series"/>;
}

export function CourseDetailPage() {
    const id = Number(useParams().id);
    const {user} = useAuth();
    const [course, setCourse] = useState<Course | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        courseApi.get(id).then(setCourse).catch(setError);
    }, [id]);
    useEffect(reload, [reload]);

    if (!course) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    const mine = course.myProgress;
    const enroll = () => {
        setError(null);
        courseApi.enroll(course.id).then(setCourse).catch(setError);
    };
    const cancelApplication = () => {
        setError(null);
        courseApi.cancelApplication(course.id).then(reload).catch(setError);
    };
    const accessOpen = course.myAccessOpen !== false;
    return (
        <section className="card">
            <h1>{course.title}</h1>
            {course.description && <p className="muted">{course.description}</p>}
            <p className="muted small">{course.lessonCount}차시{course.sequential && ' · 순서대로 진행'}</p>
            {course.isCourse && (course.applicationStartAt || course.applicationEndAt) && (
                <p className="muted small">
                    신청 기간: {course.applicationStartAt ? formatDateTime(course.applicationStartAt) : '제한 없음'} ~{' '}
                    {course.applicationEndAt ? formatDateTime(course.applicationEndAt) : '제한 없음'}
                </p>
            )}
            {course.isCourse && course.accessPeriodMode === 'FIXED' && (
                <p className="muted small">
                    수강 가능 기간: {formatDateTime(course.accessStartAt)} ~ {formatDateTime(course.accessEndAt)}
                </p>
            )}
            {course.isCourse && course.accessPeriodMode === 'DAYS_FROM_ENROLLMENT' && (
                <p className="muted small">수강 신청일로부터 {course.accessDays}일간 수강할 수 있습니다.</p>
            )}
            <ErrorBanner error={error}/>
            {user?.userType !== 'MEMBER' ? (
                <MemberOnlyNotice message="회원만 수강할 수 있습니다."/>
            ) : mine ? (
                <>
                    <p className={mine.status === 'COMPLETED' ? 'notice-box ok' : 'notice-box'}>
                        {ENROLLMENT_STATUS[mine.status]} · {mine.completedLessons} / {course.lessonCount}차시 완료
                        {course.isCourse && course.completionThresholdPercent < 100 && ` (수료 기준 ${course.completionThresholdPercent}%)`}
                        {mine.completedAt && ` · ${formatDateTime(mine.completedAt)} 과정 완료`}
                        {course.myAccessUntil && ` · 수강 기한 ${formatDateTime(course.myAccessUntil)}`}
                        {course.myAttendanceOk != null && ` · 출석 ${course.myAttendanceOk ? '인정' : '미인정'}`}
                    </p>
                    {!accessOpen && <p className="notice-box">수강 기간이 지나 더 이상 콘텐츠에 접근할 수 없습니다. 이미 쌓인 진도·수료 기록은 계속 조회할 수 있습니다.</p>}
                </>
            ) : course.paid ? (
                <div className="notice-box media-purchase">
                    <p><strong>{won(course.price ?? 0)}</strong></p>
                    <Link className="button primary" to={`/orders/new?productId=${course.productId}&quantity=1`}>구매하기</Link>
                </div>
            ) : course.myApplicationStatus === 'APPLIED' ? (
                <p className="notice-box">
                    수강 신청을 접수했습니다. 관리자 승인을 기다리는 중입니다.{' '}
                    <button type="button" onClick={cancelApplication}>신청 취소</button>
                </p>
            ) : !course.applicationOpen ? (
                <p className="notice-box">지금은 수강 신청 기간이 아닙니다.</p>
            ) : (
                <>
                    {course.myApplicationStatus === 'REJECTED' && <p className="notice-box">수강 신청이 반려되었습니다. 다시 신청할 수 있습니다.</p>}
                    <button type="button" className="primary" onClick={enroll}>
                        {course.enrollmentMode === 'APPROVAL' ? '수강 신청(승인 필요)' : '수강 신청'}
                    </button>
                </>
            )}
            {Boolean(mine) && accessOpen && (course.materials?.length ?? 0) > 0 && (
                <MaterialList title="학습자료" materials={course.materials ?? []}/>
            )}
            <ol className="lesson-list">
                {(course.lessons ?? []).map((lesson) => (
                    <LessonItem key={lesson.id} course={course} lesson={lesson} enrolled={Boolean(mine)}
                                accessOpen={accessOpen} onChange={reload}/>
                ))}
            </ol>
        </section>
    );
}

function MaterialList({title, materials}: { title: string; materials: MaterialInfo[] }) {
    return (
        <div className="materials">
            {title && <p className="small">{title}</p>}
            <ul className="file-list">
                {materials.map((material) => (
                    <li key={material.id}>
                        <a href={material.downloadUrl}>{material.title}</a>
                    </li>
                ))}
            </ul>
        </div>
    );
}

function LessonItem({course, lesson, enrolled, accessOpen, onChange}: {
    course: Course; lesson: CourseLesson; enrolled: boolean; accessOpen: boolean; onChange: () => void;
}) {
    const {user} = useAuth();
    const [open, setOpen] = useState(false);
    const [showComments, setShowComments] = useState(false);
    const canUseComments = user?.userType === 'MEMBER' || user?.userType === 'ADMIN';
    return (
        <li className={lesson.completed ? 'lesson done' : lesson.locked ? 'lesson locked' : 'lesson'}>
            <div className="lesson-head">
                <strong>{lesson.sequence}차시 {lesson.title}</strong>
                {lesson.mandatory && <span className="badge">필수</span>}
                {lesson.completed && <span className="badge ok">완료</span>}
                {enrolled && lesson.locked && <span className="badge">앞 차시를 먼저 마쳐 주세요</span>}
            </div>
            {lesson.streamingId != null && (
                <p className="small">
                    강의: <Link to={`/streaming/${lesson.streamingId}?courseId=${course.id}&lessonId=${lesson.id}`}>
                        {lesson.streamingTitle ?? '강의 보기'}
                    </Link>
                    {lesson.durationSeconds != null && ` (${formatDuration(lesson.durationSeconds)})`}
                    {enrolled && (lesson.streamingCompleted
                        ? ' · 시청 완료'
                        : lesson.lastPositionSeconds ? ` · 이어보기 (${formatDuration(lesson.lastPositionSeconds)}부터, 진도율 ${lesson.progressRate ?? 0}%)`
                            : ' · 아직 다 보지 않음')}
                </p>
            )}
            {lesson.quiz.length > 0 && (() => {
                const attemptsExhausted = lesson.attemptsAllowed != null && (lesson.attemptsUsed ?? 0) >= lesson.attemptsAllowed;
                return (
                    <p className="small">
                        퀴즈 {lesson.quiz.length}문제 · {lesson.passScore}점 이상 통과
                        {lesson.attemptsAllowed != null && ` · 응시 ${lesson.attemptsUsed ?? 0}/${lesson.attemptsAllowed}회`}
                        {enrolled && lesson.quizPassed && ` · 통과 (${lesson.quizScore}점)`}
                        {enrolled && !lesson.quizPassed && lesson.quizScore != null && ` · 최근 ${lesson.quizScore}점`}
                        {enrolled && !lesson.locked && !lesson.quizPassed && accessOpen && !attemptsExhausted && (
                            <> <button type="button" onClick={() => setOpen(!open)}>{open ? '퀴즈 닫기' : '퀴즈 풀기'}</button></>
                        )}
                        {enrolled && !lesson.quizPassed && !accessOpen && ' · 수강 기간이 지나 응시할 수 없습니다'}
                        {enrolled && !lesson.quizPassed && accessOpen && attemptsExhausted &&
                            ' · 재응시 가능 횟수를 모두 사용했습니다. 관리자에게 문의해 주세요.'}
                    </p>
                );
            })()}
            {open && <QuizForm courseId={course.id} lesson={lesson} onPassed={() => {
                setOpen(false);
                onChange();
            }}/>}
            {enrolled && accessOpen && lesson.materials.length > 0 && <MaterialList title="" materials={lesson.materials}/>}
            {canUseComments && (
                <p className="small">
                    <button type="button" className="link-button" onClick={() => setShowComments(!showComments)}>
                        {showComments ? '질문·답글 닫기' : '질문·답글'}
                    </button>
                </p>
            )}
            {showComments && <LessonComments courseId={course.id} lessonId={lesson.id}/>}
        </li>
    );
}

function LessonComments({courseId, lessonId}: { courseId: number; lessonId: number }) {
    const [comments, setComments] = useState<Comment[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [replyTo, setReplyTo] = useState<number | null>(null);
    const [editing, setEditing] = useState<number | null>(null);

    const reload = useCallback(() => {
        courseApi.comments(courseId, lessonId).then(setComments).catch(setError);
    }, [courseId, lessonId]);
    useEffect(reload, [reload]);

    const remove = async (comment: Comment) => {
        try {
            await courseApi.removeComment(courseId, lessonId, comment.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="lesson-comments">
            <h3>질문·답글 {comments.filter((c) => !c.deleted).length}</h3>
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
                                           onSave={async (content) => {
                                               await courseApi.editComment(courseId, lessonId, comment.id, content);
                                               setEditing(null);
                                               reload();
                                           }}/>
                        ) : (
                            <>
                                <p className={comment.deleted ? 'muted' : ''}>{comment.content}</p>
                                <div className="toolbar comment-actions">
                                    {comment.depth === 0 && !comment.deleted && (
                                        <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>답글</button>
                                    )}
                                    {comment.canEdit && <button type="button" className="link-button" onClick={() => setEditing(comment.id)}>수정</button>}
                                    {comment.canDelete && <button type="button" className="link-button" onClick={() => remove(comment)}>삭제</button>}
                                </div>
                            </>
                        )}
                        {replyTo === comment.id && (
                            <LessonCommentForm courseId={courseId} lessonId={lessonId} parentId={comment.id}
                                               onDone={() => {
                                                   setReplyTo(null);
                                                   reload();
                                               }}/>
                        )}
                    </li>
                ))}
                {comments.length === 0 && <li className="muted">아직 질문이 없습니다.</li>}
            </ul>
            <LessonCommentForm courseId={courseId} lessonId={lessonId} parentId={null} onDone={reload}/>
        </div>
    );
}

function LessonCommentForm({courseId, lessonId, parentId, onDone}: {
    courseId: number; lessonId: number; parentId: number | null; onDone: () => void;
}) {
    const [content, setContent] = useState('');
    const [error, setError] = useState<unknown>(null);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setError(null);
            await courseApi.addComment(courseId, lessonId, parentId, content);
            setContent('');
            onDone();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <form className="comment-form" onSubmit={submit}>
            <textarea aria-label="질문/답글 내용" rows={3} required placeholder={parentId ? '답글을 입력하세요' : '질문을 입력하세요'}
                      value={content} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <button type="submit" className="primary">{parentId ? '답글 등록' : '질문 등록'}</button>
        </form>
    );
}

function QuizForm({courseId, lesson, onPassed}: { courseId: number; lesson: CourseLesson; onPassed: () => void }) {
    const [started, setStarted] = useState<QuizStartResponse | null>(null);
    const [answers, setAnswers] = useState<(number | null)[]>([]);
    const [result, setResult] = useState<QuizResult | null>(null);
    const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null);
    const [error, setError] = useState<unknown>(null);
    const submitting = useRef(false);

    useEffect(() => {
        courseApi.startQuiz(courseId, lesson.id).then((response) => {
            setStarted(response);
            setAnswers(response.questions.map(() => null));
        }).catch(setError);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [courseId, lesson.id]);

    const submit = useCallback(async (currentAnswers: (number | null)[]) => {
        if (submitting.current) {
            return;
        }
        submitting.current = true;
        setError(null);
        try {
            const graded = await courseApi.submitQuiz(courseId, lesson.id, currentAnswers.map((answer) => answer ?? -1));
            setResult(graded);
            if (graded.passed) {
                onPassed();
            }
        } catch (e) {
            setError(e);
        } finally {
            submitting.current = false;
        }
    }, [courseId, lesson.id, onPassed]);

    useEffect(() => {
        if (!started || result || started.timeLimitMinutes == null) {
            setRemainingSeconds(null);
            return;
        }
        const deadline = new Date(started.startedAt).getTime() + started.timeLimitMinutes * 60_000;
        const tick = () => {
            const left = Math.max(0, Math.round((deadline - Date.now()) / 1000));
            setRemainingSeconds(left);
            if (left <= 0) {
                void submit(answers);
            }
        };
        tick();
        const timer = window.setInterval(tick, 1000);
        return () => window.clearInterval(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [started, result]);

    const onSubmit = (event: FormEvent) => {
        event.preventDefault();
        void submit(answers);
    };

    if (!started) {
        return <ErrorBanner error={error}/>;
    }
    return (
        <form className="quiz" onSubmit={onSubmit} aria-label={`${lesson.sequence}차시 퀴즈`}>
            {remainingSeconds != null && !result && (
                <p className={remainingSeconds <= 30 ? 'notice-box' : 'muted small'}>
                    남은 시간 {Math.floor(remainingSeconds / 60)}:{String(remainingSeconds % 60).padStart(2, '0')}
                </p>
            )}
            {started.questions.map((question, q) => (
                <fieldset key={q} className={result && !result.correct[q] ? 'quiz-question wrong' : 'quiz-question'}>
                    <legend>{q + 1}. {question.question}</legend>
                    {question.options.map((option, i) => (
                        <label key={i} className="checkbox">
                            <input type="radio" name={`lesson-${lesson.id}-q${q}`} checked={answers[q] === i} disabled={Boolean(result)}
                                   onChange={() => setAnswers(answers.map((a, idx) => (idx === q ? i : a)))}/> {option}
                        </label>
                    ))}
                    {result && !result.correct[q] && <p className="field-error">틀렸습니다.</p>}
                    {result?.revealed && result.answerIndexes && (
                        <p className="muted small">정답: {question.options[result.answerIndexes[q]]}</p>
                    )}
                    {result?.revealed && result.explanations?.[q] && <p className="muted small">해설: {result.explanations[q]}</p>}
                </fieldset>
            ))}
            <ErrorBanner error={error}/>
            {result && !result.passed && (
                <p className="notice-box">
                    {result.score}점으로 통과 점수({result.passScore}점)에 못 미쳤습니다. (시도 {result.attempts}회,
                    {result.attemptsRemaining == null ? ' 재응시 무제한' : ` 남은 응시 ${result.attemptsRemaining}회`})
                </p>
            )}
            {!result && (
                <button type="submit" className="primary" disabled={answers.some((answer) => answer == null)}>제출</button>
            )}
        </form>
    );
}

export function MyCoursesPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Course> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        courseApi.mine(page).then(setData).catch(setError);
    }, [page]);
    return (
        <section className="card">
            <h1>내 시리즈/교육과정</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>이름</th><th>상태</th><th>진도</th><th>수강 신청</th><th>완료</th><th>수강 기한</th></tr></thead>
                    <tbody>
                    {data?.content.map((course) => (
                        <tr key={course.id}>
                            <td>{course.isUsing
                                ? <Link to={`${course.isCourse ? '/courses' : '/series'}/${course.id}`}>{course.title}</Link>
                                : course.title}</td>
                            <td>{course.myProgress ? ENROLLMENT_STATUS[course.myProgress.status] : '-'}</td>
                            <td>{course.myProgress?.completedLessons ?? 0} / {course.lessonCount}차시</td>
                            <td className="small">{formatDateTime(course.myProgress?.enrolledAt)}</td>
                            <td className="small">{formatDateTime(course.myProgress?.completedAt)}</td>
                            <td className="small">
                                {course.myAccessUntil ? formatDateTime(course.myAccessUntil) : '무제한'}
                                {course.myAccessOpen === false && <span className="badge">종료</span>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">수강 중인 과정이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
