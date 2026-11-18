import {useCallback, useEffect, useMemo, useState} from 'react';
import type {Query} from '../../api/client';
import {type Banner, type Config, type Content, type Menu, type Site, siteFeatures, SITE_FEATURE_VALUES, type SiteFeature} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {useI18n} from '../../api/i18n';
import {type FieldSpec, type FormValues, SchemaForm} from '../../components/SchemaForm';
import {useSite} from '../../components/SiteContext';

type T = (key: string, fallback: string, values?: Record<string, string>) => string;

const featureField = (feature: SiteFeature) => `feature_${feature}`;

const SEO_GROUP = '검색·공유 (SEO)';

const siteFields = (t: T): FieldSpec[] => [
    {name: 'isDefault', label: t('admin.site.field.isDefault', '기본 사이트'), type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: t('admin.site.field.name', '사이트 이름'), required: true, help: t('admin.site.field.name.help', '사용자 화면의 헤더와 홈에 표시됩니다.')},
    {name: 'description', label: t('admin.site.field.description', '설명'), wide: true, help: t('admin.site.field.description.help', '홈 화면에 소개 문구로 표시됩니다.')},
    {name: 'domain', label: t('admin.site.field.domain', '도메인'), placeholder: 'www.example.com', newRow: true,
        help: t('admin.site.field.domain.help', '참고용 메타데이터입니다. 실제 사이트 구분과 접속 경로는 아래 사이트 코드로 결정됩니다.')},
    {name: 'code', label: t('admin.site.field.code', '사이트 코드'), placeholder: 'branch', newRow: true,
        help: t('admin.site.field.code.help', '영문 소문자·숫자·하이픈만. 지정하면 /s/{코드}/ 주소로 접속합니다. 기본 사이트는 비워 두면 됩니다.')},
    {name: 'locale', label: t('admin.site.field.locale', '로케일'), placeholder: 'ko', newRow: true},
    {name: 'darkModeEnabled', label: t('admin.site.field.darkModeEnabled', '다크 모드 사용'), type: 'checkbox', newRow: true,
        help: t('admin.site.field.darkModeEnabled.help', '켜면 방문자가 시스템·라이트·다크 중에서 고를 수 있고, 끄면 항상 라이트 모드로 보입니다.')},
    ...siteFeatures(t).map((feature): FieldSpec => ({name: featureField(feature.value), label: feature.label, type: 'checkbox', group: t('admin.site.group.features', '기능')})),
    {name: 'faviconFileId', label: t('admin.site.field.faviconFileId', '파비콘'), type: 'image', wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.faviconFileId.help', '브라우저 탭에 보이는 아이콘입니다. 정사각형 PNG(32×32 이상)나 ICO 파일을 권장합니다.')},
    {name: 'seoTitle', label: t('admin.site.field.seoTitle', '검색·공유 제목'), wide: true, group: t('admin.site.group.seo', SEO_GROUP), placeholder: '사이트 이름 | 한 줄 소개',
        help: t('admin.site.field.seoTitle.help', '브라우저 제목, 검색 결과, 링크 미리보기에 쓰입니다. 비워두면 사이트 이름을 씁니다.')},
    {name: 'metaDescription', label: t('admin.site.field.metaDescription', '설명 (meta description)'), type: 'textarea', group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.metaDescription.help', '검색 결과와 링크 미리보기에 보이는 문구입니다. 80~160자를 권장합니다.')},
    {name: 'metaKeywords', label: t('admin.site.field.metaKeywords', '키워드'), wide: true, group: t('admin.site.group.seo', SEO_GROUP), placeholder: '카페, 원두, 핸드드립'},
    {name: 'ogImageFileId', label: t('admin.site.field.ogImageFileId', '공유 미리보기 이미지 (OG 이미지)'), type: 'image', wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.ogImageFileId.help', '카카오톡·페이스북 등에 링크를 붙였을 때 보이는 이미지입니다. 1200×630 크기를 권장합니다.')},
    {name: 'searchIndexing', label: t('admin.site.field.searchIndexing', '검색엔진 노출 허용'), type: 'checkbox', wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.searchIndexing.help', '끄면 검색엔진이 사이트를 수집하지 않도록 robots.txt 와 메타 태그로 알립니다.')},
    {name: 'googleVerification', label: t('admin.site.field.googleVerification', '구글 서치 콘솔 확인 코드'), wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.googleVerification.help', 'google-site-verification 메타 태그의 content 값만 넣습니다.')},
    {name: 'naverVerification', label: t('admin.site.field.naverVerification', '네이버 서치어드바이저 확인 코드'), wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.naverVerification.help', 'naver-site-verification 메타 태그의 content 값만 넣습니다. 사이트맵은 /sitemap.xml 입니다.')},
    {name: 'customRobotsTxt', label: t('admin.site.field.customRobotsTxt', 'robots.txt 직접 입력'), type: 'textarea', wide: true, group: t('admin.site.group.seo', SEO_GROUP),
        help: t('admin.site.field.customRobotsTxt.help', '비워두면 위 "검색엔진 노출 허용" 설정에 따라 자동 생성됩니다. 입력하면 그 내용을 그대로 robots.txt로 내려줍니다.')},
];

const ALL_FEATURES_ON = Object.fromEntries(SITE_FEATURE_VALUES.map((value) => [featureField(value), true]));

const siteToForm = (site: Site): FormValues => ({
    ...site,
    ...Object.fromEntries(SITE_FEATURE_VALUES.map((value) => [featureField(value), site.features[value] ?? true])),
});

const toSiteBody = (values: FormValues) => ({
    ...values,
    homeContentId: values.homeContentId ? Number(values.homeContentId) : null,
    disabledFeatures: SITE_FEATURE_VALUES.filter((value) => !values[featureField(value)]),
});

const loadSites = (query: Query) => adminApi.sites.search(query);

export function SiteAdminPage() {
    const {t} = useI18n();
    const {reload} = useSite();
    const [contents, setContents] = useState<Content[]>([]);
    useEffect(() => {
        adminApi.contents.search({size: 200}).then((page) => setContents(page?.content ?? [])).catch(() => setContents([]));
    }, []);
    const fields = useMemo<FieldSpec[]>(() => siteFields(t).flatMap((field) => field.name !== 'locale' ? [field] : [field, {
        name: 'homeContentId', label: t('admin.site.field.homeContentId', '홈 화면 컨텐츠'), type: 'select', newRow: true,
        options: contents.map((content) => ({
            value: String(content.id),
            label: content.title + (content.status === 'PUBLISHED' ? '' : t('admin.site.homeContentUnpublished', ' (비공개 - 홈에 쓰이지 않음)')),
        })),
        help: t('admin.site.field.homeContentId.help', '고르면 홈 화면에 이 컨텐츠를 보여 줍니다. 선택 안 함이면 기본 홈 화면을 씁니다. 컨텐츠 화면에서 홈을 꾸미는 방법을 볼 수 있습니다.'),
    }]), [contents, t]);
    return (
        <AdminCrudPage<Site>
            title={t('admin.site.title', '사이트')}
            description={
                <>
                    <p>{t('admin.site.description1', '기본 사이트의 이름이 사용자 화면에 표시되고, 기본 사이트에서 끈 기능은 사용자 메뉴와 관리자 메뉴에서 빠지며 해당 화면과 API도 닫힙니다.')}</p>
                    <p>{t('admin.site.description2', '기본 사이트는 하나만 지정할 수 있습니다. 새로 기본으로 지정하면 기존 기본 사이트는 해제됩니다.')}</p>
                </>
            }
            load={loadSites}
            fields={fields}
            emptyForm={{isDefault: true, locale: 'ko', darkModeEnabled: true, searchIndexing: true, ...ALL_FEATURES_ON}}
            toForm={siteToForm}
            columns={[
                {label: t('admin.site.column.name', '이름'), render: (s) => s.name},
                {label: t('admin.site.column.domain', '도메인'), render: (s) => s.domain ?? '-'},
                {label: t('admin.site.column.address', '접속 주소'), render: (s) => s.code ? `/s/${s.code}/` : '/'},
                {label: t('admin.site.column.isDefault', '기본'), render: (s) => <YesNo value={s.isDefault}/>},
                {label: t('admin.site.column.features', '켜진 기능'), render: (s) => {
                    const on = siteFeatures(t).filter((feature) => s.features[feature.value] !== false);
                    return on.length ? on.map((feature) => feature.label).join(', ') : <span className="muted">{t('admin.site.noFeatures', '없음')}</span>;
                }},
            ]}
            create={(v) => adminApi.sites.create(toSiteBody(v)).then(reload)}
            update={(s, v) => adminApi.sites.update(s.id, toSiteBody(v)).then(reload)}
            remove={(s) => adminApi.sites.remove(s.id).then(reload)}
        />
    );
}

const configFields = (t: T): FieldSpec[] => [
    {name: 'type', label: t('admin.config.field.type', '입력 형식'), type: 'select', required: true, options: [
            {value: 'TEXT', label: t('admin.config.type.text', '텍스트')}, {value: 'TEXTAREA', label: t('admin.config.type.textarea', '여러 줄')},
            {value: 'RADIO', label: t('admin.config.type.radio', '단일 선택')}, {value: 'CHECKBOX', label: t('admin.config.type.checkbox', '체크박스')}]},
    {name: 'key', label: t('admin.config.field.key', '키'), required: true, placeholder: 'site.footer', help: t('admin.config.field.key.help', '영문, 숫자, _ . - 만 사용합니다.'), newRow: true},
    {name: 'value', label: t('admin.config.field.value', '값'), type: 'textarea'},
    {name: 'description', label: t('admin.config.field.description', '설명'), wide: true},
    {name: 'isHidden', label: t('admin.config.field.isHidden', '공개 API에서 숨김'), type: 'checkbox'},
];

const loadConfigs = (query: Query) => adminApi.configs.search(query);

export function ConfigAdminPage() {
    const {t} = useI18n();
    return (
        <AdminCrudPage<Config>
            title={t('admin.config.title', '설정')}
            description={t('admin.config.description', '숨김이 아닌 설정은 /api/configs 로 사이트 화면에 공개됩니다.')}
            load={loadConfigs}
            fields={configFields(t)}
            emptyForm={{type: 'TEXT', isHidden: false}}
            columns={[
                {label: t('admin.config.column.key', '키'), render: (c) => <code>{c.key}</code>},
                {label: t('admin.config.column.value', '값'), render: (c) => <span className="truncate">{c.value}</span>},
                {label: t('admin.config.field.description', '설명'), render: (c) => c.description || '-'},
                {label: t('admin.config.column.isHidden', '숨김'), render: (c) => <YesNo value={c.isHidden}/>},
            ]}
            create={(v) => adminApi.configs.create(v)}
            update={(c, v) => adminApi.configs.update(c.id, v)}
            remove={(c) => adminApi.configs.remove(c.id)}
        />
    );
}

const dismissField = (t: T): FieldSpec => ({
    name: 'dismissDays', label: t('admin.banner.field.dismissDays', '보지 않기 기간(일)'), type: 'number', min: 0, required: true,
    help: t('admin.banner.field.dismissDays.help', '방문자가 "보지 않기"로 닫으면 이 기간 동안 다시 보이지 않습니다. 1이면 "오늘 하루 보지 않기", 0이면 버튼을 표시하지 않습니다.'),
});
const periodFields = (t: T): FieldSpec[] => [
    {name: 'startDateTime', label: t('admin.banner.field.startDateTime', '게시 시작'), type: 'datetime'},
    {name: 'endDateTime', label: t('admin.banner.field.endDateTime', '게시 종료'), type: 'datetime'},
];

const topBarFields = (t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.banner.field.isUsing', '사용'), type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: t('admin.banner.field.adminName', '관리용 이름'), required: true},
    {name: 'description', label: t('admin.banner.field.text', '표시 문구'), required: true, wide: true, placeholder: '11월 3일 02:00~04:00 서비스 점검이 있습니다.'},
    {name: 'order', label: t('admin.banner.field.order', '순서'), type: 'number', required: true, min: 1, help: t('admin.banner.field.order.help', '여러 개가 게시 중이면 순서가 가장 앞선 배너가 표시됩니다.')},
    {name: 'backgroundColor', label: t('admin.banner.field.backgroundColor', '배경색'), type: 'color', newRow: true},
    {name: 'textColor', label: t('admin.banner.field.textColor', '글자색'), type: 'color'},
    {name: 'link', label: t('admin.banner.field.link', '링크'), placeholder: 'https:// 또는 /boards/1', newRow: true},
    {name: 'opensNewWindow', label: t('admin.banner.field.opensNewWindow', '새 창으로 열기'), type: 'checkbox'},
    {...dismissField(t), newRow: true},
    ...periodFields(t).map((field, index) => (index === 0 ? {...field, newRow: true} : field)),
];

const popupFields = (t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.banner.field.isUsing', '사용'), type: 'checkbox', wide: true},
    {name: 'opensNewWindow', label: t('admin.banner.field.opensNewWindow', '새 창으로 열기'), type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: t('admin.banner.field.title', '제목'), required: true},
    {name: 'description', label: t('admin.banner.field.popupContent', '내용 (이미지가 없을 때 표시)'), type: 'textarea'},
    {name: 'popupMode', label: t('admin.banner.field.popupMode', '표시 방식'), type: 'select', required: true, options: [
            {value: 'MODELESS', label: t('admin.banner.popupMode.modeless', '모달리스 (배경 클릭 가능)')},
            {value: 'MODAL', label: t('admin.banner.popupMode.modal', '모달 (배경 클릭 불가)')}],
        help: t('admin.banner.field.popupMode.help', '모달은 화면 가운데에 어두운 배경과 함께 뜨고, 닫기 전에는 뒤 화면을 사용할 수 없습니다.')},
    {name: 'order', label: t('admin.banner.field.order', '순서'), type: 'number', required: true, min: 1},
    {name: 'fileId', label: t('admin.banner.field.image', '이미지'), type: 'image'},
    {name: 'link', label: t('admin.banner.field.popupLink', '클릭 시 이동할 링크'), placeholder: 'https://'},
    {name: 'width', label: t('admin.banner.field.width', '가로(px)'), type: 'number', min: 0},
    {name: 'height', label: t('admin.banner.field.height', '세로(px)'), type: 'number', min: 0},
    {name: 'top', label: t('admin.banner.field.top', '위치 top(px)'), type: 'number', help: t('admin.banner.field.modelessOnly', '모달리스일 때만 사용합니다.')},
    {name: 'left', label: t('admin.banner.field.left', '위치 left(px)'), type: 'number', help: t('admin.banner.field.modelessOnly', '모달리스일 때만 사용합니다.')},
    dismissField(t),
    ...periodFields(t),
];

const toBannerBody = (type: 'BANNER' | 'POPUP', values: FormValues) => ({
    ...values,
    type,
    isModal: type === 'POPUP' && values.popupMode === 'MODAL',
});

const bannerToForm = (banner: Banner): FormValues => ({...banner, popupMode: banner.isModal ? 'MODAL' : 'MODELESS'});

const loadBanners = (query: Query) => adminApi.banners.search(query);

export function BannerAdminPage() {
    const {t} = useI18n();
    const bannerTabs = [
        {value: 'BANNER', label: t('admin.banner.tab.banner', '배너(상단 띠 배너)')},
        {value: 'POPUP', label: t('admin.banner.tab.popup', '팝업')},
    ] as const;
    const [type, setType] = useState<'BANNER' | 'POPUP'>('BANNER');
    const isTopBar = type === 'BANNER';
    return (
        <>
            <div className="tabs" role="tablist" aria-label={t('admin.banner.tabsLabel', '배너 종류')}>
                {bannerTabs.map((tab) => (
                    <button key={tab.value} type="button" role="tab" aria-selected={type === tab.value}
                            className={type === tab.value ? 'tab active' : 'tab'} onClick={() => setType(tab.value)}>
                        {tab.label}
                    </button>
                ))}
            </div>
            <AdminCrudPage<Banner>
                key={type}
                title={isTopBar ? t('admin.banner.title', '배너') : t('admin.banner.popupTitle', '팝업')}
                description={isTopBar
                    ? t('admin.banner.description.topBar', '사용자 화면 헤더 위에 띠 형태로 표시됩니다. 방문자는 "보지 않기"로 정한 기간 동안 닫아 둘 수 있습니다.')
                    : t('admin.banner.description.popup', '사용자 홈 화면에 표시됩니다. 표시 방식으로 모달/모달리스를 고를 수 있습니다.')}
                load={loadBanners}
                extraQuery={{type}}
                fields={isTopBar ? topBarFields(t) : popupFields(t)}
                emptyForm={isTopBar
                    ? {order: 1, isUsing: true, opensNewWindow: false, dismissDays: 1, backgroundColor: '#1D4ED8', textColor: '#FFFFFF'}
                    : {order: 1, isUsing: true, opensNewWindow: false, dismissDays: 1, popupMode: 'MODELESS'}}
                toForm={bannerToForm}
                columns={isTopBar ? [
                    {label: t('admin.banner.column.name', '이름'), render: (b) => b.name},
                    {label: t('admin.banner.column.preview', '미리보기'), render: (b) => (
                        <span className="top-bar-preview" style={{background: b.backgroundColor ?? undefined, color: b.textColor ?? undefined}}>
                            {b.description || b.name}
                        </span>
                    )},
                    {label: t('admin.banner.column.isUsing', '사용 여부'), render: (b) => <YesNo value={b.isUsing}/>},
                    {label: t('admin.banner.field.order', '순서'), render: (b) => b.order},
                    {label: t('admin.banner.column.period', '기간'), render: (b) => `${formatDateTime(b.startDateTime)} ~ ${formatDateTime(b.endDateTime)}`},
                ] : [
                    {label: t('admin.banner.column.image', '이미지'), render: (b) => b.imageUrl ? <img className="thumb" src={b.imageUrl} alt={b.name}/> : '-'},
                    {label: t('admin.banner.field.title', '제목'), render: (b) => b.name},
                    {label: t('admin.banner.field.popupMode', '표시 방식'), render: (b) => (b.isModal ? t('admin.banner.popupMode.modalShort', '모달') : t('admin.banner.popupMode.modelessShort', '모달리스'))},
                    {label: t('admin.banner.field.order', '순서'), render: (b) => b.order},
                    {label: t('admin.banner.column.period', '기간'), render: (b) => `${formatDateTime(b.startDateTime)} ~ ${formatDateTime(b.endDateTime)}`},
                    {label: t('admin.banner.column.isUsing', '사용 여부'), render: (b) => <YesNo value={b.isUsing}/>},
                ]}
                create={(v) => adminApi.banners.create(toBannerBody(type, v))}
                update={(b, v) => adminApi.banners.update(b.id, toBannerBody(type, v))}
                remove={(b) => adminApi.banners.remove(b.id)}
            />
        </>
    );
}

interface MenuKind {
    value: string;
    label: string;
    type: Menu['type'];
    feature?: SiteFeature;
    link?: string;
}

const featureKind = (feature: SiteFeature, label: string, link?: string): MenuKind =>
    ({value: `FEATURE:${feature}:${link ?? ''}`, label, type: 'FEATURE', feature, link});

const menuKinds = (t: T): MenuKind[] => [
    {value: 'CONTENT', label: t('admin.menuKind.content', '컨텐츠'), type: 'CONTENT', feature: 'CONTENT'},
    featureKind('BOARD', t('admin.menuKind.boardAll', '게시판 (전체)')),
    {value: 'BOARD', label: t('admin.menuKind.boardOne', '게시판 (하나 지정)'), type: 'BOARD', feature: 'BOARD'},
    featureKind('ARCHIVE', t('admin.menuKind.archiveAll', '아카이브 (전체)')),
    {value: 'ARCHIVE', label: t('admin.menuKind.archiveOne', '아카이브 (하나 지정)'), type: 'ARCHIVE', feature: 'ARCHIVE'},
    featureKind('QNA', t('admin.menuKind.qna', '질문답변')),
    featureKind('MAP', t('admin.menuKind.map', '지도')),
    featureKind('PRODUCT', t('admin.menuKind.product', '상품')),
    featureKind('MEDIA', t('admin.menuKind.streaming', '스트리밍'), '/streaming'),
    featureKind('MEDIA', t('admin.menuKind.series', '시리즈'), '/series'),
    featureKind('MEDIA', t('admin.menuKind.courses', '교육과정'), '/courses'),
    featureKind('PROGRAM', t('admin.menuKind.reservationItem', '예약 상품')),
    featureKind('APPLICATION', t('admin.menuKind.application', '신청·지원')),
    featureKind('REPORT', t('admin.menuKind.report', '신고')),
    {value: 'LINK', label: t('admin.menuKind.link', '링크'), type: 'LINK'},
];

const kindOf = (menu: Pick<Menu, 'type' | 'feature' | 'link'>) =>
    menu.type === 'FEATURE' ? `FEATURE:${menu.feature}:${menu.link ?? ''}` : menu.type;
const findKind = (allKinds: MenuKind[], menu: Pick<Menu, 'type' | 'feature' | 'link'>) => allKinds.find((kind) => kind.value === kindOf(menu));

const menuFields = (allowedKinds: MenuKind[], allKindsCount: number, t: T): FieldSpec[] => [
    {name: 'isUsing', label: t('admin.menu.field.isUsing', '사용'), type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: t('admin.menu.field.name', '메뉴 이름'), required: true},
    {name: 'kind', label: t('admin.menu.field.kind', '종류'), type: 'select', required: true, options: allowedKinds.map(({value, label}) => ({value, label})),
        help: allowedKinds.length < allKindsCount ? t('admin.menu.field.kind.help', '사용 중지된 기능은 메뉴 종류로 선택할 수 없습니다.') : undefined},
    {name: 'targetId', label: t('admin.menu.field.targetId', '연결 대상 ID'), type: 'number', min: 0,
        help: t('admin.menu.field.targetId.help', '컨텐츠는 컨텐츠 ID, 게시판·아카이브 (하나 지정)는 게시판·아카이브 ID')},
    {name: 'link', label: t('admin.menu.field.link', '링크'), help: t('admin.menu.field.link.help', '링크 메뉴일 때 이동할 주소'), visibleWhen: (v) => v.kind === 'LINK'},
    {name: 'sequence', label: t('admin.menu.field.sequence', '순서'), type: 'number', required: true, min: 1},
];

const toMenuBody = (allKinds: MenuKind[], {kind, ...values}: FormValues, parentId: number | null) => {
    const selected = allKinds.find((k) => k.value === kind);
    return {...values, parentId, type: selected?.type, feature: selected?.type === 'FEATURE' ? selected.feature : null,
        link: selected?.type === 'LINK' ? values.link : selected?.link ?? null};
};

const LISTING_FEATURES: SiteFeature[] = ['BOARD', 'ARCHIVE'];

const featureLabel = (feature: SiteFeature | null, t: T) => siteFeatures(t).find((f) => f.value === feature)?.label ?? feature;

export function MenuAdminPage() {
    const {t} = useI18n();
    const {isEnabled} = useSite();
    const allKinds = menuKinds(t);
    const allowedKinds = allKinds.filter((kind) => !kind.feature || isEnabled(kind.feature));
    const defaultKind = allowedKinds[0].value;
    const [menus, setMenus] = useState<Menu[]>([]);
    const [editing, setEditing] = useState<{ menu: Menu | null; parentId: number | null; values: FormValues } | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        adminApi.menus.tree().then(setMenus).catch(setError);
    }, []);
    useEffect(reload, [reload]);

    const remove = async (menu: Menu) => {
        if (!window.confirm(t('admin.menu.confirmDelete', "'{name}' 메뉴를 삭제할까요?", {name: menu.name}))) {
            return;
        }
        try {
            await adminApi.menus.remove(menu.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const move = async (siblings: Menu[], index: number, offset: -1 | 1) => {
        const ids = siblings.map((m) => m.id);
        [ids[index], ids[index + offset]] = [ids[index + offset], ids[index]];
        try {
            await adminApi.menus.reorder(siblings[0].parentId, ids);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const nextSequence = (siblings: Menu[]) => siblings.reduce((max, m) => Math.max(max, m.sequence + 1), 1);

    const editForm = () => editing && (
        <SchemaForm
            key={editing.menu?.id ?? `new-${editing.parentId}`}
            title={editing.menu ? t('admin.menu.editTitle', "'{name}' 메뉴 수정", {name: editing.menu.name})
                : editing.parentId ? t('admin.menu.addChildTitle', '하위 메뉴 추가') : t('admin.menu.addTitle', '메뉴 추가')}
            fields={menuFields(allowedKinds, allKinds.length, t)}
            initial={editing.values}
            onCancel={() => setEditing(null)}
            onSubmit={async (values) => {
                const body = toMenuBody(allKinds, values, editing.parentId);
                await (editing.menu ? adminApi.menus.update(editing.menu.id, body) : adminApi.menus.create(body));
                setEditing(null);
                reload();
            }}
        />
    );

    const renderNode = (menu: Menu, index: number, siblings: Menu[]) => {
        const isFeatureMenu = menu.type === 'FEATURE';
        const kind = findKind(allKinds, menu);
        const feature = kind?.feature ?? menu.feature;
        return (
        <li key={menu.id}>
            <div className="tree-row">
                <strong>{menu.name}</strong>
                <span className="badge">
                    {kind?.label ?? (isFeatureMenu ? featureLabel(menu.feature, t) : menu.type)}
                </span>
                {!menu.isUsing && <span className="badge">{t('admin.menu.notUsing', '사용 안 함')}</span>}
                {feature && !isEnabled(feature) && <span className="badge danger">{t('admin.menu.featureOff', '기능 꺼짐 · 사용자 화면에서 숨김')}</span>}
                <span className="muted small">
                    {isFeatureMenu
                        ? (LISTING_FEATURES.includes(menu.feature!) ? t('admin.menu.autoListing', '하위 목록은 등록된 항목으로 자동 구성') : '')
                        : menu.type === 'LINK' ? menu.link : t('admin.menu.targetId', '대상 #{id}', {id: String(menu.targetId)})}
                </span>
                <span className="actions">
                    <button type="button" aria-label={t('admin.menu.moveUp', '{name} 위로', {name: menu.name})} disabled={index === 0}
                            onClick={() => move(siblings, index, -1)}>↑</button>
                    <button type="button" aria-label={t('admin.menu.moveDown', '{name} 아래로', {name: menu.name})} disabled={index === siblings.length - 1}
                            onClick={() => move(siblings, index, 1)}>↓</button>
                    {isFeatureMenu
                        ? <button type="button" className="placeholder" tabIndex={-1} aria-hidden="true">{t('admin.menu.addChild', '하위 추가')}</button>
                        : <button type="button" onClick={() => setEditing({menu: null, parentId: menu.id,
                            values: {kind: defaultKind, sequence: nextSequence(menu.children), isUsing: true}})}>{t('admin.menu.addChild', '하위 추가')}</button>}
                    <button type="button" onClick={() => setEditing({menu, parentId: menu.parentId, values: {...menu, kind: kindOf(menu)}})}>{t('common.edit', '수정')}</button>
                    <button type="button" className="danger" onClick={() => remove(menu)}>{t('common.delete', '삭제')}</button>
                </span>
            </div>
            {editing && (editing.menu?.id === menu.id || (!editing.menu && editing.parentId === menu.id)) && (
                <div className="tree-edit">{editForm()}</div>
            )}
            {menu.children.length > 0 && <ul className="tree">{menu.children.map((child, i) => renderNode(child, i, menu.children))}</ul>}
        </li>
        );
    };

    return (
        <section className="card">
            <h1>{t('admin.menu.title', '메뉴')}</h1>
            <div className="muted page-description">
                <p>{t('admin.menu.description1', '사이트 상단 내비게이션에 순서대로 표시되며, ↑/↓ 버튼으로 같은 단계 안의 순서를 바꿀 수 있습니다. 하위 메뉴가 있는 메뉴는 삭제할 수 없습니다.')}</p>
                <p>{t('admin.menu.description2', '메뉴 종류로 게시판·아카이브·상품·예약·신청·지원·스트리밍·질문답변·신고·지도 기능을 고를 수 있고, 사이트에서 꺼 둔 기능은 고를 수 없습니다. 게시판·아카이브 (전체) 메뉴에는 등록된 게시판·아카이브가 하위 목록으로 자동으로 붙고, 따로 메뉴로 연결한 항목은 빠집니다. 기능 메뉴 아래에는 하위 메뉴를 둘 수 없으며, 기능을 끄면 사용 여부와 관계없이 숨겨집니다.')}</p>
            </div>
            <div className="toolbar">
                <button type="button" className="primary push-right" onClick={() => setEditing({menu: null, parentId: null,
                    values: {kind: defaultKind, sequence: nextSequence(menus), isUsing: true}})}>{t('admin.menu.addTopLevel', '최상위 메뉴 추가')}</button>
            </div>
            {editing && !editing.menu && editing.parentId === null && editForm()}
            <ErrorBanner error={error}/>
            {menus.length === 0 ? <p className="muted">{t('admin.menu.noData', '메뉴가 없습니다.')}</p> : <ul className="tree">{menus.map((menu, i) => renderNode(menu, i, menus))}</ul>}
        </section>
    );
}

const loadAuditLogs = (query: Query) => adminApi.auditLogs(query);

export function AuditLogPage() {
    const {t} = useI18n();
    return (
        <AdminCrudPage
            title={t('admin.auditLog.title', '관리자 작업 로그')}
            description={t('admin.auditLog.description', '관리자 API의 등록·수정·삭제 요청이 기록됩니다.')}
            load={loadAuditLogs}
            columns={[
                {label: t('admin.auditLog.column.createdAt', '일시'), render: (l) => formatDateTime(l.createdAt)},
                {label: t('admin.auditLog.column.admin', '관리자'), render: (l) => l.username},
                {label: t('admin.auditLog.column.request', '요청'), render: (l) => <code>{l.uri}</code>},
                {label: t('admin.auditLog.column.handler', '처리기'), render: (l) => <span className="muted small">{l.handler}</span>},
                {label: t('admin.auditLog.column.result', '결과'), render: (l) => l.content},
                {label: 'IP', render: (l) => l.ipAddress},
            ]}
        />
    );
}
