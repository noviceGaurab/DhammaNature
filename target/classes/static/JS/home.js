
        // Hamburger Menu and Dropdown Toggle with Animation
        document.addEventListener('DOMContentLoaded', () => {
            const hamburger = document.querySelector('.hamburger');
            const nav = document.querySelector('nav');
            const navLinks = document.querySelectorAll('nav a:not(.donate-btn)');
            const heroButton = document.querySelector('.btn-primary');
            const newsletterForm = document.querySelector('.newsletter-form');
            const dropdowns = document.querySelectorAll('.dropdown');

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
                    // Toggle the clicked dropdown
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

            // Close menu when a nav link is clicked
            navLinks.forEach(link => {
                link.addEventListener('click', (e) => {
                    // Only close menu if it's not a dropdown toggle
                    if (!link.parentElement.classList.contains('dropdown')) {
                        nav.classList.remove('active');
                        hamburger.classList.remove('fa-times');
                        hamburger.classList.add('fa-bars');
                        navLinks.forEach(link => link.style.animation = 'none');
                        // Close all dropdowns
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

            // Newsletter Form Submission
            if (newsletterForm) {
                newsletterForm.addEventListener('submit', (e) => {
                    e.preventDefault();
                    const emailInput = document.querySelector('.newsletter-input').value;
                    if (validateEmail(emailInput)) {
                        console.log('Subscribed email:', emailInput);
                        alert('Thank you for subscribing: ' + emailInput);
                        newsletterForm.reset();
                    } else {
                        alert('Please enter a valid email address.');
                    }
                });
            }

            // Email Validation
            function validateEmail(email) {
                const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                return re.test(email);
            }

            // Sermon Button Events
            const sermonButtons = document.querySelectorAll('.button-sermons');
            sermonButtons.forEach(button => {
                button.addEventListener('click', function() {
                    const action = this.textContent.toLowerCase();
                    if (action.includes('watch')) {
                        window.location.href = '15d.html';
                    } else if (action.includes('read')) {
                        window.location.href = 'sermon1.jsp';
                    } else if (action.includes('download')) {
                        window.location.href = 'download.html';
                    }
                });
            });

            // Animate elements on scroll
            const animateOnScroll = () => {
                const elements = document.querySelectorAll('.col, .red-container, .container-2, .container-3, .sermons-1, .sermons-2, .responsive, .responsive-2, .newsletter-container');
                elements.forEach(element => {
                    const elementTop = element.getBoundingClientRect().top;
                    const windowHeight = window.innerHeight;
                    if (elementTop < windowHeight - 100) {
                        element.classList.add('animate__fadeInUp');
                    }
                });
            };

            window.addEventListener('scroll', animateOnScroll);

            // Hero Button Pulse Animation
            if (heroButton) {
                setInterval(() => {
                    heroButton.classList.add('pulse');
                    setTimeout(() => {
                        heroButton.classList.remove('pulse');
                    }, 1000);
                }, 3000);
            }

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
            .pulse {
                animation: pulse 0.5s ease;
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
 