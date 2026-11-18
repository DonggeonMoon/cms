import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {AdminNotification, MarketingCampaign, NotificationChannel, NotificationTypeSetting} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {gradeNameOf, useGrades} from '../../api/grades';
import type {Page} from '../../api/types';
import {Checkbox} from '../../components/Field';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

const CHANNELS: NotificationChannel[] = ['IN_APP', 'EMAIL', 'SMS'];
type T = (key: string, fallback: string, values?: Record<string, string>) => string;
const channelLabel = (t: T): Record<NotificationChannel, string> => ({
    IN_APP: t('admin.marketing.channel.inApp', '앱 내'), EMAIL: t('admin.marketing.channel.email', '이메일'), SMS: 'SMS',
});

function AutoNotificationSettings() {
    const {t} = useI18n();
    const [settings, setSettings] = useState<NotificationTypeSetting[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        adminApi.notificationSettings.list().then(setSettings).catch(setError);
    }, []);

    const toggle = async (type: NotificationTypeSetting['type'], enabled: boolean) => {
        if (!settings) {
            return;
        }
        setSettings(settings.map((s) => (s.type === type ? {...s, enabled} : s)));
        setSaving(true);
        setError(null);
        try {
            setSettings(await adminApi.notificationSettings.save({[type]: enabled}));
        } catch (e) {
            setError(e);
        } finally {
            setSaving(false);
        }
    };

    return (
        <section>
            <h2>{t('admin.marketing.autoSettingsTitle', '거래 알림 사용 설정')}</h2>
            <p className="muted small">{t('admin.marketing.autoSettingsHelp', '가입환영·결제완료·환불처리 알림을 각각 켜고 끌 수 있습니다. 꺼두면 해당 이벤트가 생겨도 알림을 보내지 않습니다.')}</p>
            <ErrorBanner error={error}/>
            {settings ? (
                <div className="schema-grid">
                    {settings.map((s) => (
                        <div key={s.type} className="inline-check">
                            <Checkbox label={s.label} name={`notification-${s.type}`} checked={s.enabled} disabled={saving}
                                      onChange={(e) => toggle(s.type, e.target.checked)}/>
                        </div>
                    ))}
                </div>
            ) : <p className="muted">{t('common.loading', '불러오는 중…')}</p>}
        </section>
    );
}

type LogTab = 'history' | 'log';

export function MarketingAdminPage() {
    const {t} = useI18n();
    const grades = useGrades();
    const [logTab, setLogTab] = useState<LogTab>('history');
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [targetGradeId, setTargetGradeId] = useState('');
    const [channels, setChannels] = useState<Set<NotificationChannel>>(new Set(['IN_APP']));
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MarketingCampaign> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);
    const [sent, setSent] = useState<MarketingCampaign | null>(null);

    const [logPage, setLogPage] = useState(0);
    const [log, setLog] = useState<Page<AdminNotification> | null>(null);
    const [logError, setLogError] = useState<unknown>(null);

    const reload = useCallback(() => {
        adminApi.marketing.history(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    const reloadLog = useCallback(() => {
        adminApi.notifications.list(logPage).then(setLog).catch(setLogError);
    }, [logPage]);
    useEffect(reloadLog, [reloadLog]);

    const toggleChannel = (channel: NotificationChannel) => setChannels((current) => {
        const next = new Set(current);
        if (next.has(channel)) {
            next.delete(channel);
        } else {
            next.add(channel);
        }
        return next;
    });

    const send = async (e: FormEvent) => {
        e.preventDefault();
        if (channels.size === 0) {
            return;
        }
        if (!window.confirm(t('admin.marketing.confirmSend', '대상 회원에게 바로 발송합니다. 계속할까요?'))) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            setSent(await adminApi.marketing.send(title, content, targetGradeId ? Number(targetGradeId) : null, [...channels]));
            setTitle('');
            setContent('');
            setTargetGradeId('');
            setPage(0);
            reload();
            setLogPage(0);
            reloadLog();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.nav.marketing', '알림')}</h1>
            <p className="muted">{t('admin.marketing.description', '결제완료·환불·가입환영 같은 거래 알림은 이벤트가 생기면 자동으로 간다. 아래 발송은 등급으로 대상을 좁혀 바로 보내는 마케팅성 알림이고, 마케팅 활용 동의가 없는 회원에게는 가지 않는다.')}</p>
            <AutoNotificationSettings/>
            <h2>{t('admin.marketing.targetSend', '대상 발송')}</h2>
            <ErrorBanner error={error}/>
            <form onSubmit={send} className="inline-form">
                <div className="field">
                    <span>{t('admin.marketing.channels', '발송 채널')}</span>
                    {CHANNELS.map((channel) => (
                        <label key={channel} className="checkbox">
                            <input type="checkbox" checked={channels.has(channel)} onChange={() => toggleChannel(channel)}/>
                            {channelLabel(t)[channel]}
                        </label>
                    ))}
                </div>
                <div className="field">
                    <label htmlFor="campaign-title">{t('admin.content.field.title', '제목')}</label>
                    <input id="campaign-title" value={title} onChange={(e) => setTitle(e.target.value)} required/>
                </div>
                <div className="field">
                    <label htmlFor="campaign-content">{t('admin.content.field.contentHtml', '내용')}</label>
                    <textarea id="campaign-content" rows={4} value={content} onChange={(e) => setContent(e.target.value)} required/>
                </div>
                <div className="field">
                    <label htmlFor="campaign-grade">{t('admin.marketing.targetGrade', '대상 등급')}</label>
                    <select id="campaign-grade" value={targetGradeId} onChange={(e) => setTargetGradeId(e.target.value)}>
                        <option value="">{t('admin.marketing.allMembers', '전체 회원')}</option>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                    </select>
                </div>
                <button type="submit" className="primary" disabled={busy}>{t('admin.marketing.sendAction', '발송하기')}</button>
                {sent && <p className="ok">{t('admin.marketing.sentNotice', '{count}명에게 발송했습니다. (마케팅 활용 동의한 대상자만 집계)', {count: String(sent.recipientCount)})}</p>}
            </form>

            <div className="tabs" role="tablist" aria-label={t('admin.marketing.historyTabsLabel', '알림 기록')}>
                {([['history', t('admin.marketing.tab.history', '발송 이력')], ['log', t('admin.marketing.tab.log', '전체 알림 로그')]] as const).map(([value, label]) => (
                    <button key={value} type="button" role="tab" aria-selected={logTab === value}
                            className={logTab === value ? 'tab active' : 'tab'} onClick={() => setLogTab(value)}>
                        {label}
                    </button>
                ))}
            </div>

            {logTab === 'history' && (
                <>
                    <div className="table-wrap">
                        <table>
                            <thead><tr>
                                <th>{t('admin.content.field.title', '제목')}</th><th>{t('admin.marketing.target', '대상')}</th>
                                <th>{t('admin.marketing.channel', '채널')}</th><th>{t('admin.marketing.recipientCount', '발송 수')}</th>
                                <th>{t('admin.marketing.sentAt', '발송일')}</th>
                            </tr></thead>
                            <tbody>
                            {data?.content.map((campaign) => (
                                <tr key={campaign.id}>
                                    <td>{campaign.title}</td>
                                    <td>{campaign.targetGradeId ? t('admin.marketing.gradeTarget', '{grade} 등급', {grade: gradeNameOf(grades, campaign.targetGradeId) ?? ''}) : t('admin.marketing.allMembers', '전체 회원')}</td>
                                    <td>{campaign.channels.map((c) => channelLabel(t)[c]).join(', ')}</td>
                                    <td>{t('admin.marketing.peopleCount', '{count}명', {count: String(campaign.recipientCount)})}</td>
                                    <td>{formatDateTime(campaign.sentAt)}</td>
                                </tr>
                            ))}
                            {data?.content.length === 0 && <tr><td colSpan={5} className="muted">{t('admin.marketing.noHistory', '발송 이력이 없습니다.')}</td></tr>}
                            </tbody>
                        </table>
                    </div>
                    {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
                </>
            )}

            {logTab === 'log' && (
                <>
                    <p className="muted">{t('admin.marketing.logDescription', '거래 알림(결제완료·환불·가입환영 등)과 마케팅성 알림을 모두 포함한 발송 기록이다.')}</p>
                    <ErrorBanner error={logError}/>
                    <div className="table-wrap">
                        <table>
                            <thead><tr>
                                <th>{t('admin.course.member', '회원')}</th><th>{t('admin.marketing.channel', '채널')}</th>
                                <th>{t('admin.content.field.title', '제목')}</th><th>{t('admin.content.field.contentHtml', '내용')}</th>
                                <th>{t('admin.marketing.read', '읽음')}</th><th>{t('admin.marketing.sentAt', '발송일')}</th>
                            </tr></thead>
                            <tbody>
                            {log?.content.map((n) => (
                                <tr key={n.id}>
                                    <td>{n.memberName ?? `#${n.memberId}`}</td>
                                    <td>{channelLabel(t)[n.channel]}</td>
                                    <td>{n.title}</td>
                                    <td>{n.content}</td>
                                    <td>{n.channel === 'IN_APP' ? (n.read ? t('admin.marketing.readYes', '읽음') : t('admin.marketing.readNo', '안 읽음')) : '-'}</td>
                                    <td>{formatDateTime(n.createdAt)}</td>
                                </tr>
                            ))}
                            {log?.content.length === 0 && <tr><td colSpan={6} className="muted">{t('admin.marketing.noLog', '알림 기록이 없습니다.')}</td></tr>}
                            </tbody>
                        </table>
                    </div>
                    {log && <Pagination page={logPage} totalPages={log.page.totalPages} onChange={setLogPage}/>}
                </>
            )}
        </section>
    );
}
