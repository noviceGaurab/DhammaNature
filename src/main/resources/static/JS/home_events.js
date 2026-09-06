
    // Hamburger Menu Toggle with Animation
    document.addEventListener('DOMContentLoaded', () => {
        const hamburger = document.querySelector('.hamburger');
        const nav = document.querySelector('nav');
        const navLinks = document.querySelectorAll('nav a:not(.donate-btn)');

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
            });
        }

        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                nav.classList.remove('active');
                hamburger.classList.remove('fa-times');
                hamburger.classList.add('fa-bars');
                navLinks.forEach(link => link.style.animation = 'none');
            });
        });

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

        // Animate events on scroll
        const animateOnScroll = () => {
            const events = document.querySelectorAll('.event');
            events.forEach(event => {
                const eventTop = event.getBoundingClientRect().top;
                const windowHeight = window.innerHeight;
                if (eventTop < windowHeight - 100) {
                    event.classList.add('animate__fadeInUp');
                }
            });
        };

        window.addEventListener('scroll', animateOnScroll);

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

        // Form submission alert
        document.querySelector('.form-container form').addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Subscribed successfully!');
            document.querySelector('.email-input').value = '';
        });
    });

    // Add animation keyframes
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
