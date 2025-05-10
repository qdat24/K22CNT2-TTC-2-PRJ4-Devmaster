/**
 * Utility functions for form validation
 */

const Validation = (function() {
    // Validates an email address
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
    
    // Validates a password strength
    function validatePassword(password) {
        // Define regex patterns for password requirements
        const patterns = {
            length: /.{8,}/,
            uppercase: /[A-Z]/,
            lowercase: /[a-z]/,
            number: /[0-9]/,
            special: /[!@#$%^&*(),.?":{}|<>]/
        };
        
        // Check each requirement
        const requirements = {
            length: patterns.length.test(password),
            uppercase: patterns.uppercase.test(password),
            lowercase: patterns.lowercase.test(password),
            number: patterns.number.test(password),
            special: patterns.special.test(password)
        };
        
        // Calculate strength score (0-4)
        const score = Object.values(requirements).filter(Boolean).length;
        
        // Determine strength level
        let strength = '';
        if (score === 0 || password.length < 6) {
            strength = 'weak';
        } else if (score === 5) {
            strength = 'very-strong';
        } else if (score >= 3) {
            strength = 'strong';
        } else {
            strength = 'medium';
        }
        
        return {
            strength,
            requirements,
            score
        };
    }
    
    // Validates a phone number (basic Vietnamese format)
    function validatePhone(phone) {
        const phoneRegex = /^(0|\+84)(\d{9,10})$/;
        return phoneRegex.test(phone);
    }
    
    // Validates if two passwords match
    function validatePasswordMatch(password, confirmPassword) {
        return password === confirmPassword;
    }
    
    // Displays an error message for a form field
    function showError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}-error`);
        
        if (field && errorElement) {
            field.classList.add('error');
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }
    
    // Clears an error message for a form field
    function clearError(fieldId) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}-error`);
        
        if (field && errorElement) {
            field.classList.remove('error');
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    }
    
    /**
     * Updates password strength UI
     * @param {string} passwordId - The ID of the password field
     * @param {object} validationResult - The result from validatePassword
     */
    function updatePasswordStrength(passwordId, validationResult) {
        const strengthBar = document.querySelector('.strength-bar');
        const strengthText = document.querySelector('.strength-text');
        
        if (strengthBar && strengthText) {
            // Remove all strength classes
            strengthBar.classList.remove('weak', 'medium', 'strong', 'very-strong');
            
            // Add the appropriate strength class
            strengthBar.classList.add(validationResult.strength);
            
            // Update the strength text
            let strengthLabel = '';
            switch (validationResult.strength) {
                case 'weak':
                    strengthLabel = 'Yếu';
                    break;
                case 'medium':
                    strengthLabel = 'Trung bình';
                    break;
                case 'strong':
                    strengthLabel = 'Mạnh';
                    break;
                case 'very-strong':
                    strengthLabel = 'Rất mạnh';
                    break;
            }
            
            strengthText.textContent = `Độ mạnh: ${strengthLabel}`;
            
            // Update requirements list
            const requirements = validationResult.requirements;
            for (const [req, isValid] of Object.entries(requirements)) {
                const reqElement = document.querySelector(`[data-requirement="${req}"]`);
                if (reqElement) {
                    if (isValid) {
                        reqElement.classList.add('valid');
                    } else {
                        reqElement.classList.remove('valid');
                    }
                }
            }
        }
    }
    
    // Public interface
    return {
        validateEmail,
        validatePassword,
        validatePhone,
        validatePasswordMatch,
        showError,
        clearError,
        updatePasswordStrength
    };
})();

// Make the Validation object globally available
window.Validation = Validation;

// Thêm mã để kích hoạt chức năng khi trang tải xong
document.addEventListener('DOMContentLoaded', function() {
    // Kích hoạt chức năng hiển thị/ẩn mật khẩu
    const passwordToggles = document.querySelectorAll('.password-toggle');
    if (passwordToggles.length > 0) {
        passwordToggles.forEach(toggle => {
            toggle.addEventListener('click', function() {
                const passwordInput = this.parentElement.querySelector('input');
                const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordInput.setAttribute('type', type);
            });
        });
    }
    
    // Kích hoạt chức năng đánh giá độ mạnh mật khẩu
    const passwordField = document.getElementById('password');
    if (passwordField) {
        passwordField.addEventListener('input', function() {
            const password = this.value;
            const validationResult = Validation.validatePassword(password);
            
            // Cập nhật UI độ mạnh mật khẩu
            const strengthBar = document.querySelector('.strength-bar');
            if (strengthBar) {
                // Đặt chiều rộng của thanh độ mạnh dựa trên điểm số
                if (password.length === 0) {
                    strengthBar.style.width = '0';
                } else if (validationResult.score <= 2) {
                    strengthBar.style.width = '25%';
                } else if (validationResult.score === 3) {
                    strengthBar.style.width = '50%';
                } else if (validationResult.score === 4) {
                    strengthBar.style.width = '75%';
                } else {
                    strengthBar.style.width = '100%';
                }
            }
            
            // Sử dụng hàm có sẵn để cập nhật UI
            Validation.updatePasswordStrength('password', validationResult);
            
            // Kiểm tra xác nhận mật khẩu nếu có
            const confirmPasswordField = document.getElementById('confirm-password');
            if (confirmPasswordField && confirmPasswordField.value) {
                const confirmPassword = confirmPasswordField.value;
                const isMatch = Validation.validatePasswordMatch(password, confirmPassword);
                
                if (!isMatch) {
                    Validation.showError('confirm-password', 'Mật khẩu không khớp');
                } else {
                    Validation.clearError('confirm-password');
                }
            }
        });
    }
    
    // Kiểm tra xác nhận mật khẩu
    const confirmPasswordField = document.getElementById('confirm-password');
    if (confirmPasswordField && passwordField) {
        confirmPasswordField.addEventListener('input', function() {
            const password = passwordField.value;
            const confirmPassword = this.value;
            const isMatch = Validation.validatePasswordMatch(password, confirmPassword);
            
            if (!isMatch) {
                Validation.showError('confirm-password', 'Mật khẩu không khớp');
            } else {
                Validation.clearError('confirm-password');
            }
        });
    }
});