package com.dgmoonlabs.cms.domain.business.status.dto;

import com.dgmoonlabs.cms.domain.business.status.entity.StatusDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class StatusDtos {
    private StatusDtos() {
    }

    public record StatusResponse(Long id, String code, String label, String baseStatus, boolean system, int sequence) {
        public static StatusResponse from(final StatusDefinition definition) {
            return new StatusResponse(definition.getId(), definition.getCode(), definition.getLabel(), definition.getBaseStatus(),
                    definition.isSystem(), definition.getSequence());
        }
    }

    public record StatusSaveRequest(@NotBlank @Size(max = 50) String label, @Size(max = 40) String baseStatus, Integer sequence) {
    }

    public record CustomStatusRequest(@Size(max = 40) String code) {
    }
}
