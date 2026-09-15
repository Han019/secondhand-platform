package com.secondhand.platform.productimage;

import com.secondhand.platform.productimage.dto.ImageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/images")
public class ProductImageController {

    @PostMapping
    public ResponseEntity<ImageResponse> uploadImage(){

    }
}
