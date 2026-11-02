import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it, vi} from 'vitest';
import type {Menu} from '../api/domain';
import type {Page} from '../api/types';
import {AdminCrudPage} from './AdminCrudPage';
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
    const base: Menu = {id: 7, parentId: null, name: 'm', type: 'LINK', depth: 0, sequence: 0, link: null, targetId: null, feature: null, isUsing: true, children: []};

    it('maps each menu type to its route', () => {
        expect(menuHref({...base, type: 'CONTENT', targetId: 3})).toBe('/contents/3?menuId=7');
        expect(menuHref({...base, type: 'BOARD', targetId: 2})).toBe('/boards/2');
        expect(menuHref({...base, link: 'https://example.com'})).toBe('https://example.com');
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
});
