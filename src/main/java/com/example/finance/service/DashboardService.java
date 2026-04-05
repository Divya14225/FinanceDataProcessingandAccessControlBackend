package com.example.finance.service;


import com.example.finance.dto.DashboardSummary;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private TransactionRepository transactionRepository;

    public DashboardSummary getDashboardSummary(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findUserTransactionsInDateRange(userId, startDate, endDate);

        // Calculate totals
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Category wise totals
        Map<String, BigDecimal> categoryWiseTotals = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getDisplayName(),
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Recent activities (last 10)
        List<DashboardSummary.RecentActivity> recentActivities = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .limit(10)
                .map(t -> DashboardSummary.RecentActivity.builder()
                        .transactionId(t.getId())
                        .amount(t.getAmount())
                        .type(t.getType().name())
                        .category(t.getCategory().getDisplayName())
                        .description(t.getDescription())
                        .date(t.getTransactionDate())
                        .build())
                .collect(Collectors.toList());

        // Monthly trends
        Map<String, List<Transaction>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(t ->
                        String.format("%d-%02d", t.getTransactionDate().getYear(), t.getTransactionDate().getMonthValue())
                ));

        List<DashboardSummary.MonthlyTrend> monthlyTrends = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : transactionsByMonth.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            BigDecimal monthlyIncome = entry.getValue().stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyExpenses = entry.getValue().stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyTrends.add(DashboardSummary.MonthlyTrend.builder()
                    .year(year)
                    .month(month)
                    .income(monthlyIncome)
                    .expenses(monthlyExpenses)
                    .net(monthlyIncome.subtract(monthlyExpenses))
                    .build());
        }

        monthlyTrends.sort(Comparator.comparing((DashboardSummary.MonthlyTrend t) -> t.getYear())
                .thenComparing(t -> t.getMonth()));

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryWiseTotals(categoryWiseTotals)
                .recentActivities(recentActivities)
                .monthlyTrends(monthlyTrends)
                .build();
    }
}