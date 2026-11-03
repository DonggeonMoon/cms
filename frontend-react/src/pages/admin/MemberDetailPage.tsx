import {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router';
import {memberAdminApi} from '../../api/members';
import type {MemberResponse} from '../../api/types';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';

export function MemberDetailPage() {
    const {id} = useParams();
    const [member, setMember] = useState<MemberResponse | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        memberAdminApi.get(Number(id)).then(setMember).catch(setError);
    }, [id]);

    if (error) {
        return <ErrorBanner error={error}/>;
    }
    if (!member) {
        return <p className="muted">불러오는 중…</p>;
    }

    const rows: [string, string | null | undefined][] = [
        ['아이디', member.username],
        ['닉네임', member.nickname],
        ['이름', member.name],
        ['생년월일', member.birthDate],
        ['성별', member.gender],
        ['이메일', member.email],
        ['전화번호', member.telephoneNumber],
        ['휴대폰', member.mobilePhoneNumber],
        ['주소', [member.postalCode && `(${member.postalCode})`, member.address, member.detailAddress].filter(Boolean).join(' ')],
        ['직장', [member.officeName, member.officeDepartment, member.officePosition].filter(Boolean).join(' / ')],
        ['수신 동의', [
            member.isMailReceptionAgreed && '메일',
            member.isSmsReceptionAgreed && 'SMS',
            member.isMarketingUsageAgreed && '마케팅',
        ].filter(Boolean).join(', ')],
        ['로그인 실패 횟수', String(member.loginTryCount)],
        ['가입일', formatDateTime(member.createdAt)],
        ['마지막 로그인', formatDateTime(member.lastLoginDateTime)],
    ];

    return (
        <section className="card">
            <h1>회원 상세</h1>
            <dl className="meta">
                {rows.map(([label, value]) => (
                    <div key={label} className="meta-row">
                        <dt>{label}</dt>
                        <dd>{value || '-'}</dd>
                    </div>
                ))}
            </dl>
            <Link to="/admin/members">목록으로</Link>
        </section>
    );
}
