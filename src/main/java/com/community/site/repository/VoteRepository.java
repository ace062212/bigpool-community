package com.community.site.repository;

import com.community.site.entity.Vote;
import com.community.site.model.Post;
import com.community.site.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPostAndUser(Post post, User user);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post = :post AND v.isUpvote = true")
    long countUpvotesByPost(@Param("post") Post post);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post = :post AND v.isUpvote = false")
    long countDownvotesByPost(@Param("post") Post post);
} 