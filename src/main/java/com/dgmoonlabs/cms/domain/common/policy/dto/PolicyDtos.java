package com.dgmoonlabs.cms.domain.common.policy.dto;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.policy.entity.Policy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class PolicyDtos {
    private PolicyDtos() {
    }

    public record PolicySaveRequest(@NotBlank @Size(max = 200_000) String contentHtml) {
    }

    // version 0 means the built-in template is shown because nothing has been written yet
    public record PolicyResponse(PolicyType type, String title, String contentHtml, int version, String lastWorker,
                                 LocalDateTime modifiedAt) {
        public static PolicyResponse from(final Policy policy) {
            return new PolicyResponse(policy.getType(), policy.getType().getLabel(), policy.getContentHtml(),
                    policy.getVersion(), policy.getLastWorker(), policy.getModifiedAt());
        }

        public static PolicyResponse template(final PolicyType type) {
            return new PolicyResponse(type, type.getLabel(), type.getDefaultContentHtml(), 0, null, null);
        }
    }
}
