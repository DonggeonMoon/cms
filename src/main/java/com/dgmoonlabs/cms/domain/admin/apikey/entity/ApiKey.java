package com.dgmoonlabs.cms.domain.admin.apikey.entity;

import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeySaveRequest;
import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ApiKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("키 이름")
    private String name;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "`value`", nullable = false, length = EncryptedStringConverter.COLUMN_LENGTH)
    @Comment("키 값")
    private String value;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("설명")
    private String description;

    public static ApiKey from(final ApiKeySaveRequest request) {
        return ApiKey.builder()
                .name(request.name().trim())
                .value(request.value().trim())
                .description(request.descriptionOrEmpty())
                .build();
    }

    public void update(final ApiKeySaveRequest request) {
        this.name = request.name().trim();
        this.value = request.value().trim();
        this.description = request.descriptionOrEmpty();
    }
}
