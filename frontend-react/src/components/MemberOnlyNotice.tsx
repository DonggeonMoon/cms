import {Link, useLocation} from 'react-router';
import {useI18n} from '../api/i18n';

export function MemberOnlyNotice({message}: { message: string }) {
    const {t} = useI18n();
    const location = useLocation();
    return (
        <p className="notice-box">
            {message}{' '}
            <Link to="/login" state={{from: location.pathname}}>{t('nav.login', '로그인')}</Link> {t('shop.common.or', '또는')} <Link to="/join">{t('shop.common.join', '회원가입')}</Link> {t('shop.common.afterUse', '후 이용해 주세요.')}
        </p>
    );
}
