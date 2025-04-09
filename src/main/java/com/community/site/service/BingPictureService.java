package com.community.site.service;

import com.community.site.model.BingPicture;
import com.community.site.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface BingPictureService {
    BingPicture createBingPicture(BingPicture bingPicture, List<MultipartFile> imageFiles) throws IOException;
    Optional<BingPicture> findById(Long id);
    Page<BingPicture> findAllBingPictures(Pageable pageable);
    Page<BingPicture> findByAuthor(User author, Pageable pageable);
    Page<BingPicture> searchBingPictures(String keyword, Pageable pageable);
    BingPicture updateBingPicture(BingPicture bingPicture, List<MultipartFile> newImageFiles) throws IOException;
    void deleteBingPicture(Long id);
    BingPicture incrementViewCount(Long id);
} 