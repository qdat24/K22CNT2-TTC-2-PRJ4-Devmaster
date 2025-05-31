package com.freelancer.website.controller;

import com.freelancer.website.entity.User;
import com.freelancer.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    // ===== LOGIN PAGES =====
    
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        // Nếu đã login rồi thì redirect về dashboard
        User currentUser = getCurrentUser(session);
        if (currentUser != null) {
            return redirectToDashboard(currentUser);
        }
        
        return "auth/login";
    }
    
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email, 
            @RequestParam String password,
            HttpSession session,
            Model model) {
        
        // Tìm user theo email và password (plain text)
        Optional<User> userOpt = userRepository.findByEmailAndPassword(email, password);
        
        if (userOpt.isPresent() && userOpt.get().getIsActive()) {
            User user = userOpt.get();
            
            // Lưu user vào session
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userType", user.getUserType().toString());
            
            // Redirect về dashboard tương ứng
            return redirectToDashboard(user);
        } else {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
            return "auth/login";
        }
    }
    
    // ===== REGISTER PAGES =====
    
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }
    
    @GetMapping("/register/job-seeker")
    public String registerJobSeekerPage() {
        return "auth/register-job-seeker";
    }
    
    @GetMapping("/register/employer")
    public String registerEmployerPage() {
        return "auth/register-employer";
    }
    
    @PostMapping("/register/job-seeker")
    public String processJobSeekerRegister(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            HttpSession session,
            Model model) {
        
        return processRegister(email, password, fullName, phone, User.UserType.job_seeker, session, model);
    }
    
    @PostMapping("/register/employer")
    public String processEmployerRegister(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            HttpSession session,
            Model model) {
        
        return processRegister(email, password, fullName, phone, User.UserType.employer, session, model);
    }
    
    // ===== LOGOUT =====
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa toàn bộ session
        return "redirect:/home?logout=success";
    }
    
    // ===== HELPER METHODS =====
    
    private String processRegister(String email, String password, String fullName, String phone, 
                                 User.UserType userType, HttpSession session, Model model) {
        
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return userType == User.UserType.job_seeker ? "auth/register-job-seeker" : "auth/register-employer";
        }
        
        // Tạo user mới
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(password); // Plain text cho demo
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setUserType(userType);
        newUser.setIsActive(true);
        
        // Lưu vào database
        User savedUser = userRepository.save(newUser);
        
        // Auto login sau khi register
        session.setAttribute("currentUser", savedUser);
        session.setAttribute("userId", savedUser.getId());
        session.setAttribute("userType", savedUser.getUserType().toString());
        
        // Redirect về dashboard
        return redirectToDashboard(savedUser);
    }
    
    private String redirectToDashboard(User user) {
        switch (user.getUserType()) {
            case job_seeker:
                return "redirect:/dashboard/job-seeker";
            case employer:
                return "redirect:/dashboard/employer";
            case admin:
                return "redirect:/dashboard/admin";
            default:
                return "redirect:/home";
        }
    }
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}