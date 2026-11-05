package com.dgmoonlabs.cms.domain.business.reservation.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 물품 대여 연장 한 번. 신청하면 바로 반납일을 늘려 재고를 잡아 두고, 연장료는 따로 결제한다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservationExtension extends BaseEntity {
    public enum Status {
        PENDING_PAYMENT, PAID, CANCELED, DEDUCTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("예약 ID")
    private Long reservationId;

    @Column(nullable = false)
    @Comment("연장 전 반납 일시")
    private LocalDateTime previousEndAt;

    @Column(nullable = false)
    @Comment("연장한 반납 일시")
    private LocalDateTime newEndAt;

    @Column(nullable = false)
    @Comment("늘린 일수")
    private int days;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("연장료")
    private BigDecimal fee;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("상태 (결제 대기, 결제 완료, 취소, 반납 때 보증금에서 차감)")
    private Status status;

    public void markPaid() {
        if (status != Status.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = Status.PAID;
    }

    public void cancel() {
        if (status != Status.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = Status.CANCELED;
    }

    public void deductFromDeposit() {
        if (status == Status.PENDING_PAYMENT) {
            this.status = Status.DEDUCTED;
        }
    }
}
