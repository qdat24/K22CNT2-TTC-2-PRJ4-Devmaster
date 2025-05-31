package com.freelancer.website.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;  // Plain text cho demo (không encrypt)
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    private String phone;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ===== ENUM - 3 LOẠI USER =====
    public enum UserType {
        job_seeker,  // Người tìm việc
        employer,    // Người tuyển dụng  
        admin        // Quản lý - THÊM DÒNG NÀY
    }
    
    // ===== CONSTRUCTORS =====
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String email, String password, String fullName, UserType userType) {
        this();
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.userType = userType;
    }
    
    // ===== HELPER METHODS =====
    
    public boolean isJobSeeker() {
        return this.userType == UserType.job_seeker;
    }
    
    public boolean isEmployer() {
        return this.userType == UserType.employer;
    }
    
    public boolean isAdmin() {
        return this.userType == UserType.admin;
    }
    
    public String getRoleDisplayName() {
        switch (this.userType) {
            case job_seeker: return "Người tìm việc";
            case employer: return "Nhà tuyển dụng";
            case admin: return "Quản trị viên";
            default: return "Unknown";
        }
    }
    
    // ===== JPA LIFECYCLE =====
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ===== GETTERS AND SETTERS =====
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType=" + userType +
                ", isActive=" + isActive +
                '}';
    }
}