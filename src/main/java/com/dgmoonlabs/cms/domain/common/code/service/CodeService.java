package com.dgmoonlabs.cms.domain.common.code.service;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import com.dgmoonlabs.cms.domain.common.code.repository.CodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodeService {
    private final CodeRepository codeRepository;

    @Transactional(readOnly = true)
    public Page<Code> getCode(CodeRequest code, Pageable pageable) {
        return codeRepository.find(code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Code> getCodeWithoutPaging(Code code, Pageable pageable) {
        return codeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Code getCode(long id) {
        return codeRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateCode(HttpServletRequest request) {

    }
}
