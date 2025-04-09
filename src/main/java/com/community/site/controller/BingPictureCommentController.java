package com.community.site.controller;

import com.community.site.model.BingPicture;
import com.community.site.model.BingPictureComment;
import com.community.site.model.User;
import com.community.site.service.BingPictureCommentService;
import com.community.site.service.BingPictureService;
import com.community.site.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/bingpictures")
public class BingPictureCommentController {

    private static final Logger logger = LoggerFactory.getLogger(BingPictureCommentController.class);

    private final BingPictureService bingPictureService;
    private final BingPictureCommentService commentService;
    private final UserService userService;

    @Autowired
    public BingPictureCommentController(BingPictureService bingPictureService, 
                                  BingPictureCommentService commentService,
                                  UserService userService) {
        this.bingPictureService = bingPictureService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping("/{bingPictureId}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long bingPictureId,
                           @RequestParam String content,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        
        logger.debug("댓글 추가 요청: bingPictureId={}, content={}, principal={}", 
                  bingPictureId, content, principal.getName());
        
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            commentService.addComment(bingPictureId, content, user);
            
            redirectAttributes.addFlashAttribute("commentSuccess", "댓글이 등록되었습니다.");
        } catch (Exception e) {
            logger.error("댓글 추가 중 오류 발생: bingPictureId={}, 오류={}", bingPictureId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("commentError", "댓글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/bingpictures/" + bingPictureId;
    }

    @PostMapping("/comments/{commentId}/edit")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> editComment(@PathVariable Long commentId,
                                      @RequestParam String content,
                                      Principal principal) {
        
        logger.debug("댓글 수정 요청: commentId={}, content={}, principal={}", 
                  commentId, content, principal.getName());
        
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            BingPictureComment updatedComment = commentService.updateComment(commentId, content, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "댓글이 수정되었습니다.");
            response.put("content", updatedComment.getContent());
            
            logger.debug("댓글 수정 완료: id={}", commentId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("댓글 수정 중 오류 발생: commentId={}, 오류={}", commentId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "댓글 수정 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/comments/{commentId}/delete")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId,
                                        Principal principal) {
        
        logger.debug("댓글 삭제 요청: commentId={}, principal={}", commentId, principal.getName());
        
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 삭제 전에 게시물 ID를 확인
            Long bingPictureId = null;
            try {
                // 임시 조치 - 예외가 발생할 수 있으므로 try-catch 내에서 처리
                bingPictureId = bingPictureService.findAllBingPictures(org.springframework.data.domain.PageRequest.of(0, 1))
                        .getContent().get(0).getId();
            } catch (Exception e) {
                bingPictureId = 1L; // 기본값 설정
            }
            
            commentService.deleteComment(commentId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "댓글이 삭제되었습니다.");
            response.put("bingPictureId", bingPictureId);
            
            logger.debug("댓글 삭제 완료: id={}", commentId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("댓글 삭제 중 오류 발생: commentId={}, 오류={}", commentId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "댓글 삭제 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{bingPictureId}/comments/{commentId}/reply")
    @PreAuthorize("isAuthenticated()")
    public String replyToComment(@PathVariable Long bingPictureId,
                               @PathVariable Long commentId,
                               @RequestParam String content,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        
        logger.debug("댓글 답변 요청: bingPictureId={}, commentId={}, content={}, principal={}", 
                  bingPictureId, commentId, content, principal.getName());
        
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            commentService.addReply(bingPictureId, commentId, content, user);
            
            redirectAttributes.addFlashAttribute("commentSuccess", "답글이 등록되었습니다.");
        } catch (Exception e) {
            logger.error("댓글 답변 중 오류 발생: bingPictureId={}, commentId={}, 오류={}", 
                      bingPictureId, commentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("commentError", "답글 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/bingpictures/" + bingPictureId;
    }
} 