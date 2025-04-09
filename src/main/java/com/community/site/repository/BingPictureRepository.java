package com.community.site.repository;

import com.community.site.model.BingPicture;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BingPictureRepository extends JpaRepository<BingPicture, Long> {
    Page<BingPicture> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<BingPicture> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    
    @Query("SELECT b FROM BingPicture b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword% ORDER BY b.createdAt DESC")
    Page<BingPicture> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);
} 