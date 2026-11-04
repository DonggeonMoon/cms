package com.dgmoonlabs.cms.domain.admin.site.entity;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Site extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사이트 이름")
    private String name;

    @Comment("사이트 설명")
    private String description;

    @Comment("사이트 도메인")
    private String domain;

    @Comment("사이트 테마")
    private String theme;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("메뉴 종류")
    private MenuType type;

    @Comment("사이트 로케일")
    private String locale;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("기본 사이트 여부")
    private boolean isDefault;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("다크 모드 사용 (끄면 항상 라이트 모드)")
    private boolean darkModeEnabled = true;

    @Comment("파비콘 파일 ID")
    private Long faviconFileId;

    @Column(length = 100)
    @Comment("검색·공유에 쓸 제목 (없으면 사이트 이름)")
    private String seoTitle;

    @Column(length = 300)
    @Comment("메타 설명 (검색 결과·공유 미리보기 문구)")
    private String metaDescription;

    @Column(length = 300)
    @Comment("메타 키워드 (쉼표로 구분)")
    private String metaKeywords;

    @Comment("공유 미리보기 이미지(OG) 파일 ID")
    private Long ogImageFileId;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("검색엔진 노출 허용")
    private boolean searchIndexing = true;

    @Column(length = 100)
    @Comment("구글 서치 콘솔 소유 확인 코드")
    private String googleVerification;

    @Column(length = 100)
    @Comment("네이버 서치어드바이저 소유 확인 코드")
    private String naverVerification;

    @Comment("홈 화면으로 쓰는 컨텐츠 ID (없으면 기본 홈)")
    private Long homeContentId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "site_disabled_feature", joinColumns = @JoinColumn(name = "site_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "feature", columnDefinition = "varchar(50)")
    private Set<SiteFeature> disabledFeatures = new HashSet<>();

    public void update(String name, String description, String domain, String theme, MenuType type, String locale,
                       boolean isDefault, Long homeContentId, Set<SiteFeature> disabledFeatures) {
        this.name = name;
        this.description = description;
        this.domain = domain;
        this.theme = theme;
        this.type = type;
        this.locale = locale;
        this.isDefault = isDefault;
        this.homeContentId = homeContentId;
        this.disabledFeatures.clear();
        this.disabledFeatures.addAll(disabledFeatures);
    }

    public void changeDarkMode(final boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }

    public void changeSeo(final Long faviconFileId, final String seoTitle, final String metaDescription, final String metaKeywords,
                          final Long ogImageFileId, final boolean searchIndexing, final String googleVerification,
                          final String naverVerification) {
        this.faviconFileId = faviconFileId;
        this.seoTitle = seoTitle;
        this.metaDescription = metaDescription;
        this.metaKeywords = metaKeywords;
        this.ogImageFileId = ogImageFileId;
        this.searchIndexing = searchIndexing;
        this.googleVerification = googleVerification;
        this.naverVerification = naverVerification;
    }

    public void unmarkDefault() {
        this.isDefault = false;
    }

    public boolean isEnabled(final SiteFeature feature) {
        return !disabledFeatures.contains(feature);
    }

    public Set<SiteFeature> enabledFeatures() {
        Set<SiteFeature> enabled = EnumSet.allOf(SiteFeature.class);
        enabled.removeAll(disabledFeatures);
        return enabled;
    }
}
