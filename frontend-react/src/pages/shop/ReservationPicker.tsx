import {type FormEvent, useEffect, useState} from 'react';
import {useNavigate} from 'react-router';
import {useAuth} from '../../auth/AuthContext';
import type {Availability, MemberPass, ReservationItem} from '../../api/domain';
import {shopApi} from '../../api/endpoints';
import {won} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime, formatTime} from '../../components/format';
import {SpaceGrid} from '../../components/SpaceGrid';

export interface ReservationSelection {
    startAt: string | null;
    endAt: string | null;
    cells: string[];
    quantity?: number;
    partyCounts?: Record<string, number>;
}

const today = () => new Date().toISOString().slice(0, 10);
const dateOf = (value: string | null) => (value ? value.slice(0, 10) : '');
const minutesBetween = (from: string, to: string) => (new Date(to).getTime() - new Date(from).getTime()) / 60000;

export function estimateReservation(item: ReservationItem, selection: ReservationSelection): number {
    const price = Number(item.price);
    if (item.reservationType === 'TOUR') {
        const counts = selection.partyCounts ?? {};
        return (item.priceTiers ?? []).reduce((sum, tier) => sum + Number(tier.price) * (counts[tier.name] ?? 0), 0);
    }
    if (item.reservationType === 'CONSULTATION' || item.reservationType === 'VISIT') {
        return price;
    }
    if (item.reservationType === 'ITEM_RENTAL') {
        const days = selection.startAt && selection.endAt ? Math.max(0, Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440)) : 0;
        return (price * days + Number(item.depositAmount ?? 0)) * (selection.quantity ?? 1);
    }
    const perUnit = selection.cells.reduce((sum, key) => {
        const cell = item.layout?.cells.find((c) => c.key === key);
        return sum + (cell?.price != null ? Number(cell.price) : price);
    }, 0);
    if (!selection.startAt || !selection.endAt) {
        return perUnit;
    }
    if (item.reservationType === 'RENTAL' || item.reservationType === 'TABLE') {
        return perUnit * (minutesBetween(selection.startAt, selection.endAt) / (item.slotMinutes ?? 60));
    }
    if (item.reservationType === 'LODGING') {
        return perUnit * Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440);
    }
    return perUnit;
}

export function ReservationPicker({item}: { item: ReservationItem }) {
    const navigate = useNavigate();
    const {user} = useAuth();
    const firstDay = [today(), dateOf(item.startDateTime)].sort().at(-1)!;
    const [date, setDate] = useState(firstDay);
    const [checkOut, setCheckOut] = useState('');
    const [startAt, setStartAt] = useState<string | null>(null);
    const [endAt, setEndAt] = useState<string | null>(null);
    const [cells, setCells] = useState<string[]>([]);
    const [quantity, setQuantity] = useState(1);
    const [partySize, setPartySize] = useState(2);
    const [partyCounts, setPartyCounts] = useState<Record<string, number>>({});
    const [sessionId, setSessionId] = useState('');
    const [passes, setPasses] = useState<MemberPass[]>([]);
    const [passId, setPassId] = useState('');
    const [availability, setAvailability] = useState<Availability | null>(null);
    const [error, setError] = useState<unknown>(null);
    const type = item.reservationType;
    const eventSessions = (item.sessions ?? []).filter((s) => s.isOpen);
    const hasEventSessions = type === 'EVENT' && eventSessions.length > 0;
    const tourSessions = (item.sessions ?? []).filter((s) => s.isOpen);
    const hasTourSessions = type === 'TOUR' && tourSessions.length > 0;
    const totalParty = Object.values(partyCounts).reduce((sum, n) => sum + (n || 0), 0);

    useEffect(() => {
        if (type === 'TOUR') {
            setPartyCounts(Object.fromEntries((item.priceTiers ?? []).map((t) => [t.name, 0])));
        }
    }, [item.id, type, item.priceTiers]);

    useEffect(() => {
        if (user?.userType === 'MEMBER') {
            shopApi.usablePasses(item.id).then(setPasses).catch(() => setPasses([]));
        }
    }, [item.id, user?.userType]);

    useEffect(() => {
        if (type === 'TOUR') {
            setAvailability(null);
            return;
        }
        if (hasEventSessions && !sessionId) {
            setAvailability(null);
            return;
        }
        const query = type === 'CONSULTATION' || type === 'VISIT' ? {date}
            : type === 'RENTAL' || type === 'TABLE' ? {date, ...(startAt && endAt ? {from: startAt, to: endAt} : {})}
                : type === 'LODGING' || type === 'ITEM_RENTAL' ? (date && checkOut ? {from: `${date}T00:00:00`, to: `${checkOut}T00:00:00`} : null)
                    : hasEventSessions ? {sessionId: Number(sessionId)}
                        : {};
        if (query === null) {
            setAvailability(null);
            return;
        }
        shopApi.availability(item.id, query).then((loaded) => {
            setAvailability(loaded);
            setCells((current) => current.filter((key) => !loaded.takenCells.includes(key)));
        }).catch(setError);
    }, [item.id, type, date, checkOut, startAt, endAt, hasEventSessions, sessionId]);

    const byDates = type === 'LODGING' || type === 'ITEM_RENTAL';
    const selection: ReservationSelection = byDates
        ? {startAt: date && checkOut ? `${date}T00:00:00` : null, endAt: checkOut ? `${checkOut}T00:00:00` : null, cells, quantity}
        : type === 'TOUR' ? {startAt: null, endAt: null, cells: [], partyCounts}
            : {startAt, endAt, cells};
    const needsCells = type !== 'CONSULTATION' && type !== 'ITEM_RENTAL' && type !== 'VISIT' && type !== 'TOUR';
    const remaining = availability?.remainingItems ?? null;
    const rentalDays = selection.startAt && selection.endAt ? Math.round(minutesBetween(selection.startAt, selection.endAt) / 1440) : 0;
    const rentalReady = type !== 'ITEM_RENTAL' || (rentalDays > 0 && (item.maxRentalDays == null || rentalDays <= item.maxRentalDays)
        && remaining != null && quantity <= remaining);
    const ready = (type === 'EVENT' || type === 'TOUR' || selection.startAt) && (!needsCells || cells.length > 0)
        && (type !== 'RENTAL' && type !== 'TABLE' || (startAt && endAt && minutesBetween(startAt, endAt) > 0)) && rentalReady
        && (type !== 'TABLE' || partySize > 0) && (!hasEventSessions || sessionId !== '')
        && (type !== 'TOUR' || (totalParty > 0 && (!hasTourSessions || sessionId !== '')));

    const submit = (event: FormEvent) => {
        event.preventDefault();
        const query = new URLSearchParams({reservationItemId: String(item.id)});
        if (selection.startAt) query.set('startAt', selection.startAt);
        if (selection.endAt) query.set('endAt', selection.endAt);
        if (cells.length) query.set('cells', cells.join(','));
        if (type === 'ITEM_RENTAL') query.set('quantity', String(quantity));
        if (type === 'TABLE') query.set('partySize', String(partySize));
        if (type === 'TOUR') {
            query.set('quantity', String(totalParty));
            query.set('partyCounts', JSON.stringify(partyCounts));
        }
        if ((hasEventSessions || hasTourSessions) && sessionId) query.set('sessionId', sessionId);
        if (passId) query.set('passId', passId);
        navigate(`/reservations/new?${query}`);
    };

    const toggleCell = (key: string) => setCells((current) => (current.includes(key) ? current.filter((k) => k !== key) : [...current, key]));
    const slots = availability?.slots ?? [];

    return (
        <form className="reservation-picker" onSubmit={submit}>
            {hasEventSessions && (
                <div className="field">
                    <label htmlFor="sessionId">회차 선택</label>
                    <select id="sessionId" required value={sessionId} onChange={(e) => {
                        setSessionId(e.target.value);
                        setCells([]);
                    }}>
                        <option value="">선택</option>
                        {eventSessions.map((s) => (
                            <option key={s.id} value={s.id} disabled={s.remaining === 0}>
                                {s.name ? `${s.name} · ` : ''}{formatDateTime(s.startAt)}
                                {s.remaining != null && ` (잔여 ${s.remaining}석)`}{s.remaining === 0 && ' 마감'}
                            </option>
                        ))}
                    </select>
                </div>
            )}
            {hasTourSessions && (
                <div className="field">
                    <label htmlFor="sessionId">출발일 선택</label>
                    <select id="sessionId" required value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
                        <option value="">선택</option>
                        {tourSessions.map((s) => (
                            <option key={s.id} value={s.id} disabled={s.remaining === 0}>
                                {s.name ? `${s.name} · ` : ''}{formatDateTime(s.startAt)}
                                {s.remaining != null && ` (잔여 ${s.remaining}명)`}{s.remaining === 0 && ' 마감'}
                            </option>
                        ))}
                    </select>
                </div>
            )}
            {type === 'TOUR' && (
                <div className="row align-center">
                    {(item.priceTiers ?? []).map((tier) => (
                        <label key={tier.name}>{tier.name} ({won(tier.price)}){' '}
                            <input type="number" min={0} value={partyCounts[tier.name] ?? 0}
                                   onChange={(e) => setPartyCounts({...partyCounts, [tier.name]: Math.max(0, Number(e.target.value))})}/>
                        </label>
                    ))}
                </div>
            )}
            {(type === 'CONSULTATION' || type === 'VISIT' || type === 'RENTAL' || type === 'TABLE' || byDates) && (
                <div className="row align-center">
                    <label>{type === 'LODGING' ? '체크인' : type === 'ITEM_RENTAL' ? '대여일' : '날짜'}{' '}
                        <input type="date" value={date} min={firstDay} max={dateOf(item.endDateTime) || undefined}
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
            {(type === 'CONSULTATION' || type === 'VISIT') && (
                <div className="slot-list" role="radiogroup" aria-label={type === 'VISIT' ? '방문 시간' : '상담 시간'}>
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
            {(type === 'RENTAL' || type === 'TABLE') && (
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
                    {type === 'TABLE' && (
                        <label>인원{' '}
                            <input type="number" min={1} value={partySize}
                                   onChange={(e) => setPartySize(Math.max(1, Number(e.target.value)))}/>
                        </label>
                    )}
                </div>
            )}
            {needsCells && item.layout && (!hasEventSessions || sessionId)
                && (type === 'EVENT' || (type === 'RENTAL' || type === 'TABLE' ? startAt && endAt : type === 'LODGING' ? checkOut : true)) && (
                <>
                    <SpaceGrid layout={item.layout} taken={availability?.takenCells ?? []} selected={cells}
                               onCellClick={(cell) => cell && toggleCell(cell.key)}/>
                    <p className="muted small">회색 칸은 이미 예약되었습니다. {cells.length > 0 && `선택: ${cells.map((key) =>
                        item.layout?.cells.find((c) => c.key === key)?.label ?? key).join(', ')}`}</p>
                </>
            )}
            {type === 'ITEM_RENTAL' && (
                <p className="muted small">
                    {remaining != null && `이 기간에 ${remaining}개를 빌릴 수 있습니다. `}
                    {item.maxRentalDays != null && `최대 ${item.maxRentalDays}일까지 빌릴 수 있습니다. `}
                    {rentalDays > 0 && `${rentalDays}일 대여. `}
                    {Number(item.depositAmount ?? 0) > 0 && `보증금 1개당 ${won(item.depositAmount!)}이 포함되며, 반납하면 돌려받습니다.`}
                </p>
            )}
            {passes.length > 0 && (
                <div className="field">
                    <label htmlFor="passId">이용권으로 신청</label>
                    <select id="passId" value={passId} onChange={(e) => setPassId(e.target.value)}>
                        <option value="">결제로 신청</option>
                        {passes.map((pass) => (
                            <option key={pass.id} value={pass.id}>
                                {pass.productName}{pass.unit === 'COUNT' ? ` (잔여 ${pass.remainingCount}회)` : ''}
                            </option>
                        ))}
                    </select>
                </div>
            )}
            <p className="price align-right">예상 금액 {passId ? '이용권 사용 (0원)'
                : Number(item.price) === 0 && type !== 'RENTAL' && type !== 'LODGING' && type !== 'EVENT' && type !== 'TABLE' && type !== 'TOUR'
                ? '무료' : won(estimateReservation(item, selection))}</p>
            <ErrorBanner error={error}/>
            <div className="toolbar form-actions">
                <button type="submit" className="primary" disabled={!ready}>예약하기</button>
            </div>
        </form>
    );
}
