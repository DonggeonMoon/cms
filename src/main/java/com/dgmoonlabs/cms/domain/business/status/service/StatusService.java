package com.dgmoonlabs.cms.domain.business.status.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.domain.business.status.constant.StatusDomain;
import com.dgmoonlabs.cms.domain.business.status.dto.StatusDtos.StatusResponse;
import com.dgmoonlabs.cms.domain.business.status.dto.StatusDtos.StatusSaveRequest;
import com.dgmoonlabs.cms.domain.business.status.entity.StatusDefinition;
import com.dgmoonlabs.cms.domain.business.status.repository.StatusDefinitionRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatusService implements ApplicationRunner {
    private final StatusDefinitionRepository repository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        for (StatusDomain domain : StatusDomain.values()) {
            int sequence = 0;
            for (Map.Entry<String, String> status : domain.systemStatuses().entrySet()) {
                int order = sequence++;
                if (repository.findByDomainAndCode(domain, status.getKey()).isEmpty()) {
                    repository.save(StatusDefinition.builder().domain(domain).code(status.getKey()).label(status.getValue())
                            .baseStatus(status.getKey()).system(true).sequence(order).build());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<StatusResponse> getStatuses(final StatusDomain domain) {
        return repository.findAllByDomainAndDeletedAtIsNullOrderBySystemDescSequenceAscIdAsc(domain).stream()
                .map(StatusResponse::from).toList();
    }

    @Transactional
    public StatusResponse create(final StatusDomain domain, final StatusSaveRequest request) {
        requireSystemStatus(domain, request.baseStatus());
        StatusDefinition definition = repository.save(StatusDefinition.builder()
                .domain(domain)
                .code("C_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .label(request.label().trim())
                .baseStatus(request.baseStatus())
                .system(false)
                .sequence(request.sequence() == null ? 0 : request.sequence())
                .build());
        return StatusResponse.from(definition);
    }

    @Transactional
    public StatusResponse update(final Long id, final StatusSaveRequest request) {
        StatusDefinition definition = repository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
        if (definition.isSystem()) {
            definition.rename(request.label().trim());
        } else {
            requireSystemStatus(definition.getDomain(), request.baseStatus());
            definition.change(request.label().trim(), request.baseStatus(), request.sequence() == null ? definition.getSequence() : request.sequence());
        }
        return StatusResponse.from(definition);
    }

    @Transactional
    public void delete(final Long id) {
        StatusDefinition definition = repository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
        if (definition.isSystem()) {
            throw new BusinessException(ErrorCode.SYSTEM_STATUS_UNDELETABLE);
        }
        definition.delete();
        if (definition.getDomain() == StatusDomain.ORDER) {
            orderRepository.clearCustomStatus(definition.getCode());
        } else {
            reservationRepository.clearCustomStatus(definition.getCode());
        }
    }

    @Transactional
    public void applyToOrder(final Long orderId, final String code) {
        Order order = orderRepository.findById(orderId).orElseThrow(BusinessException::notFound);
        order.applyCustomStatus(validCode(StatusDomain.ORDER, code, order.getStatus().name()));
    }

    @Transactional
    public void applyToReservation(final Long reservationId, final String code) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound);
        reservation.applyCustomStatus(validCode(StatusDomain.RESERVATION, code, reservation.getStatus().name()));
    }

    private String validCode(final StatusDomain domain, final String code, final String currentStatus) {
        if (code == null || code.isBlank()) {
            return null;
        }
        StatusDefinition definition = repository.findByDomainAndCode(domain, code)
                .filter(found -> found.getDeletedAt() == null && !found.isSystem())
                .orElseThrow(BusinessException::notFound);
        if (!definition.getBaseStatus().equals(currentStatus)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        return definition.getCode();
    }

    private static void requireSystemStatus(final StatusDomain domain, final String baseStatus) {
        if (baseStatus == null || !domain.systemStatuses().containsKey(baseStatus)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
