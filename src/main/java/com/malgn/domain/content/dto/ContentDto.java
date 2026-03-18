package com.malgn.domain.content.dto;

import com.malgn.domain.content.entity.Content;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class ContentDto {

    // 콘텐츠 생성
    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        private String title;

        private String description;
    }

    // 콘텐츠 수정
    @Getter
    @Setter
    public static class UpdateRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        private String title;

        private String description;
    }

    // 콘텐츠 상세
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Long viewCount;
        private LocalDateTime createdDate;
        private String createdBy;
        private LocalDateTime lastModifiedDate;
        private String lastModifiedBy;

        public static Response from(Content content) {
            return Response.builder()
                    .id(content.getId())
                    .title(content.getTitle())
                    .description(content.getDescription())
                    .viewCount(content.getViewCount())
                    .createdDate(content.getCreatedDate())
                    .createdBy(content.getCreatedBy())
                    .lastModifiedDate(content.getLastModifiedDate())
                    .lastModifiedBy(content.getLastModifiedBy())
                    .build();
        }
    }

    // 콘첸츠 목록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryResponse {
        private Long id;
        private String title;
        private Long viewCount;
        private LocalDateTime createdDate;
        private String createdBy;

        public static SummaryResponse from(Content content) {
            return SummaryResponse.builder()
                    .id(content.getId())
                    .title(content.getTitle())
                    .viewCount(content.getViewCount())
                    .createdDate(content.getCreatedDate())
                    .createdBy(content.getCreatedBy())
                    .build();
        }
    }
}