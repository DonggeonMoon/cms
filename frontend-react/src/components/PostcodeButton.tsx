import {openPostcodeSearch, type PostcodeResult} from './postcode';

interface Props {
    onFound: (result: PostcodeResult) => void;
    onError?: (error: unknown) => void;
}

export function PostcodeButton({onFound, onError}: Props) {
    return (
        <button type="button" onClick={async () => {
            try {
                onFound(await openPostcodeSearch());
            } catch (e) {
                onError?.(e);
            }
        }}>
            우편번호 찾기
        </button>
    );
}
