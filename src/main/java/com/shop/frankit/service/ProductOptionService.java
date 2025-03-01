package com.shop.frankit.service;

import com.shop.frankit.dto.ProductOptionRequest;
import com.shop.frankit.dto.ProductOptionResponse;
import com.shop.frankit.entity.OptionValue;
import com.shop.frankit.entity.Product;
import com.shop.frankit.entity.ProductOption;
import com.shop.frankit.mapper.ProductOptionMapper;
import com.shop.frankit.repository.OptionValueRepository;
import com.shop.frankit.repository.ProductOptionRepository;
import com.shop.frankit.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductOptionMapper productOptionMapper;

    /**
     * 특정 상품의 모든 옵션 조회
     */
    @Transactional(readOnly = true)
    public List<ProductOptionResponse> findByProductId(Long productId) {
        log.info("Finding all options for product: {}", productId);
        List<ProductOption> options = productOptionRepository.findByProductId(productId);
        log.debug("Found {} options for product {}", options.size(), productId);
        return productOptionMapper.toDtoList(options);
    }

    /**
     * 특정 상품의 특정 옵션 조회
     */
    @Transactional(readOnly = true)
    public ProductOptionResponse findByIdAndProductId(Long id, Long productId) {
        log.info("Finding option {} for product {}", id, productId);
        ProductOption option = productOptionRepository.findByIdAndProductId(id, productId)
            .orElseThrow(() -> {
                log.error("Option not found: id={}, productId={}", id, productId);
                return new EntityNotFoundException("상품 옵션을 찾을 수 없습니다. 옵션 ID: " + id);
            });
        return productOptionMapper.toDto(option);
    }

    /**
     * 새 상품 옵션 등록
     */
    @Transactional
    public ProductOptionResponse create(ProductOptionRequest request, Long productId, Long userId) {
        log.info("Creating new option for product: {}", productId);

        // 상품 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", productId);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + productId);
            });

        // 상품 소유자 확인
        if (!product.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to add option to product {} owned by user {}",
                userId, productId, product.getUser().getId());
            throw new AccessDeniedException("이 상품에 옵션을 추가할 권한이 없습니다.");
        }

        // 상품당 최대 3개의 옵션만 허용
        long optionCount = productOptionRepository.countByProductId(productId);
        if (optionCount >= 3) {
            log.warn("Cannot add more than 3 options to product {}", productId);
            throw new IllegalStateException("상품당 최대 3개의 옵션만 추가할 수 있습니다.");
        }

        // 옵션 엔티티 생성
        ProductOption option = productOptionMapper.toEntity(request);
        option.setProduct(product);

        // 저장
        ProductOption savedOption = productOptionRepository.save(option);
        log.info("Option created successfully with id: {}", savedOption.getId());

        // SELECT 타입인 경우 옵션 값 저장
        if (savedOption.getType() == ProductOption.OptionType.SELECT && request.getOptionValues() != null) {
            List<OptionValue> optionValues = productOptionMapper.toOptionValueEntities(request, savedOption);
            optionValueRepository.saveAll(optionValues);
            savedOption.setOptionValues(optionValues);
            log.debug("Saved {} option values for option {}", optionValues.size(), savedOption.getId());
        }

        return productOptionMapper.toDto(savedOption);
    }

    /**
     * 상품 옵션 수정
     */
    @Transactional
    public ProductOptionResponse update(Long id, ProductOptionRequest request, Long productId, Long userId) {
        log.info("Updating option {} for product {}", id, productId);

        // 상품 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", productId);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + productId);
            });

        // 상품 소유자 확인
        if (!product.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to update option of product {} owned by user {}",
                userId, productId, product.getUser().getId());
            throw new AccessDeniedException("이 상품의 옵션을 수정할 권한이 없습니다.");
        }

        // 옵션 조회
        ProductOption option = productOptionRepository.findByIdAndProductId(id, productId)
            .orElseThrow(() -> {
                log.error("Option not found: id={}, productId={}", id, productId);
                return new EntityNotFoundException("상품 옵션을 찾을 수 없습니다. 옵션 ID: " + id);
            });

        // 옵션 업데이트
        option.setName(request.getName());
        option.setType(request.getType());
        option.setAdditionalPrice(request.getAdditionalPrice());

        // SELECT 타입인 경우 옵션 값 업데이트
        if (option.getType() == ProductOption.OptionType.SELECT) {
            // 기존 옵션 값 삭제
            optionValueRepository.deleteByProductOptionId(option.getId());

            // 새 옵션 값 추가
            if (request.getOptionValues() != null) {
                List<OptionValue> optionValues = productOptionMapper.toOptionValueEntities(request, option);
                optionValueRepository.saveAll(optionValues);
                option.setOptionValues(optionValues);
                log.debug("Updated {} option values for option {}", optionValues.size(), option.getId());
            }
        }

        // 저장
        ProductOption updatedOption = productOptionRepository.save(option);
        log.info("Option updated successfully: {}", updatedOption.getId());

        return productOptionMapper.toDto(updatedOption);
    }

    /**
     * 상품 옵션 삭제
     */
    @Transactional
    public void delete(Long id, Long productId, Long userId) {
        log.info("Deleting option {} for product {}", id, productId);

        // 상품 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> {
                log.error("Product not found with id: {}", productId);
                return new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + productId);
            });

        // 상품 소유자 확인
        if (!product.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to delete option of product {} owned by user {}",
                userId, productId, product.getUser().getId());
            throw new AccessDeniedException("이 상품의 옵션을 삭제할 권한이 없습니다.");
        }

        // 옵션 조회
        ProductOption option = productOptionRepository.findByIdAndProductId(id, productId)
            .orElseThrow(() -> {
                log.error("Option not found: id={}, productId={}", id, productId);
                return new EntityNotFoundException("상품 옵션을 찾을 수 없습니다. 옵션 ID: " + id);
            });

        // 삭제 (외래 키 제약 조건으로 인해 옵션 값도 자동 삭제됨)
        productOptionRepository.delete(option);
        log.info("Option deleted successfully: {}", id);
    }
}