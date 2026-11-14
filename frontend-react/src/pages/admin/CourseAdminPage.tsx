import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import {
    type Course, type CourseApplication, courseAdminApi, type CourseLearner, type LessonDraft, lessonBody, materialsToFileInfos, toLessonDrafts,
} from '../../api/courses';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import type {FileInfo, Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {ACCESS_PERIOD_MODE, COURSE_APPLICATION_STATUS, COURSE_ENROLLMENT_MODE, ENROLLMENT_STATUS, toOptions} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {CourseQnaAdminPage} from './CourseQnaAdminPage';
import {CourseStatisticsPage} from './CourseStatisticsPage';
import {QuizAdminPage} from './QuizAdminPage';
import {StreamingAdminPage} from './StreamingAdminPage';

const FIELDS: FieldSpec[] = [
    {name: 'isUsing', label: '공개', type: 'checkbox'},
    {name: 'title', label: '이름', required: true, wide: true},
    {name: 'sequential', label: '항목을 순서대로 듣기', type: 'checkbox', visibleWhen: (v) => Boolean(v.isCourse),
        help: '켜면 앞 항목을 마쳐야 다음 항목의 퀴즈를 풀 수 있습니다.'},
    {name: 'enrollmentMode', label: '수강 신청 방식', type: 'select', visibleWhen: (v) => Boolean(v.isCourse),
        options: toOptions(COURSE_ENROLLMENT_MODE), required: true,
        help: '자유 수강은 신청 없이 바로 수강, 신청 후 수강은 신청하면 바로 수강, 승인제는 관리자가 승인해야 수강할 수 있습니다.'},
    {name: 'applicationStartAt', label: '신청 가능 기간(모집 기간) 시작', type: 'datetime', visibleWhen: (v) => Boolean(v.isCourse),
        help: '비워두면 시작 제한이 없습니다.'},
    {name: 'applicationEndAt', label: '신청 가능 기간(모집 기간) 종료', type: 'datetime', visibleWhen: (v) => Boolean(v.isCourse),
        help: '비워두면 끝까지 신청할 수 있습니다. 기간이 지나면 신청(수강) 자체가 막힙니다.'},
    {name: 'accessPeriodMode', label: '수강 가능 기간', type: 'select', visibleWhen: (v) => Boolean(v.isCourse),
        options: toOptions(ACCESS_PERIOD_MODE), required: true},
    {name: 'accessStartAt', label: '수강 가능 기간 시작', type: 'datetime',
        visibleWhen: (v) => Boolean(v.isCourse) && v.accessPeriodMode === 'FIXED'},
    {name: 'accessEndAt', label: '수강 가능 기간 종료', type: 'datetime',
        visibleWhen: (v) => Boolean(v.isCourse) && v.accessPeriodMode === 'FIXED'},
    {name: 'accessDays', label: '수강 시작일로부터 수강 가능 일수', type: 'number', min: 1,
        visibleWhen: (v) => Boolean(v.isCourse) && v.accessPeriodMode === 'DAYS_FROM_ENROLLMENT'},
    {name: 'attendanceRequired', label: '출석 기준 사용', type: 'checkbox',
        visibleWhen: (v) => Boolean(v.isCourse) && v.accessPeriodMode !== 'UNLIMITED',
        help: '켜면 수강 기간이 끝나기 전에 수료해야 출석으로 인정됩니다. 수강 기간이 무제한이면 쓸 수 없습니다.'},
    {name: 'completionThresholdPercent', label: '수료에 필요한 전체 진도율(%)', type: 'number', min: 1, visibleWhen: (v) => Boolean(v.isCourse),
        help: '비워두면 100%(전 항목 완료)입니다. 필수로 지정한 항목은 이 비율과 무관하게 항상 완료해야 합니다.'},
    {name: 'maxPlaybackRate', label: '최대 배속', type: 'number', min: 1, visibleWhen: (v) => Boolean(v.isCourse),
        help: '예: 1을 넣으면 배속 재생을 막고, 1.5를 넣으면 1.5배까지 허용합니다. 비워두면 제한이 없습니다.'},
    {name: 'preventSkipping', label: '건너뛰기 방지(기본값)', type: 'checkbox', visibleWhen: (v) => Boolean(v.isCourse),
        help: '켜면 이미 본 구간까지만 앞으로 이동할 수 있습니다. 항목별로 따로 재정의할 수 있습니다.'},
    {name: 'price', label: '가격(원)', type: 'number', min: 1, required: true, visibleWhen: (v) => Boolean(v.paid)},
    {name: 'refundableProgressBelowPercent', label: '환불 가능 진도율 기준(%)', type: 'number', min: 1, visibleWhen: (v) => Boolean(v.paid),
        help: '진도율이 이 값 미만이면 환불 처리할 수 있습니다. 비워두면 관리자 재량으로 언제든 환불할 수 있습니다. 수강 시작 전만 허용하려면 작게(예: 1) 설정하세요.'},
    {name: 'description', label: '소개', type: 'textarea'},
    {name: 'thumbnailFileId', label: '썸네일', type: 'image', wide: true},
    {name: 'paid', label: '유료', type: 'checkbox',
        help: '켜면 결제한 회원만 수강할 수 있습니다. 판매용 상품이 자동으로 만들어지고(상품 목록에는 나오지 않음) 쿠폰·포인트를 쓸 수 있습니다. 결제가 끝나면 수강 신청 방식과 무관하게 바로 수강할 수 있습니다.'},
    {name: 'materials', label: '교육과정 전체 학습자료', type: 'files', wide: true,
        help: '수강 권한이 있는 회원만 내려받을 수 있습니다.'},
    {name: 'lessons', label: '항목', type: 'courseLessons',
        help: '항목마다 강의(스트리밍)와(교육과정인 경우) 퀴즈를 넣을 수 있습니다. 강의는 완료 진도율을 넘기면, 퀴즈는 통과 점수를 넘기면 끝나고, 모든 항목을 마치면 수료입니다. 항목별 학습자료도 따로 붙일 수 있습니다.'},
];

const courseGradeFields = (grades: Grade[]) => [gradeSelectField('accessGradeId', '수강 가능 등급', grades, '이 등급 이상 회원만 수강할 수 있습니다.')];

const toBody = ({lessons, materials, ...values}: FormValues) => ({
    ...values,
    lessons: lessonBody((lessons as LessonDraft[]) ?? []),
    materialFileIds: ((materials as FileInfo[]) ?? []).map((file) => file.id),
});

type Panel = 'learners' | 'applications';

type TopTab = 'streaming' | 'series' | 'course';

const TOP_TABS: { value: TopTab; label: string }[] = [
    {value: 'streaming', label: '비디오·오디오'},
    {value: 'series', label: '시리즈'},
    {value: 'course', label: '교육과정'},
];

type CourseSubTab = 'list' | 'quizzes' | 'statistics' | 'qna';

const COURSE_SUB_TABS: { value: CourseSubTab; label: string }[] = [
    {value: 'list', label: '교육과정'},
    {value: 'quizzes', label: '퀴즈'},
    {value: 'qna', label: '미답변 질문'},
    {value: 'statistics', label: '통계'},
];

export function CourseAdminPage() {
    const [tab, setTab] = useState<TopTab>('streaming');
    return (
        <>
            <div className="tabs" role="tablist" aria-label="스트리밍/교육과정 관리">
                {TOP_TABS.map((t) => (
                    <button key={t.value} type="button" role="tab" aria-selected={tab === t.value}
                            className={tab === t.value ? 'tab active' : 'tab'} onClick={() => setTab(t.value)}>
                        {t.label}
                    </button>
                ))}
            </div>
            {tab === 'streaming' && <StreamingAdminPage/>}
            {tab === 'series' && <CourseCrudPage isCourse={false} title="시리즈"
                description="비디오·오디오를 항목으로 묶어 시리즈를 만듭니다. 퀴즈·수강 신청·수강 진행 관리가 필요하면 교육과정 탭을 쓰세요."/>}
            {tab === 'course' && <CourseTabs/>}
        </>
    );
}

function CourseTabs() {
    const [sub, setSub] = useState<CourseSubTab>('list');
    return (
        <>
            <div className="tabs" role="tablist" aria-label="교육과정 관리">
                {COURSE_SUB_TABS.map((t) => (
                    <button key={t.value} type="button" role="tab" aria-selected={sub === t.value}
                            className={sub === t.value ? 'tab active' : 'tab'} onClick={() => setSub(t.value)}>
                        {t.label}
                    </button>
                ))}
            </div>
            {sub === 'list' && <CourseCrudPage isCourse={true} title="교육과정"
                description="비디오·오디오·퀴즈를 항목으로 묶어 퀴즈·수강 신청·수강 진행·수료를 관리하는 교육과정입니다."/>}
            {sub === 'quizzes' && <QuizAdminPage/>}
            {sub === 'statistics' && <CourseStatisticsPage/>}
            {sub === 'qna' && <CourseQnaAdminPage/>}
        </>
    );
}

function CourseCrudPage({isCourse, title, description}: { isCourse: boolean; title: string; description: string }) {
    const fields = useWithGradeFields(FIELDS, courseGradeFields);
    const [open, setOpen] = useState<{ id: number; panel: Panel } | null>(null);
    const toggle = (id: number, panel: Panel) =>
        setOpen(open?.id === id && open.panel === panel ? null : {id, panel});
    const loadCourses = (query: Query) => courseAdminApi.search(query);
    return (
        <AdminCrudPage<Course>
            title={title}
            description={<p>{description}</p>}
            load={loadCourses}
            extraQuery={{isCourse}}
            fields={fields}
            emptyForm={{isUsing: true, isCourse, sequential: true, enrollmentMode: 'FREE', accessPeriodMode: 'UNLIMITED',
                attendanceRequired: false, preventSkipping: false, paid: false, materials: [],
                lessons: [{id: null, title: '', unitName: '', streamingId: null, quizId: null, materials: [], mandatory: false, preventSkippingOverride: null}]}}
            toForm={(course) => courseAdminApi.get(course.id).then((full) => ({...full,
                lessons: toLessonDrafts(full.lessons ?? []), materials: materialsToFileInfos(full.materials ?? [])}))}
            create={(values) => courseAdminApi.create(toBody({...values, isCourse}))}
            update={(course, values) => courseAdminApi.update(course.id, toBody({...values, isCourse}))}
            remove={(course) => courseAdminApi.remove(course.id)}
            columns={[
                {label: 'ID', render: (c) => c.id},
                {label: '이름', render: (c) => <Link to={`/courses/${c.id}`}>{c.title}</Link>},
                {label: '수강 신청', render: (c) => (c.isCourse ? COURSE_ENROLLMENT_MODE[c.enrollmentMode] : '-')},
                {label: '항목', render: (c) => `${c.lessonCount}개`},
                {label: '순서대로', render: (c) => <YesNo value={c.sequential}/>},
                {label: '공개', render: (c) => <YesNo value={c.isUsing}/>},
            ]}
            actions={(c) => (
                <>
                    <button type="button" onClick={() => toggle(c.id, 'learners')}>
                        {open?.id === c.id && open.panel === 'learners' ? '수강생 닫기' : '수강생'}
                    </button>
                    {c.isCourse && c.enrollmentMode !== 'FREE' && (
                        <button type="button" onClick={() => toggle(c.id, 'applications')}>
                            {open?.id === c.id && open.panel === 'applications' ? '신청자 닫기' : '신청자'}
                        </button>
                    )}
                </>
            )}
            expandedRow={(c) => {
                if (open?.id !== c.id) {
                    return null;
                }
                return open.panel === 'learners' ? <Learners courseId={c.id}/> : <Applications courseId={c.id}/>;
            }}
        />
    );
}

function Applications({courseId}: { courseId: number }) {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<CourseApplication> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = () => {
        courseAdminApi.applications(courseId, page).then(setData).catch(setError);
    };
    useEffect(reload, [courseId, page]);
    const decide = async (applicationId: number, approve: boolean) => {
        setError(null);
        try {
            await (approve ? courseAdminApi.approveApplication(courseId, applicationId) : courseAdminApi.rejectApplication(courseId, applicationId));
            reload();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="submissions">
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>회원</th><th>상태</th><th>신청일</th><th>처리일</th><th/></tr></thead>
                    <tbody>
                    {data?.content.map((application) => (
                        <tr key={application.id}>
                            <td>{application.username}</td>
                            <td><span className={application.status === 'APPROVED' ? 'badge ok' : 'badge'}>
                                {COURSE_APPLICATION_STATUS[application.status]}
                            </span></td>
                            <td className="small">{formatDateTime(application.appliedAt)}</td>
                            <td className="small">{formatDateTime(application.decidedAt)}</td>
                            <td>
                                {application.status === 'APPLIED' && (
                                    <>
                                        <button type="button" onClick={() => decide(application.id, true)}>승인</button>
                                        <button type="button" className="danger" onClick={() => decide(application.id, false)}>반려</button>
                                    </>
                                )}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">신청자가 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </div>
    );
}

function Learners({courseId}: { courseId: number }) {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<CourseLearner> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [openMemberId, setOpenMemberId] = useState<number | null>(null);
    const reload = () => {
        courseAdminApi.learners(courseId, page).then(setData).catch(setError);
    };
    useEffect(reload, [courseId, page]);
    const extend = async (memberId: number, current: string | null) => {
        const input = window.prompt('새 수강 마감 일시(YYYY-MM-DDTHH:mm, 비우면 연장 해제)', current?.slice(0, 16) ?? '');
        if (input === null) {
            return;
        }
        setError(null);
        try {
            await courseAdminApi.extendAccess(courseId, memberId, input ? `${input}:00` : null);
            reload();
        } catch (e) {
            setError(e);
        }
    };
    const refund = async (memberId: number) => {
        if (!window.confirm('결제를 취소하고 수강 권한을 회수합니다. 계속할까요?')) {
            return;
        }
        setError(null);
        try {
            await courseAdminApi.refundEnrollment(courseId, memberId);
            reload();
        } catch (e) {
            setError(e);
        }
    };
    return (
        <div className="submissions">
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>회원</th><th>상태</th><th>진도</th><th>수강 신청</th><th>완료</th><th>수강 기한</th><th>출석</th><th/></tr></thead>
                    <tbody>
                    {data?.content.map((learner) => (
                        <>
                            <tr key={learner.memberId}>
                                <td>{learner.username}</td>
                                <td>{ENROLLMENT_STATUS[learner.status]}</td>
                                <td>{learner.completedLessons} / {learner.totalLessons}차시</td>
                                <td className="small">{formatDateTime(learner.enrolledAt)}</td>
                                <td className="small">{formatDateTime(learner.completedAt)}</td>
                                <td className="small">
                                    {learner.accessUntil ? formatDateTime(learner.accessUntil) : '무제한'}
                                    {learner.accessExtendedUntil && <span className="badge">연장됨</span>}
                                </td>
                                <td>
                                    {learner.attendanceOk == null ? '-' : (
                                        <span className={learner.attendanceOk ? 'badge ok' : 'badge'}>{learner.attendanceOk ? '인정' : '미인정'}</span>
                                    )}
                                </td>
                                <td>
                                    <button type="button" onClick={() => setOpenMemberId(openMemberId === learner.memberId ? null : learner.memberId)}>
                                        {openMemberId === learner.memberId ? '항목 진행 닫기' : '항목 진행'}
                                    </button>
                                    <button type="button" onClick={() => extend(learner.memberId, learner.accessUntil)}>연장</button>
                                    <button type="button" className="danger" onClick={() => refund(learner.memberId)}>환불</button>
                                </td>
                            </tr>
                            {openMemberId === learner.memberId && (
                                <tr>
                                    <td colSpan={8}><LearnerItemProgress courseId={courseId} memberId={learner.memberId}/></td>
                                </tr>
                            )}
                        </>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={8} className="muted">수강생이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </div>
    );
}

function LearnerItemProgress({courseId, memberId}: { courseId: number; memberId: number }) {
    const [course, setCourse] = useState<Course | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = () => {
        courseAdminApi.learnerProgress(courseId, memberId).then(setCourse).catch(setError);
    };
    useEffect(reload, [courseId, memberId]);
    const grant = async (lessonId: number) => {
        const input = window.prompt('추가로 부여할 응시 횟수', '1');
        const count = input == null ? null : Number(input);
        if (!count || count <= 0) {
            return;
        }
        setError(null);
        try {
            await courseAdminApi.grantQuizAttempts(courseId, lessonId, memberId, count);
            reload();
        } catch (e) {
            setError(e);
        }
    };
    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!course) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <table className="learner-item-progress">
            <thead><tr><th>항목</th><th>강의 진도율</th><th>마지막 위치</th><th>퀴즈 점수</th><th>응시 횟수</th><th>완료</th><th/></tr></thead>
            <tbody>
            {(course.lessons ?? []).map((lesson) => (
                <tr key={lesson.id}>
                    <td>{lesson.sequence}. {lesson.title}{lesson.mandatory && <span className="badge">필수</span>}</td>
                    <td>{lesson.streamingId == null ? '-' : `${lesson.progressRate ?? 0}%`}</td>
                    <td>{lesson.streamingId == null ? '-' : formatSeconds(lesson.lastPositionSeconds ?? 0)}</td>
                    <td>{lesson.quizId == null ? '-' : (lesson.quizScore == null ? '응시 전' : `${lesson.quizScore}점${lesson.quizPassed ? ' (통과)' : ''}`)}</td>
                    <td>{lesson.quizId == null ? '-' : `${lesson.attemptsUsed ?? 0}${lesson.attemptsAllowed == null ? '' : ` / ${lesson.attemptsAllowed}`}회`}</td>
                    <td><span className={lesson.completed ? 'badge ok' : 'badge'}>{lesson.completed ? '완료' : '진행 중'}</span></td>
                    <td>{lesson.quizId != null && <button type="button" onClick={() => grant(lesson.id)}>응시 횟수 추가</button>}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

function formatSeconds(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${String(s).padStart(2, '0')}`;
}
