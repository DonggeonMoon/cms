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
    const setQuestion = (q: number, patch: Partial<QuestionDraft>) =>
        onChange(value.map((item, i) => (i === q ? {...item, ...patch} : item)));
    return (
        <div className="quiz-questions-editor">
            {value.map((question, q) => {
                const options = question.optionsText.split(',').map((o) => o.trim()).filter(Boolean);
                return (
                    <div key={q} className="form-field-row quiz-row">
                        <input aria-label={`${q + 1}번 문제`} placeholder="문제" required value={question.question}
                               onChange={(e) => setQuestion(q, {question: e.target.value})}/>
                        <input aria-label={`${q + 1}번 선택지`} className="wide-input" placeholder="선택지를 쉼표로 구분" required
                               value={question.optionsText} onChange={(e) => setQuestion(q, {optionsText: e.target.value})}/>
                        <select aria-label={`${q + 1}번 정답`} value={question.answerIndex}
                                onChange={(e) => setQuestion(q, {answerIndex: Number(e.target.value)})}>
                            {options.map((option, i) => <option key={i} value={i}>정답: {option}</option>)}
                        </select>
                        <input aria-label={`${q + 1}번 해설`} className="wide-input" placeholder="해설 (선택)" value={question.explanation}
                               onChange={(e) => setQuestion(q, {explanation: e.target.value})}/>
                        <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== q))}>문제 삭제</button>
                    </div>
                );
            })}
            <button type="button" onClick={() => onChange([...value, emptyQuestion()])}>문제 추가</button>
        </div>
    );
}
