package com.dgmoonlabs.cms.domain.common.social.client;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;

public interface SocialOAuthClient {
    SocialProfile fetchProfile(SocialProvider provider, SocialCredentials credentials, String code, String redirectUri,
                               String state, String codeVerifier, String nonce, String appleUser);
}
