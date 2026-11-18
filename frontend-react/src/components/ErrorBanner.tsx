import {ApiError, CONNECTION_ERROR_KEYS, CONNECTION_MESSAGES, isConnectionError} from '../api/client';
import {useI18n} from '../api/i18n';

export function errorMessage(error: unknown, t?: (key: string, fallback: string) => string): string {
    if (error instanceof ApiError && isConnectionError(error)) {
        const code = error.code as keyof typeof CONNECTION_ERROR_KEYS;
        return t ? t(CONNECTION_ERROR_KEYS[code], CONNECTION_MESSAGES[code]) : error.message;
    }
    if (error instanceof ApiError || error instanceof Error) {
        return error.message;
    }
    return t ? t('common.unknownError', '알 수 없는 오류가 발생했습니다.') : '알 수 없는 오류가 발생했습니다.';
}

export function ErrorBanner({error}: { error: unknown }) {
    const {t} = useI18n();
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
    return <p role="alert" className="error-banner">{errorMessage(error, t)}</p>;
}
