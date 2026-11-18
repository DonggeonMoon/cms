import {useState} from 'react';
import {statusApi} from '../api/statuses';
import type {StatusDefinition, StatusDomain} from '../api/domain';
import {useI18n} from '../api/i18n';
import {ErrorBanner} from './ErrorBanner';

export function StatusManager({domain, statuses, onChange}: {
    domain: StatusDomain; statuses: StatusDefinition[]; onChange: () => void;
}) {
    const {t} = useI18n();
    const [open, setOpen] = useState(false);
    const [error, setError] = useState<unknown>(null);
    const [draft, setDraft] = useState({label: '', baseStatus: ''});
    const system = statuses.filter((s) => s.system);

    const run = async (action: () => Promise<unknown>) => {
        setError(null);
        try {
            await action();
            onChange();
        } catch (e) {
            setError(e);
        }
    };

    if (!open) {
        return <button type="button" onClick={() => setOpen(true)}>{t('admin.statusManager.open', '상태 관리')}</button>;
    }
    return (
        <div className="status-manager">
            <div className="row space-between">
                <strong>{t('admin.statusManager.open', '상태 관리')}</strong>
                <button type="button" onClick={() => setOpen(false)}>{t('common.close', '닫기')}</button>
            </div>
            <p className="muted small">
                {t('admin.statusManager.help', '기본 상태는 결제·취소 처리와 묶여 있어 이름만 바꿀 수 있습니다. 세부 상태는 기본 상태 아래에 추가해 진행 단계를 더 자세히 표시합니다.')}
            </p>
            <ul className="status-list">
                {system.map((base) => (
                    <li key={base.id}>
                        <input aria-label={t('admin.statusManager.baseName', '{code} 이름', {code: base.code})} defaultValue={base.label}
                               onBlur={(e) => e.target.value.trim() && e.target.value !== base.label
                                   && void run(() => statusApi.update(base.id, {label: e.target.value}))}/>
                        <span className="muted small"> {t('admin.statusManager.base', '기본')}</span>
                        <ul>
                            {statuses.filter((s) => !s.system && s.baseStatus === base.code).map((custom) => (
                                <li key={custom.id}>
                                    <input aria-label={t('admin.statusManager.customName', '{label} 이름', {label: custom.label})} defaultValue={custom.label}
                                           onBlur={(e) => e.target.value.trim() && e.target.value !== custom.label
                                               && void run(() => statusApi.update(custom.id, {label: e.target.value, baseStatus: custom.baseStatus}))}/>
                                    <button type="button" className="danger" onClick={() => {
                                        if (window.confirm(t('admin.statusManager.confirmDelete', "'{label}' 상태를 삭제할까요? 이 상태인 건은 기본 상태로 표시됩니다.", {label: custom.label}))) {
                                            void run(() => statusApi.remove(custom.id));
                                        }
                                    }}>{t('common.delete', '삭제')}</button>
                                </li>
                            ))}
                        </ul>
                    </li>
                ))}
            </ul>
            <div className="row align-center">
                <select aria-label={t('admin.statusManager.pickBase', '세부 상태를 둘 기본 상태')} value={draft.baseStatus}
                        onChange={(e) => setDraft({...draft, baseStatus: e.target.value})}>
                    <option value="">{t('admin.statusManager.pickBasePlaceholder', '기본 상태 선택')}</option>
                    {system.map((s) => <option key={s.code} value={s.code}>{s.label}</option>)}
                </select>
                <input aria-label={t('admin.statusManager.newName', '새 세부 상태 이름')} placeholder={t('admin.statusManager.newName', '새 세부 상태 이름')}
                       value={draft.label} onChange={(e) => setDraft({...draft, label: e.target.value})}/>
                <button type="button" disabled={!draft.label.trim() || !draft.baseStatus} onClick={() => run(async () => {
                    await statusApi.create(domain, draft);
                    setDraft({label: '', baseStatus: draft.baseStatus});
                })}>{t('admin.statusManager.add', '세부 상태 추가')}</button>
            </div>
            <ErrorBanner error={error}/>
        </div>
    );
}

export function CustomStatusSelect({statuses, status, customStatus, onApply}: {
    statuses: StatusDefinition[]; status: string; customStatus: string | null; onApply: (code: string | null) => void;
}) {
    const {t} = useI18n();
    const options = statuses.filter((s) => !s.system && s.baseStatus === status);
    if (options.length === 0) {
        return null;
    }
    return (
        <select aria-label={t('admin.statusManager.custom', '세부 상태')} value={customStatus ?? ''} onChange={(e) => onApply(e.target.value || null)}>
            <option value="">{t('admin.statusManager.noCustom', '세부 상태 없음')}</option>
            {options.map((s) => <option key={s.code} value={s.code}>{s.label}</option>)}
        </select>
    );
}
