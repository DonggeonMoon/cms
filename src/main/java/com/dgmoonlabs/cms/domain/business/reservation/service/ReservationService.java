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
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationType;
import com.dgmoonlabs.cms.domain.business.reservation.layout.SpaceLayout;
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
        return programs.map(program -> ProgramResponse.of(program, null, program.isOpenAt(now), null));
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgram(final Long id) {
        Program program = findProgram(id);
        if (!program.isEnabled() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        return ProgramResponse.of(program, layoutOf(program), program.isOpenAt(LocalDateTime.now()),
                reviewRepository.averageProgramRating(id));
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
                .build()).getId();
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
        Booking booking = switch (program.getReservationType()) {
            case PROGRAM -> {
                program.reserve(request.quantity());
                yield new Booking(request.quantity(), program.getPrice().multiply(BigDecimal.valueOf(request.quantity())), null, null, null);
            }
            case CONSULTATION -> bookConsultation(program, request.startAt());
            case RENTAL -> bookSpace(program, request.cellsOrEmpty(), rentalPeriod(program, request.startAt(), request.endAt()));
            case LODGING -> bookSpace(program, request.cellsOrEmpty(), lodgingPeriod(program, request.startAt(), request.endAt()));
            case EVENT -> bookSpace(program, request.cellsOrEmpty(), new Period(program.getStartDateTime(),
                    program.getEndDateTime() != null ? program.getEndDateTime() : program.getStartDateTime().plusMinutes(1), 1));
        };
        ReservationStatus status = paid || booking.total().signum() == 0 ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .reservationNumber(regexService.requireNumberFormat(RegexTarget.RESERVATION_NUMBER,
                        BusinessNumbers.next("R", reservationRepository::existsByReservationNumber)))
                .userType(userType)
                .userId(userId)
                .programId(program.getId())
                .programName(program.getName())
                .reservationType(program.getReservationType())
                .startAt(booking.startAt())
                .endAt(booking.endAt())
                .cells(booking.cells())
                .quantity(booking.quantity())
                .totalPrice(booking.total())
                .status(status)
                .applicantName(request.applicantName())
                .phoneNumber(request.phoneNumber())
                .build());
        return ReservationResponse.from(reservation);
    }

    private record Booking(int quantity, BigDecimal total, LocalDateTime startAt, LocalDateTime endAt, String cells) {
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
        List<SpaceLayout.Cell> cells = layoutOf(program).bookable(keys);
        Set<String> taken = takenCells(program.getId(), period.from(), period.to());
        if (cells.stream().anyMatch(cell -> taken.contains(cell.key()))) {
            throw new BusinessException(ErrorCode.CELL_ALREADY_RESERVED);
        }
        BigDecimal perUnit = cells.stream()
                .map(cell -> cell.price() != null ? cell.price() : program.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String joined = String.join(",", cells.stream().map(SpaceLayout.Cell::key).toList());
        return new Booking(cells.size(), perUnit.multiply(BigDecimal.valueOf(period.units())), period.from(), period.to(), joined);
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

    private List<Reservation> activeReservations(final Long programId) {
        return reservationRepository.findAllByProgramIdAndStatusNot(programId, ReservationStatus.CANCELED);
    }

    @Transactional(readOnly = true)
    public Availability availability(final Long programId, final LocalDate date, final LocalDateTime from, final LocalDateTime to) {
        Program program = findProgram(programId);
        return switch (program.getReservationType()) {
            case PROGRAM -> new Availability(List.of(), List.of());
            case CONSULTATION -> new Availability(List.of(), slotsOf(program, date));
            case RENTAL -> new Availability(from == null || to == null ? List.of() : sorted(takenCells(programId, from, to)),
                    slotsOf(program, date));
            case LODGING -> new Availability(from == null || to == null ? List.of()
                    : sorted(takenCells(programId, from.toLocalDate().atStartOfDay(), to.toLocalDate().atStartOfDay())), List.of());
            case EVENT -> new Availability(sorted(takenCells(programId, null, null)), List.of());
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
        boolean timed = type == ReservationType.CONSULTATION || type == ReservationType.RENTAL;
        if (timed && (request.slotMinutes() == null || request.openTime() == null || request.closeTime() == null
                || !request.closeTime().isAfter(request.openTime()) || request.startDateTime() == null || request.endDateTime() == null)) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        if ((type == ReservationType.LODGING && (request.startDateTime() == null || request.endDateTime() == null))
                || (type == ReservationType.EVENT && request.startDateTime() == null)) {
            throw new BusinessException(ErrorCode.RESERVATION_SETTINGS_REQUIRED);
        }
        program.changeReservationSettings(type, request.slotMinutes(), request.openTime(), request.closeTime(), layout);
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

    private void cancel(final Reservation reservation) {
        reservation.cancel();
        if (reservation.getReservationType() == ReservationType.PROGRAM) {
            programRepository.findForUpdate(reservation.getProgramId()).ifPresent(program -> program.release(reservation.getQuantity()));
        }
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
