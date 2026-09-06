
document.addEventListener('DOMContentLoaded', () => {
    const hamburger = document.querySelector('.hamburger');
    const nav = document.querySelector('nav');
    const navLinks = document.querySelectorAll('nav a:not(.donate-btn)');

    // Hamburger Menu Functionality
    if (hamburger && nav) {
        hamburger.addEventListener('click', (e) => {
            e.preventDefault();
            nav.classList.toggle('active');
            hamburger.classList.toggle('fa-bars');
            hamburger.classList.toggle('fa-times');

            // Animate nav links
            navLinks.forEach((link, index) => {
                if (nav.classList.contains('active')) {
                    link.style.animation = `navLinkFade 0.5s ease forwards ${index / 7 + 0.3}s`;
                } else {
                    link.style.animation = 'none';
                }
            });
        });
    }

    // Close menu when a nav link is clicked
    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            nav.classList.remove('active');
            hamburger.classList.remove('fa-times');
            hamburger.classList.add('fa-bars');
            navLinks.forEach(link => link.style.animation = 'none');
        });
    });

    // Close menu when clicking outside
    document.addEventListener('click', (e) => {
        if (!nav.contains(e.target) && !hamburger.contains(e.target) && nav.classList.contains('active')) {
            nav.classList.remove('active');
            hamburger.classList.remove('fa-times');
            hamburger.classList.add('fa-bars');
            navLinks.forEach(link => link.style.animation = 'none');
        }
    });

    // Smooth Scroll for Internal Links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    // Animate elements on scroll
    const animateOnScroll = () => {
        const elements = document.querySelectorAll('.user-card');
        elements.forEach(element => {
            const elementTop = element.getBoundingClientRect().top;
            const windowHeight = window.innerHeight;
            if (elementTop < windowHeight - 100) {
                element.classList.add('animate__fadeInUp');
            }
        });
    };

    window.addEventListener('scroll', animateOnScroll);

    // Parallax Effect for Header Image
    window.addEventListener('scroll', () => {
        const headerImg = document.querySelector('.header-img img');
        const scrollPosition = window.pageYOffset;
        headerImg.style.transform = `translateY(${scrollPosition * 0.3}px) scale(1.02)`;
    });

    // Back to Top Button
    const backToTop = document.createElement('button');
    backToTop.innerHTML = '<i class="fas fa-arrow-up"></i>';
    backToTop.className = 'back-to-top';
    document.body.appendChild(backToTop);

    backToTop.addEventListener('click', () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });

    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            backToTop.style.display = 'block';
        } else {
            backToTop.style.display = 'none';
        }
    });

    // Search functionality
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const userCards = document.querySelectorAll('.user-card');
            
            userCards.forEach(card => {
                const userName = card.querySelector('.user-name, .user-title').textContent.toLowerCase();
                card.style.display = userName.includes(searchTerm) ? 'flex' : 'none';
            });
        });
    }

    // Add hover effects to navigation items
    document.querySelectorAll('.nav-item, .category-item, .tag-item').forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#e6f4ea';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.backgroundColor = 'transparent';
        });
    });

    // Add click functionality for category items
    document.querySelectorAll('.category-item').forEach(item => {
        item.addEventListener('click', () => {
            const itemText = item.querySelector('span').textContent.toLowerCase();
            if (itemText === 'discussion') {
                window.location.href = 'community_discuss.jsp'; // Redirect to discussion.html
            } else if (itemText === 'about') {
                window.location.href = 'community_about.jsp'; // Redirect to meditation.html
            } else {
                console.log(`${itemText} clicked`);
            }
        });
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const itemText = item.querySelector('span').textContent.toLowerCase();
            if (itemText === 'about') {
                window.location.href = 'community_about.jsp'; // Redirect to discussion.html
            } else {
                console.log(`${itemText} clicked`);
            }
        });
    });

    // Add click functionality to badges
    document.querySelectorAll('.badge').forEach(badge => {
        badge.addEventListener('click', function() {
            this.style.backgroundColor = '#d0e8d8';
            setTimeout(() => {
                this.style.backgroundColor = '#e6f4ea';
            }, 200);
        });
    });
});

// Add animation keyframes for nav links and other elements
const styleSheet = document.createElement('style');
styleSheet.textContent = `
    @keyframes navLinkFade {
        from {
            opacity: 0;
            transform: translateX(50px);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    .animate__fadeInUp {
        animation: fadeInUp 0.6s ease forwards;
    }
    .back-to-top {
        position: fixed;
        bottom: 20px;
        right: 20px;
        background-color: #006015;
        color: #fff;
        border: none;
        border-radius: 50%;
        width: 50px;
        height: 50px;
        display: none;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
        transition: background-color 0.3s, transform 0.3s;
        z-index: 1000;
    }
    .back-to-top:hover {
        background-color: #005c14;
        transform: scale(1.1);
    }
`;
document.head.appendChild(styleSheet);
