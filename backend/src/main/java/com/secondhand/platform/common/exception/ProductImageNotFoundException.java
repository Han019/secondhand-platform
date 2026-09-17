package com.secondhand.platform.common.exception;

import java.util.List;

public class ProductImageNotFoundException extends RuntimeException {

    public ProductImageNotFoundException(Long imageId) {
        super("상품 이미지를 찾을 수 없습니다. imageId: " + imageId);
    }

    public ProductImageNotFoundException(List<Long> imageIds) {
        super("존재하지 않거나 해당 상품에 속하지 않은 이미지가 있습니다. imageIds: " + imageIds);
    }
}
