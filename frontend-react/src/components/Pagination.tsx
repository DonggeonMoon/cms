interface Props {
    page: number;
    totalPages: number;
    onChange: (page: number) => void;
}

const BLOCK_SIZE = 10;

export function Pagination({page, totalPages, onChange}: Props) {
    if (totalPages <= 1) {
        return null;
    }
    const blockStart = Math.floor(page / BLOCK_SIZE) * BLOCK_SIZE;
    const blockEnd = Math.min(blockStart + BLOCK_SIZE, totalPages);
    const pages = Array.from({length: blockEnd - blockStart}, (_, i) => blockStart + i);
    return (
        <nav className="pagination" aria-label="페이지">
            {blockStart > 0 && (
                <button type="button" aria-label={`이전 ${BLOCK_SIZE}페이지`} onClick={() => onChange(blockStart - 1)}>‹</button>
            )}
            {pages.map((p) => (
                <button key={p} type="button" className={p === page ? 'current' : undefined}
                        aria-current={p === page ? 'page' : undefined} onClick={() => onChange(p)}>{p + 1}</button>
            ))}
            {blockEnd < totalPages && (
                <button type="button" aria-label={`다음 ${BLOCK_SIZE}페이지`} onClick={() => onChange(blockEnd)}>›</button>
            )}
        </nav>
    );
}
