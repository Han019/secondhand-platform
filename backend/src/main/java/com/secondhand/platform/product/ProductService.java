package com.secondhand.platform.product;

import com.secondhand.platform.common.exception.ProductAccessDeniedException;
import com.secondhand.platform.common.exception.ProductNotFoundException;
import com.secondhand.platform.product.dto.*;
import com.secondhand.platform.user.User;
import com.secondhand.platform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//예외처리 만들기
@Service
@RequiredArgsConstructor
@Transactional( readOnly = true)
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    //로그인한 userid로 user 조회해서 Product 만들기
    @Transactional
    public void createProduct(ProductCreateRequest request, Long userId){
        User seller = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("없는 사용자입니다."));
        Product product = new Product(
                seller,
                request.title(),
                request.description(),
                request.price(),
                request.latitude(),
                request.longitude(),
                request.address()
        );

        productRepository.save(product);
    }
    //로그인한 userId와 ProductId 조회해서 수정하기
    @Transactional
    public void updateProduct(ProductUpdateRequest request, Long userId, Long productId){
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("없는 상품입니다.",productId));

        // 권환 확인
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("수정 권한이 없습니다.");
        }
        product.update(
                request.title(),
                request.description(),
                request.price()
        );
    }
    //로그인한 userId와 ProductId 조회해서 삭제하기
    @Transactional
    public void deleteProduct(Long userId, Long productId){
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("없는 상품입니다.",productId));
        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("삭제 권한이 없습니다.");
        }
        productRepository.delete(product);
    }
    //product들 조회
    public List<ProductResponse> getProducts(){
        List<Product> products = productRepository.findAll();

        return products.stream().map(ProductResponse::from).toList();
    }

    //단건 조회
    public ProductDetailResponse getProduct(Long productId){
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ProductNotFoundException("없는 상품입니다.", productId));

        return ProductDetailResponse.from(product);
    }

    //판매 상태 변경
    @Transactional
    public void changeStatus(ProductStatusRequest request, Long userId , Long productId){
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("없는 상품입니다.", productId));

        if(!product.getSeller().getId().equals(userId)){
            throw new ProductAccessDeniedException("수정 권한이 없습니다.");
        }
        if(product.getStatus() == ProductStatus.SOLD_OUT){
            throw new ProductAccessDeniedException("이미 판매 완료된 상품입니다.");
        }
        product.changeStatus(request.status());
    }
}
