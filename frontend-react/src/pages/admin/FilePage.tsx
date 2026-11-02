import {type FormEvent, useCallback, useEffect, useRef, useState} from 'react';
import {fileAdminApi, fileApi} from '../../api/files';
import type {FileInfo, MediaType, Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatBytes, formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

const MEDIA_LABELS: Record<MediaType, string> = {DOCUMENT: '문서', IMAGE: '이미지', VIDEO: '영상', ETC: '기타'};

export function FilePage() {
    const [keyword, setKeyword] = useState('');
    const [mediaType, setMediaType] = useState<MediaType | ''>('');
    const [page, setPage] = useState(0);
    const [result, setResult] = useState<Page<FileInfo> | null>(null);
    const [error, setError] = useState<unknown>(null);

    const load = useCallback(() => {
        fileAdminApi.search(keyword, mediaType, page).then(setResult).catch(setError);
    }, [keyword, mediaType, page]);

    useEffect(load, [load]);

    const remove = async (file: FileInfo) => {
        if (!window.confirm(`'${file.originalName}' 파일을 삭제할까요?`)) {
            return;
        }
        try {
            await fileAdminApi.remove(file.id);
            load();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>파일</h1>
            <UploadForm onUploaded={() => {
                setPage(0);
                load();
            }}/>
            <div className="toolbar">
                <input aria-label="파일명 검색" placeholder="파일명" value={keyword}
                       onChange={(e) => {
                           setKeyword(e.target.value);
                           setPage(0);
                       }}/>
                <select aria-label="미디어 종류" value={mediaType}
                        onChange={(e) => {
                            setMediaType(e.target.value as MediaType | '');
                            setPage(0);
                        }}>
                    <option value="">전체</option>
                    {Object.entries(MEDIA_LABELS).map(([value, label]) => (
                        <option key={value} value={value}>{label}</option>
                    ))}
                </select>
            </div>
            <ErrorBanner error={error}/>
            {result && (
                <>
                    <table>
                        <thead>
                        <tr>
                            <th>미리보기</th>
                            <th>파일명</th>
                            <th>종류</th>
                            <th>크기</th>
                            <th>다운로드</th>
                            <th>업로드일</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {result.content.map((file) => (
                            <tr key={file.id}>
                                <td>
                                    {file.thumbnailUrl
                                        ? <img className="thumb" src={file.thumbnailUrl} alt={file.alternativeText ?? file.originalName}/>
                                        : <span className="muted">{file.extension}</span>}
                                </td>
                                <td><a href={file.downloadUrl}>{file.originalName}</a></td>
                                <td>{MEDIA_LABELS[file.mediaType]}</td>
                                <td>{formatBytes(file.size)}</td>
                                <td>{file.downloadCount}</td>
                                <td>{formatDateTime(file.createdAt)}</td>
                                <td className="actions">
                                    <button type="button" className="danger" onClick={() => remove(file)}>삭제</button>
                                </td>
                            </tr>
                        ))}
                        {result.content.length === 0 && (
                            <tr>
                                <td colSpan={7} className="muted">파일이 없습니다.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={result.page.totalPages} onChange={setPage}/>
                </>
            )}
        </section>
    );
}

function UploadForm({onUploaded}: { onUploaded: () => void }) {
    const inputRef = useRef<HTMLInputElement>(null);
    const [alternativeText, setAlternativeText] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [uploading, setUploading] = useState(false);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        const file = inputRef.current?.files?.[0];
        if (!file) {
            return;
        }
        setUploading(true);
        setError(null);
        try {
            await fileApi.upload(file, alternativeText);
            if (inputRef.current) {
                inputRef.current.value = '';
            }
            setAlternativeText('');
            onUploaded();
        } catch (e) {
            setError(e);
        } finally {
            setUploading(false);
        }
    };

    return (
        <form className="inline-form" onSubmit={handleSubmit}>
            <h2>파일 업로드</h2>
            <div className="row align-end">
                <input ref={inputRef} type="file" aria-label="업로드할 파일" required/>
                <input aria-label="대체 텍스트" placeholder="대체 텍스트 (이미지)" value={alternativeText}
                       onChange={(e) => setAlternativeText(e.target.value)}/>
                <button type="submit" className="primary" disabled={uploading}>{uploading ? '업로드 중…' : '업로드'}</button>
            </div>
            <ErrorBanner error={error}/>
        </form>
    );
}
