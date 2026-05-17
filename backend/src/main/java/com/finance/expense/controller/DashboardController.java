package com.finance.expense.controller;

import com.finance.expense.dto.DashboardSummaryDto;
import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(Authentication authentication) {
        List<ExpenseDto> allExpenses = expenseService.getExpensesForUser(authentication.getName());
        
        DashboardSummaryDto summary = new DashboardSummaryDto();
        
        BigDecimal totalMonth = allExpenses.stream()
            .map(ExpenseDto::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalExpensesMonth(totalMonth);
        
        summary.setTotalExpensesWeek(totalMonth.divide(new BigDecimal("4"), 2, java.math.RoundingMode.HALF_UP));
        
        Map<String, BigDecimal> categoryMap = allExpenses.stream()
            .filter(e -> e.getCategoryName() != null)
            .collect(Collectors.groupingBy(ExpenseDto::getCategoryName,
                Collectors.mapping(ExpenseDto::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        summary.setCategoryWiseSpending(categoryMap);
        
        String highestCat = categoryMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        summary.setHighestSpendingCategory(highestCat);
        
        summary.setRecentTransactions(allExpenses.stream().limit(5).collect(Collectors.toList()));
        
        List<String> insights = new ArrayList<>();
        if (totalMonth.compareTo(new BigDecimal("1500")) > 0) {
            insights.add("Your spending is high this month!");
        } else {
            insights.add("You are well within your budget.");
        }
        if (!highestCat.equals("None")) {
            insights.add(highestCat + " is your highest expense category.");
        }
        summary.setInsights(insights);
        
        return ResponseEntity.ok(summary);
    }
}
