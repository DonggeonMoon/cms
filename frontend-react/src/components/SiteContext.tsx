import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {Outlet} from 'react-router';
import type {Archive, Board, ContentSummary, Menu, Site, SiteFeature} from '../api/domain';
import {archiveApi, boardApi, siteApi} from '../api/endpoints';
import {NotFoundPage} from '../pages/HomePage';

const FALLBACK_NAME = 'cms';

interface SiteState {
    site: Site | null;
    siteLoaded: boolean;
    name: string;
    menus: Menu[];
    // contents, boards and archives not already reachable through an admin-defined menu; the header lists them on its own
    unlistedContents: ContentSummary[];
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
    unlistedContents: [],
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
    const [contents, setContents] = useState<ContentSummary[]>([]);
    const [boards, setBoards] = useState<Board[]>([]);
    const [archives, setArchives] = useState<Archive[]>([]);
    const [configs, setConfigs] = useState<Record<string, string>>({});

    const reload = useCallback(() => {
        siteApi.current().then((loaded) => setSite(loaded ?? null)).catch(() => setSite(null)).finally(() => setSiteLoaded(true));
        siteApi.menus().then((loaded) => setMenus(Array.isArray(loaded) ? loaded : [])).catch(() => setMenus([]));
        siteApi.configs().then((loaded) => setConfigs(loaded ?? {})).catch(() => setConfigs({}));
        siteApi.contents().then((loaded) => setContents(Array.isArray(loaded) ? loaded : [])).catch(() => setContents([]));
        boardApi.list().then((loaded) => setBoards(Array.isArray(loaded) ? loaded : [])).catch(() => setBoards([]));
        archiveApi.list().then((loaded) => setArchives(Array.isArray(loaded) ? loaded : [])).catch(() => setArchives([]));
    }, []);

    useEffect(reload, [reload]);

    const name = site?.name || FALLBACK_NAME;
    useEffect(() => {
        document.title = name;
    }, [name]);

    const value = useMemo<SiteState>(() => {
        const isEnabled = (feature: SiteFeature) => site?.features?.[feature] ?? true;
        return {
            site,
            siteLoaded,
            name,
            menus: visibleMenus(menus, isEnabled),
            unlistedContents: isEnabled('CONTENT')
                ? contents.filter((content) => content.id !== site?.homeContentId && !linksTo(menus, `/contents/${content.id}`))
                : [],
            unlistedBoards: isEnabled('BOARD') ? boards.filter((board) => !linksTo(menus, `/boards/${board.id}`)) : [],
            unlistedArchives: isEnabled('ARCHIVE') ? archives.filter((archive) => !linksTo(menus, `/archives/${archive.id}`)) : [],
            configs,
            isEnabled,
            reload,
        };
    }, [site, siteLoaded, name, menus, contents, boards, archives, configs, reload]);

    return <SiteContext.Provider value={value}>{children}</SiteContext.Provider>;
}

const MENU_FEATURE: Partial<Record<Menu['type'], SiteFeature>> = {CONTENT: 'CONTENT', BOARD: 'BOARD'};

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

export function useSite() {
    return useContext(SiteContext);
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
        default:
            return menu.link ?? '#';
    }
}
