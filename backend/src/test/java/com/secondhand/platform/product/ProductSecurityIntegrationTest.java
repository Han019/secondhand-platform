package com.secondhand.platform.product;

import com.secondhand.platform.auth.jwt.JwtTokenProvider;
import com.secondhand.platform.productimage.ProductImageRepository;
import com.secondhand.platform.productimage.ProductImage;
import com.secondhand.platform.productimage.ProductImageService;
import com.secondhand.platform.productimage.PendingImageDeletionRepository;
import com.secondhand.platform.user.User;
import com.secondhand.platform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "jwt.secret=product-api-test-secret-key-at-least-32-bytes",
        "resend.api-key=test-key",
        "supabase.endpoint=http://localhost:9000",
        "supabase.region=us-east-1",
        "supabase.access-key=test-key",
        "supabase.secret-key=test-secret",
        "supabase.bucket=test-bucket"
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
    private ProductImageRepository productImageRepository;

    @Autowired
    private PendingImageDeletionRepository pendingImageDeletionRepository;

    @Autowired
    private ProductImageService productImageService;

    @MockitoBean
    private S3Client s3Client;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User seller;
    private String accessToken;

    @BeforeEach
    void setUp() {
        pendingImageDeletionRepository.deleteAll();
        productImageRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        seller = userRepository.save(
                User.create("seller1", "encoded-password", "seller1@example.com", "판매자1")
        );
        accessToken = jwtTokenProvider.issueAccessToken(seller.getId(), seller.getRole().name());
    }

    @Test
    @DisplayName("상품 등록 문서는 JSON 상품 정보와 이미지 파일을 multipart로 안내한다")
    void createProduct_documentsMultipartParts() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/products']['post']['requestBody']['content']['multipart/form-data']['encoding']['product']['contentType']")
                        .value("application/json"))
                .andExpect(jsonPath("$['paths']['/api/products']['post']['requestBody']['content']['multipart/form-data']['schema']['properties']['product']").exists())
                .andExpect(jsonPath("$['paths']['/api/products']['post']['requestBody']['content']['multipart/form-data']['schema']['properties']['image']").exists());
    }

    @Test
    @DisplayName("상품 목록 조회는 인증 없이 접근할 수 있다")
    void getProducts_allowsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상품 목록 문서는 정렬을 별도 쿼리 파라미터로 안내한다")
    void getProducts_documentsSortParameter() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/products']['get']['parameters'][?(@.name == 'sort')]").isNotEmpty());
    }

    @Test
    @DisplayName("상품 검색은 가격순으로 정렬한 결과와 전체 건수를 반환한다")
    void getProducts_searchesAndSortsBeforePaging() throws Exception {
        productRepository.save(new Product(seller, "아이패드 고가", "설명", 200_000L, null, null, "서울"));
        productRepository.save(new Product(seller, "아이패드 저가", "설명", 100_000L, null, null, "서울"));
        productRepository.save(new Product(seller, "노트북", "설명", 50_000L, null, null, "서울"));

        mockMvc.perform(get("/api/products")
                        .param("keyword", "아이패드")
                        .param("sort", "price,asc")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("아이패드 저가"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("존재하지 않는 상품 정렬 필드는 400으로 거부한다")
    void getProducts_rejectsUnknownSortProperty() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("keyword", "자전거")
                        .param("sort", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"));
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
        mockMvc.perform(multipart("/api/products")
                        .file(productPart())
                        .file(imagePart()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 Bearer Token은 401로 거부한다")
    void createProduct_rejectsInvalidAccessToken() throws Exception {
        mockMvc.perform(multipart("/api/products")
                        .header("Authorization", "Bearer invalid-token")
                        .file(productPart())
                        .file(imagePart()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Access Token으로 상품을 등록하면 DB에 저장된다")
    void createProduct_withValidAccessTokenPersistsProduct() throws Exception {
        mockMvc.perform(multipart("/api/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .file(productPart())
                        .file(imagePart()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").isNumber());

        assertThat(productRepository.findAll())
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.getSeller().getId()).isEqualTo(seller.getId());
                    assertThat(product.getTitle()).isEqualTo("자전거");
                    assertThat(product.getPrice()).isEqualTo(100_000L);
                });
        assertThat(productImageRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("상품 소유자는 이미지가 있는 상품을 삭제할 수 있다")
    void deleteProduct_deletesProductAndImages() throws Exception {
        Product product = productRepository.save(new Product(
                seller, "자전거", "설명", 100_000L, null, null, "서울"));
        productImageRepository.save(new ProductImage(product, "products/delete.jpg", 0));

        mockMvc.perform(delete("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(product.getId())).isEmpty();
        assertThat(productImageRepository.findAll()).isEmpty();
        assertThat(pendingImageDeletionRepository.findAll()).isEmpty();
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
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

    @Test
    @DisplayName("S3 삭제가 실패해도 DB 삭제를 확정하고 대기 기록을 재시도한다")
    void deleteImage_retriesAfterS3Failure() throws Exception {
        Product product = productRepository.save(new Product(
                seller, "자전거", "설명", 100_000L, 37.5, 127.0, "서울"));
        ProductImage image = productImageRepository.save(
                new ProductImage(product, "products/retry.jpg", 0));
        doThrow(new IllegalStateException("S3 unavailable"))
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        mockMvc.perform(delete("/api/products/" + product.getId() + "/images/" + image.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertThat(productImageRepository.findById(image.getId())).isEmpty();
        assertThat(pendingImageDeletionRepository.findById("products/retry.jpg")).isPresent();

        reset(s3Client);
        productImageService.retryPendingImageDeletions();
        assertThat(pendingImageDeletionRepository.findById("products/retry.jpg")).isEmpty();
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

    private MockMultipartFile productPart() {
        return new MockMultipartFile("product", "", "application/json", validRequestJson().getBytes());
    }

    private MockMultipartFile imagePart() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return new MockMultipartFile("image", "photo.png", "image/png", bytes.toByteArray());
    }
}
