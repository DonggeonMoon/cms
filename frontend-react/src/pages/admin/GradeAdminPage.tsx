import {type Grade, gradeApi} from '../../api/grades';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import type {FieldSpec} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'name', label: '등급 이름', required: true, wide: true},
    {name: 'rank', label: '순위', type: 'number', required: true, min: 0, wide: true,
        help: '숫자가 클수록 높은 등급입니다. "○○ 등급 이상"은 이 순위로 비교합니다.'},
    {name: 'isDefault', label: '기본 등급', type: 'checkbox', wide: true,
        help: '등급을 받지 않은 회원은 기본 등급으로 봅니다. 하나만 고를 수 있습니다.'},
    {name: 'description', label: '설명', wide: true},
];

const loadGrades = () => gradeApi.list();

export function GradeAdminPage() {
    return (
        <AdminCrudPage<Grade>
            title="회원 등급"
            description={
                <>
                    <p>게시판·아카이브·컨텐츠·스트리밍·상품·예약마다 "○○ 등급 이상"만 이용하게 제한할 수 있습니다.</p>
                    <p>회원 상세에서 등급을 직접 주거나, 상품에 "결제 시 부여할 등급"을 정해 구매·구독으로 등급을 줄 수 있습니다.
                        기간이 끝나면 원래 등급으로 돌아갑니다.</p>
                </>
            }
            searchable={false}
            load={loadGrades}
            fields={FIELDS}
            emptyForm={{rank: 0, isDefault: false}}
            columns={[
                {label: '순위', render: (g) => g.rank},
                {label: '등급', render: (g) => g.name},
                {label: '기본', render: (g) => <YesNo value={g.isDefault}/>},
                {label: '설명', render: (g) => g.description ?? '-'},
            ]}
            create={async (v) => {
                await gradeApi.create(v);
            }}
            update={async (g, v) => {
                await gradeApi.update(g.id, v);
            }}
            remove={(g) => gradeApi.remove(g.id)}
        />
    );
}
