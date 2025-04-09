package com.community.site.service;

import com.community.site.model.BingPicture;
import com.community.site.model.BingPictureComment;
import com.community.site.model.User;
import com.community.site.repository.BingPictureCommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("bingPictureCommentServiceImpl")
public class BingPictureCommentServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(BingPictureCommentServiceImpl.class);
    
    private final BingPictureCommentRepository bingPictureCommentRepository;
    
    @Autowired
    public BingPictureCommentServiceImpl(BingPictureCommentRepository bingPictureCommentRepository) {
        this.bingPictureCommentRepository = bingPictureCommentRepository;
    }

    @Transactional
    public BingPictureComment createComment(BingPictureComment comment) {
        logger.debug("댓글 생성 시작: bingPictureId={}, userId={}", 
                   comment.getBingPicture().getId(), comment.getAuthor().getId());
        
        BingPictureComment savedComment = bingPictureCommentRepository.save(comment);
        logger.info("댓글 저장 완료: id={}", savedComment.getId());
        return savedComment;
    }

    @Transactional(readOnly = true)
    public Optional<BingPictureComment> findById(Long id) {
        logger.debug("댓글 조회: id={}", id);
        return bingPictureCommentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<BingPictureComment> findByBingPicture(BingPicture bingPicture) {
        logger.debug("게시글별 댓글 조회: bingPictureId={}", bingPicture.getId());
        return bingPictureCommentRepository.findByBingPictureOrderByCreatedAtAsc(bingPicture);
    }

    @Transactional(readOnly = true)
    public Page<BingPictureComment> findByBingPicturePaginated(BingPicture bingPicture, Pageable pageable) {
        logger.debug("게시글별 댓글 페이지 조회: bingPictureId={}, page={}, size={}", 
                   bingPicture.getId(), pageable.getPageNumber(), pageable.getPageSize());
        return bingPictureCommentRepository.findByBingPicture(bingPicture, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BingPictureComment> findByAuthor(User author, Pageable pageable) {
        logger.debug("사용자별 댓글 조회: userId={}, page={}, size={}", 
                   author.getId(), pageable.getPageNumber(), pageable.getPageSize());
        return bingPictureCommentRepository.findByAuthor(author, pageable);
    }

    @Transactional
    public BingPictureComment updateComment(BingPictureComment comment) {
        logger.debug("댓글 업데이트: id={}", comment.getId());
        
        BingPictureComment updatedComment = bingPictureCommentRepository.save(comment);
        logger.info("댓글 업데이트 완료: id={}", updatedComment.getId());
        return updatedComment;
    }

    @Transactional
    public void deleteComment(Long id) {
        logger.debug("댓글 삭제: id={}", id);
        
        bingPictureCommentRepository.deleteById(id);
        logger.info("댓글 삭제 완료: id={}", id);
    }
} 