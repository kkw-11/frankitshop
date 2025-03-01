package com.shop.frankit.service;

import com.shop.frankit.dto.ProductOptionRequest;
import com.shop.frankit.dto.ProductOptionResponse;
import com.shop.frankit.dto.ProductRequest;
import com.shop.frankit.dto.ProductResponse;
import com.shop.frankit.entity.ProductOption;
import com.shop.frankit.entity.User;
import com.shop.frankit.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class ProductOptionServiceTest {

    @Autowired
    private ProductOptionService productOptionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User();
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        testUser.setRole("USER");
        userRepository.save(testUser);
        log.info("Test user created with id: {}", testUser.getId());

        // 테스트용 상품 생성
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("테스트 상품");
        productRequest.setDescription("테스트 상품 설명");
        productRequest.setPrice(new BigDecimal("10000"));
        productRequest.setShippingFee(new BigDecimal("2500"));

        testProduct = productService.create(productRequest, testUser.getId());
        log.info("Test product created with id: {}", testProduct.getId());
    }

    @Test
    void 옵션_생성_테스트() {
        log.info("Starting option creation test");

        // 입력 타입 옵션 생성 요청
        ProductOptionRequest inputRequest = new ProductOptionRequest();
        inputRequest.setName("각인 텍스트");
        inputRequest.setType(ProductOption.OptionType.INPUT);
        inputRequest.setAdditionalPrice(new BigDecimal("5000"));

        // 옵션 생성
        ProductOptionResponse inputOption = productOptionService.create(inputRequest, testProduct.getId(), testUser.getId());

        // 검증
        assertNotNull(inputOption);
        assertEquals("각인 텍스트", inputOption.getName());
        assertEquals(ProductOption.OptionType.INPUT, inputOption.getType());
        assertEquals(0, new BigDecimal("5000").compareTo(inputOption.getAdditionalPrice()));
        assertEquals(testProduct.getId(), inputOption.getProductId());

        // 선택 타입 옵션 생성 요청
        ProductOptionRequest selectRequest = new ProductOptionRequest();
        selectRequest.setName("색상");
        selectRequest.setType(ProductOption.OptionType.SELECT);
        selectRequest.setAdditionalPrice(new BigDecimal("3000"));
        selectRequest.setOptionValues(Arrays.asList("빨강", "파랑", "검정"));

        // 옵션 생성
        ProductOptionResponse selectOption = productOptionService.create(selectRequest, testProduct.getId(), testUser.getId());

        // 검증
        assertNotNull(selectOption);
        assertEquals("색상", selectOption.getName());
        assertEquals(ProductOption.OptionType.SELECT, selectOption.getType());
        assertEquals(0, new BigDecimal("3000").compareTo(selectOption.getAdditionalPrice()));
        assertEquals(testProduct.getId(), selectOption.getProductId());
        assertNotNull(selectOption.getOptionValues());
        assertEquals(3, selectOption.getOptionValues().size());
        assertTrue(selectOption.getOptionValues().contains("빨강"));
        assertTrue(selectOption.getOptionValues().contains("파랑"));
        assertTrue(selectOption.getOptionValues().contains("검정"));

        log.info("Option creation test passed");
    }

    @Test
    void 옵션_조회_테스트() {
        log.info("Starting option retrieval test");

        // 두 개의 옵션 생성
        ProductOptionRequest request1 = new ProductOptionRequest();
        request1.setName("사이즈");
        request1.setType(ProductOption.OptionType.SELECT);
        request1.setAdditionalPrice(new BigDecimal("2000"));
        request1.setOptionValues(Arrays.asList("S", "M", "L"));

        ProductOptionRequest request2 = new ProductOptionRequest();
        request2.setName("메시지");
        request2.setType(ProductOption.OptionType.INPUT);
        request2.setAdditionalPrice(new BigDecimal("1000"));

        ProductOptionResponse option1 = productOptionService.create(request1, testProduct.getId(), testUser.getId());
        ProductOptionResponse option2 = productOptionService.create(request2, testProduct.getId(), testUser.getId());

        // 특정 옵션 조회
        ProductOptionResponse foundOption = productOptionService.findByIdAndProductId(
            option1.getId(), testProduct.getId());

        // 검증
        assertNotNull(foundOption);
        assertEquals(option1.getId(), foundOption.getId());
        assertEquals("사이즈", foundOption.getName());

        // 모든 옵션 조회
        List<ProductOptionResponse> allOptions = productOptionService.findByProductId(testProduct.getId());

        // 검증
        assertNotNull(allOptions);
        assertEquals(2, allOptions.size());

        log.info("Option retrieval test passed");
    }

    @Test
    void 옵션_수정_테스트() {
        log.info("Starting option update test");

        // 옵션 생성
        ProductOptionRequest createRequest = new ProductOptionRequest();
        createRequest.setName("사이즈");
        createRequest.setType(ProductOption.OptionType.SELECT);
        createRequest.setAdditionalPrice(new BigDecimal("2000"));
        createRequest.setOptionValues(Arrays.asList("S", "M", "L"));

        ProductOptionResponse createdOption = productOptionService.create(
            createRequest, testProduct.getId(), testUser.getId());

        // 옵션 수정 요청
        ProductOptionRequest updateRequest = new ProductOptionRequest();
        updateRequest.setName("업데이트된 사이즈");
        updateRequest.setType(ProductOption.OptionType.SELECT);
        updateRequest.setAdditionalPrice(new BigDecimal("2500"));
        updateRequest.setOptionValues(Arrays.asList("XS", "S", "M", "L", "XL"));

        // 옵션 수정
        ProductOptionResponse updatedOption = productOptionService.update(
            createdOption.getId(), updateRequest, testProduct.getId(), testUser.getId());

        // 검증
        assertNotNull(updatedOption);
        assertEquals(createdOption.getId(), updatedOption.getId());
        assertEquals("업데이트된 사이즈", updatedOption.getName());
        assertEquals(0, new BigDecimal("2500").compareTo(updatedOption.getAdditionalPrice()));
        assertEquals(5, updatedOption.getOptionValues().size());
        assertTrue(updatedOption.getOptionValues().contains("XS"));
        assertTrue(updatedOption.getOptionValues().contains("XL"));

        log.info("Option update test passed");
    }

    @Test
    void 옵션_삭제_테스트() {
        log.info("Starting option deletion test");

        // 옵션 생성
        ProductOptionRequest request = new ProductOptionRequest();
        request.setName("사이즈");
        request.setType(ProductOption.OptionType.SELECT);
        request.setAdditionalPrice(new BigDecimal("2000"));
        request.setOptionValues(Arrays.asList("S", "M", "L"));

        ProductOptionResponse option = productOptionService.create(
            request, testProduct.getId(), testUser.getId());

        // 옵션 삭제
        productOptionService.delete(option.getId(), testProduct.getId(), testUser.getId());

        // 삭제 확인 - 조회 시 예외 발생해야 함
        assertThrows(EntityNotFoundException.class, () -> {
            productOptionService.findByIdAndProductId(option.getId(), testProduct.getId());
        });

        log.info("Option deletion test passed");
    }

    @Test
    void 옵션_최대개수_제한_테스트() {
        log.info("Starting maximum option limit test");

        // 3개의 옵션 생성
        for (int i = 0; i < 3; i++) {
            ProductOptionRequest request = new ProductOptionRequest();
            request.setName("옵션 " + (i + 1));
            request.setType(ProductOption.OptionType.INPUT);
            request.setAdditionalPrice(new BigDecimal("1000"));

            productOptionService.create(request, testProduct.getId(), testUser.getId());
        }

        // 4번째 옵션 생성 시도 - 예외 발생해야 함
        ProductOptionRequest request = new ProductOptionRequest();
        request.setName("옵션 4");
        request.setType(ProductOption.OptionType.INPUT);
        request.setAdditionalPrice(new BigDecimal("1000"));

        assertThrows(IllegalStateException.class, () -> {
            productOptionService.create(request, testProduct.getId(), testUser.getId());
        });

        log.info("Maximum option limit test passed");
    }
}