package com.community.site.controller;

import com.community.site.model.Comment;
import com.community.site.model.BigPicture;
import com.community.site.model.User;
import com.community.site.service.CommentService;
import com.community.site.service.FileService;
import com.community.site.service.FirebaseStorageService;
import com.community.site.service.BigPictureService;
import com.community.site.service.UserService;
import com.community.site.service.VoteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.community.site.repository.VoteRepository;
import com.community.site.entity.Vote;
import com.community.site.entity.BigPictureVote;
import com.community.site.service.BigPictureVoteService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/bigpictures")
public class BigPictureController {

    private static final Logger logger = LoggerFactory.getLogger(BigPictureController.class);

    private final BigPictureService bigPictureService;
    private final UserService userService;
    private final CommentService commentService;
    private final FileService fileService;
    private final FirebaseStorageService firebaseStorageService;
    private final BigPictureVoteService bigPictureVoteService;
    
    @Autowired
    public BigPictureController(BigPictureService bigPictureService, UserService userService, 
                          CommentService commentService, FileService fileService,
                          @Qualifier("firebaseStorageService") FirebaseStorageService firebaseStorageService,
                          BigPictureVoteService bigPictureVoteService) {
        this.bigPictureService = bigPictureService;
        this.userService = userService;
        this.commentService = commentService;
        this.fileService = fileService;
        this.firebaseStorageService = firebaseStorageService;
        this.bigPictureVoteService = bigPictureVoteService;
    }

    @GetMapping
    public String listBigPictures(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          Model model) {
        logger.debug("빅픽처 목록 조회 요청: page={}, size={}, keyword={}", page, size, keyword);
        
        // 정렬: 최신순 (ID 내림차순)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        
        Page<BigPicture> bigPicturePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 기능
            bigPicturePage = bigPictureService.searchBigPictures(keyword, pageRequest);
            model.addAttribute("keyword", keyword);
        } else {
            // 전체 목록
            bigPicturePage = bigPictureService.findAllBigPictures(pageRequest);
        }
        
        model.addAttribute("bigPictures", bigPicturePage.getContent());
        model.addAttribute("currentPage", bigPicturePage.getNumber());
        model.addAttribute("totalPages", bigPicturePage.getTotalPages());
        model.addAttribute("totalItems", bigPicturePage.getTotalElements());
        
        return "bigpicture/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public String viewBigPicture(@PathVariable Long id, Model model, Principal principal) {
        logger.debug("빅픽처 조회 시작: id={}", id);
        
        try {
            BigPicture bigPicture = bigPictureService.findById(id)
                    .orElse(null);
            if (bigPicture == null) {
                logger.warn("빅픽처를 찾을 수 없음: id={}", id);
                return "error/404";
            }
            
            // 조회수 증가
            bigPicture = bigPictureService.incrementViewCount(id);
            
            // 이미지 파일 목록 로그
            if (bigPicture.getImageFiles() != null && !bigPicture.getImageFiles().isEmpty()) {
                logger.debug("빅픽처 이미지 파일: {}", bigPicture.getImageFiles());
                
                // 이미지 파일 목록을 확인하고 모델에 추가
                List<String> imageFiles = bigPicture.getImageFilesList();
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
            
            List<Comment> comments = commentService.findByBigPicture(bigPicture);
            model.addAttribute("bigPicture", bigPicture);
            model.addAttribute("comments", comments);
            model.addAttribute("newComment", new Comment());
            
            // 현재 접근자가 작성자인지 어드민인지 확인
            boolean isAuthor = false;
            boolean isAdmin = false;
            
            try {
                if (principal != null) {
                    User currentUser = userService.findByUsername(principal.getName())
                            .orElse(null);
                    
                    if (currentUser != null) {
                        // 현재 사용자가 작성자인지 확인
                        isAuthor = bigPicture.getAuthor().getId().equals(currentUser.getId());
                        
                        // 어드민 권한 확인
                        isAdmin = currentUser.getRole().equals("ROLE_ADMIN");
                        
                        model.addAttribute("currentUser", currentUser);

                        // 사용자의 투표 상태 확인
                        Optional<BigPictureVote> userVote = bigPictureVoteService.findByBigPictureAndUser(bigPicture, currentUser);
                        if (userVote.isPresent()) {
                            model.addAttribute("userVoted", true);
                            model.addAttribute("userVoteType", userVote.get().isUpvote() ? "up" : "down");
                        } else {
                            model.addAttribute("userVoted", false);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("사용자 정보 확인 중 오류: {}", e.getMessage());
            }
            
            // 투표 수 추가
            long upvotes = bigPictureVoteService.getUpvoteCount(bigPicture);
            long downvotes = bigPictureVoteService.getDownvoteCount(bigPicture);
            model.addAttribute("upvotes", upvotes);
            model.addAttribute("downvotes", downvotes);
            
            model.addAttribute("isAuthor", isAuthor);
            model.addAttribute("isAdmin", isAdmin);
            
            return "bigpicture/view";
        } catch (Exception e) {
            logger.error("빅픽처 조회 중 오류: {}", e.getMessage(), e);
            return "error/500";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String newBigPictureForm(Model model, HttpSession session) {
        model.addAttribute("bigPicture", new BigPicture());
        
        // 폼 제출 토큰 생성 및 세션에 저장
        String formToken = UUID.randomUUID().toString();
        session.setAttribute("bigPictureFormToken", formToken);
        model.addAttribute("formToken", formToken);
        
        return "bigpicture/form";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createBigPicture(@Valid @ModelAttribute("bigPicture") BigPicture bigPicture,
                            BindingResult result,
                            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                            @RequestParam(value = "formToken", required = false) String formToken,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) throws IOException {
        logger.debug("빅픽처 생성 요청: 제목={}", bigPicture.getTitle());
        
        // 중복 제출 확인
        String sessionToken = (String) session.getAttribute("bigPictureFormToken");
        if (sessionToken == null || !sessionToken.equals(formToken)) {
            logger.warn("폼 토큰 불일치: 세션={}, 요청={}", sessionToken, formToken);
            redirectAttributes.addFlashAttribute("message", "이미 처리된 요청이거나 유효하지 않은 요청입니다.");
            return "redirect:/bigpictures";
        }
        
        // 폼 유효성 검증 실패 시 폼 다시 표시
        if (result.hasErrors()) {
            logger.debug("폼 유효성 검증 실패: {}", result.getAllErrors());
            return "bigpicture/form";
        }
                
        try {
            // 작성자 설정
            User author = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다."));
            bigPicture.setAuthor(author);
            
            // 이미지 업로드 처리
            if (uploadFiles != null && uploadFiles.length > 0) {
                for (MultipartFile file : uploadFiles) {
                    if (!file.isEmpty()) {
                        try {
                            // 파이어베이스에 이미지 업로드
                            String savedFileName = firebaseStorageService.saveImage(file);
                            
                            // 빅픽처 엔티티에 이미지 파일명 추가
                            if (savedFileName != null) {
                                bigPicture.addImageFile(savedFileName);
                                logger.debug("이미지 파일이 저장되었습니다: {}", savedFileName);
                            }
                        } catch (Exception e) {
                            logger.error("이미지 업로드 중 오류: {}", e.getMessage(), e);
                        }
                    }
                }
            }
            
            // 빅픽처 저장
            BigPicture savedBigPicture = bigPictureService.createBigPicture(bigPicture);
            
            // 세션에서 폼 토큰 삭제
            session.removeAttribute("bigPictureFormToken");
            
            redirectAttributes.addFlashAttribute("message", "빅픽처가 성공적으로 등록되었습니다.");
            return "redirect:/bigpictures/" + savedBigPicture.getId();
            
        } catch (Exception e) {
            logger.error("빅픽처 생성 중 오류: {}", e.getMessage(), e);
            model.addAttribute("error", "빅픽처를 저장하는 중 오류가 발생했습니다.");
            return "bigpicture/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editBigPictureForm(@PathVariable Long id, Model model, Principal principal) {
        logger.debug("빅픽처 수정 폼 요청: id={}", id);
        
        BigPicture bigPicture = bigPictureService.findById(id)
                .orElse(null);
        
        if (bigPicture == null) {
            return "error/404";
        }
        
        // 작성자 또는 관리자만 수정 가능
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null || (!bigPicture.getAuthor().getId().equals(user.getId()) 
                && !user.getRole().equals("ROLE_ADMIN"))) {
            return "error/403";
        }
        
        model.addAttribute("bigPicture", bigPicture);
        
        // 기존 이미지 목록 설정
        if (bigPicture.getImageFiles() != null && !bigPicture.getImageFiles().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (String imageFile : bigPicture.getImageFilesList()) {
                String imageUrl = firebaseStorageService.getImageUrl(imageFile);
                if (imageUrl != null) {
                    imageUrls.add(imageUrl);
                }
            }
            model.addAttribute("imageUrls", imageUrls);
        }
        
        return "bigpicture/edit";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String updateBigPicture(@PathVariable Long id,
                           @Valid @ModelAttribute("bigPicture") BigPicture bigPictureForm,
                           BindingResult result,
                           @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) throws IOException {
        logger.debug("빅픽처 수정 요청: id={}", id);
        
        // 폼 유효성 검증 실패 시 폼 다시 표시
        if (result.hasErrors()) {
            return "bigpicture/edit";
        }
        
        // 기존 빅픽처 조회
        BigPicture existingBigPicture = bigPictureService.findById(id)
                .orElse(null);
        
        if (existingBigPicture == null) {
            return "error/404";
        }
        
        // 작성자 또는 관리자만 수정 가능
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null || (!existingBigPicture.getAuthor().getId().equals(user.getId()) 
                && !user.getRole().equals("ROLE_ADMIN"))) {
            return "error/403";
        }
        
        // 기존 빅픽처 데이터 업데이트
        existingBigPicture.setTitle(bigPictureForm.getTitle());
        existingBigPicture.setContent(bigPictureForm.getContent());
        
        // 새 이미지 업로드 처리
        if (uploadFiles != null && uploadFiles.length > 0) {
            for (MultipartFile file : uploadFiles) {
                if (!file.isEmpty()) {
                    try {
                        // 파이어베이스에 이미지 업로드
                        String savedFileName = firebaseStorageService.saveImage(file);
                        
                        // 빅픽처 엔티티에 이미지 파일명 추가
                        if (savedFileName != null) {
                            existingBigPicture.addImageFile(savedFileName);
                            logger.debug("이미지 파일이 저장되었습니다: {}", savedFileName);
                        }
                    } catch (Exception e) {
                        logger.error("이미지 업로드 중 오류: {}", e.getMessage(), e);
                    }
                }
            }
        }
        
        // 빅픽처 업데이트
        BigPicture updatedBigPicture = bigPictureService.updateBigPicture(existingBigPicture);
        
        redirectAttributes.addFlashAttribute("message", "빅픽처가 성공적으로 수정되었습니다.");
        return "redirect:/bigpictures/" + updatedBigPicture.getId();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteBigPicture(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        logger.debug("빅픽처 삭제 요청: id={}", id);
        
        BigPicture bigPicture = bigPictureService.findById(id)
                .orElse(null);
        
        if (bigPicture == null) {
            return "error/404";
        }
        
        // 작성자 또는 관리자만 삭제 가능
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null || (!bigPicture.getAuthor().getId().equals(user.getId()) 
                && !user.getRole().equals("ROLE_ADMIN"))) {
            return "error/403";
        }
        
        // 빅픽처 삭제
        bigPictureService.deleteBigPicture(id);
        
        redirectAttributes.addFlashAttribute("message", "빅픽처가 성공적으로 삭제되었습니다.");
        return "redirect:/bigpictures";
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public String addComment(@PathVariable Long id,
                      @Valid @ModelAttribute("newComment") Comment comment,
                      BindingResult result,
                      Principal principal,
                      RedirectAttributes redirectAttributes) {
        logger.debug("빅픽처 댓글 추가 요청: bigPictureId={}", id);
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentError", "댓글 내용을 입력해주세요.");
            return "redirect:/bigpictures/" + id;
        }
        
        BigPicture bigPicture = bigPictureService.findById(id)
                .orElse(null);
        
        if (bigPicture == null) {
            return "error/404";
        }
        
        // 작성자 설정
        User author = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인된 사용자를 찾을 수 없습니다."));
        
        comment.setAuthor(author);
        comment.setBigPicture(bigPicture);
        
        // 댓글 저장
        commentService.createComment(comment);
        
        redirectAttributes.addFlashAttribute("message", "댓글이 성공적으로 등록되었습니다.");
        return "redirect:/bigpictures/" + id;
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> vote(@PathVariable Long id,
                           @RequestParam boolean upvote,
                           Principal principal) {
        logger.debug("빅픽처 투표 요청: id={}, upvote={}, 사용자={}", id, upvote, principal.getName());
        
        try {
            // 사용자 조회
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
            
            // 투표 처리
            bigPictureVoteService.vote(id, user, upvote);
            
            // 투표 수 조회
            BigPicture bigPicture = bigPictureService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("빅픽처를 찾을 수 없습니다: " + id));
            
            long upvotes = bigPictureVoteService.getUpvoteCount(bigPicture);
            long downvotes = bigPictureVoteService.getDownvoteCount(bigPicture);
            
            // 응답 데이터 구성
            var response = new HashMap<String, Object>();
            response.put("upvotes", upvotes);
            response.put("downvotes", downvotes);
            
            // 사용자의 현재 투표 상태
            Optional<BigPictureVote> userVote = bigPictureVoteService.findByBigPictureAndUser(bigPicture, user);
            if (userVote.isPresent()) {
                response.put("userVoted", true);
                response.put("userVoteType", userVote.get().isUpvote() ? "up" : "down");
            } else {
                response.put("userVoted", false);
            }
            
            logger.debug("투표 처리 완료: id={}, 추천={}, 비추천={}", id, upvotes, downvotes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("투표 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}