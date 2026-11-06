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
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return (
            <div role="alert" className="error-banner">
                {error.fieldErrors.length === 1 ? error.fieldErrors[0].message : (
                    <ul className="error-banner-list">
                        {error.fieldErrors.map((fieldError) => <li key={fieldError.field}>{fieldError.message}</li>)}
                    </ul>
                )}
            </div>
        );
    }
    return <p role="alert" className="error-banner">{errorMessage(error)}</p>;
}
