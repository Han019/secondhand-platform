package com.secondhand.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=context-test-secret-key-at-least-32-bytes",
        "resend.api-key=test-key",
        "supabase.endpoint=http://localhost:9000",
        "supabase.region=us-east-1",
        "supabase.access-key=test-key",
        "supabase.secret-key=test-secret",
        "supabase.bucket=test-bucket"
})
class SecondhandPlatformApplicationTests {

    @Test
    void contextLoads() {
    }
}
