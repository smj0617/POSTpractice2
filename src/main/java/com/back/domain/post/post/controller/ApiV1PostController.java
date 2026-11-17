package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    // [2-3 추가] 검증 규칙
    //   @NotBlank : null, "", "   "(공백만) 모두 안 됨
    //   @Size     : 글자 수 범위
    record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,

            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    // 글 작성: POST /api/v1/posts
    @PostMapping
    @ResponseStatus(CREATED) // 성공하면 201 Created (새로 만들어졌다는 뜻)
    public PostDto write(
            // 요청 본문의 JSON 을 PostWriteReqBody 로 변환
            // [2-3 추가] @Valid : 위의 검증 규칙을 검사한다. 어기면 메서드가 실행되지 않고 400 응답
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new PostDto(post); // 엔티티 → DTO 로 바꿔서 응답 (JSON 으로 변환됨)
    }

    // [3-1 추가] 글 삭제: DELETE /api/v1/posts/{id}
    // 예) DELETE /api/v1/posts/3 → 3번 글 삭제
    @DeleteMapping("/{id}")
    public PostDto delete(
            @PathVariable long id // 주소의 {id} 부분을 꺼내서 id 변수에 넣는다
    ) {
        Post post = postService.findById(id).get(); // 삭제할 글을 먼저 찾는다

        postService.delete(post);

        return new PostDto(post); // 삭제된 글의 정보를 응답 (상태코드는 기본값 200)
    }
}
