import {useEffect, useState} from 'react';
import {identityVerificationApi} from '../../api/identityVerification';
import {ErrorBanner} from '../../components/ErrorBanner';
import {useI18n} from '../../api/i18n';

export function IdentityVerificationSettingPage() {
    const {t} = useI18n();
    const [enabled, setEnabled] = useState<boolean | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        identityVerificationApi.get().then((s) => setEnabled(s.enabled)).catch(setError);
    }, []);

    if (enabled === null) {
        return <section className="card"><h1>{t('admin.identityVerification.title', '본인인증')}</h1><ErrorBanner error={error}/></section>;
    }

    const change = async (next: boolean) => {
        setError(null);
        setSaved(false);
        try {
            setEnabled((await identityVerificationApi.update(next)).enabled);
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.identityVerification.title', '본인인증')}</h1>
            <div className="muted page-description">
                <p>{t('admin.identityVerification.description', '회원가입·실명 확인 등에서 쓸 본인인증 연동 지점만 제공합니다. 나이스평가정보·PASS 등 실제 인증사 연동은 아직 꽂혀 있지 않아, 켜도 수단이 연동되기 전까지는 요청이 "본인인증 수단이 아직 연동되지 않았습니다" 오류로 끝납니다.')}</p>
            </div>
            <label className="checkbox">
                <input type="checkbox" checked={enabled} onChange={(e) => void change(e.target.checked)}/>
                {t('admin.identityVerification.useFeature', '본인인증 기능 사용')}
            </label>
            {saved && <span className="badge ok">{t('admin.loginSecurity.savedBadge', '저장됨')}</span>}
            <ErrorBanner error={error}/>
        </section>
    );
}
