package com.dgmoonlabs.cms.domain.business.message.entity;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/** 주문 제작·AS 같은 건 하나를 두고 회원과 관리자가 주고받는 메시지. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CustomerMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("대상")
    private ProgressTargetType targetType;

    @Column(nullable = false)
    @Comment("대상 ID")
    private Long targetId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("보낸 사람 종류")
    private UserType senderType;

    @Comment("보낸 사람 ID (회원 ID 또는 관리자 ID)")
    private Long senderId;

    @Column(nullable = false, length = 100)
    @Comment("보낸 사람 이름")
    private String senderName;

    @Column(nullable = false, columnDefinition = "text")
    @Comment("내용")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("첨부 파일 ID 목록")
    private List<Long> fileIds;
}
