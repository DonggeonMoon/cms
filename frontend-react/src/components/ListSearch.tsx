import {type FormEvent, useEffect, useState} from 'react';
import {useSearchParams} from 'react-router';

export function ListSearch({label, placeholder, placement = 'top'}: {
    label: string;
    placeholder: string;
    placement?: 'top' | 'bottom';
}) {
    const [params, setParams] = useSearchParams();
    const keyword = params.get('keyword') ?? '';
    const [input, setInput] = useState(keyword);

    useEffect(() => setInput(keyword), [keyword]);

    const submit = (event: FormEvent) => {
        event.preventDefault();
        setParams(input.trim() ? {keyword: input.trim()} : {});
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
