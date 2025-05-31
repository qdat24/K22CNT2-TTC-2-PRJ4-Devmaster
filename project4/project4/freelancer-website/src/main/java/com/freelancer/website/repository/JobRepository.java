package com.freelancer.website.repository;

import com.freelancer.website.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // ===== BASIC METHODS =====
    
    // Tìm jobs active sắp xếp theo ngày tạo
    List<Job> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // Đếm jobs active
    Long countByIsActiveTrue();
    
    // Tìm jobs theo employer
    List<Job> findByEmployerIdAndIsActiveTrueOrderByCreatedAtDesc(Long employerId);
    
    // Đếm jobs theo employer
    Long countByEmployerIdAndIsActiveTrue(Long employerId);
    
    // Tìm jobs theo location
    List<Job> findByLocationContainingAndIsActiveTrueOrderByCreatedAtDesc(String location);
    
    // Tìm jobs theo type
    List<Job> findByJobTypeAndIsActiveTrueOrderByCreatedAtDesc(Job.JobType jobType);
    
    // Remote jobs
    List<Job> findByIsRemoteTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    // Tìm jobs theo experience level
    List<Job> findByExperienceLevelAndIsActiveTrueOrderByCreatedAtDesc(Job.ExperienceLevel experienceLevel);
    
    // Jobs sắp xếp theo view count (hot jobs)
    List<Job> findByIsActiveTrueOrderByViewsCountDesc();
    
    // Jobs được tạo sau một thời điểm (recent jobs)
    List<Job> findByIsActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);
    
    // ===== ADMIN METHODS =====
    
    // All jobs cho admin (bao gồm cả inactive)
    List<Job> findAllByOrderByCreatedAtDesc();
    
    // Pagination cho admin
    Page<Job> findAll(Pageable pageable);
    
    // Pagination theo active status
    Page<Job> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Pagination theo employer
    Page<Job> findByEmployerId(Long employerId, Pageable pageable);
    
    // ===== COUNT METHODS =====
    
    // Count all jobs cho admin
    @Query("SELECT COUNT(j) FROM Job j")
    Long countAllJobs();
    
    // Đếm jobs inactive
    @Query("SELECT COUNT(j) FROM Job j WHERE j.isActive = false")
    Long countInactiveJobs();
    
    // Đếm jobs theo trạng thái
    Long countByIsActive(Boolean isActive);
    
    // Đếm jobs theo employer và trạng thái
    Long countByEmployerIdAndIsActive(Long employerId, Boolean isActive);
    
    // ===== SEARCH METHODS =====
    
    // Search jobs theo keyword (cho public)
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    // Search jobs cho admin (bao gồm cả inactive)
    @Query("SELECT j FROM Job j WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isActive IS NULL OR j.isActive = :isActive) " +
           "ORDER BY j.createdAt DESC")
    List<Job> searchJobsForAdmin(@Param("keyword") String keyword, @Param("isActive") Boolean isActive);
    
    // Search với pagination
    @Query("SELECT j FROM Job j WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:isActive IS NULL OR j.isActive = :isActive)")
    Page<Job> searchJobsWithPagination(@Param("keyword") String keyword, 
                                      @Param("isActive") Boolean isActive,
                                      Pageable pageable);
    
    // ===== FILTER METHODS =====
    
    // Jobs trong budget range
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "((j.budgetMin >= :minBudget AND j.budgetMax <= :maxBudget) OR " +
           "(j.budgetMin IS NULL AND j.budgetMax IS NULL))")
    List<Job> findJobsInBudgetRange(@Param("minBudget") BigDecimal minBudget, 
                                   @Param("maxBudget") BigDecimal maxBudget);
    
    // Filter theo nhiều điều kiện
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:isRemote IS NULL OR j.isRemote = :isRemote) " +
           "ORDER BY j.createdAt DESC")
    List<Job> filterJobs(@Param("jobType") Job.JobType jobType,
                        @Param("experienceLevel") Job.ExperienceLevel experienceLevel,
                        @Param("location") String location,
                        @Param("isRemote") Boolean isRemote);
    
    // ===== STATISTICS METHODS =====
    
    // Thống kê jobs theo tháng
    @Query("SELECT MONTH(j.createdAt) as month, COUNT(j) as count " +
           "FROM Job j WHERE YEAR(j.createdAt) = :year " +
           "GROUP BY MONTH(j.createdAt) ORDER BY month")
    List<Object[]> getJobStatsByMonth(@Param("year") int year);
    
    // Thống kê theo job type
    @Query("SELECT j.jobType, COUNT(j) FROM Job j WHERE j.isActive = true GROUP BY j.jobType")
    List<Object[]> countJobsByType();
    
    // Thống kê theo experience level
    @Query("SELECT j.experienceLevel, COUNT(j) FROM Job j WHERE j.isActive = true GROUP BY j.experienceLevel")
    List<Object[]> countJobsByExperienceLevel();
    
    // Top locations
    @Query("SELECT j.location, COUNT(j) as count FROM Job j WHERE j.isActive = true AND j.location IS NOT NULL " +
           "GROUP BY j.location ORDER BY count DESC")
    List<Object[]> getTopJobLocations(Pageable pageable);
    
    // ===== TRENDING & POPULAR =====
    
    // Jobs được view nhiều nhất
    @Query("SELECT j FROM Job j WHERE j.isActive = true ORDER BY j.viewsCount DESC")
    List<Job> findMostViewedJobs(Pageable pageable);
    
    // Jobs có nhiều ứng tuyển nhất
    @Query("SELECT j FROM Job j WHERE j.isActive = true ORDER BY j.applicationsCount DESC")
    List<Job> findMostAppliedJobs(Pageable pageable);
    
    // Jobs mới trong N ngày
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.createdAt >= :fromDate ORDER BY j.createdAt DESC")
    List<Job> findRecentJobs(@Param("fromDate") LocalDateTime fromDate);
    
    // Jobs sắp hết hạn
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.deadline IS NOT NULL AND j.deadline BETWEEN CURRENT_DATE AND :toDate ORDER BY j.deadline ASC")
    List<Job> findJobsNearDeadline(@Param("toDate") java.time.LocalDate toDate);
    
    // ===== EMPLOYER SPECIFIC =====
    
    // Jobs của employer với nhiều filter
    @Query("SELECT j FROM Job j WHERE j.employerId = :employerId AND " +
           "(:isActive IS NULL OR j.isActive = :isActive) " +
           "ORDER BY j.createdAt DESC")
    List<Job> findJobsByEmployerAndStatus(@Param("employerId") Long employerId, 
                                         @Param("isActive") Boolean isActive);
    
    // Thống kê jobs của employer
    @Query("SELECT j.isActive, COUNT(j) FROM Job j WHERE j.employerId = :employerId GROUP BY j.isActive")
    List<Object[]> getEmployerJobStats(@Param("employerId") Long employerId);
}