import {type ReactNode, useState} from 'react';
import {fileApi} from '../api/files';
import type {FileInfo} from '../api/types';
import {ErrorBanner} from './ErrorBanner';
import {useI18n} from '../api/i18n';

export function FilesField({label, value, onChange, help}: {
    label: ReactNode; value: FileInfo[]; onChange: (v: FileInfo[]) => void; help?: ReactNode;
}) {
    const {t} = useI18n();
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
                            {t('admin.filesField.remove', '제거')}
                        </button>
                    </li>
                ))}
            </ul>
            <input type="file" multiple aria-label={t('admin.filesField.add', '첨부파일 추가')} disabled={uploading} onChange={async (e) => {
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
            {uploading && <p className="muted small">{t('admin.filesField.uploading', '업로드 중…')}</p>}
            <ErrorBanner error={error}/>
            {help}
        </div>
    );
}
