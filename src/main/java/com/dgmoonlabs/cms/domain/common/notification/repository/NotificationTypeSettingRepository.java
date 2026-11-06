package com.dgmoonlabs.cms.domain.common.notification.repository;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationType;
import com.dgmoonlabs.cms.domain.common.notification.entity.NotificationTypeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTypeSettingRepository extends JpaRepository<NotificationTypeSetting, Long> {
    Optional<NotificationTypeSetting> findByType(NotificationType type);
}
