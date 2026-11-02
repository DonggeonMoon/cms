import {useEffect, useState} from 'react';
import type {Policy} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {ErrorBanner} from '../components/ErrorBanner';
import {formatDateTime} from '../components/format';
import {HtmlView} from '../components/HtmlView';

export function PrivacyPolicyPage() {
    const [policy, setPolicy] = useState<Policy | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        siteApi.policy('PRIVACY_POLICY').then(setPolicy).catch(setError);
    }, []);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!policy) {
        return <p className="muted">불러오는 중…</p>;
    }
    return (
        <article className="card">
            <h1>{policy.title}</h1>
            {policy.modifiedAt && <p className="muted small">최종 수정 {formatDateTime(policy.modifiedAt)}</p>}
            <HtmlView html={policy.contentHtml}/>
        </article>
    );
}
