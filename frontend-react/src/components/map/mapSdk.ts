import type {MapProvider} from '../../api/domain';

/* eslint-disable @typescript-eslint/no-explicit-any -- the map SDKs are loaded at runtime without types */
declare global {
    interface Window {
        kakao?: any;
        naver?: any;
        google?: any;
        __cmsGoogleMapsReady?: () => void;
    }
}

export interface LatLng {
    lat: number;
    lng: number;
}

const loading = new Map<string, Promise<void>>();

function loadScript(src: string): Promise<void> {
    return new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = src;
        script.async = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error(`지도 스크립트를 불러오지 못했습니다: ${src}`));
        document.head.appendChild(script);
    });
}

export function loadMapSdk(provider: MapProvider, apiKey: string): Promise<void> {
    const cacheKey = `${provider}:${apiKey}`;
    let promise = loading.get(cacheKey);
    if (!promise) {
        promise = provider === 'KAKAO'
            ? loadScript(`https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(apiKey)}&autoload=false&libraries=services`)
                .then(() => new Promise<void>((resolve) => window.kakao.maps.load(() => resolve())))
            : provider === 'NAVER'
                ? loadScript(`https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(apiKey)}&submodules=geocoder`)
                : loadGoogleMaps(apiKey);
        promise.catch(() => loading.delete(cacheKey));
        loading.set(cacheKey, promise);
    }
    return promise;
}

function loadGoogleMaps(apiKey: string): Promise<void> {
    return new Promise((resolve, reject) => {
        window.__cmsGoogleMapsReady = () => resolve();
        loadScript(`https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&loading=async&callback=__cmsGoogleMapsReady`)
            .catch(reject);
    });
}

export async function geocode(provider: MapProvider, apiKey: string, address: string): Promise<LatLng | null> {
    await loadMapSdk(provider, apiKey);
    if (provider === 'KAKAO') {
        const kakao = window.kakao;
        return new Promise((resolve) => {
            new kakao.maps.services.Geocoder().addressSearch(address, (result: any[], status: string) => {
                resolve(status === kakao.maps.services.Status.OK && result[0]
                    ? {lat: Number(result[0].y), lng: Number(result[0].x)}
                    : null);
            });
        });
    }
    if (provider === 'GOOGLE') {
        const google = window.google;
        return new Promise((resolve) => {
            new google.maps.Geocoder().geocode({address}, (results: any[] | null, status: string) => {
                const location = status === 'OK' ? results?.[0]?.geometry?.location : null;
                resolve(location ? {lat: location.lat(), lng: location.lng()} : null);
            });
        });
    }
    const naver = window.naver;
    return new Promise((resolve) => {
        naver.maps.Service.geocode({query: address}, (status: number, response: any) => {
            const found = status === naver.maps.Service.Status.OK ? response?.v2?.addresses?.[0] : null;
            resolve(found ? {lat: Number(found.y), lng: Number(found.x)} : null);
        });
    });
}

export interface Place {
    name: string;
    address?: string | null;
    latitude: number;
    longitude: number;
}

export function externalLinks(provider: MapProvider, place: Place): { label: string; href: string }[] {
    const name = encodeURIComponent(place.name);
    if (provider === 'KAKAO') {
        return [
            {label: '카카오맵에서 보기', href: `https://map.kakao.com/link/map/${name},${place.latitude},${place.longitude}`},
            {label: '카카오맵 길찾기', href: `https://map.kakao.com/link/to/${name},${place.latitude},${place.longitude}`},
        ];
    }
    if (provider === 'GOOGLE') {
        const position = `${place.latitude},${place.longitude}`;
        return [
            {label: '구글 지도에서 보기', href: `https://www.google.com/maps/search/?api=1&query=${position}`},
            {label: '구글 지도 길찾기', href: `https://www.google.com/maps/dir/?api=1&destination=${position}`},
        ];
    }
    const query = encodeURIComponent(place.address || place.name);
    return [{label: '네이버 지도에서 보기', href: `https://map.naver.com/p/search/${query}`}];
}
