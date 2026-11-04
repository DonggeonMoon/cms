import {type FormEvent, useEffect, useState} from 'react';
import {useNavigate} from 'react-router';
import type {Availability, Program} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatTime} from '../../components/format';
import {SpaceGrid} from '../../components/SpaceGrid';

export interface ReservationSelection {
    startAt: string | null;
    endAt: string | null;
    cells: string[];
    quantity?: number;
}

const today = () => new Date().toISOString().slice(0, 10);
const dateOf = (value: string | null) => (value ? value.slice(0, 10) : '');
const minutesBetween = (from: string, to: string) => (new Date(to).getTime() - new Date(from).getTime()) / 60000;

export function estimateReservation(program: Program, selection: ReservationSelection): number {
    const price = Number(program.price);
    if (program.reservationType === 'CONSULTATION') {
        return price;
    }
    if (program.reservationType === 'ITEM_RENTAL') {
        const days = selection.startAt && selection.endAt ? Math.max(0, Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440)) : 0;
        return (price * days + Number(program.depositAmount ?? 0)) * (selection.quantity ?? 1);
    }
    const perUnit = selection.cells.reduce((sum, key) => {
        const cell = program.layout?.cells.find((c) => c.key === key);
        return sum + (cell?.price != null ? Number(cell.price) : price);
    }, 0);
    if (!selection.startAt || !selection.endAt) {
        return perUnit;
    }
    if (program.reservationType === 'RENTAL') {
        return perUnit * (minutesBetween(selection.startAt, selection.endAt) / (program.slotMinutes ?? 60));
    }
    if (program.reservationType === 'LODGING') {
        return perUnit * Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440);
    }
    return perUnit;
}

export function ReservationPicker({program}: { program: Program }) {
    const navigate = useNavigate();
    const firstDay = [today(), dateOf(program.startDateTime)].sort().at(-1)!;
    const [date, setDate] = useState(firstDay);
    const [checkOut, setCheckOut] = useState('');
    const [startAt, setStartAt] = useState<string | null>(null);
    const [endAt, setEndAt] = useState<string | null>(null);
    const [cells, setCells] = useState<string[]>([]);
    const [quantity, setQuantity] = useState(1);
    const [availability, setAvailability] = useState<Availability | null>(null);
    const [error, setError] = useState<unknown>(null);
    const type = program.reservationType;

    useEffect(() => {
        const query = type === 'CONSULTATION' ? {date}
            : type === 'RENTAL' ? {date, ...(startAt && endAt ? {from: startAt, to: endAt} : {})}
                : type === 'LODGING' || type === 'ITEM_RENTAL' ? (date && checkOut ? {from: `${date}T00:00:00`, to: `${checkOut}T00:00:00`} : null)
                    : {};
        if (query === null) {
            setAvailability(null);
            return;
        }
        shopApi.availability(program.id, query).then((loaded) => {
            setAvailability(loaded);
            setCells((current) => current.filter((key) => !loaded.takenCells.includes(key)));
        }).catch(setError);
    }, [program.id, type, date, checkOut, startAt, endAt]);

    const byDates = type === 'LODGING' || type === 'ITEM_RENTAL';
    const selection: ReservationSelection = byDates
        ? {startAt: date && checkOut ? `${date}T00:00:00` : null, endAt: checkOut ? `${checkOut}T00:00:00` : null, cells, quantity}
        : {startAt, endAt, cells};
    const needsCells = type !== 'CONSULTATION' && type !== 'ITEM_RENTAL';
    const remaining = availability?.remainingItems ?? null;
    const rentalDays = selection.startAt && selection.endAt ? Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440) : 0;
    const rentalReady = type !== 'ITEM_RENTAL' || (rentalDays > 0 && (program.maxRentalDays == null || rentalDays <= program.maxRentalDays)
        && remaining != null && quantity <= remaining);
    const ready = (type === 'EVENT' || selection.startAt) && (!needsCells || cells.length > 0)
        && (type !== 'RENTAL' || (startAt && endAt && minutesBetween(startAt, endAt) > 0)) && rentalReady;

    const submit = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({programId: String(program.id)});
        if (selection.startAt) query.set('startAt', selection.startAt);
        if (selection.endAt) query.set('endAt', selection.endAt);
        if (cells.length) query.set('cells', cells.join(','));
        if (type === 'ITEM_RENTAL') query.set('quantity', String(quantity));
        navigate(`/reservations/new?${query}`);
    };

    const toggleCell = (key: string) => setCells((current) => (current.includes(key) ? current.filter((k) => k !== key) : [...current, key]));
    const slots = availability?.slots ?? [];

    return (
        <form className="reservation-picker" onSubmit={submit}>
            {(type === 'CONSULTATION' || type === 'RENTAL' || byDates) && (
                <div className="row align-center">
                    <label>{type === 'LODGING' ? '체크인' : type === 'ITEM_RENTAL' ? '대여일' : '날짜'}{' '}
                        <input type="date" value={date} min={firstDay} max={dateOf(program.endDateTime) || undefined}
                               onChange={(e) => {
                                   setDate(e.target.value);
                                   setStartAt(null);
                                   setEndAt(null);
                               }}/>
                    </label>
                    {byDates && (
                        <label>{type === 'ITEM_RENTAL' ? '반납일' : '체크아웃'}{' '}
                            <input type="date" value={checkOut} min={date} onChange={(e) => setCheckOut(e.target.value)}/>
                        </label>
                    )}
                    {type === 'ITEM_RENTAL' && (
                        <label>수량{' '}
                            <input type="number" min={1} max={remaining ?? undefined} value={quantity}
                                   onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}/>
                        </label>
                    )}
                </div>
            )}
            {type === 'CONSULTATION' && (
                <div className="slot-list" role="radiogroup" aria-label="상담 시간">
                    {slots.map((slot) => (
                        <button key={slot.startAt} type="button" role="radio" aria-checked={startAt === slot.startAt}
                                className={startAt === slot.startAt ? 'primary' : ''} disabled={slot.remaining <= 0}
                                onClick={() => {
                                    setStartAt(slot.startAt);
                                    setEndAt(slot.endAt);
                                }}>
                            {formatTime(slot.startAt)}{slot.remaining <= 0 && ' 마감'}
                        </button>
                    ))}
                    {slots.length === 0 && <p className="muted">이 날은 예약할 수 있는 시간이 없습니다.</p>}
                </div>
            )}
            {type === 'RENTAL' && (
                <div className="row align-center">
                    <label>시작{' '}
                        <select value={startAt ?? ''} onChange={(e) => setStartAt(e.target.value || null)}>
                            <option value="">선택</option>
                            {slots.map((slot) => <option key={slot.startAt} value={slot.startAt}>{formatTime(slot.startAt)}</option>)}
                        </select>
                    </label>
                    <label>종료{' '}
                        <select value={endAt ?? ''} onChange={(e) => setEndAt(e.target.value || null)}>
                            <option value="">선택</option>
                            {slots.filter((slot) => !startAt || slot.endAt > startAt).map((slot) => (
                                <option key={slot.endAt} value={slot.endAt}>{formatTime(slot.endAt)}</option>
                            ))}
                        </select>
                    </label>
                </div>
            )}
            {needsCells && program.layout && (type === 'EVENT' || (type === 'RENTAL' ? startAt && endAt : type === 'LODGING' ? checkOut : true)) && (
                <>
                    <SpaceGrid layout={program.layout} taken={availability?.takenCells ?? []} selected={cells}
                               onCellClick={(cell) => cell && toggleCell(cell.key)}/>
                    <p className="muted small">회색 칸은 이미 예약되었습니다. {cells.length > 0 && `선택: ${cells.map((key) =>
                        program.layout?.cells.find((c) => c.key === key)?.label ?? key).join(', ')}`}</p>
                </>
            )}
            {type === 'ITEM_RENTAL' && (
                <p className="muted small">
                    {remaining != null && `이 기간에 ${remaining}개를 빌릴 수 있습니다. `}
                    {program.maxRentalDays != null && `최대 ${program.maxRentalDays}일까지 빌릴 수 있습니다. `}
                    {rentalDays > 0 && `${rentalDays}일 대여. `}
                    {Number(program.depositAmount ?? 0) > 0 && `보증금 1개당 ${won(program.depositAmount!)}이 포함되며, 반납하면 돌려받습니다.`}
                </p>
            )}
            <p className="price align-right">예상 금액 {Number(program.price) === 0 && type !== 'RENTAL' && type !== 'LODGING' && type !== 'EVENT'
                ? '무료' : won(estimateReservation(program, selection))}</p>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={!ready}>예약하기</button>
            </div>
        </form>
    );
}
