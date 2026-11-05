package com.dgmoonlabs.cms.domain.business.attendance.service;

import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.MarkRequest;
import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.MyAttendance;
import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.RosterEntry;
import com.dgmoonlabs.cms.domain.business.attendance.entity.ClassAttendance;
import com.dgmoonlabs.cms.domain.business.attendance.repository.ClassAttendanceRepository;
import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.domain.business.subscription.entity.Subscription;
import com.dgmoonlabs.cms.domain.business.subscription.repository.SubscriptionRepository;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassAttendanceService {
    private final ClassAttendanceRepository attendanceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<RosterEntry> getRoster(final Long productId, final LocalDate date) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByProductIdAndStatusOrderByIdAsc(productId, SubscriptionStatus.ACTIVE);
        Map<Long, String> names = memberRepository.findAllById(subscriptions.stream().map(Subscription::getMemberId).distinct().toList())
                .stream().collect(Collectors.toMap(Member::getId, Member::getName));
        Map<Long, ClassAttendance> marked = attendanceRepository.findAllByProductIdAndAttendedOn(productId, date)
                .stream().collect(Collectors.toMap(ClassAttendance::getSubscriptionId, Function.identity()));
        return subscriptions.stream()
                .map(subscription -> {
                    ClassAttendance attendance = marked.get(subscription.getId());
                    return new RosterEntry(subscription.getId(), subscription.getMemberId(), names.get(subscription.getMemberId()),
                            attendance == null ? null : attendance.isPresent(), attendance == null ? null : attendance.getNote());
                })
                .toList();
    }

    @Transactional
    public RosterEntry mark(final MarkRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId()).orElseThrow(BusinessException::notFound);
        String note = blankToNull(request.note());
        ClassAttendance attendance = attendanceRepository.findBySubscriptionIdAndAttendedOn(request.subscriptionId(), request.attendedOn())
                .orElse(null);
        if (attendance == null) {
            attendance = attendanceRepository.save(ClassAttendance.builder()
                    .subscriptionId(subscription.getId())
                    .memberId(subscription.getMemberId())
                    .productId(subscription.getProductId())
                    .attendedOn(request.attendedOn())
                    .present(request.present())
                    .note(note)
                    .build());
        } else {
            attendance.mark(request.present(), note);
        }
        return new RosterEntry(subscription.getId(), subscription.getMemberId(),
                memberRepository.findById(subscription.getMemberId()).map(Member::getName).orElse(null),
                attendance.isPresent(), attendance.getNote());
    }

    @Transactional(readOnly = true)
    public Page<MyAttendance> getMine(final Long memberId, final Long subscriptionId, final Pageable pageable) {
        subscriptionRepository.findByIdAndMemberId(subscriptionId, memberId).orElseThrow(BusinessException::notFound);
        return attendanceRepository.findAllBySubscriptionIdOrderByAttendedOnDesc(subscriptionId, pageable).map(MyAttendance::from);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
