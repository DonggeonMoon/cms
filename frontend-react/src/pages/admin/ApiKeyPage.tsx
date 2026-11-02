import {useCallback} from 'react';
import type {Query} from '../../api/client';
import type {ApiKey} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage} from '../../components/AdminCrudPage';
import {formatDateTime} from '../../components/format';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';
import type {FieldSpec} from '../../components/SchemaForm';

const API_KEY_FIELDS: FieldSpec[] = [
    {name: 'name', label: '키 이름', required: true, placeholder: '카카오맵 JavaScript 키',
        help: '지도 설정처럼 API 키가 필요한 곳에서 이 이름으로 고릅니다.'},
    {name: 'value', label: '키 값', required: true, wide: true},
    {name: 'description', label: '설명', wide: true, placeholder: '발급처, 등록한 도메인 등'},
];

const maskApiKey = (value: string) => (value.length <= 4 ? '••••' : `${value.slice(0, 4)}${'•'.repeat(8)}`);

export function ApiKeyPage() {
    return <ReauthGate title="API 키 관리">{(guard) => <ApiKeyList guard={guard}/>}</ReauthGate>;
}

function ApiKeyList({guard}: { guard: ReauthGuard }) {
    const load = useCallback((query: Query) => guard(adminApi.apiKeys.search(query)), [guard]);
    return (
        <AdminCrudPage<ApiKey>
            title="API 키 관리"
            description="외부 서비스의 API 키를 이름을 붙여 등록합니다. 키 값은 암호화되어 저장되고, 사용하는 화면에서는 키 이름으로 선택합니다."
            load={load}
            fields={API_KEY_FIELDS}
            emptyForm={{}}
            columns={[
                {label: '키 이름', render: (k) => k.name},
                {label: '키 값', render: (k) => <code>{maskApiKey(k.value)}</code>},
                {label: '설명', render: (k) => k.description || '-'},
                {label: '마지막 변경', render: (k) => formatDateTime(k.modifiedAt)},
            ]}
            create={(v) => guard(adminApi.apiKeys.create(v))}
            update={(k, v) => guard(adminApi.apiKeys.update(k.id, v))}
            remove={(k) => guard(adminApi.apiKeys.remove(k.id))}
        />
    );
}
