import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {Banner} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {dismissForDays, dismissLabel, isDismissed} from './dismissal';

const STORAGE_KEY = 'cms.dismissedTopBars';

// the "배너": a strip above the site header; the first active one (by order) that was not dismissed today is shown
export function TopBar() {
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
        <div className="top-bar" role="region" aria-label="공지 배너"
             style={{background: banner.backgroundColor ?? undefined, color: banner.textColor ?? undefined}}>
            <div className="top-bar-message">{message}</div>
            <div className="top-bar-actions">
                {banner.dismissDays > 0 && (
                    <button type="button" className="link-button" style={{color: 'inherit'}} onClick={() => {
                        dismissForDays(STORAGE_KEY, banner.id, banner.dismissDays);
                        setBanner(null);
                    }}>{dismissLabel(banner.dismissDays)}</button>
                )}
                <button type="button" className="top-bar-close" aria-label="배너 닫기" style={{color: 'inherit'}}
                        onClick={() => setBanner(null)}>×</button>
            </div>
        </div>
    );
}
