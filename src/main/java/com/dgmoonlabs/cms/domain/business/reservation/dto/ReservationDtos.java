package com.dgmoonlabs.cms.domain.business.reservation.dto;

import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.domain.business.reservation.layout.SpaceLayout;
import jakarta.validation.Valid;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import jakarta.validation.constraints.*;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
            Long accessGradeId
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
                                  Long accessGradeId) {
        public static ProgramResponse of(final Program program, final SpaceLayout layout, final boolean isOpen,
                                         final Double averageRating) {
            return new ProgramResponse(program.getId(), program.getName(), program.getDescription(),
                    program.getImageFileId(),
                    program.getImageFileId() == null ? null : "/api/files/" + program.getImageFileId() + "/view",
                    program.getPrice(), program.getReservationType(), program.getSlotMinutes(), program.getOpenTime(),
                    program.getCloseTime(), layout, program.getMaximumPerson(), program.getAvailablePerson(),
                    program.getStartDateTime(), program.getEndDateTime(), program.getApplicationStartDateTime(),
                    program.getApplicationEndDateTime(), program.getContentHtml(), program.isEnabled(), isOpen, averageRating,
                    program.getAccessGradeId());
        }
    }

    public record Slot(LocalDateTime startAt, LocalDateTime endAt, int remaining) {
    }

    public record Availability(List<String> takenCells, List<Slot> slots) {
    }

    @Builder
    public record ReservationCreateRequest(
            @NotNull Long programId,
            @NotNull @Min(1) @Max(100) Integer quantity,
            @NotBlank @Size(max = 50) String applicantName,
            @NotBlank @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber,
            LocalDateTime startAt,
            LocalDateTime endAt,
            @Size(max = 100) List<@NotBlank @Size(max = 20) String> cells
    ) {
        public List<String> cellsOrEmpty() {
            return cells == null ? List.of() : cells;
        }
    }

    public record ReservationResponse(Long id, String reservationNumber, Long userId, Long programId, String programName,
                                      ReservationType reservationType, LocalDateTime startAt, LocalDateTime endAt,
                                      List<String> cells, int quantity, BigDecimal totalPrice, ReservationStatus status, String customStatus,
                                      String applicantName, String phoneNumber, LocalDateTime createdAt) {
        public static ReservationResponse from(final Reservation reservation) {
            return new ReservationResponse(reservation.getId(), reservation.getReservationNumber(),
                    reservation.getUserId(), reservation.getProgramId(), reservation.getProgramName(),
                    reservation.getReservationType(), reservation.getStartAt(), reservation.getEndAt(),
                    reservation.cellKeys(), reservation.getQuantity(), reservation.getTotalPrice(), reservation.getStatus(),
                    reservation.getCustomStatus(),
                    reservation.getApplicantName(), reservation.getPhoneNumber(), reservation.getCreatedAt());
        }
    }
}
