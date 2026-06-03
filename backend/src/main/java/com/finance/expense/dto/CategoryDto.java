package com.finance.expense.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private boolean isDefault;
    private int expenseCount;
    private BigDecimal totalSpend;
}
