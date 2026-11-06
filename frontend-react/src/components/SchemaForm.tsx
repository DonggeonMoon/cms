import {type FormEvent, lazy, type ReactNode, Suspense, useState} from 'react';
import {ApiError} from '../api/client';
import {fileApi} from '../api/files';
import type {FileInfo} from '../api/types';
import type {FormField, SpaceLayout} from '../api/domain';
import {ErrorBanner} from './ErrorBanner';
import type {CategoryAttribute} from '../api/categories';
import {CategoryAttributesEditor} from './CategoryAttributesEditor';
import type {LessonDraft} from '../api/courses';
import {CourseLessonsEditor} from './CourseLessonsEditor';
import {FormFieldsEditor} from './FormFieldsEditor';
import {LayoutEditor} from './LayoutEditor';
import {StableLabel} from './StableLabel';
import {type OptionSet, ProductOptionsEditor} from './ProductOptionsEditor';

export type FieldType = 'text' | 'password' | 'number' | 'checkbox' | 'select' | 'datetime' | 'date' | 'textarea' | 'html' | 'image' | 'files' | 'color' | 'media' | 'time' | 'layout' | 'formFields' | 'productOptions' | 'categoryAttributes' | 'courseLessons';

export interface FieldSpec {
    name: string;
    label: string;
    type?: FieldType;
    required?: boolean;
    options?: { value: string; label: string }[];
    placeholder?: string;
    help?: string;
    min?: number;
    emptyOptionLabel?: string;
    wide?: boolean;
    spaced?: boolean;
    newRow?: boolean;
    group?: string;
    visibleWhen?: (values: FormValues) => boolean;
}

export type FormValues = Record<string, unknown>;

export interface MediaValue {
    id: number;
    name: string;
    durationSeconds: number | null;
    thumbnailId: number | null;
}

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
            {specs.filter((field) => !field.visibleWhen || field.visibleWhen(values)).map((field) => (
                <div key={field.name} className={[
                    field.wide || ['html', 'textarea', 'files', 'media', 'layout', 'formFields', 'productOptions', 'categoryAttributes', 'courseLessons'].includes(field.type ?? '') ? 'wide' : '',
                    field.spaced ? 'spaced' : '',
                    field.newRow ? 'new-row' : '',
                    field.type === 'checkbox' && !field.wide ? 'inline-check' : '',
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
                <button type="submit" className="primary" disabled={saving}><StableLabel current={saving ? '저장 중…' : submitLabel} labels={['저장 중…', submitLabel]}/></button>
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
                        {!field.required && <option value="">{field.emptyOptionLabel ?? '선택 안 함'}</option>}
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
            return <HtmlField id={id} label={label} labelText={field.label} value={(value as string) ?? ''} onChange={onChange} help={help}/>;
        case 'image':
            return <ImageField id={id} label={label} value={value as number | null} onChange={onChange} help={help}/>;
        case 'formFields':
            return (
                <div className="field">
                    {label}
                    <FormFieldsEditor value={(value as FormField[]) ?? []} onChange={onChange}/>
                    {help}
                </div>
            );
        case 'courseLessons':
            return (
                <div className="field">
                    {label}
                    <CourseLessonsEditor value={(value as LessonDraft[]) ?? []} onChange={onChange}/>
                    {help}
                </div>
            );
        case 'categoryAttributes':
            return (
                <div className="field">
                    {label}
                    <CategoryAttributesEditor value={(value as CategoryAttribute[]) ?? []} onChange={onChange}/>
                    {help}
                </div>
            );
        case 'productOptions':
            return (
                <div className="field">
                    {label}
                    <ProductOptionsEditor value={value as OptionSet | undefined} onChange={onChange}/>
                    {help}
                </div>
            );
        case 'layout':
            return (
                <div className="field">
                    {label}
                    <LayoutEditor value={(value as SpaceLayout | null) ?? null} onChange={onChange}/>
                    {help}
                </div>
            );
        case 'media':
            return <MediaField id={id} label={label} value={(value as MediaValue | null) ?? null} onChange={onChange} help={help}/>;
        case 'files':
            return <FilesField label={label} value={(value as FileInfo[]) ?? []} onChange={onChange} help={help}/>;
        default:
            return (
                <div className="field">
                    {label}
                    <input id={id} type={field.type === 'number' ? 'number' : field.type === 'datetime' ? 'datetime-local'
                        : field.type === 'date' ? 'date' : field.type === 'password' ? 'password' : field.type === 'time' ? 'time' : 'text'}
                           value={value == null ? '' : field.type === 'datetime' ? String(value).slice(0, 16) : String(value)}
                           required={field.required} placeholder={field.placeholder} min={field.min}
                           step={field.type === 'number' ? 'any' : undefined}
                           aria-invalid={Boolean(error)} onChange={(e) => onChange(e.target.value)}/>
                    {help}
                </div>
            );
    }
}

const RichTextEditor = lazy(() => import('./RichTextEditor').then((module) => ({default: module.RichTextEditor})));

function HtmlField({id, label, labelText, value, onChange, help}: {
    id: string; label: ReactNode; labelText: string; value: string; onChange: (v: unknown) => void; help: ReactNode;
}) {
    return (
        <div className="field">
            {label}
            <Suspense fallback={<p className="muted">편집기를 불러오는 중…</p>}>
                <RichTextEditor id={id} label={labelText} value={value} onChange={onChange}/>
            </Suspense>
            <p className="muted small">저장할 때 허용되지 않은 태그와 스크립트는 제거됩니다.</p>
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

const THUMBNAIL_MAX_WIDTH = 1280;

interface MediaInfo {
    durationSeconds: number | null;
    frame: File | null;
}

function readMediaInfo(file: File): Promise<MediaInfo> {
    return new Promise((resolve) => {
        const isVideo = !file.type.startsWith('audio/');
        const media = document.createElement(isVideo ? 'video' : 'audio');
        const url = URL.createObjectURL(file);
        let durationSeconds: number | null = null;
        const done = (frame: File | null) => {
            URL.revokeObjectURL(url);
            resolve({durationSeconds, frame});
        };
        media.preload = 'auto';
        media.muted = true;
        media.onerror = () => done(null);
        media.onloadedmetadata = () => {
            durationSeconds = Number.isFinite(media.duration) ? Math.round(media.duration) : null;
            if (!(media instanceof HTMLVideoElement) || !media.videoWidth) {
                done(null);
                return;
            }
            media.onseeked = () => {
                const scale = Math.min(1, THUMBNAIL_MAX_WIDTH / media.videoWidth);
                const canvas = document.createElement('canvas');
                canvas.width = Math.round(media.videoWidth * scale);
                canvas.height = Math.round(media.videoHeight * scale);
                const context = canvas.getContext('2d');
                if (!context) {
                    done(null);
                    return;
                }
                context.drawImage(media, 0, 0, canvas.width, canvas.height);
                canvas.toBlob((blob) => done(blob && new File([blob], `${file.name.replace(/\.[^.]+$/, '')}-thumbnail.jpg`,
                    {type: 'image/jpeg'})), 'image/jpeg', 0.85);
            };
            media.currentTime = Math.min((durationSeconds ?? 0) * 0.1, 5);
        };
        media.src = url;
    });
}

function MediaField({id, label, value, onChange, help}: {
    id: string; label: ReactNode; value: MediaValue | null; onChange: (v: unknown) => void; help: ReactNode;
}) {
    const [error, setError] = useState<unknown>(null);
    const [uploading, setUploading] = useState(false);

    const run = async (action: () => Promise<void>) => {
        setUploading(true);
        setError(null);
        try {
            await action();
        } catch (err) {
            setError(err);
        } finally {
            setUploading(false);
        }
    };

    return (
        <div className="field">
            {label}
            {value && <p>{value.name}</p>}
            <input id={id} type="file" accept="video/*,audio/*" disabled={uploading} onChange={(e) => {
                const file = e.target.files?.[0];
                e.target.value = '';
                if (file) {
                    void run(async () => {
                        const [uploaded, info] = await Promise.all([fileApi.upload(file), readMediaInfo(file)]);
                        const thumbnail = info.frame ? await fileApi.upload(info.frame, `${uploaded.originalName} 썸네일`) : null;
                        onChange({id: uploaded.id, name: uploaded.originalName, durationSeconds: info.durationSeconds,
                            thumbnailId: thumbnail?.id ?? null});
                    });
                }
            }}/>
            {uploading && <p className="muted small">업로드 중…</p>}
            {value && (
                <div className="row align-center">
                    <label htmlFor={`${id}-duration`}>재생 시간(초)</label>
                    <input id={`${id}-duration`} type="number" min={1} required value={value.durationSeconds ?? ''}
                           onChange={(e) => onChange({...value, durationSeconds: e.target.value ? Number(e.target.value) : null})}/>
                </div>
            )}
            {value && value.durationSeconds == null && (
                <p className="muted small">이 브라우저에서 재생 시간을 읽지 못했습니다. 직접 입력해 주세요.</p>
            )}
            {value && (
                <div className="media-thumbnail-field">
                    <label htmlFor={`${id}-thumbnail`}>썸네일</label>
                    {value.thumbnailId
                        ? <img className="preview-image" src={`/api/files/${value.thumbnailId}/view`} alt="썸네일 미리보기"/>
                        : <p className="muted small">썸네일이 없습니다. 동영상은 올릴 때 화면 한 장면을 자동으로 썸네일로 만듭니다.</p>}
                    <div className="row align-center">
                        <input id={`${id}-thumbnail`} type="file" accept="image/*" disabled={uploading} onChange={(e) => {
                            const image = e.target.files?.[0];
                            e.target.value = '';
                            if (image) {
                                void run(async () => onChange({...value, thumbnailId: (await fileApi.upload(image)).id}));
                            }
                        }}/>
                        {value.thumbnailId && <button type="button" onClick={() => onChange({...value, thumbnailId: null})}>썸네일 제거</button>}
                    </div>
                </div>
            )}
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
