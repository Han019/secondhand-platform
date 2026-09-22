package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.dto.ImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Product Images", description = "상품 이미지 업로드, 정렬 및 삭제 API")
@SecurityRequirement(name = "bearerAuth")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Operation(
            summary = "상품 이미지 추가",
            description = "상품 소유자가 이미지를 추가합니다. 기존 이미지와 합쳐 최대 10장까지 저장하며, 업로드된 이미지 경로를 반환합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestPart("image") List<MultipartFile> images,
            @PathVariable Long productId,
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        List<String> imagePaths = productImageService.uploadImages(images,productId,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageResponse(imagePaths));
    }

    @Operation(
            summary = "상품 이미지 순서 변경",
            description = "상품 소유자가 해당 상품의 모든 이미지 ID를 원하는 순서대로 전달하여 정렬 순서를 변경합니다. 첫 번째 이미지가 목록의 대표 이미지로 사용됩니다."
    )
    @PatchMapping("/order")
    public ResponseEntity<Void> reorderImages(
            @PathVariable Long productId,
            @RequestBody List<Long> imageIds,
            @AuthenticationPrincipal Long userId
    ) {
        productImageService.reorderImages(productId, userId, imageIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "상품 이미지 단건 삭제",
            description = "상품 소유자가 해당 상품에 속한 이미지 한 장을 삭제합니다."
    )
    @DeleteMapping("/{imageId}")//단일 건수 지우기
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal Long userId
    ) {
        productImageService.deleteImage(productId,userId,imageId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "상품 이미지 선택 삭제",
            description = "상품 소유자가 imageIds 쿼리 파라미터로 전달한 이미지를 한 번에 삭제합니다."
    )
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
