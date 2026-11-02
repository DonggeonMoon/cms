import {type FormEvent, useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router';
import {ApiError} from '../api/client';
import type {ReportDetail, ReportSummary} from '../api/domain';
import {govApi} from '../api/endpoints';
import type {Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime} from '../components/format';
import {HtmlView} from '../components/HtmlView';
import {REPORT_STATUS} from '../components/labels';
import {type FieldSpec, SchemaForm} from '../components/SchemaForm';

export function ReportWritePage() {
    const {user} = useAuth();
    const navigate = useNavigate();
    const [lookup, setLookup] = useState('');
    const fields: FieldSpec[] = [
        {name: 'title', label: '제목', required: true, wide: true},
        ...(!user ? [
            {name: 'reporterName', label: '이름', required: true},
            {name: 'password', label: '조회 비밀번호', required: true, help: '처리 결과를 확인할 때 필요합니다.'},
        ] as FieldSpec[] : []),
        {name: 'phoneNumber', label: '연락처'},
        {name: 'email', label: '이메일'},
        {name: 'contentHtml', label: '내용', type: 'html'},
    ];

    return (
        <>
            <section className="card">
                <h1>신고 접수</h1>
                <p className="muted">접수된 내용은 담당자 확인 후 답변드립니다. 개인정보는 처리 목적 외에 사용하지 않습니다.</p>
                <SchemaForm fields={fields} initial={{contentHtml: ''}} submitLabel="접수하기"
                            onSubmit={async (values) => {
                                const {id} = await govApi.createReport(values);
                                if (user) {
                                    navigate('/mypage/reports');
                                } else {
                                    window.alert(`접수되었습니다. 접수 번호는 ${id}번입니다.\n접수 번호와 비밀번호로 처리 결과를 확인할 수 있습니다.`);
                                    navigate(`/reports/${id}`);
                                }
                            }}/>
            </section>
            <section className="card">
                <h2>접수 결과 확인</h2>
                <form className="toolbar" onSubmit={(e) => {
                    e.preventDefault();
                    if (lookup) {
                        navigate(`/reports/${lookup}`);
                    }
                }}>
                    <input aria-label="접수 번호" placeholder="접수 번호" value={lookup} onChange={(e) => setLookup(e.target.value)}/>
                    <button type="submit">확인</button>
                </form>
            </section>
        </>
    );
}

export function ReportDetailPage() {
    const id = Number(useParams().id);
    const [report, setReport] = useState<ReportDetail | null>(null);
    const [needsPassword, setNeedsPassword] = useState(false);
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        govApi.report(id).then(setReport).catch((e: unknown) => {
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
            setReport(await govApi.accessReport(id, password));
            setNeedsPassword(false);
            setError(null);
        } catch (e) {
            setError(e);
        }
    };

    if (needsPassword) {
        return (
            <section className="card narrow">
                <h1>접수 #{id} 확인</h1>
                <form onSubmit={unlock}>
                    <input type="password" aria-label="조회 비밀번호" value={password} onChange={(e) => setPassword(e.target.value)}/>
                    <ErrorBanner error={error}/>
                    <button type="submit" className="primary">확인</button>
                </form>
            </section>
        );
    }
    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!report) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <article className="card">
            <h1>#{report.id} {report.title}</h1>
            <p><span className="badge">{REPORT_STATUS[report.status]}</span> <span className="muted small">{formatDateTime(report.createdAt)}</span></p>
            <HtmlView html={report.contentHtml}/>
            <h2>답변</h2>
            {report.answers.length === 0 && <p className="muted">아직 답변이 없습니다.</p>}
            {report.answers.map((answer) => (
                <div key={answer.id} className="answer">
                    <strong>{answer.title}</strong>
                    <HtmlView html={answer.contentHtml}/>
                    <p className="muted small">{answer.authorName} · {formatDateTime(answer.createdAt)}</p>
                </div>
            ))}
        </article>
    );
}

export function MyReportsPage() {
    const [data, setData] = useState<Page<ReportSummary> | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        govApi.myReports(0).then(setData).catch(setError);
    }, []);
    return (
        <section className="card">
            <h1>나의 신고</h1>
            <ErrorBanner error={error}/>
            <ul className="list">
                {data?.content.map((report) => (
                    <li key={report.id}>
                        <Link to={`/reports/${report.id}`}>#{report.id} {report.title}</Link>
                        <span className="badge">{REPORT_STATUS[report.status]}</span>
                        <span className="muted small"> {formatDateTime(report.createdAt)}</span>
                    </li>
                ))}
                {data?.content.length === 0 && <li className="muted">접수 내역이 없습니다.</li>}
            </ul>
            <Link className="button primary" to="/reports/new">새로 접수</Link>
        </section>
    );
}
