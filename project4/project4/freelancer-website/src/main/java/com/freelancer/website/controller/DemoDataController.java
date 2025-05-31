package com.freelancer.website.controller;

import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class DemoDataController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @GetMapping("/create-demo-data")
    @ResponseBody
    public String createDemoData() {
        try {
            // Tạo demo users
            if (userRepository.count() < 5) {
                createDemoUsers();
            }
            
            // Tạo demo jobs
            if (jobRepository.count() < 5) {
                createDemoJobs();
            }
            
            return "Demo data created successfully! Users: " + userRepository.count() + ", Jobs: " + jobRepository.count();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating demo data: " + e.getMessage();
        }
    }
    
    private void createDemoUsers() {
        // Admin user
        User admin = new User();
        admin.setEmail("admin@freelancervn.com");
        admin.setPassword("admin123");
        admin.setFullName("Admin FreelancerVN");
        admin.setUserType(User.UserType.admin);
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
        
        // Job Seekers
        User jobSeeker1 = new User();
        jobSeeker1.setEmail("jobseeker1@gmail.com");
        jobSeeker1.setPassword("123456");
        jobSeeker1.setFullName("Nguyễn Văn An");
        jobSeeker1.setPhone("0123456789");
        jobSeeker1.setUserType(User.UserType.job_seeker);
        jobSeeker1.setIsActive(true);
        jobSeeker1.setCreatedAt(LocalDateTime.now());
        userRepository.save(jobSeeker1);
        
        User jobSeeker2 = new User();
        jobSeeker2.setEmail("jobseeker2@gmail.com");
        jobSeeker2.setPassword("123456");
        jobSeeker2.setFullName("Trần Thị Bình");
        jobSeeker2.setPhone("0987654321");
        jobSeeker2.setUserType(User.UserType.job_seeker);
        jobSeeker2.setIsActive(true);
        jobSeeker2.setCreatedAt(LocalDateTime.now());
        userRepository.save(jobSeeker2);
        
        // Employers
        User employer1 = new User();
        employer1.setEmail("employer1@company.com");
        employer1.setPassword("123456");
        employer1.setFullName("Công ty TNHH ABC");
        employer1.setPhone("0111222333");
        employer1.setUserType(User.UserType.employer);
        employer1.setIsActive(true);
        employer1.setCreatedAt(LocalDateTime.now());
        userRepository.save(employer1);
        
        User employer2 = new User();
        employer2.setEmail("employer2@techcorp.com");
        employer2.setPassword("123456");
        employer2.setFullName("TechCorp Vietnam");
        employer2.setPhone("0444555666");
        employer2.setUserType(User.UserType.employer);
        employer2.setIsActive(true);
        employer2.setCreatedAt(LocalDateTime.now());
        userRepository.save(employer2);
    }
    
    private void createDemoJobs() {
        // Lấy employers để tạo jobs
        User employer1 = userRepository.findByEmail("employer1@company.com").orElse(null);
        User employer2 = userRepository.findByEmail("employer2@techcorp.com").orElse(null);
        
        if (employer1 != null) {
            // Job 1
            Job job1 = new Job();
            job1.setEmployerId(employer1.getId());
            job1.setTitle("Thiết kế website bán hàng");
            job1.setDescription("Cần thiết kế website bán hàng online với giao diện đẹp, responsive và user-friendly");
            job1.setRequirements("Kinh nghiệm HTML/CSS, JavaScript, có portfolio thiết kế web");
            job1.setSkillsRequired("HTML, CSS, JavaScript, Photoshop, Figma");
            job1.setBudgetMin(new BigDecimal("3000000"));
            job1.setBudgetMax(new BigDecimal("5000000"));
            job1.setJobType(Job.JobType.fixed);
            job1.setExperienceLevel(Job.ExperienceLevel.intermediate);
            job1.setDuration("2-3 tuần");
            job1.setLocation("Hà Nội");
            job1.setIsRemote(true);
            job1.setIsActive(true);
            job1.setViewsCount(45);
            job1.setApplicationsCount(8);
            job1.setCreatedAt(LocalDateTime.now().minusDays(2));
            jobRepository.save(job1);
            
            // Job 2
            Job job2 = new Job();
            job2.setEmployerId(employer1.getId());
            job2.setTitle("Viết content marketing");
            job2.setDescription("Viết content cho fanpage Facebook và website về lĩnh vực thời trang");
            job2.setRequirements("Có kinh nghiệm viết content, hiểu về marketing");
            job2.setSkillsRequired("Content Writing, Marketing, Social Media");
            job2.setBudgetMin(new BigDecimal("2000000"));
            job2.setBudgetMax(new BigDecimal("4000000"));
            job2.setJobType(Job.JobType.monthly);
            job2.setExperienceLevel(Job.ExperienceLevel.entry);
            job2.setDuration("1-2 tháng");
            job2.setLocation("Hồ Chí Minh");
            job2.setIsRemote(false);
            job2.setIsActive(true);
            job2.setViewsCount(32);
            job2.setApplicationsCount(12);
            job2.setCreatedAt(LocalDateTime.now().minusDays(1));
            jobRepository.save(job2);
        }
        
        if (employer2 != null) {
            // Job 3
            Job job3 = new Job();
            job3.setEmployerId(employer2.getId());
            job3.setTitle("Phát triển mobile app Android");
            job3.setDescription("Phát triển ứng dụng mobile Android cho hệ thống quản lý bán hàng");
            job3.setRequirements("Kinh nghiệm Android development tối thiểu 2 năm");
            job3.setSkillsRequired("Java, Kotlin, Android Studio, Firebase");
            job3.setBudgetMin(new BigDecimal("15000000"));
            job3.setBudgetMax(new BigDecimal("25000000"));
            job3.setJobType(Job.JobType.fixed);
            job3.setExperienceLevel(Job.ExperienceLevel.expert);
            job3.setDuration("2-3 tháng");
            job3.setLocation("Remote");
            job3.setIsRemote(true);
            job3.setIsActive(true);
            job3.setViewsCount(89);
            job3.setApplicationsCount(15);
            job3.setCreatedAt(LocalDateTime.now().minusHours(6));
            jobRepository.save(job3);
            
            // Job 4
            Job job4 = new Job();
            job4.setEmployerId(employer2.getId());
            job4.setTitle("Data Entry và xử lý dữ liệu Excel");
            job4.setDescription("Cần xử lý và làm sạch dữ liệu khách hàng từ file Excel, tạo báo cáo");
            job4.setRequirements("Thành thạo Excel, có kinh nghiệm xử lý dữ liệu");
            job4.setSkillsRequired("Excel, Data Processing, SQL");
            job4.setBudgetMin(new BigDecimal("1500000"));
            job4.setBudgetMax(new BigDecimal("3000000"));
            job4.setJobType(Job.JobType.hourly);
            job4.setExperienceLevel(Job.ExperienceLevel.entry);
            job4.setDuration("1 tháng");
            job4.setLocation("Hà Nội");
            job4.setIsRemote(true);
            job4.setIsActive(true);
            job4.setViewsCount(28);
            job4.setApplicationsCount(6);
            job4.setCreatedAt(LocalDateTime.now().minusHours(12));
            jobRepository.save(job4);
        }
    }
}