package com.malgn.domain.content.controller;

import com.malgn.domain.content.dto.ContentDto;
import com.malgn.domain.content.service.ContentService;
import com.malgn.global.dto.ApiResponse;
import com.malgn.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Contents", description = "콘텐츠 관리 API")
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    @Operation(summary = "콘텐츠 생성",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ContentDto.Response>> create(
            @Valid @RequestBody ContentDto.CreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Content created successfully",
                        contentService.create(request)));
    }

    @GetMapping
    @Operation(summary = "콘텐츠 목록 조회",
            description = "페이징 처리. title로 검색, sort로 정렬 가능\n\n" +
                    "정렬 예시: `sort=createdDate,desc` / `sort=viewCount,desc` / `sort=title,asc`")
    public ResponseEntity<ApiResponse<Page<ContentDto.SummaryResponse>>> findAll(
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "createdDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                contentService.findAll(title, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "콘텐츠 상세 조회",
            description = "조회할 때마다 view_count 1 증가")
    public ResponseEntity<ApiResponse<ContentDto.Response>> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contentService.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "콘텐츠 수정",
            description = "작성자 본인 또는 ADMIN만 가능",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ContentDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody ContentDto.UpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Content updated successfully",
                contentService.update(id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "콘텐츠 삭제",
            description = "작성자 본인 또는 ADMIN만 가능",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        contentService.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Content deleted successfully"));
    }
}