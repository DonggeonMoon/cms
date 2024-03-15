package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Order extends BaseEntity {
    @Id
    private Long id;
}
