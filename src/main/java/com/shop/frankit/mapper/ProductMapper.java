package com.shop.frankit.mapper;

import com.shop.frankit.dto.ProductRequest;
import com.shop.frankit.dto.ProductResponse;
import com.shop.frankit.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ProductMapper {

    // Request → Entity
    public Product toEntity(ProductRequest dto) {
        log.debug("Converting ProductRequest to Product entity: {}", dto);
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setShippingFee(dto.getShippingFee());
        product.setRegisteredAt(LocalDateTime.now());
        return product;
    }

    // Request → Entity 업데이트
    public void updateFromDto(Product product, ProductRequest dto) {
        log.debug("Updating Product entity from ProductRequest: {}", dto);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setShippingFee(dto.getShippingFee());
    }

    // Entity → Response
    public ProductResponse toDto(Product product) {
        log.debug("Converting Product entity to ProductResponse: {}", product.getId());
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setShippingFee(product.getShippingFee());
        dto.setRegisteredAt(product.getRegisteredAt());
        dto.setUserId(product.getUser().getId());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    // Page<Entity> → Page<Response>
    public Page<ProductResponse> toDtoPage(Page<Product> productPage) {
        log.debug("Converting Page<Product> to Page<ProductResponse>, total elements: {}", productPage.getTotalElements());
        return productPage.map(this::toDto);
    }
}
