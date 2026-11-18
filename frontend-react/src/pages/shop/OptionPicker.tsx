import {useState} from 'react';
import type {ProductDetail, ProductOption} from '../../api/domain';
import {won} from '../../components/AdminCrudPage';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const soldOut = (option: ProductOption) => !option.isEnabled || option.stock === 0;

const optionLabel = (option: ProductOption, t: T) => {
    const extra = Number(option.additionalPrice) > 0 ? ` (+${won(option.additionalPrice)})` : '';
    const status = soldOut(option) ? t('shop.optionPicker.soldOut', ' · 품절')
        : option.stock != null && option.stock <= 5 ? t('shop.optionPicker.lowStock', ' · {count}개 남음', {count: String(option.stock)}) : '';
    return `${option.name}${extra}${status}`;
};

export function OptionPicker({product, optionId, onChange}: {
    product: ProductDetail;
    optionId: string;
    onChange: (optionId: string) => void;
}) {
    const {t} = useI18n();
    const groups = product.optionGroups ?? [];
    const [picked, setPicked] = useState<string[]>(() => {
        const current = product.options.find((o) => String(o.id) === optionId);
        return groups.map((_, i) => current?.optionValues[i] ?? '');
    });

    if (groups.length === 0) {
        const options = product.options.filter((o) => o.isEnabled);
        if (options.length === 0) {
            return null;
        }
        return (
            <div className="field">
                <label htmlFor="option">{t('shop.optionPicker.option', '옵션')}</label>
                <select id="option" value={optionId} onChange={(e) => onChange(e.target.value)}>
                    <option value="">{t('shop.optionPicker.default', '기본')}</option>
                    {options.map((o) => <option key={o.id} value={o.id} disabled={soldOut(o)}>{optionLabel(o, t)}</option>)}
                </select>
            </div>
        );
    }

    const pick = (index: number, value: string) => {
        const next = picked.map((v, i) => (i === index ? value : i > index ? '' : v));
        setPicked(next);
        const match = next.every(Boolean)
            ? product.options.find((o) => o.optionValues.every((v, i) => v === next[i]))
            : undefined;
        onChange(match && !soldOut(match) ? String(match.id) : '');
    };
    const available = (index: number, value: string) => product.options.some((o) => !soldOut(o)
        && o.optionValues[index] === value && picked.slice(0, index).every((v, i) => o.optionValues[i] === v));
    const chosen = product.options.find((o) => String(o.id) === optionId);

    return (
        <>
            {groups.map((group, index) => (
                <div key={group.name} className="field">
                    <label htmlFor={`option-${index}`}>{group.name}</label>
                    <select id={`option-${index}`} value={picked[index] ?? ''} disabled={index > 0 && !picked[index - 1]}
                            onChange={(e) => pick(index, e.target.value)}>
                        <option value="">{t('shop.optionPicker.selectGroup', '{group} 선택', {group: group.name})}</option>
                        {group.values.map((value) => (
                            <option key={value} value={value} disabled={!available(index, value)}>
                                {value}{available(index, value) ? '' : t('shop.optionPicker.soldOut', ' · 품절')}
                            </option>
                        ))}
                    </select>
                </div>
            ))}
            {chosen && <p className="muted small">{t('shop.optionPicker.chosen', '선택: {label}', {label: optionLabel(chosen, t)})}</p>}
        </>
    );
}
