package com.secondhand.platform.product;

import com.secondhand.platform.product.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springdoc.core.annotations.ParameterObject;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "상품 등록, 조회 및 관리 API")
public class ProductController {
    private final ProductService productService;

    //상품 등록
    @Operation(
            summary = "상품 등록",
            description = "상품 정보와 이미지 1~10장을 등록하고 생성된 productId를 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = @Encoding(name = "product", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Valid @RequestPart("product") ProductCreateRequest request,
            @RequestPart("image") List<MultipartFile> images,
            @AuthenticationPrincipal Long userId) throws IOException {
        Long productId = productService.createProduct(request, images, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProductCreateResponse(productId));
    }

    //상품 업데이트
    @Operation(
            summary = "상품 수정",
            description = "상품 소유자가 제목, 설명, 가격을 수정합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(
            @Valid @RequestBody ProductUpdateRequest request,
            @PathVariable Long productId,
            @AuthenticationPrincipal Long userId
    ){
        productService.updateProduct(request, userId, productId);
        return ResponseEntity.noContent().build();
    }

    //상품 삭제
    @Operation(
            summary = "상품 삭제",
            description = "상품 소유자가 상품과 연결된 이미지 데이터를 삭제합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal Long userId
    ){
        productService.deleteProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    //상품들 조회
    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 페이지 단위로 조회합니다. keyword는 제목과 설명에서 검색합니다. 정렬은 sort=id,desc 또는 sort=price,asc처럼 상품 필드명과 방향을 사용합니다. 각 상품의 첫 번째 이미지 Signed URL을 포함합니다."
    )
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(keyword, pageable));
    }

    //단건 조회
    @Operation(
            summary = "상품 상세 조회",
            description = "상품 상세 정보와 정렬 순서대로 나열된 이미지의 Signed URL을 조회합니다."
    )
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable Long productId){
        ProductDetailResponse product = productService.getProduct(productId);
        return ResponseEntity.ok(product);
    }
    //판매상태 변경
    @Operation(
            summary = "상품 판매 상태 변경",
            description = "상품 소유자가 판매 상태를 변경합니다. 판매 완료된 상품은 다시 변경할 수 없습니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long productId,
                                             @Valid @RequestBody ProductStatusRequest request,
                                             @AuthenticationPrincipal Long userId){
        productService.changeStatus(request, userId, productId);
        return ResponseEntity.noContent().build();
    }

}
