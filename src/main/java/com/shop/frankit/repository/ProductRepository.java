package com.shop.frankit.repository;

import com.shop.frankit.entity.Product;
import com.shop.frankit.entity.User;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * 특정 사용자의 상품만 페이징해서 조회
     */
    Page<Product> findByUser(User user, Pageable pageable);

    /**
     * 상품 이름으로 검색 (부분 일치)
     */
    Page<Product> findByNameContaining(String name, Pageable pageable);
}
