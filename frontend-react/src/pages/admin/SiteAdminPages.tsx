import {useCallback, useEffect, useMemo, useState} from 'react';
import type {Query} from '../../api/client';
import {type Banner, type Config, type Content, type Menu, type Site, SITE_FEATURES, type SiteFeature} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {AdminCrudPage, YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {type FieldSpec, type FormValues, SchemaForm} from '../../components/SchemaForm';
import {useSite} from '../../components/SiteContext';

const featureField = (feature: SiteFeature) => `feature_${feature}`;

const SITE_FIELDS: FieldSpec[] = [
    {name: 'isDefault', label: '기본 사이트', type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: '사이트 이름', required: true, help: '사용자 화면의 헤더와 홈에 표시됩니다.'},
    {name: 'domain', label: '도메인', placeholder: 'www.example.com'},
    {name: 'description', label: '설명', wide: true, help: '홈 화면에 소개 문구로 표시됩니다.'},
    {name: 'theme', label: '테마', type: 'select', options: [{value: 'market', label: '마켓 (중고거래·쇼핑몰)'}],
        help: '선택 안 함이면 기본 테마를 씁니다.'},
    {name: 'darkModeEnabled', label: '다크 모드 사용', type: 'checkbox',
        help: '켜면 방문자가 시스템·라이트·다크 중에서 고를 수 있고, 끄면 항상 라이트 모드로 보입니다.'},
    {name: 'locale', label: '로케일', placeholder: 'ko'},
    ...SITE_FEATURES.map((feature): FieldSpec => ({name: featureField(feature.value), label: feature.label, type: 'checkbox', group: '기능'})),
    {name: 'faviconFileId', label: '파비콘', type: 'image', wide: true, group: '검색·공유 (SEO)',
        help: '브라우저 탭에 보이는 아이콘입니다. 정사각형 PNG(32×32 이상)나 ICO 파일을 권장합니다.'},
    {name: 'seoTitle', label: '검색·공유 제목', wide: true, group: '검색·공유 (SEO)', placeholder: '사이트 이름 | 한 줄 소개',
        help: '브라우저 제목, 검색 결과, 링크 미리보기에 쓰입니다. 비워두면 사이트 이름을 씁니다.'},
    {name: 'metaDescription', label: '설명 (meta description)', type: 'textarea', group: '검색·공유 (SEO)',
        help: '검색 결과와 링크 미리보기에 보이는 문구입니다. 80~160자를 권장합니다.'},
    {name: 'metaKeywords', label: '키워드', wide: true, group: '검색·공유 (SEO)', placeholder: '카페, 원두, 핸드드립'},
    {name: 'ogImageFileId', label: '공유 미리보기 이미지 (OG 이미지)', type: 'image', wide: true, group: '검색·공유 (SEO)',
        help: '카카오톡·페이스북 등에 링크를 붙였을 때 보이는 이미지입니다. 1200×630 크기를 권장합니다.'},
    {name: 'searchIndexing', label: '검색엔진 노출 허용', type: 'checkbox', wide: true, group: '검색·공유 (SEO)',
        help: '끄면 검색엔진이 사이트를 수집하지 않도록 robots.txt 와 메타 태그로 알립니다.'},
    {name: 'googleVerification', label: '구글 서치 콘솔 확인 코드', wide: true, group: '검색·공유 (SEO)',
        help: 'google-site-verification 메타 태그의 content 값만 넣습니다.'},
    {name: 'naverVerification', label: '네이버 서치어드바이저 확인 코드', wide: true, group: '검색·공유 (SEO)',
        help: 'naver-site-verification 메타 태그의 content 값만 넣습니다. 사이트맵은 /sitemap.xml 입니다.'},
];

const ALL_FEATURES_ON = Object.fromEntries(SITE_FEATURES.map((feature) => [featureField(feature.value), true]));

const siteToForm = (site: Site): FormValues => ({
    ...site,
    ...Object.fromEntries(SITE_FEATURES.map((feature) => [featureField(feature.value), site.features[feature.value] ?? true])),
});

const toSiteBody = (values: FormValues) => ({
    ...values,
    homeContentId: values.homeContentId ? Number(values.homeContentId) : null,
    disabledFeatures: SITE_FEATURES.filter((feature) => !values[featureField(feature.value)]).map((feature) => feature.value),
});

const loadSites = (query: Query) => adminApi.sites.search(query);

export function SiteAdminPage() {
    const {reload} = useSite();
    const [contents, setContents] = useState<Content[]>([]);
    useEffect(() => {
        adminApi.contents.search({size: 200}).then((page) => setContents(page?.content ?? [])).catch(() => setContents([]));
    }, []);
    const fields = useMemo<FieldSpec[]>(() => SITE_FIELDS.flatMap((field) => field.name !== 'locale' ? [field] : [field, {
        name: 'homeContentId', label: '홈 화면 컨텐츠', type: 'select', wide: true,
        options: contents.map((content) => ({
            value: String(content.id), label: `${content.title}${content.status === 'PUBLISHED' ? '' : ' (비공개 - 홈에 쓰이지 않음)'}`,
        })),
        help: '고르면 홈 화면에 이 컨텐츠를 보여 줍니다. 선택 안 함이면 기본 홈 화면을 씁니다. 컨텐츠 화면에서 홈을 꾸미는 방법을 볼 수 있습니다.',
    }]), [contents]);
    return (
        <AdminCrudPage<Site>
            title="사이트"
            description={
                <>
                    <p>기본 사이트의 이름이 사용자 화면에 표시되고, 기본 사이트에서 끈 기능은 사용자 메뉴와 관리자 메뉴에서 빠지며
                        해당 화면과 API도 닫힙니다.</p>
                    <p>기본 사이트는 하나만 지정할 수 있습니다. 새로 기본으로 지정하면 기존 기본 사이트는 해제됩니다.</p>
                </>
            }
            load={loadSites}
            fields={fields}
            emptyForm={{isDefault: true, locale: 'ko', darkModeEnabled: true, searchIndexing: true, ...ALL_FEATURES_ON}}
            toForm={siteToForm}
            columns={[
                {label: '이름', render: (s) => s.name},
                {label: '도메인', render: (s) => s.domain ?? '-'},
                {label: '기본', render: (s) => <YesNo value={s.isDefault}/>},
                {label: '켜진 기능', render: (s) => {
                    const on = SITE_FEATURES.filter((feature) => s.features[feature.value] !== false);
                    return on.length ? on.map((feature) => feature.label).join(', ') : <span className="muted">없음</span>;
                }},
            ]}
            create={(v) => adminApi.sites.create(toSiteBody(v)).then(reload)}
            update={(s, v) => adminApi.sites.update(s.id, toSiteBody(v)).then(reload)}
            remove={(s) => adminApi.sites.remove(s.id).then(reload)}
        />
    );
}

const CONFIG_FIELDS: FieldSpec[] = [
    {name: 'type', label: '입력 형식', type: 'select', required: true, options: [
            {value: 'TEXT', label: '텍스트'}, {value: 'TEXTAREA', label: '여러 줄'},
            {value: 'RADIO', label: '단일 선택'}, {value: 'CHECKBOX', label: '체크박스'}]},
    {name: 'key', label: '키', required: true, placeholder: 'site.footer', help: '영문, 숫자, _ . - 만 사용합니다.', newRow: true},
    {name: 'value', label: '값', type: 'textarea'},
    {name: 'description', label: '설명', wide: true},
    {name: 'isHidden', label: '공개 API에서 숨김', type: 'checkbox'},
];

const loadConfigs = (query: Query) => adminApi.configs.search(query);

export function ConfigAdminPage() {
    return (
        <AdminCrudPage<Config>
            title="설정"
            description="숨김이 아닌 설정은 /api/configs 로 사이트 화면에 공개됩니다."
            load={loadConfigs}
            fields={CONFIG_FIELDS}
            emptyForm={{type: 'TEXT', isHidden: false}}
            columns={[
                {label: '키', render: (c) => <code>{c.key}</code>},
                {label: '값', render: (c) => <span className="truncate">{c.value}</span>},
                {label: '설명', render: (c) => c.description || '-'},
                {label: '숨김', render: (c) => <YesNo value={c.isHidden}/>},
            ]}
            create={(v) => adminApi.configs.create(v)}
            update={(c, v) => adminApi.configs.update(c.id, v)}
            remove={(c) => adminApi.configs.remove(c.id)}
        />
    );
}

const DISMISS_FIELD: FieldSpec = {
    name: 'dismissDays', label: '보지 않기 기간(일)', type: 'number', required: true,
    help: '방문자가 "보지 않기"로 닫으면 이 기간 동안 다시 보이지 않습니다. 1이면 "오늘 하루 보지 않기", 0이면 버튼을 표시하지 않습니다.',
};
const PERIOD_FIELDS: FieldSpec[] = [
    {name: 'startDateTime', label: '게시 시작', type: 'datetime'},
    {name: 'endDateTime', label: '게시 종료', type: 'datetime'},
];

const TOP_BAR_FIELDS: FieldSpec[] = [
    {name: 'isUsing', label: '사용', type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: '관리용 이름', required: true},
    {name: 'order', label: '순서', type: 'number', required: true, min: 1, help: '여러 개가 게시 중이면 순서가 가장 앞선 배너가 표시됩니다.'},
    {name: 'description', label: '표시 문구', required: true, wide: true, placeholder: '11월 3일 02:00~04:00 서비스 점검이 있습니다.'},
    {name: 'backgroundColor', label: '배경색', type: 'color', newRow: true},
    {name: 'textColor', label: '글자색', type: 'color'},
    {name: 'link', label: '링크', placeholder: 'https:// 또는 /boards/1', newRow: true},
    {name: 'opensNewWindow', label: '새 창으로 열기', type: 'checkbox'},
    {...DISMISS_FIELD, newRow: true},
    ...PERIOD_FIELDS.map((field, index) => (index === 0 ? {...field, newRow: true} : field)),
];

const POPUP_FIELDS: FieldSpec[] = [
    {name: 'isUsing', label: '사용', type: 'checkbox', wide: true},
    {name: 'opensNewWindow', label: '새 창으로 열기', type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: '제목', required: true},
    {name: 'popupMode', label: '표시 방식', type: 'select', required: true, options: [
            {value: 'MODELESS', label: '모달리스 (배경 클릭 가능)'},
            {value: 'MODAL', label: '모달 (배경 클릭 불가)'}],
        help: '모달은 화면 가운데에 어두운 배경과 함께 뜨고, 닫기 전에는 뒤 화면을 사용할 수 없습니다.'},
    {name: 'order', label: '순서', type: 'number', required: true, min: 1},
    {name: 'fileId', label: '이미지', type: 'image'},
    {name: 'description', label: '내용 (이미지가 없을 때 표시)', type: 'textarea'},
    {name: 'link', label: '클릭 시 이동할 링크', placeholder: 'https://'},
    {name: 'width', label: '가로(px)', type: 'number'},
    {name: 'height', label: '세로(px)', type: 'number'},
    {name: 'top', label: '위치 top(px)', type: 'number', help: '모달리스일 때만 사용합니다.'},
    {name: 'left', label: '위치 left(px)', type: 'number', help: '모달리스일 때만 사용합니다.'},
    DISMISS_FIELD,
    ...PERIOD_FIELDS,
];

const toBannerBody = (type: 'BANNER' | 'POPUP', values: FormValues) => ({
    ...values,
    type,
    isModal: type === 'POPUP' && values.popupMode === 'MODAL',
});

const bannerToForm = (banner: Banner): FormValues => ({...banner, popupMode: banner.isModal ? 'MODAL' : 'MODELESS'});

const loadBanners = (query: Query) => adminApi.banners.search(query);

const BANNER_TABS = [
    {value: 'BANNER', label: '배너(상단 띠 배너)'},
    {value: 'POPUP', label: '팝업'},
] as const;

export function BannerAdminPage() {
    const [type, setType] = useState<'BANNER' | 'POPUP'>('BANNER');
    const isTopBar = type === 'BANNER';
    return (
        <>
            <div className="tabs" role="tablist" aria-label="배너 종류">
                {BANNER_TABS.map((tab) => (
                    <button key={tab.value} type="button" role="tab" aria-selected={type === tab.value}
                            className={type === tab.value ? 'tab active' : 'tab'} onClick={() => setType(tab.value)}>
                        {tab.label}
                    </button>
                ))}
            </div>
            <AdminCrudPage<Banner>
                key={type}
                title={isTopBar ? '배너' : '팝업'}
                description={isTopBar
                    ? '사용자 화면 헤더 위에 띠 형태로 표시됩니다. 방문자는 "보지 않기"로 정한 기간 동안 닫아 둘 수 있습니다.'
                    : '사용자 홈 화면에 표시됩니다. 표시 방식으로 모달/모달리스를 고를 수 있습니다.'}
                load={loadBanners}
                extraQuery={{type}}
                fields={isTopBar ? TOP_BAR_FIELDS : POPUP_FIELDS}
                emptyForm={isTopBar
                    ? {order: 1, isUsing: true, opensNewWindow: false, dismissDays: 1, backgroundColor: '#1D4ED8', textColor: '#FFFFFF'}
                    : {order: 1, isUsing: true, opensNewWindow: false, dismissDays: 1, popupMode: 'MODELESS'}}
                toForm={bannerToForm}
                columns={isTopBar ? [
                    {label: '이름', render: (b) => b.name},
                    {label: '미리보기', render: (b) => (
                        <span className="top-bar-preview" style={{background: b.backgroundColor ?? undefined, color: b.textColor ?? undefined}}>
                            {b.description || b.name}
                        </span>
                    )},
                    {label: '사용 여부', render: (b) => <YesNo value={b.isUsing}/>},
                    {label: '순서', render: (b) => b.order},
                    {label: '기간', render: (b) => `${formatDateTime(b.startDateTime)} ~ ${formatDateTime(b.endDateTime)}`},
                ] : [
                    {label: '이미지', render: (b) => b.imageUrl ? <img className="thumb" src={b.imageUrl} alt={b.name}/> : '-'},
                    {label: '제목', render: (b) => b.name},
                    {label: '표시 방식', render: (b) => (b.isModal ? '모달' : '모달리스')},
                    {label: '순서', render: (b) => b.order},
                    {label: '기간', render: (b) => `${formatDateTime(b.startDateTime)} ~ ${formatDateTime(b.endDateTime)}`},
                    {label: '사용 여부', render: (b) => <YesNo value={b.isUsing}/>},
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
}

const featureKind = (feature: SiteFeature, label: string): MenuKind => ({value: `FEATURE:${feature}`, label, type: 'FEATURE', feature});

const MENU_KINDS: MenuKind[] = [
    {value: 'CONTENT', label: '컨텐츠', type: 'CONTENT', feature: 'CONTENT'},
    featureKind('BOARD', '게시판 (전체)'),
    {value: 'BOARD', label: '게시판 (하나 지정)', type: 'BOARD', feature: 'BOARD'},
    featureKind('ARCHIVE', '아카이브 (전체)'),
    {value: 'ARCHIVE', label: '아카이브 (하나 지정)', type: 'ARCHIVE', feature: 'ARCHIVE'},
    featureKind('PRODUCT', '상품'),
    featureKind('PROGRAM', '예약'),
    featureKind('APPLICATION', '신청·지원'),
    featureKind('QNA', '질문답변'),
    featureKind('REPORT', '신고'),
    featureKind('MAP', '지도'),
    featureKind('MEDIA', '스트리밍'),
    {value: 'LINK', label: '링크', type: 'LINK'},
];

const kindOf = (menu: Pick<Menu, 'type' | 'feature'>) => menu.type === 'FEATURE' ? `FEATURE:${menu.feature}` : menu.type;
const findKind = (menu: Pick<Menu, 'type' | 'feature'>) => MENU_KINDS.find((kind) => kind.value === kindOf(menu));

const menuFields = (allowedKinds: MenuKind[]): FieldSpec[] => [
    {name: 'isUsing', label: '사용', type: 'checkbox', wide: true, spaced: true},
    {name: 'name', label: '메뉴 이름', required: true},
    {name: 'kind', label: '종류', type: 'select', required: true, options: allowedKinds.map(({value, label}) => ({value, label})),
        help: allowedKinds.length < MENU_KINDS.length ? '사용 중지된 기능은 메뉴 종류로 선택할 수 없습니다.' : undefined},
    {name: 'targetId', label: '연결 대상 ID', type: 'number', help: '컨텐츠는 컨텐츠 ID, 게시판·아카이브 (하나 지정)는 게시판·아카이브 ID'},
    {name: 'link', label: '링크', help: '링크 메뉴일 때 이동할 주소'},
    {name: 'sequence', label: '순서', type: 'number', required: true, min: 1},
];

const toMenuBody = ({kind, ...values}: FormValues, parentId: number | null) => {
    const selected = MENU_KINDS.find((k) => k.value === kind);
    return {...values, parentId, type: selected?.type, feature: selected?.type === 'FEATURE' ? selected.feature : null};
};

const LISTING_FEATURES: SiteFeature[] = ['BOARD', 'ARCHIVE'];

const featureLabel = (feature: SiteFeature | null) => SITE_FEATURES.find((f) => f.value === feature)?.label ?? feature;

export function MenuAdminPage() {
    const {isEnabled} = useSite();
    const allowedKinds = MENU_KINDS.filter((kind) => !kind.feature || isEnabled(kind.feature));
    const defaultKind = allowedKinds[0].value;
    const [menus, setMenus] = useState<Menu[]>([]);
    const [editing, setEditing] = useState<{ menu: Menu | null; parentId: number | null; values: FormValues } | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        adminApi.menus.tree().then(setMenus).catch(setError);
    }, []);
    useEffect(reload, [reload]);

    const remove = async (menu: Menu) => {
        if (!window.confirm(`'${menu.name}' 메뉴를 삭제할까요?`)) {
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
            title={editing.menu ? `'${editing.menu.name}' 메뉴 수정` : editing.parentId ? '하위 메뉴 추가' : '메뉴 추가'}
            fields={menuFields(allowedKinds)}
            initial={editing.values}
            onCancel={() => setEditing(null)}
            onSubmit={async (values) => {
                const body = toMenuBody(values, editing.parentId);
                await (editing.menu ? adminApi.menus.update(editing.menu.id, body) : adminApi.menus.create(body));
                setEditing(null);
                reload();
            }}
        />
    );

    const renderNode = (menu: Menu, index: number, siblings: Menu[]) => {
        const isFeatureMenu = menu.type === 'FEATURE';
        const kind = findKind(menu);
        const feature = kind?.feature ?? menu.feature;
        return (
        <li key={menu.id}>
            <div className="tree-row">
                <strong>{menu.name}</strong>
                <span className="badge">
                    {kind?.label ?? (isFeatureMenu ? featureLabel(menu.feature) : menu.type)}
                </span>
                {!menu.isUsing && <span className="badge">사용 안 함</span>}
                {feature && !isEnabled(feature) && <span className="badge danger">기능 꺼짐 · 사용자 화면에서 숨김</span>}
                <span className="muted small">
                    {isFeatureMenu
                        ? (LISTING_FEATURES.includes(menu.feature!) ? '하위 목록은 등록된 항목으로 자동 구성' : '')
                        : menu.type === 'LINK' ? menu.link : `대상 #${menu.targetId}`}
                </span>
                <span className="actions">
                    <button type="button" aria-label={`${menu.name} 위로`} disabled={index === 0}
                            onClick={() => move(siblings, index, -1)}>↑</button>
                    <button type="button" aria-label={`${menu.name} 아래로`} disabled={index === siblings.length - 1}
                            onClick={() => move(siblings, index, 1)}>↓</button>
                    {isFeatureMenu
                        ? <button type="button" className="placeholder" tabIndex={-1} aria-hidden="true">하위 추가</button>
                        : <button type="button" onClick={() => setEditing({menu: null, parentId: menu.id,
                            values: {kind: defaultKind, sequence: nextSequence(menu.children), isUsing: true}})}>하위 추가</button>}
                    <button type="button" onClick={() => setEditing({menu, parentId: menu.parentId, values: {...menu, kind: kindOf(menu)}})}>수정</button>
                    <button type="button" className="danger" onClick={() => remove(menu)}>삭제</button>
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
            <h1>메뉴</h1>
            <div className="muted page-description">
                <p>사이트 상단 내비게이션에 순서대로 표시되며, ↑/↓ 버튼으로 같은 단계 안의 순서를 바꿀 수 있습니다. 하위 메뉴가 있는 메뉴는 삭제할 수 없습니다.</p>
                <p>메뉴 종류로 <strong>게시판·아카이브·상품·예약·신청·지원·스트리밍·질문답변·신고·지도</strong> 기능을 고를 수 있고,
                    사이트에서 꺼 둔 기능은 고를 수 없습니다. 게시판·아카이브 (전체) 메뉴에는 등록된 게시판·아카이브가 하위 목록으로
                    자동으로 붙고, 따로 메뉴로 연결한 항목은 빠집니다. 기능 메뉴 아래에는 하위 메뉴를 둘 수 없으며,
                    기능을 끄면 사용 여부와 관계없이 숨겨집니다.</p>
            </div>
            <div className="toolbar">
                <button type="button" className="primary push-right" onClick={() => setEditing({menu: null, parentId: null,
                    values: {kind: defaultKind, sequence: nextSequence(menus), isUsing: true}})}>최상위 메뉴 추가</button>
            </div>
            {editing && !editing.menu && editing.parentId === null && editForm()}
            <ErrorBanner error={error}/>
            {menus.length === 0 ? <p className="muted">메뉴가 없습니다.</p> : <ul className="tree">{menus.map((menu, i) => renderNode(menu, i, menus))}</ul>}
        </section>
    );
}

const loadAuditLogs = (query: Query) => adminApi.auditLogs(query);

export function AuditLogPage() {
    return (
        <AdminCrudPage
            title="관리자 작업 로그"
            description="관리자 API의 등록·수정·삭제 요청이 기록됩니다."
            load={loadAuditLogs}
            columns={[
                {label: '일시', render: (l) => formatDateTime(l.createdAt)},
                {label: '관리자', render: (l) => l.username},
                {label: '요청', render: (l) => <code>{l.uri}</code>},
                {label: '처리기', render: (l) => <span className="muted small">{l.handler}</span>},
                {label: '결과', render: (l) => l.content},
                {label: 'IP', render: (l) => l.ipAddress},
            ]}
        />
    );
}
