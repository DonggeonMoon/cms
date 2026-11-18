import {type FormEvent, useState} from 'react';
import {currentPostcodeProvider, openPostcodeSearch, searchJusoAddress, type PostcodeResult} from './postcode';
import {ErrorBanner} from './ErrorBanner';
import {useI18n} from '../api/i18n';

interface Props {
    onFound: (result: PostcodeResult) => void;
    onError?: (error: unknown) => void;
}

export function PostcodeButton({onFound, onError}: Props) {
    const {t} = useI18n();
    const [searching, setSearching] = useState(false);
    return (
        <>
            <button type="button" onClick={async () => {
                try {
                    if (await currentPostcodeProvider() === 'JUSO') {
                        setSearching(true);
                    } else {
                        onFound(await openPostcodeSearch(t));
                    }
                } catch (e) {
                    onError?.(e);
                }
            }}>
                {t('shop.postcode.find', '우편번호 찾기')}
            </button>
            {searching && (
                <JusoSearchModal
                    onPick={(result) => {
                        setSearching(false);
                        onFound(result);
                    }}
                    onClose={() => setSearching(false)}
                />
            )}
        </>
    );
}

function JusoSearchModal({onPick, onClose}: { onPick: (result: PostcodeResult) => void; onClose: () => void }) {
    const {t} = useI18n();
    const [keyword, setKeyword] = useState('');
    const [results, setResults] = useState<PostcodeResult[] | null>(null);
    const [error, setError] = useState<unknown>(null);

    const search = (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        searchJusoAddress(keyword).then(setResults).catch(setError);
    };

    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="popup modal-popup" role="dialog" aria-modal="true" aria-label={t('shop.postcode.search', '주소 검색')}
                 onClick={(e) => e.stopPropagation()}>
                <h2>{t('shop.postcode.search', '주소 검색')}</h2>
                <form className="toolbar" onSubmit={search}>
                    <input autoFocus aria-label={t('common.searchKeyword', '검색어')} placeholder={t('shop.postcode.searchPlaceholder', '도로명, 지번, 건물명 등')} value={keyword}
                           onChange={(e) => setKeyword(e.target.value)}/>
                    <button type="submit" className="primary" disabled={!keyword.trim()}>{t('common.search', '검색')}</button>
                    <button type="button" onClick={onClose}>{t('common.close', '닫기')}</button>
                </form>
                <ErrorBanner error={error}/>
                {results != null && (
                    results.length === 0 ? <p className="muted">{t('shop.common.noSearchResults', '검색 결과가 없습니다.')}</p> : (
                        <ul className="postcode-results">
                            {results.map((result, i) => (
                                <li key={i}>
                                    <button type="button" onClick={() => onPick(result)}>
                                        [{result.zonecode}] {result.roadAddress} ({result.jibunAddress})
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )
                )}
            </div>
        </div>
    );
}
