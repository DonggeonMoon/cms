import {Link, useLocation} from 'react-router';

export function MemberOnlyNotice({message}: { message: string }) {
    const location = useLocation();
    return (
        <p className="notice-box">
            {message}{' '}
            <Link to="/login" state={{from: location.pathname}}>로그인</Link> 또는 <Link to="/join">회원가입</Link> 후 이용해 주세요.
        </p>
    );
}
