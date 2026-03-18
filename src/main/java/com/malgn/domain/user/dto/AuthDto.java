package com.malgn.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    // 로그인
    @Getter
    @Setter
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // 회원가입
    @Getter
    @Setter
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String role = "USER";
    }

    // 로그인/회원가입 후 토큰 반환
    @Getter
    public static class TokenResponse {
        private final String accessToken;
        private final String tokenType = "Bearer";
        private final String username;
        private final String role;

        public TokenResponse(String accessToken, String username, String role) {
            this.accessToken = accessToken;
            this.username = username;
            this.role = role;
        }
    }
}