package com.dgmoonlabs.cms.domain.business.message.dto;

import com.dgmoonlabs.cms.domain.business.message.entity.CustomerMessage;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class MessageDtos {
    private MessageDtos() {
    }

    public record MessageSaveRequest(@NotBlank @Size(max = 2000) String content, @Size(max = 10) List<Long> fileIds) {
        public List<Long> fileIdsOrEmpty() {
            return fileIds == null ? List.of() : fileIds.stream().distinct().toList();
        }
    }

    public record MessageResponse(Long id, UserType senderType, String senderName, String content, List<String> fileUrls,
                                  LocalDateTime createdAt) {
        public static MessageResponse from(final CustomerMessage message) {
            List<Long> fileIds = message.getFileIds() == null ? List.of() : message.getFileIds();
            return new MessageResponse(message.getId(), message.getSenderType(), message.getSenderName(), message.getContent(),
                    fileIds.stream().map(id -> "/api/files/" + id + "/view").toList(), message.getCreatedAt());
        }
    }
}
