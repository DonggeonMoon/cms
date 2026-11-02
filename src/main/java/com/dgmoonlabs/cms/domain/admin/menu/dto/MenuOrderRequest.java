package com.dgmoonlabs.cms.domain.admin.menu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MenuOrderRequest(
        Long parentId,
        @NotEmpty List<@NotNull Long> ids
) {
}
