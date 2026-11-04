import {useEffect, useState} from 'react';
import {type Grade, gradeApi, gradeNameOf} from '../api/grades';

let cached: Promise<Grade[]> | null = null;

function loadGradesOnce(): Promise<Grade[]> {
    cached ??= gradeApi.list().then((grades) => (Array.isArray(grades) ? grades : [])).catch(() => {
        cached = null;
        return [];
    });
    return cached;
}

export function GradeBadge({gradeId}: { gradeId: number | null | undefined }) {
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
    return name ? <span className="badge grade">{name} 이상</span> : null;
}
