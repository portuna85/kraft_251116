package com.kraft.service;

import com.kraft.domain.post.Post;
import com.kraft.domain.post.PostRepository;
import com.kraft.domain.user.User;
import com.kraft.domain.user.UserRepository;
import com.kraft.web.dto.PostResponseDto;
import com.kraft.web.dto.PostSaveRequestDto;
import com.kraft.web.dto.PostUpdateRequestDto;
import com.kraft.web.dto.PostsListResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;


    /**
     * 로그인된 사용자의 이메일(userEmail)이 전달되면 DB에서 User를 조회해 Post에 설정한 뒤 저장합니다.
     * userEmail이 null이면 익명 저장(도메인에서 허용하는 경우)으로 동작합니다.
     */
    @Transactional
    public Long save(PostSaveRequestDto requestDto, String userEmail) {
        Post post = requestDto.toEntity();

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("로그인 정보는 있으나 해당 이메일로 DB에서 사용자를 찾을 수 없습니다. 이메일='" + userEmail + "'. 먼저 User를 생성하세요."));
            post.setUser(user);
        }

        return postRepository.save(post).getId();
    }

    /**
     * 소유자 검증을 포함한 업데이트 (userEmail nullable)
     */
    @Transactional
    public Long update(Long id, PostUpdateRequestDto requestDto, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // 소유자 검증: 게시글에 user가 설정되어 있으면 로그인 사용자와 이메일 비교
        if (post.getUser() != null) {
            if (userEmail == null) {
                throw new AccessDeniedException("인증된 사용자만 이 게시글을 수정할 수 있습니다.");
            }
            String ownerEmail = post.getUser().getEmail();
            if (!ownerEmail.equals(userEmail)) {
                throw new AccessDeniedException("게시글 소유자가 아니므로 수정할 권한이 없습니다.");
            }
        }

        post.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }

    /**
     * 소유자 검증을 포함한 삭제
     */
    @Transactional
    public void delete(Long id, String userEmail) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        if (post.getUser() != null) {
            if (userEmail == null) {
                throw new AccessDeniedException("인증된 사용자만 이 게시글을 삭제할 수 있습니다.");
            }
            String ownerEmail = post.getUser().getEmail();
            if (!ownerEmail.equals(userEmail)) {
                throw new AccessDeniedException("게시글 소유자가 아니므로 삭제할 권한이 없습니다.");
            }
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(Long id) {
        Post entity = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));

        return new PostResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PostsListResponseDto> findAllDesc() {
        return postRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }
}