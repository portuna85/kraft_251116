package com.kraft.domain.post;

import com.kraft.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PostRepository 테스트
 * @Import(JpaConfig.class): JPA Auditing 활성화를 위해 JpaConfig import
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(JpaConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager em;

    @AfterEach
    void cleanup() {
        em.clear();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 저장 후 조회")
    void saveAndFindPost() {
        // given
        String title = "테스트 게시글";
        String content = "테스트 본문";
        String author = "test@example.com";

        postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build());

        // when
        List<Post> postList = postRepository.findAll();

        // then
        assertThat(postList).hasSize(1);
        Post post = postList.get(0);
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getAuthor()).isEqualTo(author);
    }

    @Test
    @DisplayName("게시글 ID로 조회")
    void findById() {
        // given
        Post savedPost = postRepository.save(Post.builder()
                .title("제목")
                .content("본문")
                .author("작성자")
                .build());

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.get().getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        // given
        Post savedPost = postRepository.save(Post.builder()
                .title("원본 제목")
                .content("원본 본문")
                .author("원본 작성자")
                .build());

        // when
        Post post = postRepository.findById(savedPost.getId()).orElseThrow();

        // then
        assertThat(post.getTitle()).isEqualTo("원본 제목");
        assertThat(post.getContent()).isEqualTo("원본 본문");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        // given
        Post savedPost = postRepository.save(Post.builder()
                .title("삭제할 게시글")
                .content("삭제할 본문")
                .author("삭제할 작성자")
                .build());

        // when
        postRepository.deleteById(savedPost.getId());

        // then
        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
        assertThat(deletedPost).isEmpty();
    }

    @Test
    @DisplayName("BaseTimeEntity 등록 시 생성일시와 수정일시 자동 설정")
    void baseTimeEntityRegistration() {
        // given
        LocalDateTime before = LocalDateTime.now().minusSeconds(5);

        // when
        Post savedPost = postRepository.saveAndFlush(Post.builder()
                .title("시간 테스트 제목")
                .content("시간 테스트 본문")
                .author("시간 테스트 작성자")
                .build());

        em.flush();
        em.clear();

        Post post = postRepository.findById(savedPost.getId()).orElseThrow();
        LocalDateTime after = LocalDateTime.now().plusSeconds(5);

        // then
        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getModifiedAt()).isNotNull();
        assertThat(post.getCreatedAt()).isBetween(before, after);
        assertThat(post.getModifiedAt()).isBetween(before, after);
        assertThat(post.getCreatedAt()).isBeforeOrEqualTo(post.getModifiedAt());
    }

    @Test
    @DisplayName("제목 없이 저장 시 예외 발생")
    void saveWithoutTitleThrowsException() {
        // given & when & then
        assertThrows(IllegalArgumentException.class, () -> {
            Post.builder()
                    .title(null)
                    .content("본문")
                    .author("작성자")
                    .build();
        });
    }

    @Test
    @DisplayName("본문 없이 저장 시 예외 발생")
    void saveWithoutContentThrowsException() {
        // given & when & then
        assertThrows(IllegalArgumentException.class, () -> {
            Post.builder()
                    .title("제목")
                    .content(null)
                    .author("작성자")
                    .build();
        });
    }

    @Test
    @DisplayName("여러 게시글 저장 후 전체 조회")
    void findAllPosts() {
        // given
        postRepository.save(Post.builder()
                .title("제목1")
                .content("본문1")
                .author("작성자1")
                .build());

        postRepository.save(Post.builder()
                .title("제목2")
                .content("본문2")
                .author("작성자2")
                .build());

        postRepository.save(Post.builder()
                .title("제목3")
                .content("본문3")
                .author("작성자3")
                .build());

        // when
        List<Post> postList = postRepository.findAll();

        // then
        assertThat(postList).hasSize(3);
        assertThat(postList)
                .extracting(Post::getTitle)
                .containsExactlyInAnyOrder("제목1", "제목2", "제목3");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 결과 반환")
    void findByNonExistentId() {
        // when
        Optional<Post> foundPost = postRepository.findById(999L);

        // then
        assertThat(foundPost).isEmpty();
    }
}

