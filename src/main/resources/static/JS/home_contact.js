
        function onClickSubmit() {
            console.log("Login successfully.");
            const name = document.getElementById("Name").value;
    const email = document.getElementById("Email").value;
    const subject = document.getElementById("Subject").value;
    const message = document.getElementById("Message").value;

    console.log (`Name: ${name}`);
    console.log (`Email: ${email}`);
  }

  const toggleBtn = document.querySelector('.theme-toggle');
  toggleBtn.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
  });

  // Form validation
  const form = document.getElementById('contactForm');

  form.addEventListener('submit', function (e) {
    e.preventDefault(); // Prevent form submission

    // Get values
    const name = document.getElementById('Name').value.trim();
    const email = document.getElementById('Email').value.trim();
    const subject = document.getElementById('Subject').value.trim();
    const message = document.getElementById('Message').value.trim();

    // Clear errors
    document.getElementById('NameError').innerText = '';
    document.getElementById('EmailError').innerText = '';
    document.getElementById('SubjectError').innerText = '';
    document.getElementById('MessageError').innerText = '';

    let hasError = false;

    // Validate Name
    if (name === '') {
      document.getElementById('NameError').innerText = 'Please enter your name.';
      hasError = true;
    }

    // Validate Email
    if (email === '') {
      document.getElementById('EmailError').innerText = 'Please enter your email.';
      hasError = true;
    } else if (!email.includes('@')) {
      document.getElementById('EmailError').innerText = 'Email must contain @.';
      hasError = true;
    }

    // Validate Subject
    if (subject === '') {
      document.getElementById('SubjectError').innerText = 'Please enter a subject.';
      hasError = true;
    }

    // Validate Message
    if (message === '') {
      document.getElementById('MessageError').innerText = 'Please enter a message.';
      hasError = true;
    }

    // Example switch usage: show log based on subject
    switch(subject.toLowerCase()) {
      case 'hello':
        console.log('User said hello.');
        break;
      case 'help':
        console.log('User needs help.');
        break;
      default:
        console.log('General message subject.');
    }

    // If no errors, submit or do something
    if (!hasError) {
      alert('Form submitted successfully!');
      form.reset();
    }
  });
