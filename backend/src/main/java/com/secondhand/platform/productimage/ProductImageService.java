package com.secondhand.platform.productimage;

import com.secondhand.platform.common.exception.ProductAccessDeniedException;
import com.secondhand.platform.common.exception.InvalidProductImageRequestException;
import com.secondhand.platform.common.exception.ProductImageNotFoundException;
import com.secondhand.platform.common.exception.ProductNotFoundException;
import com.secondhand.platform.product.Product;
import com.secondhand.platform.product.ProductRepository;
import com.secondhand.platform.productimage.config.ImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {
    //이미지 업로드 및 상품 이미지 모델 구현
    private final ImageProperties imageProperties;
    private final ProductImageRepository productImageRepository;
    private final S3Client s3Client;
    private final ProductRepository productRepository;
    private final S3Presigner s3Presigner;
    private final PendingImageDeletionRepository pendingImageDeletionRepository;
    private final ProductImageCleanupService productImageCleanupService;

    @Transactional(rollbackFor = IOException.class)
    public List<String> uploadImages(List<MultipartFile> images, Long productId,Long userId) throws IOException {

        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("상품이 없습니다.", productId)
        );
        //권한 확인
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("상품 이미지 등록 권한이 없습니다.");
        }

        if (images == null || images.isEmpty()) {
            throw new InvalidProductImageRequestException("이미지를 한 장 이상 첨부해주세요.");
        }
        if (images.size() > 10) {
            throw new InvalidProductImageRequestException("이미지는 최대 10장까지 첨부 가능합니다.");
        }
        List<String> imagePaths = new ArrayList<>();
        List<ProductImage> productImages = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED && !imagePaths.isEmpty()) {
                    deleteUploadedImages(imagePaths);
                }
            }
        });

        List<ProductImage> existing = productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(productId);
        // ponytail: 같은 상품의 동시 업로드는 순서가 겹칠 수 있으므로 필요해지면 상품 행에 잠금을 겁니다.
        int i = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;
        for (MultipartFile image : images) {
            String imagePath = uploadImage(image, imagePaths);
            productImages.add(new ProductImage(product, imagePath, i));
            i++;
        }
        productImageRepository.saveAll(productImages);

        return imagePaths;
    }

    @Transactional
    public void reorderImages(Long productId, Long userId, List<Long> imageIds) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("상품을 찾을 수 없습니다.", productId));
        if (!product.getSeller().getId().equals(userId)) {
            throw new ProductAccessDeniedException("이미지 순서 변경 권한이 없습니다.");
        }

        List<ProductImage> images = productImageRepository.findAllByProduct_IdOrderBySortOrderAsc(productId);
        Map<Long, ProductImage> byId = new HashMap<>();
        for (ProductImage image : images) {
            byId.put(image.getId(), image);
        }
        if (imageIds == null || imageIds.size() != images.size()
                || !byId.keySet().equals(new HashSet<>(imageIds))) {
            throw new InvalidProductImageRequestException("상품의 이미지 ID를 원하는 순서대로 모두 보내주세요.");
        }

        for (int i = 0; i < imageIds.size(); i++) {
            byId.get(imageIds.get(i)).changeSortOrder(i);
        }
    }

    private String uploadImage(MultipartFile images, List<String> imagePaths) throws IOException {

        if(images.isEmpty() || images.getContentType() == null || !images.getContentType().startsWith("image/")){
            throw new InvalidProductImageRequestException("이미지 파일만 업로드할 수 있습니다.");
        }

//        현재 시간 + 고유한 uid 조합으로 파일 이름 생성
        String fileName = System.currentTimeMillis()
                + "_"
                + UUID.randomUUID().toString().substring(0,8)
                + ".jpg";

        String imagePath = "products/" + fileName;
        //이미지 압축
        byte[] compressedImage = compressImage(images);
        imagePaths.add(imagePath);
        //이미지 저장하기
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(imageProperties.bucket())
                .key(imagePath)
                .contentType("image/jpeg")
                .build(),
                RequestBody.fromBytes(compressedImage));
        return imagePath;
    }

    //이미지 용량 줄이기
    private byte[] compressImage(MultipartFile image) throws IOException{
        try(
                InputStream inputStream = image.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ){
            Thumbnails.of(inputStream).size(1200,1200)
                    .outputFormat("jpg").outputQuality(0.75).toOutputStream(outputStream);
            return outputStream.toByteArray();
        }
    }
    private void deleteUploadedImages(List<String> imagePaths) {
        try {
            List<ObjectIdentifier> objects = imagePaths.stream()
                    .map(path -> ObjectIdentifier.builder().key(path).build())
                    .toList();
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(imageProperties.bucket())
                    .delete(Delete.builder().objects(objects).build())
                    .build());
            response.errors().forEach(error ->
                    log.error("Rolled-back image cleanup failed: key={}, code={}", error.key(), error.code()));
        } catch (RuntimeException e) {
            log.error("Rolled-back image cleanup failed: paths={}", imagePaths, e);
        }
    }

    //상품 삭제시 이미지 전체 삭제 구현
    // 개별 이미지 삭제 구현
    // 단일 이미지 지우기
    @Transactional
    public void deleteImage(Long productId, Long userId, Long imageId){
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("상품을 찾을 수 없습니다.",productId));
        //다르면
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("이미지 삭제 권한이 없습니다.");
        }

        ProductImage productImage = productImageRepository
                .findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() -> new ProductImageNotFoundException(imageId));

        deleteStoredImages(List.of(productImage));

    }

    @Transactional
    // 상품지우면 한번에 다 지우기
    public void deleteImages(Long productId, Long userId){
        //@AuthenticatdPrincipal Long userId로 받은거와
        //product에서 찾은거와 비교해서 다르면 exceoption

        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("상품을 찾을 수 없습니다.",productId));
        //다르면
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("접근 권한이 없습니다.");
        }

        List<ProductImage> productImages = productImageRepository
                .findAllByProduct_IdOrderBySortOrderAsc(productId);

        deleteStoredImages(productImages);
    }

    @Transactional
    public void deleteImages(Long productId, Long userId, List<Long> imageIds) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("상품을 찾을 수 없습니다.", productId));

        if (!product.getSeller().getId().equals(userId)) {
            throw new ProductAccessDeniedException("이미지 삭제 권한이 없습니다.");
        }

        if (imageIds == null || imageIds.isEmpty()) {
            throw new InvalidProductImageRequestException("삭제할 이미지를 선택해주세요.");
        }

        List<Long> requestedImageIds = imageIds.stream().distinct().toList();
        if (requestedImageIds.size() > 10) {
            throw new InvalidProductImageRequestException("이미지는 최대 10장까지 삭제할 수 있습니다.");
        }

        List<ProductImage> productImages = productImageRepository
                .findAllByProduct_IdAndIdIn(productId, requestedImageIds);

        if (productImages.size() != requestedImageIds.size()) {
            throw new ProductImageNotFoundException(requestedImageIds);
        }

        deleteStoredImages(productImages);
    }

    private void deleteStoredImages(List<ProductImage> productImages) {
        if (productImages.isEmpty()) {
            return;
        }

        List<String> imagePaths = productImages.stream().map(ProductImage::getImagePath).toList();
        pendingImageDeletionRepository.saveAll(imagePaths.stream().map(PendingImageDeletion::new).toList());
        productImageRepository.deleteAllInBatch(productImages);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                imagePaths.forEach(ProductImageService.this::deletePendingImageSafely);
            }
        });
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void retryPendingImageDeletions() {
        // ponytail: 전체 대기 목록을 매분 조회합니다. 대기량이 커지면 페이징과 재시도 시각을 추가합니다.
        pendingImageDeletionRepository.findAll().stream()
                .map(PendingImageDeletion::getImagePath)
                .forEach(this::deletePendingImageSafely);
    }

    private void deletePendingImageSafely(String imagePath) {
        try {
            productImageCleanupService.deletePendingImage(imagePath);
        } catch (RuntimeException e) {
            log.error("Image deletion failed; will retry: path={}", imagePath, e);
        }
    }

    //이미지 signed URL 생성
    public String generateSignedUrl(String imagePath){
        GetObjectRequest request = GetObjectRequest.builder().bucket(imageProperties.bucket())
                .key(imagePath).build();

        return s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(request)
                        .build()
        ).url().toString();
    }
}
