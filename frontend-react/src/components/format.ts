export function formatDateTime(value: string | null | undefined): string {
    if (!value) {
        return '-';
    }
    return new Date(value).toLocaleString('ko-KR', {dateStyle: 'medium', timeStyle: 'short'});
}

export function formatDate(value: string | null | undefined): string {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function rowNumber(page: { number: number; size: number; totalElements: number }, index: number): number {
    return page.totalElements - page.number * page.size - index;
}

export function formatBytes(bytes: number): string {
    if (bytes < 1024) {
        return `${bytes} B`;
    }
    const units = ['KB', 'MB', 'GB'];
    let value = bytes / 1024;
    let unit = 0;
    while (value >= 1024 && unit < units.length - 1) {
        value /= 1024;
        unit++;
    }
    return `${value.toFixed(1)} ${units[unit]}`;
}

export function formatDuration(totalSeconds: number): string {
    const seconds = Math.max(0, Math.floor(totalSeconds));
    const pad = (n: number) => String(n).padStart(2, '0');
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return hours > 0 ? `${hours}:${pad(minutes)}:${pad(seconds % 60)}` : `${minutes}:${pad(seconds % 60)}`;
}

export function formatTime(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleTimeString('ko-KR', {hour: '2-digit', minute: '2-digit'}) : '';
}
