package com.secondhand.platform.productimage;

import com.secondhand.platform.common.exception.ProductImageNotFoundException;
import com.secondhand.platform.product.Product;
import com.secondhand.platform.product.ProductRepository;
import com.secondhand.platform.productimage.config.ImageProperties;
import com.secondhand.platform.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Product product;

    @Mock
    private User seller;

    @Mock
    private ProductImage productImage;

    private ProductImageService productImageService;

    @BeforeEach
    void setUp() {
        ImageProperties imageProperties =
                new ImageProperties(null, null, null, null, "product-images");

        productImageService = new ProductImageService(
                imageProperties,
                productImageRepository,
                s3Client,
                productRepository
        );
    }

    @Test
    @DisplayName("상품 소유자는 해당 상품에 속한 이미지 한 장을 삭제할 수 있다")
    void deleteImage_deletesOwnedProductImage() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findByIdAndProduct_Id(100L, 10L))
                .thenReturn(Optional.of(productImage));
        when(productImage.getImagePath()).thenReturn("products/image-1.jpg");

        productImageService.deleteImage(10L, 1L, 100L);

        ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().bucket()).isEqualTo("product-images");
        assertThat(requestCaptor.getValue().key()).isEqualTo("products/image-1.jpg");
        verify(productImageRepository).delete(productImage);
    }

    @Test
    @DisplayName("요청한 상품에 속하지 않은 이미지는 삭제하지 않는다")
    void deleteImage_rejectsImageFromAnotherProduct() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findByIdAndProduct_Id(200L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.deleteImage(10L, 1L, 200L))
                .isInstanceOf(ProductImageNotFoundException.class);

        verifyNoInteractions(s3Client);
        verify(productImageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("상품의 전체 이미지는 Storage 요청 한 번과 DB 일괄 삭제로 제거한다")
    void deleteImages_deletesStorageAndDatabaseInBatch() {
        ProductImage first = mock(ProductImage.class);
        ProductImage second = mock(ProductImage.class);
        List<ProductImage> images = List.of(first, second);

        givenOwnedProduct(10L, 1L);
        when(first.getImagePath()).thenReturn("products/image-1.jpg");
        when(second.getImagePath()).thenReturn("products/image-2.jpg");
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(images);
        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenReturn(DeleteObjectsResponse.builder().build());

        productImageService.deleteImages(10L, 1L);

        ArgumentCaptor<DeleteObjectsRequest> requestCaptor =
                ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(s3Client, times(1)).deleteObjects(requestCaptor.capture());
        assertThat(requestCaptor.getValue().delete().objects())
                .extracting(object -> object.key())
                .containsExactly("products/image-1.jpg", "products/image-2.jpg");
        verify(productImageRepository).deleteAllInBatch(images);
    }

    @Test
    @DisplayName("선택한 여러 이미지는 Storage 요청 한 번과 DB 일괄 삭제로 제거한다")
    void deleteImages_deletesSelectedImagesInBatch() {
        ProductImage first = mock(ProductImage.class);
        ProductImage second = mock(ProductImage.class);
        List<ProductImage> images = List.of(first, second);

        givenOwnedProduct(10L, 1L);
        when(first.getImagePath()).thenReturn("products/image-1.jpg");
        when(second.getImagePath()).thenReturn("products/image-2.jpg");
        when(productImageRepository.findAllByProduct_IdAndIdIn(10L, List.of(100L, 200L)))
                .thenReturn(images);
        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenReturn(DeleteObjectsResponse.builder().build());

        productImageService.deleteImages(10L, 1L, List.of(100L, 200L));

        verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
        verify(productImageRepository).deleteAllInBatch(images);
    }

    @Test
    @DisplayName("선택 목록에 없거나 다른 상품의 이미지가 포함되면 아무것도 삭제하지 않는다")
    void deleteImages_rejectsMissingOrForeignImage() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findAllByProduct_IdAndIdIn(10L, List.of(100L, 200L)))
                .thenReturn(List.of(productImage));

        assertThatThrownBy(() ->
                productImageService.deleteImages(10L, 1L, List.of(100L, 200L)))
                .isInstanceOf(ProductImageNotFoundException.class);

        verifyNoInteractions(s3Client);
        verify(productImageRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    @DisplayName("상품에 이미지가 없으면 Storage와 DB 삭제를 호출하지 않는다")
    void deleteImages_doesNothingWhenProductHasNoImages() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(List.of());

        productImageService.deleteImages(10L, 1L);

        verifyNoInteractions(s3Client);
        verify(productImageRepository, never()).deleteAllInBatch(anyList());
    }

    private void givenOwnedProduct(Long productId, Long userId) {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(userId);
    }
}
