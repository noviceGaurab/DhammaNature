
    document.addEventListener('DOMContentLoaded', () => {
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

                dropdowns.forEach(dropdown => {
                    const dropdownContent = dropdown.querySelector('.dropdown-content');
                    dropdownContent.classList.remove('active');
                });
            });
        }

        dropdowns.forEach(dropdown => {
            const dropdownToggle = dropdown.querySelector('a');
            const dropdownContent = dropdown.querySelector('.dropdown-content');

            dropdownToggle.addEventListener('click', (e) => {
                e.preventDefault();
                dropdownContent.classList.toggle('active');

                dropdowns.forEach(otherDropdown => {
                    if (otherDropdown !== dropdown) {
                        otherDropdown.querySelector('.dropdown-content').classList.remove('active');
                    }
                });

                if (nav.classList.contains('active') && window.innerWidth <= 1200) {
                    nav.classList.remove('active');
                    hamburger.classList.remove('fa-times');
                    hamburger.classList.add('fa-bars');
                    navLinks.forEach(link => link.style.animation = 'none');
                }
            });
        });

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

        let donationAmount = 100;
        const goal = 10000;
        const progress = document.querySelector('.progress');
        const progressText = document.querySelector('.progress-text');

        function setAmount(value) {
            if (value && !isNaN(value) && value > 0) {
                donationAmount = Math.min(parseInt(value), goal);
                document.getElementById('amount').value = donationAmount;
                updateProgress();
            }
        }

        function updateProgress() {
            const percentage = (donationAmount / goal) * 100;
            progress.style.width = `${Math.min(percentage, 100)}%`;
            progressText.textContent = `${Math.min(Math.round(percentage), 100)}%`;
        }

        document.getElementById('donationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const firstName = this.querySelector('input[placeholder="First Name"]').value;
            const lastName = this.querySelector('input[placeholder="Last Name"]').value;
            const email = this.querySelector('input[placeholder="Email Address"]').value;
            const comment = this.querySelector('textarea').value;
            const date = new Date().toLocaleString('en-US', { dateStyle: 'full', timeStyle: 'short', timeZone: 'Asia/Kathmandu' });
            const donationId = Math.floor(Math.random() * 1000) + 100;

            if (firstName && lastName && email && donationAmount) {
                const params = new URLSearchParams({
                    firstName: encodeURIComponent(firstName),
                    lastName: encodeURIComponent(lastName),
                    email: encodeURIComponent(email),
                    amount: donationAmount,
                    date: encodeURIComponent(date),
                    donationId: donationId,
                    comment: encodeURIComponent(comment || 'None'),
                    paymentMethod: 'Test donation'
                });
                window.location.href = `donation_confirm.jsp?${params.toString()}`;
            } else {
                alert('Please fill all required fields and enter a valid donation amount.');
            }
        });

        updateProgress();
    });

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
