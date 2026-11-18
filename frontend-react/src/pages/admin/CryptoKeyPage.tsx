import {useEffect, useState} from 'react';
import type {CryptoAlgorithm, CryptoKey, ReencryptResult} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {ReauthGate, type ReauthGuard} from '../../components/ReauthGate';
import {StableLabel} from '../../components/StableLabel';
import {useI18n} from '../../api/i18n';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const algorithms = (t: T): { value: CryptoAlgorithm; label: string }[] => [
    {value: 'AES', label: t('admin.cryptoKey.algorithm.aes', 'AES (국제 표준)')},
    {value: 'SEED', label: t('admin.cryptoKey.algorithm.seed', 'SEED (KISA)')},
    {value: 'ARIA', label: t('admin.cryptoKey.algorithm.aria', 'ARIA (국가 표준)')},
];

export function CryptoKeyPage() {
    const {t} = useI18n();
    return <ReauthGate title={t('admin.nav.cryptoKey', '암호화 키 관리')}>{(guard) => <CryptoKeyPanel guard={guard}/>}</ReauthGate>;
}

function CryptoKeyPanel({guard}: { guard: ReauthGuard }) {
    const {t} = useI18n();
    const [key, setKey] = useState<CryptoKey | null>(null);
    const [algorithm, setAlgorithm] = useState<CryptoAlgorithm>('AES');
    const [shown, setShown] = useState(false);
    const [busy, setBusy] = useState(false);
    const [notice, setNotice] = useState<string | null>(null);
    const [error, setError] = useState<unknown>(null);

    const apply = (loaded: CryptoKey) => {
        setKey(loaded);
        setAlgorithm(loaded.algorithm ?? 'AES');
    };

    useEffect(() => {
        guard(adminApi.cryptoKey.get()).then(apply).catch(setError);
    }, [guard]);

    const run = async (action: () => Promise<ReencryptResult>, done: string) => {
        setBusy(true);
        setError(null);
        setNotice(null);
        try {
            const result = await guard(action());
            apply(result.key);
            setNotice(t('admin.cryptoKey.reencryptedNotice', '{done} 저장된 개인정보 {count}건을 다시 암호화했습니다.', {done, count: result.reencryptedValues.toLocaleString('ko-KR')}));
        } catch (e) {
            setError(e);
        } finally {
            setBusy(false);
        }
    };

    const regenerate = () => {
        if (window.confirm(t('admin.cryptoKey.confirmRegenerate', '새 키를 만들고 모든 개인정보를 복호화한 뒤 새 키로 다시 암호화합니다. 계속할까요?'))) {
            void run(adminApi.cryptoKey.regenerate, t('admin.cryptoKey.regeneratedNotice', '새 키를 만들었습니다.'));
        }
    };

    const changeAlgorithm = () => {
        if (window.confirm(t('admin.cryptoKey.confirmChangeAlgorithm', '알고리즘을 {algorithm}(으)로 바꾸고 모든 개인정보를 다시 암호화합니다. 계속할까요?', {algorithm}))) {
            void run(() => adminApi.cryptoKey.changeAlgorithm(algorithm), t('admin.cryptoKey.changedAlgorithmNotice', '알고리즘을 {algorithm}(으)로 바꿨습니다.', {algorithm}));
        }
    };

    return (
        <section className="card">
            <h1>{t('admin.nav.cryptoKey', '암호화 키 관리')}</h1>
            <div className="muted page-description">
                <p>{t('admin.cryptoKey.description1', '회원·관리자·비회원의 이름, 이메일, 연락처, 주소와 주문·예약의 수령인·신청자 정보는 이 키로 암호화되어 저장됩니다. 로그인 아이디는 검색과 로그인을 위해 암호화하지 않습니다.')}</p>
                <br />
                <p>{t('admin.cryptoKey.description2', '키는 관리자가 처음 로그인할 때 무작위 UUID로 만들어지고, 서버 환경변수의 마스터키로 한 번 더 감싸 보관됩니다. 키를 다시 만들거나 알고리즘을 바꾸면 저장된 값을 모두 복호화한 뒤 새 설정으로 다시 암호화합니다.')}</p>
            </div>
            <ErrorBanner error={error}/>
            {notice && <p className="notice" role="status">{notice}</p>}
            {key && !key.initialized && (
                <div className="toolbar">
                    <p className="muted">{t('admin.cryptoKey.noKeyYet', '아직 암호화 키가 없습니다.')}</p>
                    <button type="button" className="primary push-right" disabled={busy} onClick={regenerate}>{t('admin.cryptoKey.generateKey', '키 생성')}</button>
                </div>
            )}
            {key?.initialized && (
                <>
                    <dl className="key-details">
                        <dt>{t('admin.cryptoKey.algorithmLabel', '알고리즘')}</dt>
                        <dd>{key.algorithm}</dd>
                        <dt>{t('admin.cryptoKey.theKey', '암호화 키')}</dt>
                        <dd>
                            <code>{shown ? key.key : '••••••••-••••-••••-••••-••••••••••••'}</code>
                            <button type="button" className="link-button" onClick={() => setShown((v) => !v)}>
                                <StableLabel current={shown ? t('admin.cryptoKey.hide', '숨기기') : t('admin.cryptoKey.show', '보기')} labels={[t('admin.cryptoKey.hide', '숨기기'), t('admin.cryptoKey.show', '보기')]}/>
                            </button>
                        </dd>
                        <dt>{t('admin.cryptoKey.createdAt', '처음 생성')}</dt>
                        <dd>{formatDateTime(key.createdAt)}</dd>
                        <dt>{t('admin.cryptoKey.modifiedAt', '마지막 변경')}</dt>
                        <dd>{formatDateTime(key.modifiedAt)}</dd>
                    </dl>
                    <div className="toolbar">
                        <select aria-label={t('admin.cryptoKey.algorithmAriaLabel', '암호화 알고리즘')} value={algorithm} disabled={busy}
                                onChange={(e) => setAlgorithm(e.target.value as CryptoAlgorithm)}>
                            {algorithms(t).map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                        </select>
                        <button type="button" disabled={busy || algorithm === key.algorithm} onClick={changeAlgorithm}>
                            {t('admin.cryptoKey.changeAlgorithm', '알고리즘 변경')}
                        </button>
                        <button type="button" className="danger push-right" disabled={busy} onClick={regenerate}>
                            <StableLabel current={busy ? t('admin.cryptoKey.processing', '처리 중…') : t('admin.cryptoKey.regenerateKey', '키 재생성')}
                                         labels={[t('admin.cryptoKey.processing', '처리 중…'), t('admin.cryptoKey.regenerateKey', '키 재생성')]}/>
                        </button>
                    </div>
                </>
            )}
        </section>
    );
}
