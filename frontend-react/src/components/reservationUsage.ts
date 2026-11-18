import type {Reservation} from '../api/domain';
import {formatDate, formatDateTime, formatTime} from './format';

type TFunc = (key: string, fallback: string, values?: Record<string, string>) => string;

export function reservationUsage(reservation: Pick<Reservation, 'reservationType' | 'startAt' | 'endAt' | 'cells' | 'partySize' | 'partyBreakdown'>, t: TFunc): string {
    const cells = reservation.cells.length ? ` · ${reservation.cells.join(', ')}` : '';
    switch (reservation.reservationType) {
        case 'TOUR': {
            const breakdown = Object.entries(reservation.partyBreakdown ?? {})
                .map(([name, count]) => t('label.reservationUsage.partyCount', '{name} {count}명', {name, count: String(count)})).join(', ');
            return `${formatDateTime(reservation.startAt)}${breakdown ? ` · ${breakdown}` : ''}`;
        }
        case 'TABLE':
            return `${formatDateTime(reservation.startAt)} ~ ${formatTime(reservation.endAt)}${cells}${reservation.partySize ? ` · ${t('label.reservationUsage.people', '{count}명', {count: String(reservation.partySize)})}` : ''}`;
        case 'CONSULTATION':
        case 'VISIT':
        case 'RENTAL':
            return `${formatDateTime(reservation.startAt)} ~ ${formatTime(reservation.endAt)}${cells}`;
        case 'LODGING':
            return `${formatDate(reservation.startAt)} ~ ${formatDate(reservation.endAt)}${cells}`;
        case 'ITEM_RENTAL':
            return t('label.reservationUsage.itemRental', '{start} 대여 ~ {end} 반납', {start: formatDate(reservation.startAt), end: formatDate(reservation.endAt)});
        case 'EVENT':
            return `${formatDateTime(reservation.startAt)}${cells}`;
        case 'PROGRAM':
            return formatDateTime(reservation.startAt);
        default:
            return '-';
    }
}
