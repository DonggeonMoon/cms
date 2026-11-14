import {useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import {type Quiz, type QuizStatistics, quizAdminApi} from '../../api/courses';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {REVEAL_POLICY, SCORE_POLICY, toOptions} from '../../components/labels';
import {draftsToQuestions, type QuestionDraft, questionsToDrafts} from '../../components/QuizQuestionsEditor';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'isUsing', label: '사용', type: 'checkbox'},
    {name: 'title', label: '퀴즈 이름', required: true, wide: true},
    {name: 'passScore', label: '통과 점수(%)', type: 'number', min: 1, required: true},
    {name: 'retakeLimit', label: '재응시 허용 횟수', type: 'number', min: 1, help: '비워두면 무제한입니다.'},
    {name: 'timeLimitMinutes', label: '제한 시간(분)', type: 'number', min: 1, help: '비워두면 제한이 없습니다. 시간이 지나면 자동 제출됩니다.'},
    {name: 'shuffleQuestions', label: '문제 순서 섞기', type: 'checkbox'},
    {name: 'shuffleOptions', label: '보기 순서 섞기', type: 'checkbox'},
    {name: 'revealPolicy', label: '정답·해설 공개 시점', type: 'select', options: toOptions(REVEAL_POLICY), required: true},
    {name: 'scorePolicy', label: '인정할 점수', type: 'select', options: toOptions(SCORE_POLICY), required: true,
        help: '여러 번 응시했을 때 최고 점수와 최종(마지막) 점수 중 어느 것을 보여줄지 정합니다.'},
    {name: 'questions', label: '문항', type: 'quizQuestions', wide: true,
        help: '시리즈/교육과정의 항목에서 이 퀴즈를 골라 재사용할 수 있습니다.'},
];

const toBody = ({questions, ...values}: FormValues) => ({...values, questions: draftsToQuestions((questions as QuestionDraft[]) ?? [])});

const loadQuizzes = (query: Query) => quizAdminApi.search(query);

export function QuizAdminPage() {
    const [openId, setOpenId] = useState<number | null>(null);
    return (
        <AdminCrudPage<Quiz>
            title="퀴즈"
            description={<p>시리즈/교육과정의 항목으로 재사용하는 독립 퀴즈입니다. 다른 항목에서 쓰고 있으면 삭제할 수 없습니다.</p>}
            load={loadQuizzes}
            fields={FIELDS}
            emptyForm={{isUsing: true, passScore: 60, shuffleQuestions: false, shuffleOptions: false,
                revealPolicy: 'IMMEDIATELY', scorePolicy: 'HIGHEST', questions: []}}
            toForm={(quiz) => ({...quiz, questions: questionsToDrafts(quiz.questions)})}
            create={(values) => quizAdminApi.create(toBody(values))}
            update={(quiz, values) => quizAdminApi.update(quiz.id, toBody(values))}
            remove={(quiz) => quizAdminApi.remove(quiz.id)}
            columns={[
                {label: 'ID', render: (q) => q.id},
                {label: '이름', render: (q) => q.title},
                {label: '문항', render: (q) => `${q.questions.length}개`},
                {label: '통과 점수', render: (q) => `${q.passScore}%`},
                {label: '재응시', render: (q) => (q.retakeLimit == null ? '무제한' : `${q.retakeLimit}회`)},
                {label: '제한 시간', render: (q) => (q.timeLimitMinutes == null ? '없음' : `${q.timeLimitMinutes}분`)},
                {label: '쓰는 항목', render: (q) => `${q.itemCount}개`},
                {label: '사용', render: (q) => <YesNo value={q.isUsing}/>},
            ]}
            actions={(q) => (
                <button type="button" onClick={() => setOpenId(openId === q.id ? null : q.id)}>
                    {openId === q.id ? '분석 닫기' : '분석'}
                </button>
            )}
            expandedRow={(q) => (openId === q.id ? <QuizStatisticsPanel quizId={q.id}/> : null)}
        />
    );
}

function QuizStatisticsPanel({quizId}: { quizId: number }) {
    const [stats, setStats] = useState<QuizStatistics | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        quizAdminApi.statistics(quizId).then(setStats).catch(setError);
    }, [quizId]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!stats) {
        return <p className="muted">불러오는 중…</p>;
    }
    if (stats.totalAttempts === 0) {
        return <p className="muted">아직 응시 기록이 없습니다.</p>;
    }
    return (
        <div className="submissions">
            <p>
                총 응시 <strong>{stats.totalAttempts}</strong>회 (응시자 {stats.uniqueTestTakers}명) ·
                평균 점수 <strong>{stats.averageScore.toFixed(1)}점</strong> ·
                통과율 <strong>{stats.passRate.toFixed(1)}%</strong>
            </p>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>문항</th><th>응시 수</th><th>오답 수</th><th>오답률</th></tr></thead>
                    <tbody>
                    {stats.questions.map((question, index) => (
                        <tr key={index}>
                            <td>{index + 1}. {question.question}</td>
                            <td>{question.attemptCount}</td>
                            <td>{question.wrongCount}</td>
                            <td>{question.wrongRate.toFixed(1)}%</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
