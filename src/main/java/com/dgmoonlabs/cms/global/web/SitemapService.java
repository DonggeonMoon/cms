package com.dgmoonlabs.cms.global.web;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ProgramRepository;
import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.domain.content.repository.ContentRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
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
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;

    public record Entry(String path, LocalDateTime lastModified) {
    }

    @Transactional(readOnly = true)
    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("/", null));
        if (siteService.isEnabled(SiteFeature.CONTENT)) {
            contentRepository.findAllByStatusAndDeletedAtIsNullOrderByIdAsc(ContentStatus.PUBLISHED).stream()
                    .filter(content -> content.getAccessGradeId() == null)
                    .forEach(content -> entries.add(new Entry("/contents/" + content.getId(), content.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.PRODUCT)) {
            productRepository.findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(product -> product.getAccessGradeId() == null && product.getMediaId() == null)
                    .forEach(product -> entries.add(new Entry("/products/" + product.getId(), product.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.PROGRAM)) {
            programRepository.findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(program -> program.getAccessGradeId() == null)
                    .forEach(program -> entries.add(new Entry("/reservation-items/" + program.getId(), program.getModifiedAt())));
        }
        if (siteService.isEnabled(SiteFeature.MEDIA)) {
            courseRepository.findAllByIsUsingTrueAndDeletedAtIsNullOrderByIdAsc().stream()
                    .filter(course -> course.getAccessGradeId() == null)
                    .forEach(course -> entries.add(new Entry("/courses/" + course.getId(), course.getModifiedAt())));
        }
        return entries.size() > MAX_ENTRIES ? entries.subList(0, MAX_ENTRIES) : entries;
    }
}
