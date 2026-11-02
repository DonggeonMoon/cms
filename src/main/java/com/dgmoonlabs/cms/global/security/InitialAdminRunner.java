package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import com.dgmoonlabs.cms.domain.common.user.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialAdminRunner implements ApplicationRunner {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        String password = securityProperties.initialAdminPassword();
        if (password == null || password.isBlank() || adminRepository.count() > 0) {
            return;
        }
        adminRepository.save(Admin.builder()
                .username(securityProperties.initialAdminUsername())
                .password(passwordEncoder.encode(password))
                .name("관리자")
                .build());
        log.info("Created initial admin account '{}'", securityProperties.initialAdminUsername());
    }
}
