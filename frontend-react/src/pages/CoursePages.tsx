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
import {enrollmentStatus} from '../components/labels';
import {ListSearch, pageParams} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';
import {useI18n} from '../api/i18n';

function CourseOrSeriesListPage({isCourse, title, basePath}: { isCourse: boolean; title: string; basePath: string }) {
    const {t} = useI18n();
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
            <ListSearch label={t('shop.course.searchLabel', '{title} 검색', {title})} placeholder={t('shop.course.searchPlaceholder', '이름 검색')}/>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((course) => (
                    <li key={course.id}>
                        <Link to={`${basePath}/${course.id}`}><strong>{course.title}</strong></Link>
                        <span className="muted small"> {t('shop.course.lessonCount', '{count}차시', {count: String(course.lessonCount)})}</span>
                        {course.description && <p className="muted small">{course.description}</p>}
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">{keyword ? t('shop.common.noSearchResults', '검색 결과가 없습니다.') : t('shop.course.noneOpen', '열린 {title}이 없습니다.', {title})}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
}

export function CourseListPage() {
    const {t} = useI18n();
    return <CourseOrSeriesListPage isCourse={true} title={t('admin.course.tab.course', '교육과정')} basePath="/courses"/>;
}

export function SeriesListPage() {
    const {t} = useI18n();
    return <CourseOrSeriesListPage isCourse={false} title={t('admin.course.tab.series', '시리즈')} basePath="/series"/>;
}

export function CourseDetailPage() {
    const {t} = useI18n();
    const id = Number(useParams().id);
    const {user} = useAuth();
    const [course, setCourse] = useState<Course | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        courseApi.get(id).then(setCourse).catch(setError);
    }, [id]);
    useEffect(reload, [reload]);

    if (!course) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
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
            <p className="muted small">{t('shop.course.lessonCount', '{count}차시', {count: String(course.lessonCount)})}{course.sequential && ` ${t('shop.course.sequential', '· 순서대로 진행')}`}</p>
            {course.isCourse && (course.applicationStartAt || course.applicationEndAt) && (
                <p className="muted small">
                    {t('shop.course.applicationPeriod', '신청 기간: {start} ~ {end}', {
                        start: course.applicationStartAt ? formatDateTime(course.applicationStartAt) : t('admin.reservationItem.noLimit', '제한 없음'),
                        end: course.applicationEndAt ? formatDateTime(course.applicationEndAt) : t('admin.reservationItem.noLimit', '제한 없음'),
                    })}
                </p>
            )}
            {course.isCourse && course.accessPeriodMode === 'FIXED' && (
                <p className="muted small">
                    {t('shop.course.accessPeriod', '수강 가능 기간: {start} ~ {end}', {start: formatDateTime(course.accessStartAt), end: formatDateTime(course.accessEndAt)})}
                </p>
            )}
            {course.isCourse && course.accessPeriodMode === 'DAYS_FROM_ENROLLMENT' && (
                <p className="muted small">{t('shop.course.accessDays', '수강 신청일로부터 {days}일간 수강할 수 있습니다.', {days: String(course.accessDays)})}</p>
            )}
            <ErrorBanner error={error}/>
            {user?.userType !== 'MEMBER' ? (
                <MemberOnlyNotice message={t('shop.course.memberOnly', '회원만 수강할 수 있습니다.')}/>
            ) : mine ? (
                <>
                    <p className={mine.status === 'COMPLETED' ? 'notice-box ok' : 'notice-box'}>
                        {enrollmentStatus(t)[mine.status]} · {t('shop.course.completedOf', '{completed} / {total}차시 완료', {completed: String(mine.completedLessons), total: String(course.lessonCount)})}
                        {course.isCourse && course.completionThresholdPercent < 100 && t('shop.course.completionThreshold', ' (수료 기준 {percent}%)', {percent: String(course.completionThresholdPercent)})}
                        {mine.completedAt && t('shop.course.completedAt', ' · {date} 과정 완료', {date: formatDateTime(mine.completedAt)})}
                        {course.myAccessUntil && t('shop.course.accessUntil', ' · 수강 기한 {date}', {date: formatDateTime(course.myAccessUntil)})}
                        {course.myAttendanceOk != null && t('shop.course.attendance', ' · 출석 {status}', {status: course.myAttendanceOk ? t('admin.course.recognized', '인정') : t('admin.course.notRecognized', '미인정')})}
                    </p>
                    {!accessOpen && <p className="notice-box">{t('shop.course.accessClosedNotice', '수강 기간이 지나 더 이상 콘텐츠에 접근할 수 없습니다. 이미 쌓인 진도·수료 기록은 계속 조회할 수 있습니다.')}</p>}
                </>
            ) : course.paid ? (
                <div className="notice-box media-purchase">
                    <p><strong>{won(course.price ?? 0)}</strong></p>
                    <Link className="button primary" to={`/orders/new?productId=${course.productId}&quantity=1`}>{t('shop.action.buyPass', '구매하기')}</Link>
                </div>
            ) : course.myApplicationStatus === 'APPLIED' ? (
                <p className="notice-box">
                    {t('shop.course.applicationReceived', '수강 신청을 접수했습니다. 관리자 승인을 기다리는 중입니다.')}{' '}
                    <button type="button" onClick={cancelApplication}>{t('shop.course.cancelApplication', '신청 취소')}</button>
                </p>
            ) : !course.applicationOpen ? (
                <p className="notice-box">{t('shop.course.notApplicationPeriod', '지금은 수강 신청 기간이 아닙니다.')}</p>
            ) : (
                <>
                    {course.myApplicationStatus === 'REJECTED' && <p className="notice-box">{t('shop.course.rejectedNotice', '수강 신청이 반려되었습니다. 다시 신청할 수 있습니다.')}</p>}
                    <button type="button" className="primary" onClick={enroll}>
                        {course.enrollmentMode === 'APPROVAL' ? t('shop.course.enrollApproval', '수강 신청(승인 필요)') : t('shop.course.enroll', '수강 신청')}
                    </button>
                </>
            )}
            {Boolean(mine) && accessOpen && (course.materials?.length ?? 0) > 0 && (
                <MaterialList title={t('shop.course.materials', '학습자료')} materials={course.materials ?? []}/>
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
    const {t} = useI18n();
    const {user} = useAuth();
    const [open, setOpen] = useState(false);
    const [showComments, setShowComments] = useState(false);
    const canUseComments = user?.userType === 'MEMBER' || user?.userType === 'ADMIN';
    return (
        <li className={lesson.completed ? 'lesson done' : lesson.locked ? 'lesson locked' : 'lesson'}>
            <div className="lesson-head">
                <strong>{t('shop.course.lessonSequence', '{sequence}차시 {title}', {sequence: String(lesson.sequence), title: lesson.title})}</strong>
                {lesson.mandatory && <span className="badge">{t('admin.course.mandatory', '필수')}</span>}
                {lesson.completed && <span className="badge ok">{t('admin.course.completedColumn', '완료')}</span>}
                {enrolled && lesson.locked && <span className="badge">{t('shop.course.finishPreviousFirst', '앞 차시를 먼저 마쳐 주세요')}</span>}
            </div>
            {lesson.streamingId != null && (
                <p className="small">
                    {t('shop.course.lecture', '강의:')} <Link to={`/streaming/${lesson.streamingId}?courseId=${course.id}&lessonId=${lesson.id}`}>
                        {lesson.streamingTitle ?? t('shop.course.watchLecture', '강의 보기')}
                    </Link>
                    {lesson.durationSeconds != null && ` (${formatDuration(lesson.durationSeconds)})`}
                    {enrolled && (lesson.streamingCompleted
                        ? ` ${t('shop.course.watchCompleted', '· 시청 완료')}`
                        : lesson.lastPositionSeconds
                            ? t('shop.course.continueWatching', ' · 이어보기 ({time}부터, 진도율 {rate}%)', {time: formatDuration(lesson.lastPositionSeconds), rate: String(lesson.progressRate ?? 0)})
                            : ` ${t('shop.course.notWatchedYet', '· 아직 다 보지 않음')}`)}
                </p>
            )}
            {lesson.quiz.length > 0 && (() => {
                const attemptsExhausted = lesson.attemptsAllowed != null && (lesson.attemptsUsed ?? 0) >= lesson.attemptsAllowed;
                return (
                    <p className="small">
                        {t('shop.course.quizSummary', '퀴즈 {count}문제 · {score}점 이상 통과', {count: String(lesson.quiz.length), score: String(lesson.passScore)})}
                        {lesson.attemptsAllowed != null && t('shop.course.attemptsOf', ' · 응시 {used}/{allowed}회', {used: String(lesson.attemptsUsed ?? 0), allowed: String(lesson.attemptsAllowed)})}
                        {enrolled && lesson.quizPassed && t('shop.course.passedWithScore', ' · 통과 ({score}점)', {score: String(lesson.quizScore)})}
                        {enrolled && !lesson.quizPassed && lesson.quizScore != null && t('shop.course.recentScore', ' · 최근 {score}점', {score: String(lesson.quizScore)})}
                        {enrolled && !lesson.locked && !lesson.quizPassed && accessOpen && !attemptsExhausted && (
                            <> <button type="button" onClick={() => setOpen(!open)}>{open ? t('shop.course.closeQuiz', '퀴즈 닫기') : t('shop.course.takeQuiz', '퀴즈 풀기')}</button></>
                        )}
                        {enrolled && !lesson.quizPassed && !accessOpen && ` ${t('shop.course.accessClosedForQuiz', '· 수강 기간이 지나 응시할 수 없습니다')}`}
                        {enrolled && !lesson.quizPassed && accessOpen && attemptsExhausted &&
                            ` ${t('shop.course.attemptsExhausted', '· 재응시 가능 횟수를 모두 사용했습니다. 관리자에게 문의해 주세요.')}`}
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
                        {showComments ? t('shop.course.closeComments', '질문·답글 닫기') : t('shop.course.comments', '질문·답글')}
                    </button>
                </p>
            )}
            {showComments && <LessonComments courseId={course.id} lessonId={lesson.id}/>}
        </li>
    );
}

function LessonComments({courseId, lessonId}: { courseId: number; lessonId: number }) {
    const {t} = useI18n();
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
            <h3>{t('shop.course.comments', '질문·답글')} {comments.filter((c) => !c.deleted).length}</h3>
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
                                        <button type="button" className="link-button" onClick={() => setReplyTo(comment.id)}>{t('shop.course.reply', '답글')}</button>
                                    )}
                                    {comment.canEdit && <button type="button" className="link-button" onClick={() => setEditing(comment.id)}>{t('common.edit', '수정')}</button>}
                                    {comment.canDelete && <button type="button" className="link-button" onClick={() => remove(comment)}>{t('common.delete', '삭제')}</button>}
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
                {comments.length === 0 && <li className="muted">{t('shop.course.noQuestions', '아직 질문이 없습니다.')}</li>}
            </ul>
            <LessonCommentForm courseId={courseId} lessonId={lessonId} parentId={null} onDone={reload}/>
        </div>
    );
}

function LessonCommentForm({courseId, lessonId, parentId, onDone}: {
    courseId: number; lessonId: number; parentId: number | null; onDone: () => void;
}) {
    const {t} = useI18n();
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
            <textarea aria-label={t('shop.course.commentContent', '질문/답글 내용')} rows={3} required
                      placeholder={parentId ? t('shop.course.replyPlaceholder', '답글을 입력하세요') : t('shop.course.questionPlaceholder', '질문을 입력하세요')}
                      value={content} onChange={(e) => setContent(e.target.value)}/>
            <ErrorBanner error={error}/>
            <button type="submit" className="primary">{parentId ? t('shop.course.submitReply', '답글 등록') : t('shop.course.submitQuestion', '질문 등록')}</button>
        </form>
    );
}

function QuizForm({courseId, lesson, onPassed}: { courseId: number; lesson: CourseLesson; onPassed: () => void }) {
    const {t} = useI18n();
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
        <form className="quiz" onSubmit={onSubmit} aria-label={t('shop.course.quizAriaLabel', '{sequence}차시 퀴즈', {sequence: String(lesson.sequence)})}>
            {remainingSeconds != null && !result && (
                <p className={remainingSeconds <= 30 ? 'notice-box' : 'muted small'}>
                    {t('shop.course.timeRemaining', '남은 시간 {time}', {time: `${Math.floor(remainingSeconds / 60)}:${String(remainingSeconds % 60).padStart(2, '0')}`})}
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
                    {result && !result.correct[q] && <p className="field-error">{t('shop.course.wrong', '틀렸습니다.')}</p>}
                    {result?.revealed && result.answerIndexes && (
                        <p className="muted small">{t('shop.course.correctAnswer', '정답: {answer}', {answer: question.options[result.answerIndexes[q]]})}</p>
                    )}
                    {result?.revealed && result.explanations?.[q] && <p className="muted small">{t('shop.course.explanation', '해설: {text}', {text: result.explanations[q]})}</p>}
                </fieldset>
            ))}
            <ErrorBanner error={error}/>
            {result && !result.passed && (
                <p className="notice-box">
                    {t('shop.course.failedNotice', '{score}점으로 통과 점수({passScore}점)에 못 미쳤습니다. (시도 {attempts}회, {remaining})', {
                        score: String(result.score), passScore: String(result.passScore), attempts: String(result.attempts),
                        remaining: result.attemptsRemaining == null ? t('shop.course.unlimitedRetries', '재응시 무제한') : t('shop.course.remainingAttempts', '남은 응시 {count}회', {count: String(result.attemptsRemaining)}),
                    })}
                </p>
            )}
            {!result && (
                <button type="submit" className="primary" disabled={answers.some((answer) => answer == null)}>{t('shop.course.submit', '제출')}</button>
            )}
        </form>
    );
}

export function MyCoursesPage() {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Course> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        courseApi.mine(page).then(setData).catch(setError);
    }, [page]);
    return (
        <section className="card">
            <h1>{t('shop.course.myCourses', '내 시리즈/교육과정')}</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.board.column.name', '이름')}</th><th>{t('admin.order.column.status', '상태')}</th>
                        <th>{t('admin.course.progressColumn', '진도')}</th><th>{t('admin.course.enrolledAt', '수강 신청')}</th>
                        <th>{t('admin.course.completedColumn', '완료')}</th><th>{t('admin.course.accessUntil', '수강 기한')}</th>
                    </tr></thead>
                    <tbody>
                    {data?.content.map((course) => (
                        <tr key={course.id}>
                            <td>{course.isUsing
                                ? <Link to={`${course.isCourse ? '/courses' : '/series'}/${course.id}`}>{course.title}</Link>
                                : course.title}</td>
                            <td>{course.myProgress ? enrollmentStatus(t)[course.myProgress.status] : '-'}</td>
                            <td>{t('shop.course.lessonsOf', '{completed} / {total}차시', {completed: String(course.myProgress?.completedLessons ?? 0), total: String(course.lessonCount)})}</td>
                            <td className="small">{formatDateTime(course.myProgress?.enrolledAt)}</td>
                            <td className="small">{formatDateTime(course.myProgress?.completedAt)}</td>
                            <td className="small">
                                {course.myAccessUntil ? formatDateTime(course.myAccessUntil) : t('admin.course.unlimited', '무제한')}
                                {course.myAccessOpen === false && <span className="badge">{t('shop.course.ended', '종료')}</span>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('shop.course.noneEnrolled', '수강 중인 과정이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
