package com.dgmoonlabs.cms.domain.common.code.service;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.dto.CodeResponse;
import com.dgmoonlabs.cms.domain.common.code.dto.CodeSaveRequest;
import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import com.dgmoonlabs.cms.domain.common.code.repository.CodeRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public Page<CodeResponse> getCodes(final CodeRequest request, final Pageable pageable) {
        return codeRepository.find(request, pageable).map(CodeResponse::from);
    }

    @Transactional(readOnly = true)
    public List<CodeResponse> getUsingChildren(final Long parentId) {
        return codeRepository.find(CodeRequest.builder()
                        .parentId(parentId)
                        .rootOnly(parentId == null)
                        .isUsing(true)
                        .build())
                .stream()
                .map(CodeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CodeResponse getCode(final Long id) {
        return CodeResponse.from(findCode(id));
    }

    @Transactional
    public Long createCode(final CodeSaveRequest request) {
        int depth = request.parentId() == null ? 0 : findCode(request.parentId()).getDepth() + 1;
        Code code = Code.builder()
                .parentId(request.parentId())
                .depth(depth)
                .order(request.order())
                .name(request.name())
                .englishName(request.englishName())
                .description(request.description())
                .isUsing(request.isUsing())
                .build();
        return codeRepository.save(code).getId();
    }

    @Transactional
    public void updateCode(final Long id, final CodeSaveRequest request) {
        findCode(id).update(request);
    }

    @Transactional
    public void deleteCode(final Long id) {
        Code code = findCode(id);
        if (codeRepository.existsByParentIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(ErrorCode.CODE_HAS_CHILDREN);
        }
        code.delete();
    }

    private Code findCode(final Long id) {
        return codeRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
