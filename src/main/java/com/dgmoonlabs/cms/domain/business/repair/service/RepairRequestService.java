package com.dgmoonlabs.cms.domain.business.repair.service;

import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.QuoteOrderRequest;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.RepairResponse;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.RepairSaveRequest;
import com.dgmoonlabs.cms.domain.business.repair.entity.RepairRequest;
import com.dgmoonlabs.cms.domain.business.repair.repository.RepairRequestRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepairRequestService {
    private final RepairRequestRepository repairRequestRepository;
    private final MemberRepository memberRepository;
    private final OrderService orderService;
    private final FileService fileService;

    @Transactional
    public RepairResponse create(final Long memberId, final RepairSaveRequest request) {
        if (!request.photoFileIdsOrEmpty().isEmpty()) {
            fileService.requireFilesExist(request.photoFileIdsOrEmpty());
        }
        RepairRequest repair = repairRequestRepository.save(RepairRequest.builder()
                .memberId(memberId)
                .productId(request.productId())
                .title(request.title().trim())
                .description(request.description().trim())
                .photoFileIds(request.photoFileIdsOrEmpty().isEmpty() ? null : request.photoFileIdsOrEmpty())
                .build());
        return RepairResponse.from(repair);
    }

    @Transactional(readOnly = true)
    public Page<RepairResponse> getMine(final Long memberId, final Pageable pageable) {
        return repairRequestRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(RepairResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<RepairResponse> search(final String keyword, final Pageable pageable) {
        return repairRequestRepository.search(keyword == null ? "" : keyword.trim(), pageable).map(RepairResponse::from);
    }

    @Transactional(readOnly = true)
    public RepairResponse get(final Long id) {
        return RepairResponse.from(repairRequestRepository.findById(id).orElseThrow(BusinessException::notFound));
    }

    /** 견적 상품(QUOTE)으로 회원 명의 주문을 만들어 연결한다. 금액은 그 주문에서 관리자가 견적 회신으로 정한다. */
    @Transactional
    public OrderResponse createQuoteOrder(final Long repairId, final QuoteOrderRequest request) {
        RepairRequest repair = repairRequestRepository.findById(repairId).orElseThrow(BusinessException::notFound);
        Member member = memberRepository.findByIdAndDeletedAtIsNull(repair.getMemberId()).orElseThrow(BusinessException::notFound);
        OrderResponse order = orderService.createForMember(repair.getMemberId(), new OrderCreateRequest(request.productId(), null, 1,
                null, member.getName(), phoneOf(member), null, null, null, null, null, repair.getTitle(), null, null, null, null, null));
        repair.linkQuoteOrder(order.id());
        return order;
    }

    private static String phoneOf(final Member member) {
        return member.getMobilePhoneNumber() != null ? member.getMobilePhoneNumber() : member.getTelephoneNumber();
    }
}
