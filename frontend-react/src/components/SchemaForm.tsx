import {type FormEvent, type ReactNode, useState} from 'react';
import {ApiError} from '../api/client';
import {fileApi} from '../api/files';
import type {FileInfo} from '../api/types';
import {ErrorBanner} from './ErrorBanner';
import {HtmlView} from './HtmlView';

export type FieldType = 'text' | 'number' | 'checkbox' | 'select' | 'datetime' | 'textarea' | 'html' | 'image' | 'files' | 'color';

export interface FieldSpec {
    name: string;
    label: string;
    type?: FieldType;
    required?: boolean;
    options?: { value: string; label: string }[];
    placeholder?: string;
    help?: string;
    wide?: boolean;
    spaced?: boolean;
    group?: string;
}

export type FormValues = Record<string, unknown>;

interface Props {
    fields: FieldSpec[];
    initial: FormValues;
    onSubmit: (values: FormValues) => Promise<void>;
    onCancel?: () => void;
    submitLabel?: string;
    title?: string;
    footer?: ReactNode;
}

export function normalizeValues(fields: FieldSpec[], values: FormValues): FormValues {
    const result: FormValues = {...values};
    for (const field of fields) {
        const value = values[field.name];
        if (field.type === 'number') {
            result[field.name] = value === '' || value === null || value === undefined ? null : Number(value);
        } else if (field.type === 'datetime') {
            result[field.name] = value ? String(value).slice(0, 16) + ':00' : null;
        } else if (field.type === 'select' && value === '') {
            result[field.name] = null;
        }
    }
    return result;
}

export function SchemaForm({fields, initial, onSubmit, onCancel, submitLabel = '저장', title, footer}: Props) {
    const [values, setValues] = useState<FormValues>(initial);
    const [error, setError] = useState<unknown>(null);
    const [saving, setSaving] = useState(false);

    const set = (name: string, value: unknown) => setValues((prev) => ({...prev, [name]: value}));
    const errorOf = (name: string) => (error instanceof ApiError ? error.fieldMessage(name) : undefined);
    const groups = [...new Set(fields.flatMap((field) => (field.group ? [field.group] : [])))];
    const grid = (specs: FieldSpec[]) => (
        <div className="schema-grid">
            {specs.map((field) => (
                <div key={field.name} className={[
                    field.wide || ['html', 'textarea', 'files'].includes(field.type ?? '') ? 'wide' : '',
                    field.spaced ? 'spaced' : '',
                ].filter(Boolean).join(' ')}>
                    <FieldInput field={field} value={values[field.name]} onChange={(v) => set(field.name, v)}
                                error={errorOf(field.name)}/>
                </div>
            ))}
        </div>
    );

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSaving(true);
        setError(null);
        try {
            await onSubmit(normalizeValues(fields, values));
        } catch (e) {
            setError(e);
        } finally {
            setSaving(false);
        }
    };

    return (
        <form className="inline-form" onSubmit={handleSubmit}>
            {title && <h2>{title}</h2>}
            {grid(fields.filter((field) => !field.group))}
            {groups.map((group) => (
                <fieldset key={group}>
                    <legend>{group}</legend>
                    {grid(fields.filter((field) => field.group === group))}
                </fieldset>
            ))}
            <ErrorBanner error={error}/>
            {footer}
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={saving}>{saving ? '저장 중…' : submitLabel}</button>
                {onCancel && <button type="button" onClick={onCancel}>취소</button>}
            </div>
        </form>
    );
}

interface FieldInputProps {
    field: FieldSpec;
    value: unknown;
    onChange: (value: unknown) => void;
    error?: string;
}

function FieldInput({field, value, onChange, error}: FieldInputProps) {
    const id = `field-${field.name}`;
    const label = (
        <label htmlFor={id}>{field.label}{field.required && <span className="required"> *</span>}</label>
    );
    const help = (
        <>
            {field.help && <p className="muted small">{field.help}</p>}
            {error && <p className="field-error">{error}</p>}
        </>
    );

    switch (field.type) {
        case 'checkbox':
            return (
                <label className="checkbox">
                    <input id={id} type="checkbox" checked={Boolean(value)} onChange={(e) => onChange(e.target.checked)}/>
                    {field.label}
                </label>
            );
        case 'select':
            return (
                <div className="field">
                    {label}
                    <select id={id} value={value == null ? '' : String(value)} required={field.required}
                            onChange={(e) => onChange(e.target.value)}>
                        {!field.required && <option value="">선택 안 함</option>}
                        {field.options?.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                    {help}
                </div>
            );
        case 'textarea':
            return (
                <div className="field">
                    {label}
                    <textarea id={id} rows={4} value={(value as string) ?? ''} required={field.required}
                              onChange={(e) => onChange(e.target.value)}/>
                    {help}
                </div>
            );
        case 'color':
            return (
                <div className="field">
                    {label}
                    <div className="row align-center">
                        <input id={id} type="color" value={(value as string) || '#000000'} onChange={(e) => onChange(e.target.value)}/>
                        <code>{(value as string) || '-'}</code>
                    </div>
                    {help}
                </div>
            );
        case 'html':
            return <HtmlField id={id} label={label} value={(value as string) ?? ''} onChange={onChange} help={help}/>;
        case 'image':
            return <ImageField id={id} label={label} value={value as number | null} onChange={onChange} help={help}/>;
        case 'files':
            return <FilesField label={label} value={(value as FileInfo[]) ?? []} onChange={onChange} help={help}/>;
        default:
            return (
                <div className="field">
                    {label}
                    <input id={id} type={field.type === 'number' ? 'number' : field.type === 'datetime' ? 'datetime-local' : 'text'}
                           value={value == null ? '' : field.type === 'datetime' ? String(value).slice(0, 16) : String(value)}
                           required={field.required} placeholder={field.placeholder} step={field.type === 'number' ? 'any' : undefined}
                           aria-invalid={Boolean(error)} onChange={(e) => onChange(e.target.value)}/>
                    {help}
                </div>
            );
    }
}

function HtmlField({id, label, value, onChange, help}: {
    id: string; label: ReactNode; value: string; onChange: (v: unknown) => void; help: ReactNode;
}) {
    const [preview, setPreview] = useState(false);
    return (
        <div className="field">
            <div className="field-head">
                {label}
                <button type="button" className="link-button" onClick={() => setPreview(!preview)}>
                    {preview ? 'HTML 편집' : '미리보기'}
                </button>
            </div>
            {preview
                ? <div className="html-preview"><HtmlView html={value}/></div>
                : <textarea id={id} rows={10} className="mono" value={value} placeholder="<p>내용을 입력하세요</p>"
                            onChange={(e) => onChange(e.target.value)}/>}
            <p className="muted small">HTML로 작성합니다. 저장할 때 허용되지 않은 태그와 스크립트는 제거됩니다.</p>
            {help}
        </div>
    );
}

function ImageField({id, label, value, onChange, help}: {
    id: string; label: ReactNode; value: number | null; onChange: (v: unknown) => void; help: ReactNode;
}) {
    const [error, setError] = useState<unknown>(null);
    return (
        <div className="field">
            {label}
            {value && <img className="preview-image" src={`/api/files/${value}/view`} alt=""/>}
            <div className="row align-center">
                <input id={id} type="file" accept="image/*" onChange={async (e) => {
                    const file = e.target.files?.[0];
                    if (!file) {
                        return;
                    }
                    try {
                        setError(null);
                        onChange((await fileApi.upload(file)).id);
                    } catch (err) {
                        setError(err);
                    }
                }}/>
                {value && <button type="button" onClick={() => onChange(null)}>이미지 제거</button>}
            </div>
            <ErrorBanner error={error}/>
            {help}
        </div>
    );
}

export function FilesField({label, value, onChange, help}: {
    label: ReactNode; value: FileInfo[]; onChange: (v: FileInfo[]) => void; help?: ReactNode;
}) {
    const [error, setError] = useState<unknown>(null);
    const [uploading, setUploading] = useState(false);
    return (
        <div className="field">
            {label}
            <ul className="file-list">
                {value.map((file) => (
                    <li key={file.id}>
                        {file.originalName}
                        <button type="button" className="link-button" onClick={() => onChange(value.filter((f) => f.id !== file.id))}>
                            제거
                        </button>
                    </li>
                ))}
            </ul>
            <input type="file" multiple aria-label="첨부파일 추가" disabled={uploading} onChange={async (e) => {
                const files = Array.from(e.target.files ?? []);
                e.target.value = '';
                setUploading(true);
                setError(null);
                try {
                    const uploaded: FileInfo[] = [];
                    for (const file of files) {
                        uploaded.push(await fileApi.upload(file));
                    }
                    onChange([...value, ...uploaded]);
                } catch (err) {
                    setError(err);
                } finally {
                    setUploading(false);
                }
            }}/>
            {uploading && <p className="muted small">업로드 중…</p>}
            <ErrorBanner error={error}/>
            {help}
        </div>
    );
}
