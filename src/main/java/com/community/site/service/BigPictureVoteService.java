package com.community.site.service;

import com.community.site.model.BigPicture;
import com.community.site.model.User;
import com.community.site.entity.BigPictureVote;
import com.community.site.repository.BigPictureRepository;
import com.community.site.repository.BigPictureVoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BigPictureVoteService {
    
    private static final Logger logger = LoggerFactory.getLogger(BigPictureVoteService.class);
    
    private final BigPictureVoteRepository bigPictureVoteRepository;
    private final BigPictureRepository bigPictureRepository;
    
    public BigPictureVoteService(BigPictureVoteRepository bigPictureVoteRepository, BigPictureRepository bigPictureRepository) {
        this.bigPictureVoteRepository = bigPictureVoteRepository;
        this.bigPictureRepository = bigPictureRepository;
    }
    
    @Transactional
    public void vote(Long bigPictureId, User user, boolean isUpvote) {
        logger.info("BigPicture 투표 시작: bigPictureId={}, userId={}, isUpvote={}", bigPictureId, user.getId(), isUpvote);
        
        if (bigPictureId == null) {
            logger.error("BigPicture ID가 null입니다.");
            throw new IllegalArgumentException("BigPicture ID가 null입니다.");
        }
        
        if (user == null) {
            logger.error("사용자가 null입니다.");
            throw new IllegalArgumentException("사용자가 null입니다.");
        }
        
        BigPicture bigPicture = bigPictureRepository.findById(bigPictureId)
                .orElseThrow(() -> {
                    logger.error("BigPicture를 찾을 수 없습니다: {}", bigPictureId);
                    return new IllegalArgumentException("BigPicture를 찾을 수 없습니다: " + bigPictureId);
                });
        
        logger.debug("BigPicture 찾음: id={}, 제목={}", bigPicture.getId(), bigPicture.getTitle());
        
        try {
            Optional<BigPictureVote> existingVote = bigPictureVoteRepository.findByBigPictureAndUser(bigPicture, user);
            
            if (existingVote.isPresent()) {
                BigPictureVote vote = existingVote.get();
                logger.debug("기존 투표 찾음: id={}, isUpvote={}", vote.getId(), vote.isUpvote());
                
                if (vote.isUpvote() == isUpvote) {
                    // 같은 투표를 한 경우 투표 취소
                    logger.info("같은 투표 타입이므로 취소합니다: {}", isUpvote ? "추천" : "비추천");
                    bigPictureVoteRepository.delete(vote);
                    bigPictureVoteRepository.flush();
                    logger.info("투표 취소 완료: id={}", vote.getId());
                } else {
                    // 반대 투표로 변경 - 기존 투표 삭제 후 새로 생성
                    logger.info("반대 투표로 변경합니다: {} -> {}", 
                              vote.isUpvote() ? "추천" : "비추천", 
                              isUpvote ? "추천" : "비추천");
                    
                    // 기존 투표 삭제
                    bigPictureVoteRepository.delete(vote);
                    bigPictureVoteRepository.flush();
                    
                    // 새 투표 생성
                    BigPictureVote newVote = new BigPictureVote(bigPicture, user, isUpvote);
                    bigPictureVoteRepository.save(newVote);
                    bigPictureVoteRepository.flush();
                    logger.info("투표 타입 변경 완료: id={}, 새로운 타입={}", newVote.getId(), isUpvote ? "추천" : "비추천");
                }
            } else {
                // 새로운 투표 생성
                logger.info("새 투표 생성: bigPictureId={}, userId={}, isUpvote={}", 
                          bigPicture.getId(), user.getId(), isUpvote);
                BigPictureVote newVote = new BigPictureVote(bigPicture, user, isUpvote);
                bigPictureVoteRepository.save(newVote);
                bigPictureVoteRepository.flush();
                logger.info("새 투표 저장됨: id={}", newVote.getId());
            }
            
            logger.info("투표 처리 완료: bigPictureId={}, userId={}, isUpvote={}", 
                      bigPicture.getId(), user.getId(), isUpvote);
        } catch (Exception e) {
            logger.error("투표 처리 중 오류 발생: bigPictureId={}, userId={}, 오류={}", 
                      bigPictureId, user.getId(), e.getMessage(), e);
            throw new RuntimeException("투표 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<BigPictureVote> findByBigPictureAndUser(BigPicture bigPicture, User user) {
        if (bigPicture == null || user == null) {
            logger.warn("투표 조회 실패: bigPicture 또는 user가 null입니다. bigPicture={}, user={}", 
                bigPicture != null ? bigPicture.getId() : "null", user != null ? user.getId() : "null");
            return Optional.empty();
        }
        
        try {
            logger.debug("투표 조회: bigPictureId={}, userId={}", bigPicture.getId(), user.getId());
            return bigPictureVoteRepository.findByBigPictureAndUser(bigPicture, user);
        } catch (Exception e) {
            logger.error("투표 조회 중 오류 발생: bigPictureId={}, userId={}, 오류={}", 
                bigPicture.getId(), user.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Transactional(readOnly = true)
    public long getUpvoteCount(BigPicture bigPicture) {
        if (bigPicture == null) {
            logger.warn("추천 수 조회 실패: bigPicture가 null입니다");
            return 0;
        }
        
        try {
            long count = bigPictureVoteRepository.countUpvotesByBigPicture(bigPicture);
            logger.debug("추천 수 조회: bigPictureId={}, count={}", bigPicture.getId(), count);
            return count;
        } catch (Exception e) {
            logger.error("추천 수 조회 중 오류 발생: bigPictureId={}, 오류={}", 
                bigPicture.getId(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Transactional(readOnly = true)
    public long getDownvoteCount(BigPicture bigPicture) {
        if (bigPicture == null) {
            logger.warn("비추천 수 조회 실패: bigPicture가 null입니다");
            return 0;
        }
        
        try {
            long count = bigPictureVoteRepository.countDownvotesByBigPicture(bigPicture);
            logger.debug("비추천 수 조회: bigPictureId={}, count={}", bigPicture.getId(), count);
            return count;
        } catch (Exception e) {
            logger.error("비추천 수 조회 중 오류 발생: bigPictureId={}, 오류={}", 
                bigPicture.getId(), e.getMessage(), e);
            return 0;
        }
    }
} 