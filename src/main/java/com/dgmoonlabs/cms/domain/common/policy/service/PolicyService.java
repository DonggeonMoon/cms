package com.dgmoonlabs.cms.domain.common.policy.service;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.policy.dto.PolicyDtos.PolicyResponse;
import com.dgmoonlabs.cms.domain.common.policy.dto.PolicyDtos.PolicySaveRequest;
import com.dgmoonlabs.cms.domain.common.policy.entity.Policy;
import com.dgmoonlabs.cms.domain.common.policy.repository.PolicyRepository;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(final PolicyType type) {
        return policyRepository.findByType(type).map(PolicyResponse::from).orElseGet(() -> PolicyResponse.template(type));
    }

    @Transactional
    public PolicyResponse savePolicy(final PolicyType type, final PolicySaveRequest request) {
        String html = htmlSanitizer.sanitize(request.contentHtml());
        String worker = CurrentUser.get().map(LoginUser::username).orElse(null);
        Policy policy = policyRepository.findByType(type)
                .orElseGet(() -> policyRepository.save(Policy.builder().type(type).contentHtml("").version(0).build()));
        policy.update(html, worker);
        return PolicyResponse.from(policy);
    }
}
