import type {OptionGroup, ProductOption} from '../api/domain';
import {useI18n} from '../api/i18n';

export interface OptionDraft {
    id?: number;
    name: string;
    additionalPrice: number;
    stock: number | null;
    isEnabled: boolean;
    optionValues: string[];
}

export interface GroupDraft {
    name: string;
    valuesText: string;
}

export interface OptionSet {
    groups: GroupDraft[];
    options: OptionDraft[];
}

export const EMPTY_OPTION_SET: OptionSet = {groups: [], options: []};

const splitValues = (text: string) => [...new Set(text.split(',').map((v) => v.trim()).filter(Boolean))];

export function validGroups(groups: GroupDraft[]): OptionGroup[] {
    return groups.map((g) => ({name: g.name.trim(), values: splitValues(g.valuesText)}))
        .filter((g) => g.name && g.values.length > 0);
}

const keyOf = (values: string[]) => values.join('\u0000');

function combinations(groups: OptionGroup[]): string[][] {
    return groups.reduce<string[][]>((acc, group) => acc.flatMap((prefix) => group.values.map((v) => [...prefix, v])), [[]]);
}

/** 옵션 그룹이 바뀌면 조합을 다시 만들되, 이미 있던 조합의 금액·재고·ID는 그대로 둔다. */
export function regenerate(groups: GroupDraft[], current: OptionDraft[]): OptionDraft[] {
    const valid = validGroups(groups);
    if (valid.length === 0) {
        return current.filter((o) => o.optionValues.length === 0);
    }
    const existing = new Map(current.map((o) => [keyOf(o.optionValues), o]));
    return combinations(valid).map((values) => existing.get(keyOf(values))
        ?? {name: values.join(' / '), additionalPrice: 0, stock: null, isEnabled: true, optionValues: values});
}

export function toOptionSet(groups: OptionGroup[], options: ProductOption[]): OptionSet {
    return {
        groups: groups.map((g) => ({name: g.name, valuesText: g.values.join(', ')})),
        options: options.map((o) => ({id: o.id, name: o.name, additionalPrice: Number(o.additionalPrice), stock: o.stock,
            isEnabled: o.isEnabled, optionValues: o.optionValues ?? []})),
    };
}

export function optionSetBody(set: OptionSet | undefined) {
    const value = set ?? EMPTY_OPTION_SET;
    const groups = validGroups(value.groups);
    return {
        optionGroups: groups,
        options: (groups.length > 0 ? value.options.filter((o) => o.optionValues.length > 0) : value.options.filter((o) => o.name.trim()))
            .map((o) => ({...o, name: o.name.trim()})),
    };
}

export function ProductOptionsEditor({value, onChange}: { value: OptionSet | undefined; onChange: (set: OptionSet) => void }) {
    const {t} = useI18n();
    const set = value ?? EMPTY_OPTION_SET;
    const hasGroups = validGroups(set.groups).length > 0;
    const setGroups = (groups: GroupDraft[]) => onChange({groups, options: regenerate(groups, set.options)});
    const updateOption = (index: number, patch: Partial<OptionDraft>) =>
        onChange({...set, options: set.options.map((o, i) => (i === index ? {...o, ...patch} : o))});

    return (
        <div className="product-options-editor">
            <div className="option-groups">
                {set.groups.map((group, index) => (
                    <div key={index} className="form-field-row">
                        <input aria-label={t('admin.productOptions.groupNameOf', '{index}번 옵션 그룹 이름', {index: String(index + 1)})} placeholder={t('admin.productOptions.groupNamePlaceholder', '그룹 이름 (예: 색상)')} value={group.name}
                               onChange={(e) => setGroups(set.groups.map((g, i) => (i === index ? {...g, name: e.target.value} : g)))}/>
                        <input aria-label={t('admin.productOptions.groupValuesOf', '{index}번 옵션 그룹 값', {index: String(index + 1)})} className="wide-input" placeholder={t('admin.productOptions.groupValuesPlaceholder', '값을 쉼표로 구분 (예: 빨강, 파랑)')}
                               value={group.valuesText}
                               onChange={(e) => setGroups(set.groups.map((g, i) => (i === index ? {...g, valuesText: e.target.value} : g)))}/>
                        <button type="button" className="danger" onClick={() => setGroups(set.groups.filter((_, i) => i !== index))}>{t('common.delete', '삭제')}</button>
                    </div>
                ))}
                <div className="toolbar">
                    {set.groups.length < 5 && (
                        <button type="button" onClick={() => setGroups([...set.groups, {name: '', valuesText: ''}])}>{t('admin.productOptions.addGroup', '옵션 그룹 추가')}</button>
                    )}
                    {!hasGroups && (
                        <button type="button" onClick={() => onChange({...set, options: [...set.options,
                                {name: '', additionalPrice: 0, stock: null, isEnabled: true, optionValues: []}]})}>
                            {t('admin.productOptions.addSingle', '단일 옵션 추가')}
                        </button>
                    )}
                </div>
            </div>
            {set.options.length > 0 && (
                <table className="compact">
                    <thead>
                    <tr><th>{hasGroups ? t('admin.productOptions.combination', '조합') : t('admin.productOptions.optionName', '옵션명')}</th><th>{t('admin.productOptions.additionalPrice', '추가 금액')}</th><th>{t('admin.order.column.stock', '재고')}</th><th>{t('admin.productOptions.sale', '판매')}</th>{!hasGroups && <th/>}</tr>
                    </thead>
                    <tbody>
                    {set.options.map((option, index) => (
                        <tr key={option.id ?? `new-${index}-${option.optionValues.join('/')}`}>
                            <td>
                                {hasGroups ? option.optionValues.join(' / ') : (
                                    <input aria-label={t('admin.productOptions.optionNameOf', '{index}번 옵션명', {index: String(index + 1)})} value={option.name}
                                           onChange={(e) => updateOption(index, {name: e.target.value})}/>
                                )}
                            </td>
                            <td>
                                <input aria-label={t('admin.productOptions.additionalPriceOf', '{name} 추가 금액', {name: option.name || String(index + 1)})} type="number" min={0} value={option.additionalPrice}
                                       onChange={(e) => updateOption(index, {additionalPrice: Number(e.target.value) || 0})}/>
                            </td>
                            <td>
                                <input aria-label={t('admin.productOptions.stockOf', '{name} 재고', {name: option.name || String(index + 1)})} type="number" min={0} placeholder={t('admin.layoutEditor.noLimit', '제한 없음')}
                                       value={option.stock ?? ''}
                                       onChange={(e) => updateOption(index, {stock: e.target.value === '' ? null : Number(e.target.value)})}/>
                            </td>
                            <td>
                                <input aria-label={t('admin.productOptions.saleOf', '{name} 판매', {name: option.name || String(index + 1)})} type="checkbox" checked={option.isEnabled}
                                       onChange={(e) => updateOption(index, {isEnabled: e.target.checked})}/>
                            </td>
                            {!hasGroups && (
                                <td>
                                    <button type="button" className="danger"
                                            onClick={() => onChange({...set, options: set.options.filter((_, i) => i !== index)})}>{t('common.delete', '삭제')}</button>
                                </td>
                            )}
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}
