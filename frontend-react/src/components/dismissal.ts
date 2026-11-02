const localDate = (offsetDays = 0) => {
    const date = new Date();
    date.setDate(date.getDate() + offsetDays);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

function read(storageKey: string): Record<string, string> {
    return JSON.parse(localStorage.getItem(storageKey) ?? '{}') as Record<string, string>;
}

export function isDismissed(storageKey: string, id: number): boolean {
    try {
        const hiddenUntil = read(storageKey)[id];
        return Boolean(hiddenUntil) && localDate() < hiddenUntil;
    } catch {
        return false;
    }
}

export function dismissForDays(storageKey: string, id: number, days: number) {
    try {
        const today = localDate();
        const kept = Object.fromEntries(Object.entries(read(storageKey)).filter(([, until]) => today < until));
        localStorage.setItem(storageKey, JSON.stringify({...kept, [id]: localDate(days)}));
    } catch {
    }
}

export function dismissLabel(days: number): string {
    return days === 1 ? '오늘 하루 보지 않기' : `${days}일간 보지 않기`;
}
