import {type FormEvent, useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {ApiKeyOption, PostcodeProvider, PostcodeSetting} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {useI18n} from '../../api/i18n';

interface SettingForm {
    provider: PostcodeProvider;
    jusoApiKeyId: string;
}

const toForm = (setting: PostcodeSetting): SettingForm => ({
    provider: setting.provider,
    jusoApiKeyId: setting.jusoApiKeyId?.toString() ?? '',
});

const toNumber = (value: string) => (value.trim() === '' ? null : Number(value));

export function PostcodeAdminPage() {
    const {t} = useI18n();
    const [form, setForm] = useState<SettingForm | null>(null);
    const [apiKeys, setApiKeys] = useState<ApiKeyOption[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [message, setMessage] = useState('');

    useEffect(() => {
        Promise.all([adminApi.postcode.settings(), adminApi.apiKeys.options()]).then(([setting, keys]) => {
            setApiKeys(keys);
            setForm(toForm(setting));
        }).catch(setError);
    }, []);

    if (!form) {
        return (
            <section className="card">
                <h1>{t('admin.postcode.title', '우편번호 API')}</h1>
                {error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>}
            </section>
        );
    }

    const set = <K extends keyof SettingForm>(key: K, value: SettingForm[K]) => setForm({...form, [key]: value});

    const save = async (event?: FormEvent) => {
        event?.preventDefault();
        setError(null);
        setMessage('');
        try {
            const updated = await adminApi.postcode.updateSettings({...form, jusoApiKeyId: toNumber(form.jusoApiKeyId)});
            setForm(toForm(updated));
            setMessage(t('common.saved', '저장했습니다.'));
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.postcode.title', '우편번호 API')}</h1>
            <p className="muted page-description">{t('admin.postcode.description', '주소 입력 화면의 "우편번호 찾기"가 어떤 API로 주소를 검색할지 고릅니다.')}</p>

            <form className="inline-form" onSubmit={save}>
                <div className="field">
                    <label htmlFor="provider">{t('admin.postcode.provider', '우편번호 검색 제공자')}</label>
                    <select id="provider" value={form.provider} onChange={(e) => set('provider', e.target.value as PostcodeProvider)}>
                        <option value="KAKAO">{t('admin.postcode.kakao', '카카오 우편번호 (무료, API 키 불필요)')}</option>
                        <option value="JUSO">{t('admin.postcode.juso', '행안부 도로명주소 API')}</option>
                    </select>
                </div>
                {form.provider === 'JUSO' && (
                    <>
                        <div className="field">
                            <label htmlFor="jusoApiKeyId">{t('admin.postcode.jusoApiKey', '행안부 API 키')}</label>
                            <select id="jusoApiKeyId" value={form.jusoApiKeyId} onChange={(e) => set('jusoApiKeyId', e.target.value)}>
                                <option value="">{t('admin.postcode.pleaseSelect', '선택하세요')}</option>
                                {apiKeys.map((k) => <option key={k.id} value={k.id}>{k.name}</option>)}
                            </select>
                        </div>
                        <p className="muted small">
                            {t('admin.postcode.jusoHelp1', '행안부 주소정보 누리집(juso.go.kr)에서 발급받은 도로명주소 API 키를')} <Link to="/admin/api-keys">{t('admin.apiKey.title', 'API 키 관리')}</Link>{t('admin.postcode.jusoHelp2', '에 먼저 등록한 뒤 이름으로 고릅니다.')}
                        </p>
                    </>
                )}
                <ErrorBanner error={error}/>
                {message && <p className="ok">{message}</p>}
                <div className="toolbar form-actions">
                    <button type="submit" className="primary">{t('common.save', '저장')}</button>
                </div>
            </form>
        </section>
    );
}
