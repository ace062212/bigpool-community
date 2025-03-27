package com.community.site.controller;

import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.service.PostService;
import com.community.site.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class UserController {

    private final UserService userService;
    private final PostService postService;
    
    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute @Valid User user,
                         @RequestParam String passwordConfirm,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        try {
            if (!user.getPassword().equals(passwordConfirm)) {
                model.addAttribute("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                return "user/register";
            }

            if (userService.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "이미 사용 중인 아이디입니다.");
                return "user/register";
            }

            if (userService.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "이미 사용 중인 이메일입니다.");
                return "user/register";
            }

            userService.registerUser(user);
            attributes.addFlashAttribute("message", "회원가입에 성공했습니다! 로그인해주세요.");
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String loginForm(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        }
        return "user/login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }

            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Page<Post> posts = postService.findByAuthor(
                    user,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            );

            model.addAttribute("user", user);
            model.addAttribute("posts", posts);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", posts.getTotalPages());

            return "user/profile";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "프로필 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error/error";
        }
    }
} 