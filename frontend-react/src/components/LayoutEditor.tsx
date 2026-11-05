import {useState} from 'react';
import type {CellKind, LayoutCell, SpaceLayout} from '../api/domain';
import {CELL_KIND} from './labels';
import {SpaceGrid} from './SpaceGrid';

type Brush = CellKind | 'PICK';

const BRUSHES: { value: Brush; label: string }[] = [
    {value: 'PICK', label: '선택'},
    ...(['SEAT', 'ROOM', 'SPACE', 'AISLE', 'STAGE'] as CellKind[]).map((kind) => ({value: kind as Brush, label: CELL_KIND[kind]})),
    {value: 'EMPTY', label: '지우기'},
];

export const EMPTY_LAYOUT: SpaceLayout = {rows: 5, cols: 8, cells: []};

const defaultLabel = (kind: CellKind, row: number, col: number) => {
    if (kind === 'STAGE') {
        return '무대';
    }
    if (kind === 'AISLE') {
        return '';
    }
    return kind === 'ROOM' ? `${row + 1}0${col + 1}` : `${String.fromCharCode(65 + row)}${col + 1}`;
};

export function LayoutEditor({value, onChange}: { value: SpaceLayout | null; onChange: (layout: SpaceLayout) => void }) {
    const layout = value ?? EMPTY_LAYOUT;
    const [brush, setBrush] = useState<Brush>('SEAT');
    const [picked, setPicked] = useState<string | null>(null);
    const pickedCell = layout.cells.find((cell) => cell.key === picked) ?? null;

    const resize = (rows: number, cols: number) => {
        const r = Math.max(1, Math.min(40, rows || 1));
        const c = Math.max(1, Math.min(40, cols || 1));
        onChange({rows: r, cols: c, cells: layout.cells.filter((cell) => cell.row < r && cell.col < c)});
    };

    const paint = (cell: LayoutCell | null, row: number, col: number) => {
        if (brush === 'PICK') {
            setPicked(cell?.key ?? null);
            return;
        }
        const others = layout.cells.filter((existing) => !(existing.row === row && existing.col === col));
        if (brush === 'EMPTY') {
            onChange({...layout, cells: others});
            return;
        }
        const key = cell?.key ?? `r${row}c${col}`;
        const label = cell && cell.kind === brush ? cell.label : defaultLabel(brush, row, col);
        onChange({...layout, cells: [...others, {key, row, col, kind: brush, label, price: cell?.price ?? null, capacity: cell?.capacity ?? null}]});
        setPicked(key);
    };

    const updatePicked = (patch: Partial<LayoutCell>) => {
        onChange({...layout, cells: layout.cells.map((cell) => (cell.key === picked ? {...cell, ...patch} : cell))});
    };

    const bookable = layout.cells.filter((cell) => cell.kind === 'SEAT' || cell.kind === 'ROOM' || cell.kind === 'SPACE').length;

    return (
        <div className="layout-editor">
            <div className="row align-center">
                <label>행 <input type="number" min={1} max={40} value={layout.rows} onChange={(e) => resize(Number(e.target.value), layout.cols)}/></label>
                <label>열 <input type="number" min={1} max={40} value={layout.cols} onChange={(e) => resize(layout.rows, Number(e.target.value))}/></label>
                <span className="muted small">예약 가능한 칸 {bookable}개</span>
            </div>
            <div className="toolbar" role="radiogroup" aria-label="칠할 종류">
                {BRUSHES.map((option) => (
                    <button key={option.value} type="button" role="radio" aria-checked={brush === option.value}
                            className={`brush kind-${option.value.toLowerCase()}${brush === option.value ? ' active' : ''}`}
                            onClick={() => setBrush(option.value)}>{option.label}</button>
                ))}
            </div>
            <SpaceGrid layout={layout} editing selected={picked ? [picked] : []} onCellClick={paint}/>
            {pickedCell && (
                <div className="row align-center layout-cell-editor">
                    <strong>{CELL_KIND[pickedCell.kind]}</strong>
                    <label>이름 <input value={pickedCell.label} maxLength={20} onChange={(e) => updatePicked({label: e.target.value})}/></label>
                    {(pickedCell.kind === 'SEAT' || pickedCell.kind === 'ROOM' || pickedCell.kind === 'SPACE') && (
                        <>
                            <label>가격(원) <input type="number" min={0} placeholder="기본 가격" value={pickedCell.price ?? ''}
                                                 onChange={(e) => updatePicked({price: e.target.value === '' ? null : Number(e.target.value)})}/></label>
                            <label>인원(명) <input type="number" min={1} placeholder="제한 없음" value={pickedCell.capacity ?? ''}
                                                 onChange={(e) => updatePicked({capacity: e.target.value === '' ? null : Number(e.target.value)})}/></label>
                        </>
                    )}
                </div>
            )}
            <p className="muted small">종류를 고르고 칸을 누르면 칠해집니다. "선택"으로 칸을 누르면 이름·가격(비우면 기본 가격)·인원(식당 테이블 등에서 쓰며, 비우면 제한 없음)을 고칠 수 있습니다.</p>
        </div>
    );
}
