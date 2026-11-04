import {ATTRIBUTE_TYPE, type AttributeType, type CategoryAttribute} from '../api/categories';
import {toOptions} from './labels';

const nextKey = (attributes: CategoryAttribute[]) => {
    let n = attributes.length + 1;
    while (attributes.some((attribute) => attribute.key === `attr${n}`)) {
        n++;
    }
    return `attr${n}`;
};

export function CategoryAttributesEditor({value, onChange}: { value: CategoryAttribute[]; onChange: (attributes: CategoryAttribute[]) => void }) {
    const update = (index: number, patch: Partial<CategoryAttribute>) =>
        onChange(value.map((attribute, i) => (i === index ? {...attribute, ...patch} : attribute)));
    return (
        <div className="form-fields-editor">
            {value.map((attribute, index) => (
                <div key={index} className="form-field-row">
                    <input aria-label={`${index + 1}번 속성 이름`} placeholder="속성 이름" value={attribute.label} required
                           onChange={(e) => update(index, {label: e.target.value})}/>
                    <input aria-label={`${index + 1}번 속성 키`} placeholder="키(영문)" value={attribute.key} required pattern="[A-Za-z0-9_-]{1,40}"
                           onChange={(e) => update(index, {key: e.target.value})}/>
                    <select aria-label={`${index + 1}번 속성 형식`} value={attribute.type}
                            onChange={(e) => update(index, {type: e.target.value as AttributeType})}>
                        {toOptions(ATTRIBUTE_TYPE).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                    </select>
                    {attribute.type === 'NUMBER' && (
                        <input aria-label={`${index + 1}번 속성 단위`} placeholder="단위(kg 등)" value={attribute.unit ?? ''}
                               onChange={(e) => update(index, {unit: e.target.value || null})}/>
                    )}
                    {attribute.type !== 'TEXT' && (
                        <label className="checkbox">
                            <input type="checkbox" checked={attribute.filterable} onChange={(e) => update(index, {filterable: e.target.checked})}/> 검색 조건
                        </label>
                    )}
                    {attribute.type === 'SELECT' && (
                        <input aria-label={`${index + 1}번 속성 선택지`} className="wide-input" placeholder="선택지를 쉼표로 구분"
                               value={(attribute.options ?? []).join(', ')}
                               onChange={(e) => update(index, {options: e.target.value.split(',').map((o) => o.trim()).filter(Boolean)})}/>
                    )}
                    <span className="actions">
                        <button type="button" className="danger" onClick={() => onChange(value.filter((_, i) => i !== index))}>삭제</button>
                    </span>
                </div>
            ))}
            <button type="button" onClick={() => onChange([...value, {key: nextKey(value), label: '', type: 'SELECT', options: [], unit: null, filterable: true}])}>
                속성 추가
            </button>
        </div>
    );
}
