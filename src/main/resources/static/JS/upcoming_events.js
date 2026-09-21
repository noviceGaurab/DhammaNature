
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
            const elements = document.querySelectorAll('.event-list');
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

        // Events Hub Script
        let currentDate = new Date('2025-08-31T17:26:00+0545');
        const dateDisplay = document.getElementById('dateDisplay');
        const eventList = document.getElementById('eventList');
        let currentView = 'find';

        function updateDate() {
            dateDisplay.textContent = `Now - ${currentDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
        }

        function navigate(direction) {
            if (direction === 'prev') currentDate.setDate(currentDate.getDate() - 1);
            else if (direction === 'next') currentDate.setDate(currentDate.getDate() + 1);
            else currentDate = new Date('2025-08-31T17:26:00+0545');
            updateDate();
            filterEvents();
        }

        const events = [
            { date: '2025-09-01', time: 'Aug 25, 2025 @ 09:23 PM - Aug 27, 2025 @ 06:00 PM', title: 'Summer Dhamma Retreat', location: 'Dhamma Center, New York, NY, United States', price: '$175', image: '/site-assets/pexels-ajaybhargavguduru-939700/image', description: 'Join us for a serene weekend of meditation and dhamma teachings in the heart of New York.' },
            { date: '2025-09-02', time: 'Aug 26, 2025 @ 07:00 PM - Aug 28, 2025 @ 05:00 PM', title: 'Mindfulness Workshop', location: 'Brooklyn Dhamma Hall, NY, United States', price: '$250', image: '/site-assets/pexels-caleboquendo-3030090/image', description: 'Explore mindfulness practices and dhamma insights at our iconic Brooklyn venue.' }
        ];

        function renderEvents() {
            console.log('Rendering events with images:', events.map(e => e.image));
            eventList.innerHTML = '';
            let currentMonthYear = '';
            events.forEach((event, index) => {
                const eventMonthYear = new Date(event.date).toLocaleString('en-US', { month: 'long', year: 'numeric' });
                if (eventMonthYear !== currentMonthYear) {
                    const dateHeader = document.createElement('div');
                    dateHeader.className = 'event-date';
                    dateHeader.textContent = eventMonthYear;
                    eventList.appendChild(dateHeader);
                    currentMonthYear = eventMonthYear;
                }
                const eventItem = document.createElement('div');
                eventItem.className = 'event-item';
                eventItem.innerHTML = `
                    <div class="event-details">
                        <div class="date">${new Date(event.date).toLocaleString('en-US', { weekday: 'short', day: 'numeric' })}</div>
                        <div class="time">${event.time}</div>
                        <div class="title">${event.title}</div>
                        <div class="location">${event.location}</div>
                        <div class="price">${event.price}</div>
                    </div>
                    <div class="event-image">
                        <img src="${event.image}" alt="${event.title}" onerror="this.src='/site-assets/transparent_logo/image'">
                    </div>
                `;
                eventList.appendChild(eventItem);
            });
            // Add click event listeners after rendering
            document.querySelectorAll('.event-item').forEach(item => {
                item.addEventListener('click', () => {
                    const topicText = item.querySelector('.title').textContent.trim();
                    if (topicText === 'Summer Dhamma Retreat') {
                        const eventParams = new URLSearchParams({
                            title: topicText,
                            date: '2025-09-01',
                            time: 'Aug 25, 2025 @ 09:23 PM - Aug 27, 2025 @ 06:00 PM',
                            location: 'Dhamma Center, New York, NY, United States',
                            price: '$175',
                            image: '/site-assets/pexels-ajaybhargavguduru-939700/image',
                            description: 'Join us for a serene weekend of meditation and dhamma teachings in the heart of New York.'
                        });
                        window.location.href = `upcoming_events_descrpt.jsp?${eventParams.toString()}`;
                    } else {
                        console.log(`${topicText} clicked`);
                    }
                });
            });
        }

        function filterEvents() {
            const month = currentDate.toLocaleString('en-US', { month: 'long' });
            const year = currentDate.getFullYear();
            document.querySelectorAll('.event-item').forEach(item => {
                const itemDate = new Date(item.querySelector('.date').textContent.split(' ')[1] + ' ' + month + ' ' + year);
                const itemMonth = itemDate.toLocaleString('en-US', { month: 'long' });
                const itemYear = itemDate.getFullYear();
                item.style.display = (itemMonth === month && itemYear === year) ? 'flex' : 'none';
            });
            document.querySelectorAll('.event-date').forEach(date => {
                const dateText = date.textContent.split(' ');
                const dateMonth = dateText[0];
                const dateYear = dateText[1];
                date.style.display = (dateMonth === month && dateYear == year) ? 'block' : 'none';
            });
        }

        function searchEvents() {
            const searchTerm = document.getElementById('eventSearch').value.toLowerCase();
            const items = document.querySelectorAll('.event-item');
            items.forEach(item => {
                const text = item.textContent.toLowerCase();
                item.style.display = text.includes(searchTerm) ? 'flex' : 'none';
            });
        }

        function setView(view) {
            currentView = view;
            document.querySelectorAll('.nav-tabs button').forEach(btn => btn.classList.remove('active'));
            document.querySelector(`.nav-tabs button[onclick="setView('${view}')"]`).classList.add('active');
            renderEvents();
        }

        function loadPreviousEvents() {
            if (currentPage > 1) {
                currentPage--;
                renderEvents();
            }
        }

        function loadNextEvents() {
            currentPage++;
            renderEvents();
        }

        function addToCalendar() {
            const visibleEvents = document.querySelectorAll('.event-item[style="display: flex;"]');
            if (visibleEvents.length > 0) {
                const event = events.find(e => e.date === visibleEvents[0].querySelector('.date').textContent.split(' ')[1]);
                alert(`Added ${event.title} on ${event.date} to your calendar!`);
            } else {
                alert('No event selected to add to calendar.');
            }
        }

        // Initial Render
        renderEvents();
        updateDate();
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
