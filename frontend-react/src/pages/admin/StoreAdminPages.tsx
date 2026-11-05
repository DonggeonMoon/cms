import {type FormEvent, useCallback, useEffect, useState} from 'react';
import type {Order, OrderStatus} from '../../api/domain';
import {adminApi} from '../../api/endpoints';
import {DAY_OF_WEEK, type DayOfWeek, FULFILLMENT, type StoreSetting, storeApi} from '../../api/store';
import {ErrorBanner} from '../../components/ErrorBanner';
import {Checkbox, Field} from '../../components/Field';
import {formatTime} from '../../components/format';
import {ORDER_STATUS} from '../../components/labels';

const DAYS = Object.keys(DAY_OF_WEEK) as DayOfWeek[];

export function StoreSettingPage() {
    const [form, setForm] = useState<{ pickupEnabled: boolean; dineInEnabled: boolean; tablesText: string; openTime: string; closeTime: string;
        closedDays: DayOfWeek[]; paused: boolean; notice: string; localDeliveryEnabled: boolean; deliveryZonesText: string;
        deliveryFee: number | string; deliveryMinimumAmount: number | string; deliverySlotMinutes: number | string } | null>(null);
    const [saved, setSaved] = useState<StoreSetting | null>(null);
    const [error, setError] = useState<unknown>(null);
    useEffect(() => {
        storeApi.setting().then((setting) => {
            setSaved(setting);
            setForm({...setting, tablesText: setting.tables.join(', '), openTime: setting.openTime?.slice(0, 5) ?? '',
                closeTime: setting.closeTime?.slice(0, 5) ?? '', notice: setting.notice ?? '',
                deliveryZonesText: setting.deliveryZones.join(', '), deliveryFee: setting.deliveryFee ?? '',
                deliveryMinimumAmount: setting.deliveryMinimumAmount ?? '', deliverySlotMinutes: setting.deliverySlotMinutes ?? ''});
        }).catch(setError);
    }, []);
    if (!form) {
        return error ? <ErrorBanner error={error}/> : <p className="muted">불러오는 중…</p>;
    }
    const submit = async (event: FormEvent) => {
        event.preventDefault();
        setError(null);
        try {
            const {tablesText, deliveryZonesText, ...rest} = form;
            setSaved(await storeApi.save({...rest, tables: tablesText.split(',').map((t) => t.trim()).filter(Boolean),
                deliveryZones: deliveryZonesText.split(',').map((t) => t.trim()).filter(Boolean),
                openTime: form.openTime || null, closeTime: form.closeTime || null,
                deliveryFee: form.deliveryFee === '' ? null : Number(form.deliveryFee),
                deliveryMinimumAmount: form.deliveryMinimumAmount === '' ? null : Number(form.deliveryMinimumAmount),
                deliverySlotMinutes: form.deliverySlotMinutes === '' ? null : Number(form.deliverySlotMinutes)}));
        } catch (e) {
            setError(e);
        }
    };
    return (
        <section className="card">
            <h1>매장 주문</h1>
            <p className="muted">"매장 주문 전용"으로 표시한 상품을 포장·매장 식사·배달로 주문받습니다. 들어온 주문은 주문 접수 화면에서 처리합니다.</p>
            {saved && <p className={saved.openNow ? 'notice-box ok' : 'notice-box'}>{saved.openNow ? '지금 주문을 받고 있습니다.' : '지금은 주문을 받지 않습니다.'}</p>}
            <form onSubmit={submit}>
                <Checkbox label="포장 주문 받기" checked={form.pickupEnabled} onChange={(e) => setForm({...form, pickupEnabled: e.target.checked})}/>
                <Checkbox label="매장 식사(테이블) 주문 받기" checked={form.dineInEnabled}
                          onChange={(e) => setForm({...form, dineInEnabled: e.target.checked})}/>
                {form.dineInEnabled && (
                    <Field label="테이블" name="tables" placeholder="1번, 2번, 창가" value={form.tablesText}
                           onChange={(e) => setForm({...form, tablesText: e.target.value})}/>
                )}
                <div className="row">
                    <Field label="주문 시작 시각" name="openTime" type="time" value={form.openTime} onChange={(e) => setForm({...form, openTime: e.target.value})}/>
                    <Field label="주문 마감 시각" name="closeTime" type="time" value={form.closeTime} onChange={(e) => setForm({...form, closeTime: e.target.value})}/>
                </div>
                <p className="muted small">비워두면 시간 제한 없이 받습니다. 마감이 시작보다 이르면 다음 날 새벽까지 받습니다.</p>
                <fieldset className="area-checks">
                    <legend>휴무 요일</legend>
                    {DAYS.map((day) => (
                        <label key={day} className="checkbox">
                            <input type="checkbox" checked={form.closedDays.includes(day)} onChange={(e) => setForm({...form,
                                closedDays: e.target.checked ? [...form.closedDays, day] : form.closedDays.filter((d) => d !== day)})}/> {DAY_OF_WEEK[day]}
                        </label>
                    ))}
                </fieldset>
                <Checkbox label="배달 주문 받기" checked={form.localDeliveryEnabled}
                          onChange={(e) => setForm({...form, localDeliveryEnabled: e.target.checked})}/>
                {form.localDeliveryEnabled && (
                    <>
                        <Field label="배달 가능 지역(우편번호 앞자리)" name="deliveryZones" placeholder="062, 063" value={form.deliveryZonesText}
                               onChange={(e) => setForm({...form, deliveryZonesText: e.target.value})}/>
                        <div className="row">
                            <Field label="배달 요금(원)" name="deliveryFee" type="number" value={form.deliveryFee}
                                   onChange={(e) => setForm({...form, deliveryFee: e.target.value})}/>
                            <Field label="최소 주문 금액(원)" name="deliveryMinimumAmount" type="number" value={form.deliveryMinimumAmount}
                                   onChange={(e) => setForm({...form, deliveryMinimumAmount: e.target.value})}/>
                            <Field label="배달 시간대 단위(분)" name="deliverySlotMinutes" type="number" value={form.deliverySlotMinutes}
                                   onChange={(e) => setForm({...form, deliverySlotMinutes: e.target.value})}/>
                        </div>
                        <p className="muted small">시간대 단위를 비워두면 시간대를 받지 않고 바로 배달로 받습니다.</p>
                    </>
                )}
                <Checkbox label="주문 잠시 멈춤 (재료 소진·주방 혼잡)" checked={form.paused} onChange={(e) => setForm({...form, paused: e.target.checked})}/>
                <Field label="주문 화면 안내 문구" name="notice" value={form.notice} onChange={(e) => setForm({...form, notice: e.target.value})}/>
                <ErrorBanner error={error}/>
                <button type="submit" className="primary">저장</button>
            </form>
        </section>
    );
}

const NEXT: Partial<Record<OrderStatus, { status: OrderStatus; label: string }>> = {
    PAID: {status: 'PREPARING', label: '접수'},
    PREPARING: {status: 'READY', label: '준비 완료'},
    READY: {status: 'COMPLETED', label: '전달 완료'},
};

const REFRESH_MS = 10_000;

export function StoreOrderQueuePage() {
    const [orders, setOrders] = useState<Order[] | null>(null);
    const [error, setError] = useState<unknown>(null);
    const reload = useCallback(() => {
        storeApi.queue().then((loaded) => {
            setOrders(loaded);
            setError(null);
        }).catch(setError);
    }, []);
    useEffect(() => {
        reload();
        const timer = window.setInterval(reload, REFRESH_MS);
        return () => window.clearInterval(timer);
    }, [reload]);
    const advance = (order: Order) => {
        const next = NEXT[order.status];
        if (next) {
            adminApi.orders.changeStatus(order.id, next.status).then(reload).catch(setError);
        }
    };
    return (
        <section className="card">
            <h1>주문 접수</h1>
            <p className="muted small">결제를 마친 포장·매장 식사 주문입니다. 10초마다 새로 불러옵니다.</p>
            <ErrorBanner error={error}/>
            <div className="kitchen-board">
                {orders?.map((order) => {
                    const next = NEXT[order.status];
                    return (
                        <article key={order.id} className={`kitchen-ticket status-${order.status.toLowerCase()}`}>
                            <header>
                                {order.pickupNumber != null && <strong className="ticket-number">{order.pickupNumber}번</strong>}
                                <span className="badge">{order.fulfillmentType === 'DINE_IN' ? `${order.tableName} 테이블` : FULFILLMENT[order.fulfillmentType]}</span>
                                <span className="muted small">{formatTime(order.createdAt)}</span>
                            </header>
                            {order.fulfillmentType === 'LOCAL_DELIVERY' && (
                                <p className="small">{order.address} {order.detailAddress}
                                    {order.deliverySlotAt && ` · ${formatTime(order.deliverySlotAt)} 배달`}</p>
                            )}
                            <ul className="plain-list">
                                {order.items.filter((item) => item.status !== 'CANCELED').map((item) => (
                                    <li key={item.id}>{item.productName}{item.optionName && ` (${item.optionName})`} × {item.quantity}</li>
                                ))}
                            </ul>
                            {order.requestMessage && <p className="small">요청: {order.requestMessage}</p>}
                            <footer>
                                <span>{ORDER_STATUS[order.status]}</span>
                                {next && <button type="button" className="primary" onClick={() => advance(order)}>{next.label}</button>}
                            </footer>
                        </article>
                    );
                })}
            </div>
            {orders?.length === 0 && <p className="muted">처리할 주문이 없습니다.</p>}
        </section>
    );
}
