package com.malgn.domain.content.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.malgn.domain.content.dto.ContentDto;
import com.malgn.domain.user.dto.AuthDto;
import com.malgn.domain.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ContentControllerTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;
    JsonMapper objectMapper = JsonMapper.builder().build();
    @Autowired
    AuthService authService;

    private String user1Token;
    private String user2Token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // user1 회원가입 후 토큰 발급
        AuthDto.RegisterRequest user1Req = new AuthDto.RegisterRequest();
        user1Req.setUsername("testuser1");
        user1Req.setPassword("user1234");
        user1Req.setRole("USER");
        user1Token = authService.register(user1Req).getAccessToken();

        // user2 회원가입 후 토큰 발급
        AuthDto.RegisterRequest user2Req = new AuthDto.RegisterRequest();
        user2Req.setUsername("testuser2");
        user2Req.setPassword("user2222");
        user2Req.setRole("USER");
        user2Token = authService.register(user2Req).getAccessToken();

        // admin 회원가입 후 토큰 발급
        AuthDto.RegisterRequest adminReq = new AuthDto.RegisterRequest();
        adminReq.setUsername("testadmin");
        adminReq.setPassword("admin1234");
        adminReq.setRole("ADMIN");
        adminToken = authService.register(adminReq).getAccessToken();
    }

    // 콘텐츠 생성 헬퍼
    private Long createContent(String token, String title) throws Exception {
        ContentDto.CreateRequest req = new ContentDto.CreateRequest();
        req.setTitle(title);
        req.setDescription(title + " 내용");

        String body = mockMvc.perform(post("/api/contents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("data").get("id").asLong();
    }

    @Test
    @DisplayName("토큰 없이 콘텐츠 생성 시 401")
    void create_withoutToken_401() throws Exception {
        ContentDto.CreateRequest req = new ContentDto.CreateRequest();
        req.setTitle("무단 생성");

        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("콘텐츠 생성 성공")
    void create_success() throws Exception {
        createContent(user1Token, "테스트 콘텐츠");
    }

    @Test
    @DisplayName("콘텐츠 목록 페이징 조회")
    void findAll_paged() throws Exception {
        createContent(user1Token, "콘텐츠 A");
        createContent(user1Token, "콘텐츠 B");

        mockMvc.perform(get("/api/contents")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("제목 검색")
    void findAll_searchByTitle() throws Exception {
        createContent(user1Token, "Spring Boot 가이드");
        createContent(user1Token, "Java 기초");

        mockMvc.perform(get("/api/contents")
                        .param("title", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("상세 조회 시 조회수 증가")
    void findById_viewCountIncrement() throws Exception {
        Long id = createContent(user1Token, "조회수 테스트");

        mockMvc.perform(get("/api/contents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1));

        mockMvc.perform(get("/api/contents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(2));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠 조회 시 404")
    void findById_notFound() throws Exception {
        mockMvc.perform(get("/api/contents/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("작성자 본인이 수정 성공")
    void update_byAuthor_success() throws Exception {
        Long id = createContent(user1Token, "원본 제목");

        ContentDto.UpdateRequest req = new ContentDto.UpdateRequest();
        req.setTitle("수정된 제목");
        req.setDescription("수정된 내용");

        mockMvc.perform(put("/api/contents/" + id)
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("ADMIN이 타인 콘텐츠 수정 성공")
    void update_byAdmin_success() throws Exception {
        Long id = createContent(user1Token, "user1 콘텐츠");

        ContentDto.UpdateRequest req = new ContentDto.UpdateRequest();
        req.setTitle("관리자가 수정");
        req.setDescription("관리자 수정 내용");

        mockMvc.perform(put("/api/contents/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("관리자가 수정"));
    }

    @Test
    @DisplayName("다른 사용자가 수정 시 403")
    void update_byOtherUser_forbidden() throws Exception {
        Long id = createContent(user1Token, "user1 전용 콘텐츠");

        ContentDto.UpdateRequest req = new ContentDto.UpdateRequest();
        req.setTitle("해킹 시도");
        req.setDescription("해킹 내용");

        mockMvc.perform(put("/api/contents/" + id)
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("작성자 본인이 삭제 성공")
    void delete_byAuthor_success() throws Exception {
        Long id = createContent(user1Token, "삭제할 콘텐츠");

        mockMvc.perform(delete("/api/contents/" + id)
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contents/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 사용자가 삭제 시 403")
    void delete_byOtherUser_forbidden() throws Exception {
        Long id = createContent(user1Token, "보호된 콘텐츠");

        mockMvc.perform(delete("/api/contents/" + id)
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden());
    }
}