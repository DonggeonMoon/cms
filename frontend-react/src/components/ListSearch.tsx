import {type FormEvent, useEffect, useState} from 'react';
import {useSearchParams} from 'react-router';

export function ListSearch({label, placeholder, placement = 'top', keepFilters = false}: {
    label: string;
    placeholder: string;
    placement?: 'top' | 'bottom';
    keepFilters?: boolean;
}) {
    const [params, setParams] = useSearchParams();
    const keyword = params.get('keyword') ?? '';
    const [input, setInput] = useState(keyword);

    useEffect(() => setInput(keyword), [keyword]);

    const submit = (event: FormEvent) => {
        event.preventDefault();
        if (!keepFilters) {
            setParams(input.trim() ? {keyword: input.trim()} : {});
            return;
        }
        const next = new URLSearchParams(params);
        next.delete('page');
        if (input.trim()) {
            next.set('keyword', input.trim());
        } else {
            next.delete('keyword');
        }
        setParams(next);
    };

    return (
        <form className={`list-search ${placement}`} role="search" onSubmit={submit}>
            <input aria-label={label} placeholder={placeholder} value={input} onChange={(e) => setInput(e.target.value)}/>
            <button type="submit">검색</button>
        </form>
    );
}

export function pageParams(keyword: string, page: number): Record<string, string> {
    return keyword ? {keyword, page: String(page)} : {page: String(page)};
}
