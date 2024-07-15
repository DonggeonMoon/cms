package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.dto.UsernameCheckRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserCustomRepository {
    Page<User> find(UsernameCheckRequest usernameCheckRequest, Pageable pageable);

    List<User> find(UsernameCheckRequest usernameCheckRequest);
}
