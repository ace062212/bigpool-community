package com.community.site.controller;

import com.community.site.model.Notice;
import com.community.site.model.Post;
import com.community.site.service.NoticeService;
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
    private final NoticeService noticeService;
    
    public HomeController(PostService postService, NoticeService noticeService) {
        this.postService = postService;
        this.noticeService = noticeService;
    }

    @GetMapping({"", "/", "/home"})
    public String home(Model model, Principal principal) {
        // 로그인 하지 않은 사용자는 랜딩 페이지로
        if (principal == null) {
            return "landing";
        }
        
        // 최신 게시물 조회 (최대 4개)
        Page<Post> posts = postService.findAllPosts(
            PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        // 주요 공지사항 조회 (고정된 공지, 없으면 최신순 1개)
        Page<Notice> pinnedNotices = noticeService.findPinnedNotices(
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "priority", "createdAt"))
        );
        
        // 고정된 공지가 없으면 최신 공지사항 1개를 가져옴
        if (pinnedNotices.isEmpty()) {
            pinnedNotices = noticeService.findAllNotices(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
            );
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("pinnedNotices", pinnedNotices);
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