package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** 상품 목록의 속성 조건: attr.키=값(여러 번 가능), min.키=숫자, max.키=숫자. */
public record AttributeFilters(Map<String, List<String>> choices, Map<String, BigDecimal[]> ranges) {
    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9_-]{1,40}$");
    private static final int MAX_FILTERS = 20;

    public static AttributeFilters from(final MultiValueMap<String, String> params) {
        Map<String, List<String>> choices = new LinkedHashMap<>();
        Map<String, BigDecimal[]> ranges = new LinkedHashMap<>();
        params.forEach((name, values) -> {
            int dot = name.indexOf('.');
            if (dot < 0) {
                return;
            }
            String prefix = name.substring(0, dot);
            String key = name.substring(dot + 1);
            if (!List.of("attr", "min", "max").contains(prefix)) {
                return;
            }
            List<String> picked = values.stream().filter(value -> value != null && !value.isBlank()).map(String::trim).toList();
            if (!KEY.matcher(key).matches() || picked.isEmpty()) {
                return;
            }
            if (prefix.equals("attr")) {
                choices.put(key, picked);
                return;
            }
            BigDecimal number = parse(picked.getFirst());
            BigDecimal[] range = ranges.computeIfAbsent(key, ignored -> new BigDecimal[2]);
            range[prefix.equals("min") ? 0 : 1] = number;
        });
        if (choices.size() + ranges.size() > MAX_FILTERS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return new AttributeFilters(choices, ranges);
    }

    public static AttributeFilters none() {
        return new AttributeFilters(Map.of(), Map.of());
    }

    private static BigDecimal parse(final String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
