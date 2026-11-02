import {Navigate, Outlet, useLocation} from 'react-router';
import type {UserType} from '../api/types';
import {useAuth} from './AuthContext';

export function RequireRole({role}: { role: UserType }) {
    const {user, loading} = useAuth();
    const location = useLocation();

    if (loading) {
        return <p className="muted">불러오는 중…</p>;
    }
    if (!user || user.userType !== role) {
        const loginPath = role === 'ADMIN' ? '/admin/login' : '/login';
        return <Navigate to={loginPath} replace state={{from: location.pathname}}/>;
    }
    return <Outlet/>;
}
