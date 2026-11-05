import {render, screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthProvider} from '../auth/AuthContext';
import {routes} from '../router';

const SITE = {
    id: 1, name: '우리 기관', description: '소개 문구', domain: null, theme: null, type: null, locale: 'ko', isDefault: true,
    features: {CONTENT: true, BOARD: true, ARCHIVE: true, QNA: false, PRODUCT: false, PROGRAM: true, REPORT: true, SATISFACTION: true},
};

function respond(status: number, body?: unknown) {
    return new Response(body === undefined ? null : JSON.stringify(body), {status});
}

const FEATURE_NAMES = {BOARD: '게시판', ARCHIVE: '아카이브', PRODUCT: '상품', PROGRAM: '프로그램', QNA: '질문답변', REPORT: '신고', MAP: '지도'};
const FEATURE_MENUS = Object.entries(FEATURE_NAMES).map(([feature, name], index) => ({
    id: 100 + index, parentId: null, name, type: 'FEATURE', depth: 0, sequence: 100 + index, link: null, targetId: null,
    feature, isUsing: true, children: [],
}));

function withFeatureMenus(handler: (url: string, init?: RequestInit) => Promise<Response>, featureMenus: unknown[] = FEATURE_MENUS) {
    return vi.fn(async (url: string, init?: RequestInit) => {
        const response = await handler(url, init);
        if (url !== '/api/menus') {
            return response;
        }
        const own = response.ok ? await response.json().catch(() => []) : [];
        return respond(200, [...(Array.isArray(own) ? own : []), ...featureMenus]);
    });
}

function stubApi() {
    vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
        if (url === '/api/site') {
            return respond(200, SITE);
        }
        if (url === '/api/auth/me') {
            return respond(401, {code: 'UNAUTHENTICATED'});
        }
        if (url === '/api/menus' || url.startsWith('/api/banners') || url === '/api/boards' || url === '/api/archives' || url === '/api/contents') {
            return respond(200, []);
        }
        if (url === '/api/configs') {
            return respond(200, {});
        }
        return respond(204);
    }));
}

function renderAt(path: string) {
    render(<AuthProvider><RouterProvider router={createMemoryRouter(routes, {initialEntries: [path]})}/></AuthProvider>);
}

describe('site features', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('asks guests for the chosen contact and a matching lookup password before reporting', async () => {
        const posts: string[] = [];
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string, init?: RequestInit) => {
            if (url === '/api/site') {
                return respond(200, SITE);
            }
            if (url === '/api/reports/guest-posting') {
                return respond(200, {allowed: true, collectPhone: false, collectEmail: true});
            }
            if (url === '/api/reports' && init?.method === 'POST') {
                posts.push(String(init.body));
                return respond(201, {id: 1});
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            return respond(200, url === '/api/configs' ? {} : []);
        }));
        renderAt('/reports/new');

        expect(await screen.findByLabelText(/이메일/)).toBeRequired();
        expect(screen.queryByLabelText(/연락처/)).not.toBeInTheDocument();
        await userEvent.type(screen.getByLabelText(/^이름/), '시민');
        await userEvent.type(screen.getByLabelText(/^조회 비밀번호 \*/), 'pw1234');
        await userEvent.type(screen.getByLabelText(/조회 비밀번호 확인/), 'pw9999');
        await userEvent.type(screen.getByLabelText(/이메일/), 'a@example.com');
        await userEvent.type(screen.getByRole('textbox', {name: /^제목/}), '가로등');
        await userEvent.click(screen.getByRole('button', {name: '접수하기'}));

        expect(await screen.findByText('조회 비밀번호 확인이 일치하지 않습니다.')).toBeInTheDocument();
        expect(posts).toHaveLength(0);
    });

    it('asks guests to log in when guest reports are turned off', async () => {
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, SITE);
            }
            if (url === '/api/reports/guest-posting') {
                return respond(200, {allowed: false});
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            return respond(200, url === '/api/configs' ? {} : []);
        }));
        renderAt('/reports/new');

        expect(await screen.findByText(/신고는 회원만 접수할 수 있습니다/)).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: '접수하기'})).not.toBeInTheDocument();
    });

    it('shows the site name and only the enabled features in the header', async () => {
        stubApi();
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '프로그램'})).toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '상품'})).not.toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '질문답변'})).not.toBeInTheDocument();
        expect(within(nav).getByRole('link', {name: '신고'})).toBeInTheDocument();
        expect(screen.getAllByText('우리 기관').length).toBeGreaterThan(0);
        expect(await screen.findByText('소개 문구')).toBeInTheDocument();
    });

    it('hides content menus such as the intro page when content is switched off', async () => {
        const menus = [
            {id: 1, parentId: null, name: '소개', type: 'CONTENT', depth: 0, sequence: 1, link: null, targetId: 1, children: []},
            {id: 2, parentId: null, name: '외부 링크', type: 'LINK', depth: 0, sequence: 1, link: '/qna', targetId: null, children: []},
        ];
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, {...SITE, features: {...SITE.features, CONTENT: false}});
            }
            if (url === '/api/menus') {
                return respond(200, menus);
            }
            return url === '/api/auth/me' ? respond(401, {code: 'UNAUTHENTICATED'}) : respond(200, []);
        }));
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '외부 링크'})).toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '소개'})).not.toBeInTheDocument();
    });

    it('names the admin console after the site', async () => {
        stubApi();
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            return url === '/api/site' ? respond(200, SITE) : respond(204);
        }));
        renderAt('/admin');

        expect(await screen.findByRole('link', {name: '우리 기관 관리자'})).toBeInTheDocument();
        expect(document.title).toBe('우리 기관');
    });

    it('leaves dashboard tiles of switched-off features out', async () => {
        const dashboard = {
            memberCount: 3, articleCount: 4, unansweredQuestions: 5, pendingPaymentOrders: 1, paidOrders: 2,
            revenueLast30Days: 10000, confirmedReservations: 6, receivedReports: 7, todayVisits: 8,
            last7DaysVisits: [{date: '2026-11-02', visits: 8}],
        };
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            if (url === '/api/admin/dashboard') {
                return respond(200, dashboard);
            }
            return url === '/api/site' ? respond(200, SITE) : respond(200, []);
        }));
        renderAt('/admin');

        expect(await screen.findByText('확정 예약')).toBeInTheDocument();
        expect(screen.getByText('게시글')).toBeInTheDocument();
        expect(screen.queryByText('미답변 질문')).not.toBeInTheDocument();
        expect(screen.queryByText('결제 대기 주문')).not.toBeInTheDocument();
        expect(screen.queryByText('최근 30일 매출')).not.toBeInTheDocument();
    });

    it('drops the admin menus of switched-off features but keeps the site menu', async () => {
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            if (url === '/api/site') {
                return respond(200, {...SITE, features: {...SITE.features, PROGRAM: false}});
            }
            if (url === '/api/admin/dashboard') {
                return respond(200, {
                    memberCount: 0, articleCount: 0, unansweredQuestions: 0, pendingPaymentOrders: 0, paidOrders: 0,
                    revenueLast30Days: 0, confirmedReservations: 0, receivedReports: 0, todayVisits: 0, last7DaysVisits: [],
                });
            }
            return respond(200, []);
        }));
        renderAt('/admin');

        const sidebar = await screen.findByRole('navigation', {name: '관리자 메뉴'});
        await waitFor(() => expect(within(sidebar).queryByRole('link', {name: '프로그램'})).not.toBeInTheDocument());
        expect(within(sidebar).queryByRole('link', {name: /예약/})).not.toBeInTheDocument();
        expect(within(sidebar).queryByRole('link', {name: /할인/})).not.toBeInTheDocument();
        expect(within(sidebar).queryByRole('link', {name: '질문답변'})).not.toBeInTheDocument();
        expect(within(sidebar).queryByRole('link', {name: '결제'})).not.toBeInTheDocument();
        expect(within(sidebar).getByRole('link', {name: '게시판'})).toBeInTheDocument();
        expect(within(sidebar).getByRole('link', {name: '사이트'})).toBeInTheDocument();
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    it('lists boards in the header even without a board menu, without duplicating menu-linked ones', async () => {
        const boards = [
            {id: 1, name: '자유게시판'}, {id: 2, name: '공지사항'},
        ];
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, SITE);
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url === '/api/boards') {
                return respond(200, boards);
            }
            if (url === '/api/menus') {
                return respond(200, [{id: 9, parentId: null, name: '공지', type: 'BOARD', depth: 0, sequence: 1, link: null,
                    targetId: 2, children: []}]);
            }
            return respond(200, []);
        }));
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '게시판'})).toHaveAttribute('href', '/boards/1');
        expect(within(nav).getByRole('link', {name: '공지'})).toHaveAttribute('href', '/boards/2');
        expect(within(nav).queryByRole('link', {name: '공지사항'})).not.toBeInTheDocument();
    });

    it('lists archives in the header but not contents without a menu of their own', async () => {
        const archives = [{id: 3, name: '자료실'}];
        const contents = [{id: 5, title: '기관 소개'}, {id: 6, title: '연혁'}];
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, SITE);
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url === '/api/contents') {
                return respond(200, contents);
            }
            return respond(200, url === '/api/archives' ? archives : []);
        }));
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '아카이브'})).toHaveAttribute('href', '/archives/3');
        expect(within(nav).queryByRole('link', {name: '기관 소개'})).not.toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '연혁'})).not.toBeInTheDocument();
    });

    it('hides the archive header menu once the feature is switched off', async () => {
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, {...SITE, features: {...SITE.features, ARCHIVE: false}});
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            return respond(200, url === '/api/archives' ? [{id: 3, name: '자료실'}] : []);
        }));
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '프로그램'})).toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '아카이브'})).not.toBeInTheDocument();
    });

    it('closes the admin pages of switched-off features', async () => {
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            return url === '/api/site' ? respond(200, SITE) : respond(200, []);
        }));
        renderAt('/admin/questions');

        expect(await screen.findByRole('heading', {name: '페이지를 찾을 수 없습니다'})).toBeInTheDocument();
    });

    it('splits the admin reviews into product and program tabs', async () => {
        const requested: string[] = [];
        const review = (targetType: string) => ({
            id: targetType === 'PRODUCT' ? 1 : 2, targetType, productId: targetType === 'PRODUCT' ? 7 : null,
            programId: targetType === 'PROGRAM' ? 9 : null, rating: 4,
            title: targetType === 'PRODUCT' ? '상품 좋아요' : '강좌 좋아요', contentHtml: '', authorName: '회원',
            canDelete: true, createdAt: '2026-11-02T10:00:00',
        });
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            if (url === '/api/site') {
                return respond(200, {...SITE, features: {...SITE.features, PRODUCT: true}});
            }
            if (url.startsWith('/api/admin/reviews')) {
                requested.push(url);
                const type = url.includes('targetType=PROGRAM') ? 'PROGRAM' : 'PRODUCT';
                return respond(200, {content: [review(type)], page: {size: 20, number: 0, totalElements: 1, totalPages: 1}});
            }
            return respond(200, []);
        }));
        renderAt('/admin/reviews');

        expect(await screen.findByText('상품 좋아요')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: '#7'})).toHaveAttribute('href', '/products/7');

        await userEvent.click(screen.getByRole('tab', {name: '예약 상품 리뷰'}));
        expect(await screen.findByText('강좌 좋아요')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: '#9'})).toHaveAttribute('href', '/programs/9');
        expect(requested.some((url) => url.includes('targetType=PROGRAM'))).toBe(true);
    });

    it('does not offer switched-off features as menu types', async () => {
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/auth/me') {
                return respond(200, {id: 1, username: 'admin', name: '관리자', userType: 'ADMIN'});
            }
            if (url === '/api/site') {
                return respond(200, {...SITE, features: {...SITE.features, BOARD: false}});
            }
            return respond(200, []);
        }));
        renderAt('/admin/menus');

        await userEvent.click(await screen.findByRole('button', {name: '최상위 메뉴 추가'}));
        const select = screen.getByLabelText(/종류/);
        await waitFor(() => expect(within(select).queryByRole('option', {name: '게시판'})).not.toBeInTheDocument());
        expect(within(select).getByRole('option', {name: '컨텐츠'})).toBeInTheDocument();
    });

    it('applies the site theme and shows new products with a search box on the home page', async () => {
        const page = (content: unknown[]) => ({content, page: {size: 20, number: 0, totalElements: content.length, totalPages: 1}});
        const requested: string[] = [];
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            requested.push(url);
            if (url === '/api/site') {
                return respond(200, {...SITE, theme: 'market', features: {...SITE.features, PRODUCT: true}});
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url.startsWith('/api/products')) {
                return respond(200, page([{id: 4, name: '닌텐도 스위치 OLED', description: '박스 풀구성', price: 290000,
                    availableQuantity: 5, isEnabled: true, imageUrl: null, createdAt: '2026-11-02T10:00:00'}]));
            }
            return respond(200, url.startsWith('/api/programs') ? page([]) : []);
        }));
        renderAt('/');

        const card = await screen.findByRole('link', {name: /닌텐도 스위치 OLED/});
        expect(card).toHaveAttribute('href', '/products/4');
        expect(within(card).getByRole('img', {name: '닌텐도 스위치 OLED'})).toBeInTheDocument();
        expect(document.querySelector('.site')).toHaveAttribute('data-theme', 'market');

        await userEvent.type(screen.getByLabelText('상품 검색'), '스위치');
        await userEvent.click(screen.getByRole('button', {name: '검색'}));
        await waitFor(() => expect(requested.some((url) => url.includes('keyword=%EC%8A%A4%EC%9C%84%EC%B9%98'))).toBe(true));
    });

    it('shows the chosen home content with live blocks mounted into its placeholders', async () => {
        const page = (content: unknown[]) => ({content, page: {size: 20, number: 0, totalElements: content.length, totalPages: 1}});
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, {...SITE, homeContentId: 9, features: {...SITE.features, PRODUCT: true}});
            }
            if (url === '/api/auth/me') {
                return respond(401, {code: 'UNAUTHENTICATED'});
            }
            if (url === '/api/contents/9') {
                return respond(200, {id: 9, title: '홈', status: 'PUBLISHED',
                    contentHtml: '<section class="hero"><h1>여름 맞이 대방출</h1><div data-block="product-search"></div></section>'
                        + '<div data-block="new-products"></div><div data-block="unknown"></div>'});
            }
            if (url === '/api/contents') {
                return respond(200, [{id: 9, title: '홈'}]);
            }
            if (url.startsWith('/api/products')) {
                return respond(200, page([{id: 4, name: '닌텐도 스위치', description: null, price: 230000,
                    availableQuantity: 5, isEnabled: true, imageUrl: null, createdAt: '2026-11-02T10:00:00'}]));
            }
            return respond(200, []);
        }));
        renderAt('/');

        expect(await screen.findByRole('heading', {name: '여름 맞이 대방출'})).toBeInTheDocument();
        expect(await screen.findByRole('link', {name: /닌텐도 스위치/})).toBeInTheDocument();
        expect(screen.getByLabelText('상품 검색')).toBeInTheDocument();
        expect(screen.queryByRole('heading', {name: '우리 기관'})).not.toBeInTheDocument();
    });

    it('uses the names and visibility set on the feature menus', async () => {
        const renamed = FEATURE_MENUS.map((menu) => menu.feature === 'PROGRAM' ? {...menu, name: '클래스 신청', sequence: 1}
            : menu.feature === 'REPORT' ? {...menu, isUsing: false} : menu);
        vi.stubGlobal('fetch', withFeatureMenus(async (url: string) => {
            if (url === '/api/site') {
                return respond(200, SITE);
            }
            return url === '/api/auth/me' ? respond(401, {code: 'UNAUTHENTICATED'}) : respond(200, []);
        }, renamed));
        renderAt('/');

        const nav = await screen.findByRole('navigation', {name: '주 메뉴'});
        expect(await within(nav).findByRole('link', {name: '클래스 신청'})).toHaveAttribute('href', '/programs');
        expect(within(nav).queryByRole('link', {name: '프로그램'})).not.toBeInTheDocument();
        expect(within(nav).queryByRole('link', {name: '신고'})).not.toBeInTheDocument();
        expect(within(nav).getByRole('link', {name: '지도'})).toBeInTheDocument();
    });

    it('treats a disabled feature page as not found', async () => {
        stubApi();
        renderAt('/products');

        expect(await screen.findByRole('heading', {name: '페이지를 찾을 수 없습니다'})).toBeInTheDocument();
    });
});
