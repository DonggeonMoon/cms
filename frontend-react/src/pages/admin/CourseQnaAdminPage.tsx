import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import {courseAdminApi} from '../../api/courses';
import type {Page} from '../../api/types';
import type {UnansweredQuestion} from '../../api/courses';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

export function CourseQnaAdminPage() {
    const {t} = useI18n();
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<UnansweredQuestion> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        courseAdminApi.unansweredQuestions(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>{t('admin.courseQna.title', '교육과정 미답변 질문')}</h1>
            <p className="muted">{t('admin.courseQna.description', '시리즈/교육과정 항목에 달린 질문 중 아직 답글이 없는 것만 모아 보여줍니다.')}</p>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>{t('admin.courseQna.course', '과정')}</th>
                        <th>{t('admin.courseQna.item', '항목')}</th>
                        <th>{t('shop.board.author', '작성자')}</th>
                        <th>{t('shop.report.content', '내용')}</th>
                        <th>{t('shop.board.createdAt', '작성일')}</th>
                        <th/>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.content.map((question) => (
                        <tr key={question.commentId}>
                            <td>{question.courseTitle}</td>
                            <td>{question.lessonTitle}</td>
                            <td>{question.authorName ?? ''}</td>
                            <td>{question.content}</td>
                            <td className="small">{formatDateTime(question.createdAt)}</td>
                            <td>
                                {question.courseId != null &&
                                    <Link to={`${question.isCourse ? '/courses' : '/series'}/${question.courseId}`}>{t('admin.application.view', '보기')}</Link>}
                            </td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('admin.courseQna.none', '미답변 질문이 없습니다.')}</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
