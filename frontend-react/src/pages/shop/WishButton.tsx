import {useEffect, useState} from 'react';
import {shopApi} from '../../api/endpoints';
import {useAuth} from '../../auth/AuthContext';
import {ErrorBanner} from '../../components/ErrorBanner';
import {useI18n} from '../../api/i18n';

export function WishButton({productId}: { productId: number }) {
    const {t} = useI18n();
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
                {wished ? t('shop.wishButton.wished', '♥ 찜함') : t('shop.wishButton.wish', '♡ 찜하기')}
            </button>
            <ErrorBanner error={error}/>
        </>
    );
}
