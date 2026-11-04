package com.dgmoonlabs.cms.domain.common.grade.service;

import com.dgmoonlabs.cms.domain.common.grade.constant.GradeChangeReason;
import com.dgmoonlabs.cms.domain.common.grade.dto.GradeDtos.*;
import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGrade;
import com.dgmoonlabs.cms.domain.common.grade.entity.MemberGradeHistory;
import com.dgmoonlabs.cms.domain.common.grade.repository.MemberGradeHistoryRepository;
import com.dgmoonlabs.cms.domain.common.grade.repository.MemberGradeRepository;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.error.GradeRequiredException;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {
    /** 등급을 참조하는 테이블과 컬럼. 쓰고 있는 등급을 지우면 막아 두었던 메뉴가 열리므로 삭제를 막는다. */
    private static final List<String[]> GRADE_REFERENCES = List.of(
            new String[]{"board_config", "read_grade_id"}, new String[]{"board_config", "write_grade_id"},
            new String[]{"archive", "access_grade_id"}, new String[]{"content", "access_grade_id"},
            new String[]{"media", "access_grade_id"}, new String[]{"program", "access_grade_id"},
            new String[]{"product", "access_grade_id"}, new String[]{"product", "grant_grade_id"});

    private final MemberGradeRepository gradeRepository;
    private final MemberGradeHistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<GradeResponse> getGrades() {
        return gradeRepository.findAllByOrderByRankAscIdAsc().stream().map(GradeResponse::from).toList();
    }

    @Transactional
    public GradeResponse create(final GradeSaveRequest request) {
        if (gradeRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_NAME);
        }
        MemberGrade grade = gradeRepository.save(MemberGrade.builder()
                .name(request.name()).description(request.description()).rank(request.rank()).build());
        applyDefault(grade, request.isDefault());
        return GradeResponse.from(grade);
    }

    @Transactional
    public GradeResponse update(final Long id, final GradeSaveRequest request) {
        if (gradeRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_NAME);
        }
        MemberGrade grade = findGrade(id);
        grade.update(request.name(), request.description(), request.rank());
        applyDefault(grade, request.isDefault());
        return GradeResponse.from(grade);
    }

    @Transactional
    public void delete(final Long id) {
        MemberGrade grade = findGrade(id);
        if (grade.isDefault() || memberRepository.existsByGradeIdOrBaseGradeId(id, id) || isReferenced(id)) {
            throw new BusinessException(ErrorCode.GRADE_IN_USE);
        }
        gradeRepository.delete(grade);
    }

    public Long validGradeId(final Long gradeId) {
        if (gradeId != null && !gradeRepository.existsById(gradeId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return gradeId;
    }

    @Transactional(readOnly = true)
    public boolean meets(final Long requiredGradeId) {
        if (requiredGradeId == null || CurrentUser.isAdmin()) {
            return true;
        }
        Optional<MemberGrade> required = gradeRepository.findById(requiredGradeId);
        if (required.isEmpty()) {
            return false;
        }
        return currentMember().flatMap(this::effectiveGrade)
                .map(grade -> grade.getRank() >= required.get().getRank())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public void require(final Long requiredGradeId) {
        if (!meets(requiredGradeId)) {
            throw new GradeRequiredException(gradeRepository.findById(requiredGradeId).map(MemberGrade::getName).orElse("지정된"));
        }
    }

    @Transactional(readOnly = true)
    public MyGradeResponse getMine() {
        Member member = currentMember().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        return toMyGrade(member);
    }

    @Transactional(readOnly = true)
    public MyGradeResponse getOf(final Long memberId) {
        return toMyGrade(findMember(memberId));
    }

    @Transactional(readOnly = true)
    public List<GradeHistoryResponse> historyOf(final Long memberId) {
        Map<Long, String> names = gradeNames();
        return historyRepository.findTop50ByMemberIdOrderByIdDesc(memberId).stream()
                .map(history -> GradeHistoryResponse.of(history, names)).toList();
    }

    @Transactional
    public void assign(final Long memberId, final MemberGradeRequest request) {
        Member member = findMember(memberId);
        validGradeId(request.gradeId());
        Snapshot before = Snapshot.of(member);
        Long base = request.expiresAt() == null ? null : permanentGradeId(member);
        member.changeGrade(request.gradeId(), request.expiresAt(), base);
        record(member, before, GradeChangeReason.MANUAL, null,
                CurrentUser.get().map(LoginUser::username).orElse(null));
    }

    /**
     * 결제로 등급을 준다. 이미 더 높은 등급이면 그대로 두고, 같은 등급이면 만료일만 늘린다.
     * 기간이 있는 등급은 끝나면 원래의 무기한 등급으로 돌아간다.
     */
    @Transactional
    public void grantByOrder(final Long memberId, final Long gradeId, final LocalDateTime expiresAt,
                             final GradeChangeReason reason, final Long orderId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId).orElse(null);
        MemberGrade grant = gradeId == null ? null : gradeRepository.findById(gradeId).orElse(null);
        if (member == null || grant == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Snapshot before = Snapshot.of(member);
        Long currentId = member.currentGradeId(now);
        MemberGrade current = effectiveGrade(member).orElse(null);
        if (Objects.equals(currentId, gradeId)) {
            if (member.isGradeExpired(now) || member.getGradeExpiresAt() == null) {
                return;
            }
            LocalDateTime extended = expiresAt == null || member.getGradeExpiresAt().isAfter(expiresAt)
                    ? (expiresAt == null ? null : member.getGradeExpiresAt()) : expiresAt;
            member.changeGrade(gradeId, extended, member.getBaseGradeId());
        } else if (current != null && current.getRank() > grant.getRank()) {
            return;
        } else {
            member.changeGrade(gradeId, expiresAt, permanentGradeId(member));
        }
        record(member, before, reason, orderId, null);
    }

    /**
     * 취소된 주문으로 받은 등급을 되돌린다. 그 주문 뒤로 등급이 다시 바뀌었다면(관리자 지정, 다른 구매)
     * 지금 등급을 존중해 그대로 둔다.
     */
    @Transactional
    public void revokeByOrder(final Long memberId, final Long orderId, final List<Long> gradeIds) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId).orElse(null);
        MemberGradeHistory last = historyRepository.findFirstByMemberIdOrderByIdDesc(memberId).orElse(null);
        if (member == null || last == null || !orderId.equals(last.getOrderId()) || !gradeIds.contains(last.getToGradeId())
                || !Objects.equals(member.getGradeId(), last.getToGradeId())) {
            return;
        }
        Snapshot before = Snapshot.of(member);
        member.changeGrade(last.getFromGradeId(), last.getFromExpiresAt(), last.getFromBaseGradeId());
        record(member, before, GradeChangeReason.REFUNDED, orderId, null);
    }

    @Scheduled(cron = "${cms.grade.expire-cron:0 10 0 * * *}")
    public void expireOnSchedule() {
        int expired = expireDue(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} member grades", expired);
        }
    }

    @Transactional
    public int expireDue(final LocalDateTime now) {
        List<Member> members = memberRepository.findAllByGradeExpiresAtLessThanEqualAndDeletedAtIsNull(now);
        for (Member member : members) {
            Snapshot before = Snapshot.of(member);
            member.changeGrade(member.getBaseGradeId(), null, null);
            record(member, before, GradeChangeReason.EXPIRED, null, null);
        }
        return members.size();
    }

    private Optional<Member> currentMember() {
        return CurrentUser.get().filter(user -> CurrentUser.isMember())
                .flatMap(user -> memberRepository.findByIdAndDeletedAtIsNull(user.id()));
    }

    private Optional<MemberGrade> effectiveGrade(final Member member) {
        Long gradeId = member.currentGradeId(LocalDateTime.now());
        return Optional.ofNullable(gradeId).flatMap(gradeRepository::findById)
                .or(gradeRepository::findFirstByIsDefaultTrue);
    }

    /** 기간 등급이 끝났을 때 돌아갈 등급: 지금 무기한 등급이면 그것, 기간 등급 중이면 그 등급의 복귀 등급. */
    private static Long permanentGradeId(final Member member) {
        return member.getGradeExpiresAt() == null ? member.getGradeId() : member.getBaseGradeId();
    }

    private MyGradeResponse toMyGrade(final Member member) {
        LocalDateTime now = LocalDateTime.now();
        MemberGrade grade = effectiveGrade(member).orElse(null);
        boolean timed = !member.isGradeExpired(now) && member.getGradeExpiresAt() != null;
        String baseName = timed ? Optional.ofNullable(member.getBaseGradeId()).flatMap(gradeRepository::findById)
                .or(gradeRepository::findFirstByIsDefaultTrue).map(MemberGrade::getName).orElse(null) : null;
        return new MyGradeResponse(grade == null ? null : grade.getId(), grade == null ? null : grade.getName(),
                grade == null ? 0 : grade.getRank(), timed ? member.getGradeExpiresAt() : null, baseName);
    }

    private record Snapshot(Long gradeId, LocalDateTime expiresAt, Long baseGradeId) {
        static Snapshot of(final Member member) {
            return new Snapshot(member.getGradeId(), member.getGradeExpiresAt(), member.getBaseGradeId());
        }
    }

    private void record(final Member member, final Snapshot before, final GradeChangeReason reason, final Long orderId,
                        final String changedBy) {
        historyRepository.save(MemberGradeHistory.builder()
                .memberId(member.getId())
                .fromGradeId(before.gradeId())
                .fromExpiresAt(before.expiresAt())
                .fromBaseGradeId(before.baseGradeId())
                .toGradeId(member.getGradeId())
                .expiresAt(member.getGradeExpiresAt())
                .reason(reason)
                .orderId(orderId)
                .changedBy(changedBy)
                .build());
    }

    private void applyDefault(final MemberGrade grade, final boolean isDefault) {
        if (isDefault) {
            gradeRepository.findAll().forEach(other -> other.markDefault(other.getId().equals(grade.getId())));
        } else {
            grade.markDefault(false);
        }
    }

    private boolean isReferenced(final Long gradeId) {
        return GRADE_REFERENCES.stream().anyMatch(ref -> {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from " + ref[0] + " where " + ref[1] + " = ?", Integer.class, gradeId);
            return count != null && count > 0;
        });
    }

    @Transactional(readOnly = true)
    public Function<Member, String> gradeNamer() {
        Map<Long, String> names = gradeNames();
        String defaultName = gradeRepository.findFirstByIsDefaultTrue().map(MemberGrade::getName).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        return member -> {
            Long gradeId = member.currentGradeId(now);
            return gradeId == null ? defaultName : names.getOrDefault(gradeId, defaultName);
        };
    }

    private Map<Long, String> gradeNames() {
        return gradeRepository.findAll().stream().collect(Collectors.toMap(MemberGrade::getId, MemberGrade::getName));
    }

    private MemberGrade findGrade(final Long id) {
        return gradeRepository.findById(id).orElseThrow(BusinessException::notFound);
    }

    private Member findMember(final Long id) {
        return memberRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
