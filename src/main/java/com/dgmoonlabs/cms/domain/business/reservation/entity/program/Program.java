package com.dgmoonlabs.cms.domain.business.reservation.entity.program;

import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Program extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("프로그램명")
    private String name;

    @Comment("프로그램 설명")
    private String description;

    @Comment("대표 이미지 파일 ID")
    private Long imageFileId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("참가비 (1인)")
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'PROGRAM'")
    @Enumerated(EnumType.STRING)
    @Comment("예약 유형")
    private ReservationType reservationType = ReservationType.PROGRAM;

    @Comment("상담·대관 예약 단위(분)")
    private Integer slotMinutes;

    @Comment("상담·대관 운영 시작 시각")
    private LocalTime openTime;

    @Comment("상담·대관 운영 종료 시각")
    private LocalTime closeTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("공간 배치표 (좌석·객실·공간)")
    private String layout;

    @Comment("최대 인원")
    private Integer maximumPerson;

    @Comment("잔여 인원")
    private Integer availablePerson;

    @Comment("시작 일시")
    private LocalDateTime startDateTime;

    @Comment("종료 일시")
    private LocalDateTime endDateTime;

    @Comment("신청 시작 일시")
    private LocalDateTime applicationStartDateTime;

    @Comment("신청 종료 일시")
    private LocalDateTime applicationEndDateTime;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    private boolean isEnabled;

    public void update(final String name, final String description, final Long imageFileId, final BigDecimal price,
                       final Integer maximumPerson, final LocalDateTime startDateTime, final LocalDateTime endDateTime,
                       final LocalDateTime applicationStartDateTime, final LocalDateTime applicationEndDateTime,
                       final String contentHtml, final String contentText, final boolean isEnabled) {
        if (maximumPerson != null && this.maximumPerson != null && availablePerson != null) {
            int reserved = this.maximumPerson - availablePerson;
            if (maximumPerson < reserved) {
                throw new BusinessException(ErrorCode.PROGRAM_FULL);
            }
            this.availablePerson = maximumPerson - reserved;
        } else {
            this.availablePerson = maximumPerson;
        }
        this.name = name;
        this.description = description;
        this.imageFileId = imageFileId;
        this.price = price;
        this.maximumPerson = maximumPerson;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.applicationStartDateTime = applicationStartDateTime;
        this.applicationEndDateTime = applicationEndDateTime;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.isEnabled = isEnabled;
    }

    public void changeReservationSettings(final ReservationType reservationType, final Integer slotMinutes,
                                          final LocalTime openTime, final LocalTime closeTime, final String layout) {
        this.reservationType = reservationType;
        boolean timed = reservationType == ReservationType.CONSULTATION || reservationType == ReservationType.RENTAL;
        this.slotMinutes = timed ? slotMinutes : null;
        this.openTime = timed ? openTime : null;
        this.closeTime = timed ? closeTime : null;
        this.layout = reservationType.usesLayout() ? layout : null;
    }

    public boolean isOpenAt(final LocalDateTime now) {
        return isEnabled && getDeletedAt() == null
                && (applicationStartDateTime == null || !now.isBefore(applicationStartDateTime))
                && (applicationEndDateTime == null || !now.isAfter(applicationEndDateTime));
    }

    public void reserve(final int persons) {
        if (availablePerson == null) {
            return;
        }
        if (availablePerson < persons) {
            throw new BusinessException(ErrorCode.PROGRAM_FULL);
        }
        availablePerson -= persons;
    }

    public void release(final int persons) {
        if (availablePerson != null) {
            availablePerson = Math.min(maximumPerson == null ? Integer.MAX_VALUE : maximumPerson, availablePerson + persons);
        }
    }
}
