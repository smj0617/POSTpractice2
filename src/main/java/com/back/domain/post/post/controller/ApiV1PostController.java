package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

// HTTP 요청을 받는 입구
// 요청 → Controller → Service → Repository → DB
@RestController // 메서드가 돌려준 값을 JSON 으로 바꿔서 응답한다
@RequestMapping("/api/v1/posts") // 이 클래스의 모든 URL 앞에 /api/v1/posts 가 붙는다
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    // 요청 본문(JSON)을 담을 상자
    // {"title": "...", "content": "..."} → PostWriteReqBody(title, content)
    record PostWriteReqBody(
            String title,
            String content
    ) {
    }

    // 글 작성: POST /api/v1/posts
    @PostMapping
    @ResponseStatus(CREATED) // 성공하면 201 Created (새로 만들어졌다는 뜻)
    public PostDto write(
            @RequestBody PostWriteReqBody reqBody // 요청 본문의 JSON 을 PostWriteReqBody 로 변환
    ) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new PostDto(post); // 엔티티 → DTO 로 바꿔서 응답 (JSON 으로 변환됨)
    }
}
