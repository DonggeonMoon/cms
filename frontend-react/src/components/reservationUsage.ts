import type {Reservation} from '../api/domain';
import {formatDate, formatDateTime, formatTime} from './format';

export function reservationUsage(reservation: Pick<Reservation, 'reservationType' | 'startAt' | 'endAt' | 'cells'>): string {
    const cells = reservation.cells.length ? ` · ${reservation.cells.join(', ')}` : '';
    switch (reservation.reservationType) {
        case 'CONSULTATION':
        case 'RENTAL':
            return `${formatDateTime(reservation.startAt)} ~ ${formatTime(reservation.endAt)}${cells}`;
        case 'LODGING':
            return `${formatDate(reservation.startAt)} ~ ${formatDate(reservation.endAt)}${cells}`;
        case 'EVENT':
            return `${formatDateTime(reservation.startAt)}${cells}`;
        default:
            return '-';
    }
}
