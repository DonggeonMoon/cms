package com.dgmoonlabs.cms.domain.postcode.client;

import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSearchResponse;

public interface JusoAddressClient {
    PostcodeSearchResponse search(String apiKey, String keyword, int page, int countPerPage);
}
