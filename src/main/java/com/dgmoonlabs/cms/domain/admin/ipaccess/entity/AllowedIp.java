package com.dgmoonlabs.cms.domain.admin.ipaccess.entity;

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
public class AllowedIp extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    @Comment("허용할 IP 주소 또는 대역 (CIDR)")
    private String address;

    @Column(nullable = false, length = 200)
    @ColumnDefault("''")
    @Comment("메모 (사무실, 담당자 등)")
    private String memo;

    public void update(final String address, final String memo) {
        this.address = address;
        this.memo = memo;
    }
}
