
    // Hamburger Menu Toggle with Animation
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

        // Event Detail Script
        const urlParams = new URLSearchParams(window.location.search);
        const event = {
            date: urlParams.get('date'),
            time: urlParams.get('time'),
            title: urlParams.get('title'),
            location: urlParams.get('location'),
            price: urlParams.get('price'),
            image: urlParams.get('image'),
            description: urlParams.get('description')
        };

        document.getElementById('eventTitle').textContent = event.title || 'Event Title';
        document.getElementById('eventImage').innerHTML = `<img src="${event.image || '/assets/images/pexels-2152214764-32117889.jpg'}" alt="${event.title || 'Event Image'}">`;
        document.getElementById('eventDate').textContent = `Date: ${event.date ? new Date(event.date).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) : 'TBD'}`;
        document.getElementById('eventTime').textContent = `Time: ${event.time || 'TBD'}`;
        document.getElementById('eventLocation').textContent = `Location: ${event.location || 'TBD'}`;
        document.getElementById('eventPrice').textContent = `Price: ${event.price || 'Free'}`;
        document.getElementById('eventDescription').textContent = event.description || 'No description available.';
    });

    // Add animation keyframes for nav links and other elements
    const styleSheet = document.createElement('style');
    styleSheet.textContent = `
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

    function goBack() {
        window.history.back();
    }

    function addToCalendar() {
        const urlParams = new URLSearchParams(window.location.search);
        const eventTitle = urlParams.get('title') || 'Event';
        const eventDate = urlParams.get('date') || 'TBD';
        alert(`Added ${eventTitle} on ${eventDate} to your calendar!`);
    }
