package com.dgmoonlabs.cms.domain.business.reservation.dto;

import com.dgmoonlabs.cms.domain.business.reservation.constant.QueueStatus;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.domain.business.reservation.layout.SpaceLayout;
import jakarta.validation.Valid;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.ReservationExtension;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.ProgramSession;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import jakarta.validation.constraints.*;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class ReservationDtos {
    private ReservationDtos() {
    }

    @Builder
    public record ProgramSaveRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 500) String description,
            Long imageFileId,
            @NotNull @PositiveOrZero BigDecimal price,
            @Positive Integer maximumPerson,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            LocalDateTime applicationStartDateTime,
            LocalDateTime applicationEndDateTime,
            @Size(max = 200_000) String contentHtml,
            boolean isEnabled,
            ReservationType reservationType,
            @Min(10) @Max(1440) Integer slotMinutes,
            LocalTime openTime,
            LocalTime closeTime,
            @Valid SpaceLayout layout,
            Long accessGradeId,
            @PositiveOrZero BigDecimal depositAmount,
            @Positive @Max(365) Integer maxRentalDays,
            @PositiveOrZero BigDecimal lateFeePerDay,
            boolean supportsQueue,
            @Valid List<@Valid PriceTier> priceTiers
    ) {
        public ReservationType reservationTypeOrDefault() {
            return reservationType == null ? ReservationType.PROGRAM : reservationType;
        }

        public boolean hasValidPeriods() {
            return isOrdered(startDateTime, endDateTime) && isOrdered(applicationStartDateTime, applicationEndDateTime);
        }

        private static boolean isOrdered(final LocalDateTime start, final LocalDateTime end) {
            return start == null || end == null || end.isAfter(start);
        }
    }

    public record ProgramResponse(Long id, String name, String description, Long imageFileId, String imageUrl,
                                  BigDecimal price, ReservationType reservationType, Integer slotMinutes,
                                  LocalTime openTime, LocalTime closeTime, SpaceLayout layout,
                                  Integer maximumPerson, Integer availablePerson,
                                  LocalDateTime startDateTime, LocalDateTime endDateTime,
                                  LocalDateTime applicationStartDateTime, LocalDateTime applicationEndDateTime,
                                  String contentHtml, boolean isEnabled, boolean isOpen, Double averageRating,
                                  Long accessGradeId, BigDecimal depositAmount, Integer maxRentalDays,
                                  BigDecimal lateFeePerDay, List<SessionResponse> sessions, boolean supportsQueue,
                                  List<PriceTier> priceTiers) {
        public static ProgramResponse of(final Program program, final SpaceLayout layout, final boolean isOpen,
                                         final Double averageRating) {
            return of(program, layout, isOpen, averageRating, List.of(), List.of());
        }

        public static ProgramResponse of(final Program program, final SpaceLayout layout, final boolean isOpen,
                                         final Double averageRating, final List<SessionResponse> sessions,
                                         final List<PriceTier> priceTiers) {
            return new ProgramResponse(program.getId(), program.getName(), program.getDescription(),
                    program.getImageFileId(),
                    program.getImageFileId() == null ? null : "/api/files/" + program.getImageFileId() + "/view",
                    program.getPrice(), program.getReservationType(), program.getSlotMinutes(), program.getOpenTime(),
                    program.getCloseTime(), layout, program.getMaximumPerson(), program.getAvailablePerson(),
                    program.getStartDateTime(), program.getEndDateTime(), program.getApplicationStartDateTime(),
                    program.getApplicationEndDateTime(), program.getContentHtml(), program.isEnabled(), isOpen, averageRating,
                    program.getAccessGradeId(), program.getDepositAmount(), program.getMaxRentalDays(),
                    program.getLateFeePerDay(), sessions, program.isSupportsQueue(), priceTiers);
        }
    }

    public record PriceTier(@NotBlank @Size(max = 30) String name, @NotNull @Positive BigDecimal price) {
    }

    public record SessionSaveRequest(@Size(max = 100) String name, @NotNull LocalDateTime startAt, LocalDateTime endAt,
                                     @Positive Integer capacity) {
    }

    public record SessionResponse(Long id, String name, LocalDateTime startAt, LocalDateTime endAt, Integer capacity,
                                  int reservedCount, Integer remaining, boolean isOpen) {
        public static SessionResponse of(final ProgramSession session, final LocalDateTime now) {
            return new SessionResponse(session.getId(), session.getName(), session.getStartAt(), session.getEndAt(),
                    session.getCapacity(), session.getReservedCount(), session.remaining(), session.isOpenAt(now));
        }
    }

    /** paid: 관리자가 연장할 때 연장료를 이미 받았으면 true. */
    public record ExtendRequest(@NotNull LocalDateTime endAt, boolean paid) {
    }

    public record ExtensionResponse(Long id, Long reservationId, LocalDateTime previousEndAt, LocalDateTime newEndAt, int days,
                                    BigDecimal fee, ReservationExtension.Status status, LocalDateTime createdAt) {
        public static ExtensionResponse from(final ReservationExtension extension) {
            return new ExtensionResponse(extension.getId(), extension.getReservationId(), extension.getPreviousEndAt(),
                    extension.getNewEndAt(), extension.getDays(), extension.getFee(), extension.getStatus(), extension.getCreatedAt());
        }
    }

    public record Slot(LocalDateTime startAt, LocalDateTime endAt, int remaining) {
    }

    /** remainingItems: 물품 대여에서 고른 기간 동안 남는 수량(가장 적게 남는 날 기준). */
    public record Availability(List<String> takenCells, List<Slot> slots, Integer remainingItems) {
        public Availability(final List<String> takenCells, final List<Slot> slots) {
            this(takenCells, slots, null);
        }
    }

    public record WorkerRequest(@NotBlank @Size(max = 50) String workerName,
                                @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String workerPhone) {
    }

    public record WorkCompleteRequest(@Size(max = 5000) String report, @Size(max = 20) List<@NotNull Long> photoFileIds) {
    }

    public record ReturnRequest(@PositiveOrZero BigDecimal deduction, @Size(max = 500) String memo) {
    }

    @Builder
    public record ReservationCreateRequest(
            @NotNull Long programId,
            @NotNull @Min(1) @Max(100) Integer quantity,
            @NotBlank @Size(max = 50) String applicantName,
            @NotBlank @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber,
            LocalDateTime startAt,
            LocalDateTime endAt,
            @Size(max = 100) List<@NotBlank @Size(max = 20) String> cells,
            @Size(max = 10) String visitPostalCode,
            @Size(max = 200) String visitAddress,
            @Size(max = 200) String visitDetailAddress,
            @Size(max = 1000) String requestMemo,
            Long sessionId,
            Long passId,
            @Positive @Max(1000) Integer partySize,
            Map<@NotBlank String, @Positive Integer> partyCounts
    ) {
        public List<String> cellsOrEmpty() {
            return cells == null ? List.of() : cells;
        }
    }

    public record CheckInRequest(@NotBlank @Size(max = 40) String code) {
    }

    public record ReservationResponse(Long id, String reservationNumber, Long userId, Long programId, String programName,
                                      ReservationType reservationType, LocalDateTime startAt, LocalDateTime endAt,
                                      List<String> cells, int quantity, BigDecimal totalPrice, ReservationStatus status, String customStatus,
                                      String applicantName, String phoneNumber, BigDecimal depositAmount,
                                      LocalDateTime returnedAt, BigDecimal depositRefunded, String returnMemo,
                                      boolean overdue, long lateDays, String visitPostalCode, String visitAddress,
                                      String visitDetailAddress, String requestMemo, String workerName, String workerPhone,
                                      LocalDateTime workerAssignedAt, LocalDateTime workCompletedAt, String workReport,
                                      List<String> workPhotoUrls, LocalDateTime createdAt, UserType userType, Long sessionId,
                                      BigDecimal lateFeePerDay, BigDecimal expectedLateFee, BigDecimal lateFee,
                                      BigDecimal unpaidExtensionFee, BigDecimal outstandingAmount, Long pendingExtensionId,
                                      BigDecimal pendingExtensionFee, Long passId, String checkInCode, LocalDateTime checkedInAt,
                                      Integer queueNumber, QueueStatus queueStatus, Integer partySize,
                                      Map<String, Integer> partyBreakdown) {
        public static ReservationResponse from(final Reservation reservation) {
            LocalDateTime now = LocalDateTime.now();
            return new ReservationResponse(reservation.getId(), reservation.getReservationNumber(),
                    reservation.getUserId(), reservation.getProgramId(), reservation.getProgramName(),
                    reservation.getReservationType(), reservation.getStartAt(), reservation.getEndAt(),
                    reservation.cellKeys(), reservation.getQuantity(), reservation.getTotalPrice(), reservation.getStatus(),
                    reservation.getCustomStatus(),
                    reservation.getApplicantName(), reservation.getPhoneNumber(), reservation.getDepositAmount(),
                    reservation.getReturnedAt(), reservation.getDepositRefunded(), reservation.getReturnMemo(),
                    reservation.isOverdueAt(now), reservation.lateDaysAt(now), reservation.getVisitPostalCode(),
                    reservation.getVisitAddress(), reservation.getVisitDetailAddress(), reservation.getRequestMemo(),
                    reservation.getWorkerName(), reservation.getWorkerPhone(), reservation.getWorkerAssignedAt(),
                    reservation.getWorkCompletedAt(), reservation.getWorkReport(),
                    reservation.getWorkPhotoFileIds() == null ? List.of()
                            : reservation.getWorkPhotoFileIds().stream().map(id -> "/api/files/" + id + "/view").toList(),
                    reservation.getCreatedAt(), reservation.getUserType(), reservation.getSessionId(),
                    reservation.getLateFeePerDay(),
                    reservation.getReturnedAt() == null ? reservation.lateFeeAt(now) : reservation.getLateFee(),
                    reservation.getLateFee(), reservation.getUnpaidExtensionFee(), reservation.getOutstandingAmount(),
                    reservation.getPendingExtensionId(), reservation.getPendingExtensionFee(), reservation.getPassId(),
                    reservation.getCheckInCode(), reservation.getCheckedInAt(),
                    reservation.getQueueNumber(), reservation.getQueueStatus(), reservation.getPartySize(),
                    reservation.getPartyBreakdown());
        }
    }

    public record QueueStatusRequest(@NotNull QueueStatus status) {
    }
}
