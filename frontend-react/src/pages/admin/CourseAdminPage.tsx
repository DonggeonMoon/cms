import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Query} from '../../api/client';
import {type Course, courseAdminApi, type CourseLearner, type LessonDraft, lessonBody, toLessonDrafts} from '../../api/courses';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import type {Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {ENROLLMENT_STATUS} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'title', label: '과정 이름', required: true, wide: true},
    {name: 'isUsing', label: '공개', type: 'checkbox'},
    {name: 'sequential', label: '차시를 순서대로 듣기', type: 'checkbox',
        help: '켜면 앞 차시를 마쳐야 다음 차시 퀴즈를 풀 수 있습니다.'},
    {name: 'description', label: '소개', type: 'textarea'},
    {name: 'thumbnailFileId', label: '썸네일', type: 'image', wide: true},
    {name: 'lessons', label: '차시', type: 'courseLessons',
        help: '차시마다 강의(스트리밍)와 퀴즈를 넣을 수 있습니다. 강의는 완료 진도율을 넘기면, 퀴즈는 통과 점수를 넘기면 끝나고, 모든 차시를 마치면 과정 완료입니다.'},
];

const courseGradeFields = (grades: Grade[]) => [gradeSelectField('accessGradeId', '수강 가능 등급', grades, '이 등급 이상 회원만 수강할 수 있습니다.')];

const toBody = ({lessons, ...values}: FormValues) => ({...values, lessons: lessonBody((lessons as LessonDraft[]) ?? [])});

const loadCourses = (query: Query) => courseAdminApi.search(query);

export function CourseAdminPage() {
    const fields = useWithGradeFields(FIELDS, courseGradeFields);
    const [openId, setOpenId] = useState<number | null>(null);
    return (
        <AdminCrudPage<Course>
            title="교육 과정"
            description={<p>스트리밍 강의를 차시로 묶어 과정을 만들고, 차시별 퀴즈로 이해도를 확인합니다.</p>}
            load={loadCourses}
            fields={fields}
            emptyForm={{isUsing: true, sequential: true, lessons: [{id: null, title: '', streamingId: null, passScore: 60, quiz: []}]}}
            toForm={(course) => courseAdminApi.get(course.id).then((full) => ({...full, lessons: toLessonDrafts(full.lessons ?? [])}))}
            create={(values) => courseAdminApi.create(toBody(values))}
            update={(course, values) => courseAdminApi.update(course.id, toBody(values))}
            remove={(course) => courseAdminApi.remove(course.id)}
            columns={[
                {label: 'ID', render: (c) => c.id},
                {label: '과정', render: (c) => <Link to={`/courses/${c.id}`}>{c.title}</Link>},
                {label: '차시', render: (c) => `${c.lessonCount}개`},
                {label: '순서대로', render: (c) => <YesNo value={c.sequential}/>},
                {label: '공개', render: (c) => <YesNo value={c.isUsing}/>},
            ]}
            actions={(c) => (
                <button type="button" onClick={() => setOpenId(openId === c.id ? null : c.id)}>{openId === c.id ? '수강생 닫기' : '수강생'}</button>
            )}
            expandedRow={(c) => openId === c.id && <Learners courseId={c.id}/>}
        />
    );
}

function Learners({courseId}: { courseId: number }) {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<CourseLearner> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        courseAdminApi.learners(courseId, page).then(setData).catch(setError);
    }, [courseId, page]);
    return (
        <div className="submissions">
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>회원</th><th>상태</th><th>진도</th><th>수강 신청</th><th>완료</th></tr></thead>
                    <tbody>
                    {data?.content.map((learner) => (
                        <tr key={learner.memberId}>
                            <td>{learner.username}</td>
                            <td>{ENROLLMENT_STATUS[learner.status]}</td>
                            <td>{learner.completedLessons} / {learner.totalLessons}차시</td>
                            <td className="small">{formatDateTime(learner.enrolledAt)}</td>
                            <td className="small">{formatDateTime(learner.completedAt)}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">수강생이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </div>
    );
}
