package com.secondhand.platform.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, unique = true, length = 70)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    private String profileImage;
    @Column(nullable = false)
    private double mannerScore = 36.5;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 예비 열

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private User(
            String loginId,
            String password,
            String email,
            String nickname
    ){
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = Role.USER;
    }
    public static User create(
            String loginId,
            String password,
            String email,
            String nickname
    ){
        return new User(loginId, password, email, nickname);
    }

}
