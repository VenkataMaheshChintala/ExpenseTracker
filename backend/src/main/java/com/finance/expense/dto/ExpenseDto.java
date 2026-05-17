package com.finance.expense.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDto {
    private Long id;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String description;
    private LocalDate date;
    private String paymentMethod;
    private String notes;
}
