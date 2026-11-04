import {useEffect, useState} from 'react';

export type ColorMode = 'system' | 'light' | 'dark';

const LABEL: Record<ColorMode, string> = {system: '시스템', light: '라이트', dark: '다크'};
const ICON: Record<ColorMode, string> = {system: '◐', light: '☀', dark: '☾'};
const NEXT: Record<ColorMode, ColorMode> = {system: 'light', light: 'dark', dark: 'system'};

function readStored(key: string): ColorMode {
    try {
        const stored = localStorage.getItem(key);
        return stored === 'light' || stored === 'dark' ? stored : 'system';
    } catch {
        return 'system';
    }
}

/** html 요소의 data-color-mode 로 적용한다. system 이면 속성을 지워 운영체제 설정을 따른다. */
function apply(mode: ColorMode) {
    const root = document.documentElement;
    if (mode === 'system') {
        delete root.dataset.colorMode;
    } else {
        root.dataset.colorMode = mode;
    }
}

/**
 * 화면 모드를 이 브라우저에 기억해 적용한다. allowDark 가 false 면(사이트에서 다크 모드를 끈 경우) 항상 라이트.
 * 영역(관리자/사이트)마다 저장 키를 따로 써서 서로 영향을 주지 않는다.
 */
export function useColorMode(storageKey: string, allowDark = true): [ColorMode, (mode: ColorMode) => void] {
    const [mode, setMode] = useState<ColorMode>(() => readStored(storageKey));
    useEffect(() => {
        apply(allowDark ? mode : 'light');
    }, [mode, allowDark]);
    useEffect(() => () => apply('system'), []);
    const change = (next: ColorMode) => {
        setMode(next);
        try {
            localStorage.setItem(storageKey, next);
        } catch {
            // 저장할 수 없는 환경(사생활 보호 모드 등)에서는 이번 화면에만 적용한다.
        }
    };
    return [mode, change];
}

export function ColorModeToggle({mode, onChange}: { mode: ColorMode; onChange: (mode: ColorMode) => void }) {
    return (
        <button type="button" className="color-mode-toggle" aria-label={`화면 모드: ${LABEL[mode]} (눌러서 ${LABEL[NEXT[mode]]}로 바꾸기)`}
                title={`화면 모드: ${LABEL[mode]}`} onClick={() => onChange(NEXT[mode])}>
            <span aria-hidden="true">{ICON[mode]}</span> {LABEL[mode]}
        </button>
    );
}
