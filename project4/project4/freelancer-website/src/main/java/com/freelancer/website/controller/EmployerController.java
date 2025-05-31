package com.freelancer.website.controller;

import com.freelancer.website.entity.Application;
import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import com.freelancer.website.repository.ApplicationRepository;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller 
@RequestMapping("/employer")
public class EmployerController {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ===== 1. TẠO JOB MỚI =====
    
    @GetMapping("/job/create")
    public String showCreateJobForm(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        model.addAttribute("job", new Job());
        model.addAttribute("currentUser", currentUser);
        return "employer/create-job";
    }
    
    @PostMapping("/job/create")
    public String createJob(@ModelAttribute Job job, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        job.setEmployerId(currentUser.getId());
        job.setIsActive(true);
        jobRepository.save(job);
        return "redirect:/dashboard/employer?success=job_created";
    }
    
    @PostMapping("/job/create-ajax")
    @ResponseBody
    public ResponseEntity<?> createJobAjax(@RequestBody Job job, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        try {
            // Validation
            if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tiêu đề không được để trống"));
            }
            
            if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mô tả không được để trống"));
            }
            
            // Set employer and defaults
            job.setEmployerId(currentUser.getId());
            job.setIsActive(true);
            job.setViewsCount(0);
            job.setApplicationsCount(0);
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            
            Job savedJob = jobRepository.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo công việc thành công");
            response.put("job", savedJob);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== 2. SỬA JOB =====
    
    @GetMapping("/job/{id}/edit")
    public String showEditJobForm(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return "redirect:/dashboard/employer?error=job_not_found";
        }
        
        model.addAttribute("job", jobOpt.get());
        model.addAttribute("currentUser", currentUser);
        return "employer/edit-job";
    }
    
    @PostMapping("/job/{id}/edit")
    public String updateJob(@PathVariable Long id, @ModelAttribute Job jobDetails, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return "redirect:/dashboard/employer?error=job_not_found";
        }
        
        Job job = jobOpt.get();
        updateJobFields(job, jobDetails);
        jobRepository.save(job);
        return "redirect:/dashboard/employer?success=job_updated";
    }
    
    @PutMapping("/job/{id}")
    @ResponseBody
    public ResponseEntity<?> updateJobAjax(@PathVariable Long id, @RequestBody Job jobDetails, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        try {
            // Validation
            if (jobDetails.getTitle() == null || jobDetails.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tiêu đề không được để trống"));
            }
            
            if (jobDetails.getDescription() == null || jobDetails.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mô tả không được để trống"));
            }
            
            Job job = jobOpt.get();
            updateJobFields(job, jobDetails);
            job.setUpdatedAt(LocalDateTime.now());
            
            Job savedJob = jobRepository.save(job);
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Cập nhật công việc thành công", 
                "job", savedJob
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // Helper method for updating job fields
    private void updateJobFields(Job job, Job jobDetails) {
        job.setTitle(jobDetails.getTitle().trim());
        job.setDescription(jobDetails.getDescription().trim());
        job.setRequirements(jobDetails.getRequirements());
        job.setSkillsRequired(jobDetails.getSkillsRequired());
        job.setBudgetMin(jobDetails.getBudgetMin());
        job.setBudgetMax(jobDetails.getBudgetMax());
        job.setJobType(jobDetails.getJobType());
        job.setExperienceLevel(jobDetails.getExperienceLevel());
        job.setDuration(jobDetails.getDuration());
        job.setLocation(jobDetails.getLocation());
        job.setIsRemote(jobDetails.getIsRemote());
        job.setDeadline(jobDetails.getDeadline());
    }
    
    // ===== 3. XÓA JOB =====
    
    @PostMapping("/job/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteJob(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        try {
            Job job = jobOpt.get();
            job.setIsActive(false); // Soft delete
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa công việc thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/job/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteJobPermanent(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        try {
            jobRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa vĩnh viễn công việc thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== 4. BẬT/TẮT TRẠNG THÁI JOB =====
    
    @PostMapping("/job/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleJobStatus(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        try {
            Job job = jobOpt.get();
            job.setIsActive(!job.getIsActive());
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isActive", job.getIsActive());
            response.put("message", job.getIsActive() ? "Kích hoạt công việc thành công" : "Tạm dừng công việc thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== 5. XEM DANH SÁCH ỨNG VIÊN =====
    
    @GetMapping("/job/{jobId}/applications")
    public String viewJobApplications(@PathVariable Long jobId, HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return "redirect:/dashboard/employer?error=job_not_found";
        }
        
        Job job = jobOpt.get();
        List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
        
        // Load applicant info
        Map<Long, User> applicants = new HashMap<>();
        for (Application app : applications) {
            userRepository.findById(app.getJobSeekerId()).ifPresent(user -> {
                applicants.put(app.getJobSeekerId(), user);
                app.setJobSeeker(user); // Set for easy access in template
            });
        }
        
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("applicants", applicants);
        model.addAttribute("currentUser", currentUser);
        
        return "employer/job-applications";
    }
    
    // ===== 6. XỬ LÝ ỨNG VIÊN =====
    
    @PostMapping("/application/{applicationId}/status")
    @ResponseBody
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId,
                                                    @RequestParam String status,
                                                    @RequestParam(required = false) String notes,
                                                    HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Application> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Application not found"));
        }
        
        Application application = appOpt.get();
        
        Optional<Job> jobOpt = jobRepository.findById(application.getJobId());
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not authorized"));
        }
        
        try {
            Application.ApplicationStatus newStatus = Application.ApplicationStatus.valueOf(status.toUpperCase());
            application.setStatus(newStatus);
            
            if (notes != null && !notes.trim().isEmpty()) {
                application.setEmployerNotes(notes);
            }
            
            application.setUpdatedAt(LocalDateTime.now());
            applicationRepository.save(application);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
            response.put("newStatus", newStatus.toString());
            response.put("statusDisplay", application.getStatusDisplay());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "Trạng thái không hợp lệ"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    @PostMapping("/application/{applicationId}/accept")
    @ResponseBody
    public ResponseEntity<?> acceptApplication(@PathVariable Long applicationId, HttpSession session) {
        return updateApplicationStatus(applicationId, "ACCEPTED", "Đơn ứng tuyển đã được chấp nhận", session);
    }
    
    @PostMapping("/application/{applicationId}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId, HttpSession session) {
        return updateApplicationStatus(applicationId, "REJECTED", "Đơn ứng tuyển đã bị từ chối", session);
    }
    
    @PostMapping("/application/{applicationId}/interview")
    @ResponseBody
    public ResponseEntity<?> inviteInterview(@PathVariable Long applicationId, HttpSession session) {
        return updateApplicationStatus(applicationId, "INTERVIEW", "Mời ứng viên phỏng vấn", session);
    }
    
    // ===== 7. THỐNG KÊ VÀ BÁO CÁO =====
    
    @GetMapping("/jobs")
    public String myJobs(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        List<Job> myJobs = jobRepository.findByEmployerIdAndIsActiveTrueOrderByCreatedAtDesc(currentUser.getId());
        long totalApplications = applicationRepository.countApplicationsForEmployer(currentUser.getId());
        
        model.addAttribute("jobs", myJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("currentUser", currentUser);
        
        return "employer/my-jobs";
    }
    
    @GetMapping("/applications") 
    public String myApplications(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return "redirect:/login";
        }
        
        List<Application> applications = applicationRepository.findApplicationsForEmployer(currentUser.getId());
        
        // Load job and job seeker info
        Map<Long, Job> jobsMap = new HashMap<>();
        Map<Long, User> jobSeekersMap = new HashMap<>();
        
        for (Application app : applications) {
            jobRepository.findById(app.getJobId()).ifPresent(job -> {
                jobsMap.put(app.getJobId(), job);
                app.setJob(job);
            });
            
            userRepository.findById(app.getJobSeekerId()).ifPresent(jobSeeker -> {
                jobSeekersMap.put(app.getJobSeekerId(), jobSeeker);
                app.setJobSeeker(jobSeeker);
            });
        }
        
        model.addAttribute("applications", applications);
        model.addAttribute("jobsMap", jobsMap);
        model.addAttribute("jobSeekersMap", jobSeekersMap);
        model.addAttribute("currentUser", currentUser);
        
        return "employer/applications";
    }
    
    // ===== 8. API CHO DASHBOARD =====
    
    @GetMapping("/job/{id}")
    @ResponseBody
    public ResponseEntity<?> getJobById(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        return ResponseEntity.ok(jobOpt.get());
    }
    
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getEmployerStats(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        try {
            long totalJobs = jobRepository.countByEmployerIdAndIsActiveTrue(currentUser.getId());
            long totalApplications = applicationRepository.countApplicationsForEmployer(currentUser.getId());
            long acceptedApplications = applicationRepository.findApplicationsForEmployer(currentUser.getId())
                .stream()
                .mapToLong(app -> app.getStatus() == Application.ApplicationStatus.ACCEPTED ? 1 : 0)
                .sum();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalJobs", totalJobs);
            stats.put("totalApplications", totalApplications);
            stats.put("acceptedApplications", acceptedApplications);
            stats.put("pendingApplications", totalApplications - acceptedApplications);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== 9. API MỚI CHO APPLICATIONS MODAL =====
    
    @GetMapping("/job/{jobId}/applications-ajax")
    @ResponseBody
    public ResponseEntity<?> getJobApplicationsAjax(@PathVariable Long jobId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isEmployer()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty() || !jobOpt.get().getEmployerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Job not found"));
        }
        
        try {
            Job job = jobOpt.get();
            List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
            
            // Load applicant info
            for (Application app : applications) {
                userRepository.findById(app.getJobSeekerId()).ifPresent(user -> {
                    app.setJobSeeker(user);
                });
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("job", job);
            response.put("applications", applications);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    // ===== HELPER METHOD =====
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}