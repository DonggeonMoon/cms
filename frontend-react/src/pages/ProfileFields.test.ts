import {describe, expect, it} from 'vitest';
import {toIsoDate} from './ProfileFields';

describe('toIsoDate', () => {
    it('reads 8 and 6 digit birth dates', () => {
        expect(toIsoDate('19900410', 8)).toBe('1990-04-10');
        expect(toIsoDate('900410', 6)).toBe('1990-04-10');
        expect(toIsoDate('050101', 6)).toBe('2005-01-01');
    });

    it('rejects incomplete, impossible and future dates', () => {
        expect(toIsoDate('1990041', 8)).toBeNull();
        expect(toIsoDate('19900231', 8)).toBeNull();
        expect(toIsoDate('20991231', 8)).toBeNull();
        expect(toIsoDate('18991231', 8)).toBeNull();
    });
});
