package com.shop.frankit.mapper;

import com.shop.frankit.dto.ProductOptionRequest;
import com.shop.frankit.dto.ProductOptionResponse;
import com.shop.frankit.entity.OptionValue;
import com.shop.frankit.entity.ProductOption;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductOptionMapper {

    // Request → Entity
    public ProductOption toEntity(ProductOptionRequest dto) {
        log.debug("Converting ProductOptionRequest to ProductOption entity: {}", dto);
        ProductOption option = new ProductOption();
        option.setName(dto.getName());
        option.setType(dto.getType());
        option.setAdditionalPrice(dto.getAdditionalPrice());
        return option;
    }

    // Entity → Response
    public ProductOptionResponse toDto(ProductOption option) {
        log.debug("Converting ProductOption entity to ProductOptionResponse: {}", option.getId());
        ProductOptionResponse dto = new ProductOptionResponse();
        dto.setId(option.getId());
        dto.setName(option.getName());
        dto.setType(option.getType());
        dto.setAdditionalPrice(option.getAdditionalPrice());
        dto.setProductId(option.getProduct().getId());
        dto.setCreatedAt(option.getCreatedAt());
        dto.setUpdatedAt(option.getUpdatedAt());

        // SELECT 타입인 경우 옵션 값 목록 변환
        if (option.getType() == ProductOption.OptionType.SELECT && option.getOptionValues() != null) {
            dto.setOptionValues(option.getOptionValues().stream()
                .map(OptionValue::getValue)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    // Entity 리스트 → Response 리스트
    public List<ProductOptionResponse> toDtoList(List<ProductOption> options) {
        return options.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    // Request의 옵션 값 목록 → OptionValue 엔티티 목록
    public List<OptionValue> toOptionValueEntities(ProductOptionRequest request, ProductOption option) {
        if (request.getOptionValues() == null || request.getType() != ProductOption.OptionType.SELECT) {
            return new ArrayList<>();
        }

        return request.getOptionValues().stream()
            .map(value -> {
                OptionValue optionValue = new OptionValue();
                optionValue.setValue(value);
                optionValue.setProductOption(option);
                return optionValue;
            })
            .collect(Collectors.toList());
    }
}