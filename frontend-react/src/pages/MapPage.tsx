import {useEffect, useState} from 'react';
import type {Branch, Office, PublicMap} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {ErrorBanner} from '../components/ErrorBanner';
import {externalLinks, type LatLng} from '../components/map/mapSdk';
import {MapView} from '../components/map/MapView';
import {useFeatureMenuName} from '../components/SiteContext';

type Tab = 'directions' | 'branches';

export function MapPage() {
    const title = useFeatureMenuName('MAP', '지도');
    const [map, setMap] = useState<PublicMap | null>(null);
    const [tab, setTab] = useState<Tab | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        siteApi.map().then((loaded) => {
            setMap(loaded);
            setTab(loaded.directions ? 'directions' : loaded.branches ? 'branches' : null);
        }).catch(setError);
    }, []);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!map) {
        return <p className="muted">불러오는 중…</p>;
    }

    const tabs: { value: Tab; label: string }[] = [
        ...(map.directions ? [{value: 'directions' as const, label: '오시는 길'}] : []),
        ...(map.branches ? [{value: 'branches' as const, label: '지점 안내'}] : []),
    ];

    return (
        <section className="card">
            <h1>{title}</h1>
            {tabs.length === 0 && <p className="muted">준비 중입니다.</p>}
            {tabs.length > 1 && (
                <div className="tabs" role="tablist" aria-label="지도 안내">
                    {tabs.map((t) => (
                        <button key={t.value} type="button" role="tab" aria-selected={tab === t.value}
                                className={tab === t.value ? 'tab active' : 'tab'} onClick={() => setTab(t.value)}>
                            {t.label}
                        </button>
                    ))}
                </div>
            )}
            {tab === 'directions' && map.directions && <Directions map={map} office={map.directions}/>}
            {tab === 'branches' && map.branches && <Branches map={map} branches={map.branches}/>}
        </section>
    );
}

function Directions({map, office}: { map: PublicMap; office: Office }) {
    const place = {
        name: office.name || '본점',
        address: office.address,
        latitude: Number(office.latitude),
        longitude: Number(office.longitude),
    };
    return (
        <>
            {tabsTitle('오시는 길')}
            <MapView provider={map.provider} apiKey={map.apiKey} places={[place]}/>
            <dl className="meta map-info">
                <dt>주소</dt>
                <dd>{[office.address, office.detailAddress].filter(Boolean).join(' ')}</dd>
                {office.phoneNumber && (<><dt>연락처</dt><dd><a href={`tel:${office.phoneNumber}`}>{office.phoneNumber}</a></dd></>)}
                {office.businessHours && (<><dt>운영 시간</dt><dd>{office.businessHours}</dd></>)}
            </dl>
            {office.description && <p className="pre-line">{office.description}</p>}
            <div className="toolbar">
                {externalLinks(map.provider, place).map((link) => (
                    <a key={link.href} className="button" href={link.href} target="_blank" rel="noreferrer">{link.label}</a>
                ))}
            </div>
        </>
    );
}

function Branches({map, branches}: { map: PublicMap; branches: Branch[] }) {
    const [focus, setFocus] = useState<LatLng | null>(null);
    if (branches.length === 0) {
        return <p className="muted">등록된 지점이 없습니다.</p>;
    }
    const places = branches.map((b) => ({name: b.name, address: b.address, latitude: Number(b.latitude), longitude: Number(b.longitude)}));
    return (
        <>
            {tabsTitle('지점 안내')}
            <MapView provider={map.provider} apiKey={map.apiKey} places={places} focus={focus}/>
            <ul className="branch-list">
                {branches.map((branch, index) => (
                    <li key={branch.id}>
                        <button type="button" className="link-button" onClick={() => setFocus({lat: places[index].latitude, lng: places[index].longitude})}>
                            <strong>{branch.name}</strong>
                        </button>
                        <div className="muted small">{[branch.address, branch.detailAddress].filter(Boolean).join(' ')}</div>
                        {branch.phoneNumber && <div className="small"><a href={`tel:${branch.phoneNumber}`}>{branch.phoneNumber}</a></div>}
                        {branch.businessHours && <div className="small">{branch.businessHours}</div>}
                        {branch.description && <div className="small pre-line">{branch.description}</div>}
                        <div className="small">
                            {externalLinks(map.provider, places[index]).map((link) => (
                                <a key={link.href} href={link.href} target="_blank" rel="noreferrer"> {link.label}</a>
                            ))}
                        </div>
                    </li>
                ))}
            </ul>
        </>
    );
}

function tabsTitle(title: string) {
    return <h2 className="visually-hidden">{title}</h2>;
}
