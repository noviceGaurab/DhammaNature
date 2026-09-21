
  function handleLogin() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
      alert('Please fill in all fields');
      return;
    }

    window.location.href = 'home.html'; // Replace 'dashboard.html' with the URL or path of the target pag
  }

  function handleGoogleLogin() {
    console.log('Google login attempt');
  }

  document.querySelectorAll('.input-group input').forEach(input => {
    input.addEventListener('blur', function() {
      if (this.value.trim() === '') {
        this.style.borderColor = '#ff6b6b';
        this.previousElementSibling.style.color = '#ff6b6b';
      } else {
        this.style.borderColor = '#277946';
        this.previousElementSibling.style.color = '#277946';
      }
    });
  });

  function showNewPasswordForm() {
      document.getElementById('newPasswordModal').style.display = 'block';
  }

  function hideNewPasswordForm() {
      document.getElementById('newPasswordModal').style.display = 'none';
  }

  function saveNewPassword() {
      const email = document.getElementById('email').value; // From login form
      const newPassword = document.getElementById('newPassword').value;
      const confirmPassword = document.getElementById('confirmPassword').value;

      if (!email) {
          alert('Please enter your email in the login form first.');
          return;
      }

      if (newPassword !== confirmPassword) {
          alert('Passwords do not match!');
          return;
      }

      // Send data to ForgotPasswordServlet via AJAX
      const xhr = new XMLHttpRequest();
      xhr.open('POST', '${pageContext.request.contextPath}/ForgotPasswordServlet', true);
      xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
      xhr.onreadystatechange = function () {
          if (xhr.readyState === 4 && xhr.status === 200) {
              alert(xhr.responseText); // Show servlet response
              hideNewPasswordForm(); // Close modal
          }
      };
      const data = 'email=' + encodeURIComponent(email) + '&newPassword=' + encodeURIComponent(newPassword);
      xhr.send(data);
  }

  function handleGoogleLogin() {
      alert('Google login not implemented yet.');
  }