package com.shop.frankit.service;

import static org.junit.jupiter.api.Assertions.*;
import com.shop.frankit.dto.ProductRequest;
import com.shop.frankit.dto.ProductResponse;
import com.shop.frankit.entity.User;
import com.shop.frankit.repository.ProductRepository;
import com.shop.frankit.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private ProductRequest productRequest;
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User();
        testUser.setEmail("test" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        testUser.setRole("USER");
        userRepository.save(testUser);
        log.info("테스트 사용자가 생성되었습니다. ID: {}", testUser.getId());

        // 테스트용 상품 요청 준비
        productRequest = new ProductRequest();
        productRequest.setName("테스트 상품");
        productRequest.setDescription("테스트 상품 설명");
        productRequest.setPrice(new BigDecimal("10000"));
        productRequest.setShippingFee(new BigDecimal("2500"));
        log.info("테스트 상품 요청이 준비되었습니다");
    }

    @Test
    @DisplayName("상품 생성 기능 테스트")
    void testCreateProduct() {
        log.info("상품 생성 테스트 시작");
        // 상품 생성
        ProductResponse response = productService.create(productRequest, testUser.getId());

        // 검증
        assertNotNull(response.getId());
        assertEquals("테스트 상품", response.getName());
        assertEquals(testUser.getId(), response.getUserId());
        assertNotNull(response.getRegisteredAt());
        log.info("상품 생성 테스트 통과, 생성된 상품 ID: {}", response.getId());
    }

    @Test
    @DisplayName("상품 단일 조회 기능 테스트")
    void testFindProductById() {
        log.info("상품 조회 테스트 시작");
        // 먼저 상품 저장
        ProductResponse savedResponse = productService.create(productRequest, testUser.getId());
        log.debug("조회 테스트용 상품이 생성되었습니다. ID: {}", savedResponse.getId());

        // 상품 조회
        ProductResponse foundResponse = productService.findById(savedResponse.getId());

        // 검증
        assertNotNull(foundResponse);
        assertEquals(savedResponse.getId(), foundResponse.getId());
        assertEquals("테스트 상품", foundResponse.getName());
        assertEquals(0, new BigDecimal("10000").compareTo(foundResponse.getPrice()));
        log.info("상품 조회 테스트 통과");
    }

    @Test
    @DisplayName("상품 정보 수정 기능 테스트")
    void testUpdateProduct() {
        log.info("상품 수정 테스트 시작");
        // 먼저 상품 저장
        ProductResponse savedResponse = productService.create(productRequest, testUser.getId());
        log.debug("수정 테스트용 상품이 생성되었습니다. ID: {}", savedResponse.getId());

        // 수정할 데이터 준비
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("수정된 상품");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setPrice(new BigDecimal("15000"));
        updateRequest.setShippingFee(new BigDecimal("0"));

        // 상품 수정
        ProductResponse updatedResponse = productService.update(savedResponse.getId(), updateRequest, testUser.getId());

        // 검증
        assertEquals("수정된 상품", updatedResponse.getName());
        assertEquals("수정된 설명", updatedResponse.getDescription());
        assertEquals(0, new BigDecimal("15000").compareTo(updatedResponse.getPrice()));
        assertEquals(0, new BigDecimal("0").compareTo(updatedResponse.getShippingFee()));
        log.info("상품 수정 테스트 통과");
    }

    @Test
    @DisplayName("상품 목록 페이징 조회 기능 테스트")
    void testFindAllProductsWithPagination() {
        log.info("상품 목록 조회 테스트 시작");

        // 여러 개의 테스트 상품 등록 (페이징 테스트를 위해)
        for (int i = 0; i < 3; i++) {
            ProductRequest newRequest = new ProductRequest();
            newRequest.setName("테스트 상품 " + (i + 1));
            newRequest.setDescription("테스트 상품 설명 " + (i + 1));
            newRequest.setPrice(new BigDecimal("1000" + i));
            newRequest.setShippingFee(new BigDecimal("200" + i));

            productService.create(newRequest, testUser.getId());
            log.debug("{}번 상품 생성 완료", i + 1);
        }

        // 페이징 요청 생성 (첫 페이지, 페이지당 2개 항목)
        PageRequest pageRequest = PageRequest.of(0, 2);

        // 상품 목록 조회
        Page<ProductResponse> productPage = productService.findAll(pageRequest);

        // 검증
        assertNotNull(productPage);
        assertEquals(2, productPage.getContent().size());  // 페이지 크기 확인
        assertEquals(0, productPage.getNumber());          // 페이지 번호 확인
        assertTrue(productPage.getTotalElements() >= 3);   // 전체 항목 수 확인

        log.info("상품 목록 조회 테스트 통과, 총 {}개의 상품이 조회됨",
            productPage.getTotalElements());
    }

    @Test
    @DisplayName("상품 이름 기반 검색 기능 테스트")
    void testSearchProductsByName() {
        log.info("상품 이름 검색 테스트 시작");

        // 다양한 이름을 가진 테스트 상품 등록
        String[] productNames = {"아이폰 케이스", "갤럭시 케이스", "아이패드 커버", "스마트폰 충전기", "아이폰 충전기"};
        for (String name : productNames) {
            ProductRequest newRequest = new ProductRequest();
            newRequest.setName(name);
            newRequest.setDescription(name + " 상세 설명");
            newRequest.setPrice(new BigDecimal("10000"));
            newRequest.setShippingFee(new BigDecimal("2500"));

            productService.create(newRequest, testUser.getId());
            log.debug("이름이 '{}'인 상품 생성 완료", name);
        }

        // 페이징 요청 생성
        PageRequest pageRequest = PageRequest.of(0, 10);

        // 케이스 검색
        String searchKeyword = "케이스";
        Page<ProductResponse> caseResults = productService.searchByName(searchKeyword, pageRequest);

        // 아이폰 검색
        String searchKeyword2 = "아이폰";
        Page<ProductResponse> iphoneResults = productService.searchByName(searchKeyword2, pageRequest);

        // 검증
        assertNotNull(caseResults);
        assertEquals(2, caseResults.getTotalElements());  // "아이폰 케이스", "갤럭시 케이스" 2개

        assertNotNull(iphoneResults);
        assertEquals(2, iphoneResults.getTotalElements());  // "아이폰 케이스", "아이폰 충전기" 2개

        // 검색 결과의 이름이 검색어를 포함하는지 확인
        for (ProductResponse product : caseResults.getContent()) {
            assertTrue(product.getName().contains(searchKeyword),
                "Product name should contain search keyword");
        }

        for (ProductResponse product : iphoneResults.getContent()) {
            assertTrue(product.getName().contains(searchKeyword2),
                "Product name should contain search keyword");
        }

        log.info("상품 이름 검색 테스트 통과 - '{}' 키워드로 {}개 상품, '{}' 키워드로 {}개 상품 검색됨",
            searchKeyword, caseResults.getTotalElements(),
            searchKeyword2, iphoneResults.getTotalElements());
    }

    @Test
    @DisplayName("상품 삭제 기능 테스트")
    void testDeleteProduct() {
        log.info("상품 삭제 테스트 시작");
        // 먼저 상품 저장
        ProductResponse savedResponse = productService.create(productRequest, testUser.getId());
        log.debug("삭제 테스트용 상품이 생성되었습니다. ID: {}", savedResponse.getId());

        // 상품 삭제
        productService.delete(savedResponse.getId(), testUser.getId());
        log.debug("상품 삭제 실행 완료");

        // 삭제 후 조회 시 예외 발생하는지 확인
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            productService.findById(savedResponse.getId());
        });

        log.info("상품 삭제 테스트 통과, 예외 메시지: {}", exception.getMessage());
    }
}