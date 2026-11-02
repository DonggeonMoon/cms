// stands in for a missing image with a tinted tile and the item's initial, so listings without photos still look tidy
export function Thumbnail({src, name}: { src: string | null | undefined; name: string }) {
    if (src) {
        return <img className="thumbnail" src={src} alt={name}/>;
    }
    const hue = [...name].reduce((sum, char) => sum + char.charCodeAt(0), 0) % 360;
    return (
        <div className="thumbnail thumbnail-empty" role="img" aria-label={name}
             style={{background: `linear-gradient(135deg, hsl(${hue} 70% 88%), hsl(${(hue + 40) % 360} 65% 78%))`}}>
            <span style={{color: `hsl(${hue} 45% 32%)`}}>{name.trim().charAt(0)}</span>
        </div>
    );
}
