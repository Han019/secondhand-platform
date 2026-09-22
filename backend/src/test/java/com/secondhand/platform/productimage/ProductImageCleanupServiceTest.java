package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.config.ImageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageCleanupServiceTest {
    @Mock PendingImageDeletionRepository pendingImageDeletionRepository;
    @Mock S3Client s3Client;

    @Test
    void deletePendingImage_removesQueueOnlyAfterS3Delete() {
        ProductImageCleanupService service = new ProductImageCleanupService(
                pendingImageDeletionRepository, s3Client,
                new ImageProperties(null, null, null, null, "product-images"));

        service.deletePendingImage("products/photo.jpg");

        InOrder order = inOrder(s3Client, pendingImageDeletionRepository);
        order.verify(s3Client).deleteObject(argThat((DeleteObjectRequest request) ->
                request.bucket().equals("product-images") && request.key().equals("products/photo.jpg")));
        order.verify(pendingImageDeletionRepository).deleteById("products/photo.jpg");
    }

    @Test
    void deletePendingImage_keepsQueueWhenS3DeleteFails() {
        ProductImageCleanupService service = new ProductImageCleanupService(
                pendingImageDeletionRepository, s3Client,
                new ImageProperties(null, null, null, null, "product-images"));
        doThrow(new IllegalStateException("S3 unavailable"))
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> service.deletePendingImage("products/photo.jpg"))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(pendingImageDeletionRepository);
    }
}
