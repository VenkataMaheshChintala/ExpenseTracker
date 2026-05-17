package com.finance.expense.repository;

import com.finance.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<String> findByEmail(String email); // Fixed this temporarily to avoid type issues if used incorrectly
    // Actually we need to return Optional<User>
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);
}
