import {useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import type {Learner, MediaItem} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {MediaType, Page} from '../../api/types';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../../components/format';
import {ENROLLMENT_STATUS} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues, MediaValue} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'title', label: '제목', required: true, wide: true},
    {name: 'media', label: '동영상/음악 파일', type: 'media', required: true,
        help: 'mp4, webm, mov 동영상 또는 mp3, m4a, wav, ogg 음악. 올리면 재생 시간을 자동으로 읽고, 파일에 맞춰 동영상/음악으로 분류합니다. 동영상은 한 장면을 썸네일로 만들며, 다른 이미지로 바꿀 수 있습니다.'},
    {name: 'completionRate', label: '수강 완료 진도율(%)', type: 'number', required: true, min: 1,
        help: '실제로 재생한 구간이 이 비율 이상이면 수강 완료로 기록합니다.'},
    {name: 'isUsing', label: '공개', type: 'checkbox'},
    {name: 'description', label: '설명', type: 'textarea'},
];

const toBody = (values: FormValues) => {
    const {media, ...rest} = values;
    const file = media as MediaValue | null;
    return {...rest, fileId: file?.id ?? null, thumbnailFileId: file?.thumbnailId ?? null, durationSeconds: file?.durationSeconds ?? null};
};

const loadMedia = (query: Query) => adminApi.media.search(query);

export function MediaAdminPage() {
    const [openId, setOpenId] = useState<number | null>(null);
    const [mediaType, setMediaType] = useState<MediaType | ''>('');
    return (
        <AdminCrudPage<MediaItem>
            title="스트리밍"
            description={<p>회원이 재생하면 진도가 회원별로 저장되고, 완료 진도율을 넘기면 수강 완료로 기록됩니다.</p>}
            load={loadMedia}
            extraQuery={{mediaType}}
            filters={
                <select aria-label="종류" value={mediaType} onChange={(e) => setMediaType(e.target.value as MediaType | '')}>
                    <option value="">전체 종류</option>
                    <option value="VIDEO">동영상</option>
                    <option value="AUDIO">음악</option>
                </select>
            }
            fields={FIELDS}
            emptyForm={{completionRate: 90, isUsing: true, media: null}}
            toForm={(item) => ({
                title: item.title,
                description: item.description ?? '',
                completionRate: item.completionRate,
                isUsing: item.isUsing,
                media: {id: item.fileId, name: item.fileName ?? `파일 #${item.fileId}`, durationSeconds: item.durationSeconds,
                    thumbnailId: item.thumbnailFileId},
            })}
            create={(values) => adminApi.media.create(toBody(values))}
            update={(item, values) => adminApi.media.update(item.id, toBody(values))}
            remove={(item) => adminApi.media.remove(item.id)}
            columns={[
                {label: '번호', render: (l) => l.id},
                {label: '썸네일', render: (l) => (l.thumbnailUrl ? <img className="media-admin-thumbnail" src={l.thumbnailUrl} alt=""/> : '-')},
                {label: '제목', render: (l) => l.title},
                {label: '종류', render: (l) => (l.mediaType === 'AUDIO' ? '음악' : '동영상')},
                {label: '재생 시간', render: (l) => formatDuration(l.durationSeconds)},
                {label: '완료 기준', render: (l) => `${l.completionRate}%`},
                {label: '공개', render: (l) => <YesNo value={l.isUsing}/>},
                {label: '등록일', render: (l) => formatDateTime(l.createdAt)},
            ]}
            actions={(l) => (
                <button type="button" onClick={() => setOpenId(openId === l.id ? null : l.id)}>
                    {openId === l.id ? '수강 현황 닫기' : '수강 현황'}
                </button>
            )}
            expandedRow={(l) => (openId === l.id ? <Learners mediaId={l.id}/> : null)}
        />
    );
}

function Learners({mediaId}: { mediaId: number }) {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Learner> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.media.learners(mediaId, page).then(setData).catch(setError);
    }, [mediaId, page]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!data) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <>
            <p className="muted">수강생 {data.page.totalElements}명</p>
            <table>
                <thead>
                <tr>
                    <th>아이디</th>
                    <th>진도율</th>
                    <th>상태</th>
                    <th>시작일</th>
                    <th>마지막 학습</th>
                    <th>완료일</th>
                </tr>
                </thead>
                <tbody>
                {data.content.map((learner) => (
                    <tr key={learner.memberId}>
                        <td>{learner.username}</td>
                        <td>{learner.progressRate}%</td>
                        <td>
                            <span className={`badge${learner.status === 'COMPLETED' ? ' ok' : ''}`}>{ENROLLMENT_STATUS[learner.status]}</span>
                        </td>
                        <td>{formatDateTime(learner.startedAt)}</td>
                        <td>{formatDateTime(learner.lastWatchedAt)}</td>
                        <td>{formatDateTime(learner.completedAt)}</td>
                    </tr>
                ))}
                {data.content.length === 0 && (
                    <tr>
                        <td colSpan={6} className="muted">아직 재생한 회원이 없습니다.</td>
                    </tr>
                )}
                </tbody>
            </table>
            <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>
        </>
    );
}
