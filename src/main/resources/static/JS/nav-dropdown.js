document.addEventListener('DOMContentLoaded', () => {
    const hamburger = document.querySelector('.hamburger, .site-hamburger');
    const nav = document.querySelector('nav, .site-nav');
    const dropdowns = document.querySelectorAll('.dropdown, .site-dropdown');

    if (hamburger && nav) {
        hamburger.addEventListener('click', (e) => {
            e.preventDefault();
            nav.classList.toggle('active');
            hamburger.classList.toggle('is-open');
            dropdowns.forEach((dropdown) => {
                const panel = dropdown.querySelector('.dropdown-content, .site-dropdown-content');
                if (panel) panel.classList.remove('active');
            });
        });
    }

    dropdowns.forEach((dropdown) => {
        const toggle = dropdown.querySelector(':scope > a');
        const panel = dropdown.querySelector('.dropdown-content, .site-dropdown-content');
        if (!toggle || !panel) return;

        toggle.addEventListener('click', (e) => {
            // On mobile, use click to open; on desktop hover handles it.
            if (window.innerWidth > 992) return;
            e.preventDefault();
            const willOpen = !panel.classList.contains('active');
            dropdowns.forEach((other) => {
                const otherPanel = other.querySelector('.dropdown-content, .site-dropdown-content');
                if (otherPanel) otherPanel.classList.remove('active');
            });
            if (willOpen) panel.classList.add('active');
        });
    });
});

// ===== Sticky top nav - keep the header bar visible when scrolling =====
(function () {
    let ticking = false;
    function updateSticky() {
        ticking = false;
        const scrolled = (window.pageYOffset || document.documentElement.scrollTop || 0) > 120;
        document.body.classList.toggle('nav-is-sticky', scrolled);
    }
    function onScroll() {
        if (!ticking) {
            window.requestAnimationFrame(updateSticky);
            ticking = true;
        }
    }
    window.addEventListener('scroll', onScroll, { passive: true });
    updateSticky();
})();
