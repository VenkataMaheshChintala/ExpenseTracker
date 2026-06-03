package com.finance.expense.service;

import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.entity.Category;
import com.finance.expense.entity.Expense;
import com.finance.expense.entity.User;
import com.finance.expense.repository.CategoryRepository;
import com.finance.expense.repository.ExpenseRepository;
import com.finance.expense.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ExpenseDto> getExpensesForUser(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        return expenseRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public ExpenseDto addExpense(String email, ExpenseDto expenseDto) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        Category category = null;
        if (expenseDto.getCategoryId() != null) {
            category = categoryRepository.findById(expenseDto.getCategoryId()).orElse(null);
        } else if (expenseDto.getCategoryName() != null) {
            category = categoryRepository.findFirstByNameAndUserId(expenseDto.getCategoryName(), user.getId())
                .orElseGet(() -> categoryRepository.save(Category.builder()
                    .name(expenseDto.getCategoryName())
                    .isDefault(false)
                    .user(user)
                    .build()));
        }
        
        Expense expense = Expense.builder()
                .amount(expenseDto.getAmount())
                .category(category)
                .description(expenseDto.getDescription())
                .date(expenseDto.getDate())
                .paymentMethod(expenseDto.getPaymentMethod())
                .notes(expenseDto.getNotes())
                .user(user)
                .build();
                
        expense = expenseRepository.save(expense);
        return mapToDto(expense);
    }
    
    public void deleteExpense(String email, Long id) {
        expenseRepository.deleteById(id);
    }

    public ExpenseDto updateExpense(String email, Long id, ExpenseDto expenseDto) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        Expense expense = expenseRepository.findById(id).orElseThrow();
        
        Category category = null;
        if (expenseDto.getCategoryName() != null) {
            category = categoryRepository.findFirstByNameAndUserId(expenseDto.getCategoryName(), user.getId())
                .orElseGet(() -> categoryRepository.save(Category.builder()
                    .name(expenseDto.getCategoryName())
                    .isDefault(false)
                    .user(user)
                    .build()));
        }

        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setDate(expenseDto.getDate());
        expense.setCategory(category);
        
        expense = expenseRepository.save(expense);
        return mapToDto(expense);
    }

    private ExpenseDto mapToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        if (expense.getCategory() != null) {
            dto.setCategoryId(expense.getCategory().getId());
            dto.setCategoryName(expense.getCategory().getName());
        }
        dto.setDescription(expense.getDescription());
        dto.setDate(expense.getDate());
        dto.setPaymentMethod(expense.getPaymentMethod());
        dto.setNotes(expense.getNotes());
        return dto;
    }
}
