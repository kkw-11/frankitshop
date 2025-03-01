package com.shop.frankit.controller;

import com.shop.frankit.dto.ProductRequest;
import com.shop.frankit.dto.ProductResponse;
import com.shop.frankit.security.UserDetailsImpl;
import com.shop.frankit.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sort) {

        log.info("Get all products request: page={}, size={}, sort={}", page, size, sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ProductResponse> products = productService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("Get product by ID request: id={}", id);
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
        @RequestParam String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        log.info("Search products request: name={}, page={}, size={}", name, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchByName(name, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Create product request received from user: {}", userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        ProductResponse product = productService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Update product request: id={}, user={}", id, userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        ProductResponse product = productService.update(id, request, userId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Delete product request: id={}, user={}", id, userDetails.getUsername());
        Long userId = ((UserDetailsImpl) userDetails).getId();
        productService.delete(id, userId);
        return ResponseEntity.ok(Map.of("message", "상품이 삭제되었습니다."));
    }
}