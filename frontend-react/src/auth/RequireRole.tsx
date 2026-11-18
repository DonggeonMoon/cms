import {Navigate, Outlet, useLocation} from 'react-router';
import type {UserType} from '../api/types';
import {useAuth} from './AuthContext';
import {useI18n} from '../api/i18n';

export function RequireRole({role}: { role: UserType }) {
    const {t} = useI18n();
    const {user, loading} = useAuth();
    const location = useLocation();

    if (loading) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    if (!user || user.userType !== role) {
        const loginPath = role === 'ADMIN' ? '/admin/login' : '/login';
        return <Navigate to={loginPath} replace state={{from: location.pathname}}/>;
    }
    return <Outlet/>;
}
