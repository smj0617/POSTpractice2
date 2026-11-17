package com.back.domain.post.postComment.dto;

import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;

// [4-1 추가] 응답으로 내보낼 댓글 데이터
// PostDto 와 같은 이유로 엔티티(PostComment)를 그대로 내보내지 않는다
//   - PostComment.post → Post.comments → PostComment.post → ... 순환참조 방지
// 글 전체 대신 "몇 번 글의 댓글인지(postId)"만 담는다
public record PostCommentDto(
        Long id,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        Long postId,
        String content
) {
    // PostComment 엔티티 → PostCommentDto 로 바꾸는 생성자
    public PostCommentDto(PostComment postComment) {
        this(
                postComment.getId(),
                postComment.getCreateDate(),
                postComment.getModifyDate(),
                postComment.getPost().getId(),
                postComment.getContent()
        );
    }
}
