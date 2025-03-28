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
    
    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        model.addAttribute("user", user);
        return "user/edit-profile";
    }
    
    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam("nickname") String nickname,
                             Principal principal,
                             RedirectAttributes attributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 닉네임 업데이트
            user.setNickname(nickname);
            userService.updateUser(user);
            
            attributes.addFlashAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
            return "redirect:/profile";
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "프로필 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
    
    @GetMapping("/profile/change-password")
    public String changePasswordForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        model.addAttribute("user", user);
        return "user/change-password";
    }
    
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Principal principal,
                                RedirectAttributes attributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
            if (!newPassword.equals(confirmPassword)) {
                attributes.addFlashAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return "redirect:/profile/change-password";
            }
            
            // 비밀번호 길이 검증
            if (newPassword.length() < 8) {
                attributes.addFlashAttribute("error", "비밀번호는 최소 8자 이상이어야 합니다.");
                return "redirect:/profile/change-password";
            }
            
            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 비밀번호 변경 시도
            boolean changed = userService.changePassword(user, currentPassword, newPassword);
            
            if (changed) {
                attributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
                return "redirect:/profile";
            } else {
                attributes.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "redirect:/profile/change-password";
            }
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }
    
    // 관리자 전용 비밀번호 변경 페이지 (본인 인증 없이 변경 가능)
    @GetMapping("/admin/reset-password")
    public String adminResetPasswordForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 관리자 권한 체크
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        
        return "user/admin-reset-password";
    }
    
    @PostMapping("/admin/reset-password")
    public String adminResetPassword(@RequestParam("username") String targetUsername,
                                    @RequestParam("newPassword") String newPassword,
                                    @RequestParam("confirmPassword") String confirmPassword,
                                    Principal principal,
                                    RedirectAttributes attributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        try {
            // 관리자 권한 확인
            String adminUsername = principal.getName();
            User admin = userService.findByUsername(adminUsername)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            if (!"ROLE_ADMIN".equals(admin.getRole())) {
                attributes.addFlashAttribute("error", "관리자 권한이 필요합니다.");
                return "redirect:/";
            }
            
            // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
            if (!newPassword.equals(confirmPassword)) {
                attributes.addFlashAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return "redirect:/admin/reset-password";
            }
            
            // 비밀번호 길이 검증
            if (newPassword.length() < 8) {
                attributes.addFlashAttribute("error", "비밀번호는 최소 8자 이상이어야 합니다.");
                return "redirect:/admin/reset-password";
            }
            
            // 대상 사용자 찾기
            User targetUser = userService.findByUsername(targetUsername)
                    .orElse(null);
            
            if (targetUser == null) {
                attributes.addFlashAttribute("error", "해당 사용자를 찾을 수 없습니다.");
                return "redirect:/admin/reset-password";
            }
            
            // 비밀번호 직접 설정 (현재 비밀번호 확인 없이)
            targetUser.setPassword(newPassword);
            userService.registerUser(targetUser); // 내부적으로 비밀번호 암호화 처리
            
            attributes.addFlashAttribute("message", targetUsername + " 사용자의 비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/admin/reset-password";
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/reset-password";
        }
    }
} 