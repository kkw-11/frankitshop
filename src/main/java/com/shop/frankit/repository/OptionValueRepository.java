package com.shop.frankit.repository;

import com.shop.frankit.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {

    // 특정 상품 옵션의 모든 옵션 값 조회
    List<OptionValue> findByProductOptionId(Long productOptionId);

    // 특정 상품 옵션의 모든 옵션 값 삭제
    void deleteByProductOptionId(Long productOptionId);
}