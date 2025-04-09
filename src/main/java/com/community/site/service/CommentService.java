package com.community.site.service;

import com.community.site.model.BigPicture;
import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    Comment createComment(Comment comment);
    Optional<Comment> findById(Long id);
    List<Comment> findByPost(Post post);
    Page<Comment> findByPostPaginated(Post post, Pageable pageable);
    Page<Comment> findByAuthor(User author, Pageable pageable);
    Comment updateComment(Comment comment);
    void deleteComment(Long id);
    
    // BigPicture 관련 메서드
    List<Comment> findByBigPicture(BigPicture bigPicture);
    Page<Comment> findByBigPicturePaginated(BigPicture bigPicture, Pageable pageable);
} 