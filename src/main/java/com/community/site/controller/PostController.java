package com.community.site.controller;

import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.service.CommentService;
import com.community.site.service.FileService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final FileService fileService;
    
    public PostController(PostService postService, UserService userService, CommentService commentService, FileService fileService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.fileService = fileService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listPosts(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String search) {
        
        try {
            // 페이지 크기 고정 (10개로 설정)
            size = 10;
            
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
            System.out.println("===== viewPost 메서드 시작 (ID: " + id + ") =====");
            
            Post post = postService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: ID = " + id));
            
            // 조회수 증가
            post = postService.incrementViewCount(id);
            
            // 이미지 파일 디버깅
            if (post.getImageFiles() != null && !post.getImageFiles().isEmpty()) {
                System.out.println("이미지 파일 정보: " + post.getImageFiles());
                System.out.println("이미지 파일 목록:");
                List<String> imageList = post.getImageFilesList();
                for (int i = 0; i < imageList.size(); i++) {
                    String imagePath = imageList.get(i);
                    System.out.println("  " + (i+1) + ". " + imagePath);
                    // 파일 실제 존재 여부 확인
                    Path fullPath = Paths.get("src/main/resources/static/uploads", imagePath);
                    System.out.println("     절대경로: " + fullPath.toAbsolutePath());
                    System.out.println("     파일존재: " + Files.exists(fullPath));
                }
            } else {
                System.out.println("이미지 파일 없음");
            }
            
            // 이미지 URL을 모델에 직접 추가 (추가 디버깅용)
            if (post.getImageFiles() != null && !post.getImageFiles().isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (String imagePath : post.getImageFilesList()) {
                    imageUrls.add("/uploads/" + imagePath);
                }
                model.addAttribute("imageUrls", imageUrls);
                System.out.println("이미지 URL 목록 모델에 추가: " + imageUrls);
            }
            
            List<Comment> comments = commentService.findByPost(post);
            if (comments == null) {
                comments = new ArrayList<>();
            }
            
            System.out.println("댓글 수: " + comments.size());
            
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new Comment());
            
            // 현재 사용자가 게시글 작성자인지 확인
            boolean isAuthor = false;
            boolean isAdmin = false;
            
            if (principal != null) {
                User user = userService.findByUsername(principal.getName())
                        .orElse(null);
                if (user != null) {
                    // 관리자 권한 확인
                    isAdmin = "ROLE_ADMIN".equals(user.getRole());
                    System.out.println("현재 사용자 권한: " + (isAdmin ? "관리자" : "일반 사용자"));
                    
                    // 작성자 확인
                    if (post.getAuthor() != null && post.getAuthor().getId() != null) {
                        isAuthor = user.getId().equals(post.getAuthor().getId());
                        System.out.println("현재 사용자는 게시글 작성자" + (isAuthor ? "입니다" : "가 아닙니다"));
                    }
                }
            }
            
            model.addAttribute("isAuthor", isAuthor);
            model.addAttribute("isAdmin", isAdmin);
            
            System.out.println("===== viewPost 메서드 종료 =====");
            return "post/view";
        } catch (IllegalArgumentException e) {
            // 게시글 존재하지 않는 경우 404
            System.err.println("404 오류: " + e.getMessage());
            model.addAttribute("message", e.getMessage());
            return "error/404";
        } catch (Exception e) {
            // 기타 예외
            System.err.println("게시글 조회 중 오류 발생: " + e.getMessage());
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
    public String createPost(@Valid @ModelAttribute("post") Post post,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        System.out.println("===== createPost 메서드 시작 =====");
        
        if (result.hasErrors()) {
            System.out.println("폼 검증 오류 발생: " + result.getAllErrors());
            return "post/form";
        }
        
        try {
            System.out.println("사용자 검색 중: " + principal.getName());
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            post.setAuthor(user);
            System.out.println("사용자 설정 완료: " + user.getUsername());
            
            // 이미지 파일 처리 - POST 객체와 분리하여 처리
            if (uploadFiles != null && uploadFiles.length > 0) {
                System.out.println("이미지 파일 개수: " + uploadFiles.length);
                
                // 실제 비어있지 않은 파일만 필터링
                List<MultipartFile> nonEmptyFiles = new ArrayList<>();
                for (MultipartFile file : uploadFiles) {
                    if (file != null && !file.isEmpty()) {
                        System.out.println("유효한 파일 발견: " + file.getOriginalFilename() + ", 크기: " + file.getSize());
                        nonEmptyFiles.add(file);
                    } else if (file != null) {
                        System.out.println("빈 파일 무시: " + file.getOriginalFilename());
                    }
                }
                
                if (!nonEmptyFiles.isEmpty()) {
                    System.out.println("유효한 파일 개수: " + nonEmptyFiles.size());
                    try {
                        List<String> savedFilePaths = fileService.saveImages(nonEmptyFiles);
                        
                        if (savedFilePaths != null && !savedFilePaths.isEmpty()) {
                            // 쉼표로 구분된 파일 경로를 저장
                            String imagePaths = String.join(",", savedFilePaths);
                            post.setImageFiles(imagePaths);
                            System.out.println("이미지 경로 저장 완료: " + imagePaths);
                        } else {
                            System.out.println("저장된 이미지 경로가 없음");
                        }
                    } catch (IOException e) {
                        System.err.println("이미지 업로드 중 오류: " + e.getMessage());
                        e.printStackTrace();
                        model.addAttribute("error", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
                        return "post/form";
                    }
                } else {
                    System.out.println("유효한 파일이 없음");
                }
            } else {
                System.out.println("이미지 파일이 없음");
            }
            
            // 게시글 저장
            System.out.println("게시글 저장 시작: " + post.getTitle());
            Post savedPost = postService.createPost(post);
            System.out.println("게시글 저장 완료. ID: " + savedPost.getId());
            
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 작성되었습니다.");
            System.out.println("===== createPost 메서드 종료 =====");
            return "redirect:/posts/" + savedPost.getId();
        } catch (Exception e) {
            System.err.println("게시글 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "게시글 저장 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("post", post); // 작성 중이던 글 데이터 유지
            return "post/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editPostForm(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자 또는 관리자만 수정할 수 있음
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (!isAdmin && !post.getAuthor().getId().equals(user.getId())) {
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
                            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "post/form";
        }
        
        try {
            Post existingPost = postService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            // 작성자 또는 관리자만 수정할 수 있음
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            if (!isAdmin && !existingPost.getAuthor().getId().equals(user.getId())) {
                throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
            }
            
            existingPost.setTitle(post.getTitle());
            existingPost.setContent(post.getContent());
            
            // 이미지 파일 처리
            if (uploadFiles != null && uploadFiles.length > 0) {
                // 실제 비어있지 않은 파일만 필터링
                List<MultipartFile> nonEmptyFiles = new ArrayList<>();
                for (MultipartFile file : uploadFiles) {
                    if (file != null && !file.isEmpty()) {
                        nonEmptyFiles.add(file);
                    }
                }
                
                if (!nonEmptyFiles.isEmpty()) {
                    try {
                        List<String> newFilePaths = fileService.saveImages(nonEmptyFiles);
                        
                        if (newFilePaths != null && !newFilePaths.isEmpty()) {
                            // 기존 이미지 파일이 있으면 새 파일과 합침
                            if (existingPost.getImageFiles() != null && !existingPost.getImageFiles().isEmpty()) {
                                List<String> existingPaths = existingPost.getImageFilesList();
                                existingPaths.addAll(newFilePaths);
                                existingPost.setImageFiles(String.join(",", existingPaths));
                            } else {
                                // 기존 이미지가 없으면 새 파일만 저장
                                existingPost.setImageFiles(String.join(",", newFilePaths));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("error", "이미지 업로드 중 오류가 발생했습니다.");
                    }
                }
            }
            
            postService.updatePost(existingPost);
            
            return "redirect:/posts/" + existingPost.getId();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/posts/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deletePost(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자 또는 관리자만 삭제할 수 있음
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (!isAdmin && !post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 삭제할 권한이 없습니다.");
        }
        
        postService.deletePost(id);
        attributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        
        return "redirect:/posts";
    }
} 