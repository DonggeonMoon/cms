import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {ChatbotWidget} from './ChatbotWidget';

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('ChatbotWidget', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('stays hidden while the chatbot is off', async () => {
        const fetchMock = vi.fn(async () => respond(200, {enabled: false, welcomeMessage: null}));
        vi.stubGlobal('fetch', fetchMock);
        render(<ChatbotWidget/>);
        await vi.waitFor(() => expect(fetchMock).toHaveBeenCalled());
        expect(screen.queryByRole('button', {name: /상담/})).not.toBeInTheDocument();
    });

    it('sends the conversation and shows the reply', async () => {
        const sent: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/chatbot/messages') {
                sent.push(JSON.parse(String(init?.body)));
                return respond(200, {reply: '회원가입 메뉴에서 가입하세요.'});
            }
            if (url === '/api/chatbot') {
                return respond(200, {enabled: true, welcomeMessage: '반가워요'});
            }
            return respond(204);
        }));
        render(<ChatbotWidget/>);

        await userEvent.click(await screen.findByRole('button', {name: /상담/}));
        expect(screen.getByText('반가워요')).toBeInTheDocument();
        await userEvent.type(screen.getByLabelText('챗봇에게 보낼 메시지'), '가입 방법?');
        await userEvent.click(screen.getByRole('button', {name: '보내기'}));

        expect(await screen.findByText('회원가입 메뉴에서 가입하세요.')).toBeInTheDocument();
        expect(sent[0]).toEqual({messages: [{role: 'USER', content: '가입 방법?'}]});
    });
});
