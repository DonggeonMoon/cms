import {Link, useSearchParams} from 'react-router';
import type {CustomerRecord} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {formatDate} from '../../components/format';
import type {FieldSpec, FormValues} from '../../components/SchemaForm';
import type {FileInfo} from '../../api/types';

const RECORD_FIELDS: FieldSpec[] = [
    {name: 'memberId', label: '회원 ID', type: 'number', required: true,
        help: '회원 상세나 예약 내역에서 들어오면 자동으로 채워집니다.'},
    {name: 'reservationId', label: '관련 예약 번호', type: 'number', help: '선택 사항입니다.'},
    {name: 'recordedOn', label: '기록한 날', type: 'date', required: true},
    {name: 'category', label: '분류', placeholder: '시술, 상담, 진단 등'},
    {name: 'title', label: '제목', required: true, wide: true},
    {name: 'sharedWithMember', label: '회원에게 보여주기', type: 'checkbox', wide: true},
    {name: 'content', label: '내용', type: 'textarea'},
    {name: 'photos', label: '사진', type: 'files'},
];

const photosOf = (record: CustomerRecord): FileInfo[] =>
    record.photoFileIds.map((id, i) => ({
        id, originalName: `사진 ${i + 1}`, extension: '', mimeType: '', mediaType: 'IMAGE', size: 0,
        alternativeText: null, downloadCount: 0, downloadUrl: record.photoUrls[i] ?? '', thumbnailUrl: record.photoUrls[i] ?? null,
        createdAt: record.createdAt,
    }));

const toRecordBody = (values: FormValues) => {
    const {photos, ...rest} = values;
    return {...rest, photoFileIds: ((photos as FileInfo[] | undefined) ?? []).map((file) => file.id)};
};

export function CustomerRecordAdminPage() {
    const [params] = useSearchParams();
    const memberId = params.get('memberId');
    const reservationId = params.get('reservationId');

    return (
        <>
            {memberId && <Link to={`/admin/members/${memberId}`}>← 회원 상세로</Link>}
            <AdminCrudPage<CustomerRecord>
                title="고객 기록"
                description={<p>회원별 시술·상담 이력입니다. 내용은 암호화되어 저장됩니다.</p>}
                load={(query) => adminApi.customerRecords.search({...query, memberId: memberId ?? undefined})}
                fields={RECORD_FIELDS}
                emptyForm={{
                    memberId: memberId ?? '', reservationId: reservationId ?? '',
                    recordedOn: new Date().toISOString().slice(0, 10), sharedWithMember: false, photos: [],
                }}
                toForm={(r) => ({...r, photos: photosOf(r)})}
                columns={[
                    {label: '날짜', render: (r) => formatDate(r.recordedOn)},
                    {label: '회원', render: (r) => <Link to={`/admin/members/${r.memberId}`}>#{r.memberId}</Link>},
                    {label: '예약', render: (r) => r.reservationId ? `#${r.reservationId}` : '-'},
                    {label: '분류', render: (r) => r.category || '-'},
                    {label: '제목', render: (r) => r.title},
                    {label: '회원에게 공개', render: (r) => <YesNo value={r.sharedWithMember}/>},
                    {label: '사진', render: (r) => r.photoUrls.length > 0 ? `${r.photoUrls.length}장` : '-'},
                    {label: '작성자', render: (r) => r.authorName || '-'},
                ]}
                create={async (v) => { await adminApi.customerRecords.create(toRecordBody(v)); }}
                update={async (r, v) => { await adminApi.customerRecords.update(r.id, toRecordBody(v)); }}
                remove={(r) => adminApi.customerRecords.remove(r.id)}
            />
        </>
    );
}
