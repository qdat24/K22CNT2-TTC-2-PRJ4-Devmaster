package com.freelancer.website.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "job_seeker_id"})
})
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;
    
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;
    
    @Column(name = "proposed_rate", precision = 10, scale = 2)
    private BigDecimal proposedRate;
    
    @Column(name = "cv_url")
    private String cvUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "employer_notes", columnDefinition = "TEXT")
    private String employerNotes;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations for convenience
    @Transient
    private Job job;
    
    @Transient
    private User jobSeeker;
    
    // Enums
    public enum ApplicationStatus {
        PENDING, REVIEWING, ACCEPTED, REJECTED, INTERVIEW
    }
    
    // Constructors
    public Application() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ApplicationStatus.PENDING;
    }
    
    public Application(Long jobId, Long jobSeekerId) {
        this();
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
    }
    
    public Application(Long jobId, Long jobSeekerId, String coverLetter, BigDecimal proposedRate) {
        this(jobId, jobSeekerId);
        this.coverLetter = coverLetter;
        this.proposedRate = proposedRate;
    }
    
    // JPA Lifecycle
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public String getStatusDisplay() {
        switch (this.status) {
            case PENDING: return "Chờ xử lý";
            case REVIEWING: return "Đang xem xét";
            case ACCEPTED: return "Đã chấp nhận";
            case REJECTED: return "Đã từ chối";
            case INTERVIEW: return "Mời phỏng vấn";
            default: return "Không xác định";
        }
    }
    
    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return this.status == ApplicationStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return this.status == ApplicationStatus.REJECTED;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    
    public Long getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(Long jobSeekerId) { this.jobSeekerId = jobSeekerId; }
    
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    
    public BigDecimal getProposedRate() { return proposedRate; }
    public void setProposedRate(BigDecimal proposedRate) { this.proposedRate = proposedRate; }
    
    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
    
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    
    public String getEmployerNotes() { return employerNotes; }
    public void setEmployerNotes(String employerNotes) { this.employerNotes = employerNotes; }
    
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Transient getters/setters
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    
    public User getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(User jobSeeker) { this.jobSeeker = jobSeeker; }
    
    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", jobSeekerId=" + jobSeekerId +
                ", status=" + status +
                ", appliedAt=" + appliedAt +
                '}';
    }
}