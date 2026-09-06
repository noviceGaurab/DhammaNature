
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

    // Basic interactivity for nav buttons
    document.querySelectorAll('.nav-buttons button').forEach(button => {
        button.addEventListener('click', () => {
            alert('Button clicked: ' + button.textContent);
        });
    });

    // Button group functionality for Read Online, Listen audio, Discuss
    document.querySelectorAll('.button-group button').forEach(button => {
        button.addEventListener('click', () => {
            const action = button.textContent.toLowerCase();
            if (action.includes('read now')) {
                window.location.href = 'sermon1_descrpt.jsp'; // Redirect to read-online.html
            } else if (action.includes('listen audio')) {
                alert('Listen audio clicked: ' + button.textContent);
            } else if (action.includes('discuss')) {
                alert('Discuss clicked: ' + button.textContent);
                window.location.href = 'sermon1_discuss.jsp'; // Redirect to forum
            }
        });
    });

    // Search bar functionality
    document.querySelector('.search-bar input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            alert('Searching for: ' + e.target.value);
        }
    });

    // Update time
    function updateTime() {
        const now = new Date();
        console.log(`Time updated: ${now.toLocaleTimeString()}`);
    }
    setInterval(updateTime, 1000);
});
