package com.malgn.domain.content.service;

import com.malgn.domain.content.dto.ContentDto;
import com.malgn.domain.content.entity.Content;
import com.malgn.domain.content.repository.ContentRepository;
import com.malgn.domain.user.entity.User;
import com.malgn.global.exception.AccessDeniedException;
import com.malgn.global.exception.ResourceNotFoundException;
import com.malgn.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    // 콘텐츠 저장 - 생성일은 JPA Auditing에서 자동 입력
    @Transactional
    public ContentDto.Response create(ContentDto.CreateRequest request) {
        Content content = Content.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return ContentDto.Response.from(contentRepository.save(content));
    }

    // 제목 검색어 있으면 필터링, 없으면 전체 페이징
    @Transactional(readOnly = true)
    public Page<ContentDto.SummaryResponse> findAll(String title, Pageable pageable) {
        if (StringUtils.hasText(title)) {
            return contentRepository
                    .findByTitleContainingIgnoreCase(title, pageable)
                    .map(ContentDto.SummaryResponse::from);
        }
        return contentRepository.findAll(pageable)
                .map(ContentDto.SummaryResponse::from);
    }

    // 조회할때 마다 조회수 증가
    @Transactional
    public ContentDto.Response findById(Long id) {
        Content content = getContentOrThrow(id);
        content.increaseViewCount();
        return ContentDto.Response.from(content);
    }

    // 권한 체크 후 수정 호출 - setter 대신 도메인 메서드 사용
    @Transactional
    public ContentDto.Response update(Long id, ContentDto.UpdateRequest request,
                                      UserPrincipal currentUser) {
        Content content = getContentOrThrow(id);
        checkPermission(content, currentUser);
        content.update(request.getTitle(), request.getDescription());
        return ContentDto.Response.from(content);
    }

    // 권한 체크 후 삭제
    @Transactional
    public void delete(Long id, UserPrincipal currentUser) {
        Content content = getContentOrThrow(id);
        checkPermission(content, currentUser);
        contentRepository.delete(content);
    }

    // private
    private Content getContentOrThrow(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Content not found with id: " + id));
    }

    // ADMIN이면 통과 아니면 본인 확인
    private void checkPermission(Content content, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + User.Role.ADMIN.name()));

        if (!isAdmin && !content.getCreatedBy().equals(currentUser.getUsername())) {
            throw new AccessDeniedException(
                    "You do not have permission to modify this content");
        }
    }
}