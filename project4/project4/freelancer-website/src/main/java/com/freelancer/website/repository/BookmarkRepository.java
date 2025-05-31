package com.freelancer.website.repository;

import com.freelancer.website.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    // Tìm bookmarks theo job seeker
    List<Bookmark> findByJobSeekerIdOrderByCreatedAtDesc(Long jobSeekerId);
    
    // Tìm bookmark cụ thể
    Optional<Bookmark> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
    
    // Kiểm tra đã bookmark chưa
    boolean existsByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
    
    // Đếm bookmarks theo job seeker
    Long countByJobSeekerId(Long jobSeekerId);
    
    // Xóa bookmark
    void deleteByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
}