package com.secondhand.platform.product;

import com.secondhand.platform.product.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    //상품 등록
    @PostMapping
    public ResponseEntity<Void> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        productService.createProduct(request, userId);
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
    public ResponseEntity<List<ProductResponse>> getProducts(){
        List<ProductResponse> products = productService.getProducts();
        return ResponseEntity.ok(products);
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
