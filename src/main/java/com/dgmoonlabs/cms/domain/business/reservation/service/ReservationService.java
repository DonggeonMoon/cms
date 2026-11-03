package com.dgmoonlabs.cms.domain.business.reservation.service;

import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.*;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ProgramRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.domain.business.review.repository.ReviewRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ProgramRepository programRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentService paymentService;
    private final FileService fileService;
    private final HtmlSanitizer htmlSanitizer;
    private final RegexService regexService;

    @Transactional(readOnly = true)
    public Page<ProgramResponse> getPrograms(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        LocalDateTime now = LocalDateTime.now();
        Page<Program> programs = CurrentUser.isAdmin()
                ? programRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable)
                : programRepository.findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable);
        return programs.map(program -> ProgramResponse.of(program, program.isOpenAt(now)));
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgram(final Long id) {
        Program program = findProgram(id);
        if (!program.isEnabled() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        return ProgramResponse.of(program, program.isOpenAt(LocalDateTime.now()), reviewRepository.averageProgramRating(id));
    }

    @Transactional
    public Long createProgram(final ProgramSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        return programRepository.save(Program.builder()
                .name(request.name())
                .description(request.description())
                .imageFileId(request.imageFileId())
                .price(request.price())
                .maximumPerson(request.maximumPerson())
                .availablePerson(request.maximumPerson())
                .startDateTime(request.startDateTime())
                .endDateTime(request.endDateTime())
                .applicationStartDateTime(request.applicationStartDateTime())
                .applicationEndDateTime(request.applicationEndDateTime())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .isEnabled(request.isEnabled())
                .build()).getId();
    }

    @Transactional
    public void updateProgram(final Long id, final ProgramSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        programRepository.findForUpdate(id)
                .filter(program -> program.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound)
                .update(request.name(), request.description(), request.imageFileId(), request.price(),
                        request.maximumPerson(), request.startDateTime(), request.endDateTime(),
                        request.applicationStartDateTime(), request.applicationEndDateTime(), html,
                        htmlSanitizer.toPlainText(html), request.isEnabled());
    }

    @Transactional
    public void deleteProgram(final Long id) {
        findProgram(id).delete();
    }

    @Transactional
    public ReservationResponse reserve(final Long memberId, final ReservationCreateRequest request) {
        Program program = programRepository.findForUpdate(request.programId())
                .filter(found -> found.isOpenAt(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_NOT_OPEN));
        return create(UserType.MEMBER, memberId, program, request, false);
    }

    @Transactional
    public ReservationResponse reserveAsAdmin(final Long adminId, final ReservationCreateRequest request, final boolean paid) {
        Program program = programRepository.findForUpdate(request.programId())
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        return create(UserType.ADMIN, adminId, program, request, paid);
    }

    private ReservationResponse create(final UserType userType, final Long userId, final Program program,
                                       final ReservationCreateRequest request, final boolean paid) {
        program.reserve(request.quantity());

        BigDecimal total = program.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        ReservationStatus status = paid || total.signum() == 0 ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .reservationNumber(regexService.requireNumberFormat(RegexTarget.RESERVATION_NUMBER,
                        BusinessNumbers.next("R", reservationRepository::existsByReservationNumber)))
                .userType(userType)
                .userId(userId)
                .programId(program.getId())
                .programName(program.getName())
                .quantity(request.quantity())
                .totalPrice(total)
                .status(status)
                .applicantName(request.applicantName())
                .phoneNumber(request.phoneNumber())
                .build());
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getMyReservations(final Long memberId, final Pageable pageable) {
        return reservationRepository.findAllByUserTypeAndUserIdOrderByIdDesc(UserType.MEMBER, memberId, pageable).map(ReservationResponse::from);
    }

    @Transactional
    public void cancelMyReservation(final Long memberId, final Long reservationId) {
        cancel(reservationRepository.findByIdAndUserTypeAndUserId(reservationId, UserType.MEMBER, memberId).orElseThrow(BusinessException::notFound));
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchReservations(final Long programId, final ReservationStatus status,
                                                        final String keyword, final Pageable pageable) {
        String text = keyword == null ? "" : keyword.trim();
        if (!text.isEmpty() && regexService.matches(RegexTarget.RESERVATION_NUMBER, text)) {
            return reservationRepository.findByReservationNumber(text, pageable).map(ReservationResponse::from);
        }
        return reservationRepository.search(programId, status, text, pageable).map(ReservationResponse::from);
    }

    @Transactional
    public void confirmReservation(final Long reservationId) {
        reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound).confirm();
    }

    @Transactional
    public void cancelReservation(final Long reservationId) {
        cancel(reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound));
    }

    private void cancel(final Reservation reservation) {
        reservation.cancel();
        programRepository.findForUpdate(reservation.getProgramId()).ifPresent(program -> program.release(reservation.getQuantity()));
        paymentService.refundIfPaid(PaymentTargetType.RESERVATION, reservation.getId());
    }

    private void validate(final ProgramSaveRequest request) {
        if (!request.hasValidPeriods()) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        if (request.imageFileId() != null) {
            fileService.requireFilesExist(List.of(request.imageFileId()));
        }
    }

    private Program findProgram(final Long id) {
        return programRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
