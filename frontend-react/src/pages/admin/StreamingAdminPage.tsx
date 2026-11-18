import {useEffect, useState} from 'react';
import type {Query} from '../../api/client';
import type {Learner, StreamingItem} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {MediaType, Page} from '../../api/types';
import {AdminCrudPage, won, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime, formatDuration} from '../../components/format';
import {enrollmentStatus} from '../../components/labels';
import {Pagination} from '../../components/Pagination';
import type {FieldSpec, FormValues, MediaValue} from '../../components/SchemaForm';
import {type Grade, gradeSelectField, useWithGradeFields} from '../../api/grades';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const streamingFields = (t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.course.field.isUsing', '공개'), type: 'checkbox'},
    {name: 'title', label: t('admin.content.field.title', '제목'), required: true, wide: true},
    {name: 'description', label: t('admin.board.field.description', '설명'), type: 'textarea'},
    {name: 'media', label: t('admin.streaming.field.media', '동영상/음악 파일'), type: 'media', required: true,
        help: t('admin.streaming.field.media.help', 'mp4, webm, mov 동영상 또는 mp3, m4a, wav, ogg 음악. 올리면 재생 시간을 자동으로 읽고, 파일에 맞춰 동영상/음악으로 분류합니다. 동영상은 한 장면을 썸네일로 만들며, 다른 이미지로 바꿀 수 있습니다.')},
    {name: 'paid', label: t('admin.course.field.paid', '유료'), type: 'checkbox',
        help: t('admin.streaming.field.paid.help', '켜면 결제한 회원만 볼 수 있습니다. 판매용 상품이 자동으로 만들어지고(상품 목록에는 나오지 않음) 쿠폰·포인트를 쓸 수 있습니다.')},
    {name: 'price', label: t('admin.course.field.price', '가격(원)'), type: 'number', min: 1, required: true, newRow: true, visibleWhen: (v) => Boolean(v.paid)},
    {name: 'accessDays', label: t('admin.streaming.field.accessDays', '시청 가능 기간(일)'), type: 'number', min: 1, visibleWhen: (v) => Boolean(v.paid),
        help: t('admin.streaming.field.accessDays.help', '결제일부터 셉니다. 비워두면 무기한입니다.')},
];

const streamingGradeFields = (grades: Grade[], t: T) => [
    gradeSelectField('accessGradeId', t('admin.streaming.field.accessGradeId', '시청 등급'), grades, t('admin.streaming.field.accessGradeId.help', '이 등급 이상 회원만 재생할 수 있습니다.'), t),
];

const toBody = (values: FormValues) => {
    const {media, ...rest} = values;
    const file = media as MediaValue | null;
    return {...rest, fileId: file?.id ?? null, thumbnailFileId: file?.thumbnailId ?? null, durationSeconds: file?.durationSeconds ?? null};
};

const loadStreaming = (query: Query) => adminApi.streaming.search(query);

export function StreamingAdminPage() {
    const {t} = useI18n();
    const [openId, setOpenId] = useState<number | null>(null);
    const [mediaType, setMediaType] = useState<MediaType | ''>('');
    const fields = useWithGradeFields(streamingFields(t), (grades) => streamingGradeFields(grades, t));
    return (
        <AdminCrudPage<StreamingItem>
            title={t('admin.streaming.title', '스트리밍')}
            description={<p>{t('admin.streaming.description', '회원이 재생하면 진도가 회원별로 저장됩니다. 진도 체크 사용 여부·완료 기준은 이 영상을 차시로 쓰는 시리즈/교육과정에서 정합니다.')}</p>}
            load={loadStreaming}
            extraQuery={{mediaType}}
            filters={
                <select aria-label={t('shop.streaming.typeTabsLabel', '종류')} value={mediaType} onChange={(e) => setMediaType(e.target.value as MediaType | '')}>
                    <option value="">{t('admin.streaming.allTypes', '전체 종류')}</option>
                    <option value="VIDEO">{t('shop.streaming.video', '동영상')}</option>
                    <option value="AUDIO">{t('shop.streaming.audio', '음악')}</option>
                </select>
            }
            fields={fields}
            emptyForm={{isUsing: true, media: null}}
            toForm={(item) => ({
                title: item.title,
                description: item.description ?? '',
                isUsing: item.isUsing,
                accessGradeId: item.accessGradeId,
                media: {id: item.fileId, name: item.fileName ?? t('admin.streaming.fileFallback', '파일 #{id}', {id: String(item.fileId)}), durationSeconds: item.durationSeconds,
                    thumbnailId: item.thumbnailFileId},
            })}
            create={(values) => adminApi.streaming.create(toBody(values))}
            update={(item, values) => adminApi.streaming.update(item.id, toBody(values))}
            remove={(item) => adminApi.streaming.remove(item.id)}
            columns={[
                {label: t('admin.streaming.column.id', '번호'), render: (l) => l.id},
                {label: t('admin.streaming.column.thumbnail', '썸네일'), render: (l) => (l.thumbnailUrl ? <img className="media-admin-thumbnail" src={l.thumbnailUrl} alt=""/> : '-')},
                {label: t('admin.content.field.title', '제목'), render: (l) => l.title},
                {label: t('admin.streaming.column.type', '종류'), render: (l) => (l.mediaType === 'AUDIO' ? t('shop.streaming.audio', '음악') : t('shop.streaming.video', '동영상'))},
                {label: t('admin.streaming.column.duration', '재생 시간'), render: (l) => formatDuration(l.durationSeconds)},
                {label: t('admin.streaming.column.sale', '판매'), render: (l) => (l.paid ? t('admin.streaming.paidPrice', '유료 {price}', {price: won(l.price ?? 0)}) : t('shop.common.free', '무료'))},
                {label: t('admin.streaming.column.trackProgress', '진도 체크 (소속 시리즈/교육과정 설정)'), render: (l) => (l.trackProgress ? t('admin.streaming.trackProgressOn', '사용 ({rate}%)', {rate: String(l.completionRate)}) : t('admin.map.keyNotUsed', '사용 안 함'))},
                {label: t('admin.course.field.isUsing', '공개'), render: (l) => <YesNo value={l.isUsing}/>},
                {label: t('admin.archive.column.createdAt', '등록일'), render: (l) => formatDateTime(l.createdAt)},
            ]}
            actions={(l) => (
                l.trackProgress && (
                    <button type="button" onClick={() => setOpenId(openId === l.id ? null : l.id)}>
                        {openId === l.id ? t('admin.streaming.closeLearnerStatus', '수강 현황 닫기') : t('admin.streaming.learnerStatus', '수강 현황')}
                    </button>
                )
            )}
            expandedRow={(l) => (openId === l.id ? <Learners streamingId={l.id}/> : null)}
        />
    );
}

function Learners({streamingId}: { streamingId: number }) {
    const {t} = useI18n();
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
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <>
            <p className="muted">{t('admin.streaming.learnerCount', '수강생 {count}명', {count: String(data.page.totalElements)})}</p>
            <table>
                <thead>
                <tr>
                    <th>{t('admin.memberJoin.username', '아이디')}</th>
                    <th>{t('shop.streaming.progressRateLabel', '진도율')}</th>
                    <th>{t('admin.order.column.status', '상태')}</th>
                    <th>{t('shop.streaming.startedAt', '시작일')}</th>
                    <th>{t('shop.streaming.lastWatchedAt', '마지막 학습')}</th>
                    <th>{t('admin.course.completedColumn', '완료일')}</th>
                </tr>
                </thead>
                <tbody>
                {data.content.map((learner) => (
                    <tr key={learner.memberId}>
                        <td>{learner.username}</td>
                        <td>{learner.progressRate}%</td>
                        <td>
                            <span className={`badge${learner.status === 'COMPLETED' ? ' ok' : ''}`}>{enrollmentStatus(t)[learner.status]}</span>
                        </td>
                        <td>{formatDateTime(learner.startedAt)}</td>
                        <td>{formatDateTime(learner.lastWatchedAt)}</td>
                        <td>{formatDateTime(learner.completedAt)}</td>
                    </tr>
                ))}
                {data.content.length === 0 && (
                    <tr>
                        <td colSpan={6} className="muted">{t('admin.streaming.noLearners', '아직 재생한 회원이 없습니다.')}</td>
                    </tr>
                )}
                </tbody>
            </table>
            <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>
        </>
    );
}
