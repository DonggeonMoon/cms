import {type FormEvent, useState} from 'react';
import {ErrorBanner} from './ErrorBanner';
import {useI18n} from '../api/i18n';

export function CommentEditor({initial, needsPassword, onSave, onCancel}: {
    initial: string;
    needsPassword?: boolean;
    onSave: (content: string, password?: string) => Promise<void>;
    onCancel: () => void;
}) {
    const {t} = useI18n();
    const [content, setContent] = useState(initial);
    const [password, setPassword] = useState('');
    const [error, setError] = useState<unknown>(null);
    const [saving, setSaving] = useState(false);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSaving(true);
        setError(null);
        try {
            await onSave(content, needsPassword ? password : undefined);
        } catch (e) {
            setError(e);
        } finally {
            setSaving(false);
        }
    };

    return (
        <form className="comment-form" onSubmit={handleSubmit}>
            <textarea aria-label={t('shop.commentEditor.content', '댓글 수정 내용')} rows={3} required value={content} onChange={(e) => setContent(e.target.value)}/>
            {needsPassword && (
                <input type="password" aria-label={t('shop.commentEditor.password', '댓글 비밀번호')} placeholder={t('shop.commentEditor.passwordPlaceholder', '작성 시 입력한 비밀번호')} required value={password}
                       onChange={(e) => setPassword(e.target.value)}/>
            )}
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={saving}>{t('common.save', '저장')}</button>
                <button type="button" onClick={onCancel}>{t('common.cancel', '취소')}</button>
            </div>
        </form>
    );
}
