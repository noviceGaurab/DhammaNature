
document.getElementById('signupForm').addEventListener('submit', function(e) {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const acceptTerms = document.querySelector('input[name="acceptTerms"]').checked;
    const errorMessage = document.getElementById('errorMessage');

    errorMessage.textContent = ''; // Clear previous errors

    if (!email || !password || !acceptTerms) {
        e.preventDefault();
        errorMessage.textContent = 'Please fill all required fields and accept the Terms of Service.';
        return;
    }

    if (!validateEmail(email)) {
        e.preventDefault();
        errorMessage.textContent = 'Please enter a valid email address.';
        return;
    }

    if (!validatePassword(password)) {
        e.preventDefault();
        errorMessage.textContent = 'Password must be at least 8 characters long.';
        return;
    }

    console.log('Form valid, submitting to server:', { email, password, acceptTerms });
    // Form submits to action="signup" if validation passes
});

function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePassword(password) {
    return password.length >= 8;
}

// Add error message element on page load
document.addEventListener('DOMContentLoaded', function() {
    const formSection = document.querySelector('.form-section');
    const errorDiv = document.createElement('p');
    errorDiv.id = 'errorMessage';
    errorDiv.style.color = 'red';
    formSection.insertBefore(errorDiv, formSection.querySelector('form'));
});
