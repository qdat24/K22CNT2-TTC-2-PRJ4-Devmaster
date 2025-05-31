package com.freelancer.website.repository;

import com.freelancer.website.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    // Tìm applications theo job seeker
    List<Application> findByJobSeekerIdOrderByAppliedAtDesc(Long jobSeekerId);
    
    // Tìm applications theo job
    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);
    
    // Kiểm tra đã apply chưa
    boolean existsByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
    
    // Đếm applications theo job seeker
    Long countByJobSeekerId(Long jobSeekerId);
    
    // Đếm applications theo job
    Long countByJobId(Long jobId);
    
    // Tìm applications cho employer - KHÔNG DUPLICATE
    @Query("SELECT a FROM Application a " +
           "JOIN Job j ON a.jobId = j.id " +
           "WHERE j.employerId = :employerId " +
           "ORDER BY a.appliedAt DESC")
    List<Application> findRecentApplicationsForEmployer(@Param("employerId") Long employerId);
    
    // Đếm applications cho employer
    @Query("SELECT COUNT(a) FROM Application a " +
           "JOIN Job j ON a.jobId = j.id " +
           "WHERE j.employerId = :employerId")
    Long countApplicationsForEmployer(@Param("employerId") Long employerId);
    
    // Tìm tất cả applications cho employer (alias khác để tránh confusion)
    @Query("SELECT a FROM Application a " +
           "JOIN Job j ON a.jobId = j.id " +
           "WHERE j.employerId = :employerId " +
           "ORDER BY a.appliedAt DESC")
    List<Application> findApplicationsForEmployer(@Param("employerId") Long employerId);
}