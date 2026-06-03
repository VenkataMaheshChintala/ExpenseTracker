package com.finance.expense.repository;

import com.finance.expense.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    List<Category> findByUserIdOrIsDefaultTrue(Long userId);
    java.util.Optional<Category> findFirstByNameAndUserId(String name, Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
}
