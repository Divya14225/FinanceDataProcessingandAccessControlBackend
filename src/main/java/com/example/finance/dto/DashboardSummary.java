package com.example.finance.dto;


import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> categoryWiseTotals;
    private List<RecentActivity> recentActivities;
    private List<MonthlyTrend> monthlyTrends;

    @Data
    @Builder
    public static class RecentActivity {
        private String transactionId;
        private BigDecimal amount;
        private String type;
        private String category;
        private String description;
        private LocalDateTime date;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        private int year;
        private int month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}