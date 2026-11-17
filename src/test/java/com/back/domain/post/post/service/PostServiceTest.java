package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest         // 스프링을 띄운다 → PostService, PostRepository, DB 모두 진짜로 동작
@ActiveProfiles("test") // application-test.yaml 적용 → 메모리 DB
@Transactional          // 각 테스트가 끝나면 DB 변경을 되돌린다(롤백) → 테스트끼리 서로 영향 없음
public class PostServiceTest {
    @Autowired // 스프링이 관리하는 PostService 객체를 넣어 달라
    private PostService postService;

    @Test
    @DisplayName("처음에는 샘플 글 3개가 있다") // [1-4 수정] 0개 → 3개 (BaseInitData 가 샘플 글 3건을 만들기 때문)
    void t1() {
        assertThat(postService.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("write() 로 글을 저장하면 id 와 생성일이 채워진다")
    void t2() {
        Post post = postService.write("제목", "내용");

        // 1-2 의 PostTest.t2 에서는 null 이었던 값들이, DB에 저장한 뒤에는 채워져 있다
        assertThat(post.getId()).isNotNull();
        assertThat(post.getCreateDate()).isNotNull();
        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
    }

    @Test
    @DisplayName("write() 를 2번 하면 글 개수가 2개 늘어난다") // [1-4 수정] "2개가 된다" → "2개 늘어난다"
    void t3() {
        long before = postService.count(); // 샘플 데이터가 몇 개든 상관없도록, 저장 전 개수를 먼저 잰다

        postService.write("제목 1", "내용 1");
        postService.write("제목 2", "내용 2");

        assertThat(postService.count()).isEqualTo(before + 2);
    }
}
