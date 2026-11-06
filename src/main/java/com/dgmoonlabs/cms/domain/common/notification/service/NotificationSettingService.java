package com.dgmoonlabs.cms.domain.common.notification.service;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationType;
import com.dgmoonlabs.cms.domain.common.notification.dto.NotificationDtos.NotificationTypeSettingResponse;
import com.dgmoonlabs.cms.domain.common.notification.entity.NotificationTypeSetting;
import com.dgmoonlabs.cms.domain.common.notification.repository.NotificationTypeSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final NotificationTypeSettingRepository repository;

    /** 설정이 없으면(한 번도 끈 적 없으면) 기본값은 사용(true)이다. */
    @Transactional(readOnly = true)
    public boolean isEnabled(final NotificationType type) {
        return repository.findByType(type).map(NotificationTypeSetting::isEnabled).orElse(true);
    }

    @Transactional(readOnly = true)
    public List<NotificationTypeSettingResponse> getAll() {
        return Arrays.stream(NotificationType.values())
                .map((type) -> NotificationTypeSettingResponse.of(type, isEnabled(type)))
                .toList();
    }

    @Transactional
    public List<NotificationTypeSettingResponse> update(final Map<NotificationType, Boolean> request) {
        request.forEach((type, enabled) -> {
            NotificationTypeSetting setting = repository.findByType(type)
                    .orElseGet(() -> repository.save(NotificationTypeSetting.builder().type(type).enabled(true).build()));
            setting.update(Boolean.TRUE.equals(enabled));
        });
        return getAll();
    }
}
