import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import type {Menu} from '../api/domain';
import type {Page} from '../api/types';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';
import {AdminCrudPage} from './AdminCrudPage';
import {CommentEditor} from './CommentEditor';
import {Pagination} from './Pagination';
import {normalizeValues} from './SchemaForm';
import {menuHref} from './SiteContext';

describe('normalizeValues', () => {
    it('turns empty optional inputs into null and pads datetimes with seconds', () => {
        const result = normalizeValues(
            [
                {name: 'count', label: '', type: 'number'},
                {name: 'stock', label: '', type: 'number'},
                {name: 'start', label: '', type: 'datetime'},
                {name: 'end', label: '', type: 'datetime'},
                {name: 'type', label: '', type: 'select'},
            ],
            {count: '', stock: '12', start: '2026-11-02T09:30', end: '', type: '', extra: 'kept'},
        );
        expect(result).toEqual({count: null, stock: 12, start: '2026-11-02T09:30:00', end: null, type: null, extra: 'kept'});
    });
});

describe('menuHref', () => {
    const base: Menu = {id: 7, parentId: null, name: 'm', type: 'LINK', depth: 0, sequence: 1, link: null, targetId: null, feature: null, isUsing: true, children: []};

    it('maps each menu type to its route', () => {
        expect(menuHref({...base, type: 'CONTENT', targetId: 3})).toBe('/contents/3?menuId=7');
        expect(menuHref({...base, type: 'BOARD', targetId: 2})).toBe('/boards/2');
        expect(menuHref({...base, link: 'https://example.com'})).toBe('https://example.com');
    });
});

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

describe('main navigation', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('sends a feature menu with its own link override there instead of the feature\'s default path', async () => {
        const menus: Menu[] = [
            {id: 20, parentId: null, name: '시리즈', type: 'FEATURE', depth: 0, sequence: 1, link: '/series', targetId: null, feature: 'MEDIA', isUsing: true, children: []},
            {id: 21, parentId: null, name: '스트리밍', type: 'FEATURE', depth: 0, sequence: 2, link: null, targetId: null, feature: 'MEDIA', isUsing: true, children: []},
        ];
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            if (url === '/api/auth/me') return respond(401);
            if (url === '/api/site') return respond(200, {id: 1, name: '사이트', features: {}});
            if (url === '/api/menus') return respond(200, menus);
            if (url === '/api/configs') return respond(200, {});
            return url.startsWith('/api/banners') || url.startsWith('/api/boards') || url.startsWith('/api/archives')
                ? respond(200, []) : respond(204);
        }));
        render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: ['/']})}/></AuthProvider>);

        // 자체 link가 있는 메뉴는 그 링크로, 없는 메뉴는 기능의 기본 경로(FEATURE_PATHS)로 간다.
        expect(await screen.findByRole('link', {name: '시리즈'})).toHaveAttribute('href', '/series');
        expect(screen.getByRole('link', {name: '스트리밍'})).toHaveAttribute('href', '/streaming');
    });
});

interface Row {
    id: number;
    name: string;
}

function page(rows: Row[]): Page<Row> {
    return {content: rows, page: {size: 20, number: 0, totalElements: rows.length, totalPages: 1}};
}

describe('AdminCrudPage', () => {
    it('lists rows and creates a new one through the schema form', async () => {
        let rows: Row[] = [{id: 1, name: '첫 번째'}];
        const load = vi.fn(async () => page(rows));
        const create = vi.fn(async (values: Record<string, unknown>) => {
            rows = [...rows, {id: 2, name: String(values.name)}];
        });

        render(
            <AdminCrudPage<Row>
                title="테스트"
                load={load}
                columns={[{label: '이름', render: (row) => row.name}]}
                fields={[{name: 'name', label: '이름 입력', required: true}]}
                create={create}
            />,
        );

        expect(await screen.findByText('첫 번째')).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '새로 등록'}));
        await userEvent.type(screen.getByLabelText(/이름 입력/), '두 번째');
        await userEvent.click(screen.getByRole('button', {name: '저장'}));

        expect(create).toHaveBeenCalledWith({name: '두 번째'});
        expect(await screen.findByText('두 번째')).toBeInTheDocument();
        await waitFor(() => expect(load).toHaveBeenCalledTimes(2));
    });

    it('edits a row inline instead of showing the form at the top', async () => {
        const rows: Row[] = [{id: 1, name: '첫 번째'}, {id: 2, name: '두 번째'}];
        const load = vi.fn(async () => page(rows));
        const update = vi.fn(async () => {});

        render(
            <AdminCrudPage<Row>
                title="테스트"
                load={load}
                columns={[{label: '이름', render: (row) => row.name}]}
                fields={[{name: 'name', label: '이름 입력', required: true}]}
                update={update}
            />,
        );

        await screen.findByText('두 번째');
        const editButtons = screen.getAllByRole('button', {name: '수정'});
        await userEvent.click(editButtons[1]);

        const input = screen.getByLabelText(/이름 입력/) as HTMLInputElement;
        expect(input.closest('tr')?.previousElementSibling?.textContent).toContain('두 번째');
        expect(input.value).toBe('두 번째');

        await userEvent.clear(input);
        await userEvent.type(input, '수정됨');
        await userEvent.click(screen.getByRole('button', {name: '저장'}));

        expect(update).toHaveBeenCalledWith(rows[1], {id: 2, name: '수정됨'});
    });
});

describe('Pagination', () => {
    it('shows the ten pages of the current block and jumps between blocks', async () => {
        const onChange = vi.fn();
        render(<Pagination page={12} totalPages={25} onChange={onChange}/>);

        expect(screen.getByRole('button', {name: '13'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('button', {name: '11'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: '20'})).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: '10'})).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: '21'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: '이전 10페이지'}));
        expect(onChange).toHaveBeenLastCalledWith(9);
        await userEvent.click(screen.getByRole('button', {name: '다음 10페이지'}));
        expect(onChange).toHaveBeenLastCalledWith(20);
        await userEvent.click(screen.getByRole('button', {name: '15'}));
        expect(onChange).toHaveBeenLastCalledWith(14);
    });

    it('hides the arrows when every page fits in one block', () => {
        render(<Pagination page={0} totalPages={3} onChange={vi.fn()}/>);

        expect(screen.getAllByRole('button')).toHaveLength(3);
    });
});

describe('CommentEditor', () => {
    it('saves the edited text with the guest password', async () => {
        const onSave = vi.fn(async () => undefined);
        render(<CommentEditor initial="처음 댓글" needsPassword onSave={onSave} onCancel={() => undefined}/>);

        const box = screen.getByRole('textbox', {name: '댓글 수정 내용'});
        await userEvent.clear(box);
        await userEvent.type(box, '고친 댓글');
        await userEvent.type(screen.getByLabelText('댓글 비밀번호'), 'pw1234');
        await userEvent.click(screen.getByRole('button', {name: '저장'}));

        expect(onSave).toHaveBeenCalledWith('고친 댓글', 'pw1234');
    });
});
