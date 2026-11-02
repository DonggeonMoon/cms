export function HtmlView({html}: { html: string | null | undefined }) {
    return <div className="html-content" dangerouslySetInnerHTML={{__html: html ?? ''}}/>;
}
