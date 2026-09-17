package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestPart("image") List<MultipartFile> images,
            @PathVariable Long productId,
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        List<String> imagePaths = productImageService.uploadImages(images,productId,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageResponse(imagePaths));
    }

    @DeleteMapping("/{imageId}")//단일 건수 지우기
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal Long userId
    ) {
        productImageService.deleteImage(productId,userId,imageId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteImages(
            @PathVariable Long productId,
            @RequestParam("imageIds") List<Long> imageIds,
            @AuthenticationPrincipal Long userId
    ) {
        productImageService.deleteImages(productId, userId, imageIds);

        return ResponseEntity.noContent().build();
    }
}
