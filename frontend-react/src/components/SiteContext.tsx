import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {Outlet} from 'react-router';
import type {Archive, Board, Menu, Site, SiteFeature} from '../api/domain';
import {archiveApi, boardApi, siteApi} from '../api/endpoints';
import {NotFoundPage} from '../pages/HomePage';
import {useI18n} from '../api/i18n';

const FALLBACK_NAME = 'cms';

interface SiteState {
    site: Site | null;
    siteLoaded: boolean;
    name: string;
    menus: Menu[];
    unlistedBoards: Board[];
    unlistedArchives: Archive[];
    configs: Record<string, string>;
    isEnabled: (feature: SiteFeature) => boolean;
    reload: () => void;
}

const SiteContext = createContext<SiteState>({
    site: null,
    siteLoaded: false,
    name: FALLBACK_NAME,
    menus: [],
    unlistedBoards: [],
    unlistedArchives: [],
    configs: {},
    isEnabled: () => true,
    reload: () => undefined,
});

export function SiteProvider({children}: { children: ReactNode }) {
    const [site, setSite] = useState<Site | null>(null);
    const [siteLoaded, setSiteLoaded] = useState(false);
    const [menus, setMenus] = useState<Menu[]>([]);
    const [boards, setBoards] = useState<Board[]>([]);
    const [archives, setArchives] = useState<Archive[]>([]);
    const [configs, setConfigs] = useState<Record<string, string>>({});

    const reload = useCallback(() => {
        siteApi.current().then((loaded) => setSite(loaded ?? null)).catch(() => setSite(null)).finally(() => setSiteLoaded(true));
        siteApi.menus().then((loaded) => setMenus(Array.isArray(loaded) ? loaded : [])).catch(() => setMenus([]));
        siteApi.configs().then((loaded) => setConfigs(loaded ?? {})).catch(() => setConfigs({}));
        boardApi.list().then((loaded) => setBoards(Array.isArray(loaded) ? loaded : [])).catch(() => setBoards([]));
        archiveApi.list().then((loaded) => setArchives(Array.isArray(loaded) ? loaded : [])).catch(() => setArchives([]));
    }, []);

    useEffect(reload, [reload]);

    const {t} = useI18n();
    const name = t('site.name', site?.name || FALLBACK_NAME);
    useEffect(() => {
        document.title = name;
    }, [name]);

    const value = useMemo<SiteState>(() => {
        const isEnabled = (feature: SiteFeature) => site?.features?.[feature] ?? true;
        return {
            site,
            siteLoaded,
            name,
            menus: translateMenus(visibleMenus(menus, isEnabled), t),
            unlistedBoards: isEnabled('BOARD') ? boards.filter((board) => !linksTo(menus, `/boards/${board.id}`)) : [],
            unlistedArchives: isEnabled('ARCHIVE') ? archives.filter((archive) => !linksTo(menus, `/archives/${archive.id}`)) : [],
            configs: configs['site.footer'] ? {...configs, 'site.footer': t('site.footer', configs['site.footer'])} : configs,
            isEnabled,
            reload,
        };
    }, [site, siteLoaded, name, menus, boards, archives, configs, reload, t]);

    return <SiteContext.Provider value={value}>{children}</SiteContext.Provider>;
}

const MENU_FEATURE: Partial<Record<Menu['type'], SiteFeature>> = {CONTENT: 'CONTENT', BOARD: 'BOARD', ARCHIVE: 'ARCHIVE'};

export const FEATURE_PATHS: Partial<Record<SiteFeature, string>> = {
    PRODUCT: '/products', PROGRAM: '/programs', QNA: '/qna', REPORT: '/reports/new', MAP: '/map', MEDIA: '/media', APPLICATION: '/applications',
};

function linksTo(menus: Menu[], href: string): boolean {
    return menus.some((menu) => menuHref(menu).split('?')[0] === href || linksTo(menu.children, href));
}

function visibleMenus(menus: Menu[], isEnabled: (feature: SiteFeature) => boolean): Menu[] {
    return menus
        .filter((menu) => {
            const feature = menu.type === 'FEATURE' ? menu.feature : MENU_FEATURE[menu.type];
            return menu.isUsing !== false && (!feature || isEnabled(feature));
        })
        .map((menu) => ({...menu, children: visibleMenus(menu.children, isEnabled)}));
}

function translateMenus(menus: Menu[], t: (key: string, fallback: string) => string): Menu[] {
    return menus.map((menu) => ({...menu, name: t(`menu.${menu.id}`, menu.name), children: translateMenus(menu.children, t)}));
}

export function useSite() {
    return useContext(SiteContext);
}

function findFeatureMenu(menus: Menu[], feature: SiteFeature): Menu | undefined {
    for (const menu of menus) {
        const found = menu.type === 'FEATURE' && menu.feature === feature ? menu : findFeatureMenu(menu.children, feature);
        if (found) {
            return found;
        }
    }
    return undefined;
}

/** 관리자가 정한 기능 메뉴 이름. 메뉴가 없으면 기본 이름을 쓴다. */
export function useFeatureMenuName(feature: SiteFeature, fallback: string): string {
    const {menus} = useSite();
    return findFeatureMenu(menus, feature)?.name ?? fallback;
}

export function RequireFeature({feature}: { feature: SiteFeature | SiteFeature[] }) {
    const {isEnabled} = useSite();
    if (!(Array.isArray(feature) ? feature : [feature]).some(isEnabled)) {
        return <NotFoundPage/>;
    }
    return <Outlet/>;
}

export function menuHref(menu: Menu): string {
    switch (menu.type) {
        case 'CONTENT':
            return `/contents/${menu.targetId}?menuId=${menu.id}`;
        case 'BOARD':
            return `/boards/${menu.targetId}`;
        case 'ARCHIVE':
            return `/archives/${menu.targetId}`;
        case 'FEATURE':
            return (menu.feature && FEATURE_PATHS[menu.feature]) ?? '#';
        default:
            return menu.link ?? '#';
    }
}
