import type {MapProvider} from '../../api/domain';

/* eslint-disable @typescript-eslint/no-explicit-any -- the Kakao/Naver map SDKs ship without types */
declare global {
    interface Window {
        kakao?: any;
        naver?: any;
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
            : loadScript(`https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(apiKey)}&submodules=geocoder`);
        promise.catch(() => loading.delete(cacheKey));
        loading.set(cacheKey, promise);
    }
    return promise;
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
    const query = encodeURIComponent(place.address || place.name);
    return [{label: '네이버 지도에서 보기', href: `https://map.naver.com/p/search/${query}`}];
}
