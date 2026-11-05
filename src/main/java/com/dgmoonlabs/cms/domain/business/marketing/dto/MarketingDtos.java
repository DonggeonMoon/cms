package com.dgmoonlabs.cms.domain.business.marketing.dto;

import com.dgmoonlabs.cms.domain.business.marketing.entity.MarketingCampaign;
import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public final class MarketingDtos {
    private MarketingDtos() {
    }

    public record CampaignSendRequest(@NotBlank @Size(max = 100) String title, @NotBlank @Size(max = 2000) String content,
                                      Long targetGradeId, @NotEmpty Set<NotificationChannel> channels) {
    }

    public record CampaignResponse(Long id, String title, String content, Long targetGradeId, Set<NotificationChannel> channels,
                                    int recipientCount, LocalDateTime sentAt) {
        public static CampaignResponse from(final MarketingCampaign entity) {
            return new CampaignResponse(entity.getId(), entity.getTitle(), entity.getContent(), entity.getTargetGradeId(),
                    Set.copyOf(entity.getChannels()), entity.getRecipientCount(), entity.getSentAt());
        }
    }
}
