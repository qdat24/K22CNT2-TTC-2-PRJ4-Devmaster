package com.freelancer.website.controller;

import com.freelancer.website.entity.Job;
import com.freelancer.website.entity.User;
import com.freelancer.website.repository.JobRepository;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/api")
    public String home() {
        return "🎉 Freelancer Website API đang chạy thành công!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "✅ Database connection OK! Users: " + userRepository.count() + " | Jobs: " + jobRepository.count();
    }
    
    // ===== USER TEST METHODS =====
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/create-demo-users")
    public String createDemoUsers() {
        if (userRepository.count() >= 3) {
            return "✅ Đã có demo users! Total: " + userRepository.count();
        }
        
        // 1. Job seeker
        User jobSeeker = new User();
        jobSeeker.setEmail("jobseeker@gmail.com");
        jobSeeker.setPassword("123456");
        jobSeeker.setFullName("Nguyễn Văn Tìm Việc");
        jobSeeker.setPhone("0123456789");
        jobSeeker.setUserType(User.UserType.job_seeker);
        userRepository.save(jobSeeker);
        
        // 2. Employer
        User employer = new User();
        employer.setEmail("employer@company.com");
        employer.setPassword("123456");
        employer.setFullName("Trần Thị Tuyển Dụng");
        employer.setPhone("0987654321");
        employer.setUserType(User.UserType.employer);
        userRepository.save(employer);
        
        // 3. Admin
        User admin = new User();
        admin.setEmail("admin@freelancer.com");
        admin.setPassword("admin123");
        admin.setFullName("Admin FreelancerVN");
        admin.setPhone("0999888777");
        admin.setUserType(User.UserType.admin);
        userRepository.save(admin);
        
        return "✅ Đã tạo 3 demo users:\n" +
               "- Job Seeker: jobseeker@gmail.com / 123456\n" +
               "- Employer: employer@company.com / 123456\n" +
               "- Admin: admin@freelancer.com / admin123";
    }
    
    @GetMapping("/count-users")
    public String countUsers() {
        long count = userRepository.count();
        long jobSeekers = userRepository.countByUserTypeAndActive(User.UserType.job_seeker);
        long employers = userRepository.countByUserTypeAndActive(User.UserType.employer);
        long admins = userRepository.countByUserTypeAndActive(User.UserType.admin);
        
        return "📊 Total users: " + count + 
               " | Job Seekers: " + jobSeekers + 
               " | Employers: " + employers + 
               " | Admins: " + admins;
    }
    
    @GetMapping("/clear-users")
    public String clearUsers() {
        userRepository.deleteAll();
        return "🗑️ Đã xóa tất cả users!";
    }
    
    // ===== JOB TEST METHODS =====
    
    @GetMapping("/test-jobs")
    public List<Job> getAllJobs() {
        return jobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    @GetMapping("/create-sample-jobs")
    public String createSampleJobs() {
        if (jobRepository.count() > 0) {
            return "✅ Đã có " + jobRepository.count() + " jobs trong database!";
        }
        
        // Tìm employer để làm job owner
        List<User> employers = userRepository.findByUserType(User.UserType.employer);
        if (employers.isEmpty()) {
            return "❌ Cần tạo employer trước! Chạy /create-demo-users";
        }
        
        Long employerId = employers.get(0).getId();
        
        // Job 1: Lập trình web
        Job job1 = new Job();
        job1.setEmployerId(employerId);
        job1.setTitle("Lập trình viên ReactJS Frontend");
        job1.setDescription("Cần tuyển lập trình viên ReactJS có kinh nghiệm để phát triển website thương mại điện tử. Dự án kéo dài 3 tháng với team 5 người.");
        job1.setRequirements("- Ít nhất 2 năm kinh nghiệm ReactJS\n- Biết HTML, CSS, JavaScript\n- Có kinh nghiệm làm việc với API\n- Ưu tiên có kinh nghiệm Redux, TypeScript");
        job1.setSkillsRequired("ReactJS, JavaScript, HTML, CSS, Redux, API Integration");
        job1.setBudgetMin(new BigDecimal("15000000"));
        job1.setBudgetMax(new BigDecimal("25000000"));
        job1.setCurrency("VND");
        job1.setJobType(Job.JobType.monthly);
        job1.setExperienceLevel(Job.ExperienceLevel.intermediate);
        job1.setDuration("3 tháng");
        job1.setLocation("Hà Nội");
        job1.setIsRemote(true);
        job1.setDeadline(LocalDate.now().plusDays(30));
        job1.setApplicationsCount(5); // Demo applications
        job1.setViewsCount(45);
        jobRepository.save(job1);
        
        // Job 2: Mobile app
        Job job2 = new Job();
        job2.setEmployerId(employerId);
        job2.setTitle("Phát triển ứng dụng Mobile Flutter");
        job2.setDescription("Xây dựng ứng dụng di động đặt đồ ăn sử dụng Flutter. App cần tích hợp payment gateway và real-time tracking.");
        job2.setRequirements("- Kinh nghiệm Flutter/Dart\n- Đã từng làm app thương mại\n- Biết tích hợp API\n- Có portfolio mobile apps");
        job2.setSkillsRequired("Flutter, Dart, Mobile Development, API, Firebase");
        job2.setBudgetMin(new BigDecimal("50000000"));
        job2.setBudgetMax(new BigDecimal("80000000"));
        job2.setCurrency("VND");
        job2.setJobType(Job.JobType.fixed);
        job2.setExperienceLevel(Job.ExperienceLevel.expert);
        job2.setDuration("4-6 tháng");
        job2.setLocation("TP.HCM");
        job2.setIsRemote(false);
        job2.setDeadline(LocalDate.now().plusDays(45));
        job2.setApplicationsCount(8); // Demo applications
        job2.setViewsCount(72);
        jobRepository.save(job2);
        
        // Job 3: Data entry
        Job job3 = new Job();
        job3.setEmployerId(employerId);
        job3.setTitle("Data Entry và xử lý dữ liệu Excel");
        job3.setDescription("Cần người nhập liệu và xử lý dữ liệu khách hàng từ các nguồn khác nhau vào hệ thống Excel/Google Sheets.");
        job3.setRequirements("- Thành thạo Excel, Google Sheets\n- Tỉ mỉ, chính xác\n- Có thể làm việc remote\n- Cam kết thời gian");
        job3.setSkillsRequired("Excel, Google Sheets, Data Entry, Data Processing");
        job3.setBudgetMin(new BigDecimal("5000000"));
        job3.setBudgetMax(new BigDecimal("8000000"));
        job3.setCurrency("VND");
        job3.setJobType(Job.JobType.hourly);
        job3.setExperienceLevel(Job.ExperienceLevel.entry);
        job3.setDuration("2 tháng");
        job3.setLocation("Remote");
        job3.setIsRemote(true);
        job3.setDeadline(LocalDate.now().plusDays(15));
        job3.setApplicationsCount(3); // Demo applications
        job3.setViewsCount(28);
        jobRepository.save(job3);
        
        return "✅ Đã tạo 3 sample jobs với demo applications";
    }
    
    @GetMapping("/count-jobs")
    public String countJobs() {
        long count = jobRepository.count();
        long activeCount = jobRepository.findByIsActiveTrueOrderByCreatedAtDesc().size();
        return "📊 Tổng số jobs: " + count + " | Active jobs: " + activeCount;
    }
    
    @GetMapping("/clear-jobs")
    public String clearJobs() {
        jobRepository.deleteAll();
        return "🗑️ Đã xóa tất cả jobs!";
    }
    
    @GetMapping("/search-react")
    public List<Job> searchReactJobs() {
        return jobRepository.searchJobs("React");
    }
    
    @GetMapping("/remote-jobs")
    public List<Job> getRemoteJobs() {
        return jobRepository.findByIsRemoteTrueAndIsActiveTrueOrderByCreatedAtDesc();
    }
    
    // ===== SETUP & DEMO =====
    
    @GetMapping("/setup-demo")
    public String setupDemo() {
        // Clear existing data
        jobRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create demo users
        createDemoUsers();
        
        // Create jobs
        createSampleJobs();
        
        return "🚀 DEMO SETUP COMPLETE!\n" +
               "✅ Users: " + userRepository.count() + "\n" +
               "✅ Jobs: " + jobRepository.count() + "\n\n" +
               "🔐 Demo Login Accounts:\n" +
               "- Job Seeker: jobseeker@gmail.com / 123456\n" +
               "- Employer: employer@company.com / 123456\n" +
               "- Admin: admin@freelancer.com / admin123\n\n" +
               "🌐 Test URLs:\n" +
               "- /login (Login page)\n" +
               "- /dashboard/job-seeker (Job seeker dashboard)\n" +
               "- /dashboard/employer (Employer dashboard)\n" +
               "- /dashboard/admin (Admin dashboard)\n" +
               "- /home (Homepage)\n" +
               "- /jobs (Jobs listing)";
    }
}