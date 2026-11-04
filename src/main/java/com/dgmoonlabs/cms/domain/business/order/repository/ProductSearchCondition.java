package com.dgmoonlabs.cms.domain.business.order.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @param choices 선택형 속성: 키별로 고른 값 중 하나라도 맞으면 된다
 * @param ranges  숫자 속성: 키별 [최소, 최대], 한쪽은 비워도 된다
 */
public record ProductSearchCondition(String keyword, List<Long> categoryIds, boolean onlyOnSale,
                                     Map<String, List<String>> choices, Map<String, BigDecimal[]> ranges) {
}
