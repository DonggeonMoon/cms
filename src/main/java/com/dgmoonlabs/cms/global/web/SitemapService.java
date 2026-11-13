package com.dgmoonlabs.cms.global.web;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.domain.archive.constant.ArchiveStatus;
import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveArticleRepository;
import com.dgmoonlabs.cms.domain.archive.repository.ArchiveRepository;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.Board;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardConfig;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardConfigRepository;
import com.dgmoonlabs.cms.domain.board.repository.BoardRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationItemRepository;
import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 검색엔진에 알릴 공개 페이지 목록. 꺼 둔 기능과 비공개·회원 등급 전용 항목은 뺀다. */
@Service
@RequiredArgsConstructor
public class SitemapService {
    private static final int MAX_ENTRIES = 5_000;

    private final SiteService siteService;
    private final ContentRepository contentRepository;
    private final ProductRepository productRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final CourseRepository courseRepository;
    private final StreamingRepository streamingRepository;
    private final BoardRepository boardRepository;
    private final BoardConfigRepository boardConfigRepository;
    private final BoardArticleRepository boardArticleRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchiveArticleRepository archiveArticleRepository;

    public record Entry(String path, LocalDateTime lastModified) {
    }

    @Transactional(readOnly = true)
    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("/", null));
        if (siteService.isEnabled(SiteFeature.CONTENT)) {
            contentRepository.findAllByStatusAndSiteIdAndDeletedAtIsNullOrderByIdAsc(ContentStatus.PUBLISHED, siteService.currentSiteId()).stream()
                    .filter(content -> content.getAccessGradeId() == null)
                    .forEach(content -> entries.add(new Entry("/contents/" + content.getId(), content.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.PRODUCT)) {
            productRepository.findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(product -> product.getAccessGradeId() == null && product.getStreamingId() == null)
                    .forEach(product -> entries.add(new Entry("/products/" + product.getId(), product.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.PROGRAM)) {
            reservationItemRepository.findAllByIsEnabledTrueAndSiteIdAndDeletedAtIsNullOrderByIdAsc(siteService.currentSiteId()).stream()
                    .filter(item -> item.getAccessGradeId() == null)
                    .forEach(item -> entries.add(new Entry("/reservation-items/" + item.getId(), item.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.MEDIA)) {
            courseRepository.findAllByIsUsingTrueAndSiteIdAndDeletedAtIsNullOrderByIdAsc(siteService.currentSiteId()).stream()
                    .filter(course -> course.getAccessGradeId() == null)
                    .forEach(course -> entries.add(new Entry("/courses/" + course.getId(), course.getModifiedAt())));
            streamingRepository.findAllByIsUsingTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(streaming -> streaming.getAccessGradeId() == null)
                    .forEach(streaming -> entries.add(new Entry("/streaming/" + streaming.getId(), streaming.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.BOARD)) {
            boardRepository.findAllBySiteIdAndDeletedAtIsNullOrderByIdAsc(siteService.currentSiteId())
                    .forEach(board -> addBoardEntries(entries, board));
        }
        if (siteService.isEnabled(SiteFeature.ARCHIVE)) {
            archiveRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(archive -> archive.getAccessGradeId() == null)
                    .forEach(archive -> addArchiveEntries(entries, archive));
        }
        return entries.size() > MAX_ENTRIES ? entries.subList(0, MAX_ENTRIES) : entries;
    }

    private void addBoardEntries(final List<Entry> entries, final Board board) {
        BoardConfig config = boardConfigRepository.findByBoardId(board.getId()).orElse(null);
        if (config == null || !config.isGuestAccessible() || !config.isGuestReadable() || config.getReadGradeId() != null) {
            return;
        }
        entries.add(new Entry("/boards/" + board.getId(), board.getModifiedAt()));
        boardArticleRepository.findAllByBoardIdAndIsHiddenFalseAndIsSecretFalseAndDeletedAtIsNullOrderByIdAsc(board.getId())
                .forEach(article -> entries.add(new Entry("/boards/" + board.getId() + "/articles/" + article.getId(),
                        article.getModifiedAt())));
    }

    private void addArchiveEntries(final List<Entry> entries, final Archive archive) {
        entries.add(new Entry("/archives/" + archive.getId(), archive.getModifiedAt()));
        archiveArticleRepository.findAllByArchiveIdAndStatusAndDeletedAtIsNullOrderByIdAsc(archive.getId(), ArchiveStatus.PUBLISHED)
                .forEach(article -> entries.add(new Entry("/archives/" + archive.getId() + "/articles/" + article.getId(),
                        article.getModifiedAt())));
    }
}
