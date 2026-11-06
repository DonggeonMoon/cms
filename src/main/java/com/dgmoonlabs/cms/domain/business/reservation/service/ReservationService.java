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
import com.dgmoonlabs.cms.domain.business.reservation.entity.reservationitem.ReservationItem;
import com.dgmoonlabs.cms.domain.business.reservation.entity.reservationitem.ReservationItemSession;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationItemRepository;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationItemSessionRepository;
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
    private final ReservationItemRepository reservationItemRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentService paymentService;
    private final FileService fileService;
    private final HtmlSanitizer htmlSanitizer;
    private final RegexService regexService;
    private final JsonMapper jsonMapper;
    private final GradeService gradeService;
    private final ReservationItemSessionRepository sessionRepository;
    private final ReservationExtensionRepository extensionRepository;
    private final PassService passService;

    @Transactional(readOnly = true)
    public Page<ReservationItemResponse> getReservationItems(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        LocalDateTime now = LocalDateTime.now();
        Page<ReservationItem> items = CurrentUser.isAdmin()
                ? reservationItemRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable)
                : reservationItemRepository.findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable);
        return items.map(item -> ReservationItemResponse.of(item, null, item.isOpenAt(now), null));
    }

    @Transactional(readOnly = true)
    public ReservationItemResponse getReservationItem(final Long id) {
        ReservationItem item = findReservationItem(id);
        if (!item.isEnabled() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        LocalDateTime now = LocalDateTime.now();
        return ReservationItemResponse.of(item, layoutOf(item), item.isOpenAt(now),
                reviewRepository.averageReservationItemRating(id), sessionsOf(item).stream()
                        .map(session -> SessionResponse.of(session, now)).toList(), priceTiersOf(item));
    }

    private List<ReservationItemSession> sessionsOf(final ReservationItem item) {
        return item.supportsSessions()
                ? sessionRepository.findAllByReservationItemIdAndDeletedAtIsNullOrderByStartAtAsc(item.getId()) : List.of();
    }

    @Transactional
    public SessionResponse createSession(final Long reservationItemId, final SessionSaveRequest request) {
        ReservationItem item = findReservationItem(reservationItemId);
        if (!item.supportsSessions()) {
            throw new BusinessException(ErrorCode.SESSIONS_NOT_SUPPORTED);
        }
        requireOrdered(request);
        ReservationItemSession session = sessionRepository.save(ReservationItemSession.builder()
                .reservationItemId(item.getId())
                .name(blankToNull(request.name()))
                .startAt(request.startAt())
                .endAt(request.endAt())
                .capacity(request.capacity())
                .build());
        return SessionResponse.of(session, LocalDateTime.now());
    }

    @Transactional
    public SessionResponse updateSession(final Long reservationItemId, final Long sessionId, final SessionSaveRequest request) {
        requireOrdered(request);
        ReservationItemSession session = lockedSession(reservationItemId, sessionId);
        session.update(blankToNull(request.name()), request.startAt(), request.endAt(), request.capacity());
        return SessionResponse.of(session, LocalDateTime.now());
    }

    @Transactional
    public void deleteSession(final Long reservationItemId, final Long sessionId) {
        ReservationItemSession session = lockedSession(reservationItemId, sessionId);
        if (reservationRepository.existsBySessionIdAndStatusNot(session.getId(), ReservationStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.SESSION_HAS_RESERVATIONS);
        }
        session.delete();
    }

    private ReservationItemSession lockedSession(final Long reservationItemId, final Long sessionId) {
        return sessionRepository.findForUpdate(sessionId)
                .filter(session -> session.getReservationItemId().equals(reservationItemId) && session.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
    }

    private static void requireOrdered(final SessionSaveRequest request) {
        if (request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
    }

    /** 회차가 있는 상품은 회차를 꼭 골라야 한다. 회차를 잠가 두고 정원을 확인한다. */
    private Optional<ReservationItemSession> sessionFor(final ReservationItem item, final Long sessionId) {
        if (!item.supportsSessions() || !sessionRepository.existsByReservationItemIdAndDeletedAtIsNull(item.getId())) {
            return Optional.empty();
        }
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.SESSION_REQUIRED);
        }
        return Optional.of(sessionRepository.findForUpdate(sessionId)
                .filter(session -> session.getReservationItemId().equals(item.getId()) && session.isOpenAt(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_OPEN)));
    }

    @Transactional
    public Long createReservationItem(final ReservationItemSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        ReservationItem item = reservationItemRepository.save(ReservationItem.builder()
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
        applySettings(item, request);
        item.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
        return item.getId();
    }

    @Transactional
    public void updateReservationItem(final Long id, final ReservationItemSaveRequest request) {
        validate(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        ReservationItem item = reservationItemRepository.findForUpdate(id)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        item.update(request.name(), request.description(), request.imageFileId(), request.price(),
                        request.maximumPerson(), request.startDateTime(), request.endDateTime(),
                        request.applicationStartDateTime(), request.applicationEndDateTime(), html,
                        htmlSanitizer.toPlainText(html), request.isEnabled());
        applySettings(item, request);
        item.changeAccessGrade(gradeService.validGradeId(request.accessGradeId()));
    }

    @Transactional
    public void deleteReservationItem(final Long id) {
        findReservationItem(id).delete();
    }

    @Transactional
    public ReservationResponse reserve(final Long memberId, final ReservationCreateRequest request) {
        ReservationItem item = reservationItemRepository.findForUpdate(request.reservationItemId())
                .filter(found -> found.isOpenAt(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_ITEM_NOT_OPEN));
        gradeService.require(item.getAccessGradeId());
        return create(UserType.MEMBER, memberId, item, request, false);
    }

    @Transactional
    public ReservationResponse reserveAsAdmin(final Long adminId, final ReservationCreateRequest request, final boolean paid) {
        ReservationItem item = reservationItemRepository.findForUpdate(request.reservationItemId())
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        return create(UserType.ADMIN, adminId, item, request, paid);
    }

    private ReservationResponse create(final UserType userType, final Long userId, final ReservationItem item,
                                       final ReservationCreateRequest request, final boolean paid) {
        Optional<ReservationItemSession> session = sessionFor(item, request.sessionId());
        Booking booking = switch (item.getReservationType()) {
            case PROGRAM -> {
                if (session.isPresent()) {
                    session.get().reserve(request.quantity());
                } else {
                    item.reserve(request.quantity());
                }
                yield new Booking(request.quantity(), item.getPrice().multiply(BigDecimal.valueOf(request.quantity())),
                        session.map(ReservationItemSession::getStartAt).orElse(null), session.map(ReservationItemSession::getEndAt).orElse(null), null);
            }
            case EVENT -> {
                if (session.isEmpty()) {
                    yield bookSpace(item, request.cellsOrEmpty(), new Period(item.getStartDateTime(),
                            item.getEndDateTime() != null ? item.getEndDateTime() : item.getStartDateTime().plusMinutes(1), 1));
                }
                ReservationItemSession show = session.get();
                Booking seats = bookSpace(item, request.cellsOrEmpty(), sessionTakenCells(show.getId()),
                        new Period(show.getStartAt(), show.endOrStart(), 1));
                show.reserve(seats.quantity());
                yield seats;
            }
            case CONSULTATION -> bookConsultation(item, request.startAt());
            case VISIT -> {
                requireVisitAddress(request);
                yield bookConsultation(item, request.startAt());
            }
            case RENTAL -> bookSpace(item, request.cellsOrEmpty(), rentalPeriod(item, request.startAt(), request.endAt()));
            case TABLE -> bookTable(item, request.cellsOrEmpty(), rentalPeriod(item, request.startAt(), request.endAt()),
                    request.partySize());
            case LODGING -> bookSpace(item, request.cellsOrEmpty(), lodgingPeriod(item, request.startAt(), request.endAt()));
            case ITEM_RENTAL -> bookItems(item, request.quantity(), request.startAt(), request.endAt());
            case TOUR -> bookTour(item, session, request.partyCounts());
        };
        MemberPass usedPass = request.passId() == null ? null
                : passService.consumeForMember(userId, request.passId(), item.getId(), booking.quantity());
        BigDecimal totalPrice = usedPass == null ? booking.total() : BigDecimal.ZERO;
        ReservationStatus status = paid || totalPrice.signum() == 0 ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .reservationNumber(regexService.requireNumberFormat(RegexTarget.RESERVATION_NUMBER,
                        BusinessNumbers.next("R", reservationRepository::existsByReservationNumber)))
                .checkInCode(BusinessNumbers.next("CHK", reservationRepository::existsByCheckInCode))
                .userType(userType)
                .userId(userId)
                .reservationItemId(item.getId())
                .reservationItemName(item.getName())
                .reservationType(item.getReservationType())
                .startAt(booking.startAt())
                .endAt(booking.endAt())
                .cells(booking.cells())
                .quantity(booking.quantity())
                .totalPrice(totalPrice)
                .depositAmount(booking.deposit())
                .status(status)
                .applicantName(request.applicantName())
                .phoneNumber(request.phoneNumber())
                .visitPostalCode(item.getReservationType() == ReservationType.VISIT ? blankToNull(request.visitPostalCode()) : null)
                .visitAddress(item.getReservationType() == ReservationType.VISIT ? request.visitAddress().trim() : null)
                .visitDetailAddress(item.getReservationType() == ReservationType.VISIT ? blankToNull(request.visitDetailAddress()) : null)
                .requestMemo(blankToNull(request.requestMemo()))
                .sessionId(session.map(ReservationItemSession::getId).orElse(null))
                .passId(usedPass == null ? null : usedPass.getId())
                .lateFeePerDay(item.getReservationType() == ReservationType.ITEM_RENTAL ? item.getLateFeePerDay() : null)
                .partySize(item.getReservationType() == ReservationType.TABLE ? request.partySize() : null)
                .partyBreakdown(item.getReservationType() == ReservationType.TOUR ? request.partyCounts() : null)
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

    private Booking bookConsultation(final ReservationItem item, final LocalDateTime startAt) {
        Slot slot = slotsOf(item, startAt == null ? null : startAt.toLocalDate()).stream()
                .filter(candidate -> candidate.startAt().equals(startAt))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESERVATION_TIME));
        if (slot.remaining() <= 0) {
            throw new BusinessException(ErrorCode.RESERVATION_SLOT_FULL);
        }
        return new Booking(1, item.getPrice(), slot.startAt(), slot.endAt(), null);
    }

    private Booking bookSpace(final ReservationItem item, final List<String> keys, final Period period) {
        return bookSpace(item, keys, takenCells(item.getId(), period.from(), period.to()), period);
    }

    private Booking bookSpace(final ReservationItem item, final List<String> keys, final Set<String> taken, final Period period) {
        List<SpaceLayout.Cell> cells = layoutOf(item).bookable(keys);
        if (cells.stream().anyMatch(cell -> taken.contains(cell.key()))) {
            throw new BusinessException(ErrorCode.CELL_ALREADY_RESERVED);
        }
        BigDecimal perUnit = cells.stream()
                .map(cell -> cell.price() != null ? cell.price() : item.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String joined = String.join(",", cells.stream().map(SpaceLayout.Cell::key).toList());
        return new Booking(cells.size(), perUnit.multiply(BigDecimal.valueOf(period.units())), period.from(), period.to(), joined);
    }

    private Booking bookTable(final ReservationItem item, final List<String> keys, final Period period, final Integer partySize) {
        if (partySize == null || partySize <= 0) {
            throw new BusinessException(ErrorCode.PARTY_SIZE_REQUIRED);
        }
        List<SpaceLayout.Cell> cells = layoutOf(item).bookable(keys);
        int capacity = cells.stream().mapToInt(cell -> cell.capacity() == null ? Integer.MAX_VALUE : cell.capacity()).sum();
        if (capacity < partySize) {
            throw new BusinessException(ErrorCode.TABLE_CAPACITY_EXCEEDED);
        }
        return bookSpace(item, keys, period);
    }

    /** 인원 유형(성인·아동 등)별 인원 × 요금을 더해 총액을 낸다. 출발일 회차가 있으면 전체 인원만큼 정원을 잡는다. */
    private Booking bookTour(final ReservationItem item, final Optional<ReservationItemSession> session, final Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            throw new BusinessException(ErrorCode.PARTY_BREAKDOWN_REQUIRED);
        }
        Map<String, BigDecimal> priceByTier = priceTiersOf(item).stream()
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
        return new Booking(totalQuantity, total, session.map(ReservationItemSession::getStartAt).orElse(null),
                session.map(ReservationItemSession::getEndAt).orElse(null), null);
    }

    private List<PriceTier> priceTiersOf(final ReservationItem item) {
        return item.getPriceTiers() == null ? List.of() : List.of(jsonMapper.readValue(item.getPriceTiers(), PriceTier[].class));
    }

    /** 대여일부터 반납일 전날까지를 대여 일수로 센다. 금액은 1일 대여료 × 일수 × 수량에 보증금을 더한다. */
    private Booking bookItems(final ReservationItem item, final int quantity, final LocalDateTime startAt, final LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        LocalDate from = startAt.toLocalDate();
        LocalDate to = endAt.toLocalDate();
        int days = (int) ChronoUnit.DAYS.between(from, to);
        if (days <= 0 || from.isBefore(LocalDate.now()) || !withinPeriod(item, from) || !withinPeriod(item, to.minusDays(1))
                || (item.getMaxRentalDays() != null && days > item.getMaxRentalDays())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        if (remainingItems(item, from, to) < quantity) {
            throw new BusinessException(ErrorCode.RENTAL_OUT_OF_STOCK);
        }
        BigDecimal deposit = (item.getDepositAmount() == null ? BigDecimal.ZERO : item.getDepositAmount())
                .multiply(BigDecimal.valueOf(quantity));
        BigDecimal fee = item.getPrice().multiply(BigDecimal.valueOf((long) days * quantity));
        return new Booking(quantity, fee.add(deposit), from.atStartOfDay(), to.atStartOfDay(), null, deposit);
    }

    private int remainingItems(final ReservationItem item, final LocalDate from, final LocalDate to) {
        return remainingItems(item, from, to, null);
    }

    /** excludeId: 연장할 예약 자신은 빼고 센다. */
    private int remainingItems(final ReservationItem item, final LocalDate from, final LocalDate to, final Long excludeId) {
        int stock = item.getMaximumPerson() == null ? 0 : item.getMaximumPerson();
        List<Reservation> active = activeReservations(item.getId()).stream()
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
        ReservationItem item = reservationItemRepository.findForUpdate(reservation.getReservationItemId()).orElseThrow(BusinessException::notFound);
        LocalDate currentEnd = reservation.getEndAt().toLocalDate();
        LocalDate newEnd = endAt.toLocalDate();
        int days = (int) ChronoUnit.DAYS.between(currentEnd, newEnd);
        long totalDays = ChronoUnit.DAYS.between(reservation.getStartAt().toLocalDate(), newEnd);
        if (days <= 0 || !withinPeriod(item, newEnd.minusDays(1))
                || (item.getMaxRentalDays() != null && totalDays > item.getMaxRentalDays())) {
            throw new BusinessException(ErrorCode.INVALID_EXTENSION);
        }
        if (reservation.getPendingExtensionId() != null) {
            throw new BusinessException(ErrorCode.EXTENSION_PAYMENT_PENDING);
        }
        if (remainingItems(item, currentEnd, newEnd, reservation.getId()) < reservation.getQuantity()) {
            throw new BusinessException(ErrorCode.RENTAL_OUT_OF_STOCK);
        }
        BigDecimal fee = item.getPrice().multiply(BigDecimal.valueOf((long) days * reservation.getQuantity()));
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

    private Period rentalPeriod(final ReservationItem item, final LocalDateTime startAt, final LocalDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt) || !startAt.toLocalDate().equals(endAt.toLocalDate())
                || !withinPeriod(item, startAt.toLocalDate()) || !onSlotBoundary(item, startAt) || !onSlotBoundary(item, endAt)) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        long minutes = Duration.between(startAt, endAt).toMinutes();
        return new Period(startAt, endAt, (int) (minutes / item.getSlotMinutes()));
    }

    private Period lodgingPeriod(final ReservationItem item, final LocalDateTime checkIn, final LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        LocalDate in = checkIn.toLocalDate();
        LocalDate out = checkOut.toLocalDate();
        if (!out.isAfter(in) || !withinPeriod(item, in) || !withinPeriod(item, out.minusDays(1))) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        return new Period(in.atStartOfDay(), out.atStartOfDay(), (int) ChronoUnit.DAYS.between(in, out));
    }

    private boolean withinPeriod(final ReservationItem item, final LocalDate date) {
        return (item.getStartDateTime() == null || !date.isBefore(item.getStartDateTime().toLocalDate()))
                && (item.getEndDateTime() == null || !date.isAfter(item.getEndDateTime().toLocalDate()));
    }

    private static boolean onSlotBoundary(final ReservationItem item, final LocalDateTime at) {
        LocalTime time = at.toLocalTime();
        if (time.isBefore(item.getOpenTime()) || time.isAfter(item.getCloseTime())) {
            return false;
        }
        return Duration.between(item.getOpenTime(), time).toMinutes() % item.getSlotMinutes() == 0;
    }

    private List<Slot> slotsOf(final ReservationItem item, final LocalDate date) {
        if (date == null || item.getSlotMinutes() == null || item.getOpenTime() == null || item.getCloseTime() == null
                || !withinPeriod(item, date)) {
            return List.of();
        }
        int capacity = item.getMaximumPerson() == null ? 1 : item.getMaximumPerson();
        Map<LocalDateTime, Long> booked = new HashMap<>();
        for (Reservation reservation : activeReservations(item.getId())) {
            if (reservation.getStartAt() != null) {
                booked.merge(reservation.getStartAt(), 1L, Long::sum);
            }
        }
        List<Slot> slots = new ArrayList<>();
        LocalDateTime cursor = date.atTime(item.getOpenTime());
        LocalDateTime close = date.atTime(item.getCloseTime());
        while (!cursor.plusMinutes(item.getSlotMinutes()).isAfter(close)) {
            LocalDateTime end = cursor.plusMinutes(item.getSlotMinutes());
            slots.add(new Slot(cursor, end, (int) Math.max(0, capacity - booked.getOrDefault(cursor, 0L))));
            cursor = end;
        }
        return slots;
    }

    private Set<String> takenCells(final Long reservationItemId, final LocalDateTime from, final LocalDateTime to) {
        Set<String> taken = new HashSet<>();
        for (Reservation reservation : activeReservations(reservationItemId)) {
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

    private List<Reservation> activeReservations(final Long reservationItemId) {
        return reservationRepository.findAllByReservationItemIdAndStatusNot(reservationItemId, ReservationStatus.CANCELED);
    }

    @Transactional(readOnly = true)
    public Availability availability(final Long reservationItemId, final LocalDate date, final LocalDateTime from, final LocalDateTime to,
                                     final Long sessionId) {
        ReservationItem item = findReservationItem(reservationItemId);
        if (sessionId != null && item.getReservationType() == ReservationType.EVENT) {
            sessionRepository.findByIdAndReservationItemIdAndDeletedAtIsNull(sessionId, reservationItemId).orElseThrow(BusinessException::notFound);
            return new Availability(sorted(sessionTakenCells(sessionId)), List.of());
        }
        return switch (item.getReservationType()) {
            case PROGRAM -> new Availability(List.of(), List.of());
            case CONSULTATION, VISIT -> new Availability(List.of(), slotsOf(item, date));
            case RENTAL, TABLE -> new Availability(from == null || to == null ? List.of() : sorted(takenCells(reservationItemId, from, to)),
                    slotsOf(item, date));
            case LODGING -> new Availability(from == null || to == null ? List.of()
                    : sorted(takenCells(reservationItemId, from.toLocalDate().atStartOfDay(), to.toLocalDate().atStartOfDay())), List.of());
            case EVENT -> new Availability(sorted(takenCells(reservationItemId, null, null)), List.of());
            case ITEM_RENTAL -> new Availability(List.of(), List.of(), from == null || to == null || !to.toLocalDate().isAfter(from.toLocalDate())
                    ? null : remainingItems(item, from.toLocalDate(), to.toLocalDate()));
            case TOUR -> new Availability(List.of(), List.of());
        };
    }

    private static List<String> sorted(final Set<String> keys) {
        return keys.stream().sorted().toList();
    }

    private void applySettings(final ReservationItem item, final ReservationItemSaveRequest request) {
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
        item.changeReservationSettings(type, request.slotMinutes(), request.openTime(), request.closeTime(), layout);
        item.changeRentalSettings(request.depositAmount(), request.maxRentalDays(), request.lateFeePerDay());
        item.changeQueueSetting(request.supportsQueue());
        item.changeTourSettings(type == ReservationType.TOUR ? jsonMapper.writeValueAsString(request.priceTiers()) : null);
    }

    private SpaceLayout layoutOf(final ReservationItem item) {
        return item.getLayout() == null ? null : jsonMapper.readValue(item.getLayout(), SpaceLayout.class);
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
    public Page<ReservationResponse> searchReservations(final Long reservationItemId, final ReservationStatus status,
                                                        final String keyword, final Pageable pageable) {
        String text = keyword == null ? "" : keyword.trim();
        if (!text.isEmpty() && regexService.matches(RegexTarget.RESERVATION_NUMBER, text)) {
            return reservationRepository.findByReservationNumber(text, pageable).map(ReservationResponse::from);
        }
        return reservationRepository.search(reservationItemId, status, text, pageable).map(ReservationResponse::from);
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
        reservationItemRepository.findByIdAndDeletedAtIsNull(reservation.getReservationItemId())
                .filter(ReservationItem::isSupportsQueue)
                .ifPresent(item -> {
                    LocalDate today = LocalDate.now();
                    int next = reservationRepository.maxQueueNumber(item.getId(), today.atStartOfDay(),
                            today.plusDays(1).atStartOfDay()) + 1;
                    reservation.enterQueue(next);
                });
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getQueue(final Long reservationItemId, final LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        return reservationRepository.findQueue(reservationItemId, day.atStartOfDay(), day.plusDays(1).atStartOfDay())
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
            reservationItemRepository.findForUpdate(reservation.getReservationItemId()).ifPresent(item -> item.release(reservation.getQuantity()));
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

    private void validate(final ReservationItemSaveRequest request) {
        if (!request.hasValidPeriods()) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        if (request.imageFileId() != null) {
            fileService.requireFilesExist(List.of(request.imageFileId()));
        }
    }

    private ReservationItem findReservationItem(final Long id) {
        return reservationItemRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
