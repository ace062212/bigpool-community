package com.community.site.controller;

import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.service.CommentService;
import com.community.site.service.PostService;
import com.community.site.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    
    public PostController(PostService postService, UserService userService, CommentService commentService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listPosts(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String search) {
        
        try {
            Page<Post> posts;
            if (search != null && !search.isEmpty()) {
                posts = postService.searchPosts(search, 
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
                model.addAttribute("search", search);
            } else {
                posts = postService.findAllPosts(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
            }
            
            model.addAttribute("posts", posts);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", posts.getTotalPages());
            
            return "post/list";
        } catch (Exception e) {
            // 로그 기록
            e.printStackTrace();
            model.addAttribute("errorMessage", "게시글 목록을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String viewPost(@PathVariable Long id, Model model, Principal principal) {
        try {
            Post post = postService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: ID = " + id));
            
            // 조회수 증가
            post = postService.incrementViewCount(id);
            
            List<Comment> comments = commentService.findByPost(post);
            if (comments == null) {
                comments = new ArrayList<>();
            }
            
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new Comment());
            
            // 현재 사용자가 게시글 작성자인지 확인
            boolean isAuthor = false;
            if (principal != null && post.getAuthor() != null) {
                User user = userService.findByUsername(principal.getName())
                        .orElse(null);
                if (user != null && post.getAuthor().getId() != null) {
                    isAuthor = user.getId().equals(post.getAuthor().getId());
                }
            }
            model.addAttribute("isAuthor", isAuthor);
            
            return "post/view";
        } catch (IllegalArgumentException e) {
            // 게시글 존재하지 않는 경우 404
            model.addAttribute("message", e.getMessage());
            return "error/404";
        } catch (Exception e) {
            // 기타 예외
            e.printStackTrace();
            model.addAttribute("message", "게시글을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String newPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createPost(@Valid @ModelAttribute Post post,
                            BindingResult result,
                            Principal principal) {
        if (result.hasErrors()) {
            return "post/form";
        }
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        post.setAuthor(user);
        Post savedPost = postService.createPost(post);
        
        return "redirect:/posts/" + savedPost.getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editPostForm(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자만 수정할 수 있음
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }
        
        model.addAttribute("post", post);
        return "post/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updatePost(@PathVariable Long id,
                            @Valid @ModelAttribute Post post,
                            BindingResult result,
                            Principal principal) {
        if (result.hasErrors()) {
            return "post/form";
        }
        
        Post existingPost = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자만 수정할 수 있음
        if (!existingPost.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }
        
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        
        postService.updatePost(existingPost);
        
        return "redirect:/posts/" + existingPost.getId();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deletePost(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자만 삭제할 수 있음
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }
        
        postService.deletePost(id);
        attributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        
        return "redirect:/posts";
    }
} 