import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {afterEach, describe, expect, it} from 'vitest';
import {ColorModeToggle, useColorMode} from './ColorMode';

function Harness({allowDark = true}: { allowDark?: boolean }) {
    const [mode, setMode] = useColorMode('test-color-mode', allowDark);
    return <ColorModeToggle mode={mode} onChange={setMode}/>;
}

describe('color mode', () => {
    afterEach(() => {
        localStorage.clear();
        delete document.documentElement.dataset.colorMode;
    });

    it('cycles system → light → dark and remembers the choice', async () => {
        const {unmount} = render(<Harness/>);
        expect(document.documentElement.dataset.colorMode).toBeUndefined();
        await userEvent.click(screen.getByRole('button', {name: /화면 모드: 시스템/}));
        expect(document.documentElement.dataset.colorMode).toBe('light');
        await userEvent.click(screen.getByRole('button', {name: /화면 모드: 라이트/}));
        expect(document.documentElement.dataset.colorMode).toBe('dark');
        expect(localStorage.getItem('test-color-mode')).toBe('dark');
        unmount();

        render(<Harness/>);
        expect(screen.getByRole('button', {name: /화면 모드: 다크/})).toBeInTheDocument();
        expect(document.documentElement.dataset.colorMode).toBe('dark');
    });

    it('stays light when the site turned dark mode off', () => {
        localStorage.setItem('test-color-mode', 'dark');
        render(<Harness allowDark={false}/>);
        expect(document.documentElement.dataset.colorMode).toBe('light');
    });
});
