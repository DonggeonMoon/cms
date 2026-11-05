package com.dgmoonlabs.cms.domain.business.attendance.dto;

import com.dgmoonlabs.cms.domain.business.attendance.entity.ClassAttendance;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class ClassAttendanceDtos {
    private ClassAttendanceDtos() {
    }

    public record MarkRequest(@NotNull Long subscriptionId, @NotNull LocalDate attendedOn, boolean present,
                              @Size(max = 200) String note) {
    }

    /** 출결을 아직 안 매겼으면 present는 null. */
    public record RosterEntry(Long subscriptionId, Long memberId, String memberName, Boolean present, String note) {
    }

    public record MyAttendance(LocalDate attendedOn, boolean present, String note) {
        public static MyAttendance from(final ClassAttendance attendance) {
            return new MyAttendance(attendance.getAttendedOn(), attendance.isPresent(), attendance.getNote());
        }
    }
}
