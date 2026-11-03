import {type FormEvent, useEffect, useRef, useState} from 'react';
import type {ChatMessage} from '../api/domain';
import {chatbotApi} from '../api/endpoints';
import {ErrorBanner} from './ErrorBanner';

const MAX_HISTORY = 20;

export function ChatbotWidget() {
    const [status, setStatus] = useState<{ enabled: boolean; welcomeMessage: string | null } | null>(null);
    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState('');
    const [sending, setSending] = useState(false);
    const [error, setError] = useState<unknown>(null);
    const listRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        chatbotApi.status().then(setStatus).catch(() => setStatus(null));
    }, []);

    useEffect(() => {
        if (listRef.current) {
            listRef.current.scrollTop = listRef.current.scrollHeight;
        }
    }, [messages, open, sending]);

    if (!status?.enabled) {
        return null;
    }

    const send = async (event: FormEvent) => {
        event.preventDefault();
        const text = input.trim();
        if (!text || sending) {
            return;
        }
        const next: ChatMessage[] = [...messages, {role: 'USER', content: text}];
        setMessages(next);
        setInput('');
        setSending(true);
        setError(null);
        try {
            const reply = await chatbotApi.send(next.slice(-MAX_HISTORY));
            setMessages([...next, {role: 'ASSISTANT', content: reply}]);
        } catch (e) {
            setError(e);
        } finally {
            setSending(false);
        }
    };

    return (
        <div className="chatbot">
            {open && (
                <section className="chatbot-panel" aria-label="챗봇 상담">
                    <header className="chatbot-header">
                        <strong>무엇이든 물어보세요</strong>
                        <button type="button" className="link-button" aria-label="챗봇 닫기" onClick={() => setOpen(false)}>✕</button>
                    </header>
                    <div className="chatbot-messages" ref={listRef} aria-live="polite">
                        <p className="chatbot-bubble assistant">{status.welcomeMessage || '안녕하세요! 무엇을 도와드릴까요?'}</p>
                        {messages.map((message, index) => (
                            <p key={index} className={`chatbot-bubble ${message.role === 'USER' ? 'user' : 'assistant'}`}>{message.content}</p>
                        ))}
                        {sending && <p className="chatbot-bubble assistant muted">답변을 작성하고 있어요…</p>}
                    </div>
                    <ErrorBanner error={error}/>
                    <form className="chatbot-form" onSubmit={send}>
                        <input aria-label="챗봇에게 보낼 메시지" placeholder="메시지를 입력하세요" maxLength={2000} value={input}
                               onChange={(e) => setInput(e.target.value)}/>
                        <button type="submit" className="primary" disabled={sending || !input.trim()}>보내기</button>
                    </form>
                    <p className="chatbot-note muted small">AI 답변은 정확하지 않을 수 있습니다. 개인정보는 입력하지 마세요.</p>
                </section>
            )}
            <button type="button" className="chatbot-toggle primary" aria-expanded={open} onClick={() => setOpen(!open)}>
                {open ? '닫기' : '💬 상담'}
            </button>
        </div>
    );
}
