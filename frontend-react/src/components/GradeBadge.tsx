import {useEffect, useState} from 'react';
import {type Grade, gradeApi, gradeNameOf} from '../api/grades';
import {useI18n} from '../api/i18n';

let cached: Promise<Grade[]> | null = null;

function loadGradesOnce(): Promise<Grade[]> {
    cached ??= gradeApi.list().then((grades) => (Array.isArray(grades) ? grades : [])).catch(() => {
        cached = null;
        return [];
    });
    return cached;
}

export function GradeBadge({gradeId}: { gradeId: number | null | undefined }) {
    const {t} = useI18n();
    const [name, setName] = useState<string | null>(null);
    useEffect(() => {
        if (gradeId == null) {
            setName(null);
            return;
        }
        let alive = true;
        loadGradesOnce().then((grades) => alive && setName(gradeNameOf(grades, gradeId)));
        return () => {
            alive = false;
        };
    }, [gradeId]);
    return name ? <span className="badge grade">{t('shop.gradeBadge.orAbove', '{name} 이상', {name})}</span> : null;
}
