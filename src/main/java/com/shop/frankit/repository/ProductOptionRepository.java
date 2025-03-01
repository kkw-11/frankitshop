package com.shop.frankit.repository;

import com.shop.frankit.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    // 특정 상품의 모든 옵션 조회
    List<ProductOption> findByProductId(Long productId);

    // 특정 상품의 특정 옵션 조회
    Optional<ProductOption> findByIdAndProductId(Long id, Long productId);

    // 특정 상품의 옵션 개수 조회
    long countByProductId(Long productId);
}