package com.secondhand.platform.product;

import com.secondhand.platform.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id",nullable = false)
    private User seller;
    //상품 제목
    @Column(nullable = false, length = 100)
    private String title;
    //상품 설명
    @Column(nullable = false)
    private String description;
    //상품 가격
    @Column(nullable = false)
    private Long price;

    //위도 경도 데이터
    private Double latitude;
    private Double longitude;
    private String address;

    private LocalDateTime createdAt;
    //예비 열
    private String category;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;


    public Product(
        User seller,
        String title,
        String description,
        Long price,
        Double latitude,
        Double longitude,
        String address
    ){
        this.seller = seller;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = ProductStatus.ON_SALE;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }
    public void update(
            String title,
            String description,
            Long price

    ) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }
    public void changeStatus(ProductStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
