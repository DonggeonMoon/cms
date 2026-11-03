import Image from '@tiptap/extension-image';
import {TableKit} from '@tiptap/extension-table';
import {type Editor, EditorContent, useEditor} from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import {type ReactNode, useRef, useState} from 'react';
import {fileApi} from '../api/files';
import {ErrorBanner} from './ErrorBanner';
import {HtmlView} from './HtmlView';

type Mode = 'visual' | 'html' | 'preview';

const MODES: { value: Mode; label: string }[] = [
    {value: 'visual', label: '편집기'},
    {value: 'html', label: 'HTML'},
    {value: 'preview', label: '미리보기'},
];

const EDITOR_TAGS = new Set([
    'p', 'br', 'strong', 'b', 'em', 'i', 'u', 's', 'strike', 'del', 'code', 'pre',
    'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 'blockquote', 'hr', 'a', 'img',
    'table', 'colgroup', 'col', 'thead', 'tbody', 'tr', 'th', 'td',
]);
const EDITOR_ATTRIBUTES: Record<string, string[]> = {
    a: ['href', 'target', 'rel'],
    img: ['src', 'alt', 'title', 'width', 'height'],
    table: ['style'],
    col: ['style'],
    th: ['colspan', 'rowspan', 'colwidth', 'style'],
    td: ['colspan', 'rowspan', 'colwidth', 'style'],
};

// 편집기가 모르는 구조(<section class="hero">, data-block 등)는 지워지므로 그런 내용은 HTML 모드로 연다.
export function fitsVisualEditor(html: string): boolean {
    if (!html.trim()) {
        return true;
    }
    const body = new DOMParser().parseFromString(html, 'text/html').body;
    return Array.from(body.querySelectorAll('*')).every((element) => {
        const tag = element.tagName.toLowerCase();
        const allowed = EDITOR_ATTRIBUTES[tag] ?? [];
        return EDITOR_TAGS.has(tag) && Array.from(element.attributes).every((attribute) => allowed.includes(attribute.name));
    });
}

export function RichTextEditor({id, label, value, onChange}: {
    id: string;
    label: string;
    value: string;
    onChange: (html: string) => void;
}) {
    const [mode, setMode] = useState<Mode>(() => (fitsVisualEditor(value) ? 'visual' : 'html'));
    const editor = useEditor({
        extensions: [
            StarterKit.configure({link: {openOnClick: false}}),
            Image,
            TableKit.configure({table: {resizable: false}}),
        ],
        content: mode === 'visual' ? value : '',
        shouldRerenderOnTransaction: true,
        editorProps: {attributes: {id, class: 'rich-text-content', role: 'textbox', 'aria-multiline': 'true', 'aria-label': label}},
        onUpdate: ({editor: current}) => onChange(current.isEmpty ? '' : current.getHTML()),
    });

    const changeMode = (next: Mode) => {
        if (next === 'visual' && mode !== 'visual') {
            if (!fitsVisualEditor(value)
                && !window.confirm('편집기에서 지원하지 않는 HTML 구조(레이아웃, class, 블록 등)가 있습니다. 편집기로 바꾸면 그 부분이 사라질 수 있습니다. 계속할까요?')) {
                return;
            }
            editor?.commands.setContent(value, {emitUpdate: false});
        }
        setMode(next);
    };

    return (
        <div className="rich-text">
            <div className="rich-text-modes" role="group" aria-label={`${label} 편집 방식`}>
                {MODES.map((m) => (
                    <button key={m.value} type="button" aria-pressed={mode === m.value}
                            className={mode === m.value ? 'active' : undefined} onClick={() => changeMode(m.value)}>
                        {m.label}
                    </button>
                ))}
            </div>
            {mode === 'visual' && editor && (
                <div className="rich-text-editor">
                    <Toolbar editor={editor}/>
                    <EditorContent editor={editor}/>
                </div>
            )}
            {mode === 'html' && (
                <textarea id={id} rows={12} className="mono" value={value} aria-label={label}
                          placeholder="<p>내용을 입력하세요</p>" onChange={(e) => onChange(e.target.value)}/>
            )}
            {mode === 'preview' && <div className="html-preview"><HtmlView html={value}/></div>}
        </div>
    );
}

function Toolbar({editor}: { editor: Editor }) {
    const fileInput = useRef<HTMLInputElement>(null);
    const [error, setError] = useState<unknown>(null);
    const chain = () => editor.chain().focus();

    const setLink = () => {
        const previous = editor.getAttributes('link').href as string | undefined;
        const href = window.prompt('링크 주소를 입력하세요. 비우면 링크를 지웁니다.', previous ?? 'https://');
        if (href === null) {
            return;
        }
        if (href.trim() === '') {
            chain().extendMarkRange('link').unsetLink().run();
        } else {
            chain().extendMarkRange('link').setLink({href: href.trim()}).run();
        }
    };

    const uploadImage = async (file: File) => {
        try {
            setError(null);
            const uploaded = await fileApi.upload(file);
            chain().setImage({src: `/api/files/${uploaded.id}/view`, alt: file.name}).run();
        } catch (e) {
            setError(e);
        }
    };

    const tool = (label: string, title: string, active: boolean, run: () => void, disabled = false): ReactNode => (
        <button type="button" title={title} aria-label={title} aria-pressed={active} disabled={disabled}
                className={active ? 'active' : undefined} onMouseDown={(e) => e.preventDefault()} onClick={run}>
            {label}
        </button>
    );

    return (
        <>
            <div className="rich-text-toolbar" role="toolbar" aria-label="서식">
                {tool('B', '굵게', editor.isActive('bold'), () => chain().toggleBold().run())}
                {tool('I', '기울임', editor.isActive('italic'), () => chain().toggleItalic().run())}
                {tool('U', '밑줄', editor.isActive('underline'), () => chain().toggleUnderline().run())}
                {tool('S', '취소선', editor.isActive('strike'), () => chain().toggleStrike().run())}
                <span className="rich-text-divider" aria-hidden="true"/>
                {tool('H2', '제목', editor.isActive('heading', {level: 2}), () => chain().toggleHeading({level: 2}).run())}
                {tool('H3', '소제목', editor.isActive('heading', {level: 3}), () => chain().toggleHeading({level: 3}).run())}
                {tool('•', '글머리 목록', editor.isActive('bulletList'), () => chain().toggleBulletList().run())}
                {tool('1.', '번호 목록', editor.isActive('orderedList'), () => chain().toggleOrderedList().run())}
                {tool('❝', '인용', editor.isActive('blockquote'), () => chain().toggleBlockquote().run())}
                {tool('―', '구분선', false, () => chain().setHorizontalRule().run())}
                <span className="rich-text-divider" aria-hidden="true"/>
                {tool('링크', '링크', editor.isActive('link'), setLink)}
                {tool('이미지', '이미지 올리기', false, () => fileInput.current?.click())}
                {tool('표', '표 넣기', editor.isActive('table'),
                    () => chain().insertTable({rows: 3, cols: 3, withHeaderRow: true}).run(), editor.isActive('table'))}
                {editor.isActive('table') && (
                    <>
                        {tool('+행', '아래에 행 추가', false, () => chain().addRowAfter().run())}
                        {tool('+열', '오른쪽에 열 추가', false, () => chain().addColumnAfter().run())}
                        {tool('−행', '행 삭제', false, () => chain().deleteRow().run())}
                        {tool('−열', '열 삭제', false, () => chain().deleteColumn().run())}
                        {tool('표 삭제', '표 삭제', false, () => chain().deleteTable().run())}
                    </>
                )}
                <span className="rich-text-divider" aria-hidden="true"/>
                {tool('↶', '실행 취소', false, () => chain().undo().run(), !editor.can().undo())}
                {tool('↷', '다시 실행', false, () => chain().redo().run(), !editor.can().redo())}
                <input ref={fileInput} type="file" accept="image/*" hidden onChange={(e) => {
                    const file = e.target.files?.[0];
                    e.target.value = '';
                    if (file) {
                        void uploadImage(file);
                    }
                }}/>
            </div>
            <ErrorBanner error={error}/>
        </>
    );
}
