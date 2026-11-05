import type {Reservation} from '../api/domain';
import {formatDate, formatDateTime, formatTime} from './format';

export function reservationUsage(reservation: Pick<Reservation, 'reservationType' | 'startAt' | 'endAt' | 'cells' | 'partySize' | 'partyBreakdown'>): string {
    const cells = reservation.cells.length ? ` · ${reservation.cells.join(', ')}` : '';
    switch (reservation.reservationType) {
        case 'TOUR': {
            const breakdown = Object.entries(reservation.partyBreakdown ?? {}).map(([name, count]) => `${name} ${count}명`).join(', ');
            return `${formatDateTime(reservation.startAt)}${breakdown ? ` · ${breakdown}` : ''}`;
        }
        case 'TABLE':
            return `${formatDateTime(reservation.startAt)} ~ ${formatTime(reservation.endAt)}${cells}${reservation.partySize ? ` · ${reservation.partySize}명` : ''}`;
        case 'CONSULTATION':
        case 'VISIT':
        case 'RENTAL':
            return `${formatDateTime(reservation.startAt)} ~ ${formatTime(reservation.endAt)}${cells}`;
        case 'LODGING':
            return `${formatDate(reservation.startAt)} ~ ${formatDate(reservation.endAt)}${cells}`;
        case 'ITEM_RENTAL':
            return `${formatDate(reservation.startAt)} 대여 ~ ${formatDate(reservation.endAt)} 반납`;
        case 'EVENT':
            return `${formatDateTime(reservation.startAt)}${cells}`;
        case 'PROGRAM':
            return formatDateTime(reservation.startAt);
        default:
            return '-';
    }
}
