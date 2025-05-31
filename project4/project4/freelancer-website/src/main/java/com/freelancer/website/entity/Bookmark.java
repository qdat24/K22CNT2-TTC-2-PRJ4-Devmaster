package com.freelancer.website.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
public class Bookmark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Bookmark() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Bookmark(Long jobSeekerId, Long jobId) {
        this();
        this.jobSeekerId = jobSeekerId;
        this.jobId = jobId;
    }
    
    // JPA Lifecycle
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getJobSeekerId() {
        return jobSeekerId;
    }
    
    public void setJobSeekerId(Long jobSeekerId) {
        this.jobSeekerId = jobSeekerId;
    }
    
    public Long getJobId() {
        return jobId;
    }
    
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}