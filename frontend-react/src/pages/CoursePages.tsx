import {type FormEvent, useCallback, useEffect, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import {type Course, courseApi, type CourseLesson, type QuizResult} from '../api/courses';
import type {Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../components/format';
import {ENROLLMENT_STATUS} from '../components/labels';
import {ListSearch, pageParams} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';

export function CourseListPage() {
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const [data, setData] = useState<Page<Course> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        courseApi.list(keyword, page).then(setData).catch(setError);
    }, [keyword, page]);
    return (
        <section className="card">
            <h1>교육 과정</h1>
            <ListSearch label="교육 과정 검색" placeholder="과정 이름 검색"/>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((course) => (
                    <li key={course.id}>
                        <Link to={`/courses/${course.id}`}><strong>{course.title}</strong></Link>
                        <span className="muted small"> {course.lessonCount}차시</span>
                        {course.description && <p className="muted small">{course.description}</p>}
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : '열린 교육 과정이 없습니다.'}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => setParams(pageParams(keyword, next))}/>}
        </section>
    );
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
    return (
        <section className="card">
            <h1>{course.title}</h1>
            {course.description && <p className="muted">{course.description}</p>}
            <p className="muted small">{course.lessonCount}차시{course.sequential && ' · 순서대로 진행'}</p>
            <ErrorBanner error={error}/>
            {user?.userType !== 'MEMBER' ? (
                <MemberOnlyNotice message="회원만 수강할 수 있습니다."/>
            ) : !mine ? (
                <button type="button" className="primary" onClick={enroll}>수강 신청</button>
            ) : (
                <p className={mine.status === 'COMPLETED' ? 'notice-box ok' : 'notice-box'}>
                    {ENROLLMENT_STATUS[mine.status]} · {mine.completedLessons} / {course.lessonCount}차시 완료
                    {mine.completedAt && ` · ${formatDateTime(mine.completedAt)} 과정 완료`}
                </p>
            )}
            <ol className="lesson-list">
                {(course.lessons ?? []).map((lesson) => (
                    <LessonItem key={lesson.id} course={course} lesson={lesson} enrolled={Boolean(mine)} onChange={reload}/>
                ))}
            </ol>
        </section>
    );
}

function LessonItem({course, lesson, enrolled, onChange}: {
    course: Course; lesson: CourseLesson; enrolled: boolean; onChange: () => void;
}) {
    const [open, setOpen] = useState(false);
    return (
        <li className={lesson.completed ? 'lesson done' : lesson.locked ? 'lesson locked' : 'lesson'}>
            <div className="lesson-head">
                <strong>{lesson.sequence}차시 {lesson.title}</strong>
                {lesson.completed && <span className="badge ok">완료</span>}
                {enrolled && lesson.locked && <span className="badge">앞 차시를 먼저 마쳐 주세요</span>}
            </div>
            {lesson.streamingId != null && (
                <p className="small">
                    강의: <Link to={`/streaming/${lesson.streamingId}`}>{lesson.streamingTitle ?? '강의 보기'}</Link>
                    {lesson.durationSeconds != null && ` (${formatDuration(lesson.durationSeconds)})`}
                    {enrolled && (lesson.streamingCompleted ? ' · 시청 완료' : ' · 아직 다 보지 않음')}
                </p>
            )}
            {lesson.quiz.length > 0 && (
                <p className="small">
                    퀴즈 {lesson.quiz.length}문제 · {lesson.passScore}점 이상 통과{enrolled && lesson.quizPassed && ' · 통과'}
                    {enrolled && !lesson.locked && !lesson.quizPassed && (
                        <> <button type="button" onClick={() => setOpen(!open)}>{open ? '퀴즈 닫기' : '퀴즈 풀기'}</button></>
                    )}
                </p>
            )}
            {open && <QuizForm courseId={course.id} lesson={lesson} onPassed={() => {
                setOpen(false);
                onChange();
            }}/>}
        </li>
    );
}

function QuizForm({courseId, lesson, onPassed}: { courseId: number; lesson: CourseLesson; onPassed: () => void }) {
    const [answers, setAnswers] = useState<(number | null)[]>(lesson.quiz.map(() => null));
    const [result, setResult] = useState<QuizResult | null>(null);
    const [error, setError] = useState<unknown>(null);
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            const graded = await courseApi.submitQuiz(courseId, lesson.id, answers.map((answer) => answer ?? -1));
            setResult(graded);
            if (graded.passed) {
                onPassed();
            }
        } catch (e) {
            setError(e);
        }
    };
    return (
        <form className="quiz" onSubmit={submit} aria-label={`${lesson.sequence}차시 퀴즈`}>
            {lesson.quiz.map((question, q) => (
                <fieldset key={q} className={result && !result.correct[q] ? 'quiz-question wrong' : 'quiz-question'}>
                    <legend>{q + 1}. {question.question}</legend>
                    {question.options.map((option, i) => (
                        <label key={i} className="checkbox">
                            <input type="radio" name={`lesson-${lesson.id}-q${q}`} checked={answers[q] === i}
                                   onChange={() => setAnswers(answers.map((a, idx) => (idx === q ? i : a)))}/> {option}
                        </label>
                    ))}
                    {result && !result.correct[q] && <p className="field-error">틀렸습니다.</p>}
                </fieldset>
            ))}
            <ErrorBanner error={error}/>
            {result && !result.passed && (
                <p className="notice-box">{result.score}점으로 통과 점수({result.passScore}점)에 못 미쳤습니다. 다시 풀어 보세요. (시도 {result.attempts}회)</p>
            )}
            <button type="submit" className="primary" disabled={answers.some((answer) => answer == null)}>제출</button>
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
            <h1>교육 과정</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>과정</th><th>상태</th><th>진도</th><th>수강 신청</th><th>완료</th></tr></thead>
                    <tbody>
                    {data?.content.map((course) => (
                        <tr key={course.id}>
                            <td>{course.isUsing ? <Link to={`/courses/${course.id}`}>{course.title}</Link> : course.title}</td>
                            <td>{course.myProgress ? ENROLLMENT_STATUS[course.myProgress.status] : '-'}</td>
                            <td>{course.myProgress?.completedLessons ?? 0} / {course.lessonCount}차시</td>
                            <td className="small">{formatDateTime(course.myProgress?.enrolledAt)}</td>
                            <td className="small">{formatDateTime(course.myProgress?.completedAt)}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">수강 중인 과정이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
