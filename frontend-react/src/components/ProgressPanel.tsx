import {useCallback, useEffect, useState} from 'react';
import type {CustomerMessage, Progress, ProgressStageTemplate, ProgressStageEvent, ProgressTargetType} from '../api/domain';
import {adminApi} from '../api/endpoints';
import type {FileInfo} from '../api/types';
import {ErrorBanner} from './ErrorBanner';
import {FilesField} from './SchemaForm';
import {formatDateTime} from './format';

export function ProgressTemplateManager({targetType}: { targetType: ProgressTargetType }) {
    const [open, setOpen] = useState(false);
    const [templates, setTemplates] = useState<ProgressStageTemplate[]>([]);
    const [draft, setDraft] = useState({code: '', label: ''});
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        adminApi.progress.templates(targetType).then(setTemplates).catch(setError);
    }, [targetType]);
    useEffect(() => {
        if (open) {
            reload();
        }
    }, [open, reload]);

    const add = async () => {
        if (!draft.code.trim() || !draft.label.trim()) {
            return;
        }
        setError(null);
        try {
            await adminApi.progress.createTemplate(targetType, {code: draft.code.trim(), label: draft.label.trim(), sequence: templates.length + 1});
            setDraft({code: '', label: ''});
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="status-manager">
            <button type="button" onClick={() => setOpen(!open)}>{open ? '진행 단계 설정 닫기' : '진행 단계 설정'}</button>
            {open && (
                <>
                    <p className="muted small">주문마다 거칠 수 있는 진행 단계를 정합니다(예: 시안 확인, 제작 중, 출고 완료).</p>
                    <ul className="plain-list">
                        {templates.map((t) => (
                            <li key={t.id}>
                                {t.sequence}. {t.label} <code className="small">{t.code}</code>
                                <button type="button" className="link-button" onClick={() => {
                                    if (window.confirm(`'${t.label}' 단계를 지울까요?`)) {
                                        void adminApi.progress.removeTemplate(t.id).then(reload);
                                    }
                                }}>삭제</button>
                            </li>
                        ))}
                        {templates.length === 0 && <li className="muted small">아직 정한 단계가 없습니다.</li>}
                    </ul>
                    <div className="toolbar">
                        <input aria-label="단계 코드" placeholder="코드 (예: DESIGN_CONFIRM)" value={draft.code}
                               onChange={(e) => setDraft({...draft, code: e.target.value})}/>
                        <input aria-label="단계 이름" placeholder="이름 (예: 시안 확인)" value={draft.label}
                               onChange={(e) => setDraft({...draft, label: e.target.value})}/>
                        <button type="button" onClick={add}>단계 추가</button>
                    </div>
                    <ErrorBanner error={error}/>
                </>
            )}
        </div>
    );
}

interface Props {
    getProgress: () => Promise<Progress>;
    advance?: (stageCode: string, memo: string) => Promise<ProgressStageEvent>;
    listMessages: () => Promise<CustomerMessage[]>;
    postMessage: (content: string, fileIds: number[]) => Promise<CustomerMessage>;
}

export function ProgressAndMessages({getProgress, advance, listMessages, postMessage}: Props) {
    const [progress, setProgress] = useState<Progress | null>(null);
    const [stageCode, setStageCode] = useState('');
    const [memo, setMemo] = useState('');
    const [messages, setMessages] = useState<CustomerMessage[] | null>(null);
    const [content, setContent] = useState('');
    const [files, setFiles] = useState<FileInfo[]>([]);
    const [error, setError] = useState<unknown>(null);

    const reloadProgress = useCallback(() => {
        getProgress().then(setProgress).catch(setError);
    }, [getProgress]);
    const reloadMessages = useCallback(() => {
        listMessages().then(setMessages).catch(setError);
    }, [listMessages]);
    useEffect(reloadProgress, [reloadProgress]);
    useEffect(reloadMessages, [reloadMessages]);

    const submitAdvance = async () => {
        if (!advance || !stageCode) {
            return;
        }
        setError(null);
        try {
            await advance(stageCode, memo);
            setMemo('');
            reloadProgress();
        } catch (e) {
            setError(e);
        }
    };

    const submitMessage = async () => {
        if (!content.trim()) {
            return;
        }
        setError(null);
        try {
            await postMessage(content.trim(), files.map((f) => f.id));
            setContent('');
            setFiles([]);
            reloadMessages();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="progress-panel">
            <h3>진행 단계</h3>
            {progress && (
                <>
                    {progress.history.length === 0 && <p className="muted small">아직 진행 단계가 없습니다.</p>}
                    <ol className="progress-history">
                        {progress.history.map((event) => (
                            <li key={event.id}>
                                <strong>{event.stageLabel}</strong> · {formatDateTime(event.createdAt)}
                                {event.actorName && ` · ${event.actorName}`}
                                {event.memo && <div className="muted small">{event.memo}</div>}
                            </li>
                        ))}
                    </ol>
                    {advance && progress.templates.length > 0 && (
                        <div className="toolbar">
                            <select aria-label="다음 단계" value={stageCode} onChange={(e) => setStageCode(e.target.value)}>
                                <option value="">단계 선택</option>
                                {progress.templates.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
                            </select>
                            <input aria-label="메모" placeholder="메모(선택)" value={memo} onChange={(e) => setMemo(e.target.value)}/>
                            <button type="button" disabled={!stageCode} onClick={submitAdvance}>단계 변경</button>
                        </div>
                    )}
                </>
            )}

            <h3>메시지</h3>
            <ul className="message-thread">
                {messages?.map((message) => (
                    <li key={message.id} className={message.senderType === 'ADMIN' ? 'from-admin' : 'from-member'}>
                        <div className="muted small">{message.senderName} · {formatDateTime(message.createdAt)}</div>
                        <p className="pre-wrap">{message.content}</p>
                        {message.fileUrls.length > 0 && (
                            <div className="work-photos">
                                {message.fileUrls.map((url, i) => <a key={url} href={url} target="_blank" rel="noreferrer">첨부 {i + 1}</a>)}
                            </div>
                        )}
                    </li>
                ))}
                {messages?.length === 0 && <li className="muted small">아직 메시지가 없습니다.</li>}
            </ul>
            <div className="field">
                <label htmlFor="message-content">메시지 보내기</label>
                <textarea id="message-content" rows={3} value={content} onChange={(e) => setContent(e.target.value)}/>
            </div>
            <FilesField label="첨부 파일" value={files} onChange={setFiles}/>
            <button type="button" className="primary" disabled={!content.trim()} onClick={submitMessage}>보내기</button>
            <ErrorBanner error={error}/>
        </div>
    );
}
