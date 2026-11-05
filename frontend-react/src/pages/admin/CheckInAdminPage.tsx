import {type FormEvent, useRef, useState} from 'react';
import type {Reservation} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {reservationUsage} from '../../components/reservationUsage';

export function CheckInAdminPage() {
    const [code, setCode] = useState('');
    const [result, setResult] = useState<Reservation | null>(null);
    const [error, setError] = useState<unknown>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        if (!code.trim()) {
            return;
        }
        setError(null);
        try {
            setResult(await adminApi.reservations.checkIn(code.trim()));
        } catch (e) {
            setResult(null);
            setError(e);
        } finally {
            setCode('');
            inputRef.current?.focus();
        }
    };

    return (
        <section className="card">
            <h1>체크인</h1>
            <p className="muted">회원이 보여 준 체크인 코드나 QR의 코드 값을 입력하거나 스캐너로 찍어 넣으세요.</p>
            <form className="toolbar" onSubmit={submit}>
                <input ref={inputRef} autoFocus aria-label="체크인 코드" placeholder="체크인 코드" value={code}
                       onChange={(e) => setCode(e.target.value)}/>
                <button type="submit" className="primary">체크인</button>
            </form>
            <ErrorBanner error={error}/>
            {result && (
                <div className="card">
                    <p className="price">체크인 완료 · {formatDateTime(result.checkedInAt)}</p>
                    <p>{result.applicantName} ({result.phoneNumber})</p>
                    <p className="muted">{result.programName} · {reservationUsage(result)}</p>
                </div>
            )}
        </section>
    );
}
