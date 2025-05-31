package com.freelancer.website.controller;

import com.freelancer.website.entity.Job;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class WebController {
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Trang chủ
    @GetMapping("/home")
    public String home(Model model) {
        // Lấy statistics
        long totalJobs = jobRepository.count();
        long totalUsers = userRepository.count();
        long totalEmployers = userRepository.countByUserTypeAndActive(
            com.freelancer.website.entity.User.UserType.employer);
        long totalJobSeekers = userRepository.countByUserTypeAndActive(
            com.freelancer.website.entity.User.UserType.job_seeker);
        
        // FIXED: Lấy jobs mới nhất (top 6 cho trang chủ)
        List<Job> latestJobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        if (latestJobs.size() > 6) {
            latestJobs = latestJobs.subList(0, 6);
        }
        
        // Truyền data vào view
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalEmployers", totalEmployers);
        model.addAttribute("totalJobSeekers", totalJobSeekers);
        model.addAttribute("latestJobs", latestJobs);
        
        return "index";
    }
    
    // Trang danh sách jobs
    @GetMapping("/jobs")
    public String jobs(Model model, 
                      @RequestParam(required = false) String search,
                      @RequestParam(required = false) String location,
                      @RequestParam(required = false) String type) {
        
        List<Job> jobs;
        
        if (search != null && !search.trim().isEmpty()) {
            jobs = jobRepository.searchJobs(search);
        } else if (location != null && !location.trim().isEmpty()) {
            jobs = jobRepository.findByLocationContainingAndIsActiveTrueOrderByCreatedAtDesc(location);
        } else if (type != null && !type.trim().isEmpty()) {
            try {
                Job.JobType jobType = Job.JobType.valueOf(type);
                jobs = jobRepository.findByJobTypeAndIsActiveTrueOrderByCreatedAtDesc(jobType);
            } catch (IllegalArgumentException e) {
                jobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            }
        } else {
            jobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        }
        
        model.addAttribute("jobs", jobs);
        model.addAttribute("search", search);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        
        return "jobs";
    }
    
    // Trang chi tiết job
    @GetMapping("/job/{id}")
    public String jobDetail(@PathVariable Long id, Model model) {
        Optional<Job> optionalJob = jobRepository.findById(id);
        
        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            
            // Tăng view count
            job.incrementViewsCount();
            jobRepository.save(job);
            
            // Lấy thông tin employer
            Optional<com.freelancer.website.entity.User> employer = 
                userRepository.findById(job.getEmployerId());
            
            // Lấy jobs liên quan (cùng employer)
            List<Job> relatedJobs = jobRepository.findByEmployerIdAndIsActiveTrueOrderByCreatedAtDesc(job.getEmployerId());
            if (relatedJobs.size() > 4) {
                relatedJobs = relatedJobs.subList(0, 4);
            }
            
            // Tính ngân sách display
            String budgetDisplay = "Thỏa thuận";
            if (job.getBudgetMin() != null && job.getBudgetMax() != null) {
                budgetDisplay = String.format("%,.0f - %,.0f VND", 
                    job.getBudgetMin().doubleValue(), 
                    job.getBudgetMax().doubleValue());
            }
            
            // Tính số jobs của employer
            long employerJobsCount = jobRepository.countByEmployerIdAndIsActiveTrue(job.getEmployerId());
            
            model.addAttribute("job", job);
            model.addAttribute("employer", employer.orElse(null));
            model.addAttribute("relatedJobs", relatedJobs);
            model.addAttribute("budgetDisplay", budgetDisplay);
            model.addAttribute("employerJobsCount", employerJobsCount);
            
            return "job-detail";
        } else {
            return "redirect:/jobs";
        }
    }
    
    // Trang search results
    @GetMapping("/search")
    public String search(@RequestParam String q, Model model) {
        List<Job> jobs = jobRepository.searchJobs(q);
        model.addAttribute("jobs", jobs);
        model.addAttribute("searchQuery", q);
        model.addAttribute("resultCount", jobs.size());
        
        return "search-results";
    }
    
    // Redirect / về /home
    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }
}