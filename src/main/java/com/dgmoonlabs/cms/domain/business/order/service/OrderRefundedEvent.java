package com.dgmoonlabs.cms.domain.business.order.service;

import java.util.List;

public record OrderRefundedEvent(Long orderId, Long memberId, List<Long> productIds) {
}
