/**
 * JavaScript functionality for the forgot password page
 */
document.addEventListener('DOMContentLoaded', function() {
    const forgotPasswordForm = document.getElementById('forgot-password-form');
    const emailField = document.getElementById('email');
    const formContainer = document.getElementById('forgot-password-form-container');
    const successMessage = document.getElementById('reset-success');
    
    // Form submission
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Validate email
            const isEmailValid = validateEmail();
            
            // If email is valid, process the form
            if (isEmailValid) {
                // Simulate sending reset email
                simulateResetEmail();
            }
        });
    }
    
    /**
     * Validate email field
     * @return {boolean} Whether validation passed
     */
    function validateEmail() {
        const email = emailField.value.trim();
        
        if (email === '') {
            Validation.showError('email', 'Vui lòng nhập email của bạn');
            return false;
        }
        
        if (!Validation.validateEmail(email)) {
            Validation.showError('email', 'Email không hợp lệ');
            return false;
        }
        
        Validation.clearError('email');
        return true;
    }
    
    /**
     * Simulate sending a password reset email
     */
    function simulateResetEmail() {
        const email = emailField.value.trim();
        
        // Show loading state
        const submitButton = forgotPasswordForm.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        submitButton.disabled = true;
        submitButton.textContent = 'Đang xử lý...';
        
        // In a real app, this would be an API call
        console.log('Gửi yêu cầu khôi phục mật khẩu đến:', email);
        
        // Simulate API delay
        setTimeout(function() {
            // Hide form and show success message
            formContainer.style.display = 'none';
            successMessage.classList.add('show');
            
            // For demo purposes, log the action
            console.log('Reset email đã được gửi đến:', email);
            
            // Reset button state in case the user navigates back
            submitButton.disabled = false;
            submitButton.textContent = originalText;
        }, 1500);
    }
    
    // Add event listener for real-time validation
    if (emailField) {
        emailField.addEventListener('blur', validateEmail);
        emailField.addEventListener('input', function() {
            if (emailField.classList.contains('error')) {
                validateEmail();
            }
        });
    }
});