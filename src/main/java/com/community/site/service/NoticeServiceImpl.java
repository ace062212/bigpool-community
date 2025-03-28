package com.community.site.service;

import com.community.site.model.Notice;
import com.community.site.model.User;
import com.community.site.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    
    public NoticeServiceImpl(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Override
    @Transactional
    public Notice createNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notice> findById(Long id) {
        return noticeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notice> findAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notice> findByAuthor(User author, Pageable pageable) {
        return noticeRepository.findByAuthor(author, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notice> searchNotices(String keyword, Pageable pageable) {
        return noticeRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Notice> findPinnedNotices(Pageable pageable) {
        return noticeRepository.findByPinnedTrue(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Notice> findByPriorityOrder(Pageable pageable) {
        return noticeRepository.findByOrderByPriorityDescCreatedAtDesc(pageable);
    }

    @Override
    @Transactional
    public Notice updateNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Notice incrementViewCount(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setViewCount(notice.getViewCount() + 1);
        return noticeRepository.save(notice);
    }
} 