package com.shop.frankit.service;

import com.shop.frankit.dto.ProductRequest;
import com.shop.frankit.dto.ProductResponse;
import com.shop.frankit.entity.Product;
import com.shop.frankit.entity.User;
import com.shop.frankit.mapper.ProductMapper;
import com.shop.frankit.repository.ProductRepository;
import com.shop.frankit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
 import jakarta.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    /**
     * 모든 상품을 페이징하여 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        log.info("Finding all products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = productRepository.findAll(pageable);
        log.debug("Found {} products", productPage.getTotalElements());
        return productMapper.toDtoPage(productPage);
    }

    /**
     * 특정 ID의 상품 조회
     */
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        log.info("Finding product by id: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", id);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + id);
            });
        log.debug("Found product: {}", product.getName());
        return productMapper.toDto(product);
    }

    /**
     * 특정 사용자의 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> findByUser(Long userId, Pageable pageable) {
        log.info("Finding products by user id: {}, page={}, size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with id: {}", userId);
                return new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
            });
        Page<Product> productPage = productRepository.findByUser(user, pageable);
        log.debug("Found {} products for user: {}", productPage.getTotalElements(), userId);
        return productMapper.toDtoPage(productPage);
    }

    /**
     * 새 상품 등록
     */
    @Transactional
    public ProductResponse create(ProductRequest requestDto, Long userId) {
        log.info("Creating new product for user: {}", userId);
        log.debug("Product request: {}", requestDto);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("User not found with id: {}", userId);
                return new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
            });

        // DTO → Entity 변환
        Product product = productMapper.toEntity(requestDto);
        product.setUser(user);

        // 저장
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());

        // Entity → DTO 변환 후 반환
        return productMapper.toDto(savedProduct);
    }

    /**
     * 상품 정보 수정
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest requestDto, Long userId) {
        log.info("Updating product with id: {} for user: {}", id, userId);
        log.debug("Product update request: {}", requestDto);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", id);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + id);
            });

        // 상품 소유자 확인
        if (!product.getUser().getId().equals(userId)) {
            log.warn("Authentication failed: User {} attempted to update product {} owned by user {}", userId, id, product.getUser().getId());
            throw new AccessDeniedException("이 상품을 수정할 권한이 없습니다.");
        }

        // DTO 정보로 Entity 업데이트
        productMapper.updateFromDto(product, requestDto);

        // 저장
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", updatedProduct.getId());

        // Entity → DTO 변환 후 반환
        return productMapper.toDto(updatedProduct);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void delete(Long id, Long userId) {
        log.info("Deleting product with id: {} for user: {}", id, userId);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", id);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + id);
            });

        // 상품 소유자 확인
        if (!product.getUser().getId().equals(userId)) {
            log.warn("Authentication failed: User {} attempted to delete product {} owned by user {}", userId, id, product.getUser().getId());
            throw new AccessDeniedException("이 상품을 삭제할 권한이 없습니다.");
        }

        productRepository.delete(product);
        log.info("Product deleted successfully: {}", id);
    }

    /**
     * 상품 이름으로 검색
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchByName(String name, Pageable pageable) {
        log.info("Searching products by name containing: '{}', page={}, size={}", name, pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = productRepository.findByNameContaining(name, pageable);
        log.debug("Found {} products matching search criteria", productPage.getTotalElements());
        return productMapper.toDtoPage(productPage);
    }
}
