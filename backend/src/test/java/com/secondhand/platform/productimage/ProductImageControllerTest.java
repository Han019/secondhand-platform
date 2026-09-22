package com.secondhand.platform.productimage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProductImageControllerTest {

    @Mock
    private ProductImageService productImageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProductImageController(productImageService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("선택한 이미지 여러 장을 삭제하고 204를 반환한다")
    void deleteImages_returnsNoContent() throws Exception {
        authenticate(1L);

        mockMvc.perform(delete("/api/products/10/images")
                        .queryParam("imageIds", "100,200"))
                .andExpect(status().isNoContent());

        verify(productImageService)
                .deleteImages(10L, 1L, List.of(100L, 200L));
    }

    @Test
    @DisplayName("이미지 순서 변경은 ID 순서를 서비스에 전달하고 204를 반환한다")
    void reorderImages_returnsNoContent() throws Exception {
        authenticate(1L);

        mockMvc.perform(patch("/api/products/10/images/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[200,100]"))
                .andExpect(status().isNoContent());

        verify(productImageService).reorderImages(10L, 1L, List.of(200L, 100L));
    }

    private void authenticate(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
        SecurityContextHolder.setContext(context);
    }
}
