// HTML coming from the API was sanitized server-side (OWASP policy) before it was stored
export function HtmlView({html}: { html: string | null | undefined }) {
    return <div className="html-content" dangerouslySetInnerHTML={{__html: html ?? ''}}/>;
}
