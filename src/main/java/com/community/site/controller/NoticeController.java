package com.community.site.controller;

import com.community.site.model.Notice;
import com.community.site.model.User;
import com.community.site.service.NoticeService;
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

@Controller
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;
    
    public NoticeController(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @GetMapping
    public String listNotices(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String search) {
        
        try {
            Page<Notice> notices;
            if (search != null && !search.isEmpty()) {
                notices = noticeService.searchNotices(search, 
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
                model.addAttribute("search", search);
            } else {
                notices = noticeService.findByPriorityOrder(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "priority", "createdAt")));
            }
            
            model.addAttribute("notices", notices);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", notices.getTotalPages());
            
            return "notice/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "공지사항 목록을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/{id}")
    public String viewNotice(@PathVariable Long id, Model model, Principal principal) {
        try {
            Notice notice = noticeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: ID = " + id));
            
            // 조회수 증가
            notice = noticeService.incrementViewCount(id);
            
            model.addAttribute("notice", notice);
            
            // 현재 사용자가 관리자인지 확인
            boolean isAdmin = false;
            if (principal != null) {
                User user = userService.findByUsername(principal.getName())
                        .orElse(null);
                if (user != null) {
                    // ROLE_ADMIN 권한 체크
                    isAdmin = "ROLE_ADMIN".equals(user.getRole());
                }
            }
            model.addAttribute("isAdmin", isAdmin);
            
            return "notice/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            return "error/404";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "공지사항을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")  // 관리자만 공지사항 생성 가능
    public String newNoticeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "notice/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createNotice(@Valid @ModelAttribute Notice notice,
                            BindingResult result,
                            Principal principal) {
        if (result.hasErrors()) {
            return "notice/form";
        }
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        notice.setAuthor(user);
        Notice savedNotice = noticeService.createNotice(notice);
        
        return "redirect:/notices/" + savedNotice.getId();
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editNoticeForm(@PathVariable Long id, Model model) {
        Notice notice = noticeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        
        model.addAttribute("notice", notice);
        return "notice/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateNotice(@PathVariable Long id,
                            @Valid @ModelAttribute Notice notice,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "notice/form";
        }
        
        Notice existingNotice = noticeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        
        existingNotice.setTitle(notice.getTitle());
        existingNotice.setContent(notice.getContent());
        existingNotice.setPriority(notice.getPriority());
        existingNotice.setPinned(notice.isPinned());
        
        noticeService.updateNotice(existingNotice);
        
        return "redirect:/notices/" + existingNotice.getId();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes attributes) {
        noticeService.deleteNotice(id);
        attributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        
        return "redirect:/notices";
    }
} 