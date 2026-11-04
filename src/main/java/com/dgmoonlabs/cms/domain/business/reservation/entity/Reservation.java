package com.dgmoonlabs.cms.domain.business.reservation.entity;

import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("예약 번호")
    private String reservationNumber;

    @Comment("예약 순번")
    private String reservationSequence;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Column(name = "product_id")
    @Comment("프로그램 ID")
    private Long programId;

    @Comment("예약 당시 프로그램명")
    private String programName;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'PROGRAM'")
    @Enumerated(EnumType.STRING)
    @Comment("예약 유형 (예약 당시)")
    private ReservationType reservationType = ReservationType.PROGRAM;

    @Comment("이용 시작 일시 (상담·대관 시작, 숙박 체크인)")
    private LocalDateTime startAt;

    @Comment("이용 종료 일시 (상담·대관 종료, 숙박 체크아웃)")
    private LocalDateTime endAt;

    @Column(length = 2000)
    @Comment("예약한 칸 (배치표 칸 키, 쉼표로 구분)")
    private String cells;

    @Comment("인원")
    private Integer quantity;

    @Comment("할인 ID")
    private Long discountId;

    @Column(precision = 15, scale = 2)
    @Comment("총 금액")
    private BigDecimal totalPrice;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("예약 상태")
    private ReservationStatus status;

    @Column(length = 40)
    @Comment("세부 상태 코드 (기본 상태 아래에 관리자가 추가한 상태)")
    private String customStatus;

    @Comment("신청자 이름")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String applicantName;

    @Comment("연락처")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String phoneNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(precision = 15, scale = 2)
    @Comment("물품 대여 보증금 합계 (총 금액에 포함)")
    private BigDecimal depositAmount;

    @Comment("물품 반납 일시")
    private LocalDateTime returnedAt;

    @Column(precision = 15, scale = 2)
    @Comment("돌려준 보증금")
    private BigDecimal depositRefunded;

    @Column(length = 500)
    @Comment("반납 메모 (파손·연체로 보증금을 차감한 사유 등)")
    private String returnMemo;

    public List<String> cellKeys() {
        return cells == null || cells.isBlank() ? List.of() : List.of(cells.split(","));
    }

    public boolean overlaps(final LocalDateTime from, final LocalDateTime to) {
        return startAt == null || endAt == null || (startAt.isBefore(to) && endAt.isAfter(from));
    }

    public void confirm() {
        this.customStatus = null;
        if (status != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void markReturned(final LocalDateTime now, final BigDecimal refund, final String memo) {
        if (reservationType != ReservationType.ITEM_RENTAL) {
            throw new BusinessException(ErrorCode.NOT_ITEM_RENTAL);
        }
        if (returnedAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }
        if (status != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.returnedAt = now;
        this.depositRefunded = refund;
        this.returnMemo = memo;
    }

    public boolean isOverdueAt(final LocalDateTime now) {
        return reservationType == ReservationType.ITEM_RENTAL && status == ReservationStatus.CONFIRMED && returnedAt == null
                && endAt != null && now.isAfter(endAt);
    }

    /** 반납 예정일 다음 날부터 하루씩 센다. 아직 반납하지 않았으면 오늘까지. */
    public long lateDaysAt(final LocalDateTime now) {
        if (reservationType != ReservationType.ITEM_RENTAL || endAt == null || status == ReservationStatus.CANCELED) {
            return 0;
        }
        LocalDateTime until = returnedAt != null ? returnedAt : now;
        return Math.max(0, ChronoUnit.DAYS.between(endAt.toLocalDate(), until.toLocalDate()));
    }

    /** 반납한 날부터는 물품이 돌아온 것으로 보고 재고에서 빼지 않는다. */
    public boolean holdsItemOn(final LocalDate date) {
        return status != ReservationStatus.CANCELED && startAt != null && endAt != null
                && !date.isBefore(startAt.toLocalDate()) && date.isBefore(endAt.toLocalDate())
                && (returnedAt == null || date.isBefore(returnedAt.toLocalDate()));
    }

    public void cancel() {
        this.customStatus = null;
        if (returnedAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }
        if (!status.isCancelable()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ReservationStatus.CANCELED;
    }

    public void applyCustomStatus(final String code) {
        this.customStatus = code;
    }
}
