import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it, vi} from 'vitest';
import {fitsVisualEditor, RichTextEditor} from './RichTextEditor';

describe('rich text editor', () => {
    it('knows which HTML the visual editor can keep', () => {
        expect(fitsVisualEditor('')).toBe(true);
        expect(fitsVisualEditor('<h2>제목</h2><p><strong>굵게</strong> <a href="/x">링크</a></p><img src="/a.png" alt="">')).toBe(true);
        expect(fitsVisualEditor('<section class="hero"><h1>배너</h1></section>')).toBe(false);
        expect(fitsVisualEditor('<div data-block="new-products"></div>')).toBe(false);
        expect(fitsVisualEditor('<p style="color:red">빨강</p>')).toBe(false);
    });

    it('opens plain content in the visual editor with a toolbar', () => {
        render(<RichTextEditor id="body" label="내용" value="<p>안녕하세요</p>" onChange={() => undefined}/>);

        expect(screen.getByRole('toolbar', {name: '서식'})).toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: '내용'})).toHaveTextContent('안녕하세요');
        expect(screen.getByRole('button', {name: '편집기'})).toHaveAttribute('aria-pressed', 'true');
    });

    it('keeps custom layouts in HTML mode and asks before switching', async () => {
        const html = '<section class="hero"><div data-block="new-products"></div></section>';
        const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
        render(<RichTextEditor id="body" label="내용" value={html} onChange={() => undefined}/>);

        expect(screen.getByRole('textbox', {name: '내용'})).toHaveValue(html);
        await userEvent.click(screen.getByRole('button', {name: '편집기'}));

        expect(confirm).toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: '내용'})).toHaveValue(html);
        confirm.mockRestore();
    });
});
