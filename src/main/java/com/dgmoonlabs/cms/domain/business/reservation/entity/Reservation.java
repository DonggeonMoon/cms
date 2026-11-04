package com.dgmoonlabs.cms.domain.business.reservation.entity;

import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

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

    public void confirm() {
        this.customStatus = null;
        if (status != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.customStatus = null;
        if (!status.isCancelable()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ReservationStatus.CANCELED;
    }

    public void applyCustomStatus(final String code) {
        this.customStatus = code;
    }
}
