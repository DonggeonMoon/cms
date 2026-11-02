import {request} from './client';
import type {LoginUser, UserType} from './types';

export const authApi = {
    me: () => request<LoginUser>('/api/auth/me'),
    login: (username: string, password: string, userType: UserType) =>
        request<LoginUser>('/api/auth/login', {method: 'POST', body: {username, password, userType}}),
    logout: () => request<void>('/api/auth/logout', {method: 'POST'}),
};
