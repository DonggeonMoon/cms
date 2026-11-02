import {Outlet} from 'react-router';
import {type ApiZone, setApiZone} from '../api/client';
import {AuthProvider} from './AuthContext';

// each zone has its own login: set the zone before any child renders so their first requests already carry it
export function AuthZone({zone}: { zone: ApiZone }) {
    setApiZone(zone);
    return (
        <AuthProvider key={zone}>
            <Outlet/>
        </AuthProvider>
    );
}
