package com.community.site.service;

import com.community.site.model.BigPicture;
import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByPost(Post post) {
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findByPostPaginated(Post post, Pageable pageable) {
        return commentRepository.findByPost(post, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findByAuthor(User author, Pageable pageable) {
        return commentRepository.findByAuthor(author, pageable);
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
    
    // BigPicture 관련 메서드 구현
    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByBigPicture(BigPicture bigPicture) {
        return commentRepository.findByBigPictureOrderByCreatedAtDesc(bigPicture);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findByBigPicturePaginated(BigPicture bigPicture, Pageable pageable) {
        return commentRepository.findByBigPicture(bigPicture, pageable);
    }
} 