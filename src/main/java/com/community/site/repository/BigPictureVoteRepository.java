package com.community.site.repository;

import com.community.site.entity.BigPictureVote;
import com.community.site.model.BigPicture;
import com.community.site.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BigPictureVoteRepository extends JpaRepository<BigPictureVote, Long> {
    Optional<BigPictureVote> findByBigPictureAndUser(BigPicture bigPicture, User user);
    
    @Query("SELECT COUNT(v) FROM BigPictureVote v WHERE v.bigPicture = :bigPicture AND v.isUpvote = true")
    long countUpvotesByBigPicture(@Param("bigPicture") BigPicture bigPicture);
    
    @Query("SELECT COUNT(v) FROM BigPictureVote v WHERE v.bigPicture = :bigPicture AND v.isUpvote = false")
    long countDownvotesByBigPicture(@Param("bigPicture") BigPicture bigPicture);
} 