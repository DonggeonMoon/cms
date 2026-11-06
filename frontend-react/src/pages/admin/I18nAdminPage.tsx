import {Fragment, type FormEvent, useCallback, useEffect, useState} from 'react';
import {DEFAULT_MESSAGES, i18nApi, type I18nSetting, LANGUAGE_NAMES, type Translation} from '../../api/i18n';
import {adminApi} from '../../api/endpoints';
import type {Menu} from '../../api/domain';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Field} from '../../components/Field';

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
                <p>번역은 키 하나씩 등록·수정·삭제합니다. 메뉴는 <code>menu.메뉴ID</code>, 사이트 이름은 <code>site.name</code>,
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
            {tab && <TranslationEditor key={tab} language={tab}/>}
        </section>
    );
}

function TranslationEditor({language}: { language: string }) {
    const [translations, setTranslations] = useState<Translation[] | null>(null);
    const [adding, setAdding] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [notice, setNotice] = useState<string | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        i18nApi.translations(language).then(setTranslations).catch(setError);
    }, [language]);
    useEffect(reload, [reload]);

    const remove = async (translation: Translation) => {
        if (!window.confirm(`'${translation.key}' 번역을 삭제할까요?`)) {
            return;
        }
        setError(null);
        try {
            await i18nApi.removeTranslation(translation.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const fillMissing = async () => {
        setError(null);
        setNotice(null);
        try {
            const [menus, site] = await Promise.all([adminApi.menus.tree(), adminApi.sites.search({size: 1}).catch(() => null)]);
            const existing = new Set((translations ?? []).map((t) => t.key));
            const missing = baseEntries(menus, site?.content[0]?.name ?? null).filter(([key]) => !existing.has(key));
            for (const [key, message] of missing) {
                await i18nApi.createTranslation(language, key, message);
            }
            setNotice(missing.length ? `키 ${missing.length}개를 한국어 원문과 함께 추가했습니다. 번역으로 바꿔 주세요.` : '빠진 키가 없습니다.');
            reload();
        } catch (e) {
            setError(e);
        }
    };

    return (
        <div className="translation-editor">
            <div className="toolbar list-toolbar">
                <span className="muted small">저장된 번역 {translations?.length ?? 0}개</span>
                <button type="button" onClick={fillMissing}>기본 키 채우기</button>
                <button type="button" className="primary push-right" onClick={() => setAdding(true)}>번역 추가</button>
            </div>
            {adding && (
                <TranslationForm onCancel={() => setAdding(false)}
                                 onSave={(key, message) => i18nApi.createTranslation(language, key, message).then(() => {
                                     setAdding(false);
                                     reload();
                                 })}/>
            )}
            {notice && <p className="ok">{notice}</p>}
            <ErrorBanner error={error}/>
            {translations && (
                <div className="table-wrap">
                    <table>
                        <thead><tr><th>키</th><th>번역</th><th/></tr></thead>
                        <tbody>
                        {translations.map((translation) => (
                            <Fragment key={translation.id}>
                                <tr>
                                    <td><code>{translation.key}</code></td>
                                    <td>{translation.message}</td>
                                    <td className="actions">
                                        <button type="button" onClick={() => setEditingId(editingId === translation.id ? null : translation.id)}>
                                            {editingId === translation.id ? '닫기' : '수정'}
                                        </button>
                                        <button type="button" className="danger" onClick={() => remove(translation)}>삭제</button>
                                    </td>
                                </tr>
                                {editingId === translation.id && (
                                    <tr className="expanded-row">
                                        <td colSpan={3}>
                                            <TranslationForm initialKey={translation.key} initialMessage={translation.message}
                                                             onCancel={() => setEditingId(null)}
                                                             onSave={(key, message) => i18nApi.updateTranslation(translation.id, key, message).then(() => {
                                                                 setEditingId(null);
                                                                 reload();
                                                             })}/>
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        ))}
                        {translations.length === 0 && <tr><td colSpan={3} className="muted">등록된 번역이 없습니다.</td></tr>}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

function TranslationForm({initialKey = '', initialMessage = '', onSave, onCancel}: {
    initialKey?: string; initialMessage?: string; onSave: (key: string, message: string) => Promise<unknown>; onCancel: () => void;
}) {
    const [key, setKey] = useState(initialKey);
    const [message, setMessage] = useState(initialMessage);
    const [error, setError] = useState<unknown>(null);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            await onSave(key.trim(), message);
        } catch (e) {
            setError(e);
        }
    };

    return (
        <form className="inline-form" onSubmit={submit}>
            <Field label="키" name="translation-key" required value={key} placeholder="nav.login"
                   onChange={(e) => setKey(e.target.value)}/>
            <Field label="번역" name="translation-message" required value={message}
                   onChange={(e) => setMessage(e.target.value)}/>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary">저장</button>
                <button type="button" onClick={onCancel}>취소</button>
            </div>
        </form>
    );
}
