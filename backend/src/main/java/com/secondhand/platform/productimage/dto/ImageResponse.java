package com.secondhand.platform.productimage.dto;

import java.util.List;

public record ImageResponse(
        List<String> imagePaths
) {
}
