
    // Show comment modal
    function showCommentModal() {
        document.getElementById('commentModal').style.display = 'flex';
        document.getElementById('commentText').focus();
    }

    // Close comment modal
    function closeCommentModal() {
        document.getElementById('commentModal').style.display = 'none';
        document.getElementById('commentText').value = '';
        document.getElementById('nameInput').value = localStorage.getItem('commentName') || '';
        document.getElementById('emailInput').value = localStorage.getItem('commentEmail') || '';
        document.getElementById('saveInfo').checked = !!localStorage.getItem('commentName');
    }

    // Submit comment function
    function submitComment() {
        const commentText = document.getElementById('commentText').value.trim();
        const name = document.getElementById('nameInput').value.trim();
        const email = document.getElementById('emailInput').value.trim();
        const saveInfo = document.getElementById('saveInfo').checked;

        if (!commentText || !name || !email) {
            alert('Please fill in all required fields: Comment, Name, and Email.');
            return;
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            alert('Please enter a valid email address.');
            return;
        }

        const replyItem = document.createElement('div');
        replyItem.classList.add('reply-item');
        replyItem.innerHTML = `
            <span class="reply-author"><i class="fas fa-reply"></i> ${name}</span>
            <p class="reply-text">${commentText}</p>
            <p class="reply-email">Posted by: ${email}</p>
        `;
        document.querySelector('.replies-list').appendChild(replyItem);

        if (saveInfo) {
            localStorage.setItem('commentName', name);
            localStorage.setItem('commentEmail', email);
        } else {
            localStorage.removeItem('commentName');
            localStorage.removeItem('commentEmail');
        }

        closeCommentModal();
    }

    document.addEventListener('DOMContentLoaded', () => {
        // Load saved info on page load
        window.addEventListener('load', () => {
            const savedName = localStorage.getItem('commentName');
            const savedEmail = localStorage.getItem('commentEmail');
            if (savedName) document.getElementById('nameInput').value = savedName;
            if (savedEmail) document.getElementById('emailInput').value = savedEmail;
            if (savedName && savedEmail) document.getElementById('saveInfo').checked = true;
        });

        // Hover effects for navigation
        document.querySelectorAll('.nav-item, .category-item, .tag-item').forEach(item => {
            item.addEventListener('mouseenter', () => {
                item.style.backgroundColor = '#f0f0f0';
            });
            item.addEventListener('mouseleave', () => {
                item.style.backgroundColor = 'transparent';
            });
        });

        // Handle click events for nav items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const itemText = item.querySelector('span').textContent.toLowerCase();
                if (itemText === 'roles') {
                    window.location.href = 'users_variety.jsp';
                } else {
                    console.log(`${itemText} clicked`);
                }
            });
        });

        // Topic row click event
        document.querySelectorAll('.topic-row').forEach(row => {
            row.addEventListener('click', () => {
                console.log('Topic clicked');
            });
        });
    });
