package com.shop.frankit.dto;

import com.shop.frankit.entity.ProductOption.OptionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductOptionResponse {
    private Long id;
    private String name;
    private OptionType type;
    private BigDecimal additionalPrice;
    private Long productId;
    private List<String> optionValues;  // SELECT 타입인 경우 선택 가능한 값 목록
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
