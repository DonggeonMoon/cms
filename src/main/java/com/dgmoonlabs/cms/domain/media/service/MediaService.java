package com.dgmoonlabs.cms.domain.media.service;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.media.dto.MediaDtos;
import com.dgmoonlabs.cms.domain.media.dto.MediaDtos.*;
import com.dgmoonlabs.cms.domain.media.entity.Media;
import com.dgmoonlabs.cms.domain.media.entity.MediaEnrollment;
import com.dgmoonlabs.cms.domain.media.entity.MediaProgress;
import com.dgmoonlabs.cms.domain.media.entity.WatchedRanges;
import com.dgmoonlabs.cms.domain.media.repository.MediaAccessRepository;
import com.dgmoonlabs.cms.domain.media.repository.MediaEnrollmentRepository;
import com.dgmoonlabs.cms.domain.media.repository.MediaProgressRepository;
import com.dgmoonlabs.cms.domain.media.repository.MediaRepository;
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
public class MediaService {
    private static final Set<MediaType> PLAYABLE = EnumSet.of(MediaType.VIDEO, MediaType.AUDIO);

    private final GradeService gradeService;
    private final MediaRepository mediaRepository;
    private final MediaProgressRepository progressRepository;
    private final MediaEnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final MediaAccessRepository accessRepository;
    private final MediaSalesService salesService;

    @Transactional(readOnly = true)
    public Page<MediaResponse> getMediaList(final String keyword, final MediaType mediaType, final Pageable pageable) {
        return toResponses(mediaRepository.search(Objects.requireNonNullElse(keyword, ""), typesOf(mediaType), true, pageable),
                currentMemberId());
    }

    @Transactional(readOnly = true)
    public MediaResponse getMedia(final Long id) {
        Media media = findPublic(id);
        gradeService.require(media.getAccessGradeId());
        return toResponses(List.of(media), currentMemberId()).getFirst();
    }

    @Transactional
    public MyProgress start(final Long id, final LoginUser member) {
        Media media = findPublic(id);
        gradeService.require(media.getAccessGradeId());
        requirePurchased(media, member.id());
        MediaEnrollment enrollment = enrollmentOf(media.getId(), member.id());
        MediaProgress progress = progressRepository.findByMediaIdAndMemberId(media.getId(), member.id())
                .orElseGet(() -> progressRepository.save(MediaProgress.start(media.getId(), member.id())));
        return MyProgress.of(enrollment, progress);
    }

    @Transactional
    public MyProgress record(final Long id, final LoginUser member, final ProgressRequest request) {
        Media media = findPublic(id);
        gradeService.require(media.getAccessGradeId());
        requirePurchased(media, member.id());
        MediaEnrollment enrollment = enrollmentOf(media.getId(), member.id());
        MediaProgress progress = progressRepository.findForUpdate(media.getId(), member.id())
                .orElseGet(() -> progressRepository.save(MediaProgress.start(media.getId(), member.id())));

        int duration = media.getDurationSeconds();
        WatchedRanges ranges = WatchedRanges.parse(progress.getWatchedRanges());
        for (WatchedRange range : request.ranges()) {
            ranges = ranges.add((int) Math.round(range.start()), (int) Math.round(range.end()), duration);
        }
        progress.record(ranges, (int) Math.round(request.position()), duration);
        if (progress.getProgressRate() >= media.getCompletionRate()) {
            enrollment.complete();
        }
        progressRepository.flush();
        return MyProgress.of(enrollment, progress);
    }

    @Transactional(readOnly = true)
    public Page<MyMedia> getMyMedia(final Long memberId, final Pageable pageable) {
        Page<MediaEnrollment> enrollments = enrollmentRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        List<Long> mediaIds = enrollments.map(MediaEnrollment::getMediaId).toList();
        Map<Long, Media> mediaItems = byKey(mediaRepository.findAllById(mediaIds), Media::getId);
        Map<Long, MediaProgress> progresses = byKey(
                progressRepository.findAllByMemberIdAndMediaIdIn(memberId, mediaIds), MediaProgress::getMediaId);
        Map<Long, FileResponse> files = filesOf(mediaItems.values());
        return enrollments.map(enrollment -> {
            Media media = mediaItems.get(enrollment.getMediaId());
            FileResponse file = media == null ? null : files.get(media.getFileId());
            return new MyMedia(enrollment.getMediaId(),
                    media == null ? "삭제된 항목" : media.getTitle(),
                    media == null ? null : media.getMediaType(),
                    media == null ? null : MediaDtos.thumbnailUrl(media),
                    media == null ? 0 : media.getDurationSeconds(),
                    media != null && media.isUsing() && media.getDeletedAt() == null,
                    MyProgress.of(enrollment, progresses.get(enrollment.getMediaId())));
        });
    }

    @Transactional(readOnly = true)
    public Page<MediaResponse> searchMedia(final String keyword, final MediaType mediaType, final Pageable pageable) {
        return toResponses(mediaRepository.search(Objects.requireNonNullElse(keyword, ""), typesOf(mediaType), false, pageable), null);
    }

    /** 파일 주소를 직접 열지 못하게 보호해 두고, 권한을 확인한 뒤에만 내려준다. 범위 요청(Range)으로 건너뛰며 재생할 수 있다. */
    @Transactional(readOnly = true)
    public DownloadableFile stream(final Long id) {
        boolean admin = CurrentUser.isAdmin();
        Media media = admin ? findMedia(id) : findPublic(id);
        if (!admin) {
            gradeService.require(media.getAccessGradeId());
            if (!canPlay(media, currentMemberId(), LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.MEDIA_PURCHASE_REQUIRED);
            }
        }
        return fileService.viewProtected(media.getFileId());
    }

    @Transactional
    public Long createMedia(final MediaSaveRequest request) {
        requirePrice(request);
        Media media = mediaRepository.save(Media.builder()
                .title(request.title().trim())
                .description(request.description())
                .fileId(request.fileId())
                .thumbnailFileId(requireImage(request.thumbnailFileId()))
                .mediaType(playableType(request.fileId()))
                .durationSeconds(request.durationSeconds())
                .completionRate(request.completionRateOrDefault())
                .isUsing(request.isUsingOrDefault())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .build());
        applySale(media, request);
        return media.getId();
    }

    @Transactional
    public void updateMedia(final Long id, final MediaSaveRequest request) {
        requirePrice(request);
        Media media = findMedia(id);
        media.update(request.title().trim(), request.description(), request.fileId(),
                requireImage(request.thumbnailFileId()), playableType(request.fileId()),
                request.durationSeconds(), request.completionRateOrDefault(), request.isUsingOrDefault());
        media.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
        applySale(media, request);
    }

    @Transactional
    public void deleteMedia(final Long id) {
        Media media = findMedia(id);
        media.delete();
        salesService.syncProduct(media);
    }

    private void applySale(final Media media, final MediaSaveRequest request) {
        media.changeSale(request.paidOrDefault(), request.price(), request.accessDays());
        fileService.protect(List.of(media.getFileId()));
        salesService.syncProduct(media);
    }

    private static void requirePrice(final MediaSaveRequest request) {
        if (request.paidOrDefault() && (request.price() == null || request.price().signum() <= 0)) {
            throw new BusinessException(ErrorCode.MEDIA_PRICE_REQUIRED);
        }
    }

    private void requirePurchased(final Media media, final Long memberId) {
        if (!canPlay(media, memberId, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.MEDIA_PURCHASE_REQUIRED);
        }
    }

    private boolean canPlay(final Media media, final Long memberId, final LocalDateTime now) {
        return !media.isPaid() || (memberId != null && !accessRepository.findActive(memberId, List.of(media.getId()), now).isEmpty());
    }

    @Transactional(readOnly = true)
    public Page<LearnerResponse> getLearners(final Long id, final Pageable pageable) {
        findMedia(id);
        Page<MediaEnrollment> enrollments = enrollmentRepository.findAllByMediaIdOrderByIdDesc(id, pageable);
        List<Long> memberIds = enrollments.map(MediaEnrollment::getMemberId).toList();
        Map<Long, MediaProgress> progresses = byKey(
                progressRepository.findAllByMediaIdAndMemberIdIn(id, memberIds), MediaProgress::getMemberId);
        Map<Long, String> usernames = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return enrollments.map(enrollment -> LearnerResponse.of(enrollment, progresses.get(enrollment.getMemberId()),
                usernames.getOrDefault(enrollment.getMemberId(), "(탈퇴 회원)")));
    }

    private Page<MediaResponse> toResponses(final Page<Media> mediaItems, final Long memberId) {
        return new PageImpl<>(toResponses(mediaItems.getContent(), memberId), mediaItems.getPageable(), mediaItems.getTotalElements());
    }

    private List<MediaResponse> toResponses(final List<Media> mediaItems, final Long memberId) {
        List<Long> mediaIds = mediaItems.stream().map(Media::getId).toList();
        Map<Long, MediaEnrollment> enrollments = memberId == null ? Map.of() : byKey(
                enrollmentRepository.findAllByMemberIdAndMediaIdIn(memberId, mediaIds), MediaEnrollment::getMediaId);
        Map<Long, MediaProgress> progresses = memberId == null ? Map.of() : byKey(
                progressRepository.findAllByMemberIdAndMediaIdIn(memberId, mediaIds), MediaProgress::getMediaId);
        Map<Long, FileResponse> files = filesOf(mediaItems);
        LocalDateTime now = LocalDateTime.now();
        boolean admin = CurrentUser.isAdmin();
        Map<Long, LocalDateTime> purchased = new HashMap<>();
        if (memberId != null) {
            accessRepository.findActive(memberId, mediaIds, now).forEach(access -> purchased.merge(access.getMediaId(),
                    access.getExpiresAt() == null ? LocalDateTime.MAX : access.getExpiresAt(), (a, b) -> a.isAfter(b) ? a : b));
        }
        return mediaItems.stream().map(media -> {
            LocalDateTime until = purchased.get(media.getId());
            boolean playable = admin || !media.isPaid() || until != null;
            return MediaResponse.of(media, files.get(media.getFileId()),
                    MyProgress.of(enrollments.get(media.getId()), progresses.get(media.getId())), playable,
                    until == null || until.equals(LocalDateTime.MAX) ? null : until);
        }).toList();
    }

    private Map<Long, FileResponse> filesOf(final Collection<Media> mediaItems) {
        return byKey(fileService.getFiles(mediaItems.stream().map(Media::getFileId).distinct().toList()), FileResponse::id);
    }

    private MediaEnrollment enrollmentOf(final Long mediaId, final Long memberId) {
        return enrollmentRepository.findByMediaIdAndMemberId(mediaId, memberId)
                .orElseGet(() -> enrollmentRepository.save(MediaEnrollment.start(mediaId, memberId)));
    }

    private Long requireImage(final Long fileId) {
        if (fileId != null && fileService.getFile(fileId).mediaType() != MediaType.IMAGE) {
            throw new BusinessException(ErrorCode.MEDIA_THUMBNAIL_IMAGE_REQUIRED);
        }
        return fileId;
    }

    private static Set<MediaType> typesOf(final MediaType mediaType) {
        return mediaType == null ? PLAYABLE : EnumSet.of(mediaType);
    }

    private MediaType playableType(final Long fileId) {
        MediaType type = fileService.getFile(fileId).mediaType();
        if (!PLAYABLE.contains(type)) {
            throw new BusinessException(ErrorCode.MEDIA_FILE_REQUIRED);
        }
        return type;
    }

    private static Long currentMemberId() {
        return CurrentUser.get().filter(user -> user.userType() == UserType.MEMBER).map(LoginUser::id).orElse(null);
    }

    private Media findPublic(final Long id) {
        return mediaRepository.findByIdAndIsUsingTrueAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private Media findMedia(final Long id) {
        return mediaRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static <T> Map<Long, T> byKey(final Collection<T> items, final Function<T, Long> key) {
        return items.stream().collect(Collectors.toMap(key, Function.identity(), (first, second) -> first));
    }
}
