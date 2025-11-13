package com.kraft.web;

import com.kraft.config.auth.dto.SessionUser;
import com.kraft.config.auth.LoginUser;
import com.kraft.service.PostService;
import com.kraft.web.dto.PostResponseDto;
import com.kraft.web.dto.PostSaveRequestDto;
import com.kraft.web.dto.PostUpdateRequestDto;
import com.kraft.web.dto.PostsListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @PostMapping("/api/post")
    public Long save(@RequestBody PostSaveRequestDto requestDto, @LoginUser SessionUser user) {
        String email = user != null ? user.getEmail() : null;
        return postService.save(requestDto, email);
    }

    @PutMapping("/api/post/{id}")
    public Long update(@PathVariable Long id, @RequestBody PostUpdateRequestDto requestDto, @LoginUser SessionUser user) {
        String email = user != null ? user.getEmail() : null;
        return postService.update(id, requestDto, email);
    }

    @DeleteMapping("/api/post/{id}")
    public Long delete(@PathVariable Long id, @LoginUser SessionUser user) {
        String email = user != null ? user.getEmail() : null;
        postService.delete(id, email);
        return id;
    }

    @GetMapping("/api/post/{id}")
    public PostResponseDto findById(@PathVariable Long id) {
        return postService.findById(id);
    }

    @GetMapping("/api/post/list")
    public List<PostsListResponseDto> findAll() {
        return postService.findAllDesc();
    }
}
