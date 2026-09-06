
    // Dynamically update event details based on query parameter
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('event');
    const images = {
        1: 'assets/images/pexels-pixabay-50709.jpg',
        2: 'assets/images/pexels-pixabay-236302.jpg',
        3: 'assets/images/pexels-pixabay-45178.jpg',
        4: 'assets/images/pexels-sibi-ar-3290250-4940194.jpg'
    };
    if (eventId && images[eventId]) {
        document.querySelector('.event-detail img').src = images[eventId];
    }
