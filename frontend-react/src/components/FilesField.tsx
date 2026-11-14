import {type ReactNode, useState} from 'react';
import {fileApi} from '../api/files';
import type {FileInfo} from '../api/types';
import {ErrorBanner} from './ErrorBanner';

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
