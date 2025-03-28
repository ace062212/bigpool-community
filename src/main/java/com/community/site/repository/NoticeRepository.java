package com.community.site.repository;

import com.community.site.model.Notice;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findByAuthor(User author, Pageable pageable);
    Page<Notice> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Notice> findByPinnedTrue(Pageable pageable);
    Page<Notice> findByOrderByPriorityDescCreatedAtDesc(Pageable pageable);
} 