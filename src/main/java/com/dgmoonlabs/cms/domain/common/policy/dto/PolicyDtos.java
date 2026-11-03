package com.dgmoonlabs.cms.domain.common.policy.dto;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.policy.entity.Policy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class PolicyDtos {
    private PolicyDtos() {
    }

    public record PolicySaveRequest(@NotBlank @Size(max = 200_000) String contentHtml, Boolean isUsing) {
        public boolean isUsingOrDefault() {
            return isUsing == null || isUsing;
        }
    }

    public record PolicyResponse(PolicyType type, String title, String contentHtml, boolean isUsing, boolean optional,
                                 int version, String lastWorker, LocalDateTime modifiedAt) {
        public static PolicyResponse from(final Policy policy) {
            return new PolicyResponse(policy.getType(), policy.getType().getLabel(), policy.getContentHtml(),
                    policy.isUsing(), policy.getType().isOptional(), policy.getVersion(), policy.getLastWorker(),
                    policy.getModifiedAt());
        }

        public static PolicyResponse template(final PolicyType type) {
            return new PolicyResponse(type, type.getLabel(), type.getDefaultContentHtml(), true, type.isOptional(), 0,
                    null, null);
        }
    }
}
