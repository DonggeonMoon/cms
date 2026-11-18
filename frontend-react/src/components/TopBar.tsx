import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Banner} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {dismissForDays, dismissLabel, isDismissed} from './dismissal';
import {useI18n} from '../api/i18n';

const STORAGE_KEY = 'cms.dismissedTopBars';

export function TopBar() {
    const {t} = useI18n();
    const [banner, setBanner] = useState<Banner | null>(null);

    useEffect(() => {
        siteApi.banners('BANNER')
            .then((list) => setBanner((Array.isArray(list) ? list : []).find((b) => !isDismissed(STORAGE_KEY, b.id)) ?? null))
            .catch(() => setBanner(null));
    }, []);

    if (!banner) {
        return null;
    }
    const text = banner.description || banner.name;
    const isInternal = banner.link?.startsWith('/') && !banner.opensNewWindow;
    const message = !banner.link
        ? <span>{text}</span>
        : isInternal
            ? <Link to={banner.link!}>{text}</Link>
            : <a href={banner.link!} target={banner.opensNewWindow ? '_blank' : undefined} rel="noreferrer">{text}</a>;

    return (
        <div className="top-bar" role="region" aria-label={t('shop.topBar.label', '공지 배너')}
             style={{background: banner.backgroundColor ?? undefined, color: banner.textColor ?? undefined}}>
            <div className="top-bar-message">{message}</div>
            <div className="top-bar-actions">
                {banner.dismissDays > 0 && (
                    <button type="button" className="link-button" style={{color: 'inherit'}} onClick={() => {
                        dismissForDays(STORAGE_KEY, banner.id, banner.dismissDays);
                        setBanner(null);
                    }}>{dismissLabel(banner.dismissDays, t)}</button>
                )}
                <button type="button" className="top-bar-close" aria-label={t('shop.topBar.close', '배너 닫기')} style={{color: 'inherit'}}
                        onClick={() => setBanner(null)}>×</button>
            </div>
        </div>
    );
}
