package com.kraft.web;

import com.kraft.config.auth.dto.SessionUser;
import com.kraft.config.auth.LoginUser;
import com.kraft.service.PostService;
import com.kraft.web.dto.PostResponseDto;
import com.kraft.web.dto.PostSaveRequestDto;
import com.kraft.web.dto.PostUpdateRequestDto;
import com.kraft.web.dto.PostsListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 REST API 컨트롤러
 * 게시글 CRUD 작업을 처리합니다.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    /**
     * 게시글 생성
     * @param requestDto 게시글 생성 요청 데이터
     * @param user 현재 로그인한 사용자 (선택)
     * @return 생성된 게시글 ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long save(@RequestBody PostSaveRequestDto requestDto, @LoginUser SessionUser user) {
        String email = getUserEmail(user);
        return postService.save(requestDto, email);
    }

    /**
     * 게시글 수정
     * @param id 게시글 ID
     * @param requestDto 게시글 수정 요청 데이터
     * @param user 현재 로그인한 사용자 (선택)
     * @return 수정된 게시글 ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<Long> update(
            @PathVariable Long id,
            @RequestBody PostUpdateRequestDto requestDto,
            @LoginUser SessionUser user) {
        String email = getUserEmail(user);
        Long updatedId = postService.update(id, requestDto, email);
        return ResponseEntity.ok(updatedId);
    }

    /**
     * 게시글 삭제
     * @param id 게시글 ID
     * @param user 현재 로그인한 사용자 (선택)
     * @return 삭제된 게시글 ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> delete(@PathVariable Long id, @LoginUser SessionUser user) {
        String email = getUserEmail(user);
        postService.delete(id, email);
        return ResponseEntity.ok(id);
    }

    /**
     * 게시글 단건 조회
     * @param id 게시글 ID
     * @return 게시글 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> findById(@PathVariable Long id) {
        PostResponseDto post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 목록 조회 (최신순)
     * @return 게시글 목록
     */
    @GetMapping
    public ResponseEntity<List<PostsListResponseDto>> findAll() {
        List<PostsListResponseDto> posts = postService.findAllDesc();
        return ResponseEntity.ok(posts);
    }

    /**
     * 로그인한 사용자의 이메일 추출
     * @param user 세션 사용자 정보
     * @return 사용자 이메일 (로그인하지 않은 경우 null)
     */
    private String getUserEmail(SessionUser user) {
        return user != null ? user.getEmail() : null;
    }
}
