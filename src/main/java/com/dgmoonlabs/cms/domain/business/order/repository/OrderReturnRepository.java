package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnStatus;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
    boolean existsByOrderIdAndStatus(Long orderId, OrderReturnStatus status);

    Page<OrderReturn> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Page<OrderReturn> findAllByStatusOrderByIdDesc(OrderReturnStatus status, Pageable pageable);

    Page<OrderReturn> findAllByOrderByIdDesc(Pageable pageable);
}
