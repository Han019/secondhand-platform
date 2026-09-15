package com.secondhand.platform.product;

import com.secondhand.platform.auth.jwt.JwtTokenProvider;
import com.secondhand.platform.user.User;
import com.secondhand.platform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "jwt.secret=product-api-test-secret-key-at-least-32-bytes",
        "resend.api-key=test-key"
})
@AutoConfigureMockMvc
class ProductSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User seller;
    private String accessToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        seller = userRepository.save(
                User.create("seller1", "encoded-password", "seller1@example.com", "판매자1")
        );
        accessToken = jwtTokenProvider.issueAccessToken(seller.getId(), seller.getRole().name());
    }

    @Test
    @DisplayName("상품 목록 조회는 인증 없이 접근할 수 있다")
    void getProducts_allowsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 단건 조회는 상세 정보와 판매자의 공개 정보만 반환한다")
    void getProduct_returnsDetailWithPublicSellerInformation() throws Exception {
        Product product = productRepository.save(new Product(
                seller,
                "자전거",
                "상태 좋습니다",
                100_000L,
                37.5665,
                126.9780,
                "서울시 동대문구"
        ));

        mockMvc.perform(get("/api/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.title").value("자전거"))
                .andExpect(jsonPath("$.description").value("상태 좋습니다"))
                .andExpect(jsonPath("$.price").value(100_000))
                .andExpect(jsonPath("$.status").value("ON_SALE"))
                .andExpect(jsonPath("$.latitude").value(37.5665))
                .andExpect(jsonPath("$.longitude").value(126.9780))
                .andExpect(jsonPath("$.address").value("서울시 동대문구"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.seller.id").value(seller.getId()))
                .andExpect(jsonPath("$.seller.nickname").value("판매자1"))
                .andExpect(jsonPath("$.seller.mannerScore").value(36.5))
                .andExpect(jsonPath("$.seller.loginId").doesNotExist())
                .andExpect(jsonPath("$.seller.email").doesNotExist())
                .andExpect(jsonPath("$.seller.password").doesNotExist());
    }

    @Test
    @DisplayName("상품 등록은 인증되지 않은 요청을 401로 거부한다")
    void createProduct_rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 Bearer Token은 401로 거부한다")
    void createProduct_rejectsInvalidAccessToken() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Access Token으로 상품을 등록하면 DB에 저장된다")
    void createProduct_withValidAccessTokenPersistsProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated());

        assertThat(productRepository.findAll())
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.getSeller().getId()).isEqualTo(seller.getId());
                    assertThat(product.getTitle()).isEqualTo("자전거");
                    assertThat(product.getPrice()).isEqualTo(100_000L);
                });
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제는 404를 반환한다")
    void deleteProduct_returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(delete("/api/products/999999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 판매자의 상품 삭제는 403을 반환한다")
    void deleteProduct_returnsForbiddenForNonOwner() throws Exception {
        User otherSeller = userRepository.save(
                User.create("seller2", "encoded-password", "seller2@example.com", "판매자2")
        );
        Product product = productRepository.save(new Product(
                otherSeller,
                "노트북",
                "다른 판매자의 상품",
                500_000L,
                37.5665,
                126.9780,
                "서울시 동대문구"
        ));

        mockMvc.perform(delete("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    private String validRequestJson() {
        return """
                {
                  "title": "자전거",
                  "description": "상태 좋습니다",
                  "price": 100000,
                  "latitude": 37.5665,
                  "longitude": 126.9780,
                  "address": "서울시 동대문구"
                }
                """;
    }
}
