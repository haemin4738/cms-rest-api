package com.malgn.domain.content.repository;

import com.malgn.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}