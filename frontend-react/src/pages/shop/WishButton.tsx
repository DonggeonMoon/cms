import {useEffect, useState} from 'react';
import {shopApi} from '../../api/endpoints';
import {useAuth} from '../../auth/AuthContext';
import {ErrorBanner} from '../../components/ErrorBanner';

export function WishButton({productId}: { productId: number }) {
    const {user} = useAuth();
    const [wished, setWished] = useState<boolean | null>(null);
    const [error, setError] = useState<unknown>(null);
    const member = user?.userType === 'MEMBER';

    useEffect(() => {
        if (member) {
            shopApi.wishlistIds().then((ids) => setWished(ids.includes(productId))).catch(() => setWished(false));
        }
    }, [member, productId]);

    if (!member || wished === null) {
        return null;
    }
    const toggle = async () => {
        setError(null);
        try {
            await (wished ? shopApi.unwish(productId) : shopApi.wish(productId));
            setWished(!wished);
        } catch (e) {
            setError(e);
        }
    };
    return (
        <>
            <button type="button" aria-pressed={wished} className={wished ? 'wish-button active' : 'wish-button'} onClick={toggle}>
                {wished ? '♥ 찜함' : '♡ 찜하기'}
            </button>
            <ErrorBanner error={error}/>
        </>
    );
}
