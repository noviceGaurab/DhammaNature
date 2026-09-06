
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
        const elements = document.querySelectorAll('.discussion-section');
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

    // Discuss & Discover Script
    // Hover effects for discussion items
    document.querySelectorAll('.discussion-item').forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
        });
        item.addEventListener('mouseleave', function() {
            this.style.backgroundColor = this.classList.contains('odd') ? '#f1f3f5' : '#fff';
        });
    });

    // Click event for discussion items
    document.querySelectorAll('.discussion-item').forEach(item => {
        item.addEventListener('click', () => {
            const topicText = item.querySelector('.discussion-topic').textContent.trim();
            if (topicText === 'No Experience Beyond the Five Aggregates') {
                window.location.href = 'sermon1_discuss.jsp';
            } else {
                console.log(`${topicText} clicked`);
            }
        });
    });

    // Search functionality
    const searchInput = document.getElementById('search-input');
    const discussionItems = document.querySelectorAll('.discussion-item');

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const query = this.value.toLowerCase();
            discussionItems.forEach(item => {
                const topic = item.querySelector('.discussion-topic').textContent.toLowerCase();
                item.style.display = topic.includes(query) ? 'flex' : 'none';
            });
        });
    }

    // Sorting functionality
    const sortLinks = document.querySelectorAll('.search-bar nav a');
    sortLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const sortType = this.dataset.sort;
            sortDiscussions(sortType);
        });
    });

    function sortDiscussions(type) {
        const container = document.getElementById('discussion-section');
        let items = Array.from(discussionItems);

        if (type === 'latest') {
            items.sort((a, b) => {
                const aTime = parseTime(a.dataset.activity);
                const bTime = parseTime(b.dataset.activity);
                return bTime - aTime;
            });
        } else if (type === 'hot') {
            items.sort((a, b) => {
                return b.dataset.replies - a.dataset.replies || b.dataset.views - a.dataset.views;
            });
        }
        // Reappend sorted items
        items.forEach(item => container.appendChild(item));
    }

    function parseTime(timeStr) {
        if (timeStr.includes('h')) {
            return Date.now() - parseInt(timeStr) * 3600000;
        } else if (timeStr.includes('d')) {
            return Date.now() - parseInt(timeStr) * 86400000;
        } else if (timeStr.includes('w')) {
            return Date.now() - parseInt(timeStr) * 604800000;
        } else {
            return new Date(timeStr).getTime();
        }
    }
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
    @keyframes slideIn {
        from { transform: translateX(-100%); }
        to { transform: translateX(0); }
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
 