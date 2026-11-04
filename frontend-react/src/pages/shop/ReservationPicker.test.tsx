import {fireEvent, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {useState} from 'react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import type {Program, SpaceLayout} from '../../api/domain';
import {LayoutEditor} from '../../components/LayoutEditor';
import {ReservationPicker} from './ReservationPicker';

const LAYOUT: SpaceLayout = {
    rows: 1, cols: 3, cells: [
        {key: 'r0c0', row: 0, col: 0, kind: 'SEAT', label: 'A1', price: null},
        {key: 'r0c1', row: 0, col: 1, kind: 'SEAT', label: 'A2', price: 20000},
        {key: 'r0c2', row: 0, col: 2, kind: 'SEAT', label: 'A3', price: null},
    ],
};

const EVENT = {
    id: 9, name: '재즈 공연', price: 10000, reservationType: 'EVENT', layout: LAYOUT, startDateTime: '2026-12-01T19:00:00',
    endDateTime: null, slotMinutes: null, openTime: null, closeTime: null,
} as unknown as Program;

describe('reservation picker', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('keeps taken seats disabled and prices the chosen seats', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => new Response(JSON.stringify({takenCells: ['r0c0'], slots: []}), {status: 200})));
        const router = createMemoryRouter([
            {path: '/', element: <ReservationPicker program={EVENT}/>},
            {path: '/reservations/new', element: <p>예약 정보 입력</p>},
        ]);
        render(<RouterProvider router={router}/>);

        expect(await screen.findByRole('gridcell', {name: /A1.*예약됨/})).toBeDisabled();
        expect(screen.getByRole('button', {name: '예약하기'})).toBeDisabled();
        await userEvent.click(screen.getByRole('gridcell', {name: /A2/}));
        await userEvent.click(screen.getByRole('gridcell', {name: /A3/}));
        expect(screen.getByText(/예상 금액 30,000원/)).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: '예약하기'}));
        expect(router.state.location.search).toBe('?programId=9&cells=r0c1%2Cr0c2');
    });
});

describe('layout editor', () => {
    it('paints cells with the chosen kind and a default label', async () => {
        function Host() {
            const [layout, setLayout] = useState<SpaceLayout>({rows: 2, cols: 2, cells: []});
            return <><LayoutEditor value={layout} onChange={setLayout}/><output>{JSON.stringify(layout.cells)}</output></>;
        }
        render(<Host/>);
        await userEvent.click(screen.getByRole('radio', {name: '객실'}));
        await userEvent.click(screen.getAllByRole('gridcell')[3]);
        expect(screen.getByText(/"kind":"ROOM","label":"202"/)).toBeInTheDocument();
        expect(screen.getByText('예약 가능한 칸 1개')).toBeInTheDocument();
    });

    it('prices item rentals by days and quantity with the deposit and stops at the remaining stock', async () => {
        const queries: string[] = [];
        vi.stubGlobal('fetch', vi.fn(async (url: string) => {
            queries.push(decodeURIComponent(url));
            return new Response(JSON.stringify({takenCells: [], slots: [], remainingItems: 2}), {status: 200});
        }));
        const tent = {
            id: 4, name: '텐트', price: 10000, reservationType: 'ITEM_RENTAL', layout: null, startDateTime: '2030-01-01T00:00:00',
            endDateTime: null, slotMinutes: null, openTime: null, closeTime: null, depositAmount: 50000, maxRentalDays: 5,
        } as unknown as Program;
        const router = createMemoryRouter([
            {path: '/', element: <ReservationPicker program={tent}/>},
            {path: '/reservations/new', element: <p>예약 정보 입력</p>},
        ]);
        render(<RouterProvider router={router}/>);

        const returnDay = await screen.findByLabelText(/반납일/);
        await userEvent.type(returnDay, '2030-01-04');
        expect(await screen.findByText(/이 기간에 2개를 빌릴 수 있습니다/)).toBeInTheDocument();
        expect(queries.at(-1)).toContain('from=2030-01-01T00:00:00&to=2030-01-04T00:00:00');
        expect(screen.getByText(/예상 금액 80,000원/)).toBeInTheDocument();

        const quantity = screen.getByLabelText(/수량/);
        fireEvent.change(quantity, {target: {value: '3'}});
        expect(screen.getByRole('button', {name: '예약하기'})).toBeDisabled();
        fireEvent.change(quantity, {target: {value: '2'}});
        expect(screen.getByText(/예상 금액 160,000원/)).toBeInTheDocument();
        await userEvent.click(screen.getByRole('button', {name: '예약하기'}));
        expect(await screen.findByText('예약 정보 입력')).toBeInTheDocument();
    });
});
