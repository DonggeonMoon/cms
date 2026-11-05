import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {MarketingCampaign, NotificationChannel} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {gradeNameOf, useGrades} from '../../api/grades';
import type {Page} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {Pagination} from '../../components/Pagination';

const CHANNELS: NotificationChannel[] = ['IN_APP', 'EMAIL', 'SMS'];
const CHANNEL_LABEL: Record<NotificationChannel, string> = {IN_APP: '앱 내', EMAIL: '이메일', SMS: 'SMS'};

export function MarketingAdminPage() {
    const grades = useGrades();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [targetGradeId, setTargetGradeId] = useState('');
    const [channels, setChannels] = useState<Set<NotificationChannel>>(new Set(['IN_APP']));
    const [page, setPage] = useState(0);
    const [data, setData] = useState<Page<MarketingCampaign> | null>(null);
    const [error, setError] = useState<unknown>(null);
    const [busy, setBusy] = useState(false);
    const [sent, setSent] = useState<MarketingCampaign | null>(null);

    const reload = useCallback(() => {
        adminApi.marketing.history(page).then(setData).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    const toggleChannel = (channel: NotificationChannel) => setChannels((current) => {
        const next = new Set(current);
        if (next.has(channel)) {
            next.delete(channel);
        } else {
            next.add(channel);
        }
        return next;
    });

    const send = async (e: FormEvent) => {
        e.preventDefault();
        if (channels.size === 0) {
            return;
        }
        if (!window.confirm('대상 회원에게 바로 발송합니다. 계속할까요?')) {
            return;
        }
        setBusy(true);
        setError(null);
        try {
            setSent(await adminApi.marketing.send(title, content, targetGradeId ? Number(targetGradeId) : null, [...channels]));
            setTitle('');
            setContent('');
            setTargetGradeId('');
            setPage(0);
            reload();
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    };

    return (
        <section className="card">
            <h1>대상 발송</h1>
            <p className="muted">등급으로 대상을 좁혀 회원에게 바로 알림을 보냅니다. 예약 발송은 없고, 등록하면 즉시 보냅니다.</p>
            <ErrorBanner error={error}/>
            <form onSubmit={send} className="inline-form">
                <div className="field">
                    <label htmlFor="campaign-title">제목</label>
                    <input id="campaign-title" value={title} onChange={(e) => setTitle(e.target.value)} required/>
                </div>
                <div className="field">
                    <label htmlFor="campaign-content">내용</label>
                    <textarea id="campaign-content" rows={4} value={content} onChange={(e) => setContent(e.target.value)} required/>
                </div>
                <div className="field">
                    <label htmlFor="campaign-grade">대상 등급</label>
                    <select id="campaign-grade" value={targetGradeId} onChange={(e) => setTargetGradeId(e.target.value)}>
                        <option value="">전체 회원</option>
                        {grades.map((grade) => <option key={grade.id} value={grade.id}>{grade.name}</option>)}
                    </select>
                </div>
                <div className="field">
                    <span>발송 채널</span>
                    {CHANNELS.map((channel) => (
                        <label key={channel} className="checkbox">
                            <input type="checkbox" checked={channels.has(channel)} onChange={() => toggleChannel(channel)}/>
                            {CHANNEL_LABEL[channel]}
                        </label>
                    ))}
                </div>
                <button type="submit" className="primary" disabled={busy}>발송하기</button>
                {sent && <p className="ok">{sent.recipientCount}명에게 발송했습니다.</p>}
            </form>
            <h2>발송 이력</h2>
            <div className="table-wrap">
                <table>
                    <thead><tr><th>제목</th><th>대상</th><th>채널</th><th>발송 수</th><th>발송일</th></tr></thead>
                    <tbody>
                    {data?.content.map((campaign) => (
                        <tr key={campaign.id}>
                            <td>{campaign.title}</td>
                            <td>{campaign.targetGradeId ? `${gradeNameOf(grades, campaign.targetGradeId) ?? ''} 등급` : '전체 회원'}</td>
                            <td>{campaign.channels.map((c) => CHANNEL_LABEL[c]).join(', ')}</td>
                            <td>{campaign.recipientCount}명</td>
                            <td>{formatDateTime(campaign.sentAt)}</td>
                        </tr>
                    ))}
                    {data?.content.length === 0 && <tr><td colSpan={5} className="muted">발송 이력이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {data && <Pagination page={page} totalPages={data.page.totalPages} onChange={setPage}/>}
        </section>
    );
}
