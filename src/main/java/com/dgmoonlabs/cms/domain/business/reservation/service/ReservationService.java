package com.dgmoonlabs.cms.domain.business.reservation.service;

import com.dgmoonlabs.cms.domain.business.BusinessNumbers;
import com.dgmoonlabs.cms.domain.business.pass.entity.MemberPass;
import com.dgmoonlabs.cms.domain.business.pass.service.PassService;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.service.PaymentService;
import com.dgmoonlabs.cms.domain.business.reservation.constant.QueueStatus;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.dto.ReservationDtos.*;
import com.dgmoonlabs.cms.domain.business.reservation.entity.Reservation;
import com.dgmoonlabs.cms.domain.business.reservation.entity.ReservationExtension;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import com.dgmoonlabs.cms.domain.business.reservation.entity.program.ProgramSession;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ProgramRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ProgramSessionRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationExtensionRepository;
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
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.domain.business.reservation.layout.SpaceLayout;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    private final JsonMapper jsonMapper;
    private final GradeService gradeService;
    private final ProgramSessionRepository sessionRepository;
    private final ReservationExtensionRepository extensionRepository;
    private final PassService passService;

    @Transactional(readOnly = true)
    public Page<ProgramResponse> getPrograms(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        LocalDateTime now = LocalDateTime.now();
        Page<Program> programs = CurrentUser.isAdmin()
                ? programRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable)
                : programRepository.findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable);
        return programs.map(program -> ProgramResponse.of(program, null, program.isOpenAt(now), null));
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgram(final Long id) {
        Program program = findProgram(id);
        if (!program.isEnabled() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        LocalDateTime now = LocalDateTime.now();
        return ProgramResponse.of(program, layoutOf(program), program.isOpenAt(now),
                reviewRepository.averageProgramRating(id), sessionsOf(program).stream()
                        .map(session -> SessionResponse.of(session, now)).toList(), priceTiersOf(program));
    }

    private List<ProgramSession> sessionsOf(final Program program) {
        return program.supportsSessions()
                ? sessionRepository.findAllByProgramIdAndDeletedAtIsNullOrderByStartAtAsc(program.getId()) : List.of();
    }

    @Transactional
    public SessionResponse createSession(final Long programId, final SessionSaveRequest request) {
        Program program = findProgram(programId);
        if (!program.supportsSessions()) {
            throw new BusinessException(ErrorCode.SESSIONS_NOT_SUPPORTED);
        }
        requireOrdered(request);
        ProgramSession session = sessionRepository.save(ProgramSession.builder()
                .programId(program.getId())
                .name(blankToNull(request.name()))
                .startAt(request.startAt())
                .endAt(request.endAt())
                .capacity(request.capacity())
                .build());
        return SessionResponse.of(session, LocalDateTime.now());
    }

    @Transactional
    public SessionResponse updateSession(final Long programId, final Long sessionId, final SessionSaveRequest request) {
        requireOrdered(request);
        ProgramSession session = lockedSession(programId, sessionId);
        session.update(blankToNull(request.name()), request.startAt(), request.endAt(), request.capacity());
        return SessionResponse.of(session, LocalDateTime.now());
    }

    @Transactional
    public void deleteSession(final Long programId, final Long sessionId) {
        ProgramSession session = lockedSession(programId, sessionId);
        if (reservationRepository.existsBySessionIdAndStatusNot(session.getId(), ReservationStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.SESSION_HAS_RESERVATIONS);
        }
        session.delete();
    }

    private ProgramSession lockedSession(final Long programId, final Long sessionId) {
        return sessionRepository.findForUpdate(sessionId)
                .filter(session -> session.getProgramId().equals(programId) && session.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
    }

    private static void requireOrdered(final SessionSaveRequest request) {
        if (request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
    }

    /** 회차가 있는 상품은 회차를 꼭 골라야 한다. 회차를 잠가 두고 정원을 확인한다. */
    private Optional<ProgramSession> sessionFor(final Program program, final Long sessionId) {
        if (!program.supportsSessions() || !sessionRepository.existsByProgramIdAndDeletedAtIsNull(program.getId())) {
            return Optional.empty();
        }
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.SESSION_REQUIRED);
        }
        return Optional.of(sessionRepository.findForUpdate(sessionId)
                .filter(session -> session.getProgramId().equals(program.getId()) && session.isOpenAt(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_OPEN)));
    }

    @Transactional
    public Long createProgram(final ProgramSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Program program = programRepository.save(Program.builder()
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
                .build());
        applySettings(program, request);
        program.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
        return program.getId();
    }

    @Transactional
    public void updateProgram(final Long id, final ProgramSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Program program = programRepository.findForUpdate(id)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        program.update(request.name(), request.description(), request.imageFileId(), request.price(),
                        request.maximumPerson(), request.startDateTime(), request.endDateTime(),
                        request.applicationStartDateTime(), request.applicationEndDateTime(), html,
                        htmlSanitizer.toPlainText(html), request.isEnabled());
        applySettings(program, request);
        program.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
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
        gradeService.require(program.getAccessGradeId());
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
        Optional<ProgramSession> session = sessionFor(program, request.sessionId());
        Booking booking = switch (program.getReservationType()) {
            case PROGRAM -> {
                if (session.isPresent()) {
                    session.get().reserve(request.quantity());
                } else {
                    program.reserve(request.quantity());
                }
                yield new Booking(request.quantity(), program.getPrice().multiply(BigDecimal.valueOf(request.quantity())),
                        session.map(ProgramSession::getStartAt).orElse(null), session.map(ProgramSession::getEndAt).orElse(null), null);
            }
            case EVENT -> {
                if (session.isEmpty()) {
                    yield bookSpace(program, request.cellsOrEmpty(), new Period(program.getStartDateTime(),
                            program.getEndDateTime() != null ? program.getEndDateTime() : program.getStartDateTime().plusMinutes(1), 1));
                }
                ProgramSession show = session.get();
                Booking seats = bookSpace(program, request.cellsOrEmpty(), sessionTakenCells(show.getId()),
                        new Period(show.getStartAt(), show.endOrStart(), 1));
                show.reserve(seats.quantity());
                yield seats;
            }
            case CONSULTATION -> bookConsultation(program, request.startAt());
            case VISIT -> {
                requireVisitAddress(request);
                yield bookConsultation(program, request.startAt());
            }
            case RENTAL -> bookSpace(program, request.cellsOrEmpty(), rentalPeriod(program, request.startAt(), request.endAt()));
            case TABLE -> bookTable(program, request.cellsOrEmpty(), rentalPeriod(program, request.startAt(), request.endAt()),
                    request.partySize());
            case LODGING -> bookSpace(program, request.cellsOrEmpty(), lodgingPeriod(program, request.startAt(), request.endAt()));
            case ITEM_RENTAL -> bookItems(program, request.quantity(), request.startAt(), request.endAt());
            case TOUR -> bookTour(program, session, request.partyCounts());
        };
        MemberPass usedPass = request.passId() == null ? null
                : passService.consumeForMember(userId, request.passId(), program.getId(), booking.quantity());
        BigDecimal totalPrice = usedPass == null ? booking.total() : BigDecimal.ZERO;
        ReservationStatus status = paid || totalPrice.signum() == 0 ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .reservationNumber(regexService.requireNumberFormat(RegexTarget.RESERVATION_NUMBER,
                        BusinessNumbers.next("R", reservationRepository::existsByReservationNumber)))
                .checkInCode(BusinessNumbers.next("CHK", reservationRepository::existsByCheckInCode))
                .userType(userType)
                .userId(userId)
                .programId(program.getId())
                .programName(program.getName())
                .reservationType(program.getReservationType())
                .startAt(booking.startAt())
                .endAt(booking.endAt())
                .cells(booking.cells())
                .quantity(booking.quantity())
                .totalPrice(totalPrice)
                .depositAmount(booking.deposit())
                .status(status)
                .applicantName(request.applicantName())
                .phoneNumber(request.phoneNumber())
                .visitPostalCode(program.getReservationType() == ReservationType.VISIT ? blankToNull(request.visitPostalCode()) : null)
                .visitAddress(program.getReservationType() == ReservationType.VISIT ? request.visitAddress().trim() : null)
                .visitDetailAddress(program.getReservationType() == ReservationType.VISIT ? blankToNull(request.visitDetailAddress()) : null)
                .requestMemo(blankToNull(request.requestMemo()))
                .sessionId(session.map(ProgramSession::getId).orElse(null))
                .passId(usedPass == null ? null : usedPass.getId())
                .lateFeePerDay(program.getReservationType() == ReservationType.ITEM_RENTAL ? program.getLateFeePerDay() : null)
                .partySize(program.getReservationType() == ReservationType.TABLE ? request.partySize() : null)
                .partyBreakdown(program.getReservationType() == ReservationType.TOUR ? request.partyCounts() : null)
                .build());
        return ReservationResponse.from(reservation);
    }

    private record Booking(int quantity, BigDecimal total, LocalDateTime startAt, LocalDateTime endAt, String cells,
                           BigDecimal deposit) {
        Booking(final int quantity, final BigDecimal total, final LocalDateTime startAt, final LocalDateTime endAt,
                final String cells) {
            this(quantity, total, startAt, endAt, cells, null);
        }
    }

    // units: 이용 단위 수 (대관은 시간 단위 수, 숙박은 박 수)
    private record Period(LocalDateTime from, LocalDateTime to, int units) {
    }

    private Booking bookConsultation(final Program program, final LocalDateTime startAt) {
        Slot slot = slotsOf(program, startAt == null ? null : startAt.toLocalDate()).stream()
                .filter(candidate -> candidate.startAt().equals(startAt))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESERVATION_TIME));
        if (slot.remaining() <= 0) {
            throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
        }
        return new Booking(1, program.getPrice(), slot.startAt(), slot.endAt(), null);
    }

    private Booking bookSpace(final Program program, final List<String> keys, final Period period) {
        return bookSpace(program, keys, takenCells(program.getId(), period.from(), period.to()), period);
    }

    private Booking bookSpace(final Program program, final List<String> keys, final Set<String> taken, final Period period) {
        List<SpaceLayout.Cell> cells = layoutOf(program).bookable(keys);
        if (cells.stream().anyMatch(cell -> taken.contains(cell.key()))) {
            throw new BusinessException(ErrorCode.CELL_ALREADY_RESERVED);
        }
        BigDecimal perUnit = cells.stream()
                .map(cell -> cell.price() != null ? cell.price() : program.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String joined = String.join(",", cells.stream().map(SpaceLayout.Cell::key).toList());
        return new Booking(cells.size(), perUnit.multiply(BigDecimal.valueOf(period.units())), period.from(), period.to(), joined);
    }

    private Booking bookTable(final Program program, final List<String> keys, final Period period, final Integer partySize) {
        if (partySize == null || partySize <= 0) {
            throw new BusinessException(ErrorCode.PARTY_SIZE_REQUIRED);
        }
        List<SpaceLayout.Cell> cells = layoutOf(program).bookable(keys);
        int capacity = cells.stream().mapToInt(cell -> cell.capacity() == null ? Integer.MAX_VALUE : cell.capacity()).sum();
        if (capacity < partySize) {
            throw new BusinessException(ErrorCode.TABLE_CAPACITY_EXCEEDED);
        }
        return bookSpace(program, keys, period);
    }

    /** 인원 유형(성인·아동 등)별 인원 × 요금을 더해 총액을 낸다. 출발일 회차가 있으면 전체 인원만큼 정원을 잡는다. */
    private Booking bookTour(final Program program, final Optional<ProgramSession> session, final Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            throw new BusinessException(ErrorCode.PARTY_BREAKDOWN_REQUIRED);
        }
        Map<String, BigDecimal> priceByTier = priceTiersOf(program).stream()
                .collect(Collectors.toMap(PriceTier::name, PriceTier::price));
        int quantity = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            BigDecimal unitPrice = priceByTier.get(entry.getKey());
            if (unitPrice == null || entry.getValue() == null || entry.getValue() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_PARTY_BREAKDOWN);
            }
            quantity += entry.getValue();
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(entry.getValue())));
        }
        final int totalQuantity = quantity;
        session.ifPresent(show -> show.reserve(totalQuantity));
        return new Booking(totalQuantity, total, session.map(ProgramSession::getStartAt).orElse(null),
                session.map(ProgramSession::getEndAt).orElse(null), null);
    }

    private List<PriceTier> priceTiersOf(final Program program) {
        return program.getPriceTiers() == null ? List.of() : List.of(jsonMapper.readValue(program.getPriceTiers(), PriceTier[].class));
    }

    /** 대여일부터 반납일 전날까지를 대여 일수로 센다. 금액은 1일 대여료 × 일수 × 수량에 보증금을 더한다. */
    private Booking bookItems(final Program program, final int quantity, final LocalDateTime startAt, final LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        LocalDate from = startAt.toLocalDate();
        LocalDate to = endAt.toLocalDate();
        int days = (int) ChronoUnit.DAYS.between(from, to);
        if (days <= 0 || from.isBefore(LocalDate.now()) || !withinPeriod(program, from) || !withinPeriod(program, to.minusDays(1))
                || (program.getMaxRentalDays() != null && days > program.getMaxRentalDays())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        if (remainingItems(program, from, to) < quantity) {
            throw new BusinessException(ErrorCode.RENTAL_OUT_OF_STOCK);
        }
        BigDecimal deposit = (program.getDepositAmount() == null ? BigDecimal.ZERO : program.getDepositAmount())
                .multiply(BigDecimal.valueOf(quantity));
        BigDecimal fee = program.getPrice().multiply(BigDecimal.valueOf((long) days * quantity));
        return new Booking(quantity, fee.add(deposit), from.atStartOfDay(), to.atStartOfDay(), null, deposit);
    }

    private int remainingItems(final Program program, final LocalDate from, final LocalDate to) {
        return remainingItems(program, from, to, null);
    }

    /** excludeId: 연장할 예약 자신은 빼고 센다. */
    private int remainingItems(final Program program, final LocalDate from, final LocalDate to, final Long excludeId) {
        int stock = program.getMaximumPerson() == null ? 0 : program.getMaximumPerson();
        List<Reservation> active = activeReservations(program.getId()).stream()
                .filter(reservation -> !reservation.getId().equals(excludeId)).toList();
        int remaining = stock;
        for (LocalDate day = from; day.isBefore(to); day = day.plusDays(1)) {
            LocalDate date = day;
            int held = active.stream().filter(reservation -> reservation.holdsItemOn(date)).mapToInt(Reservation::getQuantity).sum();
            remaining = Math.min(remaining, stock - held);
        }
        return Math.max(0, remaining);
    }

    @Transactional
    public ReservationResponse assignWorker(final Long reservationId, final WorkerRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound);
        reservation.assignWorker(request.workerName().trim(), blankToNull(request.workerPhone()), LocalDateTime.now());
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse completeWork(final Long reservationId, final WorkCompleteRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound);
        List<Long> photos = request.photoFileIds() == null ? List.of() : request.photoFileIds().stream().distinct().toList();
        if (!photos.isEmpty()) {
            fileService.requireFilesExist(photos);
        }
        reservation.completeWork(blankToNull(request.report()), photos, LocalDateTime.now());
        return ReservationResponse.from(reservation);
    }

    private static void requireVisitAddress(final ReservationCreateRequest request) {
        if (request.visitAddress() == null || request.visitAddress().isBlank()) {
            throw new BusinessException(ErrorCode.VISIT_ADDRESS_REQUIRED);
        }
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public ReservationResponse returnItems(final Long reservationId, final ReturnRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound);
        reservation.requireOpenRental();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal deposit = reservation.getDepositAmount() == null ? BigDecimal.ZERO : reservation.getDepositAmount();
        BigDecimal deduction = request.deduction() == null ? BigDecimal.ZERO : request.deduction();
        if (deduction.compareTo(deposit) > 0) {
            throw new BusinessException(ErrorCode.INVALID_DEPOSIT_DEDUCTION);
        }
        BigDecimal lateFee = reservation.lateFeeAt(now);
        BigDecimal unpaidExtension = BigDecimal.ZERO;
        for (ReservationExtension extension : extensionRepository.findAllByReservationIdOrderByIdAsc(reservation.getId())) {
            if (extension.getStatus() == ReservationExtension.Status.PENDING_PAYMENT) {
                unpaidExtension = unpaidExtension.add(extension.getFee());
                extension.deductFromDeposit();
            }
        }
        // 파손 차감액, 연체료, 결제하지 않은 연장료 순으로 보증금에서 빼고, 모자라면 따로 받을 금액으로 남긴다.
        BigDecimal owed = deduction.add(lateFee).add(unpaidExtension);
        BigDecimal refund = deposit.subtract(owed).max(BigDecimal.ZERO);
        BigDecimal outstanding = owed.subtract(deposit).max(BigDecimal.ZERO);
        String memo = request.memo() == null || request.memo().isBlank() ? null : request.memo().trim();
        reservation.markReturned(now, refund, memo, lateFee, unpaidExtension, outstanding);
        paymentService.refundPartially(PaymentTargetType.RESERVATION, reservation.getId(), refund);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse extendMine(final Long memberId, final Long reservationId, final ExtendRequest request) {
        Reservation reservation = reservationRepository.findByIdAndUserTypeAndUserId(reservationId, UserType.MEMBER, memberId)
                .orElseThrow(BusinessException::notFound);
        return extend(reservation, request.endAt(), false);
    }

    @Transactional
    public ReservationResponse extend(final Long reservationId, final ExtendRequest request) {
        return extend(reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound), request.endAt(), request.paid());
    }

    /** 늘린 날만큼 1일 대여료 × 수량을 받는다. 늘린 기간에 남는 수량이 있어야 한다. */
    private ReservationResponse extend(final Reservation reservation, final LocalDateTime endAt, final boolean paid) {
        reservation.requireOpenRental();
        Program program = programRepository.findForUpdate(reservation.getProgramId()).orElseThrow(BusinessException::notFound);
        LocalDate currentEnd = reservation.getEndAt().toLocalDate();
        LocalDate newEnd = endAt.toLocalDate();
        int days = (int) ChronoUnit.DAYS.between(currentEnd, newEnd);
        long totalDays = ChronoUnit.DAYS.between(reservation.getStartAt().toLocalDate(), newEnd);
        if (days <= 0 || !withinPeriod(program, newEnd.minusDays(1))
                || (program.getMaxRentalDays() != null && totalDays > program.getMaxRentalDays())) {
            throw new BusinessException(ErrorCode.INVALID_EXTENSION);
        }
        if (reservation.getPendingExtensionId() != null) {
            throw new BusinessException(ErrorCode.EXTENSION_PAYMENT_PENDING);
        }
        if (remainingItems(program, currentEnd, newEnd, reservation.getId()) < reservation.getQuantity()) {
            throw new BusinessException(ErrorCode.RENTAL_OUT_OF_STOCK);
        }
        BigDecimal fee = program.getPrice().multiply(BigDecimal.valueOf((long) days * reservation.getQuantity()));
        boolean settled = paid || fee.signum() == 0;
        ReservationExtension extension = extensionRepository.save(ReservationExtension.builder()
                .reservationId(reservation.getId())
                .previousEndAt(reservation.getEndAt())
                .newEndAt(newEnd.atStartOfDay())
                .days(days)
                .fee(fee)
                .status(settled ? ReservationExtension.Status.PAID : ReservationExtension.Status.PENDING_PAYMENT)
                .build());
        reservation.extendTo(newEnd.atStartOfDay(), extension.getId(), settled ? null : fee);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancelMyExtension(final Long memberId, final Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUserTypeAndUserId(reservationId, UserType.MEMBER, memberId)
                .orElseThrow(BusinessException::notFound);
        return cancelExtension(reservation);
    }

    @Transactional
    public ReservationResponse cancelExtension(final Long reservationId) {
        return cancelExtension(reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound));
    }

    private ReservationResponse cancelExtension(final Reservation reservation) {
        if (reservation.getPendingExtensionId() == null) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        ReservationExtension extension = extensionRepository.findById(reservation.getPendingExtensionId())
                .orElseThrow(BusinessException::notFound);
        extension.cancel();
        reservation.revertExtension(extension.getId(), extension.getPreviousEndAt());
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ExtensionResponse> getExtensions(final Long reservationId) {
        return extensionRepository.findAllByReservationIdOrderByIdAsc(reservationId).stream().map(ExtensionResponse::from).toList();
    }

    private Period rentalPeriod(final Program program, final LocalDateTime startAt, final LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt) || !startAt.toLocalDate().equals(endAt.toLocalDate())
                || !withinPeriod(program, startAt.toLocalDate()) || !onSlotBoundary(program, startAt) || !onSlotBoundary(program, endAt)) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        long minutes = Duration.between(startAt, endAt).toMinutes();
        return new Period(startAt, endAt, (int) (minutes / program.getSlotMinutes()));
    }

    private Period lodgingPeriod(final Program program, final LocalDateTime checkIn, final LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        LocalDate in = checkIn.toLocalDate();
        LocalDate out = checkOut.toLocalDate();
        if (!out.isAfter(in) || !withinPeriod(program, in) || !withinPeriod(program, out.minusDays(1))) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        return new Period(in.atStartOfDay(), out.atStartOfDay(), (int) ChronoUnit.DAYS.between(in, out));
    }

    private boolean withinPeriod(final Program program, final LocalDate date) {
        return (program.getStartDateTime() == null || !date.isBefore(program.getStartDateTime().toLocalDate()))
                && (program.getEndDateTime() == null || !date.isAfter(program.getEndDateTime().toLocalDate()));
    }

    private static boolean onSlotBoundary(final Program program, final LocalDateTime at) {
        LocalTime time = at.toLocalTime();
        if (time.isBefore(program.getOpenTime()) || time.isAfter(program.getCloseTime())) {
            return false;
        }
        return Duration.between(program.getOpenTime(), time).toMinutes() % program.getSlotMinutes() == 0;
    }

    private List<Slot> slotsOf(final Program program, final LocalDate date) {
        if (date == null || program.getSlotMinutes() == null || program.getOpenTime() == null || program.getCloseTime() == null
                || !withinPeriod(program, date)) {
            return List.of();
        }
        int capacity = program.getMaximumPerson() == null ? 1 : program.getMaximumPerson();
        Map<LocalDateTime, Long> booked = new HashMap<>();
        for (Reservation reservation : activeReservations(program.getId())) {
            if (reservation.getStartAt() != null) {
                booked.merge(reservation.getStartAt(), 1L, Long::sum);
            }
        }
        List<Slot> slots = new ArrayList<>();
        LocalDateTime cursor = date.atTime(program.getOpenTime());
        LocalDateTime close = date.atTime(program.getCloseTime());
        while (!cursor.plusMinutes(program.getSlotMinutes()).isAfter(close)) {
            LocalDateTime end = cursor.plusMinutes(program.getSlotMinutes());
            slots.add(new Slot(cursor, end, (int) Math.max(0, capacity - booked.getOrDefault(cursor, 0L))));
            cursor = end;
        }
        return slots;
    }

    private Set<String> takenCells(final Long programId, final LocalDateTime from, final LocalDateTime to) {
        Set<String> taken = new HashSet<>();
        for (Reservation reservation : activeReservations(programId)) {
            if (from == null || to == null || reservation.overlaps(from, to)) {
                taken.addAll(reservation.cellKeys());
            }
        }
        return taken;
    }

    private Set<String> sessionTakenCells(final Long sessionId) {
        Set<String> taken = new HashSet<>();
        reservationRepository.findAllBySessionIdAndStatusNot(sessionId, ReservationStatus.CANCELED)
                .forEach(reservation -> taken.addAll(reservation.cellKeys()));
        return taken;
    }

    private List<Reservation> activeReservations(final Long programId) {
        return reservationRepository.findAllByProgramIdAndStatusNot(programId, ReservationStatus.CANCELED);
    }

    @Transactional(readOnly = true)
    public Availability availability(final Long programId, final LocalDate date, final LocalDateTime from, final LocalDateTime to,
                                     final Long sessionId) {
        Program program = findProgram(programId);
        if (sessionId != null && program.getReservationType() == ReservationType.EVENT) {
            sessionRepository.findByIdAndProgramIdAndDeletedAtIsNull(sessionId, programId).orElseThrow(BusinessException::notFound);
            return new Availability(sorted(sessionTakenCells(sessionId)), List.of());
        }
        return switch (program.getReservationType()) {
            case PROGRAM -> new Availability(List.of(), List.of());
            case CONSULTATION, VISIT -> new Availability(List.of(), slotsOf(program, date));
            case RENTAL, TABLE -> new Availability(from == null || to == null ? List.of() : sorted(takenCells(programId, from, to)),
                    slotsOf(program, date));
            case LODGING -> new Availability(from == null || to == null ? List.of()
                    : sorted(takenCells(programId, from.toLocalDate().atStartOfDay(), to.toLocalDate().atStartOfDay())), List.of());
            case EVENT -> new Availability(sorted(takenCells(programId, null, null)), List.of());
            case ITEM_RENTAL -> new Availability(List.of(), List.of(), from == null || to == null || !to.toLocalDate().isAfter(from.toLocalDate())
                    ? null : remainingItems(program, from.toLocalDate(), to.toLocalDate()));
            case TOUR -> new Availability(List.of(), List.of());
        };
    }

    private static List<String> sorted(final Set<String> keys) {
        return keys.stream().sorted().toList();
    }

    private void applySettings(final Program program, final ProgramSaveRequest request) {
        ReservationType type = request.reservationTypeOrDefault();
        String layout = null;
        if (type.usesLayout()) {
            if (request.layout() == null || request.layout().cells().stream().noneMatch(cell -> cell.kind().isBookable())) {
                throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
            }
            request.layout().validate();
            layout = jsonMapper.writeValueAsString(request.layout());
        }
        boolean timed = type.usesSlots();
        if (timed && (request.slotMinutes() == null || request.openTime() == null || request.closeTime() == null
                || !request.closeTime().isAfter(request.openTime()) || request.startDateTime() == null || request.endDateTime() == null)) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        if ((type == ReservationType.LODGING && (request.startDateTime() == null || request.endDateTime() == null))
                || (type == ReservationType.EVENT && request.startDateTime() == null)) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        if (type == ReservationType.ITEM_RENTAL && request.maximumPerson() == null) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        if (type == ReservationType.TOUR && (request.priceTiers() == null || request.priceTiers().isEmpty())) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        program.changeReservationSettings(type, request.slotMinutes(), request.openTime(), request.closeTime(), layout);
        program.changeRentalSettings(request.depositAmount(), request.maxRentalDays(), request.lateFeePerDay());
        program.changeQueueSetting(request.supportsQueue());
        program.changeTourSettings(type == ReservationType.TOUR ? jsonMapper.writeValueAsString(request.priceTiers()) : null);
    }

    private SpaceLayout layoutOf(final Program program) {
        return program.getLayout() == null ? null : jsonMapper.readValue(program.getLayout(), SpaceLayout.class);
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

    @Transactional
    public ReservationResponse checkIn(final String code) {
        Reservation reservation = reservationRepository.findByCheckInCode(code).orElseThrow(BusinessException::notFound);
        reservation.checkIn(LocalDateTime.now());
        programRepository.findByIdAndDeletedAtIsNull(reservation.getProgramId())
                .filter(Program::isSupportsQueue)
                .ifPresent(program -> {
                    LocalDate today = LocalDate.now();
                    int next = reservationRepository.maxQueueNumber(program.getId(), today.atStartOfDay(),
                            today.plusDays(1).atStartOfDay()) + 1;
                    reservation.enterQueue(next);
                });
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getQueue(final Long programId, final LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        return reservationRepository.findQueue(programId, day.atStartOfDay(), day.plusDays(1).atStartOfDay())
                .stream().map(ReservationResponse::from).toList();
    }

    @Transactional
    public ReservationResponse changeQueueStatus(final Long reservationId, final QueueStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(BusinessException::notFound);
        reservation.changeQueueStatus(status);
        return ReservationResponse.from(reservation);
    }

    private void cancel(final Reservation reservation) {
        reservation.cancel();
        if (reservation.getSessionId() != null) {
            sessionRepository.findForUpdate(reservation.getSessionId()).ifPresent(session -> session.release(reservation.getQuantity()));
        } else if (reservation.getReservationType() == ReservationType.PROGRAM) {
            programRepository.findForUpdate(reservation.getProgramId()).ifPresent(program -> program.release(reservation.getQuantity()));
        }
        if (reservation.getPassId() != null) {
            passService.restoreUsage(reservation.getPassId(), reservation.getQuantity());
        }
        paymentService.refundIfPaid(PaymentTargetType.RESERVATION, reservation.getId());
        for (ReservationExtension extension : extensionRepository.findAllByReservationIdOrderByIdAsc(reservation.getId())) {
            if (extension.getStatus() == ReservationExtension.Status.PENDING_PAYMENT) {
                extension.cancel();
            } else if (extension.getStatus() == ReservationExtension.Status.PAID) {
                paymentService.refundIfPaid(PaymentTargetType.RESERVATION_EXTENSION, extension.getId());
            }
        }
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
