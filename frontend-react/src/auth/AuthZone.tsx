import {Outlet} from 'react-router';
import {type ApiZone, setApiZone} from '../api/client';
import {AuthProvider} from './AuthContext';

export function AuthZone({zone}: { zone: ApiZone }) {
    setApiZone(zone);
    return (
        <AuthProvider key={zone}>
            <Outlet/>
        </AuthProvider>
    );
}
