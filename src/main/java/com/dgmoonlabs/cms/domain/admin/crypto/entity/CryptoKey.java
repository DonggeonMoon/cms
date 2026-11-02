package com.dgmoonlabs.cms.domain.admin.crypto.entity;

import com.dgmoonlabs.cms.global.crypto.CryptoAlgorithm;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CryptoKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("암호화 알고리즘")
    private CryptoAlgorithm algorithm;

    @Column(nullable = false, length = 500)
    @Comment("마스터키로 감싼 데이터 키")
    private String wrappedKey;

    @Column(columnDefinition = "text")
    @Comment("이전 데이터 키 목록 (마스터키로 감쌈, 줄바꿈 구분)")
    private String retiredWrappedKeys;

    public void replaceKey(final String wrapped) {
        this.retiredWrappedKeys = retiredWrappedKeys == null || retiredWrappedKeys.isBlank()
                ? wrappedKey
                : retiredWrappedKeys + "\n" + wrappedKey;
        this.wrappedKey = wrapped;
    }

    public List<String> getRetiredWrappedKeyList() {
        return retiredWrappedKeys == null || retiredWrappedKeys.isBlank() ? List.of() : List.of(retiredWrappedKeys.split("\n"));
    }

    public void changeAlgorithm(final CryptoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
}
