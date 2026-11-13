package com.dgmoonlabs.cms.domain.admin.statistics.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves a visitor's country from their IP using the free ip-api.com lookup service (no API key required).
 * Results are cached in-memory since the same visitor IP is looked up on every page view.
 */
@Slf4j
@Component
public class HttpGeoIpClient implements GeoIpClient {
    private static final String UNKNOWN = "";
    private static final int CACHE_MAX_SIZE = 10_000;

    private final RestClient restClient;
    private final Map<String, String> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, String> eldest) {
            return size() > CACHE_MAX_SIZE;
        }
    };

    public HttpGeoIpClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String resolveCountryCode(final String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank() || isPrivateOrLocal(ipAddress)) {
            return UNKNOWN;
        }
        synchronized (cache) {
            String cached = cache.get(ipAddress);
            if (cached != null) {
                return cached;
            }
        }
        String resolved = lookup(ipAddress);
        synchronized (cache) {
            cache.put(ipAddress, resolved);
        }
        return resolved;
    }

    private String lookup(final String ipAddress) {
        try {
            JsonNode body = restClient.get()
                    .uri("http://ip-api.com/json/{ip}?fields=status,countryCode", ipAddress)
                    .retrieve().body(JsonNode.class);
            if (body == null || !"success".equals(body.path("status").asString(""))) {
                return UNKNOWN;
            }
            String countryCode = body.path("countryCode").asString("");
            return countryCode == null ? UNKNOWN : countryCode;
        } catch (RestClientException e) {
            log.debug("GeoIP lookup failed for {}: {}", ipAddress, e.getMessage());
            return UNKNOWN;
        }
    }

    private static boolean isPrivateOrLocal(final String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()
                    || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }
}
