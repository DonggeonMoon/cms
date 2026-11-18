import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {AppNotification} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';
import {useI18n} from '../../api/i18n';

const channelLabel = (t: (key: string, fallback: string) => string): Record<AppNotification['channel'], string> =>
    ({IN_APP: t('shop.notification.inApp', '앱'), EMAIL: t('shop.notification.email', '이메일'), SMS: 'SMS'});

export function MyNotificationsPage() {
    const {t} = useI18n();
    const CHANNEL_LABEL = channelLabel(t);
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<AppNotification> | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = () => {
        shopApi.myNotifications(page).then(setData).catch(setError);
    };
    useEffect(reload, [page]);

    const open = async (item: AppNotification) => {
        if (!item.read) {
            await shopApi.markNotificationRead(item.id).catch(() => undefined);
            reload();
        }
    };

    return (
        <section className="card">
            <h1>{t('shop.notification.title', '알림')}</h1>
            <ErrorBanner error={error}/>
            {data?.content.map((item) => (
                <article key={item.id} className={item.read ? 'card' : 'card unread'} onClick={() => open(item)}>
                    <p className="muted small">{CHANNEL_LABEL[item.channel]} · {formatDateTime(item.createdAt)}{!item.read && t('shop.notification.unread', ' · 안 읽음')}</p>
                    <h2>{item.title}</h2>
                    <p className="pre-wrap">{item.content}</p>
                    {item.link && <Link to={item.link} onClick={(e) => e.stopPropagation()}>{t('shop.notification.goTo', '바로 가기')}</Link>}
                </article>
            ))}
            {data?.content.length === 0 && <p className="muted">{t('shop.notification.noNotifications', '받은 알림이 없습니다.')}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
