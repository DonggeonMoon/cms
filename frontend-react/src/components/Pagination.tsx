import {useI18n} from '../api/i18n';

interface Props {
    page: number;
    totalPages: number;
    onChange: (page: number) => void;
}

const BLOCK_SIZE = 10;

export function Pagination({page, totalPages, onChange}: Props) {
    const {t} = useI18n();
    if (totalPages <= 1) {
        return null;
    }
    const blockStart = Math.floor(page / BLOCK_SIZE) * BLOCK_SIZE;
    const blockEnd = Math.min(blockStart + BLOCK_SIZE, totalPages);
    const pages = Array.from({length: blockEnd - blockStart}, (_, i) => blockStart + i);
    const count = String(BLOCK_SIZE);
    return (
        <nav className="pagination" aria-label={t('common.page', '페이지')}>
            {blockStart > 0 && (
                <button type="button" aria-label={t('common.prevPages', '이전 {count}페이지', {count})} onClick={() => onChange(blockStart - 1)}>‹</button>
            )}
            {pages.map((p) => (
                <button key={p} type="button" className={p === page ? 'current' : undefined}
                        aria-current={p === page ? 'page' : undefined} onClick={() => onChange(p)}>{p + 1}</button>
            ))}
            {blockEnd < totalPages && (
                <button type="button" aria-label={t('common.nextPages', '다음 {count}페이지', {count})} onClick={() => onChange(blockEnd)}>›</button>
            )}
        </nav>
    );
}
