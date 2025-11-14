// Post Update JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('updateForm');

    form.addEventListener('submit', function(e) {
        e.preventDefault();

        const id = document.getElementById('id').value;
        const title = document.getElementById('title').value.trim();
        const content = document.getElementById('content').value.trim();

        // 유효성 검사
        if (!title) {
            alert('제목을 입력해주세요.');
            document.getElementById('title').focus();
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
            content: content
        };

        // 버튼 비활성화 (중복 클릭 방지)
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="loading"></span> 수정 중...';

        // API 호출
        fetch(`/api/posts/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(postData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('게시글 수정에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            alert('게시글이 수정되었습니다.');
            window.location.href = '/';
        })
        .catch(error => {
            console.error('Error:', error);
            alert(error.message || '게시글 수정에 실패했습니다.');

            // 버튼 재활성화
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
    });
});

// 삭제 함수
function deletePost() {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    const id = document.getElementById('id').value;

    fetch(`/api/posts/${id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('삭제에 실패했습니다.');
        }
        return response.json();
    })
    .then(() => {
        alert('삭제되었습니다.');
        window.location.href = '/';
    })
    .catch(error => {
        console.error('Error:', error);
        alert('삭제에 실패했습니다.');
    });
}

