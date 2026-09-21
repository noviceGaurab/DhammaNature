
    // Dynamically update event details based on query parameter
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('event');
    const images = {
        1: '/site-assets/pexels-pixabay-50709/image',
        2: '/site-assets/pexels-pixabay-236302/image',
        3: '/site-assets/pexels-pixabay-45178/image',
        4: '/site-assets/pexels-sibi-ar-3290250-4940194/image'
    };
    if (eventId && images[eventId]) {
        document.querySelector('.event-detail img').src = images[eventId];
    }
