package com.community.site.service;

import com.community.site.model.BingPicture;
import com.community.site.model.BingPictureComment;
import com.community.site.model.User;
import com.community.site.repository.BingPictureCommentRepository;
import com.community.site.repository.BingPictureRepository;
import com.community.site.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class BingPictureCommentService {
    
    private static final Logger logger = LoggerFactory.getLogger(BingPictureCommentService.class);
    
    private final BingPictureCommentRepository commentRepository;
    private final BingPictureRepository bingPictureRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public BingPictureCommentService(BingPictureCommentRepository commentRepository, 
                                   BingPictureRepository bingPictureRepository,
                                   UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bingPictureRepository = bingPictureRepository;
        this.userRepository = userRepository;
    }
    
    // 댓글 등록
    public BingPictureComment addComment(Long bingPictureId, String content, User author) {
        logger.info("사용자 {}가 Bing 사진 ID {}에 댓글 추가 시도", author.getUsername(), bingPictureId);
        
        BingPicture bingPicture = bingPictureRepository.findById(bingPictureId)
                .orElseThrow(() -> {
                    logger.error("댓글 추가 실패: Bing 사진 ID {}를 찾을 수 없음", bingPictureId);
                    return new NoSuchElementException("해당 Bing 사진을 찾을 수 없습니다: " + bingPictureId);
                });
        
        BingPictureComment comment = BingPictureComment.createComment(bingPicture, author, content);
        BingPictureComment savedComment = commentRepository.save(comment);
        logger.info("Bing 사진 ID {}에 댓글 ID {} 성공적으로 추가됨", bingPictureId, savedComment.getId());
        
        return savedComment;
    }
    
    // 답글 등록
    public BingPictureComment addReply(Long bingPictureId, Long parentCommentId, String content, User author) {
        logger.info("사용자 {}가 Bing 사진 ID {}, 부모 댓글 ID {}에 답글 추가 시도", 
                author.getUsername(), bingPictureId, parentCommentId);
        
        BingPicture bingPicture = bingPictureRepository.findById(bingPictureId)
                .orElseThrow(() -> {
                    logger.error("답글 추가 실패: Bing 사진 ID {}를 찾을 수 없음", bingPictureId);
                    return new NoSuchElementException("해당 Bing 사진을 찾을 수 없습니다: " + bingPictureId);
                });
        
        BingPictureComment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    logger.error("답글 추가 실패: 부모 댓글 ID {}를 찾을 수 없음", parentCommentId);
                    return new NoSuchElementException("해당 부모 댓글을 찾을 수 없습니다: " + parentCommentId);
                });
        
        BingPictureComment reply = BingPictureComment.createReply(bingPicture, author, content, parentComment);
        BingPictureComment savedReply = commentRepository.save(reply);
        logger.info("부모 댓글 ID {}에 답글 ID {} 성공적으로 추가됨", parentCommentId, savedReply.getId());
        
        return savedReply;
    }
    
    // 특정 Bing 사진의 모든 댓글 조회 (생성 시간 오름차순)
    @Transactional(readOnly = true)
    public List<BingPictureComment> getAllCommentsByBingPicture(Long bingPictureId) {
        logger.debug("Bing 사진 ID {}의 모든 댓글 조회", bingPictureId);
        
        BingPicture bingPicture = bingPictureRepository.findById(bingPictureId)
                .orElseThrow(() -> {
                    logger.error("댓글 조회 실패: Bing 사진 ID {}를 찾을 수 없음", bingPictureId);
                    return new NoSuchElementException("해당 Bing 사진을 찾을 수 없습니다: " + bingPictureId);
                });
        
        return commentRepository.findByBingPictureOrderByCreatedAtAsc(bingPicture);
    }
    
    // 특정 Bing 사진의 최상위 댓글만 조회 (답글 제외, 생성 시간 오름차순)
    @Transactional(readOnly = true)
    public List<BingPictureComment> getRootCommentsByBingPicture(Long bingPictureId) {
        logger.debug("Bing 사진 ID {}의 최상위 댓글 조회", bingPictureId);
        
        BingPicture bingPicture = bingPictureRepository.findById(bingPictureId)
                .orElseThrow(() -> {
                    logger.error("최상위 댓글 조회 실패: Bing 사진 ID {}를 찾을 수 없음", bingPictureId);
                    return new NoSuchElementException("해당 Bing 사진을 찾을 수 없습니다: " + bingPictureId);
                });
        
        return commentRepository.findByBingPictureAndParentCommentIsNullOrderByCreatedAtAsc(bingPicture);
    }
    
    // 특정 댓글의 모든 답글 조회 (생성 시간 오름차순)
    @Transactional(readOnly = true)
    public List<BingPictureComment> getRepliesByParentComment(Long parentCommentId) {
        logger.debug("부모 댓글 ID {}의 모든 답글 조회", parentCommentId);
        
        BingPictureComment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    logger.error("답글 조회 실패: 부모 댓글 ID {}를 찾을 수 없음", parentCommentId);
                    return new NoSuchElementException("해당 부모 댓글을 찾을 수 없습니다: " + parentCommentId);
                });
        
        return commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
    }
    
    // 댓글 수정
    public BingPictureComment updateComment(Long commentId, String newContent, User currentUser) {
        logger.info("사용자 {}가 댓글 ID {} 수정 시도", currentUser.getUsername(), commentId);
        
        BingPictureComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.error("댓글 수정 실패: 댓글 ID {}를 찾을 수 없음", commentId);
                    return new NoSuchElementException("해당 댓글을 찾을 수 없습니다: " + commentId);
                });
        
        // 권한 확인 (자신의 댓글만 수정 가능)
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            logger.warn("댓글 수정 실패: 사용자 {}는 댓글 ID {}의 작성자가 아님", currentUser.getUsername(), commentId);
            throw new IllegalArgumentException("자신의 댓글만 수정할 수 있습니다.");
        }
        
        comment.setContent(newContent);
        BingPictureComment updatedComment = commentRepository.save(comment);
        logger.info("댓글 ID {} 수정 완료", commentId);
        
        return updatedComment;
    }
    
    // 댓글 삭제 (소프트 삭제)
    public void deleteComment(Long commentId, User currentUser) {
        logger.info("사용자 {}가 댓글 ID {} 삭제 시도", currentUser.getUsername(), commentId);
        
        BingPictureComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.error("댓글 삭제 실패: 댓글 ID {}를 찾을 수 없음", commentId);
                    return new NoSuchElementException("해당 댓글을 찾을 수 없습니다: " + commentId);
                });
        
        // 권한 확인 (자신의 댓글만 삭제 가능)
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            logger.warn("댓글 삭제 실패: 사용자 {}는 댓글 ID {}의 작성자가 아님", currentUser.getUsername(), commentId);
            throw new IllegalArgumentException("자신의 댓글만 삭제할 수 있습니다.");
        }
        
        comment.markAsDeleted();
        commentRepository.save(comment);
        logger.info("댓글 ID {} 삭제 완료 (소프트 삭제)", commentId);
    }
    
    // 사용자별 댓글 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<BingPictureComment> getCommentsByUser(Long userId, Pageable pageable) {
        logger.debug("사용자 ID {}의 댓글 조회 (페이지: {})", userId, pageable);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("사용자별 댓글 조회 실패: 사용자 ID {}를 찾을 수 없음", userId);
                    return new NoSuchElementException("해당 사용자를 찾을 수 없습니다: " + userId);
                });
        
        return commentRepository.findByAuthor(user, pageable);
    }
    
    // 댓글 개수 조회
    @Transactional(readOnly = true)
    public long countCommentsByBingPicture(Long bingPictureId) {
        logger.debug("Bing 사진 ID {}의 댓글 개수 조회", bingPictureId);
        
        BingPicture bingPicture = bingPictureRepository.findById(bingPictureId)
                .orElseThrow(() -> {
                    logger.error("댓글 개수 조회 실패: Bing 사진 ID {}를 찾을 수 없음", bingPictureId);
                    return new NoSuchElementException("해당 Bing 사진을 찾을 수 없습니다: " + bingPictureId);
                });
        
        return commentRepository.countByBingPicture(bingPicture);
    }
} 