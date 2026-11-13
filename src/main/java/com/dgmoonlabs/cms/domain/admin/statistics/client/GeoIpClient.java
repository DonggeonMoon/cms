package com.dgmoonlabs.cms.domain.admin.statistics.client;

public interface GeoIpClient {
    /**
     * @return ISO 3166-1 alpha-2 country code, or "" when the IP is private/local or lookup fails.
     */
    String resolveCountryCode(String ipAddress);
}
