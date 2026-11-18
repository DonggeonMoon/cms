import {useEffect, useState} from 'react';
import type {GuestPostingPolicy} from '../api/domain';
import {ErrorBanner} from './ErrorBanner';
import {useI18n} from '../api/i18n';

export function GuestPostingToggle({label, load, save}: {
    label: string;
    load: () => Promise<GuestPostingPolicy>;
    save: (policy: GuestPostingPolicy) => Promise<GuestPostingPolicy>;
}) {
    const {t} = useI18n();
    const [policy, setPolicy] = useState<GuestPostingPolicy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        load().then(setPolicy).catch(setError);
    }, [load]);

    const change = async (next: GuestPostingPolicy) => {
        setError(null);
        setSaved(false);
        try {
            setPolicy(await save(next));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    const contact = (key: 'collectPhone' | 'collectEmail', text: string) => {
        if (!policy) {
            return null;
        }
        const onlyOne = policy[key] && !(key === 'collectPhone' ? policy.collectEmail : policy.collectPhone);
        return (
            <label className="checkbox">
                <input type="checkbox" checked={policy[key]} disabled={onlyOne}
                       onChange={(e) => change({...policy, [key]: e.target.checked})}/>
                {text}
            </label>
        );
    };

    return (
        <div className="settings-bar">
            <label className="checkbox">
                <input type="checkbox" checked={policy?.allowed ?? false} disabled={!policy}
                       onChange={(e) => policy && change({...policy, allowed: e.target.checked})}/>
                {label}
            </label>
            {policy?.allowed && (
                <div className="row align-center guest-contacts">
                    <span className="small">{t('admin.guestPosting.contactInfo', '비회원에게 받을 연락 정보 (하나 이상, 필수 입력):')}</span>
                    {contact('collectPhone', t('admin.guestPosting.phone', '연락처'))}
                    {contact('collectEmail', t('admin.guestPosting.email', '이메일'))}
                </div>
            )}
            <p className="muted small">{t('admin.guestPosting.help', '끄면 로그인한 회원만 등록할 수 있습니다. 바꾸면 바로 저장됩니다.')}{saved && ` ${t('common.saved', '저장했습니다.')}`}</p>
            <ErrorBanner error={error}/>
        </div>
    );
}
