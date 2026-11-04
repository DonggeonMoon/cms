package com.dgmoonlabs.cms.domain.business.digital.service;

import com.dgmoonlabs.cms.domain.business.digital.dto.DigitalDtos.AccessResponse;
import com.dgmoonlabs.cms.domain.business.digital.dto.DigitalDtos.DigitalFile;
import com.dgmoonlabs.cms.domain.business.digital.entity.DigitalAccess;
import com.dgmoonlabs.cms.domain.business.digital.repository.DigitalAccessRepository;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DigitalAccessService {
    private final DigitalAccessRepository accessRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;

    /** 파일 목록·제한은 결제 시점 것으로 고정해, 나중에 상품 파일이 바뀌어도 이미 산 사람의 권한은 그대로 둔다. */
    @EventListener
    public void grantOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        List<Long> productIds = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .filter(OrderItem::isActive).map(OrderItem::getProductId).distinct().toList();
        List<Long> targets = productIds.isEmpty() && order.getProductId() != null ? List.of(order.getProductId()) : productIds;
        LocalDateTime now = LocalDateTime.now();
        for (Product product : productRepository.findAllById(targets)) {
            if (!product.hasDigitalFiles() || accessRepository.existsByOrderIdAndProductId(order.getId(), product.getId())) {
                continue;
            }
            accessRepository.save(DigitalAccess.builder()
                    .memberId(order.getUserId())
                    .orderId(order.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .fileIds(product.getDigitalFileIds())
                    .downloadLimit(product.getDownloadLimit())
                    .expiresAt(product.getDownloadDays() == null ? null : now.plusDays(product.getDownloadDays()))
                    .build());
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        LocalDateTime now = LocalDateTime.now();
        accessRepository.findAllByOrderIdAndProductIdIn(event.orderId(), event.productIds()).forEach(access -> access.revoke(now));
    }

    @Transactional(readOnly = true)
    public Page<AccessResponse> getMine(final Long memberId, final Pageable pageable) {
        Page<DigitalAccess> page = accessRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        Map<Long, FileResponse> files = fileService.getFiles(page.stream().flatMap(access -> access.getFileIds().stream())
                .distinct().toList()).stream().collect(Collectors.toMap(FileResponse::id, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        return page.map(access -> AccessResponse.of(access, access.getFileIds().stream()
                .map(files::get)
                .filter(file -> file != null)
                .map(file -> new DigitalFile(file.id(), file.originalName(), file.size(),
                        "/api/digital/%d/files/%d".formatted(access.getId(), file.id())))
                .toList(), now));
    }

    @Transactional
    public DownloadableFile download(final Long memberId, final Long accessId, final Long fileId) {
        DigitalAccess access = accessRepository.findForDownload(accessId, memberId).orElseThrow(BusinessException::notFound);
        access.useDownload(fileId, LocalDateTime.now());
        return fileService.downloadProtected(fileId);
    }
}
