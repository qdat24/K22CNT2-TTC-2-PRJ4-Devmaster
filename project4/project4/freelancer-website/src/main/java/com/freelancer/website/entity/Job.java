package com.freelancer.website.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employer_id", nullable = false)
    private Long employerId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;
    
    @Column(name = "budget_min", precision = 12, scale = 2)
    private BigDecimal budgetMin;
    
    @Column(name = "budget_max", precision = 12, scale = 2)
    private BigDecimal budgetMax;
    
    @Column(length = 10)
    private String currency = "VND";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobType jobType = JobType.fixed;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    private ExperienceLevel experienceLevel = ExperienceLevel.intermediate;
    
    @Column(length = 100)
    private String duration;
    
    private String location;
    
    @Column(name = "is_remote")
    private Boolean isRemote = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_premium")
    private Boolean isPremium = false;
    
    private LocalDate deadline;
    
    @Column(name = "applications_count")
    private Integer applicationsCount = 0;
    
    @Column(name = "views_count")
    private Integer viewsCount = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ===== TRANSIENT FIELDS (không lưu DB, chỉ để tiện sử dụng) =====
    @Transient
    private User employer;
    
    // Enums
    public enum JobType {
        hourly, fixed, monthly
    }
    
    public enum ExperienceLevel {
        entry, intermediate, expert
    }
    
    // ===== CONSTRUCTORS =====
    public Job() {}
    
    public Job(Long employerId, String title, String description) {
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.isActive = true;
        this.applicationsCount = 0;
        this.viewsCount = 0;
    }
    
    // ===== JPA LIFECYCLE METHODS =====
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.applicationsCount == null) this.applicationsCount = 0;
        if (this.viewsCount == null) this.viewsCount = 0;
        if (this.isActive == null) this.isActive = true;
        if (this.isRemote == null) this.isRemote = false;
        if (this.isPremium == null) this.isPremium = false;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ===== HELPER METHODS =====
    public void incrementViewsCount() {
        this.viewsCount = (this.viewsCount == null ? 0 : this.viewsCount) + 1;
    }
    
    public void incrementApplicationsCount() {
        this.applicationsCount = (this.applicationsCount == null ? 0 : this.applicationsCount) + 1;
    }
    
    public void decrementApplicationsCount() {
        if (this.applicationsCount == null || this.applicationsCount <= 0) {
            this.applicationsCount = 0;
        } else {
            this.applicationsCount--;
        }
    }
    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEmployerId() {
        return employerId;
    }
    
    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRequirements() {
        return requirements;
    }
    
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
    
    public String getSkillsRequired() {
        return skillsRequired;
    }
    
    public void setSkillsRequired(String skillsRequired) {
        this.skillsRequired = skillsRequired;
    }
    
    public BigDecimal getBudgetMin() {
        return budgetMin;
    }
    
    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }
    
    public BigDecimal getBudgetMax() {
        return budgetMax;
    }
    
    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public JobType getJobType() {
        return jobType;
    }
    
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }
    
    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Boolean getIsRemote() {
        return isRemote;
    }
    
    public void setIsRemote(Boolean isRemote) {
        this.isRemote = isRemote;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsPremium() {
        return isPremium;
    }
    
    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }
    
    public LocalDate getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    public Integer getApplicationsCount() {
        return applicationsCount != null ? applicationsCount : 0;
    }
    
    public void setApplicationsCount(Integer applicationsCount) {
        this.applicationsCount = applicationsCount;
    }
    
    public Integer getViewsCount() {
        return viewsCount != null ? viewsCount : 0;
    }
    
    public void setViewsCount(Integer viewsCount) {
        this.viewsCount = viewsCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ===== TRANSIENT FIELD GETTERS/SETTERS =====
    
    public User getEmployer() {
        return employer;
    }
    
    public void setEmployer(User employer) {
        this.employer = employer;
    }
    
    // ===== HELPER METHODS FOR DISPLAY =====
    
    public String getJobTypeDisplay() {
        switch (this.jobType) {
            case hourly:
                return "Theo giờ";
            case fixed:
                return "Cố định";
            case monthly:
                return "Theo tháng";
            default:
                return "Không xác định";
        }
    }
    
    public String getExperienceLevelDisplay() {
        switch (this.experienceLevel) {
            case entry:
                return "Mới bắt đầu";
            case intermediate:
                return "Trung cấp";
            case expert:
                return "Chuyên gia";
            default:
                return "Không xác định";
        }
    }
    
    public String getFormattedBudget() {
        if (budgetMin != null && budgetMax != null) {
            return String.format("%,.0f - %,.0f %s", budgetMin, budgetMax, currency);
        } else if (budgetMin != null) {
            return String.format("Từ %,.0f %s", budgetMin, currency);
        } else if (budgetMax != null) {
            return String.format("Tối đa %,.0f %s", budgetMax, currency);
        } else {
            return "Thỏa thuận";
        }
    }
    
    public boolean isExpired() {
        return deadline != null && deadline.isBefore(LocalDate.now());
    }
    
    public boolean isNew() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", employerId=" + employerId +
                ", budgetMin=" + budgetMin +
                ", budgetMax=" + budgetMax +
                ", location='" + location + '\'' +
                ", isRemote=" + isRemote +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}