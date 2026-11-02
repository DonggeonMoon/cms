package com.dgmoonlabs.cms.domain.map.entity;

import com.dgmoonlabs.cms.domain.map.constant.MapProvider;
import com.dgmoonlabs.cms.domain.map.dto.MapDtos.MapSettingRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

// a single row: map provider and keys, the two tab switches and the head office shown under "오시는 길"
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MapSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("지도 제공자")
    private MapProvider provider = MapProvider.KAKAO;

    @Comment("카카오맵 JavaScript 키")
    private String kakaoAppKey;

    @Comment("네이버 지도 Client ID (ncpKeyId)")
    private String naverClientId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("오시는 길 사용 여부")
    private boolean directionsEnabled;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("지점 안내 사용 여부")
    private boolean branchesEnabled;

    @Comment("본점 이름")
    private String officeName;

    @Comment("본점 주소")
    private String officeAddress;

    @Comment("본점 상세 주소")
    private String officeDetailAddress;

    @Column(precision = 10, scale = 7)
    @Comment("본점 위도")
    private BigDecimal officeLatitude;

    @Column(precision = 10, scale = 7)
    @Comment("본점 경도")
    private BigDecimal officeLongitude;

    @Comment("본점 연락처")
    private String officePhoneNumber;

    @Comment("본점 운영 시간")
    private String officeBusinessHours;

    @Column(columnDefinition = "text")
    @Comment("오시는 길 안내 (교통편 등)")
    private String officeDescription;

    public static MapSetting empty() {
        return MapSetting.builder().build();
    }

    public void update(final MapSettingRequest request) {
        this.provider = request.provider();
        this.kakaoAppKey = blankToNull(request.kakaoAppKey());
        this.naverClientId = blankToNull(request.naverClientId());
        this.directionsEnabled = request.directionsEnabled();
        this.branchesEnabled = request.branchesEnabled();
        this.officeName = request.officeName();
        this.officeAddress = request.officeAddress();
        this.officeDetailAddress = request.officeDetailAddress();
        this.officeLatitude = request.officeLatitude();
        this.officeLongitude = request.officeLongitude();
        this.officePhoneNumber = request.officePhoneNumber();
        this.officeBusinessHours = request.officeBusinessHours();
        this.officeDescription = request.officeDescription();
    }

    public String apiKey() {
        return provider == MapProvider.NAVER ? naverClientId : kakaoAppKey;
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
