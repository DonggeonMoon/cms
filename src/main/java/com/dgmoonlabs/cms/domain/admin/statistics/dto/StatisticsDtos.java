package com.dgmoonlabs.cms.domain.admin.statistics.dto;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class StatisticsDtos {
    private StatisticsDtos() {
    }

    public enum Dimension {
        BROWSER, OS, URL
    }

    public record VisitRequest(@NotBlank @Size(max = 500) String path, @Size(max = 500) String referrer) {
    }

    public record DailyRow(LocalDate date, long visits) {
    }

    public record CountRow(String label, long visits) {
    }

    public record VisitLogResponse(Long id, String username, String url, String referer, String ipAddress,
                                   LocalDateTime createdAt) {
        public static VisitLogResponse from(final StatisticsLog log) {
            return new VisitLogResponse(log.getId(), log.getMemberUsername(), log.getUrl(), log.getReferer(),
                    log.getIpAddress(), log.getCreatedAt());
        }
    }

    public record DashboardResponse(
            long memberCount,
            long articleCount,
            long questionCount,
            long pendingPaymentOrders,
            long paidOrders,
            BigDecimal revenueLast30Days,
            long confirmedReservations,
            long receivedReports,
            long todayVisits,
            List<DailyRow> last7DaysVisits
    ) {
    }
}
