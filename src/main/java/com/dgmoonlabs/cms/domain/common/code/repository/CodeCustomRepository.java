package com.dgmoonlabs.cms.domain.common.code.repository;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CodeCustomRepository {
    Page<Code> find(CodeRequest codeRequest, Pageable pageable);

    List<Code> find(CodeRequest codeRequest);
}
