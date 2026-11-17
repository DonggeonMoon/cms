package com.dgmoonlabs.cms.domain.postcode.client;

import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeAddress;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSearchResponse;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** 행안부(공공데이터포털) 도로명주소 API. https://business.juso.go.kr/addrlink/addrLinkApi.do */
@Slf4j
@Component
public class HttpJusoAddressClient implements JusoAddressClient {
    private static final String ENDPOINT = "https://business.juso.go.kr/addrlink/addrLinkApi.do";

    private final RestClient restClient;

    public HttpJusoAddressClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public PostcodeSearchResponse search(final String apiKey, final String keyword, final int page, final int countPerPage) {
        try {
            JsonNode body = restClient.get()
                    .uri(ENDPOINT + "?confmKey={key}&currentPage={page}&countPerPage={size}&keyword={keyword}&resultType=json",
                            apiKey, page, countPerPage, keyword)
                    .retrieve().body(JsonNode.class);
            JsonNode results = body == null ? null : body.path("results");
            JsonNode common = results == null ? null : results.path("common");
            if (common == null || !"0".equals(text(common, "errorCode"))) {
                log.warn("Juso address search failed: {}", common == null ? null : text(common, "errorMessage"));
                throw new BusinessException(ErrorCode.POSTCODE_SEARCH_FAILED);
            }
            List<PostcodeAddress> addresses = new ArrayList<>();
            for (JsonNode juso : results.path("juso")) {
                String roadAddr = text(juso, "roadAddr");
                addresses.add(new PostcodeAddress(text(juso, "zipNo"), roadAddr, roadAddr, text(juso, "jibunAddr")));
            }
            return new PostcodeSearchResponse(addresses, (int) common.path("totalCount").asLong(0));
        } catch (RestClientException e) {
            log.warn("Juso address search failed", e);
            throw new BusinessException(ErrorCode.POSTCODE_SEARCH_FAILED, e);
        }
    }

    private static String text(final JsonNode node, final String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asString("");
    }
}
