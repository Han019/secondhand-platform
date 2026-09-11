package com.secondhand.platform.auth;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.secondhand.platform.common.exception.DuplicatedUserException;
import com.secondhand.platform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    //이메일 발송
    private final Resend resend;
    private final UserRepository userRepository;
    private static final String CODE_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;

    // 6자리의 인증코드 생성
    public String createVerificationCode(){

        //코드 생성
        StringBuilder code = new StringBuilder(6);

        for(int i = 0; i < 6; i++){
            int index = random.nextInt(CODE_CHARS.length());
            code.append(CODE_CHARS.charAt(index));
        }

        return code.toString();

    }
    //redis 코드 저장, 가져오기


    public void saveCode(String email, String code){
        String key = "auth:email:code:"+email;
        redisTemplate.opsForValue().set(
                key,
                code,
                Duration.ofMinutes(5)
        );
    }
    //redis 조회
    public String getCode(String email){
        String key = "auth:email:code:"+email;
        return redisTemplate.opsForValue().get(key);
    }



    public void sendVerificationEmail(String email){
        //이메일 중복이 아니면 통과
        if(userRepository.existsByEmail(email)){
            throw new DuplicatedUserException("이미 등록된 이메일입니다.");
        }
        String verificationCode = createVerificationCode();

        CreateEmailOptions params =
                CreateEmailOptions.builder()
                        .from("Secondhand <onboarding@resend.dev>")
                        .to(email)
                        .subject("이메일 인증번호")
                        .html("""
        <div style="font-family: Arial, sans-serif;">
            <h2>이메일 인증</h2>
            <p>아래 인증번호를 입력해주세요.</p>
    
            <div style="
                font-size: 28px;
                font-weight: bold;
                letter-spacing: 6px;
                margin: 20px 0;
            ">
                %s
            </div>
    
            <p>인증번호는 5분간 유효합니다.</p>
        </div>""".formatted(verificationCode)).build();

        try{
            resend.emails().send(params);
        }catch(ResendException e){
            redisTemplate.delete("auth:email:code:" + email);
            throw new RuntimeException("인증 이메일 발송에 실패했습니다.",e);
        }

    }
}
