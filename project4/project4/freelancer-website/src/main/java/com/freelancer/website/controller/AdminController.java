package com.freelancer.website.controller;

import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    // ===== QUẢN LÝ USERS =====
    
    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            // Load all users
            List<User> users = userRepository.findAll();
            System.out.println("Found " + users.size() + " users"); // Debug log
            
            // Stats
            long totalUsers = userRepository.count();
            long totalEmployers = userRepository.countByUserTypeAndActive(User.UserType.employer);
            long totalJobSeekers = userRepository.countByUserTypeAndActive(User.UserType.job_seeker);
            
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalEmployers", totalEmployers);
            model.addAttribute("totalJobSeekers", totalJobSeekers);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            
        } catch (Exception e) {
            System.err.println("Error in manageUsers: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("error", "Lỗi khi tải dữ liệu users");
        }
        
        return "admin/users";
    }
    
    @GetMapping("/jobs")
    public String manageJobs(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            // Load all jobs
            List<Job> jobs = jobRepository.findAll();
            System.out.println("Found " + jobs.size() + " jobs"); // Debug log
            
            // Load employer info for jobs
            for (Job job : jobs) {
                userRepository.findById(job.getEmployerId())
                    .ifPresent(job::setEmployer);
            }
            
            // Stats
            long totalJobs = jobRepository.count();
            long activeJobs = jobRepository.countByIsActiveTrue();
            
            model.addAttribute("jobs", jobs);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("activeJobs", activeJobs);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            
        } catch (Exception e) {
            System.err.println("Error in manageJobs: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("jobs", List.of());
            model.addAttribute("totalJobs", 0L);
            model.addAttribute("error", "Lỗi khi tải dữ liệu jobs");
        }
        
        return "admin/jobs";
    }
    
    // ===== API ENDPOINTS =====
    
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody User user, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        try {
            // Validation
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email không được để trống"));
            }
            
            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Họ tên không được để trống"));
            }
            
            if (userRepository.existsByEmail(user.getEmail().trim())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email đã tồn tại"));
            }
            
            // Set default values
            user.setEmail(user.getEmail().trim().toLowerCase());
            user.setFullName(user.getFullName().trim());
            user.setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setPassword("123456"); // Default password
            }
            
            User savedUser = userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Tạo user thành công", "data", savedUser));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @GetMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User không tồn tại"));
        }
    }
    
    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User không tồn tại"));
        }
        
        try {
            User user = userOpt.get();
            
            // Update fields
            if (userDetails.getFullName() != null) {
                user.setFullName(userDetails.getFullName().trim());
            }
            if (userDetails.getPhone() != null) {
                user.setPhone(userDetails.getPhone());
            }
            if (userDetails.getUserType() != null) {
                user.setUserType(userDetails.getUserType());
            }
            if (userDetails.getIsActive() != null) {
                user.setIsActive(userDetails.getIsActive());
            }
            
            // Check email change
            if (userDetails.getEmail() != null && 
                !user.getEmail().equals(userDetails.getEmail().trim().toLowerCase())) {
                if (userRepository.existsByEmail(userDetails.getEmail().trim())) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email đã tồn tại"));
                }
                user.setEmail(userDetails.getEmail().trim().toLowerCase());
            }
            
            // Update password if provided
            if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
                user.setPassword(userDetails.getPassword().trim());
            }
            
            User savedUser = userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật user thành công", "data", savedUser));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User không tồn tại"));
        }
        
        try {
            User user = userOpt.get();
            
            // Prevent deleting admin users
            if (user.isAdmin()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể xóa admin user"));
            }
            
            // Prevent self-deletion
            if (user.getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể xóa chính mình"));
            }
            
            // Soft delete
            user.setIsActive(false);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa user thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // Jobs API
    @GetMapping("/jobs/{id}")
    @ResponseBody
    public ResponseEntity<?> getJobById(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            // Load employer info
            userRepository.findById(job.getEmployerId())
                .ifPresent(job::setEmployer);
            return ResponseEntity.ok(job);
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job không tồn tại"));
        }
    }
    
    @PutMapping("/jobs/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id, 
                                           @RequestParam boolean isActive,
                                           HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job không tồn tại"));
        }
        
        try {
            Job job = jobOpt.get();
            job.setIsActive(isActive);
            jobRepository.save(job);
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", isActive ? "Kích hoạt job thành công" : "Vô hiệu hóa job thành công",
                "isActive", isActive
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/jobs/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteJob(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isAdmin()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job không tồn tại"));
        }
        
        try {
            Job job = jobOpt.get();
            // Soft delete
            job.setIsActive(false);
            jobRepository.save(job);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa job thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== HELPER METHODS =====
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}