import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {authApi} from '../api/auth';
import {ApiError} from '../api/client';
import type {LoginUser, UserType} from '../api/types';

interface AuthState {
    user: LoginUser | null;
    loading: boolean;
    login: (username: string, password: string, userType: UserType) => Promise<LoginUser>;
    logout: () => Promise<void>;
    clear: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({children}: { children: ReactNode }) {
    const [user, setUser] = useState<LoginUser | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        authApi.me()
            .then(setUser)
            .catch((error: unknown) => {
                if (!(error instanceof ApiError && error.status === 401)) {
                    console.error(error);
                }
                setUser(null);
            })
            .finally(() => setLoading(false));
    }, []);

    const login = useCallback(async (username: string, password: string, userType: UserType) => {
        const loggedIn = await authApi.login(username, password, userType);
        setUser(loggedIn);
        return loggedIn;
    }, []);

    const logout = useCallback(async () => {
        await authApi.logout();
        setUser(null);
    }, []);

    const clear = useCallback(() => setUser(null), []);

    const value = useMemo(() => ({user, loading, login, logout, clear}), [user, loading, login, logout, clear]);
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used inside <AuthProvider>');
    }
    return context;
}
