import {useEffect, useState} from 'react';
import type {LessonDraft, Quiz} from '../api/courses';
import {quizAdminApi} from '../api/courses';
import type {StreamingItem} from '../api/domain';
import {adminApi} from '../api/endpoints';
import {FilesField} from './FilesField';
import {useI18n} from '../api/i18n';

const emptyLesson = (): LessonDraft => ({
    id: null, title: '', unitName: '', streamingId: null, quizId: null, materials: [], mandatory: false, preventSkippingOverride: null,
});

export function CourseLessonsEditor({value, onChange, isCourse}: {
    value: LessonDraft[]; onChange: (lessons: LessonDraft[]) => void; isCourse: boolean;
}) {
    const {t} = useI18n();
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
                const label = t('admin.courseLessons.numberOf', '{index}번', {index: String(index + 1)});
                return (
                    <fieldset key={index} className="lesson-row">
                        <legend>{label}</legend>
                        <div className="form-field-row">
                            <input aria-label={t('admin.courseLessons.titleOf', '{label} 제목', {label})} placeholder={t('admin.courseLessons.titlePlaceholder', '항목 제목')} required value={lesson.title}
                                   onChange={(e) => update(index, {title: e.target.value})}/>
                            <input aria-label={t('admin.courseLessons.unitNameOf', '{label} 단원명', {label})} placeholder={t('admin.courseLessons.unitNamePlaceholder', '단원명 (선택)')} value={lesson.unitName}
                                   onChange={(e) => update(index, {unitName: e.target.value})}/>
                            <select aria-label={t('admin.courseLessons.streamingOf', '{label} 강의', {label})} value={lesson.streamingId ?? ''}
                                    onChange={(e) => update(index, {streamingId: e.target.value ? Number(e.target.value) : null})}>
                                <option value="">{t('admin.courseLessons.selectStreaming', '강의 선택')}{isCourse ? t('admin.courseLessons.quizOnlyHint', ' (비우면 퀴즈만)') : ''}</option>
                                {streamingItems.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
                            </select>
                            {isCourse && (
                                <select aria-label={t('admin.courseLessons.quizOf', '{label} 퀴즈', {label})} value={lesson.quizId ?? ''}
                                        onChange={(e) => update(index, {quizId: e.target.value ? Number(e.target.value) : null})}>
                                    <option value="">{t('admin.courseLessons.selectQuiz', '퀴즈 선택')}</option>
                                    {quizzes.map((quiz) => <option key={quiz.id} value={quiz.id}>{quiz.title}</option>)}
                                </select>
                            )}
                            {isCourse && (
                                <label className="checkbox">
                                    <input type="checkbox" checked={lesson.mandatory}
                                           onChange={(e) => update(index, {mandatory: e.target.checked})}/> {t('admin.courseLessons.mandatory', '필수 항목')}
                                </label>
                            )}
                            {isCourse && (
                                <select aria-label={t('admin.courseLessons.preventSkippingOf', '{label} 건너뛰기 방지', {label})}
                                        value={lesson.preventSkippingOverride == null ? '' : String(lesson.preventSkippingOverride)}
                                        onChange={(e) => update(index, {
                                            preventSkippingOverride: e.target.value === '' ? null : e.target.value === 'true',
                                        })}>
                                    <option value="">{t('admin.courseLessons.preventSkippingDefault', '건너뛰기 방지: 시리즈 기본값 따름')}</option>
                                    <option value="true">{t('admin.courseLessons.preventSkippingOn', '건너뛰기 방지: 사용')}</option>
                                    <option value="false">{t('admin.courseLessons.preventSkippingOff', '건너뛰기 방지: 사용 안 함')}</option>
                                </select>
                            )}
                            <span className="actions">
                                <button type="button" aria-label={t('admin.courseLessons.moveUpOf', '{label} 위로', {label})} disabled={index === 0} onClick={() => move(index, -1)}>↑</button>
                                <button type="button" aria-label={t('admin.courseLessons.moveDownOf', '{label} 아래로', {label})} disabled={index === value.length - 1} onClick={() => move(index, 1)}>↓</button>
                                <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== index))}>{t('admin.courseLessons.removeItem', '항목 삭제')}</button>
                            </span>
                        </div>
                        <FilesField label={t('admin.courseLessons.materialsOf', '{label} 학습자료', {label})} value={lesson.materials} onChange={(materials) => update(index, {materials})}/>
                    </fieldset>
                );
            })}
            <button type="button" onClick={() => onChange([...value, emptyLesson()])}>{t('admin.courseLessons.addItem', '항목 추가')}</button>
            {!isCourse && <p className="muted small">{t('admin.courseLessons.seriesHint', '일반 시리즈는 강의(비디오·오디오)만 추가할 수 있습니다. 퀴즈를 쓰려면 "교육과정으로 사용"을 켜세요.')}</p>}
            {isCourse && <p className="muted small">{t('admin.courseLessons.quizHint', '퀴즈는 "퀴즈" 관리 화면에서 먼저 만들어야 목록에 나타납니다.')}</p>}
        </div>
    );
}
