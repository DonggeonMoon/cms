import {useEffect, useState} from 'react';
import {type BundleFormat, DEFAULT_MESSAGES, i18nApi, type I18nSetting, LANGUAGE_NAMES} from '../../api/i18n';
import {adminApi} from '../../api/endpoints';
import type {Menu} from '../../api/domain';
import {ErrorBanner} from '../../components/ErrorBanner';

const parseProperties = (text: string) => {
    const keys = new Set<string>();
    text.split('\n').forEach((line) => {
        const trimmed = line.trim();
        if (trimmed && !trimmed.startsWith('#') && !trimmed.startsWith('!')) {
            const key = trimmed.split(/\s*[=:]\s*/)[0];
            if (key) {
                keys.add(key);
            }
        }
    });
    return keys;
};

const parseYamlKeys = (text: string) => {
    const keys = new Set<string>();
    text.split('\n').forEach((line) => {
        const match = /^([A-Za-z0-9_.-]+):/.exec(line);
        if (match) {
            keys.add(match[1]);
        }
    });
    return keys;
};

const yamlValue = (value: string) => JSON.stringify(value);

function baseEntries(menus: Menu[], siteName: string | null): [string, string][] {
    const entries: [string, string][] = Object.entries(DEFAULT_MESSAGES);
    if (siteName) {
        entries.push(['site.name', siteName]);
    }
    const walk = (items: Menu[]) => items.forEach((menu) => {
        entries.push([`menu.${menu.id}`, menu.name]);
        walk(menu.children ?? []);
    });
    walk(menus);
    return entries;
}

export function I18nAdminPage() {
    const [setting, setSetting] = useState<I18nSetting | null>(null);
    const [tab, setTab] = useState('');
    const [notice, setNotice] = useState<string | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        i18nApi.setting().then((loaded) => {
            setSetting(loaded);
            setTab(loaded.languages.find((code) => code !== loaded.defaultLanguage) ?? loaded.defaultLanguage);
        }).catch(setError);
    }, []);

    if (!setting) {
        return <section className="card"><h1>국제화</h1>{error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>}</section>;
    }

    const toggle = (code: string, on: boolean) => {
        const languages = on ? [...setting.languages, code] : setting.languages.filter((c) => c !== code);
        setSetting({...setting, languages});
    };

    const save = async () => {
        setError(null);
        setNotice(null);
        try {
            const saved = await i18nApi.updateSetting(setting);
            setSetting(saved);
            setNotice('언어 설정을 저장했습니다.');
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>국제화</h1>
            <div className="muted page-description">
                <p>사용자 화면에서 쓸 언어를 고르고 언어별 번역을 넣습니다. 언어가 둘 이상이면 사용자 화면 상단에 언어 선택이 나타납니다.</p>
                <p>번역은 properties(<code>키 = 번역</code>)나 YAML 형식으로 붙여 넣습니다. 메뉴는 <code>menu.메뉴ID</code>, 사이트 이름은 <code>site.name</code>,
                    하단 문구는 <code>site.footer</code> 키를 씁니다. 번역이 없는 키는 원래 문구가 그대로 보입니다.</p>
            </div>
            <fieldset>
                <legend>사용 언어</legend>
                <div className="row align-center wrap">
                    {Object.entries(LANGUAGE_NAMES).map(([code, name]) => (
                        <label key={code} className="checkbox">
                            <input type="checkbox" checked={setting.languages.includes(code)} onChange={(e) => toggle(code, e.target.checked)}/>
                            {name} <span className="muted small">{code}</span>
                        </label>
                    ))}
                </div>
                <div className="row align-center">
                    <label htmlFor="defaultLanguage">기본 언어</label>
                    <select id="defaultLanguage" value={setting.defaultLanguage}
                            onChange={(e) => setSetting({...setting, defaultLanguage: e.target.value})}>
                        {setting.languages.map((code) => <option key={code} value={code}>{LANGUAGE_NAMES[code] ?? code}</option>)}
                    </select>
                    <button type="button" className="primary" onClick={save}>저장</button>
                    {notice && <span className="ok">{notice}</span>}
                </div>
            </fieldset>
            <ErrorBanner error={error}/>
            <div className="tabs" role="tablist" aria-label="번역 언어">
                {setting.languages.map((code) => (
                    <button key={code} type="button" role="tab" aria-selected={tab === code} className={tab === code ? 'tab active' : 'tab'}
                            onClick={() => setTab(code)}>{LANGUAGE_NAMES[code] ?? code}</button>
                ))}
            </div>
            {tab && <BundleEditor key={tab} language={tab}/>}
        </section>
    );
}

function BundleEditor({language}: { language: string }) {
    const [format, setFormat] = useState<BundleFormat>('PROPERTIES');
    const [text, setText] = useState('');
    const [count, setCount] = useState(0);
    const [notice, setNotice] = useState<string | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        i18nApi.bundle(language, format).then((bundle) => {
            setText(bundle.text);
            setCount(bundle.count);
        }).catch(setError);
    }, [language, format]);

    const fillMissing = async () => {
        setError(null);
        try {
            const [menus, site] = await Promise.all([adminApi.menus.tree(), adminApi.sites.search({size: 1}).catch(() => null)]);
            const existing = format === 'YAML' ? parseYamlKeys(text) : parseProperties(text);
            const missing = baseEntries(menus, site?.content[0]?.name ?? null).filter(([key]) => !existing.has(key));
            const lines = missing.map(([key, value]) => (format === 'YAML' ? `${key}: ${yamlValue(value)}` : `${key} = ${value}`));
            setText((current) => [current.trimEnd(), ...lines].filter(Boolean).join('\n') + '\n');
            setNotice(missing.length ? `키 ${missing.length}개를 한국어 원문과 함께 넣었습니다. 번역으로 바꾼 뒤 저장하세요.` : '빠진 키가 없습니다.');
        } catch (e) {
            setError(e);
        }
    };

    const save = async () => {
        setError(null);
        setNotice(null);
        try {
            const saved = await i18nApi.saveBundle(language, format, text);
            setText(saved.text);
            setCount(saved.count);
            setNotice(`번역 ${saved.count}개를 저장했습니다.`);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="bundle-editor">
            <div className="row align-center">
                <label className="checkbox"><input type="radio" name="format" checked={format === 'PROPERTIES'} onChange={() => setFormat('PROPERTIES')}/> properties</label>
                <label className="checkbox"><input type="radio" name="format" checked={format === 'YAML'} onChange={() => setFormat('YAML')}/> YAML</label>
                <span className="muted small">저장된 번역 {count}개</span>
                <button type="button" className="push-right" onClick={fillMissing}>기본 키 채우기</button>
            </div>
            <textarea aria-label={`${LANGUAGE_NAMES[language] ?? language} 번역`} rows={18} spellCheck={false} value={text}
                      onChange={(e) => setText(e.target.value)}
                      placeholder={format === 'YAML' ? 'nav:\n  login: Log in\nmenu.3: Notice' : 'nav.login = Log in\nmenu.3 = Notice'}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                {notice && <span className="ok">{notice}</span>}
                <button type="button" className="primary" onClick={save}>번역 저장</button>
            </div>
            <p className="muted small">저장하면 이 언어의 번역이 입력한 내용으로 통째로 바뀝니다.</p>
        </div>
    );
}
