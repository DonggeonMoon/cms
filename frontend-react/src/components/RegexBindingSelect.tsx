import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import {type NumberRegexTarget, regexAdminApi, type RegexOption} from '../api/regex';
import {ErrorBanner} from './ErrorBanner';

export function RegexBindingSelect({target, label}: { target: NumberRegexTarget; label: string }) {
    const [options, setOptions] = useState<RegexOption[]>([]);
    const [selected, setSelected] = useState<string>('');
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        Promise.all([regexAdminApi.options(), regexAdminApi.binding(target)])
            .then(([loaded, binding]) => {
                setOptions(loaded);
                setSelected(binding.regexRuleId?.toString() ?? '');
            })
            .catch(setError);
    }, [target]);

    const change = async (value: string) => {
        const previous = selected;
        setSelected(value);
        setError(null);
        setSaved(false);
        try {
            await regexAdminApi.bind(target, value ? Number(value) : null);
            setSaved(true);
        } catch (e) {
            setSelected(previous);
            setError(e);
        }
    };

    const id = `regex-binding-${target}`;
    return (
        <div className="regex-binding">
            <div className="row align-center">
                <label htmlFor={id}>{label}</label>
                <select id={id} value={selected} onChange={(e) => void change(e.target.value)}>
                    <option value="">검사 안 함</option>
                    {options.map((option) => <option key={option.id} value={option.id}>{option.name} ({option.pattern})</option>)}
                </select>
                {saved && <span className="ok small">저장했습니다.</span>}
                <Link to="/admin/regexes" className="small">정규식 관리</Link>
            </div>
            <ErrorBanner error={error}/>
        </div>
    );
}
