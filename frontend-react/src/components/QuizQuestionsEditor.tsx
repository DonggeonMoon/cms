import {useI18n} from '../api/i18n';

export interface QuestionDraft {
    question: string;
    optionsText: string;
    answerIndex: number;
    explanation: string;
}

export const emptyQuestion = (): QuestionDraft => ({question: '', optionsText: '', answerIndex: 0, explanation: ''});

export const questionsToDrafts = (
    questions: { question: string; options: string[]; answerIndex?: number; explanation?: string | null }[],
): QuestionDraft[] =>
    questions.map((q) => ({
        question: q.question, optionsText: q.options.join(', '), answerIndex: q.answerIndex ?? 0, explanation: q.explanation ?? '',
    }));

export function draftsToQuestions(drafts: QuestionDraft[]) {
    return drafts.map((q) => ({
        question: q.question,
        options: q.optionsText.split(',').map((o) => o.trim()).filter(Boolean),
        answerIndex: q.answerIndex,
        explanation: q.explanation || null,
    }));
}

export function QuizQuestionsEditor({value, onChange}: { value: QuestionDraft[]; onChange: (questions: QuestionDraft[]) => void }) {
    const {t} = useI18n();
    const setQuestion = (q: number, patch: Partial<QuestionDraft>) =>
        onChange(value.map((item, i) => (i === q ? {...item, ...patch} : item)));
    return (
        <div className="quiz-questions-editor">
            {value.map((question, q) => {
                const options = question.optionsText.split(',').map((o) => o.trim()).filter(Boolean);
                return (
                    <div key={q} className="form-field-row quiz-row">
                        <input aria-label={t('admin.quizQuestions.questionOf', '{index}번 문제', {index: String(q + 1)})} placeholder={t('admin.quizQuestions.questionPlaceholder', '문제')} required value={question.question}
                               onChange={(e) => setQuestion(q, {question: e.target.value})}/>
                        <input aria-label={t('admin.quizQuestions.optionsOf', '{index}번 선택지', {index: String(q + 1)})} className="wide-input" placeholder={t('admin.formFields.optionsPlaceholder', '선택지를 쉼표로 구분')} required
                               value={question.optionsText} onChange={(e) => setQuestion(q, {optionsText: e.target.value})}/>
                        <select aria-label={t('admin.quizQuestions.answerOf', '{index}번 정답', {index: String(q + 1)})} value={question.answerIndex}
                                onChange={(e) => setQuestion(q, {answerIndex: Number(e.target.value)})}>
                            {options.map((option, i) => <option key={i} value={i}>{t('admin.quizQuestions.answerLabel', '정답: {option}', {option})}</option>)}
                        </select>
                        <input aria-label={t('admin.quizQuestions.explanationOf', '{index}번 해설', {index: String(q + 1)})} className="wide-input" placeholder={t('admin.quizQuestions.explanationPlaceholder', '해설 (선택)')} value={question.explanation}
                               onChange={(e) => setQuestion(q, {explanation: e.target.value})}/>
                        <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== q))}>{t('admin.quizQuestions.removeQuestion', '문제 삭제')}</button>
                    </div>
                );
            })}
            <button type="button" onClick={() => onChange([...value, emptyQuestion()])}>{t('admin.quizQuestions.addQuestion', '문제 추가')}</button>
        </div>
    );
}
