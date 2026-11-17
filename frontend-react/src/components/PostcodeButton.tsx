import {type FormEvent, useState} from 'react';
import {currentPostcodeProvider, openPostcodeSearch, searchJusoAddress, type PostcodeResult} from './postcode';
import {ErrorBanner} from './ErrorBanner';

interface Props {
    onFound: (result: PostcodeResult) => void;
    onError?: (error: unknown) => void;
}

export function PostcodeButton({onFound, onError}: Props) {
    const [searching, setSearching] = useState(false);
    return (
        <>
            <button type="button" onClick={async () => {
                try {
                    if (await currentPostcodeProvider() === 'JUSO') {
                        setSearching(true);
                    } else {
                        onFound(await openPostcodeSearch());
                    }
                } catch (e) {
                    onError?.(e);
                }
            }}>
                우편번호 찾기
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
            <div className="popup modal-popup" role="dialog" aria-modal="true" aria-label="주소 검색"
                 onClick={(e) => e.stopPropagation()}>
                <h2>주소 검색</h2>
                <form className="toolbar" onSubmit={search}>
                    <input autoFocus aria-label="검색어" placeholder="도로명, 지번, 건물명 등" value={keyword}
                           onChange={(e) => setKeyword(e.target.value)}/>
                    <button type="submit" className="primary" disabled={!keyword.trim()}>검색</button>
                    <button type="button" onClick={onClose}>닫기</button>
                </form>
                <ErrorBanner error={error}/>
                {results != null && (
                    results.length === 0 ? <p className="muted">검색 결과가 없습니다.</p> : (
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
