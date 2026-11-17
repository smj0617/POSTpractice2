package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import jakarta.persistence.EntityManager; // [5-2 추가]
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // [5-2 추가]
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// [4-2 추가] 댓글 API 테스트 (ApiV1PostControllerTest 와 같은 구성)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional // 테스트가 끝나면 롤백 → 다른 테스트에 영향 없음
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    // [5-2 추가] JPA 가 객체를 관리하는 곳(영속성 컨텍스트)을 직접 다루는 도구
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("댓글 작성 - POST /api/v1/posts/1/comments 요청하면 201 과 작성된 댓글이 응답된다")
    void t1() throws Exception {
        // ① 요청 보내기 (4-1 의 curl 과 같은 요청)
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        // ② DB에서 1번 글의 댓글 목록 꺼내기 → 맨 마지막이 방금 작성한 댓글
        Post post = postService.findById(1).get();
        List<PostComment> comments = post.getComments();
        PostComment postComment = comments.getLast();

        // ③ 응답 검사
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class)) // 댓글 컨트롤러의
                .andExpect(handler().methodName("write"))                            // write 메서드가 처리
                .andExpect(status().isCreated())                                     // 201
                .andExpect(jsonPath("$.id").value(postComment.getId()))
                .andExpect(jsonPath("$.createDate").value(startsWith(postComment.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(startsWith(postComment.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.content").value("새 댓글"));

        // ④ DB 검사: 1번 글의 댓글이 3개 → 4개, 마지막 댓글이 요청한 내용
        assertThat(comments).hasSize(4);
        assertThat(postComment.getId()).isEqualTo(6); // 샘플 댓글 5개 다음
        assertThat(postComment.getContent()).isEqualTo("새 댓글");
        assertThat(postComment.getPost()).isSameAs(post); // 1번 글에 연결됨
    }

    // ---------- [4-3 추가] 실패 케이스 ----------

    @Test
    @DisplayName("댓글 작성 실패 - 내용이 비어 있으면 400 이고, 댓글이 저장되지 않는다")
    void t2() throws Exception {
        Post post = postService.findById(1).get();
        int countBefore = post.getComments().size(); // 3

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/1/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest()); // 400

        assertThat(post.getComments()).hasSize(countBefore); // 댓글 개수 그대로
    }

    @Test
    @DisplayName("댓글 작성 실패 - 없는 글(999번)에 댓글을 달면 404")
    void t3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/999/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "새 댓글"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isNotFound()); // 404
    }

    // ---------- [5-2 추가] 댓글 삭제 ----------

    @Test
    @DisplayName("댓글 삭제 - DELETE /api/v1/posts/1/comments/2 요청하면 200 과 삭제된 댓글이 응답되고, DB에서 사라진다")
    void t4() throws Exception {
        // ① 요청 보내기 (5-1 의 curl 과 같은 요청)
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1/comments/2")
                )
                .andDo(print());

        // ② 응답 검사
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk()) // 200
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.content").value("댓글 1-2"));

        // ③ 지금까지 모아 둔 SQL 을 DB로 보내고(flush), JPA 가 기억하던 객체를 모두 비운다(clear)
        //    → 테스트 전체가 @Transactional 이라 그냥 두면 DELETE 가 DB까지 가지 않은 채 롤백된다
        //    → clear 후 다시 조회하면 메모리가 아니라 진짜 DB에서 읽어 온다
        em.flush();
        em.clear();

        // ④ DB 검사: 1번 글의 댓글이 3개 → 2개, 2번 댓글은 없다
        Post post = postService.findById(1).get();
        assertThat(post.getComments()).hasSize(2);
        assertThat(post.getComments())
                .extracting(PostComment::getContent)
                .containsExactly("댓글 1-1", "댓글 1-3");
        assertThat(post.findCommentById(2)).isEmpty();
    }
}
