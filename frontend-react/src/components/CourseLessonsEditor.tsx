import {useEffect, useState} from 'react';
import type {LessonDraft} from '../api/courses';
import type {MediaItem} from '../api/domain';
import {adminApi} from '../api/endpoints';

const emptyLesson = (): LessonDraft => ({id: null, title: '', mediaId: null, passScore: 60, quiz: []});

export function CourseLessonsEditor({value, onChange}: { value: LessonDraft[]; onChange: (lessons: LessonDraft[]) => void }) {
    const [media, setMedia] = useState<MediaItem[]>([]);
    useEffect(() => {
        adminApi.media.search({size: 200}).then((page) => setMedia(page.content)).catch(() => setMedia([]));
    }, []);
    const update = (index: number, patch: Partial<LessonDraft>) =>
        onChange(value.map((lesson, i) => (i === index ? {...lesson, ...patch} : lesson)));
    const move = (index: number, offset: -1 | 1) => {
        const next = [...value];
        [next[index], next[index + offset]] = [next[index + offset], next[index]];
        onChange(next);
    };

    return (
        <div className="lessons-editor">
            {value.map((lesson, index) => {
                const label = `${index + 1}차시`;
                const setQuestion = (q: number, patch: Partial<LessonDraft['quiz'][number]>) =>
                    update(index, {quiz: lesson.quiz.map((item, i) => (i === q ? {...item, ...patch} : item))});
                return (
                    <fieldset key={index} className="lesson-row">
                        <legend>{label}</legend>
                        <div className="form-field-row">
                            <input aria-label={`${label} 제목`} placeholder="차시 제목" required value={lesson.title}
                                   onChange={(e) => update(index, {title: e.target.value})}/>
                            <select aria-label={`${label} 강의`} value={lesson.mediaId ?? ''}
                                    onChange={(e) => update(index, {mediaId: e.target.value ? Number(e.target.value) : null})}>
                                <option value="">강의 없음 (퀴즈만)</option>
                                {media.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
                            </select>
                            <span className="actions">
                                <button type="button" aria-label={`${label} 위로`} disabled={index === 0} onClick={() => move(index, -1)}>↑</button>
                                <button type="button" aria-label={`${label} 아래로`} disabled={index === value.length - 1} onClick={() => move(index, 1)}>↓</button>
                                <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== index))}>차시 삭제</button>
                            </span>
                        </div>
                        {lesson.quiz.map((question, q) => {
                            const options = question.optionsText.split(',').map((o) => o.trim()).filter(Boolean);
                            return (
                                <div key={q} className="form-field-row quiz-row">
                                    <input aria-label={`${label} ${q + 1}번 문제`} placeholder="문제" required value={question.question}
                                           onChange={(e) => setQuestion(q, {question: e.target.value})}/>
                                    <input aria-label={`${label} ${q + 1}번 선택지`} className="wide-input" placeholder="선택지를 쉼표로 구분" required
                                           value={question.optionsText} onChange={(e) => setQuestion(q, {optionsText: e.target.value})}/>
                                    <select aria-label={`${label} ${q + 1}번 정답`} value={question.answerIndex}
                                            onChange={(e) => setQuestion(q, {answerIndex: Number(e.target.value)})}>
                                        {options.map((option, i) => <option key={i} value={i}>정답: {option}</option>)}
                                    </select>
                                    <button type="button" className="danger"
                                            onClick={() => update(index, {quiz: lesson.quiz.filter((_, i) => i !== q)})}>문제 삭제</button>
                                </div>
                            );
                        })}
                        <div className="toolbar">
                            <button type="button" onClick={() => update(index, {quiz: [...lesson.quiz, {question: '', optionsText: '', answerIndex: 0}]})}>
                                퀴즈 문제 추가
                            </button>
                            {lesson.quiz.length > 0 && (
                                <label>통과 점수(%){' '}
                                    <input type="number" min={1} max={100} aria-label={`${label} 통과 점수`} value={lesson.passScore ?? 60}
                                           onChange={(e) => update(index, {passScore: Number(e.target.value)})}/>
                                </label>
                            )}
                        </div>
                    </fieldset>
                );
            })}
            <button type="button" onClick={() => onChange([...value, emptyLesson()])}>차시 추가</button>
        </div>
    );
}
