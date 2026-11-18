import {Link, useSearchParams} from 'react-router';
import type {CustomerRecord} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {formatDate} from '../../components/format';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import type {FileInfo} from '../../api/types';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

export const recordContentFields = (t: T): FieldSpec[] => [
    {name: 'recordedOn', label: t('admin.customerRecord.recordedOn', '기록한 날'), type: 'date', required: true},
    {name: 'category', label: t('admin.customerRecord.category', '분류'), placeholder: t('admin.customerRecord.categoryPlaceholder', '시술, 상담, 진단 등')},
    {name: 'title', label: t('admin.board.column.title', '제목'), required: true, wide: true},
    {name: 'sharedWithMember', label: t('admin.customerRecord.sharedWithMember', '회원에게 보여주기'), type: 'checkbox', wide: true},
    {name: 'content', label: t('shop.report.content', '내용'), type: 'textarea'},
    {name: 'photos', label: t('admin.repair.photos', '사진'), type: 'files'},
];

const photosOf = (record: CustomerRecord, t: T): FileInfo[] =>
    record.photoFileIds.map((id, i) => ({
        id, originalName: t('admin.repair.photoOf', '사진 {index}', {index: String(i + 1)}), extension: '', mimeType: '', mediaType: 'IMAGE', size: 0,
        alternativeText: null, downloadCount: 0, downloadUrl: record.photoUrls[i] ?? '', thumbnailUrl: record.photoUrls[i] ?? null,
        createdAt: record.createdAt,
    }));

export const toRecordBody = (values: FormValues) => {
    const {photos, ...rest} = values;
    return {...rest, photoFileIds: ((photos as FileInfo[] | undefined) ?? []).map((file) => file.id)};
};

export function CustomerRecordAdminPage() {
    const {t} = useI18n();
    const [params] = useSearchParams();
    const memberId = params.get('memberId');
    const reservationId = params.get('reservationId');

    return (
        <>
            {memberId && <Link to={`/admin/members/${memberId}`}>{t('admin.customerRecord.backToMember', '← 회원 상세로')}</Link>}
            {reservationId && <Link to="/admin/reservations">{t('admin.customerRecord.backToReservations', '← 예약 내역으로')}</Link>}
            <AdminCrudPage<CustomerRecord>
                title={t('admin.member.customerRecord', '고객 기록')}
                description={<p>{t('admin.customerRecord.description', '회원별 시술·상담 이력입니다. 새 기록은 예약 내역에서 "기록 남기기"로 남깁니다. 내용은 암호화되어 저장됩니다.')}</p>}
                load={(query) => adminApi.customerRecords.search({...query, memberId: memberId ?? undefined, reservationId: reservationId ?? undefined})}
                fields={recordContentFields(t)}
                toForm={(r) => ({...r, photos: photosOf(r, t)})}
                columns={[
                    {label: t('admin.product.stock.date', '날짜'), render: (r) => formatDate(r.recordedOn)},
                    {label: t('admin.nav.members', '회원'), render: (r) => <Link to={`/admin/members/${r.memberId}`}>#{r.memberId}</Link>},
                    {label: t('admin.nav.reservations', '예약'), render: (r) => r.reservationId ? `#${r.reservationId}` : '-'},
                    {label: t('admin.customerRecord.category', '분류'), render: (r) => r.category || '-'},
                    {label: t('admin.board.column.title', '제목'), render: (r) => r.title},
                    {label: t('admin.customerRecord.sharedWithMemberColumn', '회원에게 공개'), render: (r) => <YesNo value={r.sharedWithMember}/>},
                    {label: t('admin.repair.photos', '사진'), render: (r) => r.photoUrls.length > 0 ? t('admin.repair.photoCount', '{count}장', {count: String(r.photoUrls.length)}) : '-'},
                    {label: t('shop.board.author', '작성자'), render: (r) => r.authorName || '-'},
                ]}
                update={async (r, v) => { await adminApi.customerRecords.update(r.id, toRecordBody(v)); }}
                remove={(r) => adminApi.customerRecords.remove(r.id)}
            />
        </>
    );
}
