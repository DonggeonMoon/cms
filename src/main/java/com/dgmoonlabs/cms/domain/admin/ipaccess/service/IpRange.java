package com.dgmoonlabs.cms.domain.admin.ipaccess.service;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

/** IP 주소 하나(203.0.113.7) 또는 CIDR 대역(203.0.113.0/24, 2001:db8::/32). */
public record IpRange(byte[] network, int prefix) {
    private static final Pattern IPV4 = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    // 호스트 이름을 넣으면 DNS 조회가 일어나므로 숫자·콜론 형식만 받는다.
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:.]+$");

    public static IpRange parse(final String text) {
        String value = text == null ? "" : text.trim();
        int slash = value.indexOf('/');
        String host = slash < 0 ? value : value.substring(0, slash);
        byte[] address = literal(host).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_IP_ADDRESS));
        int max = address.length * 8;
        int prefix = max;
        if (slash >= 0) {
            try {
                prefix = Integer.parseInt(value.substring(slash + 1));
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_IP_ADDRESS);
            }
            if (prefix < 0 || prefix > max) {
                throw new BusinessException(ErrorCode.INVALID_IP_ADDRESS);
            }
        }
        return new IpRange(mask(address, prefix), prefix);
    }

    public boolean contains(final String ip) {
        return literal(ip).filter(address -> address.length == network.length)
                .map(address -> java.util.Arrays.equals(mask(address, prefix), network))
                .orElse(false);
    }

    /** 저장할 때 쓰는 표기. 대역은 네트워크 주소로 맞춘다 (203.0.113.9/24 → 203.0.113.0/24). */
    public String canonical() {
        try {
            String host = InetAddress.getByAddress(network).getHostAddress();
            return prefix == network.length * 8 ? host : host + "/" + prefix;
        } catch (UnknownHostException e) {
            throw new BusinessException(ErrorCode.INVALID_IP_ADDRESS);
        }
    }

    private static Optional<byte[]> literal(final String host) {
        if (host == null || host.isBlank()) {
            return Optional.empty();
        }
        String value = host.trim();
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        boolean v4 = IPV4.matcher(value).matches();
        if (!v4 && !(value.contains(":") && IPV6.matcher(value).matches())) {
            return Optional.empty();
        }
        try {
            return Optional.of(InetAddress.getByName(value).getAddress());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    private static byte[] mask(final byte[] address, final int prefix) {
        byte[] masked = address.clone();
        for (int i = 0; i < masked.length; i++) {
            int bits = Math.max(0, Math.min(8, prefix - i * 8));
            masked[i] = (byte) (masked[i] & (bits == 0 ? 0 : (0xFF << (8 - bits))));
        }
        return masked;
    }
}
