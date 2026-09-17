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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public List<String> uploadImages(List<MultipartFile> images, Long productId,Long userId) throws IOException {

        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("상품이 없습니다.", productId)
        );
        //권한 확인
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("상품 이미지 등록 권한이 없습니다.");
        }

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("이미지를 한 장 이상 첨부해주세요.");
        }
        if (images.size() > 10) {
            throw new IllegalArgumentException("이미지는 최대 10장까지 첨부 가능합니다.");
        }
        List<String> imagePaths = new ArrayList<>();
        List<ProductImage> productImages = new ArrayList<>();

        int i = 0;
        for (MultipartFile image : images) {
            String imagePath = uploadImage(image);
            imagePaths.add(imagePath);
            productImages.add(new ProductImage(product, imagePath, i));
            i++;
        }
        productImageRepository.saveAll(productImages);

        return imagePaths;
    }
    @Transactional
    public String uploadImage(MultipartFile images) throws IOException {

        if(images.getContentType() == null|| !images.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

//        현재 시간 + 고유한 uid 조합으로 파일 이름 생성
        String fileName = System.currentTimeMillis()
                + "_"
                + UUID.randomUUID().toString().substring(0,8)
                + ".jpg";

        String imagePath = "products/" + fileName;
        //이미지 압축
        byte[] compressedImage = compressImage(images);
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
    // 업로드 중간 실패시 이미 올라간 파일 제거
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

        String imagePath = productImage.getImagePath();
        log.info("Deleting image from bucket: {}",imagePath);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(imageProperties.bucket())
                .key(imagePath)
                .build());
        productImageRepository.delete(productImage);

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

        List<ObjectIdentifier> identifiers = productImages.stream()
                .map(productImage -> ObjectIdentifier.builder()
                        .key(productImage.getImagePath())
                        .build())
                .toList();

        log.info("Deleting images from bucket: {}",imageProperties.bucket());

        try{
            Delete delete = Delete.builder()
                    .objects(identifiers)
                    .quiet(true)
                    .build();
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(imageProperties.bucket())
                    .delete(delete)
                    .build();

            DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);

            if (!deleteObjectsResponse.errors().isEmpty()) {
                deleteObjectsResponse.errors().forEach(error ->
                        log.error("Failed to delete object: key={}, code={}, message={}",
                                error.key(), error.code(), error.message()));
                throw new IllegalStateException("일부 상품 이미지를 삭제하지 못했습니다.");
            }

            productImageRepository.deleteAllInBatch(productImages);

        }catch( S3Exception e){
            log.error("Failed to delete images: code={}, message={}",
                    e.awsErrorDetails().errorCode(),
                    e.awsErrorDetails().errorMessage(),
                    e);
            throw e;
        }
    }

}
