package com.kraft.domain.user;

import com.kraft.domain.BaseEntity;
import com.kraft.domain.post.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * 양방향 연관관계: User가 작성한 Post 목록
     * - mappedBy: Post 엔티티의 'user' 필드에 의해 매핑됨 (연관관계의 주인은 Post)
     * - cascade: User 삭제 시 작성한 Post도 함께 삭제 (선택사항)
     * - orphanRemoval: User에서 Post 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Post> posts = new ArrayList<>();

    @Builder
    public User(String name, String email, String picture, Role role) {
        validateName(name);
        validateEmail(email);

        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = (role != null) ? role : Role.GUEST;
    }

    // ========== 비즈니스 로직 ==========

    public User update(String name, String picture) {
        validateName(name);

        this.name = name;
        this.picture = picture;

        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

    // ========== 검증 로직 ==========

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        // 간단한 이메일 형식 검증
        if (!email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }

    // ========== 연관관계 편의 메서드 ==========

    /**
     * Post 추가 편의 메서드
     * 양방향 연관관계 동기화
     */
    public void addPost(Post post) {
        this.posts.add(post);
        if (post.getUser() != this) {
            post.setUser(this);
        }
    }

    /**
     * Post 제거 편의 메서드
     * 양방향 연관관계 동기화
     */
    public void removePost(Post post) {
        this.posts.remove(post);
        if (post.getUser() == this) {
            post.setUser(null);
        }
    }
}
