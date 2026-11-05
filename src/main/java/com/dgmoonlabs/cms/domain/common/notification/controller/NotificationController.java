package com.dgmoonlabs.cms.domain.common.notification.controller;

import com.dgmoonlabs.cms.domain.common.notification.dto.NotificationDtos.NotificationResponse;
import com.dgmoonlabs.cms.domain.common.notification.service.NotificationService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/api/members/me/notifications")
    public ResponseEntity<PagedModel<NotificationResponse>> myNotifications(@AuthenticationPrincipal final LoginUser user,
                                                                             @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(notificationService.myNotifications(user.id(), pageable)));
    }

    @GetMapping("/api/members/me/notifications/unread-count")
    public ResponseEntity<Long> unreadCount(@AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(notificationService.unreadCount(user.id()));
    }

    @PostMapping("/api/members/me/notifications/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        notificationService.markRead(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
