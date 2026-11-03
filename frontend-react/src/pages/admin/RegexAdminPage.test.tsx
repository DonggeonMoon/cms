import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {RegexAdminPage} from './RegexAdminPage';

const RULES = [
    {id: 1, name: '비밀번호', pattern: '^(?=.*[A-Za-z])(?=.*\\d).{8,64}$', message: '비밀번호 형식', hint: null,
        description: null, usedBy: ['비밀번호'], modifiedAt: null},
    {id: 2, name: '사업자등록번호', pattern: '^\\d{3}-\\d{2}-\\d{5}$', message: '형식 오류', hint: null,
        description: '추가한 규칙', usedBy: [], modifiedAt: null},
];

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('RegexAdminPage', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('lists regexes with where they are used and tests a value', async () => {
        const tested: unknown[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
            if (url === '/api/admin/regexes/test') {
                tested.push(JSON.parse(String(init?.body)));
                return respond(200, {valid: true, matches: true, error: null});
            }
            return url === '/api/admin/regexes' ? respond(200, RULES) : respond(204);
        }));
        render(<RouterProvider router={createMemoryRouter([{path: '/', element: <RegexAdminPage/>}])}/>);

        expect(await screen.findByText('사용 안 함')).toBeInTheDocument();
        expect(screen.getAllByText('비밀번호')).toHaveLength(2);
        expect(screen.queryByRole('button', {name: '삭제'})).not.toBeInTheDocument();

        await userEvent.click(screen.getAllByRole('button', {name: '테스트'})[1]);
        await userEvent.type(screen.getByLabelText('테스트할 값'), '123-45-67890');
        expect(await screen.findByText('일치')).toBeInTheDocument();
        expect(tested.at(-1)).toEqual({pattern: '^\\d{3}-\\d{2}-\\d{5}$', value: '123-45-67890'});
    });
});
