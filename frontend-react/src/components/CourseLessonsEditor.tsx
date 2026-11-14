import {useEffect, useState} from 'react';
import type {LessonDraft, Quiz} from '../api/courses';
import {quizAdminApi} from '../api/courses';
import type {StreamingItem} from '../api/domain';
import {adminApi} from '../api/endpoints';
import {FilesField} from './FilesField';

const emptyLesson = (): LessonDraft => ({
    id: null, title: '', unitName: '', streamingId: null, quizId: null, materials: [], mandatory: false, preventSkippingOverride: null,
});

export function CourseLessonsEditor({value, onChange, isCourse}: {
    value: LessonDraft[]; onChange: (lessons: LessonDraft[]) => void; isCourse: boolean;
}) {
    const [streamingItems, setStreamingItems] = useState<StreamingItem[]>([]);
    const [quizzes, setQuizzes] = useState<Quiz[]>([]);
    useEffect(() => {
        adminApi.streaming.search({size: 200}).then((page) => setStreamingItems(page.content)).catch(() => setStreamingItems([]));
        quizAdminApi.search({size: 200}).then((page) => setQuizzes(page.content)).catch(() => setQuizzes([]));
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
                const label = `${index + 1}번`;
                return (
                    <fieldset key={index} className="lesson-row">
                        <legend>{label}</legend>
                        <div className="form-field-row">
                            <input aria-label={`${label} 제목`} placeholder="항목 제목" required value={lesson.title}
                                   onChange={(e) => update(index, {title: e.target.value})}/>
                            <input aria-label={`${label} 단원명`} placeholder="단원명 (선택)" value={lesson.unitName}
                                   onChange={(e) => update(index, {unitName: e.target.value})}/>
                            <select aria-label={`${label} 강의`} value={lesson.streamingId ?? ''}
                                    onChange={(e) => update(index, {streamingId: e.target.value ? Number(e.target.value) : null})}>
                                <option value="">강의 선택{isCourse ? ' (비우면 퀴즈만)' : ''}</option>
                                {streamingItems.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
                            </select>
                            {isCourse && (
                                <select aria-label={`${label} 퀴즈`} value={lesson.quizId ?? ''}
                                        onChange={(e) => update(index, {quizId: e.target.value ? Number(e.target.value) : null})}>
                                    <option value="">퀴즈 선택</option>
                                    {quizzes.map((quiz) => <option key={quiz.id} value={quiz.id}>{quiz.title}</option>)}
                                </select>
                            )}
                            {isCourse && (
                                <label className="checkbox">
                                    <input type="checkbox" checked={lesson.mandatory}
                                           onChange={(e) => update(index, {mandatory: e.target.checked})}/> 필수 항목
                                </label>
                            )}
                            {isCourse && (
                                <select aria-label={`${label} 건너뛰기 방지`}
                                        value={lesson.preventSkippingOverride == null ? '' : String(lesson.preventSkippingOverride)}
                                        onChange={(e) => update(index, {
                                            preventSkippingOverride: e.target.value === '' ? null : e.target.value === 'true',
                                        })}>
                                    <option value="">건너뛰기 방지: 시리즈 기본값 따름</option>
                                    <option value="true">건너뛰기 방지: 사용</option>
                                    <option value="false">건너뛰기 방지: 사용 안 함</option>
                                </select>
                            )}
                            <span className="actions">
                                <button type="button" aria-label={`${label} 위로`} disabled={index === 0} onClick={() => move(index, -1)}>↑</button>
                                <button type="button" aria-label={`${label} 아래로`} disabled={index === value.length - 1} onClick={() => move(index, 1)}>↓</button>
                                <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== index))}>항목 삭제</button>
                            </span>
                        </div>
                        <FilesField label={`${label} 학습자료`} value={lesson.materials} onChange={(materials) => update(index, {materials})}/>
                    </fieldset>
                );
            })}
            <button type="button" onClick={() => onChange([...value, emptyLesson()])}>항목 추가</button>
            {!isCourse && <p className="muted small">일반 시리즈는 강의(비디오·오디오)만 추가할 수 있습니다. 퀴즈를 쓰려면 "교육과정으로 사용"을 켜세요.</p>}
            {isCourse && <p className="muted small">퀴즈는 "퀴즈" 관리 화면에서 먼저 만들어야 목록에 나타납니다.</p>}
        </div>
    );
}
