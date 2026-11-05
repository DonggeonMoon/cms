package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.constant.FulfillmentType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserTypeAndUserId(Long id, UserType userType, Long userId);

    @Query("select coalesce(max(o.pickupNumber), 0) from Order o where o.fulfillmentType <> :delivery and o.createdAt >= :since")
    int maxPickupNumberSince(FulfillmentType delivery, LocalDateTime since);

    List<Order> findAllByFulfillmentTypeInAndStatusInOrderByIdAsc(Collection<FulfillmentType> types, Collection<OrderStatus> statuses);

    Page<Order> findAllByUserTypeAndUserIdOrderByIdDesc(UserType userType, Long userId, Pageable pageable);

    @Query("select o from Order o where (:status is null or o.status = :status) "
            + "and (:keyword = '' or o.orderNumber like concat('%', :keyword, '%') "
            + "     or lower(o.productName) like lower(concat('%', :keyword, '%'))) order by o.id desc")
    Page<Order> search(OrderStatus status, String keyword, Pageable pageable);

    boolean existsByUserTypeAndUserIdAndProductIdAndStatusIn(UserType userType, Long userId, Long productId, Collection<OrderStatus> statuses);

    boolean existsByOrderNumber(String orderNumber);

    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.productId = :productId "
            + "and o.saleType = com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType.DONATION "
            + "and o.status = com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus.PAID")
    BigDecimal sumPaidDonations(Long productId);

    @Query("select count(o) from Order o where o.productId = :productId "
            + "and o.saleType = com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType.DONATION "
            + "and o.status = com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus.PAID")
    long countPaidDonations(Long productId);

    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.productId = :productId "
            + "and o.saleType = com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType.FUNDING "
            + "and o.status = com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus.PAID")
    BigDecimal sumPaidFunding(Long productId);

    @Query("select count(o) from Order o where o.productId = :productId "
            + "and o.saleType = com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType.FUNDING "
            + "and o.status = com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus.PAID")
    long countPaidFunding(Long productId);

    List<Order> findAllByProductIdAndSaleTypeAndStatus(Long productId, ProductSaleType saleType, OrderStatus status);

    @Query("select o from Order o where o.orderNumber = :orderNumber and (:status is null or o.status = :status)")
    Page<Order> findByOrderNumberAndStatus(String orderNumber, OrderStatus status, Pageable pageable);

    @Query("select coalesce(sum(o.totalPrice), 0) from Order o where o.status in :statuses and o.createdAt >= :since")
    BigDecimal sumTotalPriceSince(Collection<OrderStatus> statuses, LocalDateTime since);

    long countByStatus(OrderStatus status);

    @Modifying
    @Query("update Order x set x.customStatus = null where x.customStatus = :code")
    void clearCustomStatus(String code);
}
