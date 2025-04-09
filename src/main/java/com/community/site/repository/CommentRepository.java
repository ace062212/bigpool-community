package com.community.site.repository;

import com.community.site.model.BigPicture;
import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    Page<Comment> findByPost(Post post, Pageable pageable);
    Page<Comment> findByAuthor(User author, Pageable pageable);
    
    // BigPicture 관련 메서드
    List<Comment> findByBigPictureOrderByCreatedAtDesc(BigPicture bigPicture);
    Page<Comment> findByBigPicture(BigPicture bigPicture, Pageable pageable);
} 