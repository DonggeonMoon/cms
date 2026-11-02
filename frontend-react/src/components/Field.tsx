import type {InputHTMLAttributes} from 'react';

interface Props extends InputHTMLAttributes<HTMLInputElement> {
    label: string;
    error?: string;
}

export function Field({label, error, id, ...inputProps}: Props) {
    const inputId = id ?? inputProps.name;
    return (
        <div className="field">
            <label htmlFor={inputId}>{label}{inputProps.required && <span className="required"> *</span>}</label>
            <input id={inputId} aria-invalid={Boolean(error)} {...inputProps}/>
            {error && <p className="field-error">{error}</p>}
        </div>
    );
}

export function Checkbox({label, ...inputProps}: Omit<Props, 'error'>) {
    return (
        <label className="checkbox">
            <input type="checkbox" {...inputProps}/>
            {label}
        </label>
    );
}
