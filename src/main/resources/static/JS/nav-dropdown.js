document.addEventListener('DOMContentLoaded', () => {
    const hamburger = document.querySelector('.header-main .hamburger, .site-header .site-hamburger, header .hamburger');
    const nav = document.querySelector('.header-main nav, .site-header nav, header nav');
    const dropdowns = document.querySelectorAll('.header-main .dropdown, .site-header .site-dropdown, header .dropdown');

    function syncIcon() {
        if (!hamburger) return;
        const icon = hamburger.querySelector('i');
        if (!icon) return;
        const open = nav && nav.classList.contains('active');
        icon.classList.remove('fa-bars', 'fa-times', 'fa-xmark');
        icon.classList.add(open ? 'fa-xmark' : 'fa-bars');
        hamburger.classList.toggle('is-open', !!open);
    }

    if (hamburger && nav) {
        const closeMenu = () => {
            nav.classList.remove('active');
            hamburger.classList.remove('is-open');
            syncIcon();
        };

        hamburger.addEventListener('click', (e) => {
            e.preventDefault();
            nav.classList.toggle('active');
            syncIcon();
            dropdowns.forEach((dropdown) => {
                const panel = dropdown.querySelector('.dropdown-content, .site-dropdown-content');
                if (panel) panel.classList.remove('active');
            });
        });

        nav.addEventListener('click', (e) => {
            const link = e.target.closest('a');
            if (link && link.closest('.dropdown-content, .site-dropdown-content')) {
                closeMenu();
            }
        });

        document.addEventListener('click', (e) => {
            if (!nav.classList.contains('active')) return;
            if (nav.contains(e.target) || (hamburger && hamburger.contains(e.target))) return;
            closeMenu();
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && nav.classList.contains('active')) {
                closeMenu();
            }
        });

        syncIcon();
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