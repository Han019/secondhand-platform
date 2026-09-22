package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.config.ImageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class ProductImageCleanupService {
    private final PendingImageDeletionRepository pendingImageDeletionRepository;
    private final S3Client s3Client;
    private final ImageProperties imageProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deletePendingImage(String imagePath) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(imageProperties.bucket())
                .key(imagePath)
                .build());
        pendingImageDeletionRepository.deleteById(imagePath);
    }
}
