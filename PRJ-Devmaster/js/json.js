// JavaScript functionality
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tab functionality if tabs exist
    const tabs = document.querySelectorAll('.tab');
    if (tabs.length > 0) {
        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // Remove active class from all tabs
                tabs.forEach(t => t.classList.remove('active'));
                
                // Add active class to clicked tab
                this.classList.add('active');
                
                // Here you would normally show/hide content based on selected tab
                // For example: showTabContent(this.dataset.tabId);
            });
        });
    }
    
    // Search bar functionality
    const searchBar = document.querySelector('.search-bar input');
    const searchButton = document.querySelector('.search-bar button');
    
    if (searchButton && searchBar) {
        searchButton.addEventListener('click', function() {
            performSearch(searchBar.value);
        });
        
        // Also trigger search on Enter key
        searchBar.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });
    }
    
    // Example of search function (placeholder for actual implementation)
    function performSearch(query) {
        if (query.trim() !== '') {
            console.log('Searching for:', query);
            // In a real application, you would redirect to search results page
            // or make an AJAX call to fetch and display results
            alert('Tìm kiếm cho: ' + query);
        }
    }
    
    // Animation for category cards
    const categoryCards = document.querySelectorAll('.category-card');
    
    if (categoryCards.length > 0) {
        categoryCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-10px)';
                this.style.boxShadow = '0 15px 30px rgba(0, 0, 0, 0.1)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '0 5px 15px rgba(0, 0, 0, 0.05)';
            });
        });
    }
    
    // CTA button interactions
    const ctaButtons = document.querySelectorAll('.cta-buttons .btn');
    
    if (ctaButtons.length > 0) {
        ctaButtons.forEach(button => {
            button.addEventListener('click', function() {
                const action = this.textContent.trim();
                
                if (action === 'Đăng dự án') {
                    // Redirect to project creation page or show modal
                    console.log('Navigating to project creation');
                    alert('Chuyển đến trang đăng dự án');
                } else if (action === 'Đăng ký làm Freelancer') {
                    // Redirect to freelancer registration
                    console.log('Navigating to freelancer registration');
                    alert('Chuyển đến trang đăng ký làm Freelancer');
                }
            });
        });
    }
    
    // Responsive navigation menu functionality
    // This would be expanded in a real application with mobile menu toggle
    const windowWidth = window.innerWidth;
    
    if (windowWidth <= 768) {
        // If on mobile, you might want to initialize a mobile menu
        // initMobileMenu();
    }
    
    // Function to initialize mobile menu (placeholder)
    function initMobileMenu() {
        // Create mobile menu toggle button
        const nav = document.querySelector('.navbar');
        const mobileMenuBtn = document.createElement('button');
        mobileMenuBtn.classList.add('mobile-menu-toggle');
        mobileMenuBtn.innerHTML = '☰';
        
        // Insert at the beginning of nav
        nav.insertBefore(mobileMenuBtn, nav.firstChild);
        
        // Add toggle functionality
        mobileMenuBtn.addEventListener('click', function() {
            document.querySelector('.nav-links').classList.toggle('show');
        });
    }
});