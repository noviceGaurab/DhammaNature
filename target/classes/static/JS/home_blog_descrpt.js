
        // Load comments from localStorage on page load
        let comments = JSON.parse(localStorage.getItem('comments')) || [
            { author: 'John Doe', text: 'Great article! Really helpful insights into building a healthy lifestyle.', timestamp: 'August 28, 2025 03:21 PM +0545', email: 'john.doe@example.com' },
            { author: 'Jane Smith', text: 'Thank you for sharing such valuable tips. Looking forward to more!', timestamp: 'August 28, 2025 03:22 PM +0545', email: 'jane.smith@example.com' }
        ];

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

            const timestamp = new Date().toLocaleString('en-US', { timeZone: 'Asia/Kathmandu', hour12: true });
            const newComment = { author: name, text: commentText, timestamp, email };
            comments.push(newComment);

            // Save comments and info to localStorage
            localStorage.setItem('comments', JSON.stringify(comments));
            if (saveInfo) {
                localStorage.setItem('commentName', name);
                localStorage.setItem('commentEmail', email);
            } else {
                localStorage.removeItem('commentName');
                localStorage.removeItem('commentEmail');
            }

            renderComments();
            closeCommentModal();
        }

        // Render comments to the DOM
        function renderComments() {
            const commentList = document.getElementById('commentList');
            commentList.innerHTML = '';
            comments.forEach(comment => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <div class="comment-author">
                        ${comment.author} <i class="fas fa-clock"></i><span style="font-size: 12px; color: #7f8c8d;">(${comment.timestamp})</span>
                    </div>
                    <p>${comment.text}</p>
                    <p class="reply-email">Posted by: ${comment.email}</p>
                    <button class="reply-btn"><i class="fas fa-reply"></i> Reply</button>
                `;
                commentList.appendChild(li);
            });
        }

        // Load saved info on page load
        window.addEventListener('load', () => {
            const savedName = localStorage.getItem('commentName');
            const savedEmail = localStorage.getItem('commentEmail');
            if (savedName) document.getElementById('nameInput').value = savedName;
            if (savedEmail) document.getElementById('emailInput').value = savedEmail;
            if (savedName && savedEmail) document.getElementById('saveInfo').checked = true;
            renderComments();
        });

        // Optional: Add event listener for Enter key
        document.getElementById('commentText').addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                submitComment();
            }
        });
