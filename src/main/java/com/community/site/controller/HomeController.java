package com.community.site.controller;

import com.community.site.model.Post;
import com.community.site.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    private final PostService postService;
    
    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({"", "/", "/home"})
    public String home(Model model, Principal principal) {
        // 로그인 하지 않은 사용자는 랜딩 페이지로
        if (principal == null) {
            return "landing";
        }
        
        // 로그인한 경우 홈페이지로
        Page<Post> posts = postService.findAllPosts(
            PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", posts.getTotalPages());
        
        return "home";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/landing")
    public String landing() {
        return "landing";
    }

    /**
     * 로그아웃 성공 후 랜딩 페이지로 리다이렉트
     */
    @GetMapping("/logout-success")
    public String logoutSuccess() {
        return "redirect:/";
    }
} 