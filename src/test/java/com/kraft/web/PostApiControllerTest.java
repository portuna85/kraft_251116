package com.kraft.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraft.config.TestSecurityConfig;
import com.kraft.domain.post.Post;
import com.kraft.domain.post.PostRepository;
import com.kraft.domain.user.UserRepository;
import com.kraft.web.dto.PostSaveRequestDto;
import com.kraft.web.dto.PostUpdateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PostApiController 통합 테스트
 * - Spring Security 포함 (TestSecurityConfig 사용)
 * - 실제 HTTP 요청/응답 검증
 * - CRUD 전체 기능 테스트
 * - OAuth2는 test 프로파일에서 비활성화됨
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class PostApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;


    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 등록 - 성공")
    @WithMockUser(roles = "USER")
    void savePost() throws Exception {
        // given
        String title = "테스트 제목";
        String content = "테스트 본문";
        String author = "테스트 작성자";

        PostSaveRequestDto requestDto = PostSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        // 세션에 로그인 사용자 추가
        com.kraft.domain.user.User savedUser = userRepository.save(com.kraft.domain.user.User.builder()
                .name("테스트유저")
                .email("testuser@example.com")
                .picture(null)
                .role(com.kraft.domain.user.Role.USER)
                .build());

        com.kraft.config.auth.dto.SessionUser sessionUser = new com.kraft.config.auth.dto.SessionUser(savedUser);

        String url = "/api/post";

        // when & then
        mvc.perform(post(url)
                        .sessionAttr("user", sessionUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());

        // verify
        List<Post> all = postRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all).first().satisfies(p -> {
            assertThat(p.getTitle()).isEqualTo(title);
            assertThat(p.getContent()).isEqualTo(content);
            assertThat(p.getAuthor()).isEqualTo(author);
        });
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    @WithMockUser(roles = "USER")
    void updatePost() throws Exception {
        // given
        Post savedPost = postRepository.save(Post.builder()
                .title("원본 제목")
                .content("원본 본문")
                .author("원본 작성자")
                .build());

        Long updateId = savedPost.getId();
        String expectedTitle = "수정된 제목";
        String expectedContent = "수정된 본문";

        PostUpdateRequestDto requestDto = PostUpdateRequestDto.builder()
                .title(expectedTitle)
                .content(expectedContent)
                .build();

        String url = "/api/post/" + updateId;

        // when & then
        mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(updateId)));

        // verify
        List<Post> all = postRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all).first().satisfies(p -> {
            assertThat(p.getTitle()).isEqualTo(expectedTitle);
            assertThat(p.getContent()).isEqualTo(expectedContent);
        });
    }

    @Test
    @DisplayName("게시글 조회 - 성공")
    @WithMockUser(roles = "USER")
    void findById() throws Exception {
        // given
        String title = "조회 테스트 제목";
        String content = "조회 테스트 본문";
        String author = "조회 테스트 작성자";

        Post savedPost = postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build());

        String url = "/api/post/" + savedPost.getId();

        // when & then
        mvc.perform(get(url))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.author").value(author));
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    @WithMockUser(roles = "USER")
    void deletePost() throws Exception {
        // given
        Post savedPost = postRepository.save(Post.builder()
                .title("삭제 테스트 제목")
                .content("삭제 테스트 본문")
                .author("삭제 테스트 작성자")
                .build());

        Long deleteId = savedPost.getId();
        String url = "/api/post/" + deleteId;

        // when & then
        mvc.perform(delete(url))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(deleteId)));

        // verify
        List<Post> all = postRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    @WithMockUser(roles = "USER")
    void findAll() throws Exception {
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

        String url = "/api/post/list";

        // when & then
        mvc.perform(get(url))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("게시글 등록 - 인증 없이 실패")
    void savePostWithoutAuth() throws Exception {
        // given
        PostSaveRequestDto requestDto = PostSaveRequestDto.builder()
                .title("제목")
                .content("본문")
                .author("작성자")
                .build();

        String url = "/api/post";

        // when & then
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
