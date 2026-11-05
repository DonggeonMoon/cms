package com.dgmoonlabs.cms.domain.common.notification.service;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import com.dgmoonlabs.cms.domain.common.notification.dto.NotificationDtos.NotificationResponse;
import com.dgmoonlabs.cms.domain.common.notification.entity.Notification;
import com.dgmoonlabs.cms.domain.common.notification.repository.NotificationRepository;
import com.dgmoonlabs.cms.domain.common.notification.sender.EmailSender;
import com.dgmoonlabs.cms.domain.common.notification.sender.SmsSender;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    /** EMAIL·SMS는 회원에게 연락처가 있을 때만 보낸다. */
    @Transactional
    public void notify(final Long memberId, final String title, final String content, final String link,
                        final Set<NotificationChannel> channels) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return;
        }
        for (NotificationChannel channel : channels) {
            switch (channel) {
                case IN_APP -> save(memberId, NotificationChannel.IN_APP, title, content, link);
                case EMAIL -> {
                    if (member.getEmail() != null && !member.getEmail().isBlank()) {
                        emailSender.send(member.getEmail(), title, content);
                        save(memberId, NotificationChannel.EMAIL, title, content, link);
                    }
                }
                case SMS -> {
                    if (member.getMobilePhoneNumber() != null && !member.getMobilePhoneNumber().isBlank()) {
                        smsSender.send(member.getMobilePhoneNumber(), title, content);
                        save(memberId, NotificationChannel.SMS, title, content, link);
                    }
                }
            }
        }
    }

    public void notifyAllChannels(final Long memberId, final String title, final String content, final String link) {
        notify(memberId, title, content, link, EnumSet.allOf(NotificationChannel.class));
    }

    private void save(final Long memberId, final NotificationChannel channel, final String title, final String content,
                       final String link) {
        notificationRepository.save(Notification.builder()
                .memberId(memberId).channel(channel).title(title).content(content).link(link).build());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> myNotifications(final Long memberId, final Pageable pageable) {
        return notificationRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(final Long memberId) {
        return notificationRepository.countByMemberIdAndChannelAndReadFalse(memberId, NotificationChannel.IN_APP);
    }

    @Transactional
    public void markRead(final Long memberId, final Long id) {
        Notification notification = notificationRepository.findByIdAndMemberId(id, memberId).orElseThrow(BusinessException::notFound);
        notification.markRead();
    }
}
