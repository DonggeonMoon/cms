package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.domain.course.constant.AccessPeriodMode;
import com.dgmoonlabs.cms.domain.course.constant.EnrollmentMode;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("소속 사이트 ID")
    private Long siteId;

    @Column(nullable = false, length = 200)
    @Comment("과정 이름")
    private String title;

    @Column(length = 1000)
    @Comment("과정 소개")
    private String description;

    @Comment("썸네일 이미지 파일 ID")
    private Long thumbnailFileId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("차시를 순서대로만 들을 수 있는지")
    private boolean sequential;

    @Comment("수강 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("공개 여부")
    private boolean isUsing;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("교육과정으로 사용 여부 (켜면 퀴즈·학습 옵션을 쓸 수 있는 교육과정, 끄면 비디오·오디오만 모은 일반 시리즈)")
    private boolean isCourse;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'FREE'")
    @Enumerated(EnumType.STRING)
    @Comment("수강 신청 방식 (교육과정으로 사용하는 경우만 적용) - FREE 자유 수강, APPLY 신청 후 수강, APPROVAL 승인제")
    private EnrollmentMode enrollmentMode = EnrollmentMode.FREE;

    @Comment("신청 가능 기간(모집 기간) 시작 (없으면 제한 없음, 교육과정으로 사용하는 경우만 적용)")
    private LocalDateTime applicationStartAt;

    @Comment("신청 가능 기간(모집 기간) 종료 (없으면 제한 없음, 교육과정으로 사용하는 경우만 적용)")
    private LocalDateTime applicationEndAt;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'UNLIMITED'")
    @Enumerated(EnumType.STRING)
    @Comment("수강 가능 기간 방식 (교육과정으로 사용하는 경우만 적용) - UNLIMITED 무제한, FIXED 고정 기간, DAYS_FROM_ENROLLMENT 신청일 기준 N일")
    private AccessPeriodMode accessPeriodMode = AccessPeriodMode.UNLIMITED;

    @Comment("수강 가능 기간 시작 (FIXED 전용)")
    private LocalDateTime accessStartAt;

    @Comment("수강 가능 기간 종료 (FIXED 전용)")
    private LocalDateTime accessEndAt;

    @Comment("수강 시작일로부터 수강 가능한 일수 (DAYS_FROM_ENROLLMENT 전용)")
    private Integer accessDays;

    @Comment("최대 배속 (예: 1.0=배속 불가, 1.5=1.5배까지, 없으면 제한 없음, 교육과정으로 사용하는 경우만 적용)")
    private Double maxPlaybackRate;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("건너뛰기 방지 기본값 (교육과정으로 사용하는 경우만 적용, 항목별로 재정의 가능)")
    private boolean preventSkipping;

    @Comment("수료에 필요한 전체 진도율 기준(%) (없으면 100%, 즉 전 항목 완료)")
    private Integer completionThresholdPercent;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("출석 기준 사용 여부 (수강 가능 기간이 무제한이 아닐 때만 의미가 있음)")
    private boolean attendanceRequired;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("유료 여부 (결제한 회원만 수강)")
    private boolean paid;

    @Column(precision = 15, scale = 2)
    @Comment("판매 가격")
    private BigDecimal price;

    @Comment("판매용으로 자동 관리하는 상품 ID")
    private Long productId;

    @Comment("환불 가능 진도율 기준(%) - 진도율이 이 값 미만이면 환불 가능(없으면 관리자 재량, 수강 시작 전만 허용하려면 작게 설정)")
    private Integer refundableProgressBelowPercent;

    public void changeSale(final boolean paid, final BigDecimal price) {
        this.paid = paid;
        this.price = paid ? price : null;
    }

    public void linkProduct(final Long productId) {
        this.productId = productId;
    }

    public void changeRefundPolicy(final Integer refundableProgressBelowPercent) {
        this.refundableProgressBelowPercent = refundableProgressBelowPercent;
    }

    public void update(final String title, final String description, final Long thumbnailFileId, final boolean sequential,
                       final Long accessGradeId, final boolean isUsing, final boolean isCourse, final EnrollmentMode enrollmentMode,
                       final LocalDateTime applicationStartAt, final LocalDateTime applicationEndAt,
                       final AccessPeriodMode accessPeriodMode, final LocalDateTime accessStartAt, final LocalDateTime accessEndAt,
                       final Integer accessDays, final Double maxPlaybackRate, final boolean preventSkipping,
                       final Integer completionThresholdPercent, final boolean attendanceRequired) {
        this.title = title;
        this.description = description;
        this.thumbnailFileId = thumbnailFileId;
        this.sequential = sequential;
        this.accessGradeId = accessGradeId;
        this.isUsing = isUsing;
        this.isCourse = isCourse;
        this.enrollmentMode = isCourse ? enrollmentMode : EnrollmentMode.FREE;
        this.applicationStartAt = isCourse ? applicationStartAt : null;
        this.applicationEndAt = isCourse ? applicationEndAt : null;
        this.accessPeriodMode = isCourse ? accessPeriodMode : AccessPeriodMode.UNLIMITED;
        this.accessStartAt = isCourse && accessPeriodMode == AccessPeriodMode.FIXED ? accessStartAt : null;
        this.accessEndAt = isCourse && accessPeriodMode == AccessPeriodMode.FIXED ? accessEndAt : null;
        this.accessDays = isCourse && accessPeriodMode == AccessPeriodMode.DAYS_FROM_ENROLLMENT ? accessDays : null;
        this.maxPlaybackRate = isCourse ? maxPlaybackRate : null;
        this.preventSkipping = isCourse && preventSkipping;
        this.completionThresholdPercent = isCourse ? completionThresholdPercent : null;
        this.attendanceRequired = isCourse && accessPeriodMode != AccessPeriodMode.UNLIMITED && attendanceRequired;
    }

    /** 교육과정으로 쓰지 않는 일반 시리즈는 항상 자유 수강으로 동작한다. */
    public EnrollmentMode effectiveEnrollmentMode() {
        return isCourse ? enrollmentMode : EnrollmentMode.FREE;
    }

    public Double effectiveMaxPlaybackRate() {
        return isCourse ? maxPlaybackRate : null;
    }

    public boolean effectivePreventSkipping() {
        return isCourse && preventSkipping;
    }

    public int effectiveCompletionThresholdPercent() {
        return isCourse && completionThresholdPercent != null ? completionThresholdPercent : 100;
    }

    public boolean isPublic() {
        return isUsing && getDeletedAt() == null;
    }

    public boolean applicationOpen(final LocalDateTime now) {
        if (!isCourse) {
            return true;
        }
        if (applicationStartAt != null && now.isBefore(applicationStartAt)) {
            return false;
        }
        return applicationEndAt == null || !now.isAfter(applicationEndAt);
    }

    /** 수강생 한 명의 수강 가능 마감 일시. null이면 무기한. 관리자의 개별 연장(extendedUntil)이 더 뒤면 그걸 따른다. */
    public LocalDateTime accessDeadline(final LocalDateTime enrolledAt, final LocalDateTime extendedUntil) {
        LocalDateTime base = switch (isCourse ? accessPeriodMode : AccessPeriodMode.UNLIMITED) {
            case UNLIMITED -> null;
            case FIXED -> accessEndAt;
            case DAYS_FROM_ENROLLMENT -> accessDays == null ? null : enrolledAt.plusDays(accessDays);
        };
        if (extendedUntil == null) {
            return base;
        }
        return base != null && base.isAfter(extendedUntil) ? base : extendedUntil;
    }

    public boolean accessOpen(final LocalDateTime enrolledAt, final LocalDateTime extendedUntil, final LocalDateTime now) {
        if (isCourse && accessPeriodMode == AccessPeriodMode.FIXED && accessStartAt != null && now.isBefore(accessStartAt)) {
            return false;
        }
        LocalDateTime deadline = accessDeadline(enrolledAt, extendedUntil);
        return deadline == null || !now.isAfter(deadline);
    }
}
