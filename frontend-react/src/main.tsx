import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {RouterProvider} from 'react-router';
import {router} from './router';
import 'pretendard/dist/web/variable/pretendardvariable-dynamic-subset.css';
import './styles.css';

document.addEventListener('wheel', () => {
    const active = document.activeElement;
    if (active instanceof HTMLInputElement && active.type === 'number') {
        active.blur();
    }
}, {passive: true});

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <RouterProvider router={router}/>
    </StrictMode>,
);
