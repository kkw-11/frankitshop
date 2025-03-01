package com.shop.frankit.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

@Getter @Setter @ToString
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal shippingFee;
}
