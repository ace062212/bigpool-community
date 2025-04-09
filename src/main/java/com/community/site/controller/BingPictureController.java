package com.community.site.controller;

import com.community.site.model.BingPicture;
import com.community.site.model.BingPictureComment;
import com.community.site.model.User;
import com.community.site.service.BingPictureService;
import com.community.site.service.BingPictureCommentService;
import com.community.site.service.UserService;
import com.community.site.service.FileService;
import com.community.site.service.FirebaseStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/bingpictures")
public class BingPictureController {

    private static final Logger logger = LoggerFactory.getLogger(BingPictureController.class);

    private final BingPictureService bingPictureService;
    private final UserService userService;
    private final BingPictureCommentService commentService;
    private final FileService fileService;
    private final FirebaseStorageService firebaseStorageService;
    
    @Autowired
    public BingPictureController(BingPictureService bingPictureService, 
                                UserService userService,
                                BingPictureCommentService commentService, 
                                FileService fileService,
                                @Qualifier("firebaseStorageService") FirebaseStorageService firebaseStorageService) {
        this.bingPictureService = bingPictureService;
        this.userService = userService;
        this.commentService = commentService;
        this.fileService = fileService;
        this.firebaseStorageService = firebaseStorageService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String listBingPictures(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false, defaultValue = "latest") String sort) {
        
        try {
            // 페이지 크기 고정 (10개로 설정)
            size = 10;
            
            Sort.Direction direction = Sort.Direction.DESC;
            String sortField = "createdAt"; // 기본 정렬 필드
            
            // 정렬 옵션에 따라 필드 및 방향 설정
            switch (sort) {
                case "oldest":
                    direction = Sort.Direction.ASC;
                    break;
                case "popular":
                    sortField = "viewCount";
                    break;
                case "comments":
                    // 댓글 수로 정렬하는 로직은 복잡할 수 있으므로 일단 기본값 사용
                    sortField = "createdAt";
                    break;
                default: // latest
                    break;
            }
            
            Page<BingPicture> bingPictures;
            if (keyword != null && !keyword.isEmpty()) {
                bingPictures = bingPictureService.searchBingPictures(keyword, 
                        PageRequest.of(page, size, Sort.by(direction, sortField)));
                model.addAttribute("keyword", keyword);
            } else {
                bingPictures = bingPictureService.findAllBingPictures(
                        PageRequest.of(page, size, Sort.by(direction, sortField)));
            }
            
            // 각 BingPicture에 대해 thumbnailUrl과 commentCount 설정
            for (BingPicture picture : bingPictures.getContent()) {
                // 첫 번째 이미지를 썸네일로 사용
                List<String> imageFiles = picture.getImageFilesList();
                if (imageFiles != null && !imageFiles.isEmpty()) {
                    model.addAttribute("thumbnailUrl_" + picture.getId(), 
                            firebaseStorageService.getImageUrl(imageFiles.get(0)));
                }
                
                // 댓글 수 계산
                long commentCount = commentService.countCommentsByBingPicture(picture.getId());
                model.addAttribute("commentCount_" + picture.getId(), commentCount);
            }
            
            // 페이지네이션을 위한 변수 설정
            int currentPage = page + 1; // 0-based -> 1-based
            int totalPages = bingPictures.getTotalPages();
            if (totalPages == 0) totalPages = 1; // 결과가 없는 경우 최소 1페이지
            
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, currentPage + 2);
            
            // 최소 5개 페이지 표시를 위한 조정
            if (endPage - startPage < 4 && totalPages > 4) {
                if (startPage == 1) {
                    endPage = Math.min(5, totalPages);
                } else if (endPage == totalPages) {
                    startPage = Math.max(1, totalPages - 4);
                }
            }
            
            model.addAttribute("bingPictures", bingPictures);
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("sort", sort);
            
            logger.debug("Bing Picture 목록 로드: page={}, size={}, sort={}, 결과 수={}", 
                     currentPage, size, sort, bingPictures.getTotalElements());
            
            return "bingpicture/list";
        } catch (Exception e) {
            logger.error("Bing Picture 목록 로딩 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Bing Picture 목록을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String viewBingPicture(@PathVariable Long id, Model model, Principal principal) {
        logger.debug("Bing Picture 조회 시작: id={}", id);
        
        try {
            BingPicture bingPicture = bingPictureService.findById(id)
                    .orElse(null);
            if (bingPicture == null) {
                logger.warn("Bing Picture를 찾을 수 없음: id={}", id);
                return "error/404";
            }
            
            // 조회수 증가
            bingPicture = bingPictureService.incrementViewCount(id);
            
            // 이미지 파일 목록 로그
            if (bingPicture.getImageFiles() != null && !bingPicture.getImageFiles().isEmpty()) {
                logger.debug("Bing Picture 이미지 파일: {}", bingPicture.getImageFiles());
                
                // 이미지 파일 목록을 확인하고 모델에 추가
                List<String> imageFiles = bingPicture.getImageFilesList();
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
            
            List<BingPictureComment> comments = commentService.getRootCommentsByBingPicture(id);
            model.addAttribute("bingPicture", bingPicture);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new BingPictureComment());
            
            // 현재 접근자가 작성자인지 어드민인지 확인
            boolean isAuthorOrAdmin = false;
            boolean isAuthor = false;
            boolean isAdmin = false;
            
            try {
                if (principal != null) {
                    User currentUser = userService.findByUsername(principal.getName())
                            .orElse(null);
                    if (currentUser != null) {
                        isAuthor = currentUser.getId().equals(bingPicture.getAuthor().getId());
                        isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
                        isAuthorOrAdmin = isAuthor || isAdmin;
                    }
                }
            } catch (Exception e) {
                logger.error("사용자 권한 확인 중 오류: bingPictureId={}, principal={}, 오류={}", 
                          id, principal != null ? principal.getName() : "null", e.getMessage(), e);
                // 오류가 발생해도 페이지 로딩은 계속 진행
            }
            
            model.addAttribute("isAuthorOrAdmin", isAuthorOrAdmin);
            model.addAttribute("isAuthor", isAuthor);
            model.addAttribute("isAdmin", isAdmin);
            
            logger.debug("Bing Picture 조회 완료: id={}, 댓글 수={}, 작성자={}", 
                      id, comments.size(), principal != null ? principal.getName() : "anonymous");
            
            return "bingpicture/view";
        } catch (Exception e) {
            logger.error("Bing Picture 조회 중 오류 발생: id={}, 오류={}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Bing Picture를 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String newBingPictureForm(Model model, HttpSession session) {
        model.addAttribute("bingPicture", new BingPicture());
        
        // 폼 제출 토큰 생성 및 세션에 저장
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("bingPictureFormToken", formToken);
        model.addAttribute("formToken", formToken);
        
        return "bingpicture/new";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createBingPicture(@Valid @ModelAttribute("bingPicture") BingPicture bingPicture,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                            @RequestParam(value = "formToken", required = false) String formToken,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) throws IOException {
        
        logger.debug("Bing Picture 생성 시작: 제목={}, 작성자={}", bingPicture.getTitle(), principal.getName());
        
        // 폼 중복 제출 검사
        String sessionToken = (String) session.getAttribute("bingPictureFormToken");
        if (sessionToken == null || !sessionToken.equals(formToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "페이지가 만료되었거나 중복 제출되었습니다.");
            return "redirect:/bingpictures";
        }
        
        // 중복 제출 방지를 위한 토큰 제거
        session.removeAttribute("bingPictureFormToken");
        
        if (result.hasErrors()) {
            logger.warn("Bing Picture 폼 검증 실패: {}", result.getAllErrors());
            model.addAttribute("formToken", UUID.randomUUID().toString());
            return "bingpicture/new";
        }
        
        try {
            // 현재 사용자 설정
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            bingPicture.setAuthor(user);
            
            // Bing Picture 저장 및 이미지 업로드
            BingPicture savedBingPicture = bingPictureService.createBingPicture(bingPicture, uploadFiles);
            logger.debug("Bing Picture 생성 완료: id={}", savedBingPicture.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Bing Picture가 성공적으로 작성되었습니다.");
            return "redirect:/bingpictures/" + savedBingPicture.getId();
        } catch (Exception e) {
            logger.error("Bing Picture 생성 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Bing Picture 작성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("formToken", UUID.randomUUID().toString());
            return "bingpicture/new";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editBingPictureForm(@PathVariable Long id, Model model, Principal principal, HttpSession session) {
        try {
            BingPicture bingPicture = bingPictureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bing Picture를 찾을 수 없습니다."));
            
            // 작성자 또는 관리자만 편집 가능
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            
            if (!isAdmin && !bingPicture.getAuthor().getId().equals(user.getId())) {
                return "error/403";
            }
            
            model.addAttribute("bingPicture", bingPicture);
            
            // 폼 제출 토큰 생성 및 세션에 저장
            String formToken = UUID.randomUUID().toString();
            session.setAttribute("bingPictureFormToken", formToken);
            model.addAttribute("formToken", formToken);
            
            return "bingpicture/edit";
        } catch (Exception e) {
            logger.error("Bing Picture 편집 폼 로딩 중 오류 발생: id={}, 오류={}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Bing Picture 편집 폼을 불러오는 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateBingPicture(@PathVariable Long id,
                            @Valid @ModelAttribute BingPicture bingPicture,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
                            @RequestParam(value = "formToken", required = false) String formToken,
                            Principal principal,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) throws IOException {
        
        logger.debug("Bing Picture 수정 시작: id={}, 제목={}", id, bingPicture.getTitle());
        
        // 폼 중복 제출 검사
        String sessionToken = (String) session.getAttribute("bingPictureFormToken");
        if (sessionToken == null || !sessionToken.equals(formToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "페이지가 만료되었거나 중복 제출되었습니다.");
            return "redirect:/bingpictures/" + id;
        }
        
        // 중복 제출 방지를 위한 토큰 제거
        session.removeAttribute("bingPictureFormToken");
        
        if (result.hasErrors()) {
            logger.warn("Bing Picture 수정 폼 검증 실패: {}", result.getAllErrors());
            model.addAttribute("formToken", UUID.randomUUID().toString());
            return "bingpicture/edit";
        }
        
        try {
            // 현재 Bing Picture 정보를 가져옴
            BingPicture currentBingPicture = bingPictureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bing Picture를 찾을 수 없습니다."));
            
            // 작성자 또는 관리자만 수정 가능
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            
            if (!isAdmin && !currentBingPicture.getAuthor().getId().equals(user.getId())) {
                return "error/403";
            }
            
            // ID 설정 (경로 변수에서 가져온 ID로 설정)
            bingPicture.setId(id);
            // 기존 작성자 정보 유지
            bingPicture.setAuthor(currentBingPicture.getAuthor());
            
            // 기존 조회수 유지
            bingPicture.setViewCount(currentBingPicture.getViewCount());
            
            // Bing Picture 업데이트 및 이미지 업로드
            BingPicture updatedBingPicture = bingPictureService.updateBingPicture(bingPicture, uploadFiles);
            logger.debug("Bing Picture 수정 완료: id={}", updatedBingPicture.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Bing Picture가 성공적으로 수정되었습니다.");
            return "redirect:/bingpictures/" + updatedBingPicture.getId();
        } catch (Exception e) {
            logger.error("Bing Picture 수정 중 오류 발생: id={}, 오류={}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Bing Picture 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("formToken", UUID.randomUUID().toString());
            return "bingpicture/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteBingPicture(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        logger.debug("Bing Picture 삭제 시작: id={}, 사용자={}", id, principal.getName());
        
        try {
            // 현재 Bing Picture 정보를 가져옴
            BingPicture bingPicture = bingPictureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bing Picture를 찾을 수 없습니다."));
            
            // 작성자 또는 관리자만 삭제 가능
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            
            if (!isAdmin && !bingPicture.getAuthor().getId().equals(user.getId())) {
                attributes.addFlashAttribute("errorMessage", "삭제 권한이 없습니다.");
                return "redirect:/bingpictures/" + id;
            }
            
            // Bing Picture 삭제
            bingPictureService.deleteBingPicture(id);
            logger.debug("Bing Picture 삭제 완료: id={}", id);
            
            attributes.addFlashAttribute("successMessage", "Bing Picture가 성공적으로 삭제되었습니다.");
            return "redirect:/bingpictures";
        } catch (Exception e) {
            logger.error("Bing Picture 삭제 중 오류 발생: id={}, 오류={}", id, e.getMessage(), e);
            attributes.addFlashAttribute("errorMessage", "Bing Picture 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/bingpictures";
        }
    }
} 