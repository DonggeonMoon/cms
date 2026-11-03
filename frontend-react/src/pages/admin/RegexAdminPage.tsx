import {type ReactNode, useCallback, useEffect, useState} from 'react';
import {Link} from 'react-router';
import {type RegexRule, regexAdminApi, type RegexTestResult} from '../../api/regex';
import {ErrorBanner} from '../../components/ErrorBanner';
import {formatDateTime} from '../../components/format';
import {type FieldSpec, type FormValues, SchemaForm} from '../../components/SchemaForm';

const FIELDS: FieldSpec[] = [
    {name: 'name', label: '이름', required: true},
    {name: 'pattern', label: '정규식', required: true, wide: true, placeholder: '^\\d{5}$',
        help: '값 전체가 맞아야 통과합니다. Java 정규식 문법을 따릅니다.'},
    {name: 'message', label: '맞지 않을 때 메시지', required: true, wide: true},
    {name: 'hint', label: '입력 안내 (입력란에 흐리게 표시)', wide: true},
    {name: 'description', label: '메모', type: 'textarea'},
];

const toForm = (rule: RegexRule): FormValues => ({
    name: rule.name, pattern: rule.pattern, message: rule.message, hint: rule.hint ?? '', description: rule.description ?? '',
});

export function RegexAdminPage() {
    const [rules, setRules] = useState<RegexRule[] | null>(null);
    const [editing, setEditing] = useState<{ rule: RegexRule | null } | null>(null);
    const [testingId, setTestingId] = useState<number | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        regexAdminApi.list().then(setRules).catch(setError);
    }, []);
    useEffect(reload, [reload]);

    const form = (rule: RegexRule | null) => (
        <SchemaForm
            key={rule?.id ?? 'new'}
            title={rule ? `'${rule.name}' 정규식 수정` : '정규식 등록'}
            fields={FIELDS}
            initial={rule ? toForm(rule) : {}}
            onCancel={() => setEditing(null)}
            onSubmit={async (values) => {
                await (rule ? regexAdminApi.update(rule.id, values) : regexAdminApi.create(values));
                setEditing(null);
                reload();
            }}
        />
    );

    return (
        <section className="card">
            <h1>정규식</h1>
            <div className="muted page-description">
                <p>입력값과 번호 형식을 검사할 정규식을 등록·수정하고 테스트합니다. 어디에 쓸지는 각 설정에서 고릅니다:
                    회원 입력값은 <Link to="/admin/join-settings">회원가입 설정</Link>, 주문·예약·결제번호는 각 메뉴 상단입니다.</p>
                <p>사용 중인 정규식을 고치면 바로 반영됩니다. 번호 형식으로 쓰이는 정규식은 시스템이 만드는 번호와 계속 맞아야 저장됩니다.</p>
            </div>
            <div className="toolbar">
                <button type="button" className="primary push-right" onClick={() => setEditing({rule: null})}>정규식 등록</button>
            </div>
            {editing && !editing.rule && form(null)}
            <ErrorBanner error={error}/>
            {rules && (
                <div className="table-wrap">
                    <table className="regex-table">
                        <thead>
                        <tr>
                            <th>이름</th>
                            <th>정규식</th>
                            <th>사용처</th>
                            <th>수정일</th>
                            <th/>
                        </tr>
                        </thead>
                        <tbody>
                        {rules.map((rule) => (
                            <RegexRow key={rule.id} rule={rule}
                                      editing={editing?.rule?.id === rule.id ? form(rule) : null}
                                      testing={testingId === rule.id}
                                      onEdit={() => setEditing({rule})}
                                      onTest={() => setTestingId(testingId === rule.id ? null : rule.id)}/>
                        ))}
                        {rules.length === 0 && (
                            <tr>
                                <td colSpan={5} className="muted">등록된 정규식이 없습니다.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}

function RegexRow({rule, editing, testing, onEdit, onTest}: {
    rule: RegexRule; editing: ReactNode; testing: boolean; onEdit: () => void; onTest: () => void;
}) {
    return (
        <>
            <tr>
                <td>
                    <strong>{rule.name}</strong>
                    {rule.description && <div className="muted small">{rule.description}</div>}
                </td>
                <td>
                    <code className="regex-pattern">{rule.pattern}</code>
                    <div className="muted small">{rule.message}</div>
                </td>
                <td>
                    {rule.usedBy.length > 0
                        ? rule.usedBy.map((label) => <span key={label} className="badge ok">{label}</span>)
                        : <span className="muted small">사용 안 함</span>}
                </td>
                <td className="small">{formatDateTime(rule.modifiedAt)}</td>
                <td className="actions">
                    <button type="button" onClick={onTest}>{testing ? '테스트 닫기' : '테스트'}</button>
                    <button type="button" onClick={onEdit}>수정</button>
                </td>
            </tr>
            {(editing || testing) && (
                <tr className="expanded-row">
                    <td colSpan={5}>
                        {editing}
                        {testing && <RegexTester pattern={rule.pattern}/>}
                    </td>
                </tr>
            )}
        </>
    );
}

function RegexTester({pattern}: { pattern: string }) {
    const [value, setValue] = useState('');
    const [result, setResult] = useState<RegexTestResult | null>(null);
    const [error, setError] = useState<unknown>(null);

    useEffect(() => {
        if (value === '') {
            setResult(null);
            return;
        }
        const timer = window.setTimeout(() => {
            regexAdminApi.test(pattern, value).then(setResult).catch(setError);
        }, 250);
        return () => window.clearTimeout(timer);
    }, [pattern, value]);

    return (
        <div className="regex-tester">
            <label htmlFor="regex-test-value">테스트할 값</label>
            <div className="row align-center">
                <input id="regex-test-value" value={value} onChange={(e) => setValue(e.target.value)} placeholder="값을 입력하면 바로 검사합니다"/>
                {result && (result.valid
                    ? <span className={`badge${result.matches ? ' ok' : ' danger'}`}>{result.matches ? '일치' : '불일치'}</span>
                    : <span className="badge danger">정규식 오류: {result.error}</span>)}
            </div>
            <ErrorBanner error={error}/>
        </div>
    );
}
