package com.kraft.web;

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
    public Long save(@RequestBody PostSaveRequestDto requestDto) {
        return postService.save(requestDto);
    }

    @PutMapping("/api/post/{id}")
    public Long update(@PathVariable Long id, @RequestBody PostUpdateRequestDto requestDto) {
        return postService.update(id, requestDto);
    }

    @DeleteMapping("/api/post/{id}")
    public Long delete(@PathVariable Long id) {
        postService.delete(id);
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
