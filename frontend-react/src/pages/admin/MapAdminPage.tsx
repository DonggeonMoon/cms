import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {Branch, MapProvider, MapSetting} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import type {Page} from '../../api/types';
import {YesNo} from '../../components/AdminCrudPage';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox, Field} from '../../components/Field';
import {geocode} from '../../components/map/mapSdk';
import {MapView} from '../../components/map/MapView';
import {Pagination} from '../../components/Pagination';

type Tab = 'directions' | 'branches';

interface SettingForm {
    provider: MapProvider;
    kakaoAppKey: string;
    naverClientId: string;
    directionsEnabled: boolean;
    branchesEnabled: boolean;
    officeName: string;
    officeAddress: string;
    officeDetailAddress: string;
    officeLatitude: string;
    officeLongitude: string;
    officePhoneNumber: string;
    officeBusinessHours: string;
    officeDescription: string;
}

const toForm = (setting: MapSetting): SettingForm => ({
    provider: setting.provider,
    kakaoAppKey: setting.kakaoAppKey ?? '',
    naverClientId: setting.naverClientId ?? '',
    directionsEnabled: setting.directionsEnabled,
    branchesEnabled: setting.branchesEnabled,
    officeName: setting.office.name ?? '',
    officeAddress: setting.office.address ?? '',
    officeDetailAddress: setting.office.detailAddress ?? '',
    officeLatitude: setting.office.latitude?.toString() ?? '',
    officeLongitude: setting.office.longitude?.toString() ?? '',
    officePhoneNumber: setting.office.phoneNumber ?? '',
    officeBusinessHours: setting.office.businessHours ?? '',
    officeDescription: setting.office.description ?? '',
});

const toNumber = (value: string) => (value.trim() === '' ? null : Number(value));

export function MapAdminPage() {
    const [saved, setSaved] = useState<MapSetting | null>(null);
    const [form, setForm] = useState<SettingForm | null>(null);
    const [tab, setTab] = useState<Tab>('directions');
    const [error, setError] = useState<unknown>(null);
    const [message, setMessage] = useState('');

    useEffect(() => {
        adminApi.map.settings().then((setting) => {
            setSaved(setting);
            setForm(toForm(setting));
        }).catch(setError);
    }, []);

    if (!form || !saved) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }

    const set = <K extends keyof SettingForm>(key: K, value: SettingForm[K]) => setForm({...form, [key]: value});
    const apiKey = (form.provider === 'KAKAO' ? form.kakaoAppKey : form.naverClientId).trim() || null;

    const save = async (event?: FormEvent) => {
        event?.preventDefault();
        setError(null);
        setMessage('');
        try {
            const updated = await adminApi.map.updateSettings({
                ...form,
                officeLatitude: toNumber(form.officeLatitude),
                officeLongitude: toNumber(form.officeLongitude),
            });
            setSaved(updated);
            setForm(toForm(updated));
            setMessage('저장했습니다.');
        } catch (e) {
            setError(e);
        }
    };

    return (
        <section className="card">
            <h1>지도</h1>
            <p className="muted">사용자 화면의 지도 페이지(/map)에 표시됩니다. 켜진 탭만 사용자에게 보입니다.</p>

            <form className="inline-form" onSubmit={save}>
                <h2>지도 설정</h2>
                <div className="field">
                    <label htmlFor="provider">지도 제공자</label>
                    <select id="provider" value={form.provider} onChange={(e) => set('provider', e.target.value as MapProvider)}>
                        <option value="KAKAO">카카오맵</option>
                        <option value="NAVER">네이버 지도</option>
                    </select>
                </div>
                {form.provider === 'KAKAO' ? (
                    <Field label="카카오맵 JavaScript 키" name="kakaoAppKey" value={form.kakaoAppKey}
                           onChange={(e) => set('kakaoAppKey', e.target.value)}/>
                ) : (
                    <Field label="네이버 지도 Client ID (ncpKeyId)" name="naverClientId" value={form.naverClientId}
                           onChange={(e) => set('naverClientId', e.target.value)}/>
                )}
                <p className="muted small">
                    {form.provider === 'KAKAO'
                        ? 'Kakao Developers에서 앱을 만들고 JavaScript 키를 입력하세요. 플랫폼 > Web 에 사이트 도메인을 등록해야 지도가 표시됩니다.'
                        : '네이버 클라우드 플랫폼 Maps에서 Application을 만들고 Client ID를 입력하세요. 서비스 URL 등록과 Dynamic Map·Geocoding 사용 설정이 필요합니다.'}
                    {' '}키를 입력하지 않으면 사용자 화면에는 지도 대신 지도 앱 링크가 표시됩니다.
                </p>
                <div className="toolbar form-actions">
                    <button type="submit" className="primary">저장</button>
                </div>
            </form>

            <div className="tabs" role="tablist" aria-label="지도 메뉴">
                {([['directions', '오시는 길'], ['branches', '지점 안내']] as const).map(([value, label]) => (
                    <button key={value} type="button" role="tab" aria-selected={tab === value}
                            className={tab === value ? 'tab active' : 'tab'} onClick={() => setTab(value)}>
                        {label}{' '}
                        {(value === 'directions' ? saved.directionsEnabled : saved.branchesEnabled)
                            ? <span className="badge ok">사용 중</span>
                            : <span className="badge">사용 안 함</span>}
                    </button>
                ))}
            </div>

            {tab === 'directions' && (
                <form onSubmit={save}>
                    <Checkbox label="오시는 길 사용" name="directionsEnabled" checked={form.directionsEnabled}
                              onChange={(e) => set('directionsEnabled', e.target.checked)}/>
                    <p className="muted small">켜면 사용자 지도 페이지에 본점 위치 지도가 표시됩니다. 주소와 좌표가 필요합니다.</p>
                    <div className="row">
                        <Field label="본점 이름" name="officeName" value={form.officeName} onChange={(e) => set('officeName', e.target.value)}/>
                        <Field label="연락처" name="officePhoneNumber" value={form.officePhoneNumber}
                               onChange={(e) => set('officePhoneNumber', e.target.value)}/>
                        <Field label="운영 시간" name="officeBusinessHours" value={form.officeBusinessHours} placeholder="평일 09:00~18:00"
                               onChange={(e) => set('officeBusinessHours', e.target.value)}/>
                    </div>
                    <LocationEditor
                        provider={form.provider}
                        apiKey={apiKey}
                        name={form.officeName || '본점'}
                        address={form.officeAddress}
                        detailAddress={form.officeDetailAddress}
                        latitude={form.officeLatitude}
                        longitude={form.officeLongitude}
                        onChange={(next) => setForm({
                            ...form,
                            officeAddress: next.address,
                            officeDetailAddress: next.detailAddress,
                            officeLatitude: next.latitude,
                            officeLongitude: next.longitude,
                        })}
                    />
                    <div className="field">
                        <label htmlFor="officeDescription">찾아오시는 방법</label>
                        <textarea id="officeDescription" rows={4} value={form.officeDescription}
                                  placeholder="지하철 1호선 시청역 4번 출구에서 도보 3분"
                                  onChange={(e) => set('officeDescription', e.target.value)}/>
                    </div>
                    <ErrorBanner error={error}/>
                    {message && <p className="ok">{message}</p>}
                    <div className="toolbar form-actions">
                        <button type="submit" className="primary">저장</button>
                    </div>
                </form>
            )}

            {tab === 'branches' && (
                <>
                    <form onSubmit={save} className="row align-center">
                        <Checkbox label="지점 안내 사용" name="branchesEnabled" checked={form.branchesEnabled}
                                  onChange={(e) => set('branchesEnabled', e.target.checked)}/>
                        <button type="submit" className="primary push-right">저장</button>
                    </form>
                    <ErrorBanner error={error}/>
                    {message && <p className="ok">{message}</p>}
                    {saved.branchesEnabled
                        ? <BranchManager provider={form.provider} apiKey={apiKey}/>
                        : <p className="notice-box">지점 안내를 켜고 저장하면 지점을 등록할 수 있습니다.</p>}
                </>
            )}
        </section>
    );
}

interface Location {
    address: string;
    detailAddress: string;
    latitude: string;
    longitude: string;
}

function LocationEditor({provider, apiKey, name, onChange, ...location}: Location & {
    provider: MapProvider;
    apiKey: string | null;
    name: string;
    onChange: (next: Location) => void;
}) {
    const [error, setError] = useState('');
    const lat = toNumber(location.latitude);
    const lng = toNumber(location.longitude);
    const hasPosition = lat != null && lng != null && !Number.isNaN(lat) && !Number.isNaN(lng);

    const findCoordinates = async () => {
        setError('');
        if (!apiKey) {
            setError('지도 키를 먼저 입력하고 저장해야 주소로 좌표를 찾을 수 있습니다. 위도·경도를 직접 입력해도 됩니다.');
            return;
        }
        try {
            const found = await geocode(provider, apiKey, location.address);
            if (!found) {
                setError('주소로 위치를 찾지 못했습니다. 도로명 주소로 다시 시도하거나 지도를 클릭해 위치를 지정하세요.');
                return;
            }
            onChange({...location, latitude: found.lat.toFixed(7), longitude: found.lng.toFixed(7)});
        } catch {
            setError('지도 서비스를 불러오지 못했습니다. 키와 등록된 도메인을 확인하세요.');
        }
    };

    return (
        <div className="location-editor">
            <div className="row align-end">
                <Field label="주소" name="address" value={location.address} placeholder="서울 중구 세종대로 110"
                       onChange={(e) => onChange({...location, address: e.target.value})}/>
                <button type="button" onClick={findCoordinates} disabled={!location.address.trim()}>주소로 좌표 찾기</button>
            </div>
            <Field label="상세 주소" name="detailAddress" value={location.detailAddress}
                   onChange={(e) => onChange({...location, detailAddress: e.target.value})}/>
            <div className="row">
                <Field label="위도" name="latitude" value={location.latitude} placeholder="37.5666103"
                       onChange={(e) => onChange({...location, latitude: e.target.value})}/>
                <Field label="경도" name="longitude" value={location.longitude} placeholder="126.9783882"
                       onChange={(e) => onChange({...location, longitude: e.target.value})}/>
            </div>
            {error && <p className="field-error">{error}</p>}
            {apiKey && <p className="muted small">지도를 클릭하면 그 위치의 좌표가 입력됩니다.</p>}
            <MapView
                provider={provider}
                apiKey={apiKey}
                height={300}
                places={hasPosition ? [{name, address: location.address, latitude: lat, longitude: lng}] : []}
                focus={hasPosition ? {lat, lng} : null}
                onPick={(position) => onChange({...location, latitude: position.lat.toFixed(7), longitude: position.lng.toFixed(7)})}
                missingKeyMessage="지도 키를 입력하고 저장하면 여기서 위치를 미리 보고 클릭으로 지정할 수 있습니다."
            />
        </div>
    );
}

interface BranchForm extends Location {
    name: string;
    phoneNumber: string;
    businessHours: string;
    description: string;
    sequence: string;
    isUsing: boolean;
}

const EMPTY_BRANCH: BranchForm = {
    name: '', address: '', detailAddress: '', latitude: '', longitude: '', phoneNumber: '', businessHours: '',
    description: '', sequence: '0', isUsing: true,
};

const branchToForm = (branch: Branch): BranchForm => ({
    name: branch.name,
    address: branch.address,
    detailAddress: branch.detailAddress ?? '',
    latitude: String(branch.latitude),
    longitude: String(branch.longitude),
    phoneNumber: branch.phoneNumber ?? '',
    businessHours: branch.businessHours ?? '',
    description: branch.description ?? '',
    sequence: String(branch.sequence),
    isUsing: branch.isUsing,
});

function BranchManager({provider, apiKey}: { provider: MapProvider; apiKey: string | null }) {
    const [page, setPage] = useState(0);
    const [result, setResult] = useState<Page<Branch> | null>(null);
    const [editing, setEditing] = useState<{ id: number | null; form: BranchForm } | null>(null);
    const [error, setError] = useState<unknown>(null);

    const reload = useCallback(() => {
        adminApi.map.branches.search({page, size: 20}).then(setResult).catch(setError);
    }, [page]);
    useEffect(reload, [reload]);

    const submit = async (event: FormEvent) => {
        event.preventDefault();
        if (!editing) {
            return;
        }
        setError(null);
        const body = {
            ...editing.form,
            latitude: toNumber(editing.form.latitude),
            longitude: toNumber(editing.form.longitude),
            sequence: Number(editing.form.sequence) || 0,
        };
        try {
            await (editing.id ? adminApi.map.branches.update(editing.id, body) : adminApi.map.branches.create(body));
            setEditing(null);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const remove = async (branch: Branch) => {
        if (!window.confirm(`'${branch.name}' 지점을 삭제할까요?`)) {
            return;
        }
        try {
            await adminApi.map.branches.remove(branch.id);
            reload();
        } catch (e) {
            setError(e);
        }
    };

    const branches = result?.content ?? [];
    const setField = <K extends keyof BranchForm>(key: K, value: BranchForm[K]) =>
        editing && setEditing({...editing, form: {...editing.form, [key]: value}});

    return (
        <>
            <div className="toolbar list-toolbar">
                <span className="muted">총 {result?.page.totalElements ?? 0}개 지점</span>
                <button type="button" className="primary push-right" onClick={() => setEditing({id: null, form: EMPTY_BRANCH})}>
                    지점 등록
                </button>
            </div>
            {editing && (
                <form className="inline-form" onSubmit={submit}>
                    <h2>{editing.id ? '지점 수정' : '지점 등록'}</h2>
                    <div className="row">
                        <Field label="지점명" name="name" required value={editing.form.name} onChange={(e) => setField('name', e.target.value)}/>
                        <Field label="연락처" name="phoneNumber" value={editing.form.phoneNumber}
                               onChange={(e) => setField('phoneNumber', e.target.value)}/>
                        <Field label="운영 시간" name="businessHours" value={editing.form.businessHours}
                               onChange={(e) => setField('businessHours', e.target.value)}/>
                        <Field label="표시 순서" name="sequence" type="number" min={0} value={editing.form.sequence}
                               onChange={(e) => setField('sequence', e.target.value)}/>
                    </div>
                    <LocationEditor
                        provider={provider}
                        apiKey={apiKey}
                        name={editing.form.name || '지점'}
                        address={editing.form.address}
                        detailAddress={editing.form.detailAddress}
                        latitude={editing.form.latitude}
                        longitude={editing.form.longitude}
                        onChange={(next) => setEditing({...editing, form: {...editing.form, ...next}})}
                    />
                    <Field label="안내" name="description" value={editing.form.description}
                           onChange={(e) => setField('description', e.target.value)}/>
                    <Checkbox label="사용자 화면에 노출" name="isUsing" checked={editing.form.isUsing}
                              onChange={(e) => setField('isUsing', e.target.checked)}/>
                    <ErrorBanner error={error}/>
                    <div className="toolbar form-actions">
                        <button type="submit" className="primary">저장</button>
                        <button type="button" onClick={() => setEditing(null)}>취소</button>
                    </div>
                </form>
            )}
            {!editing && <ErrorBanner error={error}/>}
            {branches.length > 0 && (
                <MapView provider={provider} apiKey={apiKey} height={300}
                         places={branches.filter((b) => b.isUsing).map((b) => ({
                             name: b.name, address: b.address, latitude: Number(b.latitude), longitude: Number(b.longitude),
                         }))}
                         missingKeyMessage="지도 키를 입력하면 지점 위치를 지도에서 확인할 수 있습니다."/>
            )}
            <div className="table-wrap">
                <table>
                    <thead>
                    <tr><th>순서</th><th>지점명</th><th>주소</th><th>연락처</th><th>노출</th><th/></tr>
                    </thead>
                    <tbody>
                    {branches.map((branch) => (
                        <tr key={branch.id}>
                            <td>{branch.sequence}</td>
                            <td>{branch.name}</td>
                            <td>{[branch.address, branch.detailAddress].filter(Boolean).join(' ')}</td>
                            <td>{branch.phoneNumber ?? '-'}</td>
                            <td><YesNo value={branch.isUsing}/></td>
                            <td className="actions">
                                <button type="button" onClick={() => setEditing({id: branch.id, form: branchToForm(branch)})}>수정</button>
                                <button type="button" className="danger" onClick={() => remove(branch)}>삭제</button>
                            </td>
                        </tr>
                    ))}
                    {branches.length === 0 && <tr><td colSpan={6} className="muted">등록된 지점이 없습니다.</td></tr>}
                    </tbody>
                </table>
            </div>
            {result && <Pagination page={page} totalPages={result.page.totalPages} onChange={setPage}/>}
        </>
    );
}
