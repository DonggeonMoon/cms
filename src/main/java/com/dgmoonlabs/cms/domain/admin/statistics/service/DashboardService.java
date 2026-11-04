package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.DailyRow;
import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.DashboardResponse;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleRepository;
import com.dgmoonlabs.cms.domain.board.repository.QuestionRepository;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.reservation.constant.ReservationStatus;
import com.dgmoonlabs.cms.domain.business.reservation.repository.ReservationRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.government.report.constant.ReportStatus;
import com.dgmoonlabs.cms.domain.government.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final MemberRepository memberRepository;
    private final BoardArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ReportRepository reportRepository;
    private final StatisticsService statisticsService;

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        LocalDate today = LocalDate.now();
        List<DailyRow> last7Days = statisticsService.dailyVisits(today.minusDays(6), today);
        return new DashboardResponse(
                memberRepository.countByDeletedAtIsNull(),
                articleRepository.countByDeletedAtIsNull(),
                questionRepository.countUnanswered(),
                orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT),
                orderRepository.countByStatus(OrderStatus.PAID),
                orderRepository.sumTotalPriceSince(
                        EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPING, OrderStatus.DELIVERED, OrderStatus.PREPARING,
                                OrderStatus.READY, OrderStatus.COMPLETED),
                        today.minusDays(29).atStartOfDay()),
                reservationRepository.countByStatus(ReservationStatus.CONFIRMED),
                reportRepository.countByStatusAndDeletedAtIsNull(ReportStatus.RECEIVED),
                last7Days.getLast().visits(),
                last7Days
        );
    }
}
