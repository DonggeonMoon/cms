import {type FormEvent, useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {ApiKeyOption, ChatbotProvider, ChatbotSetting} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox} from '../../components/Field';
import {StableLabel} from '../../components/StableLabel';
import {useI18n} from '../../api/i18n';

const providers = (t: (key: string, fallback: string) => string): { value: ChatbotProvider; label: string; modelHint: string }[] => [
    {value: 'CLAUDE', label: 'Claude (Anthropic)', modelHint: t('admin.chatbot.hint.claude', '예: claude-haiku-5-5, claude-sonnet-5-5')},
    {value: 'OPENAI', label: 'ChatGPT (OpenAI)', modelHint: t('admin.chatbot.hint.openai', 'OpenAI 문서의 모델 ID를 입력하세요.')},
    {value: 'GEMINI', label: 'Gemini (Google)', modelHint: t('admin.chatbot.hint.gemini', 'Google AI 문서의 모델 ID를 입력하세요.')},
];

export function ChatbotAdminPage() {
    const {t} = useI18n();
    const [form, setForm] = useState<ChatbotSetting | null>(null);
    const [apiKeys, setApiKeys] = useState<ApiKeyOption[]>([]);
    const [error, setError] = useState<unknown>(null);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        Promise.all([adminApi.chatbot.get(), adminApi.apiKeys.options()])
            .then(([setting, keys]) => {
                setForm(setting);
                setApiKeys(keys);
            })
            .catch(setError);
    }, []);

    if (!form) {
        return <section className="card"><h1>{t('admin.nav.chatbot', '챗봇')}</h1>{error ? <ErrorBanner error={error}/> : <p className="muted">{t('common.loading', '불러오는 중…')}</p>}</section>;
    }
    const allProviders = providers(t);
    const provider = allProviders.find((p) => p.value === form.provider) ?? allProviders[0];

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        setSaving(true);
        setSaved(false);
        setError(null);
        try {
            const {defaultInstructions: _, ...body} = form;
            setForm(await adminApi.chatbot.save(body));
            setSaved(true);
        } catch (e) {
            setError(e);
        } finally {
            setSaving(false);
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.nav.chatbot', '챗봇')}</h1>
            <div className="muted page-description">
                <p>
                    {t('admin.chatbot.description1', '사용자 화면 오른쪽 아래에 상담 챗봇을 띄웁니다. API 키는')} <Link to="/admin/api-keys">{t('admin.chatbot.apiKeyMenu', '보안 > API 키 관리')}</Link>
                    {t('admin.chatbot.description2', '에서 먼저 등록한 뒤 여기서 고릅니다.')}
                </p>
            </div>
            <form onSubmit={handleSubmit}>
                <Checkbox label={t('admin.chatbot.enabled', '챗봇 사용')} name="enabled" checked={form.enabled}
                          onChange={(e) => setForm({...form, enabled: e.target.checked})}/>
                <div className="field">
                    <label htmlFor="provider">{t('admin.chatbot.provider', 'AI 제공사')}</label>
                    <select id="provider" value={form.provider}
                            onChange={(e) => setForm({...form, provider: e.target.value as ChatbotProvider})}>
                        {allProviders.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
                    </select>
                </div>
                <div className="field">
                    <label htmlFor="apiKeyId">{t('admin.chatbot.apiKey', 'API 키')}</label>
                    <select id="apiKeyId" value={form.apiKeyId ?? ''}
                            onChange={(e) => setForm({...form, apiKeyId: e.target.value ? Number(e.target.value) : null})}>
                        <option value="">{t('admin.social.notSelected', '선택 안 함')}</option>
                        {apiKeys.map((key) => <option key={key.id} value={key.id}>{key.name}</option>)}
                    </select>
                    {apiKeys.length === 0 && <p className="muted small">{t('admin.chatbot.noApiKeys', '등록된 API 키가 없습니다. 보안 > API 키 관리에서 먼저 등록하세요.')}</p>}
                </div>
                <div className="field">
                    <label htmlFor="model">{t('admin.chatbot.model', '모델')}</label>
                    <input id="model" value={form.model ?? ''} placeholder={provider.modelHint}
                           onChange={(e) => setForm({...form, model: e.target.value})}/>
                    <p className="muted small">{provider.modelHint}</p>
                </div>
                <div className="field">
                    <label htmlFor="welcomeMessage">{t('admin.chatbot.welcomeMessage', '첫 인사말')}</label>
                    <input id="welcomeMessage" value={form.welcomeMessage ?? ''} placeholder={t('admin.chatbot.welcomeMessagePlaceholder', '안녕하세요! 무엇을 도와드릴까요?')}
                           onChange={(e) => setForm({...form, welcomeMessage: e.target.value})}/>
                </div>
                <div className="field">
                    <label htmlFor="dailyLimitPerVisitor">{t('admin.chatbot.dailyLimit', '방문자당 하루 질문 수')}</label>
                    <input id="dailyLimitPerVisitor" type="number" min={1} max={100000} required value={form.dailyLimitPerVisitor}
                           onChange={(e) => setForm({...form, dailyLimitPerVisitor: Number(e.target.value)})}/>
                    <p className="muted small">{t('admin.chatbot.dailyLimitHelp', '같은 IP에서 최근 24시간 동안 보낼 수 있는 질문 수입니다. AI 사용 요금이 과하게 나오지 않도록 막습니다.')}</p>
                </div>
                <div className="field">
                    <div className="field-head">
                        <label htmlFor="instructions">{t('admin.chatbot.instructions', '지침')}</label>
                        <button type="button" className="link-button"
                                onClick={() => setForm({...form, instructions: form.defaultInstructions})}>{t('admin.chatbot.resetInstructions', '기본 지침으로 되돌리기')}</button>
                    </div>
                    <textarea id="instructions" rows={16} required value={form.instructions}
                              onChange={(e) => setForm({...form, instructions: e.target.value})}/>
                    <p className="muted small">{t('admin.chatbot.instructionsHelp', 'AI에 매번 함께 전달되는 안내문입니다. 사이트 이름은 자동으로 앞에 붙습니다. 기본 지침에는 지침을 바꾸거나 빼내려는 요청(프롬프트 인젝션)을 막는 규칙이 들어 있으니, 고칠 때도 그 부분은 남겨 두세요.')}</p>
                </div>
                <ErrorBanner error={error}/>
                {saved && <p className="ok">{t('common.saved', '저장했습니다.')}</p>}
                <div className="toolbar form-actions">
                    <button type="submit" className="primary" disabled={saving}>
                        <StableLabel current={saving ? t('admin.memberJoin.saving', '저장 중…') : t('common.save', '저장')} labels={[t('admin.memberJoin.saving', '저장 중…'), t('common.save', '저장')]}/>
                    </button>
                </div>
            </form>
        </section>
    );
}
