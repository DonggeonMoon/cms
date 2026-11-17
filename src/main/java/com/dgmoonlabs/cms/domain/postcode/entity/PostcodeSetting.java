package com.dgmoonlabs.cms.domain.postcode.entity;

import com.dgmoonlabs.cms.domain.postcode.constant.PostcodeProvider;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSettingRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PostcodeSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("우편번호 검색 제공자")
    private PostcodeProvider provider = PostcodeProvider.KAKAO;

    @Comment("행안부 도로명주소 API 키 (API 키 관리의 키 ID)")
    private Long jusoApiKeyId;

    public static PostcodeSetting empty() {
        return PostcodeSetting.builder().build();
    }

    public void update(final PostcodeSettingRequest request) {
        this.provider = request.provider();
        this.jusoApiKeyId = request.jusoApiKeyId();
    }

    public boolean uses(final Long apiKeyId) {
        return apiKeyId.equals(jusoApiKeyId);
    }
}
