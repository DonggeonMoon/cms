package com.dgmoonlabs.cms.domain.business.reservation.dto;

import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import jakarta.validation.constraints.*;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            boolean isEnabled
    ) {
        public boolean hasValidPeriods() {
            return isOrdered(startDateTime, endDateTime) && isOrdered(applicationStartDateTime, applicationEndDateTime);
        }

        private static boolean isOrdered(final LocalDateTime start, final LocalDateTime end) {
            return start == null || end == null || end.isAfter(start);
        }
    }

    public record ProgramResponse(Long id, String name, String description, Long imageFileId, String imageUrl,
                                  BigDecimal price, Integer maximumPerson, Integer availablePerson,
                                  LocalDateTime startDateTime, LocalDateTime endDateTime,
                                  LocalDateTime applicationStartDateTime, LocalDateTime applicationEndDateTime,
                                  String contentHtml, boolean isEnabled, boolean isOpen, Double averageRating) {
        public static ProgramResponse of(final Program program, final boolean isOpen) {
            return of(program, isOpen, null);
        }

        public static ProgramResponse of(final Program program, final boolean isOpen, final Double averageRating) {
            return new ProgramResponse(program.getId(), program.getName(), program.getDescription(),
                    program.getImageFileId(),
                    program.getImageFileId() == null ? null : "/api/files/" + program.getImageFileId() + "/view",
                    program.getPrice(), program.getMaximumPerson(), program.getAvailablePerson(),
                    program.getStartDateTime(), program.getEndDateTime(), program.getApplicationStartDateTime(),
                    program.getApplicationEndDateTime(), program.getContentHtml(), program.isEnabled(), isOpen, averageRating);
        }
    }

    @Builder
    public record ReservationCreateRequest(
            @NotNull Long programId,
            @NotNull @Min(1) @Max(100) Integer quantity,
            @NotBlank @Size(max = 50) String applicantName,
            @NotBlank @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber
    ) {
    }

    public record ReservationResponse(Long id, String reservationNumber, Long userId, Long programId, String programName,
                                      int quantity, BigDecimal totalPrice, ReservationStatus status,
                                      String applicantName, String phoneNumber, LocalDateTime createdAt) {
        public static ReservationResponse from(final Reservation reservation) {
            return new ReservationResponse(reservation.getId(), reservation.getReservationNumber(),
                    reservation.getUserId(), reservation.getProgramId(), reservation.getProgramName(),
                    reservation.getQuantity(), reservation.getTotalPrice(), reservation.getStatus(),
                    reservation.getApplicantName(), reservation.getPhoneNumber(), reservation.getCreatedAt());
        }
    }
}
