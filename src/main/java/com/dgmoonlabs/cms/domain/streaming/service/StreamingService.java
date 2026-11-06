package com.dgmoonlabs.cms.domain.streaming.service;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.streaming.dto.StreamingDtos;
import com.dgmoonlabs.cms.domain.streaming.dto.StreamingDtos.*;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingEnrollment;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingProgress;
import com.dgmoonlabs.cms.domain.streaming.entity.WatchedRanges;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingAccessRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingEnrollmentRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingProgressRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreamingService {
    private static final Set<MediaType> PLAYABLE = EnumSet.of(MediaType.VIDEO, MediaType.AUDIO);

    private final GradeService gradeService;
    private final StreamingRepository streamingRepository;
    private final StreamingProgressRepository progressRepository;
    private final StreamingEnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final StreamingAccessRepository accessRepository;
    private final StreamingSalesService salesService;

    @Transactional(readOnly = true)
    public Page<StreamingResponse> getStreamingList(final String keyword, final MediaType mediaType, final Pageable pageable) {
        return toResponses(streamingRepository.search(Objects.requireNonNullElse(keyword, ""), typesOf(mediaType), true, pageable),
                currentMemberId());
    }

    @Transactional(readOnly = true)
    public StreamingResponse getStreaming(final Long id) {
        Streaming streaming = findPublic(id);
        gradeService.require(streaming.getAccessGradeId());
        return toResponses(List.of(streaming), currentMemberId()).getFirst();
    }

    @Transactional
    public MyProgress start(final Long id, final LoginUser member) {
        Streaming streaming = findPublic(id);
        gradeService.require(streaming.getAccessGradeId());
        requirePurchased(streaming, member.id());
        if (!streaming.isTrackProgress()) {
            return null;
        }
        StreamingEnrollment enrollment = enrollmentOf(streaming.getId(), member.id());
        StreamingProgress progress = progressRepository.findByStreamingIdAndMemberId(streaming.getId(), member.id())
                .orElseGet(() -> progressRepository.save(StreamingProgress.start(streaming.getId(), member.id())));
        return MyProgress.of(enrollment, progress);
    }

    @Transactional
    public MyProgress record(final Long id, final LoginUser member, final ProgressRequest request) {
        Streaming streaming = findPublic(id);
        gradeService.require(streaming.getAccessGradeId());
        requirePurchased(streaming, member.id());
        if (!streaming.isTrackProgress()) {
            return null;
        }
        StreamingEnrollment enrollment = enrollmentOf(streaming.getId(), member.id());
        StreamingProgress progress = progressRepository.findForUpdate(streaming.getId(), member.id())
                .orElseGet(() -> progressRepository.save(StreamingProgress.start(streaming.getId(), member.id())));

        int duration = streaming.getDurationSeconds();
        WatchedRanges ranges = WatchedRanges.parse(progress.getWatchedRanges());
        for (WatchedRange range : request.ranges()) {
            ranges = ranges.add((int) Math.round(range.start()), (int) Math.round(range.end()), duration);
        }
        progress.record(ranges, (int) Math.round(request.position()), duration);
        if (progress.getProgressRate() >= streaming.getCompletionRate()) {
            enrollment.complete();
        }
        progressRepository.flush();
        return MyProgress.of(enrollment, progress);
    }

    @Transactional(readOnly = true)
    public Page<MyStreaming> getMyStreaming(final Long memberId, final Pageable pageable) {
        Page<StreamingEnrollment> enrollments = enrollmentRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        List<Long> streamingIds = enrollments.map(StreamingEnrollment::getStreamingId).toList();
        Map<Long, Streaming> streamingItems = byKey(streamingRepository.findAllById(streamingIds), Streaming::getId);
        Map<Long, StreamingProgress> progresses = byKey(
                progressRepository.findAllByMemberIdAndStreamingIdIn(memberId, streamingIds), StreamingProgress::getStreamingId);
        Map<Long, FileResponse> files = filesOf(streamingItems.values());
        return enrollments.map(enrollment -> {
            Streaming streaming = streamingItems.get(enrollment.getStreamingId());
            FileResponse file = streaming == null ? null : files.get(streaming.getFileId());
            return new MyStreaming(enrollment.getStreamingId(),
                    streaming == null ? "삭제된 항목" : streaming.getTitle(),
                    streaming == null ? null : streaming.getMediaType(),
                    streaming == null ? null : StreamingDtos.thumbnailUrl(streaming),
                    streaming == null ? 0 : streaming.getDurationSeconds(),
                    streaming != null && streaming.isUsing() && streaming.getDeletedAt() == null,
                    MyProgress.of(enrollment, progresses.get(enrollment.getStreamingId())));
        });
    }

    @Transactional(readOnly = true)
    public Page<StreamingResponse> searchStreaming(final String keyword, final MediaType mediaType, final Pageable pageable) {
        return toResponses(streamingRepository.search(Objects.requireNonNullElse(keyword, ""), typesOf(mediaType), false, pageable), null);
    }

    /** 파일 주소를 직접 열지 못하게 보호해 두고, 권한을 확인한 뒤에만 내려준다. 범위 요청(Range)으로 건너뛰며 재생할 수 있다. */
    @Transactional(readOnly = true)
    public DownloadableFile stream(final Long id) {
        boolean admin = CurrentUser.isAdmin();
        Streaming streaming = admin ? findStreaming(id) : findPublic(id);
        if (!admin) {
            gradeService.require(streaming.getAccessGradeId());
            if (!canPlay(streaming, currentMemberId(), LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.STREAMING_PURCHASE_REQUIRED);
            }
        }
        return fileService.viewProtected(streaming.getFileId());
    }

    @Transactional
    public Long createStreaming(final StreamingSaveRequest request) {
        requirePrice(request);
        Streaming streaming = streamingRepository.save(Streaming.builder()
                .title(request.title().trim())
                .description(request.description())
                .fileId(request.fileId())
                .thumbnailFileId(requireImage(request.thumbnailFileId()))
                .mediaType(playableType(request.fileId()))
                .durationSeconds(request.durationSeconds())
                .completionRate(request.completionRateOrDefault())
                .trackProgress(request.trackProgressOrDefault())
                .isUsing(request.isUsingOrDefault())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .build());
        applySale(streaming, request);
        return streaming.getId();
    }

    @Transactional
    public void updateStreaming(final Long id, final StreamingSaveRequest request) {
        requirePrice(request);
        Streaming streaming = findStreaming(id);
        streaming.update(request.title().trim(), request.description(), request.fileId(),
                requireImage(request.thumbnailFileId()), playableType(request.fileId()),
                request.durationSeconds(), request.completionRateOrDefault(), request.trackProgressOrDefault(),
                request.isUsingOrDefault());
        streaming.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
        applySale(streaming, request);
    }

    @Transactional
    public void deleteStreaming(final Long id) {
        Streaming streaming = findStreaming(id);
        streaming.delete();
        salesService.syncProduct(streaming);
    }

    private void applySale(final Streaming streaming, final StreamingSaveRequest request) {
        streaming.changeSale(request.paidOrDefault(), request.price(), request.accessDays());
        fileService.protect(List.of(streaming.getFileId()));
        salesService.syncProduct(streaming);
    }

    private static void requirePrice(final StreamingSaveRequest request) {
        if (request.paidOrDefault() && (request.price() == null || request.price().signum() <= 0)) {
            throw new BusinessException(ErrorCode.STREAMING_PRICE_REQUIRED);
        }
    }

    private void requirePurchased(final Streaming streaming, final Long memberId) {
        if (!canPlay(streaming, memberId, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.STREAMING_PURCHASE_REQUIRED);
        }
    }

    private boolean canPlay(final Streaming streaming, final Long memberId, final LocalDateTime now) {
        return !streaming.isPaid() || (memberId != null && !accessRepository.findActive(memberId, List.of(streaming.getId()), now).isEmpty());
    }

    @Transactional(readOnly = true)
    public Page<LearnerResponse> getLearners(final Long id, final Pageable pageable) {
        findStreaming(id);
        Page<StreamingEnrollment> enrollments = enrollmentRepository.findAllByStreamingIdOrderByIdDesc(id, pageable);
        List<Long> memberIds = enrollments.map(StreamingEnrollment::getMemberId).toList();
        Map<Long, StreamingProgress> progresses = byKey(
                progressRepository.findAllByStreamingIdAndMemberIdIn(id, memberIds), StreamingProgress::getMemberId);
        Map<Long, String> usernames = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return enrollments.map(enrollment -> LearnerResponse.of(enrollment, progresses.get(enrollment.getMemberId()),
                usernames.getOrDefault(enrollment.getMemberId(), "(탈퇴 회원)")));
    }

    private Page<StreamingResponse> toResponses(final Page<Streaming> streamingItems, final Long memberId) {
        return new PageImpl<>(toResponses(streamingItems.getContent(), memberId), streamingItems.getPageable(), streamingItems.getTotalElements());
    }

    private List<StreamingResponse> toResponses(final List<Streaming> streamingItems, final Long memberId) {
        List<Long> streamingIds = streamingItems.stream().map(Streaming::getId).toList();
        Map<Long, StreamingEnrollment> enrollments = memberId == null ? Map.of() : byKey(
                enrollmentRepository.findAllByMemberIdAndStreamingIdIn(memberId, streamingIds), StreamingEnrollment::getStreamingId);
        Map<Long, StreamingProgress> progresses = memberId == null ? Map.of() : byKey(
                progressRepository.findAllByMemberIdAndStreamingIdIn(memberId, streamingIds), StreamingProgress::getStreamingId);
        Map<Long, FileResponse> files = filesOf(streamingItems);
        LocalDateTime now = LocalDateTime.now();
        boolean admin = CurrentUser.isAdmin();
        Map<Long, LocalDateTime> purchased = new HashMap<>();
        if (memberId != null) {
            accessRepository.findActive(memberId, streamingIds, now).forEach(access -> purchased.merge(access.getStreamingId(),
                    access.getExpiresAt() == null ? LocalDateTime.MAX : access.getExpiresAt(), (a, b) -> a.isAfter(b) ? a : b));
        }
        return streamingItems.stream().map(streaming -> {
            LocalDateTime until = purchased.get(streaming.getId());
            boolean playable = admin || !streaming.isPaid() || until != null;
            return StreamingResponse.of(streaming, files.get(streaming.getFileId()),
                    MyProgress.of(enrollments.get(streaming.getId()), progresses.get(streaming.getId())), playable,
                    until == null || until.equals(LocalDateTime.MAX) ? null : until);
        }).toList();
    }

    private Map<Long, FileResponse> filesOf(final Collection<Streaming> streamingItems) {
        return byKey(fileService.getFiles(streamingItems.stream().map(Streaming::getFileId).distinct().toList()), FileResponse::id);
    }

    private StreamingEnrollment enrollmentOf(final Long streamingId, final Long memberId) {
        return enrollmentRepository.findByStreamingIdAndMemberId(streamingId, memberId)
                .orElseGet(() -> enrollmentRepository.save(StreamingEnrollment.start(streamingId, memberId)));
    }

    private Long requireImage(final Long fileId) {
        if (fileId != null && fileService.getFile(fileId).mediaType() != MediaType.IMAGE) {
            throw new BusinessException(ErrorCode.STREAMING_THUMBNAIL_IMAGE_REQUIRED);
        }
        return fileId;
    }

    private static Set<MediaType> typesOf(final MediaType mediaType) {
        return mediaType == null ? PLAYABLE : EnumSet.of(mediaType);
    }

    private MediaType playableType(final Long fileId) {
        MediaType type = fileService.getFile(fileId).mediaType();
        if (!PLAYABLE.contains(type)) {
            throw new BusinessException(ErrorCode.STREAMING_FILE_REQUIRED);
        }
        return type;
    }

    private static Long currentMemberId() {
        return CurrentUser.get().filter(user -> user.userType() == UserType.MEMBER).map(LoginUser::id).orElse(null);
    }

    private Streaming findPublic(final Long id) {
        return streamingRepository.findByIdAndIsUsingTrueAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private Streaming findStreaming(final Long id) {
        return streamingRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static <T> Map<Long, T> byKey(final Collection<T> items, final Function<T, Long> key) {
        return items.stream().collect(Collectors.toMap(key, Function.identity(), (first, second) -> first));
    }
}
