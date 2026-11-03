import {render, screen} from '@testing-library/react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {SocialLoginButtons} from './SocialLoginButtons';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('SocialLoginButtons', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('links each enabled provider to its authorize endpoint with the return path', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => respond(200, [
            {provider: 'KAKAO', path: 'kakao', label: '카카오'},
            {provider: 'APPLE', path: 'apple', label: '애플'},
        ])));
        render(<SocialLoginButtons redirect="/media?type=VIDEO"/>);

        expect(await screen.findByRole('link', {name: '카카오로 로그인'}))
            .toHaveAttribute('href', '/api/auth/oauth/kakao/authorize?redirect=%2Fmedia%3Ftype%3DVIDEO');
        expect(screen.getByRole('link', {name: '애플로 로그인'})).toHaveClass('social-apple');
    });

    it('shows nothing when no provider is enabled', async () => {
        const fetchMock = vi.fn(async () => respond(200, []));
        vi.stubGlobal('fetch', fetchMock);
        const {container} = render(<SocialLoginButtons redirect="/"/>);
        await vi.waitFor(() => expect(fetchMock).toHaveBeenCalled());
        expect(container).toBeEmptyDOMElement();
    });
});
