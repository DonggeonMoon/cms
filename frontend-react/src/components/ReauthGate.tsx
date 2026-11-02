import {type ReactNode, useCallback, useState} from 'react';
import {ApiError} from '../api/client';
import {adminApi} from '../api/endpoints';
import {ReauthForm} from './ReauthForm';

export type ReauthGuard = <T>(pending: Promise<T>) => Promise<T>;

const isReauthRequired = (error: unknown) => error instanceof ApiError && error.code === 'REAUTHENTICATION_REQUIRED';

export function ReauthGate({title, children}: { title: string; children: (guard: ReauthGuard) => ReactNode }) {
    const [verified, setVerified] = useState(false);
    const [expired, setExpired] = useState(false);

    const guard = useCallback<ReauthGuard>((pending) => pending.catch((error: unknown) => {
        if (isReauthRequired(error)) {
            setVerified(false);
            setExpired(true);
        }
        throw error;
    }), []);

    if (!verified) {
        return (
            <ReauthForm title={title} verify={adminApi.reauthenticate}
                        reason={expired ? '확인한 지 오래되어 다시 확인이 필요합니다. 비밀번호를 입력해 주세요.' : null}
                        onVerified={() => {
                            setExpired(false);
                            setVerified(true);
                        }}/>
        );
    }
    return children(guard);
}
