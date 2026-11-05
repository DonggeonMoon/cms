package com.dgmoonlabs.cms.domain.common.notification.repository;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import com.dgmoonlabs.cms.domain.common.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Optional<Notification> findByIdAndMemberId(Long id, Long memberId);

    long countByMemberIdAndChannelAndReadFalse(Long memberId, NotificationChannel channel);
}
