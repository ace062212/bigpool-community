package com.community.site.repository;

import com.community.site.model.BigPicture;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BigPictureRepository extends JpaRepository<BigPicture, Long> {
    Page<BigPicture> findByAuthor(User author, Pageable pageable);
    Page<BigPicture> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
} 