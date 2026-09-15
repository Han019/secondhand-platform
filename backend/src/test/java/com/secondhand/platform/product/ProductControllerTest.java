package com.secondhand.platform.product;

import com.secondhand.platform.product.dto.ProductDetailResponse;
import com.secondhand.platform.product.dto.ProductResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProductController(productService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/products는 상품을 등록하고 201을 반환한다")
    void createProduct_returnsCreated() throws Exception {
        authenticate(1L);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(productService).createProduct(any(), eq(1L));
    }

    @Test
    @DisplayName("GET /api/products는 상품 목록을 반환한다")
    void getProducts_returnsProducts() throws Exception {
        when(productService.getProducts()).thenReturn(List.of(
                new ProductResponse(10L, "자전거", 100_000L, ProductStatus.ON_SALE, "서울시 동대문구")
        ));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("자전거"))
                .andExpect(jsonPath("$[0].price").value(100_000))
                .andExpect(jsonPath("$[0].status").value("ON_SALE"));
    }

    @Test
    @DisplayName("상품 목록이 없으면 빈 배열을 반환한다")
    void getProducts_returnsEmptyArray() throws Exception {
        when(productService.getProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}는 상품을 수정하고 204를 반환한다")
    void updateProduct_returnsNoContent() throws Exception {
        authenticate(1L);

        mockMvc.perform(patch("/api/products/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson()))
                .andExpect(status().isNoContent());

        verify(productService).updateProduct(any(), eq(1L), eq(10L));
    }

    @Test
    @DisplayName("DELETE /api/products/{id}는 상품을 삭제하고 204를 반환한다")
    void deleteProduct_returnsNoContent() throws Exception {
        authenticate(1L);

        mockMvc.perform(delete("/api/products/10"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L, 10L);
    }

    @Test
    @DisplayName("GET /api/products/{id}는 상품 단건을 반환한다")
    void getProduct_returnsProduct() throws Exception {
        when(productService.getProduct(10L)).thenReturn(
                new ProductDetailResponse(
                        10L,
                        "자전거",
                        "상태 좋습니다",
                        100_000L,
                        ProductStatus.ON_SALE,
                        37.5665,
                        126.9780,
                        "서울시 동대문구",
                        null,
                        null,
                        new ProductDetailResponse.SellerResponse(1L, "판매자", "profile.jpg", 36.5)
                )
        );

        mockMvc.perform(get("/api/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("자전거"))
                .andExpect(jsonPath("$.description").value("상태 좋습니다"))
                .andExpect(jsonPath("$.price").value(100_000))
                .andExpect(jsonPath("$.status").value("ON_SALE"))
                .andExpect(jsonPath("$.latitude").value(37.5665))
                .andExpect(jsonPath("$.longitude").value(126.9780))
                .andExpect(jsonPath("$.address").value("서울시 동대문구"))
                .andExpect(jsonPath("$.seller.id").value(1))
                .andExpect(jsonPath("$.seller.nickname").value("판매자"))
                .andExpect(jsonPath("$.seller.profileImage").value("profile.jpg"))
                .andExpect(jsonPath("$.seller.mannerScore").value(36.5));

        verify(productService).getProduct(10L);
    }

    @Test
    @DisplayName("필수값이 비거나 가격이 음수면 400을 반환한다")
    void createProduct_rejectsInvalidBody() throws Exception {
        authenticate(1L);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "",
                                  "price": -1,
                                  "latitude": 37.5665,
                                  "longitude": 126.9780,
                                  "address": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 JSON 요청은 400을 반환한다")
    void createProduct_rejectsMalformedJson() throws Exception {
        authenticate(1L);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/status는 판매 상태만 변경하고 204를 반환한다")
    void changeStatus_returnsNoContent() throws Exception {
        authenticate(1L);

        mockMvc.perform(patch("/api/products/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESERVED\"}"))
                .andExpect(status().isNoContent());

        verify(productService).changeStatus(any(), eq(1L), eq(10L));
    }

    @Test
    @DisplayName("판매 상태가 없으면 400을 반환한다")
    void changeStatus_rejectsMissingStatus() throws Exception {
        authenticate(1L);

        mockMvc.perform(patch("/api/products/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private void authenticate(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
        SecurityContextHolder.setContext(context);
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

    private String updateRequestJson() {
        return """
                {
                  "title": "수정된 자전거",
                  "description": "수정된 설명",
                  "price": 90000
                }
                """;
    }
}
