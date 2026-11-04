import {won} from '../components/AdminCrudPage';
import {useCallback, useEffect, useRef, useState} from 'react';
import {Link, useParams, useSearchParams} from 'react-router';
import type {MediaItem, MediaProgress, MyMedia} from '../api/domain';
import {mediaApi} from '../api/endpoints';
import type {MediaType, Page} from '../api/types';
import {useAuth} from '../auth/AuthContext';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../components/format';
import {ENROLLMENT_STATUS} from '../components/labels';
import {ListSearch} from '../components/ListSearch';
import {MemberOnlyNotice} from '../components/MemberOnlyNotice';
import {Pagination} from '../components/Pagination';
import {useFeatureMenuName} from '../components/SiteContext';
import {Thumbnail} from '../components/Thumbnail';
import {GradeBadge} from '../components/GradeBadge';

const REPORT_INTERVAL_MS = 10_000;
// timeupdate 사이 간격이 이보다 크면 재생이 아니라 건너뛰기로 본다.
const MAX_PLAYBACK_STEP_SECONDS = 3;

type Range = { start: number; end: number };

const mediaLabel = (item: Pick<MediaItem, 'mediaType'>) => (item.mediaType === 'AUDIO' ? '음악' : '동영상');

const TYPE_TABS: { value: MediaType | ''; label: string }[] = [
    {value: '', label: '전체'},
    {value: 'VIDEO', label: '동영상'},
    {value: 'AUDIO', label: '음악'},
];

export function MediaListPage() {
    const [params, setParams] = useSearchParams();
    const page = Number(params.get('page') ?? 0);
    const keyword = params.get('keyword') ?? '';
    const mediaType = (params.get('type') ?? '') as MediaType | '';
    const title = useFeatureMenuName('MEDIA', '스트리밍');
    const [data, setData] = useState<Page<MediaItem> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        mediaApi.list(keyword, mediaType, page).then(setData).catch(setError);
    }, [keyword, mediaType, page]);

    const go = (next: { type?: string; page?: number }) => {
        const type = next.type ?? mediaType;
        setParams({...(keyword && {keyword}), ...(type && {type}), ...(next.page ? {page: String(next.page)} : {})});
    };

    return (
        <section className="card">
            <h1>{title}</h1>
            <div className="tabs" role="tablist" aria-label="종류">
                {TYPE_TABS.map((tab) => (
                    <button key={tab.value} type="button" role="tab" aria-selected={mediaType === tab.value}
                            className={mediaType === tab.value ? 'tab active' : 'tab'} onClick={() => go({type: tab.value, page: 0})}>
                        {tab.label}
                    </button>
                ))}
            </div>
            <ListSearch label={`${title} 검색`} placeholder="제목 검색"/>
            <p className="small"><Link to="/courses">강의를 차시로 묶은 교육 과정 보기 →</Link></p>
            <ErrorBanner error={error}/>
            <div className="product-grid media-grid">
                {data?.content.map((item) => (
                    <Link key={item.id} to={`/media/${item.id}`} className="product-card media-card">
                        <div className="media-cover">
                            <Thumbnail src={item.thumbnailUrl} name={item.title}/>
                            <span className="media-duration">{formatDuration(item.durationSeconds)}</span>
                            {item.myProgress && (
                                <progress className="media-cover-progress" value={item.myProgress.progressRate} max={100}
                                          aria-label={`${item.title} 진도율`}/>
                            )}
                        </div>
                        <strong className="product-name">{item.title}</strong>
                        <span className="row align-center">
                            <span className="badge">{mediaLabel(item)}</span>
                            {item.paid && <span className="badge">{item.playable ? '구매함' : won(item.price ?? 0)}</span>}
                            <GradeBadge gradeId={item.accessGradeId}/>
                            {item.myProgress && <ProgressSummary progress={item.myProgress}/>}
                        </span>
                    </Link>
                ))}
            </div>
            {data?.content.length === 0 && <p className="muted">{keyword ? '검색 결과가 없습니다.' : '등록된 동영상·음악이 없습니다.'}</p>}
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={(next) => go({page: next})}/>}
        </section>
    );
}

export function MediaDetailPage() {
    const id = Number(useParams().id);
    const {user} = useAuth();
    const [item, setItem] = useState<MediaItem | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        mediaApi.get(id).then(setItem).catch(setError);
    }, [id]);

    if (!item) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    return (
        <section className="card">
            <h1>{item.title}</h1>
            <p className="muted">
                {mediaLabel(item)} · {formatDuration(item.durationSeconds)} · 진도율 {item.completionRate}% 이상 재생하면 수강 완료
            </p>
            {user?.userType !== 'MEMBER'
                ? <MemberOnlyNotice message={item.paid ? '회원으로 로그인한 뒤 구매해 주세요.' : '스트리밍은 회원만 재생할 수 있습니다. 진도는 회원별로 저장됩니다.'}/>
                : item.playable ? (
                    <>
                        {item.paid && (
                            <p className="muted small">구매한 강의입니다.{item.accessExpiresAt && ` ${formatDateTime(item.accessExpiresAt)}까지 볼 수 있습니다.`}</p>
                        )}
                        <MediaPlayer item={item}/>
                    </>
                ) : (
                    <div className="notice-box media-purchase">
                        <p><strong>{won(item.price ?? 0)}</strong>
                            {item.accessDays != null ? ` · 결제 후 ${item.accessDays}일 동안 시청` : ' · 결제 후 기간 제한 없이 시청'}</p>
                        <Link className="button primary" to={`/orders/new?productId=${item.productId}&quantity=1`}>구매하기</Link>
                    </div>
                )}
            {item.description && <p className="media-description">{item.description}</p>}
            <p><Link to="/media">목록으로</Link></p>
        </section>
    );
}

function MediaPlayer({item}: { item: MediaItem }) {
    const mediaRef = useRef<HTMLVideoElement & HTMLAudioElement>(null);
    const [progress, setProgress] = useState<MediaProgress | null>(item.myProgress);
    const [error, setError] = useState<unknown>(null);
    const started = useRef<Promise<unknown> | null>(item.myProgress ? Promise.resolve() : null);
    const segment = useRef<Range | null>(null);
    const pending = useRef<Range[]>([]);
    const lastTime = useRef(0);

    const closeSegment = () => {
        const current = segment.current;
        if (current && current.end - current.start > 0.2) {
            pending.current.push({...current});
        }
        segment.current = null;
    };

    const report = useCallback(() => {
        const media = mediaRef.current;
        if (!media || !started.current) {
            return;
        }
        if (segment.current) {
            const current = segment.current;
            closeSegment();
            if (!media.paused) {
                segment.current = {start: current.end, end: current.end};
            }
        }
        const ranges = pending.current.splice(0);
        if (ranges.length === 0) {
            return;
        }
        const position = media.currentTime;
        started.current
            .then(() => mediaApi.record(item.id, ranges, position))
            .then((next) => {
                setProgress(next);
                setError(null);
            })
            .catch((e) => {
                pending.current.unshift(...ranges);
                setError(e);
            });
    }, [item.id]);

    useEffect(() => {
        const timer = window.setInterval(report, REPORT_INTERVAL_MS);
        window.addEventListener('pagehide', report);
        return () => {
            window.clearInterval(timer);
            window.removeEventListener('pagehide', report);
            report();
        };
    }, [report]);

    const handlePlay = () => {
        if (started.current) {
            return;
        }
        started.current = mediaApi.start(item.id).then(setProgress).catch((e) => {
            started.current = null;
            setError(e);
        });
    };

    const handlePlaying = () => {
        const time = mediaRef.current?.currentTime ?? 0;
        lastTime.current = time;
        segment.current = {start: time, end: time};
    };

    const handleTimeUpdate = () => {
        const media = mediaRef.current;
        if (!media || media.paused || media.seeking) {
            return;
        }
        const time = media.currentTime;
        const step = time - lastTime.current;
        if (segment.current && step >= 0 && step <= MAX_PLAYBACK_STEP_SECONDS) {
            segment.current.end = time;
        } else {
            closeSegment();
            segment.current = {start: time, end: time};
        }
        lastTime.current = time;
    };

    const handleStop = () => {
        closeSegment();
        report();
    };

    const resumeAt = progress && progress.status !== 'COMPLETED' ? progress.lastPositionSeconds : 0;
    const handleLoadedMetadata = () => {
        const media = mediaRef.current;
        if (media && resumeAt > 0 && resumeAt < media.duration - 3) {
            media.currentTime = resumeAt;
            lastTime.current = resumeAt;
        }
    };

    const mediaProps = {
        ref: mediaRef,
        src: item.mediaUrl ?? undefined,
        controls: true,
        preload: 'metadata' as const,
        controlsList: 'nodownload',
        onPlay: handlePlay,
        onPlaying: handlePlaying,
        onTimeUpdate: handleTimeUpdate,
        onSeeking: closeSegment,
        onPause: handleStop,
        onEnded: handleStop,
        onLoadedMetadata: handleLoadedMetadata,
    };

    return (
        <div className="media-player">
            {item.mediaType === 'AUDIO'
                ? <audio {...mediaProps} className="media-element audio"/>
                : <video {...mediaProps} className="media-element" poster={item.posterUrl ?? undefined} playsInline/>}
            <div className="media-progress">
                <progress value={progress?.progressRate ?? 0} max={100} aria-label="진도율"/>
                <span>진도율 <strong>{progress?.progressRate ?? 0}%</strong></span>
                {progress && <span className={`badge${progress.status === 'COMPLETED' ? ' ok' : ''}`}>{ENROLLMENT_STATUS[progress.status]}</span>}
                {progress?.completedAt && <span className="muted small">완료 {formatDateTime(progress.completedAt)}</span>}
            </div>
            <p className="muted small">실제로 재생한 구간만 진도에 반영됩니다. 앞으로 건너뛴 구간은 들은 것으로 보지 않습니다.</p>
            <ErrorBanner error={error}/>
        </div>
    );
}

function ProgressSummary({progress}: { progress: MediaProgress }) {
    return progress.status === 'COMPLETED'
        ? <span className="badge ok">{ENROLLMENT_STATUS.COMPLETED}</span>
        : <span className="muted small">진도 {progress.progressRate}%</span>;
}

export function MyMediaPage() {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MyMedia> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        mediaApi.mine(page).then(setData).catch(setError);
    }, [page]);

    return (
        <section className="card">
            <h1>수강 내역</h1>
            <ErrorBanner error={error}/>
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>제목</th>
                        <th>진도율</th>
                        <th>상태</th>
                        <th>시작일</th>
                        <th>마지막 학습</th>
                        <th>완료일</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.content.map((item) => (
                        <tr key={item.mediaId}>
                            <td>
                                <span className="media-title-cell">
                                    {item.thumbnailUrl && <img className="media-admin-thumbnail" src={item.thumbnailUrl} alt=""/>}
                                    {item.available ? <Link to={`/media/${item.mediaId}`}>{item.title}</Link> : <span className="muted">{item.title}</span>}
                                </span>
                                {item.durationSeconds > 0 && <span className="muted small"> ({formatDuration(item.durationSeconds)})</span>}
                            </td>
                            <td>
                                <div className="media-progress">
                                    <progress value={item.progress?.progressRate ?? 0} max={100} aria-label={`${item.title} 진도율`}/>
                                    <span>{item.progress?.progressRate ?? 0}%</span>
                                </div>
                            </td>
                            <td>
                                {item.progress && (
                                    <span className={`badge${item.progress.status === 'COMPLETED' ? ' ok' : ''}`}>
                                        {ENROLLMENT_STATUS[item.progress.status]}
                                    </span>
                                )}
                            </td>
                            <td>{formatDateTime(item.progress?.startedAt)}</td>
                            <td>{formatDateTime(item.progress?.lastWatchedAt)}</td>
                            <td>{formatDateTime(item.progress?.completedAt)}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && (
                        <tr>
                            <td colSpan={6} className="muted">수강한 항목이 없습니다. <Link to="/media">스트리밍 보러 가기</Link></td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
