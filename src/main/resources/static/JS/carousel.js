/* ============================================================
   Dhamma Nature - Generic image carousel initializer.
   Works with containers marked .dn-carousel:
     <div class="dn-carousel" data-autoplay="4500" data-mode="fade">
       <div class="dn-track"> ... .dn-slide elements ... </div>
     </div>
   Arrows (.dn-arrow.prev/.next) and dots (.dn-dots) are injected
   automatically. Supports slide mode (horizontal) and fade mode
   (cross-fade), swipe, autoplay and pause-on-hover.
   ============================================================ */
(function () {
    'use strict';

    function initCarousel(root) {
        var track = root.querySelector('.dn-track');
        if (!track) return;
        var slides = Array.prototype.slice.call(track.children);
        if (slides.length === 0) return;

        var mode = root.getAttribute('data-mode') || 'slide';
        var autoplay = parseInt(root.getAttribute('data-autoplay'), 10) || 0;
        var idx = 0;
        var timer = null;
        var hold = false;

        /* Build arrows */
        var prev = document.createElement('button');
        prev.type = 'button';
        prev.className = 'dn-arrow prev';
        prev.setAttribute('aria-label', 'Previous image');
        prev.innerHTML = '&#10094;';
        var next = document.createElement('button');
        next.type = 'button';
        next.className = 'dn-arrow next';
        next.setAttribute('aria-label', 'Next image');
        next.innerHTML = '&#10095;';
        root.appendChild(prev);
        root.appendChild(next);

        /* Build dots */
        var dotsWrap = document.createElement('div');
        dotsWrap.className = 'dn-dots';
        var dots = slides.map(function (s, i) {
            var d = document.createElement('button');
            d.type = 'button';
            d.className = 'dn-dot' + (i === 0 ? ' active' : '');
            d.setAttribute('aria-label', 'Go to image ' + (i + 1));
            d.addEventListener('click', function () { goTo(i); });
            dotsWrap.appendChild(d);
            return d;
        });
        root.appendChild(dotsWrap);

        /* Set active class for fade mode */
        function activate(i) {
            slides.forEach(function (s, n) {
                s.classList.toggle('active', n === i);
            });
            dots.forEach(function (d, n) {
                d.classList.toggle('active', n === i);
            });
        }

        function goTo(i) {
            var total = slides.length;
            idx = (i + total) % total;
            if (mode === 'fade') {
                activate(idx);
            } else {
                track.style.transform = 'translateX(-' + (idx * 100) + '%)';
            }
            activate(idx);
        }

        /* Autoplay */
        function tick() {
            if (!autoplay || hold) return;
            goTo(idx + 1);
        }
        function start() {
            if (autoplay) timer = setInterval(tick, autoplay);
        }
        function stop() {
            if (timer) { clearInterval(timer); timer = null; }
        }

        prev.addEventListener('click', function () { stop(); goTo(idx - 1); if (autoplay) start(); });
        next.addEventListener('click', function () { stop(); goTo(idx + 1); if (autoplay) start(); });
        root.addEventListener('mouseenter', function () { hold = true; });
        root.addEventListener('mouseleave', function () { hold = false; });

        /* Touch / swipe */
        var startX = 0;
        root.addEventListener('touchstart', function (e) {
            startX = e.touches[0].clientX;
        }, { passive: true });
        root.addEventListener('touchend', function (e) {
            var dx = e.changedTouches[0].clientX - startX;
            if (Math.abs(dx) > 40) {
                stop();
                goTo(dx < 0 ? idx + 1 : idx - 1);
                if (autoplay) start();
            }
        }, { passive: true });

        /* Keyboard: only when this carousel is focused/in view */
        root.setAttribute('tabindex', '0');
        root.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowLeft') { e.preventDefault(); stop(); goTo(idx - 1); if (autoplay) start(); }
            if (e.key === 'ArrowRight') { e.preventDefault(); stop(); goTo(idx + 1); if (autoplay) start(); }
        });

        /* Init */
        if (mode !== 'fade') {
            track.style.transform = 'translateX(0)';
        }
        activate(0);
        start();
    }

    function initAll() {
        var roots = document.querySelectorAll('.dn-carousel');
        for (var i = 0; i < roots.length; i++) {
            if (!roots[i].querySelector('.dn-arrow')) {
                initCarousel(roots[i]);
            }
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }
})();