package com.dgmoonlabs.cms.domain.common.notification.dto;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import com.dgmoonlabs.cms.domain.common.notification.entity.Notification;

import java.time.LocalDateTime;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationResponse(Long id, NotificationChannel channel, String title, String content, String link,
                                        boolean read, LocalDateTime createdAt) {
        public static NotificationResponse from(final Notification notification) {
            return new NotificationResponse(notification.getId(), notification.getChannel(), notification.getTitle(),
                    notification.getContent(), notification.getLink(), notification.isRead(), notification.getCreatedAt());
        }
    }

    public record AdminNotificationResponse(Long id, Long memberId, String memberName, NotificationChannel channel, String title,
                                             String content, String link, boolean read, LocalDateTime createdAt) {
        public static AdminNotificationResponse from(final Notification notification, final String memberName) {
            return new AdminNotificationResponse(notification.getId(), notification.getMemberId(), memberName, notification.getChannel(),
                    notification.getTitle(), notification.getContent(), notification.getLink(), notification.isRead(), notification.getCreatedAt());
        }
    }
}
