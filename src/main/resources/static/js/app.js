// Main Application JavaScript

// 게시글 상세보기 모달
function viewPost(postId) {
    fetch(`/api/posts/${postId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('게시글을 불러올 수 없습니다.');
            }
            return response.json();
        })
        .then(post => {
            document.getElementById('modalTitle').textContent = post.title;
            document.getElementById('modalAuthor').textContent = `작성자: ${post.author}`;
            document.getElementById('modalContent').textContent = post.content;

            // 날짜 정보가 있다면 표시 (응답에 날짜 필드가 있을 경우)
            const dateElement = document.getElementById('modalDate');
            if (dateElement) {
                dateElement.textContent = ''; // 현재 DTO에 날짜 없음
            }

            // 모달 표시
            document.getElementById('postModal').style.display = 'block';
        })
        .catch(error => {
            console.error('Error:', error);
            alert('게시글을 불러오는데 실패했습니다.');
        });
}

// 모달 닫기
function closeModal() {
    document.getElementById('postModal').style.display = 'none';
}

// 모달 외부 클릭시 닫기
window.onclick = function (event) {
    const modal = document.getElementById('postModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        closeModal();
    }
});

// 게시글 삭제
function deletePost(postId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    fetch(`/api/posts/${postId}`, {
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
            location.reload();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('삭제에 실패했습니다.');
        });
}

// 페이지 로드시 초기화
document.addEventListener('DOMContentLoaded', function () {
    console.log('Kraft Application Loaded');
});

