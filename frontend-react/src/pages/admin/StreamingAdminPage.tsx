import {useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import type {Learner, StreamingItem} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {MediaType, Page} from '../../api/types';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../../components/format';
import {ENROLLMENT_STATUS} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues, MediaValue} from '../../components/SchemaForm';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';

const FIELDS: FieldSpec[] = [
    {name: 'title', label: '제목', required: true, wide: true},
    {name: 'description', label: '설명', type: 'textarea'},
    {name: 'media', label: '동영상/음악 파일', type: 'media', required: true,
        help: 'mp4, webm, mov 동영상 또는 mp3, m4a, wav, ogg 음악. 올리면 재생 시간을 자동으로 읽고, 파일에 맞춰 동영상/음악으로 분류합니다. 동영상은 한 장면을 썸네일로 만들며, 다른 이미지로 바꿀 수 있습니다.'},
    {name: 'trackProgress', label: '진도 체크 사용', type: 'checkbox', wide: true, group: '교육',
        help: '끄면 시청 진도·수강 완료를 기록하지 않습니다. 강의가 아닌 일반 영상에 적합합니다.'},
    {name: 'completionRate', label: '수강 완료 진도율(%)', type: 'number', required: true, min: 1, wide: true, group: '교육',
        visibleWhen: (v) => Boolean(v.trackProgress),
        help: '실제로 재생한 구간이 이 비율 이상이면 수강 완료로 기록합니다.'},
    {name: 'isUsing', label: '공개', type: 'checkbox'},
    {name: 'paid', label: '유료', type: 'checkbox',
        help: '켜면 결제한 회원만 볼 수 있습니다. 판매용 상품이 자동으로 만들어지고(상품 목록에는 나오지 않음) 쿠폰·포인트를 쓸 수 있습니다.'},
    {name: 'price', label: '가격(원)', type: 'number', min: 1, required: true, visibleWhen: (v) => Boolean(v.paid)},
    {name: 'accessDays', label: '시청 가능 기간(일)', type: 'number', min: 1, visibleWhen: (v) => Boolean(v.paid),
        help: '결제일부터 셉니다. 비워두면 무기한입니다.'},
];

const streamingGradeFields = (grades: Grade[]) => [
    gradeSelectField('accessGradeId', '시청 등급', grades, '이 등급 이상 회원만 재생할 수 있습니다.'),
];

const toBody = (values: FormValues) => {
    const {media, ...rest} = values;
    const file = media as MediaValue | null;
    return {...rest, fileId: file?.id ?? null, thumbnailFileId: file?.thumbnailId ?? null, durationSeconds: file?.durationSeconds ?? null};
};

const loadStreaming = (query: Query) => adminApi.streaming.search(query);

export function StreamingAdminPage() {
    const [openId, setOpenId] = useState<number | null>(null);
    const [mediaType, setMediaType] = useState<MediaType | ''>('');
    const fields = useWithGradeFields(FIELDS, streamingGradeFields);
    return (
        <AdminCrudPage<StreamingItem>
            title="스트리밍"
            description={<p>회원이 재생하면 진도가 회원별로 저장되고, 완료 진도율을 넘기면 수강 완료로 기록됩니다.</p>}
            load={loadStreaming}
            extraQuery={{mediaType}}
            filters={
                <select aria-label="종류" value={mediaType} onChange={(e) => setMediaType(e.target.value as MediaType | '')}>
                    <option value="">전체 종류</option>
                    <option value="VIDEO">동영상</option>
                    <option value="AUDIO">음악</option>
                </select>
            }
            fields={fields}
            emptyForm={{completionRate: 90, trackProgress: true, isUsing: true, media: null}}
            toForm={(item) => ({
                title: item.title,
                description: item.description ?? '',
                completionRate: item.completionRate,
                trackProgress: item.trackProgress,
                isUsing: item.isUsing,
                accessGradeId: item.accessGradeId,
                media: {id: item.fileId, name: item.fileName ?? `파일 #${item.fileId}`, durationSeconds: item.durationSeconds,
                    thumbnailId: item.thumbnailFileId},
            })}
            create={(values) => adminApi.streaming.create(toBody(values))}
            update={(item, values) => adminApi.streaming.update(item.id, toBody(values))}
            remove={(item) => adminApi.streaming.remove(item.id)}
            columns={[
                {label: '번호', render: (l) => l.id},
                {label: '썸네일', render: (l) => (l.thumbnailUrl ? <img className="media-admin-thumbnail" src={l.thumbnailUrl} alt=""/> : '-')},
                {label: '제목', render: (l) => l.title},
                {label: '종류', render: (l) => (l.mediaType === 'AUDIO' ? '음악' : '동영상')},
                {label: '재생 시간', render: (l) => formatDuration(l.durationSeconds)},
                {label: '진도 체크', render: (l) => (l.trackProgress ? `사용 (${l.completionRate}%)` : '사용 안 함')},
                {label: '판매', render: (l) => (l.paid ? `유료 ${won(l.price ?? 0)}` : '무료')},
                {label: '공개', render: (l) => <YesNo value={l.isUsing}/>},
                {label: '등록일', render: (l) => formatDateTime(l.createdAt)},
            ]}
            actions={(l) => (
                l.trackProgress && (
                    <button type="button" onClick={() => setOpenId(openId === l.id ? null : l.id)}>
                        {openId === l.id ? '수강 현황 닫기' : '수강 현황'}
                    </button>
                )
            )}
            expandedRow={(l) => (openId === l.id ? <Learners streamingId={l.id}/> : null)}
        />
    );
}

function Learners({streamingId}: { streamingId: number }) {
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<Learner> | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        adminApi.streaming.learners(streamingId, page).then(setData).catch(setError);
    }, [streamingId, page]);

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
