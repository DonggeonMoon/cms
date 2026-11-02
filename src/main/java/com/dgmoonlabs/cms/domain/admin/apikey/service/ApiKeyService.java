package com.dgmoonlabs.cms.domain.admin.apikey.service;

import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeyOption;
import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeyResponse;
import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeySaveRequest;
import com.dgmoonlabs.cms.domain.admin.apikey.entity.ApiKey;
import com.dgmoonlabs.cms.domain.admin.apikey.repository.ApiKeyRepository;
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
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final List<ApiKeyUsage> usages;

    @Transactional(readOnly = true)
    public Page<ApiKeyResponse> search(final String keyword, final Pageable pageable) {
        return apiKeyRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByNameAsc(
                keyword == null ? "" : keyword, pageable).map(ApiKeyResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyOption> options() {
        return apiKeyRepository.findAllByDeletedAtIsNullOrderByNameAsc().stream().map(ApiKeyOption::from).toList();
    }

    @Transactional(readOnly = true)
    public ApiKeyResponse get(final Long id) {
        return ApiKeyResponse.from(find(id));
    }

    @Transactional
    public Long create(final ApiKeySaveRequest request) {
        if (apiKeyRepository.existsByNameAndDeletedAtIsNull(request.name().trim())) {
            throw new BusinessException(ErrorCode.DUPLICATE_API_KEY_NAME);
        }
        return apiKeyRepository.save(ApiKey.from(request)).getId();
    }

    @Transactional
    public void update(final Long id, final ApiKeySaveRequest request) {
        if (apiKeyRepository.existsByNameAndIdNotAndDeletedAtIsNull(request.name().trim(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_API_KEY_NAME);
        }
        find(id).update(request);
    }

    @Transactional
    public void delete(final Long id) {
        ApiKey apiKey = find(id);
        if (usages.stream().anyMatch(usage -> usage.isUsing(id))) {
            throw new BusinessException(ErrorCode.API_KEY_IN_USE);
        }
        apiKey.delete();
    }

    public void requireExists(final Long id) {
        if (id != null && apiKeyRepository.findByIdAndDeletedAtIsNull(id).isEmpty()) {
            throw new BusinessException(ErrorCode.API_KEY_NOT_FOUND);
        }
    }

    public String valueOf(final Long id) {
        return id == null ? null : apiKeyRepository.findByIdAndDeletedAtIsNull(id).map(ApiKey::getValue).orElse(null);
    }

    private ApiKey find(final Long id) {
        return apiKeyRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
