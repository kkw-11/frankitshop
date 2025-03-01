package com.shop.frankit.dto;

import com.shop.frankit.entity.ProductOption;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @ToString
public class ProductOptionRequest {
    private String name;
    private ProductOption.OptionType type;  // INPUT 또는 SELECT
    private BigDecimal additionalPrice;
    private List<String> optionValues;  // SELECT 타입인 경우 선택 가능한 값 목록
}
