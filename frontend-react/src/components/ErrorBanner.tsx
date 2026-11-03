import {ApiError} from '../api/client';

export function errorMessage(error: unknown): string {
    if (error instanceof ApiError || error instanceof Error) {
        return error.message;
    }
    return '알 수 없는 오류가 발생했습니다.';
}

export function ErrorBanner({error}: { error: unknown }) {
    if (!error) {
        return null;
    }
    return <p role="alert" className="error-banner">{errorMessage(error)}</p>;
}
