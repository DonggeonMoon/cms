package com.dgmoonlabs.cms.domain.business.store.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StoreSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("포장 주문 받기")
    private boolean pickupEnabled;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("매장 식사(테이블) 주문 받기")
    private boolean dineInEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("테이블 이름 목록")
    private List<String> tables;

    @Comment("주문 받기 시작 시각")
    private LocalTime openTime;

    @Comment("주문 마감 시각 (시작보다 이르면 다음 날 새벽까지)")
    private LocalTime closeTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("휴무 요일")
    private List<DayOfWeek> closedDays;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("주문 잠시 멈춤 (재료 소진, 주방 혼잡 등)")
    private boolean paused;

    @Column(length = 300)
    @Comment("주문 화면 안내 문구")
    private String notice;

    public void update(final boolean pickupEnabled, final boolean dineInEnabled, final List<String> tables, final LocalTime openTime,
                       final LocalTime closeTime, final List<DayOfWeek> closedDays, final boolean paused, final String notice) {
        this.pickupEnabled = pickupEnabled;
        this.dineInEnabled = dineInEnabled;
        this.tables = tables;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.closedDays = closedDays;
        this.paused = paused;
        this.notice = notice;
    }

    public boolean isOpenAt(final LocalDateTime now) {
        if (paused || (!pickupEnabled && !dineInEnabled)) {
            return false;
        }
        LocalTime time = now.toLocalTime();
        if (openTime == null || closeTime == null) {
            return !isClosedDay(now.getDayOfWeek());
        }
        if (openTime.isBefore(closeTime)) {
            return !isClosedDay(now.getDayOfWeek()) && !time.isBefore(openTime) && time.isBefore(closeTime);
        }
        if (!time.isBefore(openTime)) {
            return !isClosedDay(now.getDayOfWeek());
        }
        return time.isBefore(closeTime) && !isClosedDay(now.getDayOfWeek().minus(1));
    }

    public List<String> tablesOrEmpty() {
        return tables == null ? List.of() : tables;
    }

    private boolean isClosedDay(final DayOfWeek day) {
        return closedDays != null && closedDays.contains(day);
    }
}
