import {useEffect, useState} from 'react';
import type {Policy, PolicyType} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime} from '../components/format';
import {HtmlView} from '../components/HtmlView';
import {useI18n} from '../api/i18n';

export function PolicyDocumentPage({type}: { type: PolicyType }) {
    const {t} = useI18n();
    const [policy, setPolicy] = useState<Policy | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        setPolicy(null);
        setError(null);
        siteApi.policy(type).then(setPolicy).catch(setError);
    }, [type]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!policy) {
        return <p className="muted">{t('common.loading', '불러오는 중…')}</p>;
    }
    return (
        <article className="card">
            <h1>{policy.title}</h1>
            {policy.modifiedAt && <p className="muted small">{t('shop.policy.lastModified', '최종 수정 {date}', {date: formatDateTime(policy.modifiedAt)})}</p>}
            <HtmlView html={policy.contentHtml}/>
        </article>
    );
}

export function PrivacyPolicyPage() {
    return <PolicyDocumentPage type="PRIVACY_POLICY"/>;
}
