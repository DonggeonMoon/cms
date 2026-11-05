import {useEffect, useState} from 'react';
import {FULFILLMENT, type FulfillmentType, type StoreSetting, storeApi} from '../../api/store';
import {ErrorBanner} from '../../components/ErrorBanner';

export interface StoreChoice {
    fulfillmentType: FulfillmentType;
    tableName: string;
    requestMessage: string;
    deliverySlotAt: string;
}

export const EMPTY_STORE_CHOICE: StoreChoice = {fulfillmentType: 'PICKUP', tableName: '', requestMessage: '', deliverySlotAt: ''};

export function StoreOrderFields({value, onChange, onAvailability}: {
    value: StoreChoice; onChange: (choice: StoreChoice) => void; onAvailability: (open: boolean) => void;
}) {
    const [setting, setSetting] = useState<StoreSetting | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        storeApi.setting().then((loaded) => {
            setSetting(loaded);
            onAvailability(loaded.openNow);
            if (!loaded.pickupEnabled && loaded.dineInEnabled) {
                onChange({...value, fulfillmentType: 'DINE_IN'});
            }
        }).catch(setError);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);
    if (!setting) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">매장 정보를 불러오는 중…</p>;
    }
    const types = (['PICKUP', 'DINE_IN', 'LOCAL_DELIVERY'] as FulfillmentType[]).filter((type) =>
        type === 'PICKUP' ? setting.pickupEnabled : type === 'DINE_IN' ? setting.dineInEnabled : setting.localDeliveryEnabled);
    const delivering = value.fulfillmentType === 'LOCAL_DELIVERY';
    return (
        <fieldset>
            <legend>받는 방식</legend>
            {setting.notice && <p className="muted small">{setting.notice}</p>}
            {!setting.openNow && <p className="notice-box">지금은 주문을 받지 않습니다.{setting.openTime && setting.closeTime
                && ` 주문 가능 시간 ${setting.openTime.slice(0, 5)} ~ ${setting.closeTime.slice(0, 5)}`}</p>}
            <div className="toolbar" role="radiogroup" aria-label="받는 방식">
                {types.map((type) => (
                    <label key={type} className="checkbox">
                        <input type="radio" name="fulfillmentType" checked={value.fulfillmentType === type}
                               onChange={() => onChange({...value, fulfillmentType: type})}/> {FULFILLMENT[type]}
                    </label>
                ))}
            </div>
            {value.fulfillmentType === 'DINE_IN' && (
                <div className="field">
                    <label htmlFor="tableName">테이블</label>
                    <select id="tableName" required value={value.tableName} onChange={(e) => onChange({...value, tableName: e.target.value})}>
                        <option value="">선택해 주세요</option>
                        {setting.tables.map((table) => <option key={table} value={table}>{table}</option>)}
                    </select>
                </div>
            )}
            {delivering && (
                <p className="muted small">
                    배달 가능 지역(우편번호 앞자리): {setting.deliveryZones.join(', ')}
                    {setting.deliveryFee != null && ` · 배달 요금 ${setting.deliveryFee.toLocaleString()}원`}
                    {setting.deliveryMinimumAmount != null && ` · 최소 주문 ${setting.deliveryMinimumAmount.toLocaleString()}원`}
                </p>
            )}
            {delivering && setting.deliverySlotMinutes != null && (
                <div className="field">
                    <label htmlFor="deliverySlotAt">배달 받을 시간</label>
                    <input id="deliverySlotAt" type="datetime-local" required step={setting.deliverySlotMinutes * 60}
                           value={value.deliverySlotAt} onChange={(e) => onChange({...value, deliverySlotAt: e.target.value})}/>
                </div>
            )}
            <div className="field">
                <label htmlFor="storeRequest">요청 사항</label>
                <textarea id="storeRequest" rows={2} maxLength={2000} placeholder="예: 덜 달게, 수저 빼 주세요" value={value.requestMessage}
                          onChange={(e) => onChange({...value, requestMessage: e.target.value})}/>
            </div>
        </fieldset>
    );
}
