package com.finance.expense.service;

import com.finance.expense.dto.CategoryDto;
import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.entity.Category;
import com.finance.expense.entity.Expense;
import com.finance.expense.entity.User;
import com.finance.expense.repository.CategoryRepository;
import com.finance.expense.repository.ExpenseRepository;
import com.finance.expense.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExpenseService expenseService;

    /**
     * Returns all categories created by the user, each enriched with
     * the count of expenses and total amount spent in that category.
     */
    public List<CategoryDto> getCategoriesForUser(String email) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        return categoryRepository.findByUserId(user.getId()).stream()
                .map(cat -> buildCategoryDto(cat, user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new named category for the user, provided the name
     * is not already taken by one of their existing categories.
     */
    public CategoryDto createCategory(String email, String name) {
        User user = userRepository.findUserByEmail(email).orElseThrow();

        if (categoryRepository.existsByNameAndUserId(name.trim(), user.getId())) {
            throw new IllegalArgumentException("A category with that name already exists.");
        }

        Category category = categoryRepository.save(
                Category.builder()
                        .name(name.trim())
                        .isDefault(false)
                        .user(user)
                        .build()
        );

        CategoryDto dto = mapToCategoryDto(category);
        dto.setExpenseCount(0);
        dto.setTotalSpend(BigDecimal.ZERO);
        return dto;
    }

    /**
     * Renames an existing category that belongs to the user.
     */
    public CategoryDto renameCategory(String email, Long categoryId, String newName) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied."));

        if (categoryRepository.existsByNameAndUserId(newName.trim(), user.getId())) {
            throw new IllegalArgumentException("A category with that name already exists.");
        }

        category.setName(newName.trim());
        category = categoryRepository.save(category);
        return buildCategoryDto(category, user.getId());
    }

    /**
     * Deletes a category. Expenses linked to it will have their category set to null
     * automatically via the database (no cascade delete on expenses).
     */
    public void deleteCategory(String email, Long categoryId) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied."));
        categoryRepository.delete(category);
    }

    /**
     * Returns all expenses that belong to the specified category for the authenticated user.
     */
    public List<ExpenseDto> getExpensesForCategory(String email, Long categoryId) {
        User user = userRepository.findUserByEmail(email).orElseThrow();
        return expenseRepository.findByUserIdAndCategoryIdOrderByDateDesc(user.getId(), categoryId)
                .stream()
                .map(expenseService::mapToDto)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CategoryDto buildCategoryDto(Category category, Long userId) {
        List<Expense> expenses = expenseRepository
                .findByUserIdAndCategoryIdOrderByDateDesc(userId, category.getId());

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CategoryDto dto = mapToCategoryDto(category);
        dto.setExpenseCount(expenses.size());
        dto.setTotalSpend(total);
        return dto;
    }

    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDefault(category.isDefault());
        return dto;
    }
}
