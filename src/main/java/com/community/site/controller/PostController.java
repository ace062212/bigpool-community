package com.community.site.controller;

import com.community.site.model.Comment;
import com.community.site.model.Post;
import com.community.site.model.User;
import com.community.site.service.CommentService;
import com.community.site.service.FileService;
import com.community.site.service.FirebaseStorageService;
import com.community.site.service.PostService;
import com.community.site.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/posts")
public class PostController {

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final FileService fileService;
    private final FirebaseStorageService firebaseStorageService;
    
    @Autowired
    public PostController(PostService postService, UserService userService, 
                          CommentService commentService, FileService fileService,
                          @Qualifier("firebaseStorageService") FirebaseStorageService firebaseStorageService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.fileService = fileService;
        this.firebaseStorageService = firebaseStorageService;
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
        logger.debug("게시글 조회 시작: id={}", id);
        
        Post post = postService.findById(id)
                .orElse(null);
        if (post == null) {
            return "error/404";
        }
        
        // 조회수 증가
        post = postService.incrementViewCount(id);
        
        // 이미지 파일 목록 로그
        if (post.getImageFiles() != null && !post.getImageFiles().isEmpty()) {
            logger.debug("게시글 이미지 파일: {}", post.getImageFiles());
            
            // 이미지 파일 목록을 확인하고 모델에 추가
            List<String> imageFiles = post.getImageFilesList();
            if (!imageFiles.isEmpty()) {
                logger.debug("이미지 파일 수: {}", imageFiles.size());
                
                // 이미지 URL 목록 생성
                List<String> imageUrls = new ArrayList<>();
                for (String imageFile : imageFiles) {
                    logger.debug("이미지 파일: {}", imageFile);
                    String imageUrl = firebaseStorageService.getImageUrl(imageFile);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                        logger.debug("이미지 URL 생성: {}", imageUrl);
                    }
                }
                
                // 이미지 URL 목록을 모델에 추가
                model.addAttribute("imageUrls", imageUrls);
                logger.debug("이미지 URL 리스트 모델에 추가: {}", imageUrls);
            }
        }
        
        // 모델에 업로드 경로 추가
        model.addAttribute("uploadPath", "/uploads/");
        
        List<Comment> comments = commentService.findByPost(post);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        
        // 현재 접근자가 작성자인지 어드민인지 확인
        boolean isAuthorOrAdmin = false;
        boolean isAuthor = false;
        boolean isAdmin = false;
        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName())
                    .orElse(null);
            if (currentUser != null) {
                isAuthor = currentUser.getId().equals(post.getAuthor().getId());
                isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
                isAuthorOrAdmin = isAuthor || isAdmin;
            }
        }
        model.addAttribute("isAuthorOrAdmin", isAuthorOrAdmin);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("isAdmin", isAdmin);
        
        logger.debug("게시글 조회 완료: id={}, 댓글 수={}, 작성자={}", 
                  id, comments.size(), principal != null ? principal.getName() : "anonymous");
        
        return "post/view";
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String newPostForm(Model model, HttpSession session) {
        model.addAttribute("post", new Post());
        
        // 폼 제출 토큰 생성 및 세션에 저장
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("postFormToken", formToken);
        model.addAttribute("formToken", formToken);
        
        return "post/form";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createPost(@Valid @ModelAttribute("post") Post post,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                            @RequestParam(value = "formToken", required = false) String formToken,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) throws IOException {
        
        System.out.println("===== createPost 메서드 시작 =====");
        
        // 폼 토큰 검증 - 중복 제출 방지
        String sessionToken = (String) session.getAttribute("postFormToken");
        if (sessionToken == null || !sessionToken.equals(formToken)) {
            System.out.println("폼 토큰 불일치 - 중복 제출 또는 세션 만료");
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었거나 이미 처리된 요청입니다. 다시 시도해주세요.");
            return "redirect:/posts";
        }
        
        // 토큰 사용 후 제거 (재사용 방지)
        session.removeAttribute("postFormToken");
        
        if (result.hasErrors()) {
            System.out.println("폼 검증 오류 발생: " + result.getAllErrors());
            model.addAttribute("formToken", formToken); // 폼 재제출시 토큰 유지
            return "post/form";
        }
        
        try {
            System.out.println("사용자 검색 중: " + principal.getName());
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            
            post.setAuthor(user);
            System.out.println("사용자 설정 완료: " + user.getUsername());
            
            // 이미지 파일 처리
            if (uploadFiles != null && uploadFiles.length > 0) {
                System.out.println("이미지 파일 처리 시작");
                
                // 실제 비어있지 않은 파일만 필터링
                List<MultipartFile> nonEmptyFiles = new ArrayList<>();
                for (MultipartFile file : uploadFiles) {
                    if (file != null && !file.isEmpty() && file.getSize() > 0) {
                        System.out.println("유효한 파일 발견: " + file.getOriginalFilename() + ", 크기: " + file.getSize());
                        nonEmptyFiles.add(file);
                    } else if (file != null) {
                        System.out.println("빈 파일 무시: " + file.getOriginalFilename());
                    }
                }
                
                if (!nonEmptyFiles.isEmpty()) {
                    System.out.println("유효한 파일 개수: " + nonEmptyFiles.size());
                    try {
                        // Firebase Storage에 이미지 저장
                        List<String> savedFilePaths = firebaseStorageService.saveImages(nonEmptyFiles);
                        
                        if (savedFilePaths != null && !savedFilePaths.isEmpty()) {
                            // 쉼표로 구분된 파일 경로를 저장
                            String imagePaths = String.join(",", savedFilePaths);
                            post.setImageFiles(imagePaths);
                            System.out.println("Firebase에 이미지 업로드 완료: " + imagePaths);
                        } else {
                            System.out.println("저장된 이미지 경로가 없음");
                        }
                    } catch (Exception e) {
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
            
            // 토큰 재생성하여 모델에 추가
            String newFormToken = UUID.randomUUID().toString();
            session.setAttribute("postFormToken", newFormToken);
            model.addAttribute("formToken", newFormToken);
            
            return "post/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editPostForm(@PathVariable Long id, Model model, Principal principal, HttpSession session) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 작성자 또는 관리자만 수정할 수 있음
        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
        if (!isAdmin && !post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("게시글을 수정할 권한이 없습니다.");
        }
        
        // 폼 제출 토큰 생성 및 세션에 저장
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("editPostFormToken", formToken);
        model.addAttribute("formToken", formToken);
        
        model.addAttribute("post", post);
        return "post/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updatePost(@PathVariable Long id,
                            @Valid @ModelAttribute Post post,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                            @RequestParam(value = "formToken", required = false) String formToken,
                            Principal principal,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) throws IOException {
        
        // 폼 토큰 검증 - 중복 제출 방지
        String sessionToken = (String) session.getAttribute("editPostFormToken");
        if (sessionToken == null || !sessionToken.equals(formToken)) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었거나 이미 처리된 요청입니다. 다시 시도해주세요.");
            return "redirect:/posts/" + id;
        }
        
        // 토큰 사용 후 제거 (재사용 방지)
        session.removeAttribute("editPostFormToken");
                            
        if (result.hasErrors()) {
            model.addAttribute("formToken", formToken); // 폼 재제출시 토큰 유지
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
                        // Firebase Storage에 이미지 저장
                        List<String> newFilePaths = firebaseStorageService.saveImages(nonEmptyFiles);
                        
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