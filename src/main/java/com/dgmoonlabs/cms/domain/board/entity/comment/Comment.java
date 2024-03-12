package com.dgmoonlabs.cms.domain.board.entity.comment;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.board.constant.SnsType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Comment extends BaseEntity {
    @Id
    private Long id;
    private UserType userType;
    private Long userId;
    private Long parentId;
    private int depth;
    private int sequence;
    private String title;
    private String content;
    private SnsType snsType;
    private String snsId;
    private String snsUsername;
    private String snsUserUrl;
    private Long profileImageFileId;
    private CommentStatus status;
    private String ipAddress;
    private int hit;
}
