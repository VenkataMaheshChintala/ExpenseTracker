package com.finance.expense.controller;

import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getExpensesForUser(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseDto expenseDto, Authentication authentication) {
        return ResponseEntity.ok(expenseService.addExpense(authentication.getName(), expenseDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication authentication) {
        expenseService.deleteExpense(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @RequestBody ExpenseDto expenseDto, Authentication authentication) {
        return ResponseEntity.ok(expenseService.updateExpense(authentication.getName(), id, expenseDto));
    }
}
