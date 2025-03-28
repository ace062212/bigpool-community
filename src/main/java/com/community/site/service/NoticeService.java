package com.community.site.service;

import com.community.site.model.Notice;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeService {
    Notice createNotice(Notice notice);
    Optional<Notice> findById(Long id);
    Page<Notice> findAllNotices(Pageable pageable);
    Page<Notice> findByAuthor(User author, Pageable pageable);
    Page<Notice> searchNotices(String keyword, Pageable pageable);
    Page<Notice> findPinnedNotices(Pageable pageable);
    Page<Notice> findByPriorityOrder(Pageable pageable);
    Notice updateNotice(Notice notice);
    void deleteNotice(Long id);
    Notice incrementViewCount(Long id);
} 