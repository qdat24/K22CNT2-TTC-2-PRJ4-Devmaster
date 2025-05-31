package com.freelancer.website.controller;

import com.freelancer.website.entity.Application;
import com.freelancer.website.entity.Bookmark;
import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import com.freelancer.website.repository.ApplicationRepository;
import com.freelancer.website.repository.BookmarkRepository;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private BookmarkRepository bookmarkRepository;
    
    // ===== ADMIN DASHBOARD =====
    
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            // ===== DATA FOR ADMIN DASHBOARD =====
            
            // Overall stats
            long totalUsers = userRepository.count();
            long totalJobs = jobRepository.count();
            long totalActiveJobs = jobRepository.countByIsActiveTrue();
            long totalEmployers = userRepository.countByUserTypeAndActive(User.UserType.employer);
            long totalJobSeekers = userRepository.countByUserTypeAndActive(User.UserType.job_seeker);
            
            // Recent users (HIỂN THỊ TẤT CẢ users thay vì chỉ 5)
            List<User> recentUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(10) // Tăng từ 5 lên 10
                .toList();
            
            // Recent jobs (HIỂN THỊ TẤT CẢ jobs thay vì chỉ 5)
            List<Job> recentJobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .limit(10) // Tăng từ 5 lên 10
                .toList();
            
            // Load employer info cho recent jobs
            Map<Long, User> employersMap = new HashMap<>();
            for (Job job : recentJobs) {
                userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                    employersMap.put(job.getEmployerId(), employer);
                    job.setEmployer(employer);
                });
            }
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            
            // Stats
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("totalActiveJobs", totalActiveJobs);
            model.addAttribute("totalEmployers", totalEmployers);
            model.addAttribute("totalJobSeekers", totalJobSeekers);
            
            // Recent data
            model.addAttribute("recentUsers", recentUsers);
            model.addAttribute("recentJobs", recentJobs);
            model.addAttribute("employersMap", employersMap);
            
            model.addAttribute("pageTitle", "Dashboard - Quản trị viên");
            
            System.out.println("=== ADMIN DASHBOARD DEBUG ===");
            System.out.println("Total Users: " + totalUsers);
            System.out.println("Total Jobs: " + totalJobs);
            System.out.println("Recent Users: " + recentUsers.size());
            System.out.println("Recent Jobs: " + recentJobs.size());
            
        } catch (Exception e) {
            // Log error and show with default data
            System.err.println("Error in adminDashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("pageTitle", "Dashboard - Quản trị viên");
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu");
            
            // Set default values
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("totalJobs", 0L);
            model.addAttribute("totalEmployers", 0L);
            model.addAttribute("totalJobSeekers", 0L);
            model.addAttribute("recentUsers", List.of());
            model.addAttribute("recentJobs", List.of());
            model.addAttribute("employersMap", Map.of());
        }
        
        return "dashboard/admin";
    }
    
    // ===== JOB SEEKER DASHBOARD =====
    
    @GetMapping("/job-seeker")
    public String jobSeekerDashboard(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return "redirect:/login";
        }
        
        try {
            // ===== DATA FOR JOB SEEKER DASHBOARD =====
            
            // Recent jobs (10 việc mới nhất)
            List<Job> recentJobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream().limit(10).toList();
            
            // My applications (đơn ứng tuyển của tôi)
            List<Application> myApplications = applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(currentUser.getId());
            List<Application> recentApplications = myApplications.stream().limit(5).toList();
            
            // Load job và employer info cho applications
            Map<Long, Job> jobsMap = new HashMap<>();
            Map<Long, User> employersMap = new HashMap<>();
            
            for (Application app : recentApplications) {
                jobRepository.findById(app.getJobId()).ifPresent(job -> {
                    jobsMap.put(app.getJobId(), job);
                    // Set job object vào application để dễ sử dụng trong template
                    app.setJob(job);
                    
                    // Load employer info
                    userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                        employersMap.put(job.getEmployerId(), employer);
                        job.setEmployer(employer);
                    });
                });
            }
            
            // Saved jobs (việc đã lưu)
            List<Bookmark> savedBookmarks = bookmarkRepository.findByJobSeekerIdOrderByCreatedAtDesc(currentUser.getId());
            List<Job> savedJobs = savedBookmarks.stream()
                .limit(5)
                .map(bookmark -> jobRepository.findById(bookmark.getJobId()).orElse(null))
                .filter(job -> job != null && job.getIsActive())
                .toList();
            
            // Load employer info cho saved jobs
            for (Job job : savedJobs) {
                if (!employersMap.containsKey(job.getEmployerId())) {
                    userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                        employersMap.put(job.getEmployerId(), employer);
                        job.setEmployer(employer);
                    });
                }
            }
            
            // Load employer info cho recent jobs
            for (Job job : recentJobs) {
                userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                    job.setEmployer(employer);
                });
            }
            
            // Stats
            long totalJobs = jobRepository.countByIsActiveTrue();
            long totalEmployers = userRepository.countByUserTypeAndActive(User.UserType.employer);
            long myApplicationsCount = myApplications.size();
            long savedJobsCount = savedBookmarks.size();
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("recentJobs", recentJobs);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("totalEmployers", totalEmployers);
            
            // Applications data
            model.addAttribute("myApplicationsCount", myApplicationsCount);
            model.addAttribute("appliedJobs", recentApplications);
            model.addAttribute("recentApplications", recentApplications);
            
            // Saved jobs data  
            model.addAttribute("savedJobsCount", savedJobsCount);
            model.addAttribute("savedJobs", savedJobs);
            
            // Maps for template
            model.addAttribute("employersMap", employersMap);
            
            // Mock data for profile completion
            model.addAttribute("profileViewsCount", 28);
            
            model.addAttribute("pageTitle", "Dashboard - Người tìm việc");
            
        } catch (Exception e) {
            // Log error and show with default data
            System.err.println("Error in jobSeekerDashboard: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("pageTitle", "Dashboard - Người tìm việc");
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu");
        }
        
        return "dashboard/job-seeker";
    }
    
    // ===== EMPLOYER DASHBOARD =====
    
    @GetMapping("/employer")
    public String employerDashboard(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        try {
            // ===== DATA FOR EMPLOYER DASHBOARD =====
            
            // My jobs (việc làm của tôi)
            List<Job> myJobs = jobRepository.findByEmployerIdAndIsActiveTrueOrderByCreatedAtDesc(currentUser.getId());
            long myJobsCount = myJobs.size();
            
            // Applications to my jobs (đơn ứng tuyển vào việc của tôi)
            List<Application> recentApplications = applicationRepository.findRecentApplicationsForEmployer(currentUser.getId())
                .stream().limit(5).toList();
            long totalApplications = applicationRepository.countApplicationsForEmployer(currentUser.getId());
            
            // Load job seeker info cho applications
            Map<Long, User> jobSeekersMap = new HashMap<>();
            Map<Long, Job> jobsMap = new HashMap<>();
            
            for (Application app : recentApplications) {
                // Load job seeker info
                userRepository.findById(app.getJobSeekerId()).ifPresent(jobSeeker -> {
                    jobSeekersMap.put(app.getJobSeekerId(), jobSeeker);
                    app.setJobSeeker(jobSeeker);
                });
                
                // Load job info
                jobRepository.findById(app.getJobId()).ifPresent(job -> {
                    jobsMap.put(app.getJobId(), job);
                    app.setJob(job);
                });
            }
            
            // Stats
            long totalJobSeekers = userRepository.countByUserTypeAndActive(User.UserType.job_seeker);
            
            // Recent stats (jobs posted in last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Job> recentJobs = jobRepository.findByIsActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(thirtyDaysAgo)
                .stream()
                .filter(job -> job.getEmployerId().equals(currentUser.getId()))
                .toList();
            
            // Add data to model
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("myJobs", myJobs.stream().limit(5).toList()); // Only show 5 recent jobs on dashboard
            model.addAttribute("allMyJobs", myJobs); // All jobs for stats
            model.addAttribute("myJobsCount", myJobsCount);
            model.addAttribute("totalJobSeekers", totalJobSeekers);
            model.addAttribute("totalApplications", totalApplications);
            model.addAttribute("recentApplications", recentApplications);
            model.addAttribute("recentJobsCount", recentJobs.size());
            
            model.addAttribute("pageTitle", "Dashboard - Nhà tuyển dụng");
            
        } catch (Exception e) {
            // Log error and show with default data
            System.err.println("Error in employerDashboard: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("pageTitle", "Dashboard - Nhà tuyển dụng");
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu");
        }
        
        return "dashboard/employer";
    }
    
    // ===== HELPER METHODS =====
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}