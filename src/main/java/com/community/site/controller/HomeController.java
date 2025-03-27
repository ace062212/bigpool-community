package com.community.site.controller;

import com.community.site.model.Post;
import com.community.site.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final PostService postService;
    
    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({"", "/", "/home"})
    public String home(Model model, 
                       @RequestParam(defaultValue = "0") int page, 
                       @RequestParam(defaultValue = "10") int size) {
        
        Page<Post> posts = postService.findAllPosts(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());
        
        return "home";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
} 