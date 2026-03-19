package com.malgn.domain.user.controller;

import com.malgn.domain.user.dto.AuthDto;
import com.malgn.domain.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    AuthService authService;

    MockMvc mockMvc;
    JsonMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("로그인 성공 - JWT 토큰 반환")
    void login_success() throws Exception {
        // given - h2-data.sql의 admin 계정 사용 (단, 해시값이 맞지 않을 수 있으므로 먼저 register)
        AuthDto.RegisterRequest registerReq = new AuthDto.RegisterRequest();
        registerReq.setUsername("logintest");
        registerReq.setPassword("password123");
        registerReq.setRole("USER");
        authService.register(registerReq);

        // when & then
        AuthDto.LoginRequest loginReq = new AuthDto.LoginRequest();
        loginReq.setUsername("logintest");
        loginReq.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("logintest"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_wrongPassword() throws Exception {
        AuthDto.RegisterRequest registerReq = new AuthDto.RegisterRequest();
        registerReq.setUsername("logintest2");
        registerReq.setPassword("password123");
        authService.register(registerReq);

        AuthDto.LoginRequest loginReq = new AuthDto.LoginRequest();
        loginReq.setUsername("logintest2");
        loginReq.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void login_userNotFound() throws Exception {
        AuthDto.LoginRequest loginReq = new AuthDto.LoginRequest();
        loginReq.setUsername("notexist");
        loginReq.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");
        req.setRole("USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 username")
    void register_duplicateUsername() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("dupuser");
        req.setPassword("password123");
        authService.register(req);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - username 빈값")
    void register_blankUsername() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원가입 실패 - 잘못된 role")
    void register_invalidRole() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("roletest");
        req.setPassword("password123");
        req.setRole("SUPERUSER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}