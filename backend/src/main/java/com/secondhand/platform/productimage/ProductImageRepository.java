package com.secondhand.platform.productimage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByIdAndProduct_Id(Long imageId, Long productId);

    List<ProductImage> findAllByProduct_IdOrderBySortOrderAsc(Long productId);

    List<ProductImage> findAllByProduct_IdAndIdIn(Long productId, List<Long> imageIds);
}
