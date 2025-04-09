package com.community.site.repository;

import com.community.site.model.BingPicture;
import com.community.site.model.BingPictureComment;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BingPictureCommentRepository extends JpaRepository<BingPictureComment, Long> {
    
    // 특정 빙픽처에 대한 모든 댓글 조회 (생성 시간 오름차순)
    List<BingPictureComment> findByBingPictureOrderByCreatedAtAsc(BingPicture bingPicture);
    
    // 특정 빙픽처에 대한 모든 댓글 조회 (페이징)
    Page<BingPictureComment> findByBingPicture(BingPicture bingPicture, Pageable pageable);
    
    // 특정 빙픽처에 대한 최상위 댓글만 조회 (답글 제외, 생성 시간 오름차순)
    List<BingPictureComment> findByBingPictureAndParentCommentIsNullOrderByCreatedAtAsc(BingPicture bingPicture);
    
    // 특정 댓글에 대한 답글 조회 (생성 시간 오름차순)
    List<BingPictureComment> findByParentCommentOrderByCreatedAtAsc(BingPictureComment parentComment);
    
    // 특정 빙픽처에 대한 댓글 수 계산
    long countByBingPicture(BingPicture bingPicture);
    
    // 특정 작성자의 댓글 조회 (페이징)
    Page<BingPictureComment> findByAuthor(User author, Pageable pageable);
    
    // 최근 댓글 조회 (생성 시간 내림차순, 페이징)
    Page<BingPictureComment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 삭제되지 않은 댓글만 조회 (생성 시간 오름차순)
    List<BingPictureComment> findByBingPictureAndDeletedFalseOrderByCreatedAtAsc(BingPicture bingPicture);
    
    // 삭제되지 않은 최상위 댓글만 조회 (답글 제외, 생성 시간 오름차순)
    List<BingPictureComment> findByBingPictureAndParentCommentIsNullAndDeletedFalseOrderByCreatedAtAsc(BingPicture bingPicture);
}
