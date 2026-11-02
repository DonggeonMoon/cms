import {useEffect, useRef, useState} from 'react';
import type {MapProvider} from '../../api/domain';
import {externalLinks, type LatLng, loadMapSdk, type Place} from './mapSdk';

interface Props {
    provider: MapProvider;
    apiKey: string | null;
    places: Place[];
    focus?: LatLng | null;
    height?: number;
    onPick?: (position: LatLng) => void;
    missingKeyMessage?: string;
}

const SEOUL_CITY_HALL: LatLng = {lat: 37.5666103, lng: 126.9783882};

export function MapView({provider, apiKey, places, focus, height = 380, onPick, missingKeyMessage}: Props) {
    const containerRef = useRef<HTMLDivElement>(null);
    const [state, setState] = useState<'loading' | 'ready' | 'failed'>('loading');
    const pickRef = useRef(onPick);
    pickRef.current = onPick;

    const placesKey = JSON.stringify(places.map((p) => [p.name, p.latitude, p.longitude]));
    const focusKey = focus ? `${focus.lat},${focus.lng}` : '';

    useEffect(() => {
        if (!apiKey) {
            return;
        }
        let cancelled = false;
        setState('loading');
        loadMapSdk(provider, apiKey)
            .then(() => {
                if (cancelled || !containerRef.current) {
                    return;
                }
                containerRef.current.innerHTML = '';
                draw(provider, containerRef.current, places, focus ?? null, (position) => pickRef.current?.(position));
                setState('ready');
            })
            .catch(() => !cancelled && setState('failed'));
        return () => {
            cancelled = true;
        };
        // places/focus are compared by value through their keys
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [provider, apiKey, placesKey, focusKey]);

    if (!apiKey || state === 'failed') {
        return (
            <div className="map-fallback" style={{minHeight: Math.min(height, 200)}}>
                <p className="muted">
                    {!apiKey
                        ? missingKeyMessage ?? '지도를 표시할 수 없습니다. 아래 링크로 위치를 확인해 주세요.'
                        : '지도를 불러오지 못했습니다. 지도 키와 등록된 도메인을 확인해 주세요.'}
                </p>
                <ul className="list">
                    {places.map((place) => (
                        <li key={`${place.name}-${place.latitude}`}>
                            <strong>{place.name}</strong> {place.address && <span className="muted small">{place.address}</span>}{' '}
                            {externalLinks(provider, place).map((link) => (
                                <a key={link.href} href={link.href} target="_blank" rel="noreferrer" className="small"> {link.label}</a>
                            ))}
                        </li>
                    ))}
                </ul>
            </div>
        );
    }
    return (
        <div className="map-view" style={{height}}>
            <div ref={containerRef} className="map-canvas" aria-label="지도" role="region"/>
            {state === 'loading' && <p className="map-loading muted">지도를 불러오는 중…</p>}
        </div>
    );
}

/* eslint-disable @typescript-eslint/no-explicit-any -- SDK objects are untyped */
function draw(provider: MapProvider, container: HTMLElement, places: Place[], focus: LatLng | null,
              onPick: (position: LatLng) => void) {
    const center = focus ?? (places[0] ? {lat: places[0].latitude, lng: places[0].longitude} : SEOUL_CITY_HALL);

    if (provider === 'KAKAO') {
        const kakao = window.kakao;
        const map = new kakao.maps.Map(container, {center: new kakao.maps.LatLng(center.lat, center.lng), level: 4});
        const bounds = new kakao.maps.LatLngBounds();
        places.forEach((place) => {
            const position = new kakao.maps.LatLng(place.latitude, place.longitude);
            const marker = new kakao.maps.Marker({map, position, title: place.name});
            const info = new kakao.maps.InfoWindow({content: `<div class="map-label">${escape(place.name)}</div>`});
            kakao.maps.event.addListener(marker, 'click', () => info.open(map, marker));
            bounds.extend(position);
        });
        if (places.length > 1 && !focus) {
            map.setBounds(bounds);
        }
        kakao.maps.event.addListener(map, 'click', (event: any) =>
            onPick({lat: event.latLng.getLat(), lng: event.latLng.getLng()}));
        return;
    }

    const naver = window.naver;
    const map = new naver.maps.Map(container, {center: new naver.maps.LatLng(center.lat, center.lng), zoom: 16});
    const bounds = new naver.maps.LatLngBounds();
    places.forEach((place) => {
        const position = new naver.maps.LatLng(place.latitude, place.longitude);
        const marker = new naver.maps.Marker({map, position, title: place.name});
        const info = new naver.maps.InfoWindow({content: `<div class="map-label">${escape(place.name)}</div>`});
        naver.maps.Event.addListener(marker, 'click', () => info.open(map, marker));
        bounds.extend(position);
    });
    if (places.length > 1 && !focus) {
        map.fitBounds(bounds);
    }
    naver.maps.Event.addListener(map, 'click', (event: any) => onPick({lat: event.coord.lat(), lng: event.coord.lng()}));
}

function escape(text: string) {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
