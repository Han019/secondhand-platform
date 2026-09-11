package com.secondhand.platform.auth;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ResendEmailTest {

    @Autowired
    private Resend resend;

    @Test
    void sendEmailTest() throws ResendException {

        CreateEmailOptions params =
                CreateEmailOptions.builder()
                        .from("Secondhand <onboarding@resend.dev>")
                        .to("qustlswjsdnrlaud@gmail.com")
                        .subject("Resend 연결 테스트")
                        .html("""
                            <h2>Resend 테스트 성공</h2>
                            <p>Spring Boot에서 보낸 이메일입니다.</p>
                            <h1>A7K29Q</h1>
                            """)
                        .build();

        CreateEmailResponse response =
                resend.emails().send(params);

        System.out.println(
                "Resend Email ID = " + response.getId()
        );
    }
}