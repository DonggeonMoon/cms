package com.dgmoonlabs.cms.domain.admin.statistics.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Statistics extends BaseEntity {
    @Id
    private Long id;

    @Comment("날짜")
    private LocalDate localDate;

    @Comment("방문 국가 코드")
    private String nationCode;

    @Comment("OS")
    private int os;

    @Comment("브라우저")
    private String browser;

    @Comment("URL")
    private String url;

    @Comment("방문 횟수")
    private int count;
}
