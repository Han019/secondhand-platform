package com.secondhand.platform.productimage;

import com.secondhand.platform.common.exception.InvalidProductImageRequestException;
import com.secondhand.platform.common.exception.ProductImageNotFoundException;
import com.secondhand.platform.product.Product;
import com.secondhand.platform.product.ProductRepository;
import com.secondhand.platform.productimage.config.ImageProperties;
import com.secondhand.platform.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
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
    private PendingImageDeletionRepository pendingImageDeletionRepository;

    @Mock
    private ProductImageCleanupService productImageCleanupService;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

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
                productRepository,
                s3Presigner,
                pendingImageDeletionRepository,
                productImageCleanupService
        );
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("두 번째 이미지 업로드 실패 시 롤백 콜백이 S3 파일을 정리한다")
    void uploadImages_cleansUpAfterRollback() throws Exception {
        givenOwnedProduct(10L, 1L);
        MockMultipartFile image = image();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build())
                .thenThrow(S3Exception.builder().message("upload failed").build());
        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenReturn(DeleteObjectsResponse.builder().build());
        TransactionSynchronizationManager.initSynchronization();

        assertThatThrownBy(() -> productImageService.uploadImages(List.of(image, image), 10L, 1L))
                .isInstanceOf(S3Exception.class);
        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        ArgumentCaptor<DeleteObjectsRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(s3Client).deleteObjects(requestCaptor.capture());
        assertThat(requestCaptor.getValue().delete().objects()).hasSize(2);
        verify(productImageRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("이미지 9장이 있으면 1장을 추가해 10장까지 저장한다")
    void uploadImages_startsAfterExistingSortOrder() throws Exception {
        givenOwnedProduct(10L, 1L);
        ProductImage last = mock(ProductImage.class);
        when(last.getSortOrder()).thenReturn(8);
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(Collections.nCopies(9, last));
        TransactionSynchronizationManager.initSynchronization();

        productImageService.uploadImages(List.of(image()), 10L, 1L);

        verify(productImageRepository).saveAll(argThat(saved ->
                saved.iterator().next().getSortOrder() == 9));
    }

    @Test
    @DisplayName("기존 이미지와 새 이미지의 합계가 10장을 넘으면 업로드 전에 거부한다")
    void uploadImages_rejectsMoreThanTenImagesInTotal() throws Exception {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(Collections.nCopies(9, productImage));

        assertThatThrownBy(() -> productImageService.uploadImages(List.of(image(), image()), 10L, 1L))
                .isInstanceOf(InvalidProductImageRequestException.class);

        verifyNoInteractions(s3Client);
        verify(productImageRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("순서 변경은 같은 상품의 이미지 ID 전체를 요구하고 0부터 다시 매긴다")
    void reorderImages_reordersAllOwnedImages() {
        givenOwnedProduct(10L, 1L);
        ProductImage first = new ProductImage(product, "first.jpg", 0);
        ProductImage second = new ProductImage(product, "second.jpg", 2);
        ReflectionTestUtils.setField(first, "id", 100L);
        ReflectionTestUtils.setField(second, "id", 200L);
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(List.of(first, second));

        assertThatThrownBy(() -> productImageService.reorderImages(10L, 1L, List.of(200L, 999L)))
                .isInstanceOf(com.secondhand.platform.common.exception.InvalidProductImageRequestException.class);
        assertThat(first.getSortOrder()).isEqualTo(0);
        assertThat(second.getSortOrder()).isEqualTo(2);

        productImageService.reorderImages(10L, 1L, List.of(200L, 100L));

        assertThat(second.getSortOrder()).isEqualTo(0);
        assertThat(first.getSortOrder()).isEqualTo(1);
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("이미지 한 장은 DB에서 지운 뒤 커밋 후 S3 삭제를 요청한다")
    void deleteImage_deletesOwnedProductImage() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findByIdAndProduct_Id(100L, 10L))
                .thenReturn(Optional.of(productImage));
        when(productImage.getImagePath()).thenReturn("products/image-1.jpg");
        TransactionSynchronizationManager.initSynchronization();

        productImageService.deleteImage(10L, 1L, 100L);

        verify(pendingImageDeletionRepository).saveAll(argThat(pending ->
                pending.iterator().next().getImagePath().equals("products/image-1.jpg")));
        verify(productImageRepository).deleteAll(List.of(productImage));
        verifyNoInteractions(s3Client, productImageCleanupService);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(productImageCleanupService).deletePendingImage("products/image-1.jpg");
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
    @DisplayName("전체 이미지 삭제는 커밋 후에만 S3 삭제를 요청한다")
    void deleteImages_deletesStorageAndDatabase() {
        ProductImage first = mock(ProductImage.class);
        ProductImage second = mock(ProductImage.class);
        List<ProductImage> images = List.of(first, second);

        givenOwnedProduct(10L, 1L);
        when(first.getImagePath()).thenReturn("products/image-1.jpg");
        when(second.getImagePath()).thenReturn("products/image-2.jpg");
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(images);
        TransactionSynchronizationManager.initSynchronization();

        productImageService.deleteImages(10L, 1L);

        verifyNoInteractions(s3Client, productImageCleanupService);
        verify(productImageRepository).deleteAll(images);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(productImageCleanupService).deletePendingImage("products/image-1.jpg");
        verify(productImageCleanupService).deletePendingImage("products/image-2.jpg");
    }

    @Test
    @DisplayName("선택한 여러 이미지는 롤백하면 S3에서 삭제하지 않는다")
    void deleteImages_deletesSelectedImages() {
        ProductImage first = mock(ProductImage.class);
        ProductImage second = mock(ProductImage.class);
        List<ProductImage> images = List.of(first, second);

        givenOwnedProduct(10L, 1L);
        when(first.getImagePath()).thenReturn("products/image-1.jpg");
        when(second.getImagePath()).thenReturn("products/image-2.jpg");
        when(productImageRepository.findAllByProduct_IdAndIdIn(10L, List.of(100L, 200L)))
                .thenReturn(images);
        TransactionSynchronizationManager.initSynchronization();

        productImageService.deleteImages(10L, 1L, List.of(100L, 200L));

        verify(productImageRepository).deleteAll(images);
        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(s3Client, productImageCleanupService);
    }

    @Test
    @DisplayName("S3 삭제에 실패해도 다음 이미지 정리를 계속 시도한다")
    void retryPendingImageDeletions_continuesAfterFailure() {
        when(pendingImageDeletionRepository.findAll()).thenReturn(List.of(
                new PendingImageDeletion("products/first.jpg"),
                new PendingImageDeletion("products/second.jpg")));
        doThrow(new IllegalStateException("S3 unavailable"))
                .when(productImageCleanupService).deletePendingImage("products/first.jpg");

        productImageService.retryPendingImageDeletions();

        verify(productImageCleanupService).deletePendingImage("products/first.jpg");
        verify(productImageCleanupService).deletePendingImage("products/second.jpg");
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
        verify(productImageRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("상품에 이미지가 없으면 Storage와 DB 삭제를 호출하지 않는다")
    void deleteImages_doesNothingWhenProductHasNoImages() {
        givenOwnedProduct(10L, 1L);
        when(productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(10L))
                .thenReturn(List.of());

        productImageService.deleteImages(10L, 1L);

        verifyNoInteractions(s3Client);
        verify(productImageRepository, never()).deleteAll(anyList());
    }

    private void givenOwnedProduct(Long productId, Long userId) {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(userId);
    }

    private MockMultipartFile image() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return new MockMultipartFile("image", "photo.png", "image/png", bytes.toByteArray());
    }
}
