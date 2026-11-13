import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {request} from './client';

export const LANGUAGE_NAMES: Record<string, string> = {
    ko: '한국어', en: 'English', ja: '日本語', zh: '中文', vi: 'Tiếng Việt', th: 'ไทย', id: 'Bahasa Indonesia',
    es: 'Español', fr: 'Français', de: 'Deutsch', ru: 'Русский',
};

export interface I18nSetting {
    languages: string[];
    defaultLanguage: string;
}

export interface Translation {
    id: number;
    key: string;
    message: string;
}

export const i18nApi = {
    setting: () => request<I18nSetting>('/api/i18n'),
    messages: (language: string) => request<Record<string, string>>(`/api/i18n/messages/${language}`),
    updateSetting: (body: I18nSetting) => request<I18nSetting>('/api/admin/i18n', {method: 'PUT', body: {...body}}),
    translations: (language: string) => request<Translation[]>(`/api/admin/i18n/translations/${language}`),
    createTranslation: (language: string, key: string, message: string) =>
        request<Translation>(`/api/admin/i18n/translations/${language}`, {method: 'POST', body: {key, message}}),
    updateTranslation: (id: number, key: string, message: string) =>
        request<Translation>(`/api/admin/i18n/translations/${id}`, {method: 'PUT', body: {key, message}}),
    removeTranslation: (id: number) => request<void>(`/api/admin/i18n/translations/${id}`, {method: 'DELETE'}),
};

export const DEFAULT_MESSAGES: Record<string, string> = {
    'nav.login': '로그인',
    'nav.mypage': '마이페이지',
    'nav.cart': '장바구니',
    'nav.notifications': '알림',
    'mypage.points': '포인트',
    'mypage.coupons': '쿠폰함',
    'mypage.downloads': '내려받기',
    'mypage.courses': '교육 과정',
    'nav.logout': '로그아웃',
    'nav.greeting': '{name}님',
    'mypage.profile': '회원정보',
    'mypage.orders': '주문 내역',
    'mypage.subscriptions': '구독',
    'mypage.passes': '이용권',
    'mypage.reservations': '예약 내역',
    'mypage.records': '이용 기록',
    'mypage.wishlist': '찜한 상품',
    'mypage.applications': '신청 내역',
    'mypage.repairs': '수리·AS 접수',
    'mypage.reports': '신고',
    'mypage.streaming': '수강 내역',
    'mypage.password': '비밀번호 변경',
    'mypage.quit': '회원 탈퇴',
    'mypage.notifications': '알림',
    'mypage.wallet': '지갑',
    'mypage.licenseKeys': '라이선스 키',
    'mypage.votes': '투표',
    'footer.terms': '이용약관',
    'footer.privacy': '개인정보처리방침',
    'footer.operationPolicy': '운영정책',
    'footer.youthProtection': '청소년보호정책',
    'language.label': '언어',
};

/**
 * DEFAULT_MESSAGES 키들의 지원 언어별 번역. ko는 DEFAULT_MESSAGES 자체가 원문이라 여기 없음.
 * menu.*, site.name 처럼 사용자가 입력한 값은 미리 번역해 둘 수 없어 포함하지 않는다.
 */
export const DEFAULT_TRANSLATIONS: Record<string, Partial<Record<string, string>>> = {
    'nav.login': {
        en: 'Log in', ja: 'ログイン', zh: '登录', vi: 'Đăng nhập', th: 'เข้าสู่ระบบ', id: 'Masuk',
        es: 'Iniciar sesión', fr: 'Se connecter', de: 'Anmelden', ru: 'Войти',
    },
    'nav.mypage': {
        en: 'My Page', ja: 'マイページ', zh: '我的页面', vi: 'Trang của tôi', th: 'หน้าของฉัน', id: 'Halaman Saya',
        es: 'Mi página', fr: 'Mon espace', de: 'Mein Bereich', ru: 'Личная страница',
    },
    'nav.cart': {
        en: 'Cart', ja: 'カート', zh: '购物车', vi: 'Giỏ hàng', th: 'ตะกร้าสินค้า', id: 'Keranjang',
        es: 'Carrito', fr: 'Panier', de: 'Warenkorb', ru: 'Корзина',
    },
    'nav.logout': {
        en: 'Log out', ja: 'ログアウト', zh: '退出登录', vi: 'Đăng xuất', th: 'ออกจากระบบ', id: 'Keluar',
        es: 'Cerrar sesión', fr: 'Se déconnecter', de: 'Abmelden', ru: 'Выйти',
    },
    'nav.greeting': {
        en: 'Hi, {name}', ja: '{name}さん', zh: '{name}，您好', vi: 'Chào {name}', th: 'สวัสดี {name}', id: 'Hai, {name}',
        es: 'Hola, {name}', fr: 'Bonjour, {name}', de: 'Hallo, {name}', ru: 'Привет, {name}',
    },
    'nav.notifications': {
        en: 'Notifications', ja: '通知', zh: '通知', vi: 'Thông báo', th: 'การแจ้งเตือน', id: 'Notifikasi',
        es: 'Notificaciones', fr: 'Notifications', de: 'Benachrichtigungen', ru: 'Уведомления',
    },
    'mypage.points': {
        en: 'Points', ja: 'ポイント', zh: '积分', vi: 'Điểm', th: 'แต้มสะสม', id: 'Poin',
        es: 'Puntos', fr: 'Points', de: 'Punkte', ru: 'Баллы',
    },
    'mypage.coupons': {
        en: 'Coupons', ja: 'クーポン', zh: '优惠券', vi: 'Phiếu giảm giá', th: 'คูปอง', id: 'Kupon',
        es: 'Cupones', fr: 'Coupons', de: 'Gutscheine', ru: 'Купоны',
    },
    'mypage.downloads': {
        en: 'Downloads', ja: 'ダウンロード', zh: '下载内容', vi: 'Tải xuống', th: 'การดาวน์โหลด', id: 'Unduhan',
        es: 'Descargas', fr: 'Téléchargements', de: 'Downloads', ru: 'Загрузки',
    },
    'mypage.courses': {
        en: 'Courses', ja: '講座', zh: '课程', vi: 'Khóa học', th: 'คอร์สเรียน', id: 'Kursus',
        es: 'Cursos', fr: 'Cours', de: 'Kurse', ru: 'Курсы',
    },
    'mypage.profile': {
        en: 'Profile', ja: '会員情報', zh: '个人信息', vi: 'Thông tin cá nhân', th: 'ข้อมูลส่วนตัว', id: 'Profil',
        es: 'Perfil', fr: 'Profil', de: 'Profil', ru: 'Профиль',
    },
    'mypage.orders': {
        en: 'Orders', ja: '注文履歴', zh: '订单记录', vi: 'Lịch sử đặt hàng', th: 'ประวัติการสั่งซื้อ', id: 'Riwayat Pesanan',
        es: 'Pedidos', fr: 'Commandes', de: 'Bestellungen', ru: 'Заказы',
    },
    'mypage.subscriptions': {
        en: 'Subscriptions', ja: 'サブスクリプション', zh: '订阅', vi: 'Đăng ký định kỳ', th: 'การสมัครสมาชิก', id: 'Berlangganan',
        es: 'Suscripciones', fr: 'Abonnements', de: 'Abonnements', ru: 'Подписки',
    },
    'mypage.passes': {
        en: 'Passes', ja: '利用券', zh: '使用券', vi: 'Gói sử dụng', th: 'บัตรใช้บริการ', id: 'Paket',
        es: 'Bonos', fr: 'Pass', de: 'Pässe', ru: 'Абонементы',
    },
    'mypage.reservations': {
        en: 'Reservations', ja: '予約内容', zh: '预约记录', vi: 'Lịch sử đặt chỗ', th: 'ประวัติการจอง', id: 'Riwayat Reservasi',
        es: 'Reservas', fr: 'Réservations', de: 'Reservierungen', ru: 'Бронирования',
    },
    'mypage.records': {
        en: 'Usage History', ja: '利用履歴', zh: '使用记录', vi: 'Lịch sử sử dụng', th: 'ประวัติการใช้งาน', id: 'Riwayat Penggunaan',
        es: 'Historial de uso', fr: "Historique d'utilisation", de: 'Nutzungsverlauf', ru: 'История использования',
    },
    'mypage.wishlist': {
        en: 'Wishlist', ja: 'お気に入り', zh: '收藏商品', vi: 'Sản phẩm yêu thích', th: 'รายการที่ชอบ', id: 'Daftar Keinginan',
        es: 'Lista de deseos', fr: 'Liste de souhaits', de: 'Wunschliste', ru: 'Избранное',
    },
    'mypage.applications': {
        en: 'Applications', ja: '申請内容', zh: '申请记录', vi: 'Lịch sử đăng ký', th: 'ประวัติการสมัคร', id: 'Riwayat Pengajuan',
        es: 'Solicitudes', fr: 'Candidatures', de: 'Anträge', ru: 'Заявки',
    },
    'mypage.repairs': {
        en: 'Repair Requests', ja: '修理・AS受付', zh: '维修/售后申请', vi: 'Yêu cầu sửa chữa', th: 'การแจ้งซ่อม', id: 'Permintaan Perbaikan',
        es: 'Solicitudes de reparación', fr: 'Demandes de réparation', de: 'Reparaturanfragen', ru: 'Заявки на ремонт',
    },
    'mypage.reports': {
        en: 'Reports', ja: '通報', zh: '举报', vi: 'Báo cáo', th: 'การรายงาน', id: 'Laporan',
        es: 'Reportes', fr: 'Signalements', de: 'Meldungen', ru: 'Жалобы',
    },
    'mypage.streaming': {
        en: 'Streaming History', ja: '受講履歴', zh: '观看记录', vi: 'Lịch sử xem', th: 'ประวัติการรับชม', id: 'Riwayat Tontonan',
        es: 'Historial de reproducción', fr: 'Historique de visionnage', de: 'Wiedergabeverlauf', ru: 'История просмотров',
    },
    'mypage.password': {
        en: 'Change Password', ja: 'パスワード変更', zh: '修改密码', vi: 'Đổi mật khẩu', th: 'เปลี่ยนรหัสผ่าน', id: 'Ubah Kata Sandi',
        es: 'Cambiar contraseña', fr: 'Changer le mot de passe', de: 'Passwort ändern', ru: 'Изменить пароль',
    },
    'mypage.quit': {
        en: 'Delete Account', ja: '退会', zh: '注销会员', vi: 'Hủy tài khoản', th: 'ยกเลิกสมาชิก', id: 'Hapus Akun',
        es: 'Eliminar cuenta', fr: 'Supprimer le compte', de: 'Konto löschen', ru: 'Удалить аккаунт',
    },
    'mypage.notifications': {
        en: 'Notifications', ja: '通知', zh: '通知', vi: 'Thông báo', th: 'การแจ้งเตือน', id: 'Notifikasi',
        es: 'Notificaciones', fr: 'Notifications', de: 'Benachrichtigungen', ru: 'Уведомления',
    },
    'mypage.wallet': {
        en: 'Wallet', ja: 'ウォレット', zh: '钱包', vi: 'Ví điện tử', th: 'กระเป๋าเงิน', id: 'Dompet',
        es: 'Billetera', fr: 'Portefeuille', de: 'Wallet', ru: 'Кошелёк',
    },
    'mypage.licenseKeys': {
        en: 'License Keys', ja: 'ライセンスキー', zh: '许可证密钥', vi: 'Mã bản quyền', th: 'คีย์ใบอนุญาต', id: 'Kunci Lisensi',
        es: 'Claves de licencia', fr: 'Clés de licence', de: 'Lizenzschlüssel', ru: 'Лицензионные ключи',
    },
    'mypage.votes': {
        en: 'Votes', ja: '投票', zh: '投票', vi: 'Bình chọn', th: 'การโหวต', id: 'Voting',
        es: 'Votaciones', fr: 'Votes', de: 'Abstimmungen', ru: 'Голосования',
    },
    'footer.terms': {
        en: 'Terms of Service', ja: '利用規約', zh: '服务条款', vi: 'Điều khoản dịch vụ', th: 'ข้อกำหนดการใช้บริการ', id: 'Ketentuan Layanan',
        es: 'Términos de servicio', fr: "Conditions d'utilisation", de: 'Nutzungsbedingungen', ru: 'Условия использования',
    },
    'footer.privacy': {
        en: 'Privacy Policy', ja: '個人情報処理方針', zh: '隐私政策', vi: 'Chính sách bảo mật', th: 'นโยบายความเป็นส่วนตัว', id: 'Kebijakan Privasi',
        es: 'Política de privacidad', fr: 'Politique de confidentialité', de: 'Datenschutzrichtlinie', ru: 'Политика конфиденциальности',
    },
    'footer.operationPolicy': {
        en: 'Operation Policy', ja: '運営方針', zh: '运营政策', vi: 'Chính sách vận hành', th: 'นโยบายการดำเนินงาน', id: 'Kebijakan Operasional',
        es: 'Política de operación', fr: "Politique d'exploitation", de: 'Betriebsrichtlinie', ru: 'Политика работы сервиса',
    },
    'footer.youthProtection': {
        en: 'Youth Protection Policy', ja: '青少年保護方針', zh: '青少年保护政策', vi: 'Chính sách bảo vệ trẻ vị thành niên',
        th: 'นโยบายคุ้มครองเยาวชน', id: 'Kebijakan Perlindungan Anak', es: 'Política de protección juvenil',
        fr: 'Politique de protection des mineurs', de: 'Jugendschutzrichtlinie', ru: 'Политика защиты несовершеннолетних',
    },
    'language.label': {
        en: 'Language', ja: '言語', zh: '语言', vi: 'Ngôn ngữ', th: 'ภาษา', id: 'Bahasa',
        es: 'Idioma', fr: 'Langue', de: 'Sprache', ru: 'Язык',
    },
};

/** 기본 키 하나를 특정 언어로 번역한 문구. 번역이 없으면 한국어 원문으로 돌아간다. */
export function defaultMessageFor(key: string, language: string): string {
    return DEFAULT_TRANSLATIONS[key]?.[language] ?? DEFAULT_MESSAGES[key];
}

interface I18nState {
    language: string;
    languages: string[];
    setLanguage: (language: string) => void;
    t: (key: string, fallback: string, values?: Record<string, string>) => string;
}

const STORAGE_KEY = 'cms.language';

const format = (text: string, values?: Record<string, string>) =>
    values ? text.replace(/\{(\w+)}/g, (match, name: string) => values[name] ?? match) : text;

const I18nContext = createContext<I18nState>({
    language: 'ko',
    languages: ['ko'],
    setLanguage: () => undefined,
    t: (_key, fallback, values) => format(fallback, values),
});

function stored(): string | null {
    try {
        return localStorage.getItem(STORAGE_KEY);
    } catch {
        return null;
    }
}

export function I18nProvider({children}: { children: ReactNode }) {
    const [setting, setSetting] = useState<I18nSetting>({languages: ['ko'], defaultLanguage: 'ko'});
    const [language, setLanguageState] = useState<string>(stored() ?? 'ko');
    const [messages, setMessages] = useState<Record<string, string>>({});

    useEffect(() => {
        i18nApi.setting().then((loaded) => {
            if (loaded && Array.isArray(loaded.languages)) {
                setSetting(loaded);
                setLanguageState((current) => (loaded.languages.includes(current) ? current : loaded.defaultLanguage));
            }
        }).catch(() => undefined);
    }, []);

    useEffect(() => {
        document.documentElement.lang = language;
        i18nApi.messages(language).then((loaded) => setMessages(loaded && typeof loaded === 'object' ? loaded : {})).catch(() => setMessages({}));
    }, [language]);

    const setLanguage = useCallback((next: string) => {
        setLanguageState(next);
        try {
            localStorage.setItem(STORAGE_KEY, next);
        } catch {
            // 저장할 수 없으면 이번 화면에서만 바뀐다.
        }
    }, []);

    const value = useMemo<I18nState>(() => ({
        language,
        languages: setting.languages,
        setLanguage,
        t: (key, fallback, values) => format(messages[key] ?? fallback, values),
    }), [language, setting.languages, setLanguage, messages]);

    return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nState {
    return useContext(I18nContext);
}
