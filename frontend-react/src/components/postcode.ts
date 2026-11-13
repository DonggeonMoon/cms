/* eslint-disable @typescript-eslint/no-explicit-any -- the postcode SDK is loaded at runtime without types */
declare global {
    interface Window {
        daum?: any;
    }
}

export interface PostcodeResult {
    zonecode: string;
    address: string;
    roadAddress: string;
    jibunAddress: string;
}

const POSTCODE_SCRIPT = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
let loading: Promise<void> | null = null;

function loadPostcodeScript(): Promise<void> {
    if (window.daum?.Postcode) {
        return Promise.resolve();
    }
    if (!loading) {
        loading = new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = POSTCODE_SCRIPT;
            script.async = true;
            script.onload = () => resolve();
            script.onerror = () => reject(new Error('우편번호 검색 스크립트를 불러오지 못했습니다.'));
            document.head.appendChild(script);
        });
        loading.catch(() => {
            loading = null;
        });
    }
    return loading;
}

/** 다음(카카오) 우편번호 검색 팝업을 연다. 다른 서드파티로 바꾸려면 이 함수만 바꾸면 된다. */
export async function openPostcodeSearch(): Promise<PostcodeResult> {
    await loadPostcodeScript();
    return new Promise((resolve) => {
        new window.daum.Postcode({
            oncomplete: (data: PostcodeResult) => resolve(data),
        }).open();
    });
}
