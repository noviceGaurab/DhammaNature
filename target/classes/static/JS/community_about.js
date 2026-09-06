
    document.addEventListener('DOMContentLoaded', () => {
        // Hamburger Menu Toggle
        const hamburger = document.querySelector('.hamburger');
        const nav = document.querySelector('nav');
        const navLinks = document.querySelectorAll('nav a:not(.donate-btn)');
        const dropdowns = document.querySelectorAll('.dropdown');

        if (hamburger && nav) {
            hamburger.addEventListener('click', (e) => {
                e.preventDefault();
                nav.classList.toggle('active');
                hamburger.classList.toggle('fa-bars');
                hamburger.classList.toggle('fa-times');

                navLinks.forEach((link, index) => {
                    if (nav.classList.contains('active')) {
                        link.style.animation = `navLinkFade 0.5s ease forwards ${index / 7 + 0.3}s`;
                    } else {
                        link.style.animation = 'none';
                    }
                });

                // Close all dropdowns when hamburger toggles
                dropdowns.forEach(dropdown => {
                    const dropdownContent = dropdown.querySelector('.dropdown-content');
                    dropdownContent.classList.remove('active');
                });
            });
        }

        // Dropdown Functionality
        dropdowns.forEach(dropdown => {
            const dropdownToggle = dropdown.querySelector('a');
            const dropdownContent = dropdown.querySelector('.dropdown-content');

            dropdownToggle.addEventListener('click', (e) => {
                e.preventDefault();
                dropdownContent.classList.toggle('active');

                // Close other dropdowns
                dropdowns.forEach(otherDropdown => {
                    if (otherDropdown !== dropdown) {
                        otherDropdown.querySelector('.dropdown-content').classList.remove('active');
                    }
                });

                // Close mobile menu if open
                if (nav.classList.contains('active') && window.innerWidth <= 1200) {
                    nav.classList.remove('active');
                    hamburger.classList.remove('fa-times');
                    hamburger.classList.add('fa-bars');
                    navLinks.forEach(link => link.style.animation = 'none');
                }
            });
        });

        // Close menu and dropdowns when a nav link is clicked
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                if (!link.parentElement.classList.contains('dropdown')) {
                    nav.classList.remove('active');
                    hamburger.classList.remove('fa-times');
                    hamburger.classList.add('fa-bars');
                    navLinks.forEach(link => link.style.animation = 'none');
                    dropdowns.forEach(dropdown => {
                        dropdown.querySelector('.dropdown-content').classList.remove('active');
                    });
                }
            });
        });

        // Close dropdowns and menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!nav.contains(e.target) && !hamburger.contains(e.target)) {
                nav.classList.remove('active');
                hamburger.classList.remove('fa-times');
                hamburger.classList.add('fa-bars');
                navLinks.forEach(link => link.style.animation = 'none');
                dropdowns.forEach(dropdown => {
                    dropdown.querySelector('.dropdown-content').classList.remove('active');
                });
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

        // Hover effects for role items
        document.querySelectorAll('.nav-item, .category-item, .tag-item, .role-item').forEach(item => {
            item.addEventListener('mouseenter', function() {
                this.style.backgroundColor = '#e9ecef';
            });
            item.addEventListener('mouseleave', function() {
                this.style.backgroundColor = this.classList.contains('role-item') ? '#f1f3f5' : 'transparent';
            });
        });
    });
