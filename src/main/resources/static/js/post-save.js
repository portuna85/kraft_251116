// Post Save JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('postForm');

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        const title = document.getElementById('title').value.trim();
        const author = document.getElementById('author').value.trim();
        const content = document.getElementById('content').value.trim();

        // 유효성 검사
        if (!title) {
            alert('제목을 입력해주세요.');
            document.getElementById('title').focus();
            return;
        }

        if (!author) {
            alert('작성자를 입력해주세요.');
            document.getElementById('author').focus();
            return;
        }

        if (!content) {
            alert('내용을 입력해주세요.');
            document.getElementById('content').focus();
            return;
        }

        // 게시글 데이터
        const postData = {
            title: title,
            author: author,
            content: content
        };

        // 버튼 비활성화 (중복 클릭 방지)
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loading"></span> 등록 중...';

        // API 호출
        fetch('/api/posts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(postData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('게시글 등록에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            alert('게시글이 등록되었습니다.');
            window.location.href = '/';
        })
        .catch(error => {
            console.error('Error:', error);
            alert(error.message || '게시글 등록에 실패했습니다.');

            // 버튼 재활성화
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
    });
});

