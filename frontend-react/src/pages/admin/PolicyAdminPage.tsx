import {useEffect, useState} from 'react';
import {useParams} from 'react-router';
import type {Policy, PolicyType} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {SchemaForm} from '../../components/SchemaForm';

const WHERE_SHOWN: Record<PolicyType, string> = {
    PRIVACY_CONSENT: '회원가입 첫 단계의 동의 화면에 표시되며, 동의해야 가입할 수 있습니다.',
    PRIVACY_POLICY: '사용자 화면 하단(푸터)의 "개인정보처리방침" 링크에서 표시됩니다.',
    MAIL_RECEPTION: '회원가입·마이페이지의 "이메일 수신 동의" 선택 항목에서 표시됩니다. 사용하지 않으면 항목이 숨겨지고 동의를 받지 않습니다.',
    SMS_RECEPTION: '회원가입·마이페이지의 "SMS 수신 동의" 선택 항목에서 표시됩니다. 사용하지 않으면 항목이 숨겨지고 동의를 받지 않습니다.',
    MARKETING_USAGE: '회원가입·마이페이지의 "마케팅 활용 동의" 선택 항목에서 표시됩니다. 사용하지 않으면 항목이 숨겨지고 동의를 받지 않습니다.',
};

const TYPES: Record<string, PolicyType> = {
    'privacy-consent': 'PRIVACY_CONSENT',
    'privacy-policy': 'PRIVACY_POLICY',
    'mail-reception': 'MAIL_RECEPTION',
    'sms-reception': 'SMS_RECEPTION',
    'marketing-usage': 'MARKETING_USAGE',
};

export function PolicyAdminPage() {
    const type = TYPES[useParams().type ?? ''] ?? 'PRIVACY_CONSENT';
    const [policy, setPolicy] = useState<Policy | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        setPolicy(null);
        setSaved(false);
        adminApi.policies.get(type).then(setPolicy).catch(setError);
    }, [type]);

    if (error && !policy) {
        return <ErrorBanner error={error}/>;
    }
    if (!policy) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <section className="card">
            <h1>{policy.title}</h1>
            <p className="muted page-description">{WHERE_SHOWN[type]}</p>
            <p className={policy.version === 0 ? 'muted small' : 'muted small align-right'}>
                {policy.version === 0
                    ? '아직 작성되지 않아 기본 문구가 표시되고 있습니다. 실제 내용으로 수정해 저장해 주세요.'
                    : `v${policy.version} · ${policy.lastWorker ?? '-'} · ${formatDateTime(policy.modifiedAt)} 수정`}
            </p>
            <SchemaForm
                key={`${type}-${policy.version}`}
                fields={[
                    ...(policy.optional ? [{name: 'isUsing', label: '사용', type: 'checkbox', wide: true, spaced: true} as const] : []),
                    {name: 'contentHtml', label: '내용', type: 'html', required: true},
                ]}
                initial={{contentHtml: policy.contentHtml, isUsing: policy.isUsing}}
                onSubmit={async (values) => {
                    setSaved(false);
                    setPolicy(await adminApi.policies.save(type, String(values.contentHtml ?? ''), values.isUsing !== false));
                    setSaved(true);
                }}
            />
            {saved && <p className="ok">저장했습니다.</p>}
        </section>
    );
}
