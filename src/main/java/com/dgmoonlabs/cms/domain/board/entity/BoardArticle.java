package com.dgmoonlabs.cms.domain.board.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
public class BoardArticle extends BaseEntity {
    @Id
    private Long id;
    @Column
    private String title;
    @Lob
    private String content;
    @Lob
    private String contentHtml;
    @ColumnDefault("0")
    private boolean isNotice;
    private LocalDateTime noticeStartDateTime;
    private LocalDateTime noticeEndDateTime;
    private Long memberId;
    @ColumnDefault("0")
    private boolean isSecret;
    private String secretPassword;
    @ColumnDefault("0")
    private int hit;
    private int likes;
    private String ipAddress;
}
