import {useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import {type Quiz, type QuizStatistics, quizAdminApi} from '../../api/courses';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {revealPolicy, scorePolicy, toOptions} from '../../components/labels';
import {draftsToQuestions, type QuestionDraft, questionsToDrafts} from '../../components/QuizQuestionsEditor';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const quizFields = (t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.board.field.isUsing', '사용'), type: 'checkbox'},
    {name: 'title', label: t('admin.quiz.field.title', '퀴즈 이름'), required: true, wide: true},
    {name: 'passScore', label: t('admin.quiz.field.passScore', '통과 점수(%)'), type: 'number', min: 1, required: true},
    {name: 'retakeLimit', label: t('admin.quiz.field.retakeLimit', '재응시 허용 횟수'), type: 'number', min: 1, help: t('admin.quiz.field.retakeLimit.help', '비워두면 무제한입니다.')},
    {name: 'timeLimitMinutes', label: t('admin.quiz.field.timeLimitMinutes', '제한 시간(분)'), type: 'number', min: 1, help: t('admin.quiz.field.timeLimitMinutes.help', '비워두면 제한이 없습니다. 시간이 지나면 자동 제출됩니다.')},
    {name: 'shuffleQuestions', label: t('admin.quiz.field.shuffleQuestions', '문제 순서 섞기'), type: 'checkbox'},
    {name: 'shuffleOptions', label: t('admin.quiz.field.shuffleOptions', '보기 순서 섞기'), type: 'checkbox'},
    {name: 'revealPolicy', label: t('admin.quiz.field.revealPolicy', '정답·해설 공개 시점'), type: 'select', options: toOptions(revealPolicy(t)), required: true},
    {name: 'scorePolicy', label: t('admin.quiz.field.scorePolicy', '인정할 점수'), type: 'select', options: toOptions(scorePolicy(t)), required: true,
        help: t('admin.quiz.field.scorePolicy.help', '여러 번 응시했을 때 최고 점수와 최종(마지막) 점수 중 어느 것을 보여줄지 정합니다.')},
    {name: 'questions', label: t('admin.quiz.field.questions', '문항'), type: 'quizQuestions', wide: true,
        help: t('admin.quiz.field.questions.help', '시리즈/교육과정의 항목에서 이 퀴즈를 골라 재사용할 수 있습니다.')},
];

const toBody = ({questions, ...values}: FormValues) => ({...values, questions: draftsToQuestions((questions as QuestionDraft[]) ?? [])});

const loadQuizzes = (query: Query) => quizAdminApi.search(query);

export function QuizAdminPage() {
    const {t} = useI18n();
    const [openId, setOpenId] = useState<number | null>(null);
    return (
        <AdminCrudPage<Quiz>
            title={t('admin.course.tab.quizzes', '퀴즈')}
            description={<p>{t('admin.quiz.description', '시리즈/교육과정의 항목으로 재사용하는 독립 퀴즈입니다. 다른 항목에서 쓰고 있으면 삭제할 수 없습니다.')}</p>}
            load={loadQuizzes}
            fields={quizFields(t)}
            emptyForm={{isUsing: true, passScore: 60, shuffleQuestions: false, shuffleOptions: false,
                revealPolicy: 'IMMEDIATELY', scorePolicy: 'HIGHEST', questions: []}}
            toForm={(quiz) => ({...quiz, questions: questionsToDrafts(quiz.questions)})}
            create={(values) => quizAdminApi.create(toBody(values))}
            update={(quiz, values) => quizAdminApi.update(quiz.id, toBody(values))}
            remove={(quiz) => quizAdminApi.remove(quiz.id)}
            columns={[
                {label: 'ID', render: (q) => q.id},
                {label: t('admin.board.column.name', '이름'), render: (q) => q.title},
                {label: t('admin.quiz.field.questions', '문항'), render: (q) => t('admin.course.lessonCount', '{count}개', {count: String(q.questions.length)})},
                {label: t('admin.quiz.column.passScore', '통과 점수'), render: (q) => `${q.passScore}%`},
                {label: t('admin.quiz.column.retake', '재응시'), render: (q) => (q.retakeLimit == null ? t('admin.course.unlimited', '무제한') : t('admin.quiz.timesOf', '{count}회', {count: String(q.retakeLimit)}))},
                {label: t('admin.quiz.column.timeLimit', '제한 시간'), render: (q) => (q.timeLimitMinutes == null ? t('admin.quiz.none', '없음') : t('admin.quiz.minutesOf', '{count}분', {count: String(q.timeLimitMinutes)}))},
                {label: t('admin.quiz.column.usedByItems', '쓰는 항목'), render: (q) => t('admin.course.lessonCount', '{count}개', {count: String(q.itemCount)})},
                {label: t('admin.board.field.isUsing', '사용'), render: (q) => <YesNo value={q.isUsing}/>},
            ]}
            actions={(q) => (
                <button type="button" onClick={() => setOpenId(openId === q.id ? null : q.id)}>
                    {openId === q.id ? t('admin.quiz.closeAnalysis', '분석 닫기') : t('admin.quiz.analysis', '분석')}
                </button>
            )}
            expandedRow={(q) => (openId === q.id ? <QuizStatisticsPanel quizId={q.id}/> : null)}
        />
    );
}

function QuizStatisticsPanel({quizId}: { quizId: number }) {
    const {t} = useI18n();
    const [stats, setStats] = useState<QuizStatistics | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        quizAdminApi.statistics(quizId).then(setStats).catch(setError);
    }, [quizId]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!stats) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    if (stats.totalAttempts === 0) {
        return <p className="muted">{t('admin.quiz.noAttempts', '아직 응시 기록이 없습니다.')}</p>;
    }
    return (
        <div className="submissions">
            <p>
                {t('admin.quiz.statsSummary', '총 응시 {attempts}회 (응시자 {takers}명) · 평균 점수 {avg}점 · 통과율 {rate}%', {
                    attempts: String(stats.totalAttempts), takers: String(stats.uniqueTestTakers),
                    avg: stats.averageScore.toFixed(1), rate: stats.passRate.toFixed(1),
                })}
            </p>
            <div className="table-wrap">
                <table>
                    <thead><tr>
                        <th>{t('admin.quiz.field.questions', '문항')}</th><th>{t('admin.quiz.attemptCount', '응시 수')}</th>
                        <th>{t('admin.quiz.wrongCount', '오답 수')}</th><th>{t('admin.quiz.wrongRate', '오답률')}</th>
                    </tr></thead>
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
