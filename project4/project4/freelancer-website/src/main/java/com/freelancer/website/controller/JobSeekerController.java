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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker")
public class JobSeekerController {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private BookmarkRepository bookmarkRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ===== 1. ỨNG TUYỂN VIỆC LÀM =====
    
    @PostMapping("/job/{jobId}/apply")
    @ResponseBody
    public ResponseEntity<?> applyForJob(@PathVariable Long jobId,
                                        @RequestParam(required = false) String coverLetter,
                                        @RequestParam(required = false) BigDecimal proposedRate,
                                        @RequestParam(required = false) String cvUrl,
                                        HttpSession session) {
        
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty() || !jobOpt.get().getIsActive()) {
            return ResponseEntity.status(404).body("Công việc không tồn tại hoặc đã đóng");
        }
        
        if (applicationRepository.existsByJobIdAndJobSeekerId(jobId, currentUser.getId())) {
            return ResponseEntity.badRequest().body("Bạn đã ứng tuyển vào công việc này rồi");
        }
        
        try {
            Application application = new Application(jobId, currentUser.getId(), coverLetter, proposedRate);
            application.setCvUrl(cvUrl);
            applicationRepository.save(application);
            
            // Update job applications count
            Job job = jobOpt.get();
            job.incrementApplicationsCount();
            jobRepository.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ứng tuyển thành công!");
            response.put("applicationId", application.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    // ===== 2. XEM DANH SÁCH ĐƠN ỨNG TUYỂN =====
    
    @GetMapping("/applications")
    public String myApplications(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return "redirect:/login";
        }
        
        List<Application> applications = applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(currentUser.getId());
        
        // Load job and employer info
        Map<Long, Job> jobs = new HashMap<>();
        Map<Long, User> employers = new HashMap<>();
        
        for (Application app : applications) {
            jobRepository.findById(app.getJobId()).ifPresent(job -> {
                jobs.put(app.getJobId(), job);
                app.setJob(job);
                
                userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                    employers.put(job.getEmployerId(), employer);
                });
            });
        }
        
        model.addAttribute("applications", applications);
        model.addAttribute("jobs", jobs);
        model.addAttribute("employers", employers);
        model.addAttribute("currentUser", currentUser);
        
        return "job-seeker/applications";
    }
    
    // ===== 3. RÚT ĐƠN ỨNG TUYỂN =====
    
    @PostMapping("/application/{applicationId}/withdraw")
    @ResponseBody
    public ResponseEntity<?> withdrawApplication(@PathVariable Long applicationId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Application> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Đơn ứng tuyển không tồn tại");
        }
        
        Application application = appOpt.get();
        
        if (!application.getJobSeekerId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Không có quyền thực hiện hành động này");
        }
        
        if (!application.isPending()) {
            return ResponseEntity.badRequest().body("Chỉ có thể rút đơn ứng tuyển đang chờ xử lý");
        }
        
        try {
            applicationRepository.delete(application);
            
            // Update job applications count
            jobRepository.findById(application.getJobId()).ifPresent(job -> {
                job.decrementApplicationsCount();
                jobRepository.save(job);
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rút đơn ứng tuyển thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/application/{applicationId}")
    @ResponseBody
    public ResponseEntity<?> deleteApplication(@PathVariable Long applicationId, HttpSession session) {
        return withdrawApplication(applicationId, session);
    }
    
    // ===== 4. LƯU VIỆC LÀM YÊU THÍCH =====
    
    @PostMapping("/job/{jobId}/save")
    @ResponseBody
    public ResponseEntity<?> saveJob(@PathVariable Long jobId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Công việc không tồn tại");
        }
        
        if (bookmarkRepository.existsByJobSeekerIdAndJobId(currentUser.getId(), jobId)) {
            return ResponseEntity.badRequest().body("Việc làm này đã được lưu rồi");
        }
        
        try {
            Bookmark bookmark = new Bookmark(currentUser.getId(), jobId);
            bookmarkRepository.save(bookmark);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lưu việc làm thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    // ===== 5. BỎ LƯU VIỆC LÀM =====
    
    @PostMapping("/job/{jobId}/unsave")
    @ResponseBody
    public ResponseEntity<?> unsaveJob(@PathVariable Long jobId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Bookmark> bookmarkOpt = bookmarkRepository.findByJobSeekerIdAndJobId(currentUser.getId(), jobId);
        if (bookmarkOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Chưa lưu việc làm này");
        }
        
        try {
            bookmarkRepository.delete(bookmarkOpt.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bỏ lưu việc làm thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/job/{jobId}/bookmark")
    @ResponseBody
    public ResponseEntity<?> deleteBookmark(@PathVariable Long jobId, HttpSession session) {
        return unsaveJob(jobId, session);
    }
    
    // ===== 6. XEM VIỆC LÀM ĐÃ LƯU =====
    
    @GetMapping("/saved-jobs")
    public String savedJobs(HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return "redirect:/login";
        }
        
        List<Bookmark> bookmarks = bookmarkRepository.findByJobSeekerIdOrderByCreatedAtDesc(currentUser.getId());
        
        // Load job and employer info
        Map<Long, Job> jobs = new HashMap<>();
        Map<Long, User> employers = new HashMap<>();
        
        for (Bookmark bookmark : bookmarks) {
            jobRepository.findById(bookmark.getJobId()).ifPresent(job -> {
                if (job.getIsActive()) {
                    jobs.put(bookmark.getJobId(), job);
                    
                    userRepository.findById(job.getEmployerId()).ifPresent(employer -> {
                        employers.put(job.getEmployerId(), employer);
                    });
                }
            });
        }
        
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("jobs", jobs);
        model.addAttribute("employers", employers);
        model.addAttribute("currentUser", currentUser);
        
        return "job-seeker/saved-jobs";
    }
    
    // ===== 7. SỬA THÔNG TIN ỨNG TUYỂN =====
    
    @GetMapping("/application/{applicationId}/edit")
    public String showEditApplicationForm(@PathVariable Long applicationId, HttpSession session, Model model) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return "redirect:/login";
        }
        
        Optional<Application> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isEmpty() || !appOpt.get().getJobSeekerId().equals(currentUser.getId())) {
            return "redirect:/job-seeker/applications?error=not_found";
        }
        
        Application application = appOpt.get();
        
        // Load job info
        jobRepository.findById(application.getJobId()).ifPresent(job -> {
            application.setJob(job);
        });
        
        model.addAttribute("application", application);
        model.addAttribute("currentUser", currentUser);
        
        return "job-seeker/edit-application";
    }
    
    @PostMapping("/application/{applicationId}/edit")
    public String updateApplication(@PathVariable Long applicationId,
                                   @RequestParam(required = false) String coverLetter,
                                   @RequestParam(required = false) BigDecimal proposedRate,
                                   @RequestParam(required = false) String cvUrl,
                                   HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return "redirect:/login";
        }
        
        Optional<Application> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isEmpty() || !appOpt.get().getJobSeekerId().equals(currentUser.getId())) {
            return "redirect:/job-seeker/applications?error=not_found";
        }
        
        Application application = appOpt.get();
        
        if (!application.isPending()) {
            return "redirect:/job-seeker/applications?error=cannot_edit";
        }
        
        application.setCoverLetter(coverLetter);
        application.setProposedRate(proposedRate);
        application.setCvUrl(cvUrl);
        applicationRepository.save(application);
        
        return "redirect:/job-seeker/applications?success=updated";
    }
    
    @PutMapping("/application/{applicationId}")
    @ResponseBody
    public ResponseEntity<?> updateApplicationAjax(@PathVariable Long applicationId,
                                                  @RequestBody Application applicationDetails,
                                                  HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Application> appOpt = applicationRepository.findById(applicationId);
        if (appOpt.isEmpty() || !appOpt.get().getJobSeekerId().equals(currentUser.getId())) {
            return ResponseEntity.status(404).body("Đơn ứng tuyển không tồn tại");
        }
        
        Application application = appOpt.get();
        
        if (!application.isPending()) {
            return ResponseEntity.badRequest().body("Chỉ có thể sửa đơn ứng tuyển đang chờ xử lý");
        }
        
        try {
            application.setCoverLetter(applicationDetails.getCoverLetter());
            application.setProposedRate(applicationDetails.getProposedRate());
            application.setCvUrl(applicationDetails.getCvUrl());
            applicationRepository.save(application);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật đơn ứng tuyển thành công");
            response.put("application", application);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }
    
    // ===== 8. API CHO DASHBOARD =====
    
    @GetMapping("/job/{jobId}/application-status")
    @ResponseBody
    public ResponseEntity<?> getApplicationStatus(@PathVariable Long jobId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        Optional<Application> appOpt = applicationRepository.findByJobIdAndJobSeekerId(jobId, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            response.put("hasApplied", true);
            response.put("applicationId", app.getId());
            response.put("status", app.getStatus());
            response.put("statusDisplay", app.getStatusDisplay());
            response.put("appliedAt", app.getAppliedAt());
        } else {
            response.put("hasApplied", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/job/{jobId}/bookmark-status")
    @ResponseBody
    public ResponseEntity<?> getBookmarkStatus(@PathVariable Long jobId, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        boolean isSaved = bookmarkRepository.existsByJobSeekerIdAndJobId(currentUser.getId(), jobId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("isSaved", isSaved);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getJobSeekerStats(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isJobSeeker()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        long totalApplications = applicationRepository.countByJobSeekerId(currentUser.getId());
        long savedJobs = bookmarkRepository.countByJobSeekerId(currentUser.getId());
        long acceptedApplications = applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(currentUser.getId())
            .stream().filter(app -> app.getStatus() == Application.ApplicationStatus.ACCEPTED).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", totalApplications);
        stats.put("savedJobs", savedJobs);
        stats.put("acceptedApplications", acceptedApplications);
        
        return ResponseEntity.ok(stats);
    }
    
    // ===== HELPER METHODS =====
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}