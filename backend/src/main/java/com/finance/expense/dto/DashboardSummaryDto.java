package com.finance.expense.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardSummaryDto {
    private BigDecimal totalExpensesMonth;
    private BigDecimal totalExpensesWeek;
    private String highestSpendingCategory;
    private List<ExpenseDto> recentTransactions;
    private Map<String, BigDecimal> categoryWiseSpending;
    private List<String> insights;
}
