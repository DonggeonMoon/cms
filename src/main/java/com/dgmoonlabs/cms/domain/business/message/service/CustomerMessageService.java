package com.dgmoonlabs.cms.domain.business.message.service;

import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageResponse;
import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageSaveRequest;
import com.dgmoonlabs.cms.domain.business.message.entity.CustomerMessage;
import com.dgmoonlabs.cms.domain.business.message.repository.CustomerMessageRepository;
import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerMessageService {
    private final CustomerMessageRepository messageRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<MessageResponse> list(final ProgressTargetType targetType, final Long targetId) {
        return messageRepository.findAllByTargetTypeAndTargetIdOrderByIdAsc(targetType, targetId).stream()
                .map(MessageResponse::from).toList();
    }

    @Transactional
    public MessageResponse post(final ProgressTargetType targetType, final Long targetId, final UserType senderType,
                                final Long senderId, final String senderName, final MessageSaveRequest request) {
        List<Long> fileIds = request.fileIdsOrEmpty();
        if (!fileIds.isEmpty()) {
            fileService.requireFilesExist(fileIds);
        }
        CustomerMessage message = messageRepository.save(CustomerMessage.builder()
                .targetType(targetType)
                .targetId(targetId)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .content(request.content().trim())
                .fileIds(fileIds.isEmpty() ? null : fileIds)
                .build());
        return MessageResponse.from(message);
    }
}
