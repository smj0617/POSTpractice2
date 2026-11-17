package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // [3-2 추가]
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc // MockMvc 를 준비해 준다
@Transactional        // 테스트에서 작성한 글은 끝나면 롤백 → 다른 테스트에 영향 없음
public class ApiV1PostControllerTest {
    // MockMvc: 실제 서버(8080)를 켜지 않고, 컨트롤러에 HTTP 요청을 보낸 것처럼 흉내 내는 도구
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("글 작성 - POST /api/v1/posts 요청하면 201 과 작성된 글이 응답된다")
    void t1() throws Exception {
        // ① 요청 보내기 (2-1 의 curl 과 같은 요청)
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 글 제목",
                                            "content": "새 글 내용"
                                        }
                                        """)
                )
                .andDo(print()); // 요청/응답 내용을 콘솔에 출력 (테스트 리포트에서 확인 가능)

        // ② DB에 실제로 저장된 글 꺼내기 (샘플 글 1, 2, 3 다음이므로 4번)
        Post post = postService.findById(4).get();

        // ③ 응답 검사: 어떤 메서드가 처리했는지, 상태코드, 응답 JSON 의 모든 필드
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class)) // 이 컨트롤러가
                .andExpect(handler().methodName("write"))                     // write 메서드로 처리했다
                .andExpect(status().isCreated())                              // 201
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(startsWith(post.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(startsWith(post.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.title").value("새 글 제목"))
                .andExpect(jsonPath("$.content").value("새 글 내용"));

        // ④ DB 검사: 요청한 내용 그대로 저장되었는지
        assertThat(post.getTitle()).isEqualTo("새 글 제목");
        assertThat(post.getContent()).isEqualTo("새 글 내용");
    }

    // ---------- [2-3 추가] 실패 케이스 ----------

    @Test
    @DisplayName("글 작성 실패 - 제목이 비어 있으면 400 이고, 글이 저장되지 않는다")
    void t2() throws Exception {
        long countBefore = postService.count();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "content": "새 글 내용"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest()); // 400 : 요청이 잘못되었다

        assertThat(postService.count()).isEqualTo(countBefore); // 글 개수가 그대로
    }

    @Test
    @DisplayName("글 작성 실패 - 내용이 공백뿐이면 400 이고, 글이 저장되지 않는다")
    void t3() throws Exception {
        long countBefore = postService.count();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "새 글 제목",
                                            "content": "   "
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest());

        assertThat(postService.count()).isEqualTo(countBefore);
    }

    // ---------- [3-2 추가] 글 삭제 ----------

    @Test
    @DisplayName("글 삭제 - 댓글이 달린 1번 글을 DELETE 하면 200 과 삭제된 글이 응답되고, DB에서 사라진다")
    void t4() throws Exception {
        long countBefore = postService.count(); // 3

        // ① 요청 보내기: 1번 글(댓글 3개 달림) 삭제
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1")
                )
                .andDo(print());

        // ② 응답 검사
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())                   // 200
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("제목 1"))
                .andExpect(jsonPath("$.content").value("내용 1"));

        // ③ DB 검사: 글 개수가 1 줄었고, 1번 글은 더 이상 찾을 수 없다
        assertThat(postService.count()).isEqualTo(countBefore - 1);
        assertThat(postService.findById(1)).isEmpty();
    }
}
