package com.malgn.domain.content.service;

import com.malgn.domain.content.dto.ContentDto;
import com.malgn.domain.content.entity.Content;
import com.malgn.domain.content.repository.ContentRepository;
import com.malgn.domain.user.entity.User;
import com.malgn.global.exception.AccessDeniedException;
import com.malgn.global.exception.ResourceNotFoundException;
import com.malgn.global.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    ContentRepository contentRepository;

    @InjectMocks
    ContentService contentService;

    private Content content;
    private UserPrincipal author;
    private UserPrincipal otherUser;
    private UserPrincipal admin;

    @BeforeEach
    void setUp() {
        content = Content.builder()
                .id(1L)
                .title("테스트 제목")
                .description("테스트 내용")
                .viewCount(0L)
                .createdBy("user1")
                .build();

        author = new UserPrincipal(User.builder()
                .id(1L).username("user1")
                .password("pw").role(User.Role.USER).build());

        otherUser = new UserPrincipal(User.builder()
                .id(2L).username("user2")
                .password("pw").role(User.Role.USER).build());

        admin = new UserPrincipal(User.builder()
                .id(3L).username("admin")
                .password("pw").role(User.Role.ADMIN).build());
    }

    @Test
    @DisplayName("콘텐츠 생성 성공")
    void create_success() {
        ContentDto.CreateRequest request = new ContentDto.CreateRequest();
        request.setTitle("새 제목");
        request.setDescription("새 내용");

        given(contentRepository.save(any(Content.class))).willReturn(content);

        ContentDto.Response response = contentService.create(request);

        assertThat(response).isNotNull();
        verify(contentRepository, times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("콘텐츠 상세 조회 시 조회수 증가")
    void findById_incrementsViewCount() {
        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        contentService.findById(1L);

        assertThat(content.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠 조회 시 예외")
    void findById_notFound() {
        given(contentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("작성자 본인이 수정 성공")
    void update_byAuthor_success() {
        ContentDto.UpdateRequest request = new ContentDto.UpdateRequest();
        request.setTitle("수정된 제목");
        request.setDescription("수정된 내용");

        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        ContentDto.Response response = contentService.update(1L, request, author);

        assertThat(response.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    @DisplayName("ADMIN은 타인 콘텐츠 수정 가능")
    void update_byAdmin_success() {
        ContentDto.UpdateRequest request = new ContentDto.UpdateRequest();
        request.setTitle("관리자 수정");
        request.setDescription("관리자가 수정함");

        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        ContentDto.Response response = contentService.update(1L, request, admin);

        assertThat(response.getTitle()).isEqualTo("관리자 수정");
    }

    @Test
    @DisplayName("다른 사용자가 수정 시 예외")
    void update_byOtherUser_throws() {
        ContentDto.UpdateRequest request = new ContentDto.UpdateRequest();
        request.setTitle("해킹 시도");
        request.setDescription("해킹 내용");

        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        assertThatThrownBy(() -> contentService.update(1L, request, otherUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("작성자 본인이 삭제 성공")
    void delete_byAuthor_success() {
        given(contentRepository.findById(1L)).willReturn(Optional.of(content));
        doNothing().when(contentRepository).delete(content);

        assertThatCode(() -> contentService.delete(1L, author))
                .doesNotThrowAnyException();
        verify(contentRepository).delete(content);
    }

    @Test
    @DisplayName("다른 사용자가 삭제 시 예외")
    void delete_byOtherUser_throws() {
        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        assertThatThrownBy(() -> contentService.delete(1L, otherUser))
                .isInstanceOf(AccessDeniedException.class);
    }
}