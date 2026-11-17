package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.entity.PostComment;
import jakarta.validation.Valid;                       // [4-3 추가]
import jakarta.validation.constraints.NotBlank;        // [4-3 추가]
import jakarta.validation.constraints.Size;            // [4-3 추가]
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

// [4-1 추가] 댓글 API 입구
// 댓글은 항상 "어떤 글의" 댓글이므로 주소에 글 번호(postId)가 들어간다
//   예) POST /api/v1/posts/1/comments → 1번 글에 댓글 작성
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    // 댓글은 글을 통해서 만들기 때문에 PostService 를 사용한다
    private final PostService postService;

    // 요청 본문 {"content": "..."} 을 담을 상자
    // [4-3 추가] 검증 규칙 (2-3 글 작성과 같은 방식)
    //   @NotBlank : null, "", "   "(공백만) 모두 안 됨
    //   @Size     : 2 ~ 1000 글자
    record PostCommentWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 1000)
            String content
    ) {
    }

    // 댓글 작성: POST /api/v1/posts/{postId}/comments
    @PostMapping
    @ResponseStatus(CREATED) // 성공하면 201 Created
    public PostCommentDto write(
            @PathVariable long postId, // 주소의 {postId} → 몇 번 글에 달지
            // [4-3 추가] @Valid : 위의 검증 규칙을 검사한다. 어기면 메서드가 실행되지 않고 400 응답
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        // 댓글을 달 글을 먼저 찾는다. 없으면 404 (3-3 과 같은 방식)
        Post post = postService.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment postComment = postService.writeComment(post, reqBody.content());

        return new PostCommentDto(postComment); // 엔티티 → DTO 로 바꿔서 응답
    }

    // [5-1 추가] 댓글 삭제: DELETE /api/v1/posts/{postId}/comments/{id}
    // 예) DELETE /api/v1/posts/1/comments/2 → 1번 글의 2번 댓글 삭제
    @DeleteMapping("/{id}")
    public PostCommentDto delete(
            @PathVariable long postId, // 몇 번 글의
            @PathVariable long id      // 몇 번 댓글
    ) {
        // ① 글 찾기. 없으면 404
        Post post = postService.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "%d번 글은 존재하지 않습니다.".formatted(postId)));

        // ② 그 글의 댓글 중에서 찾기. 없으면 404
        PostComment postComment = post.findCommentById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "%d번 댓글은 존재하지 않습니다.".formatted(id)));

        // ③ 삭제
        postService.deleteComment(post, postComment);

        return new PostCommentDto(postComment); // 삭제된 댓글 정보를 응답 (상태코드 기본값 200)
    }
}
