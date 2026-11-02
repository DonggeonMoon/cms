import {useEffect, useLayoutEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import {Link} from 'react-router';
import type {Banner} from '../api/domain';
import {siteApi} from '../api/endpoints';
import {dismissForDays, dismissLabel, isDismissed} from '../components/dismissal';
import {Greeting, HOME_BLOCKS, LatestPosts, NewProducts, OpenPrograms, ProductSearch, Shortcuts} from '../components/HomeBlocks';
import {useSite} from '../components/SiteContext';

export function HomePage() {
    const {site, siteLoaded, isEnabled} = useSite();
    const homeContentId = isEnabled('CONTENT') ? site?.homeContentId : null;
    const [landing, setLanding] = useState<{ id: number; html: string } | 'none' | null>(null);

    useEffect(() => {
        if (!siteLoaded) {
            return;
        }
        if (!homeContentId) {
            setLanding('none');
            return;
        }
        siteApi.content(homeContentId)
            .then((content) => setLanding({id: content.id, html: content.contentHtml}))
            .catch(() => setLanding('none'));
    }, [siteLoaded, homeContentId]);

    return (
        <>
            {landing === 'none' && <DefaultHome/>}
            {landing && landing !== 'none' && <LandingContent key={landing.id} html={landing.html}/>}
            <PopupBanners/>
        </>
    );
}

function DefaultHome() {
    const {site, name} = useSite();
    return (
        <>
            <section className="hero">
                <h1>{name}</h1>
                {site?.description && <p className="hero-description">{site.description}</p>}
                <ProductSearch/>
                <Greeting/>
            </section>
            <Shortcuts/>
            <NewProducts/>
            <div className="home-columns">
                <OpenPrograms/>
                <LatestPosts/>
            </div>
        </>
    );
}

function LandingContent({html}: { html: string }) {
    const ref = useRef<HTMLDivElement>(null);
    const [slots, setSlots] = useState<{ element: Element; name: string }[]>([]);

    useLayoutEffect(() => {
        const found = Array.from(ref.current?.querySelectorAll('[data-block]') ?? [])
            .map((element) => ({element, name: element.getAttribute('data-block') ?? ''}))
            .filter((slot) => slot.name in HOME_BLOCKS);
        setSlots(found);
    }, [html]);

    return (
        <>
            <div ref={ref} className="html-content landing" dangerouslySetInnerHTML={{__html: html}}/>
            {slots.map((slot, index) => createPortal(HOME_BLOCKS[slot.name].render(), slot.element, `${slot.name}-${index}`))}
        </>
    );
}

const POPUP_DISMISS_KEY = 'cms.dismissedPopups';

function PopupBanners() {
    const [popups, setPopups] = useState<Banner[]>([]);

    useEffect(() => {
        siteApi.banners('POPUP')
            .then((list) => setPopups((Array.isArray(list) ? list : []).filter((popup) => !isDismissed(POPUP_DISMISS_KEY, popup.id))))
            .catch(() => setPopups([]));
    }, []);

    const close = (popup: Banner, dismiss: boolean) => {
        if (dismiss) {
            dismissForDays(POPUP_DISMISS_KEY, popup.id, popup.dismissDays);
        }
        setPopups((prev) => prev.filter((other) => other.id !== popup.id));
    };

    const modal = popups.find((popup) => popup.isModal);
    const modeless = popups.filter((popup) => !popup.isModal);

    return (
        <>
            {modeless.map((popup) => (
                <div key={popup.id} className="popup" role="dialog" aria-label={popup.name}
                     style={{top: popup.top ?? 80, left: popup.left ?? 40, width: popup.width ?? undefined}}>
                    <PopupBody popup={popup}/>
                    <PopupActions dismissDays={popup.dismissDays} onClose={(dismiss) => close(popup, dismiss)}/>
                </div>
            ))}
            {modal && <ModalPopup popup={modal} onClose={(dismiss) => close(modal, dismiss)}/>}
        </>
    );
}

function ModalPopup({popup, onClose}: { popup: Banner; onClose: (dismiss: boolean) => void }) {
    useEffect(() => {
        const previous = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = previous;
        };
    }, []);

    return (
        <div className="modal-backdrop" data-testid="modal-backdrop">
            <div className="popup modal-popup" role="dialog" aria-modal="true" aria-label={popup.name}
                 style={{width: popup.width ?? undefined}}>
                <PopupBody popup={popup}/>
                <PopupActions dismissDays={popup.dismissDays} onClose={onClose}/>
            </div>
        </div>
    );
}

function PopupBody({popup}: { popup: Banner }) {
    const body = popup.imageUrl
        ? <img src={popup.imageUrl} alt={popup.name} style={{maxHeight: popup.height ?? undefined}}/>
        : <p className="pre-line">{popup.description ?? popup.name}</p>;
    return popup.link
        ? <a href={popup.link} target={popup.opensNewWindow ? '_blank' : undefined} rel="noreferrer">{body}</a>
        : body;
}

function PopupActions({dismissDays, onClose}: { dismissDays: number; onClose: (dismiss: boolean) => void }) {
    return (
        <div className="toolbar space-between">
            {dismissDays > 0
                ? <button type="button" className="link-button" onClick={() => onClose(true)}>{dismissLabel(dismissDays)}</button>
                : <span/>}
            <button type="button" onClick={() => onClose(false)}>닫기</button>
        </div>
    );
}

export function NotFoundPage() {
    return (
        <section className="card">
            <h1>페이지를 찾을 수 없습니다</h1>
            <Link to="/">홈으로</Link>
        </section>
    );
}
