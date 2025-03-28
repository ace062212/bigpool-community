package com.community.site.controller;

import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.service.CommentService;
import com.community.site.service.PostService;
import com.community.site.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;
    
    public CommentController(CommentService commentService, PostService postService, UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long postId,
                             @Valid @ModelAttribute("newComment") Comment comment,
                             BindingResult result,
                             Principal principal,
                             RedirectAttributes attributes) {
        try {
            if (result.hasErrors()) {
                attributes.addFlashAttribute("commentError", "댓글은 비어있을 수 없습니다.");
                return "redirect:/posts/" + postId;
            }

            Post post = postService.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            comment.setPost(post);
            comment.setAuthor(user);

            commentService.createComment(comment);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "댓글 추가 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateComment(@PathVariable Long id,
                              @RequestParam("content") String content,
                              Principal principal,
                              RedirectAttributes attributes) {
        try {
            Comment comment = commentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 작성자 또는 관리자만 수정할 수 있음
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            if (!isAdmin && !comment.getAuthor().getId().equals(user.getId())) {
                throw new IllegalArgumentException("댓글을 수정할 권한이 없습니다.");
            }

            comment.setContent(content);
            commentService.updateComment(comment);

            return "redirect:/posts/" + comment.getPost().getId();
        } catch (Exception e) {
            e.printStackTrace();
            Long postId = 0L;
            try {
                Comment comment = commentService.findById(id).orElse(null);
                if (comment != null && comment.getPost() != null) {
                    postId = comment.getPost().getId();
                }
            } catch (Exception ex) {
                // 무시
            }
            attributes.addFlashAttribute("error", "댓글 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/posts/" + (postId > 0 ? postId : "");
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteComment(@PathVariable Long id,
                               Principal principal,
                               RedirectAttributes attributes) {
        try {
            Comment comment = commentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 작성자 또는 관리자만 삭제할 수 있음
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            if (!isAdmin && !comment.getAuthor().getId().equals(user.getId())) {
                throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
            }

            Long postId = comment.getPost().getId();
            commentService.deleteComment(id);
            attributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");

            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            Long postId = 0L;
            try {
                Comment comment = commentService.findById(id).orElse(null);
                if (comment != null && comment.getPost() != null) {
                    postId = comment.getPost().getId();
                }
            } catch (Exception ex) {
                // 무시
            }
            attributes.addFlashAttribute("error", "댓글 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/posts/" + (postId > 0 ? postId : "");
        }
    }
} 