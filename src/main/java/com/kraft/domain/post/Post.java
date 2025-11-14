package com.kraft.domain.post;

import com.kraft.domain.BaseEntity;
import com.kraft.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String author;

    /**
     * 다대일 연관관계: Post -> User
     * - fetch = LAZY: 지연 로딩 (N+1 문제 방지)
     * - 연관관계의 주인: Post (외래키를 관리)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Post(String title, String content, String author, User user) {
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
        this.author = author;
        // user는 연관관계 편의 메서드로 설정하는 것이 더 안전
        if (user != null) {
            this.setUser(user);
        }
    }

    // ========== 비즈니스 로직 ==========

    public void update(String title, String content) {
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
    }

    // ========== 검증 로직 ==========

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 제목은 필수입니다.");
        }
        if (title.length() > 500) {
            throw new IllegalArgumentException("게시글 제목은 500자를 초과할 수 없습니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 내용은 필수입니다.");
        }
    }

    /**
     * 연관관계 편의 메서드: User와 Post 양쪽을 동시에 설정
     * - 기존 User에서 이 Post 제거
     * - 새로운 User에 이 Post 추가
     * - 양방향 연관관계 동기화 보장
     */
    public void setUser(User user) {
        // 기존 연관관계 제거
        if (this.user != null) {
            this.user.getPosts().remove(this);
        }

        // 새로운 연관관계 설정
        this.user = user;

        // 양방향 연관관계 동기화
        if (user != null && !user.getPosts().contains(this)) {
            user.getPosts().add(this);
        }
    }

    /**
     * 연관관계 제거 편의 메서드
     */
    public void removeUser() {
        if (this.user != null) {
            this.user.getPosts().remove(this);
            this.user = null;
        }
    }
}
