import type {FormField, FormFieldType} from '../api/domain';
import {formFieldType, toOptions} from './labels';
import {useI18n} from '../api/i18n';

const CHOICE_TYPES: FormFieldType[] = ['SELECT', 'RADIO', 'CHECKBOX'];

const nextKey = (fields: FormField[]) => {
    let n = fields.length + 1;
    while (fields.some((field) => field.key === `field${n}`)) {
        n++;
    }
    return `field${n}`;
};

export function FormFieldsEditor({value, onChange}: { value: FormField[]; onChange: (fields: FormField[]) => void }) {
    const {t} = useI18n();
    const update = (index: number, patch: Partial<FormField>) => onChange(value.map((field, i) => (i === index ? {...field, ...patch} : field)));
    const move = (index: number, offset: -1 | 1) => {
        const next = [...value];
        [next[index], next[index + offset]] = [next[index + offset], next[index]];
        onChange(next);
    };

    return (
        <div className="form-fields-editor">
            {value.map((field, index) => (
                <div key={index} className="form-field-row">
                    <input aria-label={t('admin.formFields.nameOf', '{index}번 항목 이름', {index: String(index + 1)})} placeholder={t('admin.formFields.namePlaceholder', '항목 이름')} value={field.label} required
                           onChange={(e) => update(index, {label: e.target.value})}/>
                    <select aria-label={t('admin.formFields.typeOf', '{index}번 항목 형식', {index: String(index + 1)})} value={field.type}
                            onChange={(e) => update(index, {type: e.target.value as FormFieldType})}>
                        {toOptions(formFieldType(t)).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </select>
                    <label className="checkbox">
                        <input type="checkbox" checked={field.required} onChange={(e) => update(index, {required: e.target.checked})}/> {t('admin.formFields.required', '필수')}
                    </label>
                    {CHOICE_TYPES.includes(field.type) && (
                        <input aria-label={t('admin.formFields.optionsOf', '{index}번 항목 선택지', {index: String(index + 1)})} className="wide-input" value={(field.options ?? []).join(', ')}
                               placeholder={field.type === 'CHECKBOX' ? t('admin.formFields.checkboxPlaceholder', '비우면 동의 체크 하나') : t('admin.formFields.optionsPlaceholder', '선택지를 쉼표로 구분')}
                               onChange={(e) => update(index, {options: e.target.value.split(',').map((o) => o.trim()).filter(Boolean)})}/>
                    )}
                    <input aria-label={t('admin.formFields.helpOf', '{index}번 항목 안내', {index: String(index + 1)})} className="wide-input" placeholder={t('admin.formFields.helpPlaceholder', '안내 문구(선택)')} value={field.help ?? ''}
                           onChange={(e) => update(index, {help: e.target.value || null})}/>
                    <span className="actions">
                        <button type="button" aria-label={t('admin.formFields.moveUp', '위로')} disabled={index === 0} onClick={() => move(index, -1)}>↑</button>
                        <button type="button" aria-label={t('admin.formFields.moveDown', '아래로')} disabled={index === value.length - 1} onClick={() => move(index, 1)}>↓</button>
                        <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== index))}>{t('common.delete', '삭제')}</button>
                    </span>
                </div>
            ))}
            <button type="button" onClick={() => onChange([...value, {key: nextKey(value), label: '', type: 'TEXT', required: false, options: null, help: null}])}>
                {t('admin.formFields.addItem', '항목 추가')}
            </button>
        </div>
    );
}
