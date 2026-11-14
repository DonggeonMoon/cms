import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import {type CourseStatisticsResponse, courseAdminApi} from '../../api/courses';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';

export function CourseStatisticsPage() {
    const [data, setData] = useState<CourseStatisticsResponse | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        courseAdminApi.statistics().then(setData).catch(setError);
    }, []);

    return (
        <section className="card">
            <h1>시리즈/교육과정 통계</h1>
            <p className="muted">과정별 수강 현황·완료율과, 결제로 수강한 경우의 매출·환불 현황입니다.</p>
            <ErrorBanner error={error}/>
            {data && (
                <p className="summary-row">
                    전체 수강 <strong>{data.totalEnrolled}</strong>건 · 완료 <strong>{data.totalCompleted}</strong>건 ·
                    매출 합계 <strong>{won(data.totalRevenue)}</strong>
                </p>
            )}
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>이름</th>
                        <th>수강</th>
                        <th>완료율</th>
                        <th>신청(대기/승인/반려)</th>
                        <th>매출</th>
                        <th>결제/환불/대기</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.rows.map((row) => (
                        <tr key={row.courseId}>
                            <td>
                                <Link to={`/courses/${row.courseId}`}>{row.title}</Link>
                                {row.paid && <span className="badge"> 유료</span>}
                            </td>
                            <td>{row.totalEnrolled}건 (진행 {row.inProgress} · 완료 {row.completed} · 환불회수 {row.revoked})</td>
                            <td>{row.completionRate.toFixed(1)}%</td>
                            <td>
                                {row.isCourse
                                    ? `${row.pendingApplications} / ${row.approvedApplications} / ${row.rejectedApplications}`
                                    : '-'}
                            </td>
                            <td>{row.paid ? won(row.revenue) : '-'}</td>
                            <td>{row.paid ? `${row.paidCount} / ${row.refundedCount} / ${row.pendingPaymentCount}` : '-'}</td>
                        </tr>
                    ))}
                    {data?.rows.length === 0 && <tr><td colSpan={6} className="muted">등록된 시리즈/교육과정이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}
