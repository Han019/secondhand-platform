package com.secondhand.platform.product;

import com.secondhand.platform.product.dto.*;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    //상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createProduct(
            @Valid @RequestPart("product") ProductCreateRequest request,
            @RequestPart("image") List<MultipartFile> images,
            @AuthenticationPrincipal Long userId) throws IOException {
        productService.createProduct(request, images, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //상품 업데이트
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
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal Long userId
    ){
        productService.deleteProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    //상품들 조회
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(keyword, pageable));
    }

    //단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable Long productId){
        ProductDetailResponse product = productService.getProduct(productId);
        return ResponseEntity.ok(product);
    }
    //판매상태 변경
    @PatchMapping("/{productId}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long productId,
                                             @Valid @RequestBody ProductStatusRequest request,
                                             @AuthenticationPrincipal Long userId){
        productService.changeStatus(request, userId, productId);
        return ResponseEntity.noContent().build();
    }

}
