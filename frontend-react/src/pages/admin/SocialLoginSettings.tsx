import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {ApiKeyOption} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {socialAdminApi, type SocialLoginSetting, type SocialProvider} from '../../api/social';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox} from '../../components/Field';
import type {ReauthGuard} from '../../components/ReauthGate';

interface ProviderGuide {
    clientId: string;
    clientSecret: string | null;
    steps: string;
}

const GUIDES: Record<SocialProvider, ProviderGuide> = {
    KAKAO: {
        clientId: 'REST API 키',
        clientSecret: 'Client Secret (보안 설정에서 사용으로 켰을 때만)',
        steps: '앱을 만들고 카카오 로그인을 활성화한 뒤 Redirect URI를 등록합니다. 이메일을 받으려면 동의항목에서 카카오계정(이메일)을 설정해야 하며, 비즈 앱 전환이 필요할 수 있습니다.',
    },
    NAVER: {
        clientId: 'Client ID',
        clientSecret: 'Client Secret',
        steps: '애플리케이션을 등록하고 사용 API에서 네이버 로그인을, 서비스 환경에서 PC 웹과 Callback URL을 등록합니다. 받을 정보(이메일·이름 등)를 고른 뒤 검수를 받아야 모든 사용자가 쓸 수 있습니다.',
    },
    GOOGLE: {
        clientId: '클라이언트 ID',
        clientSecret: '클라이언트 보안 비밀번호',
        steps: 'OAuth 동의 화면을 구성하고, 사용자 인증 정보에서 웹 애플리케이션 OAuth 클라이언트를 만든 뒤 승인된 리디렉션 URI를 등록합니다.',
    },
    APPLE: {
        clientId: 'Services ID',
        clientSecret: null,
        steps: 'App ID에 Sign in with Apple을 켜고, Services ID를 만들어 도메인과 Return URL을 등록합니다. Keys에서 Sign in with Apple 키를 만들어 .p8 파일 내용과 Key ID를 넣습니다. HTTPS 도메인에서만 동작합니다.',
    },
    FACEBOOK: {
        clientId: '앱 ID',
        clientSecret: '앱 시크릿 코드',
        steps: '앱을 만들고 Facebook 로그인 제품을 추가한 뒤 유효한 OAuth 리디렉션 URI를 등록합니다. 이메일 권한을 쓰려면 앱 검수와 라이브 모드 전환이 필요합니다.',
    },
};

export function SocialLoginSettings({guard}: { guard: ReauthGuard }) {
    const [settings, setSettings] = useState<SocialLoginSetting[] | null>(null);
    const [apiKeys, setApiKeys] = useState<ApiKeyOption[]>([]);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        Promise.all([guard(socialAdminApi.settings()), adminApi.apiKeys.options()])
            .then(([loaded, keys]) => {
                setSettings(loaded);
                setApiKeys(keys);
            })
            .catch(setError);
    }, [guard]);

    return (
        <section className="card">
            <h2>소셜 로그인</h2>
            <div className="muted page-description">
                <p>켜 둔 제공자의 버튼이 로그인·회원가입 화면에 나타납니다. 처음 로그인한 사람은 개인정보 동의와 위 가입 항목을 입력한 뒤 가입됩니다.</p>
                <p>키는 <Link to="/admin/api-keys">API 키 관리</Link>에 먼저 등록하고 여기서 이름으로 고릅니다. 각 제공자의 개발자 콘솔에는 아래
                    Redirect URI를 그대로 등록해야 합니다. 운영 도메인이 다르면 서버 환경변수 <code>CMS_PUBLIC_BASE_URL</code>로 주소를 지정합니다.</p>
            </div>
            <ErrorBanner error={error}/>
            {settings?.map((setting) => (
                <ProviderCard key={setting.provider} initial={setting} apiKeys={apiKeys} guard={guard}/>
            ))}
        </section>
    );
}

function ProviderCard({initial, apiKeys, guard}: { initial: SocialLoginSetting; apiKeys: ApiKeyOption[]; guard: ReauthGuard }) {
    const [setting, setSetting] = useState(initial);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);
    const [copied, setCopied] = useState(false);
    const guide = GUIDES[setting.provider];
    const id = (field: string) => `social-${setting.provider}-${field}`;

    const keySelect = (field: 'clientIdKeyId' | 'clientSecretKeyId' | 'applePrivateKeyKeyId', label: string) => (
        <div className="field">
            <label htmlFor={id(field)}>{label}</label>
            <select id={id(field)} value={setting[field] ?? ''}
                    onChange={(e) => setSetting({...setting, [field]: e.target.value ? Number(e.target.value) : null})}>
                <option value="">선택 안 함</option>
                {apiKeys.map((key) => <option key={key.id} value={key.id}>{key.name}</option>)}
            </select>
        </div>
    );

    const save = async () => {
        setError(null);
        setSaved(false);
        try {
            setSetting(await guard(socialAdminApi.save(setting.provider, {
                enabled: setting.enabled,
                clientIdKeyId: setting.clientIdKeyId,
                clientSecretKeyId: setting.clientSecretKeyId,
                appleTeamId: setting.appleTeamId,
                appleKeyId: setting.appleKeyId,
                applePrivateKeyKeyId: setting.applePrivateKeyKeyId,
            })));
            setSaved(true);
        } catch (e) {
            setError(e);
        }
    };

    const copy = async () => {
        try {
            await navigator.clipboard.writeText(setting.redirectUri);
            setCopied(true);
        } catch {
            setCopied(false);
        }
    };

    return (
        <fieldset className="social-setting">
            <legend>
                {setting.label}{' '}
                {setting.enabled ? <span className="badge ok">사용 중</span> : <span className="badge">꺼짐</span>}
            </legend>
            <Checkbox label={`${setting.label} 로그인 사용`} name={id('enabled')} checked={setting.enabled}
                      onChange={(e) => setSetting({...setting, enabled: e.target.checked})}/>
            <div className="schema-grid">
                {keySelect('clientIdKeyId', guide.clientId)}
                {guide.clientSecret && keySelect('clientSecretKeyId', guide.clientSecret)}
                {setting.provider === 'APPLE' && (
                    <>
                        <div className="field">
                            <label htmlFor={id('team')}>Team ID</label>
                            <input id={id('team')} value={setting.appleTeamId ?? ''} maxLength={20}
                                   onChange={(e) => setSetting({...setting, appleTeamId: e.target.value})}/>
                        </div>
                        <div className="field">
                            <label htmlFor={id('key')}>Key ID</label>
                            <input id={id('key')} value={setting.appleKeyId ?? ''} maxLength={20}
                                   onChange={(e) => setSetting({...setting, appleKeyId: e.target.value})}/>
                        </div>
                        {keySelect('applePrivateKeyKeyId', '개인 키 (.p8 파일 내용)')}
                    </>
                )}
            </div>
            <div className="field">
                <label htmlFor={id('redirect')}>Redirect URI (개발자 콘솔에 등록)</label>
                <div className="row align-center">
                    <input id={id('redirect')} readOnly value={setting.redirectUri} onFocus={(e) => e.target.select()}/>
                    <button type="button" onClick={copy}>{copied ? '복사됨' : '복사'}</button>
                </div>
            </div>
            <p className="muted small">
                {guide.steps}{' '}
                <a href={setting.consoleUrl} target="_blank" rel="noreferrer">개발자 콘솔 열기</a>
            </p>
            <ErrorBanner error={error}/>
            <div className="toolbar">
                <button type="button" className="primary" onClick={save}>{setting.label} 설정 저장</button>
                {saved && <span className="ok">저장했습니다.</span>}
            </div>
        </fieldset>
    );
}
