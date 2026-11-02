package com.dgmoonlabs.cms.domain.map.entity;

import com.dgmoonlabs.cms.domain.map.dto.MapDtos.BranchSaveRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Branch extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("지점명")
    private String name;

    @Column(nullable = false)
    @Comment("주소")
    private String address;

    @Comment("상세 주소")
    private String detailAddress;

    @Column(nullable = false, precision = 10, scale = 7)
    @Comment("위도")
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    @Comment("경도")
    private BigDecimal longitude;

    @Comment("연락처")
    private String phoneNumber;

    @Comment("운영 시간")
    private String businessHours;

    @Comment("안내")
    private String description;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("표시 순서")
    private int sequence;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("노출 여부")
    private boolean isUsing;

    public static Branch from(final BranchSaveRequest request) {
        Branch branch = Branch.builder().build();
        branch.update(request);
        return branch;
    }

    public void update(final BranchSaveRequest request) {
        this.name = request.name();
        this.address = request.address();
        this.detailAddress = request.detailAddress();
        this.latitude = request.latitude();
        this.longitude = request.longitude();
        this.phoneNumber = request.phoneNumber();
        this.businessHours = request.businessHours();
        this.description = request.description();
        this.sequence = request.sequence();
        this.isUsing = request.isUsing();
    }
}
