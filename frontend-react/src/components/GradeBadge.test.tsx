import {render, renderHook, screen, waitFor} from '@testing-library/react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {useWithGradeFields} from '../api/grades';
import {GradeBadge} from './GradeBadge';
import type {FieldSpec} from './SchemaForm';

const GRADES = [{id: 1, name: '일반', description: null, rank: 0, isDefault: true}, {id: 2, name: '프리미엄', description: null, rank: 10, isDefault: false}];
const BASE: FieldSpec[] = [{name: 'title', label: '제목'}, {name: 'contentHtml', label: '내용', type: 'html'}];
const gradeFields = () => [{name: 'accessGradeId', label: '열람 등급'}];

describe('member grades', () => {
    afterEach(() => vi.unstubAllGlobals());

    it('labels gated items with the required grade', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => new Response(JSON.stringify(GRADES), {status: 200})));
        render(<><GradeBadge gradeId={2}/><GradeBadge gradeId={null}/></>);
        expect(await screen.findByText('프리미엄 이상')).toBeInTheDocument();
        expect(screen.queryByText('일반 이상')).not.toBeInTheDocument();
    });

    it('puts grade fields before the body editor once grades exist', async () => {
        vi.stubGlobal('fetch', vi.fn(async () => new Response(JSON.stringify(GRADES), {status: 200})));
        const {result} = renderHook(() => useWithGradeFields(BASE, gradeFields));
        await waitFor(() => expect(result.current.map((field) => field.name)).toEqual(['title', 'accessGradeId', 'contentHtml']));
    });
});
