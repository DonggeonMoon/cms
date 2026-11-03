// 모든 후보 라벨을 같은 칸에 겹쳐 두어 버튼 너비가 가장 긴 라벨에 고정되게 한다.
export function StableLabel({current, labels}: { current: string; labels: string[] }) {
    return (
        <span className="stable-label">
            {[...new Set([current, ...labels])].map((label) => (
                <span key={label} aria-hidden={label === current ? undefined : true}
                      className={label === current ? undefined : 'stable-label-ghost'}>{label}</span>
            ))}
        </span>
    );
}
