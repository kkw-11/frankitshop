package com.shop.frankit.controller;

import com.shop.frankit.dto.ProductOptionRequest;
import com.shop.frankit.dto.ProductOptionResponse;
import com.shop.frankit.security.UserDetailsImpl;
import com.shop.frankit.service.ProductOptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products/{productId}/options")
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    @Autowired
    public ProductOptionController(ProductOptionService productOptionService) {
        this.productOptionService = productOptionService;
    }

    @GetMapping
    public ResponseEntity<List<ProductOptionResponse>> getAllOptions(@PathVariable Long productId) {
        log.info("Get all options for product: id={}", productId);
        List<ProductOptionResponse> options = productOptionService.findByProductId(productId);
        return ResponseEntity.ok(options);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductOptionResponse> getOptionById(
        @PathVariable Long productId,
        @PathVariable Long id) {

        log.info("Get option by ID: productId={}, optionId={}", productId, id);
        ProductOptionResponse option = productOptionService.findByIdAndProductId(id, productId);
        return ResponseEntity.ok(option);
    }

    @PostMapping
    public ResponseEntity<ProductOptionResponse> createOption(
        @PathVariable Long productId,
        @Valid @RequestBody ProductOptionRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Create option request for product: id={}, user={}", productId, userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        ProductOptionResponse option = productOptionService.create(request, productId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(option);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductOptionResponse> updateOption(
        @PathVariable Long productId,
        @PathVariable Long id,
        @Valid @RequestBody ProductOptionRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Update option request: productId={}, optionId={}, user={}",
            productId, id, userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        ProductOptionResponse option = productOptionService.update(id, request, productId, userId);
        return ResponseEntity.ok(option);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOption(
        @PathVariable Long productId,
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Delete option request: productId={}, optionId={}, user={}",
            productId, id, userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        productOptionService.delete(id, productId, userId);
        return ResponseEntity.ok(Map.of("message", "상품 옵션이 삭제되었습니다."));
    }
}