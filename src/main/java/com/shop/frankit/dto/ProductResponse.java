package com.shop.frankit.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @ToString
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal shippingFee;
    private LocalDateTime registeredAt;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
