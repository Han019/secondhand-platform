package com.secondhand.platform.product;

import com.secondhand.platform.common.exception.ProductAccessDeniedException;
import com.secondhand.platform.common.exception.ProductNotFoundException;
import com.secondhand.platform.product.dto.ProductCreateRequest;
import com.secondhand.platform.product.dto.ProductDetailResponse;
import com.secondhand.platform.product.dto.ProductResponse;
import com.secondhand.platform.product.dto.ProductStatusRequest;
import com.secondhand.platform.product.dto.ProductUpdateRequest;
import com.secondhand.platform.user.User;
import com.secondhand.platform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private User seller;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(userRepository, productRepository);
    }

    @Test
    @DisplayName("상품 등록 시 로그인 사용자를 판매자로 저장한다")
    void createProduct_savesProductWithAuthenticatedSeller() {
        ProductCreateRequest request = request("자전거", "상태 좋습니다", 100_000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(seller));

        productService.createProduct(request, 1L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertThat(saved.getSeller()).isSameAs(seller);
        assertThat(saved.getTitle()).isEqualTo("자전거");
        assertThat(saved.getDescription()).isEqualTo("상태 좋습니다");
        assertThat(saved.getPrice()).isEqualTo(100_000L);
        assertThat(saved.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(saved.getAddress()).isEqualTo("서울시 동대문구");
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 상품을 등록할 수 없다")
    void createProduct_rejectsUnknownSeller() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request("상품", "설명", 1_000L), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("없는 사용자입니다.");

        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("판매자는 자신의 상품을 수정할 수 있다")
    void updateProduct_updatesOwnedProduct() {
        when(seller.getId()).thenReturn(1L);
        Product product = product("기존 제목", "기존 설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.updateProduct(updateRequest("수정 제목", "수정 설명", 20_000L), 1L, 10L);

        assertThat(product.getTitle()).isEqualTo("수정 제목");
        assertThat(product.getDescription()).isEqualTo("수정 설명");
        assertThat(product.getPrice()).isEqualTo(20_000L);
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 상품은 수정할 수 없다")
    void updateProduct_rejectsUnknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(updateRequest("제목", "설명", 1_000L), 1L, 999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("없는 상품입니다. productId: 999");
    }

    @Test
    @DisplayName("다른 판매자의 상품은 수정할 수 없다")
    void updateProduct_rejectsNonOwner() {
        when(seller.getId()).thenReturn(2L);
        Product product = product("기존 제목", "기존 설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(updateRequest("수정 제목", "수정 설명", 20_000L), 1L, 10L))
                .isInstanceOf(ProductAccessDeniedException.class)
                .hasMessage("수정 권한이 없습니다.");

        assertThat(product.getTitle()).isEqualTo("기존 제목");
        assertThat(product.getPrice()).isEqualTo(10_000L);
    }

    @Test
    @DisplayName("판매자는 자신의 상품을 삭제할 수 있다")
    void deleteProduct_deletesOwnedProduct() {
        when(seller.getId()).thenReturn(1L);
        Product product = product("상품", "설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L, 10L);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("존재하지 않는 상품은 삭제할 수 없다")
    void deleteProduct_rejectsUnknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L, 999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("없는 상품입니다. productId: 999");

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 판매자의 상품은 삭제할 수 없다")
    void deleteProduct_rejectsNonOwner() {
        when(seller.getId()).thenReturn(2L);
        Product product = product("상품", "설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(1L, 10L))
                .isInstanceOf(ProductAccessDeniedException.class)
                .hasMessage("삭제 권한이 없습니다.");

        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품 목록을 응답 DTO로 변환한다")
    void getProducts_mapsProductsToResponses() {
        Product first = product("자전거", "설명1", 100_000L);
        Product second = product("노트북", "설명2", 500_000L);
        when(productRepository.findAll()).thenReturn(List.of(first, second));

        List<ProductResponse> responses = productService.getProducts();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ProductResponse::title)
                .containsExactly("자전거", "노트북");
    }

    @Test
    @DisplayName("등록된 상품이 없으면 빈 목록을 반환한다")
    void getProducts_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertThat(productService.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("상품 단건을 응답 DTO로 변환한다")
    void getProduct_mapsProductToResponse() {
        when(seller.getId()).thenReturn(1L);
        when(seller.getNickname()).thenReturn("판매자");
        when(seller.getProfileImage()).thenReturn("profile.jpg");
        when(seller.getMannerScore()).thenReturn(36.5);
        Product product = product("자전거", "상태 좋습니다", 100_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductDetailResponse response = productService.getProduct(10L);

        assertThat(response.title()).isEqualTo("자전거");
        assertThat(response.description()).isEqualTo("상태 좋습니다");
        assertThat(response.price()).isEqualTo(100_000L);
        assertThat(response.status()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(response.latitude()).isEqualTo(37.5665);
        assertThat(response.longitude()).isEqualTo(126.9780);
        assertThat(response.address()).isEqualTo("서울시 동대문구");
        assertThat(response.seller().id()).isEqualTo(1L);
        assertThat(response.seller().nickname()).isEqualTo("판매자");
        assertThat(response.seller().profileImage()).isEqualTo("profile.jpg");
        assertThat(response.seller().mannerScore()).isEqualTo(36.5);
    }

    @Test
    @DisplayName("존재하지 않는 상품 단건 조회는 실패한다")
    void getProduct_rejectsUnknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("없는 상품입니다. productId: 999");
    }

    @Test
    @DisplayName("판매자는 자신의 상품 상태만 변경할 수 있다")
    void changeStatus_changesOwnedProductStatus() {
        when(seller.getId()).thenReturn(1L);
        Product product = product("상품", "설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.changeStatus(new ProductStatusRequest(ProductStatus.RESERVED), 1L, 10L);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.RESERVED);
        assertThat(product.getTitle()).isEqualTo("상품");
        assertThat(product.getPrice()).isEqualTo(10_000L);
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 상품의 상태는 변경할 수 없다")
    void changeStatus_rejectsUnknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.changeStatus(
                new ProductStatusRequest(ProductStatus.RESERVED), 1L, 999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("없는 상품입니다. productId: 999");
    }

    @Test
    @DisplayName("다른 판매자의 상품 상태는 변경할 수 없다")
    void changeStatus_rejectsNonOwner() {
        when(seller.getId()).thenReturn(2L);
        Product product = product("상품", "설명", 10_000L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.changeStatus(
                new ProductStatusRequest(ProductStatus.SOLD_OUT), 1L, 10L))
                .isInstanceOf(ProductAccessDeniedException.class)
                .hasMessage("수정 권한이 없습니다.");

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    private ProductCreateRequest request(String title, String description, Long price) {
        return new ProductCreateRequest(
                title,
                description,
                price,
                37.5665,
                126.9780,
                "서울시 동대문구"
        );
    }

    private ProductUpdateRequest updateRequest(String title, String description, Long price) {
        return new ProductUpdateRequest(title, description, price);
    }

    private Product product(String title, String description, Long price) {
        return new Product(
                seller,
                title,
                description,
                price,
                37.5665,
                126.9780,
                "서울시 동대문구"
        );
    }
}
