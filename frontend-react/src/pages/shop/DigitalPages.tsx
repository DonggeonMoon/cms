import {useEffect, useState} from 'react';
import {Link} from 'react-router';
import type {DigitalAccess} from '../../api/domain';
import {digitalApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatBytes, formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

const reason = (access: DigitalAccess) => {
    if (access.revokedAt) {
        return '주문이 취소되었습니다';
    }
    if (access.remainingDownloads === 0) {
        return '내려받기 횟수를 모두 썼습니다';
    }
    return '내려받기 기간이 지났습니다';
};

export function MyDownloadsPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<DigitalAccess> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [refresh, setRefresh] = useState(0);
    useEffect(() => {
        digitalApi.mine(page).then(setData).catch(setError);
    }, [page, refresh]);

    // 내려받기는 브라우저가 링크로 처리하므로, 남은 횟수는 잠시 뒤 다시 불러와 맞춘다.
    const afterDownload = () => window.setTimeout(() => setRefresh((n) => n + 1), 1000);

    return (
        <section className="card">
            <h1>내려받기</h1>
            <ErrorBanner error={error}/>
            <ul className="list digital-list">
                {data?.content.map((access) => (
                    <li key={access.id}>
                        <div>
                            <strong>{access.productName}</strong>{' '}
                            <Link className="muted small" to="/mypage/orders">주문 {access.orderId}</Link>
                        </div>
                        <p className="muted small">
                            {access.remainingDownloads == null ? '횟수 제한 없음' : `남은 횟수 ${access.remainingDownloads} / ${access.downloadLimit}`}
                            {' · '}
                            {access.expiresAt ? `${formatDateTime(access.expiresAt)}까지` : '기간 제한 없음'}
                        </p>
                        {access.usable ? (
                            <ul className="digital-files">
                                {access.files.map((file) => (
                                    <li key={file.id}>
                                        <a className="button" href={file.downloadUrl} download onClick={afterDownload}>{file.name}</a>
                                        <span className="muted small"> {formatBytes(file.size)}</span>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="notice-box">{reason(access)}.</p>
                        )}
                    </li>
                ))}
            </ul>
            {data?.content.length === 0 && <p className="muted">내려받을 수 있는 파일이 없습니다.</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
