
    document.addEventListener('DOMContentLoaded', () => {
        // Hamburger Menu Toggle
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

        // Date Management
        let currentDate = new Date('2025-08-31T10:38:00+0545');
        const dateDisplay = document.getElementById('dateDisplay');
        const sermonList = document.getElementById('sermonList');
        let currentView = 'upcoming';

        function updateDate() {
            dateDisplay.textContent = `Now - ${currentDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
        }

        function navigate(direction) {
            if (direction === 'prev') currentDate.setDate(currentDate.getDate() - 1);
            else if (direction === 'next') currentDate.setDate(currentDate.getDate() + 1);
            else currentDate = new Date('2025-08-31T10:38:00+0545');
            updateDate();
            filterSermons();
        }

        // Sermon Data
        const sermons = [
            { date: '2025-08-31', time: 'Aug 31, 2025 @ 10:00 AM - 12:00 PM', title: 'The Path to Enlightenment', speaker: 'Ajahn Brahm', image: '/site-assets/pexels-koithyr-1360255/image', description: 'A profound exploration of spiritual growth and inner peace led by renowned monk Ajahn Brahm.' },
            { date: '2025-09-01', time: 'Sep 1, 2025 @ 07:00 PM - 09:00 PM', title: 'Mindfulness in Daily Life', speaker: 'Ajahn Thanissaro', image: '/site-assets/pexels-ron-lach-10461522/image', description: 'Learn practical mindfulness techniques to enhance your everyday life with Ajahn Thanissaro.' }
        ];

        function renderSermons() {
            sermonList.innerHTML = '';
            let currentMonthYear = '';
            sermons.forEach((sermon, index) => {
                const sermonMonthYear = new Date(sermon.date).toLocaleString('en-US', { month: 'long', year: 'numeric' });
                if (sermonMonthYear !== currentMonthYear) {
                    const dateHeader = document.createElement('div');
                    dateHeader.className = 'sermon-date';
                    dateHeader.textContent = sermonMonthYear;
                    sermonList.appendChild(dateHeader);
                    currentMonthYear = sermonMonthYear;
                }
                const sermonItem = document.createElement('div');
                sermonItem.className = 'sermon-item';
                sermonItem.innerHTML = `
                    <div class="sermon-details">
                        <div class="date">${new Date(sermon.date).toLocaleString('en-US', { weekday: 'short', day: 'numeric' })}</div>
                        <div class="time">${sermon.time}</div>
                        <div class="title">${sermon.title}</div>
                        <div class="speaker">${sermon.speaker}</div>
                    </div>
                    <div class="sermon-image"><img src="${sermon.image}" alt="${sermon.title}"></div>
                `;
                sermonList.appendChild(sermonItem);
            });
            // Add click event listeners after rendering
            document.querySelectorAll('.sermon-item').forEach(item => {
                item.addEventListener('click', () => {
                    const topicText = item.querySelector('.title').textContent.trim();
                    const sermon = sermons.find(s => s.title === topicText);
                    if (sermon) {
                        const sermonParams = new URLSearchParams({
                            title: sermon.title,
                            date: sermon.date,
                            time: sermon.time,
                            speaker: sermon.speaker,
                            image: sermon.image,
                            description: sermon.description
                        });
                        window.location.href = `upcoming_sermons_descrpt.jsp?${sermonParams.toString()}`;
                    }
                });
            });
        }

        function filterSermons() {
            const month = currentDate.toLocaleString('en-US', { month: 'long' });
            const year = currentDate.getFullYear();
            document.querySelectorAll('.sermon-item').forEach(item => {
                const itemDate = new Date(item.querySelector('.date').textContent.split(' ')[1] + ' ' + month + ' ' + year);
                const itemMonth = itemDate.toLocaleString('en-US', { month: 'long' });
                const itemYear = itemDate.getFullYear();
                item.style.display = (itemMonth === month && itemYear === year) ? 'flex' : 'none';
            });
            document.querySelectorAll('.sermon-date').forEach(date => {
                const dateText = date.textContent.split(' ');
                const dateMonth = dateText[0];
                const dateYear = dateText[1];
                date.style.display = (dateMonth === month && dateYear === year) ? 'block' : 'none';
            });
        }

        // Search Functionality
        function searchSermons() {
            const searchTerm = document.getElementById('sermonSearch').value.toLowerCase();
            const items = document.querySelectorAll('.sermon-item');
            items.forEach(item => {
                const text = item.textContent.toLowerCase();
                item.style.display = text.includes(searchTerm) ? 'flex' : 'none';
            });
        }

        // View Management
        function setView(view) {
            currentView = view;
            document.querySelectorAll('.nav-tabs button').forEach(btn => btn.classList.remove('active'));
            document.querySelector(`.nav-tabs button[onclick="setView('${view}')"]`).classList.add('active');
            renderSermons();
        }

        // Pagination Logic
        let currentPage = 1;
        const sermonsPerPage = 2;

        function loadPreviousSermons() {
            if (currentPage > 1) {
                currentPage--;
                renderSermons();
            }
        }

        function loadNextSermons() {
            currentPage++;
            renderSermons();
        }

        // Add to Calendar
        function addToCalendar() {
            const visibleSermons = document.querySelectorAll('.sermon-item[style="display: flex;"]');
            if (visibleSermons.length > 0) {
                const sermon = sermons.find(s => s.date === visibleSermons[0].querySelector('.date').textContent.split(' ')[1] + '-' + currentDate.getFullYear());
                alert(`Added ${sermon.title} on ${sermon.date} to your calendar!`);
            } else {
                alert('No sermon selected to add to calendar.');
            }
        }

        // Initial Render
        renderSermons();
        updateDate();

        // Add animation keyframes
        const styleSheet = document.createElement('style');
        styleSheet.textContent = `
            @keyframes navLinkFade {
                from { opacity: 0; transform: translateX(50px); }
                to { opacity: 1; transform: translateX(0); }
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
    });
