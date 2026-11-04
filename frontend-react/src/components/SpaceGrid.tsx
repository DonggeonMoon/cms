import type {LayoutCell, SpaceLayout} from '../api/domain';
import {won} from './AdminCrudPage';
import {CELL_KIND} from './labels';

interface Props {
    layout: SpaceLayout;
    selected?: string[];
    taken?: string[];
    onCellClick?: (cell: LayoutCell | null, row: number, col: number) => void;
    editing?: boolean;
}

export function SpaceGrid({layout, selected = [], taken = [], onCellClick, editing = false}: Props) {
    const at = new Map(layout.cells.map((cell) => [`${cell.row}:${cell.col}`, cell]));
    return (
        <div className="space-grid" style={{gridTemplateColumns: `repeat(${layout.cols}, minmax(2.6rem, 1fr))`}}
             role="grid" aria-label="배치표">
            {Array.from({length: layout.rows * layout.cols}, (_, index) => {
                const row = Math.floor(index / layout.cols);
                const col = index % layout.cols;
                const cell = at.get(`${row}:${col}`) ?? null;
                const kind = cell?.kind ?? 'EMPTY';
                const bookable = kind === 'SEAT' || kind === 'ROOM' || kind === 'SPACE';
                const isTaken = cell != null && taken.includes(cell.key);
                const isSelected = cell != null && selected.includes(cell.key);
                const clickable = editing || (bookable && !isTaken);
                const title = cell ? `${CELL_KIND[kind]} ${cell.label}${cell.price != null ? ` · ${won(cell.price)}` : ''}${isTaken ? ' (예약됨)' : ''}` : '빈칸';
                return (
                    <button key={index} type="button" role="gridcell" title={title} aria-label={title}
                            aria-pressed={bookable && !editing ? isSelected : undefined}
                            className={`space-cell kind-${kind.toLowerCase()}${isTaken ? ' taken' : ''}${isSelected ? ' selected' : ''}`}
                            disabled={!clickable} onClick={() => onCellClick?.(cell, row, col)}>
                        {cell?.label ?? ''}
                    </button>
                );
            })}
        </div>
    );
}
