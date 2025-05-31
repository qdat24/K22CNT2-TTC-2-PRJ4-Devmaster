package com.freelancer.website.repository;

import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ===== BASIC METHODS =====
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    // Tìm user theo user type
    List<User> findByUserType(User.UserType userType);
    
    // Tìm user active
    List<User> findByIsActiveTrue();
    
    // Tìm user theo fullName chứa keyword
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> findByFullNameContaining(@Param("keyword") String keyword);
    
    // Login method
    Optional<User> findByEmailAndPassword(String email, String password);
    
    // ===== COUNT METHODS =====
    
    // Đếm số user theo type và active
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isActive = true")
    long countByUserTypeAndActive(@Param("userType") User.UserType userType);
    
    // Đếm tất cả users active
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    // Đếm tất cả users inactive
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false")
    long countInactiveUsers();
    
    // ===== FIND BY TYPE METHODS =====
    
    // Tìm tất cả employers active
    @Query("SELECT u FROM User u WHERE u.userType = 'employer' AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveEmployers();
    
    // Tìm tất cả job seekers active  
    @Query("SELECT u FROM User u WHERE u.userType = 'job_seeker' AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveJobSeekers();
    
    // Tìm admins
    @Query("SELECT u FROM User u WHERE u.userType = 'admin' AND u.isActive = true")
    List<User> findAdmins();
    
    // ===== PAGINATION METHODS =====
    
    // Pagination cho tất cả users
    Page<User> findAll(Pageable pageable);
    
    // Pagination theo user type
    Page<User> findByUserType(User.UserType userType, Pageable pageable);
    
    // Pagination theo active status
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Pagination theo user type và active status
    Page<User> findByUserTypeAndIsActive(User.UserType userType, Boolean isActive, Pageable pageable);
    
    // ===== SEARCH METHODS =====
    
    // Tìm kiếm user theo nhiều tiêu chí
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:userType IS NULL OR u.userType = CAST(:userType AS com.freelancer.website.entity.User$UserType)) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) " +
           "ORDER BY u.createdAt DESC")
    List<User> searchUsers(@Param("keyword") String keyword, 
                          @Param("userType") String userType, 
                          @Param("isActive") Boolean isActive);
    
    // Tìm kiếm với pagination
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:userType IS NULL OR u.userType = CAST(:userType AS com.freelancer.website.entity.User$UserType)) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> searchUsersWithPagination(@Param("keyword") String keyword, 
                                        @Param("userType") String userType, 
                                        @Param("isActive") Boolean isActive,
                                        Pageable pageable);
    
    // ===== RECENT USERS =====
    
    // Lấy users mới nhất
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
    
    // Lấy users mới nhất theo type
    @Query("SELECT u FROM User u WHERE u.userType = :userType ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByType(@Param("userType") User.UserType userType, Pageable pageable);
    
    // ===== ADMIN SPECIFIC METHODS =====
    
    // Tìm users có thể bị xóa (không phải admin và không phải chính mình)
    @Query("SELECT u FROM User u WHERE u.userType != 'admin' AND u.id != :currentUserId")
    List<User> findDeletableUsers(@Param("currentUserId") Long currentUserId);
    
    // Đếm users theo từng loại
    @Query("SELECT u.userType, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.userType")
    List<Object[]> countUsersByType();
    
    // ===== STATISTICS METHODS =====
    
    // Thống kê users theo tháng
    @Query("SELECT MONTH(u.createdAt) as month, COUNT(u) as count " +
           "FROM User u WHERE YEAR(u.createdAt) = :year " +
           "GROUP BY MONTH(u.createdAt) ORDER BY month")
    List<Object[]> getUserStatsByMonth(@Param("year") int year);
    
    // Thống kê users hoạt động trong N ngày gần đây
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :fromDate AND u.isActive = true")
    long countActiveUsersFromDate(@Param("fromDate") java.time.LocalDateTime fromDate);
    
    // ===== VALIDATION METHODS =====
    
    // Kiểm tra email trừ user hiện tại (cho update)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("userId") Long userId);
    
    // Kiểm tra user có thể deactivate không
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'admin' AND u.isActive = true AND u.id != :userId")
    long countOtherActiveAdmins(@Param("userId") Long userId);
    
    // ===== FOR JOBS PAGINATION (HELPER METHOD) =====
    
    // Helper method để lấy jobs với pagination cho admin
    @Query("SELECT j FROM Job j ORDER BY j.createdAt DESC")
    Page<Job> findAllJobsPaginated(Pageable pageable);
}