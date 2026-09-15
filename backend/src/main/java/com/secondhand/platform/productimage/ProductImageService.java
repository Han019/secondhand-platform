package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.config.ImageProperties;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageService {
    //이미지 업로드 및 상품 이미지 모델 구현
    private final ImageProperties imageProperties;
    private final ProductImageRepository productImageRepository;
    private final S3Client s3Client;

    public String uploadImage(MultipartFile image) throws IOException {

        if(image.isEmpty()){
            throw new IllegalArgumentException("이미지가 비었습니다.");
        }
        if(image.getContentType() == null|| !image.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
        //현재 시간 + 고유한 uid 조합으로 파일 이름 생성
        String fileName = System.currentTimeMillis()
                + "_"
                + UUID.randomUUID().toString().substring(0,8)
                + ".jpg";

        String imagePath = "products/" + fileName;
        //이미지 압축
        byte[] compressedImage = compressImage(image);
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
    public void deleteImage(Long productId){}
    public void saveImage(){}
}
