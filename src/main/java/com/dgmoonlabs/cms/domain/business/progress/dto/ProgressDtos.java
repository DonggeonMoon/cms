package com.dgmoonlabs.cms.domain.business.progress.dto;

import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageEvent;
import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class ProgressDtos {
    private ProgressDtos() {
    }

    public record StageTemplateSaveRequest(@NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 100) String label,
                                           int sequence) {
    }

    public record StageTemplateResponse(Long id, String code, String label, int sequence) {
        public static StageTemplateResponse from(final ProgressStageTemplate template) {
            return new StageTemplateResponse(template.getId(), template.getCode(), template.getLabel(), template.getSequence());
        }
    }

    public record AdvanceRequest(@NotNull @Size(max = 50) String stageCode, @Size(max = 1000) String memo) {
    }

    public record StageEventResponse(Long id, String stageCode, String stageLabel, String memo, String actorName,
                                     LocalDateTime createdAt) {
        public static StageEventResponse from(final ProgressStageEvent event) {
            return new StageEventResponse(event.getId(), event.getStageCode(), event.getStageLabel(), event.getMemo(),
                    event.getActorName(), event.getCreatedAt());
        }
    }

    public record ProgressResponse(List<StageTemplateResponse> templates, List<StageEventResponse> history, String currentStageCode) {
    }
}
