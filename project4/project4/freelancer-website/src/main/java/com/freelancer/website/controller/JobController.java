package com.freelancer.website.controller;

import com.freelancer.website.entity.Job;
import com.freelancer.website.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    
    @Autowired
    private JobRepository jobRepository;
    
    // GET: Lấy tất cả jobs active
    @GetMapping
    public List<Job> getAllActiveJobs() {
        return jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    // GET: Lấy job theo ID (và tăng view count)
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Optional<Job> job = jobRepository.findById(id);
        if (job.isPresent()) {
            Job jobEntity = job.get();
            jobEntity.incrementViewsCount();
            jobRepository.save(jobEntity);
            return ResponseEntity.ok(jobEntity);
        }
        return ResponseEntity.notFound().build();
    }
    
    // POST: Tạo job mới
    @PostMapping
    public Job createJob(@RequestBody Job job) {
        return jobRepository.save(job);
    }
    
    // PUT: Cập nhật job
    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job jobDetails) {
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            
            job.setTitle(jobDetails.getTitle());
            job.setDescription(jobDetails.getDescription());
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
            
            return ResponseEntity.ok(jobRepository.save(job));
        }
        return ResponseEntity.notFound().build();
    }
    
    // DELETE: Xóa job (set inactive)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            job.setIsActive(false);
            jobRepository.save(job);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // GET: Lấy jobs theo employer ID
    @GetMapping("/employer/{employerId}")
    public List<Job> getJobsByEmployer(@PathVariable Long employerId) {
        return jobRepository.findByEmployerIdAndIsActiveTrueOrderByCreatedAtDesc(employerId);
    }
    
    // GET: Tìm kiếm jobs
    @GetMapping("/search")
    public List<Job> searchJobs(@RequestParam String keyword) {
        return jobRepository.searchJobs(keyword);
    }
    
    // GET: Lọc jobs theo location
    @GetMapping("/location/{location}")
    public List<Job> getJobsByLocation(@PathVariable String location) {
        return jobRepository.findByLocationContainingAndIsActiveTrueOrderByCreatedAtDesc(location);
    }
    
    // GET: Lấy jobs remote
    @GetMapping("/remote")
    public List<Job> getRemoteJobs() {
        return jobRepository.findByIsRemoteTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }
    
    // GET: Lọc theo experience level
    @GetMapping("/experience/{level}")
    public List<Job> getJobsByExperienceLevel(@PathVariable String level) {
        try {
            Job.ExperienceLevel experienceLevel = Job.ExperienceLevel.valueOf(level);
            return jobRepository.findByExperienceLevelAndIsActiveTrueOrderByCreatedAtDesc(experienceLevel);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
    
    // GET: Lọc theo job type
    @GetMapping("/type/{type}")
    public List<Job> getJobsByType(@PathVariable String type) {
        try {
            Job.JobType jobType = Job.JobType.valueOf(type);
            return jobRepository.findByJobTypeAndIsActiveTrueOrderByCreatedAtDesc(jobType);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }
    
    // GET: Lọc theo budget range
    @GetMapping("/budget")
    public List<Job> getJobsByBudgetRange(
            @RequestParam BigDecimal minBudget, 
            @RequestParam BigDecimal maxBudget) {
        return jobRepository.findJobsInBudgetRange(minBudget, maxBudget);
    }
    
    // GET: Jobs mới nhất
    @GetMapping("/latest")
    public List<Job> getLatestJobs() {
        List<Job> allJobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return allJobs.size() > 10 ? allJobs.subList(0, 10) : allJobs;
    }
    
    // GET: Top 6 jobs
    @GetMapping("/top6")
    public List<Job> getTop6Jobs() {
        List<Job> allJobs = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        return allJobs.size() > 6 ? allJobs.subList(0, 6) : allJobs;
    }
    
    // GET: Hot jobs (nhiều view)
    @GetMapping("/hot")
    public List<Job> getHotJobs() {
        List<Job> allJobs = jobRepository.findByIsActiveTrueOrderByViewsCountDesc();
        return allJobs.size() > 10 ? allJobs.subList(0, 10) : allJobs;
    }
    
    // GET: Jobs gần đây (7 ngày)
    @GetMapping("/recent")
    public List<Job> getRecentJobs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return jobRepository.findByIsActiveTrueAndCreatedAtAfterOrderByCreatedAtDesc(sevenDaysAgo);
    }
    
    // GET: Đếm số jobs của employer
    @GetMapping("/employer/{employerId}/count")
    public long countJobsByEmployer(@PathVariable Long employerId) {
        return jobRepository.countByEmployerIdAndIsActiveTrue(employerId);
    }
    
    // GET: Filter jobs theo nhiều tiêu chí
    @GetMapping("/filter")
    public List<Job> filterJobs(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean isRemote) {
        
        Job.JobType jType = null;
        Job.ExperienceLevel expLevel = null;
        
        try {
            if (jobType != null) jType = Job.JobType.valueOf(jobType);
            if (experienceLevel != null) expLevel = Job.ExperienceLevel.valueOf(experienceLevel);
        } catch (IllegalArgumentException e) {
            // Ignore invalid enum values
        }
        
        return jobRepository.filterJobs(jType, expLevel, location, isRemote);
    }
}