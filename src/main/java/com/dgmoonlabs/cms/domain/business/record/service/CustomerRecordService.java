package com.dgmoonlabs.cms.domain.business.record.service;

import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.CustomerRecordResponse;
import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.CustomerRecordSaveRequest;
import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.MyCustomerRecord;
import com.dgmoonlabs.cms.domain.business.record.entity.CustomerRecord;
import com.dgmoonlabs.cms.domain.business.record.repository.CustomerRecordRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerRecordService {
    private final CustomerRecordRepository recordRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<CustomerRecordResponse> search(final Long memberId, final Long reservationId, final String keyword,
                                               final Pageable pageable) {
        return recordRepository.search(memberId, reservationId, keyword == null ? "" : keyword.trim(), pageable)
                .map(CustomerRecordResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<MyCustomerRecord> getMine(final Long memberId, final Pageable pageable) {
        return recordRepository.findAllByMemberIdAndSharedWithMemberTrueAndDeletedAtIsNullOrderByRecordedOnDescIdDesc(memberId, pageable)
                .map(MyCustomerRecord::from);
    }

    @Transactional
    public CustomerRecordResponse create(final LoginUser admin, final CustomerRecordSaveRequest request) {
        validate(request);
        CustomerRecord record = recordRepository.save(CustomerRecord.builder()
                .memberId(request.memberId())
                .reservationId(request.reservationId())
                .recordedOn(request.recordedOn())
                .category(blankToNull(request.category()))
                .title(request.title().trim())
                .content(blankToNull(request.content()))
                .photoFileIds(photos(request))
                .sharedWithMember(request.sharedWithMember())
                .authorAdminId(admin.id())
                .authorName(admin.name())
                .build());
        return CustomerRecordResponse.from(record);
    }

    @Transactional
    public CustomerRecordResponse update(final Long id, final CustomerRecordSaveRequest request) {
        CustomerRecord record = recordRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
        if (!record.getMemberId().equals(request.memberId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        validate(request);
        record.update(request.reservationId(), request.recordedOn(), blankToNull(request.category()), request.title().trim(),
                blankToNull(request.content()), photos(request), request.sharedWithMember());
        return CustomerRecordResponse.from(record);
    }

    @Transactional
    public void delete(final Long id) {
        recordRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound).delete();
    }

    private void validate(final CustomerRecordSaveRequest request) {
        memberRepository.findByIdAndDeletedAtIsNull(request.memberId()).orElseThrow(BusinessException::notFound);
        if (request.reservationId() != null && reservationRepository
                .findByIdAndUserTypeAndUserId(request.reservationId(), UserType.MEMBER, request.memberId()).isEmpty()) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OF_MEMBER);
        }
        if (!request.photosOrEmpty().isEmpty()) {
            fileService.requireFilesExist(request.photosOrEmpty());
        }
    }

    private static List<Long> photos(final CustomerRecordSaveRequest request) {
        return request.photosOrEmpty().isEmpty() ? null : request.photosOrEmpty();
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
