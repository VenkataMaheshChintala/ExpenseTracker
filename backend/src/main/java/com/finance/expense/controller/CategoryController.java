package com.finance.expense.controller;

import com.finance.expense.dto.CategoryDto;
import com.finance.expense.dto.ExpenseDto;
import com.finance.expense.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** GET /api/categories — all categories for the logged-in user, with stats */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(Authentication authentication) {
        return ResponseEntity.ok(categoryService.getCategoriesForUser(authentication.getName()));
    }

    /** POST /api/categories — create a new category; body: {"name": "..."} */
    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Category name must not be empty.");
        }
        try {
            CategoryDto created = categoryService.createCategory(authentication.getName(), name);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** PUT /api/categories/{id} — rename a category; body: {"name": "..."} */
    @PutMapping("/{id}")
    public ResponseEntity<?> renameCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Category name must not be empty.");
        }
        try {
            CategoryDto updated = categoryService.renameCategory(authentication.getName(), id, name);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** DELETE /api/categories/{id} — delete a category */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            categoryService.deleteCategory(authentication.getName(), id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** GET /api/categories/{id}/expenses — all expenses in a specific category */
    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<ExpenseDto>> getCategoryExpenses(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(categoryService.getExpensesForCategory(authentication.getName(), id));
    }
}
