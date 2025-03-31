package com.community.site.service;

import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.entity.Vote;
import com.community.site.repository.PostRepository;
import com.community.site.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VoteService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoteService.class);
    
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    
    public VoteService(VoteRepository voteRepository, PostRepository postRepository) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
    }
    
    @Transactional
    public void vote(Long postId, User user, boolean isUpvote) {
        logger.info("투표 시작: postId={}, userId={}, isUpvote={}", postId, user.getId(), isUpvote);
        
        if (postId == null) {
            logger.error("게시글 ID가 null입니다.");
            throw new IllegalArgumentException("게시글 ID가 null입니다.");
        }
        
        if (user == null) {
            logger.error("사용자가 null입니다.");
            throw new IllegalArgumentException("사용자가 null입니다.");
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.error("게시글을 찾을 수 없습니다: {}", postId);
                    return new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId);
                });
        
        logger.debug("게시글 찾음: id={}, 제목={}", post.getId(), post.getTitle());
        
        try {
            Optional<Vote> existingVote = voteRepository.findByPostAndUser(post, user);
            
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                logger.debug("기존 투표 찾음: id={}, isUpvote={}", vote.getId(), vote.isUpvote());
                
                if (vote.isUpvote() == isUpvote) {
                    // 같은 투표를 한 경우 투표 취소
                    logger.info("같은 투표 타입이므로 취소합니다: {}", isUpvote ? "추천" : "비추천");
                    voteRepository.delete(vote);
                    voteRepository.flush();
                    logger.info("투표 취소 완료: id={}", vote.getId());
                } else {
                    // 반대 투표로 변경 - 기존 투표 삭제 후 새로 생성
                    logger.info("반대 투표로 변경합니다: {} -> {}", 
                              vote.isUpvote() ? "추천" : "비추천", 
                              isUpvote ? "추천" : "비추천");
                    
                    // 기존 투표 삭제
                    voteRepository.delete(vote);
                    voteRepository.flush();
                    
                    // 새 투표 생성
                    Vote newVote = new Vote(post, user, isUpvote);
                    voteRepository.save(newVote);
                    voteRepository.flush();
                    logger.info("투표 타입 변경 완료: id={}, 새로운 타입={}", newVote.getId(), isUpvote ? "추천" : "비추천");
                }
            } else {
                // 새로운 투표 생성
                logger.info("새 투표 생성: postId={}, userId={}, isUpvote={}", 
                          post.getId(), user.getId(), isUpvote);
                Vote newVote = new Vote(post, user, isUpvote);
                voteRepository.save(newVote);
                voteRepository.flush();
                logger.info("새 투표 저장됨: id={}", newVote.getId());
            }
            
            logger.info("투표 처리 완료: postId={}, userId={}, isUpvote={}", 
                      post.getId(), user.getId(), isUpvote);
        } catch (Exception e) {
            logger.error("투표 처리 중 오류 발생: postId={}, userId={}, 오류={}", 
                      postId, user.getId(), e.getMessage(), e);
            throw new RuntimeException("투표 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<Vote> findByPostAndUser(Post post, User user) {
        if (post == null || user == null) {
            logger.warn("투표 조회 실패: post 또는 user가 null입니다. post={}, user={}", 
                post != null ? post.getId() : "null", user != null ? user.getId() : "null");
            return Optional.empty();
        }
        
        try {
            logger.debug("투표 조회: postId={}, userId={}", post.getId(), user.getId());
            return voteRepository.findByPostAndUser(post, user);
        } catch (Exception e) {
            logger.error("투표 조회 중 오류 발생: postId={}, userId={}, 오류={}", 
                post.getId(), user.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Transactional(readOnly = true)
    public long getUpvoteCount(Post post) {
        if (post == null) {
            logger.warn("추천 수 조회 실패: post가 null입니다");
            return 0;
        }
        
        try {
            long count = voteRepository.countUpvotesByPost(post);
            logger.debug("추천 수 조회: postId={}, count={}", post.getId(), count);
            return count;
        } catch (Exception e) {
            logger.error("추천 수 조회 중 오류 발생: postId={}, 오류={}", 
                post.getId(), e.getMessage(), e);
            return 0;
        }
    }
    
    @Transactional(readOnly = true)
    public long getDownvoteCount(Post post) {
        if (post == null) {
            logger.warn("비추천 수 조회 실패: post가 null입니다");
            return 0;
        }
        
        try {
            long count = voteRepository.countDownvotesByPost(post);
            logger.debug("비추천 수 조회: postId={}, count={}", post.getId(), count);
            return count;
        } catch (Exception e) {
            logger.error("비추천 수 조회 중 오류 발생: postId={}, 오류={}", 
                post.getId(), e.getMessage(), e);
            return 0;
        }
    }
} 